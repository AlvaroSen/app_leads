package com.example.app_leads;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.app_leads.admin.activity_home_admin;
import com.example.app_leads.ejecutivo.activity_home_ejecutivo;
import com.example.app_leads.model.User;
import com.example.app_leads.subgerente.activity_home_subgerente;
import com.example.app_leads.api.api_config;

import org.json.JSONException;
import org.json.JSONObject;

public class activity_login extends AppCompatActivity {

    private EditText etUsuario;
    private EditText etClave;
    private Button  btnLogin;
    private RequestQueue queue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Habilita Edge-to-Edge para dibujar tras la barra de estado
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        // Aplica los Insets del sistema para ajustar padding
        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.main),
                (v, insets) -> {
                    Insets sys = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                    v.setPadding(sys.left, sys.top, sys.right, sys.bottom);
                    return insets;
                }
        );

        // Inicializa la cola de peticiones de Volley
        queue = Volley.newRequestQueue(this);

        // Referencias a los campos UI
        etUsuario = findViewById(R.id.input_username);
        etClave   = findViewById(R.id.input_password);
        btnLogin  = findViewById(R.id.boton_login);

        // Asigna el listener para cuando el usuario pulse Iniciar Sesión
        btnLogin.setOnClickListener(v -> login());
    }

    /**
     * Método que se encarga de realizar el login contra el API.
     */
    private void login() {
        // 1) Lee y valida los campos de usuario y clave
        String usuario = etUsuario.getText().toString().trim();
        String clave   = etClave.getText().toString().trim();
        if (usuario.isEmpty() || clave.isEmpty()) {
            Toast.makeText(this, "Completa ambos campos", Toast.LENGTH_SHORT).show();
            return;
        }

        // 2) Construye el JSON con las credenciales
        JSONObject body = new JSONObject();
        try {
            body.put("username", usuario);
            body.put("clave",   clave);
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error construyendo la petición", Toast.LENGTH_SHORT).show();
            return;
        }

        // 3) Crea la petición POST a la URL de login
        JsonObjectRequest req = new JsonObjectRequest(
                Request.Method.POST,
                api_config.LOGIN,
                body,
                resp -> {
                    try {
                        // 4) Comprueba el campo "status" de la respuesta
                        if (!"ok".equalsIgnoreCase(resp.getString("status"))) {
                            Toast.makeText(this,
                                    resp.optString("mensaje", "Usuario o contraseña inválidos"),
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // 5) Verifica que el usuario esté activo
                        boolean isActive = resp.optBoolean("is_active", true);
                        if (!isActive) {
                            Toast.makeText(this,
                                    "Usuario inactivo. Contacta con el administrador.",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // 6) Extrae y almacena el token de autenticación
                        String token = resp.optString("token", "");
                        getSharedPreferences("prefs", MODE_PRIVATE)
                                .edit()
                                .putString("auth_token", token)
                                .apply();

                        // 7) Crea el objeto User con los datos del JSON
                        User user = new User(
                                resp.optString("usuario"),
                                resp.optString("nombre"),
                                resp.optString("role"),
                                isActive,
                                token
                        );

                        // 8) Decide a qué pantalla navegar según el rol
                        Intent intent;
                        switch (user.getRole().toLowerCase()) {
                            case "admin":
                                intent = new Intent(this, activity_home_admin.class);
                                break;
                            case "Ejecutivo":
                                intent = new Intent(this, activity_home_ejecutivo.class);
                                break;
                            case "Subgerente":
                                intent = new Intent(this, activity_home_subgerente.class);
                                break;
                            default:
                                // Por defecto, vamos a Ejecutivo
                                intent = new Intent(this, activity_home_ejecutivo.class);
                        }

                        // 9) Pasa el User completo a la siguiente Activity
                        intent.putExtra("user", user);
                        startActivity(intent);
                        finish();

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this,
                                "Respuesta inesperada del servidor",
                                Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    // 10) Manejo de errores de la petición (red, servidor, etc.)
                    Toast.makeText(this,
                            "Error de conexión: " + error.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
        ) {
            @Override
            public String getBodyContentType() {
                // Indica que enviamos JSON en UTF-8
                return "application/json; charset=utf-8";
            }
        };

        // 11) Encola la petición para que se ejecute
        queue.add(req);
    }
}
