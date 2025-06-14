package com.example.app_leads;

import android.content.Intent;
import android.content.SharedPreferences;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.main),
                (v, insets) -> {
                    Insets sys = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                    v.setPadding(sys.left, sys.top, sys.right, sys.bottom);
                    return insets;
                }
        );

        etUsuario = findViewById(R.id.input_username);
        etClave   = findViewById(R.id.input_password);
        btnLogin  = findViewById(R.id.boton_login);

        btnLogin.setOnClickListener(v -> login());
    }

    private void login() {
        String usuario = etUsuario.getText().toString().trim();
        String clave   = etClave.getText().toString().trim();
        if (usuario.isEmpty() || clave.isEmpty()) {
            Toast.makeText(this, "Completa ambos campos", Toast.LENGTH_SHORT).show();
            return;
        }

        JSONObject body = new JSONObject();
        try {
            body.put("username", usuario);
            body.put("clave",   clave);
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error construyendo la petición", Toast.LENGTH_SHORT).show();
            return;
        }

        JsonObjectRequest req = new JsonObjectRequest(
                Request.Method.POST,
                api_config.LOGIN,
                body,
                resp -> {
                    try {
                        // 1) Status OK?
                        if (!"ok".equalsIgnoreCase(resp.getString("status"))) {
                            Toast.makeText(this,
                                    resp.optString("mensaje", "Usuario o contraseña inválidos"),
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // 2) Usuario activo?
                        boolean isActive = resp.optBoolean("is_active", true);
                        if (!isActive) {
                            Toast.makeText(this,
                                    "Usuario inactivo. Contacta con el administrador.",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // 3) Extraemos campos
                        String usuarioResp   = resp.optString("usuario", "");
                        String nombre        = resp.optString("nombre", "");
                        String role          = resp.optString("role", "");
                        String accessToken   = resp.optString("access", "");
                        String refreshToken  = resp.optString("refresh", "");

                        // 4) Guardamos tokens en SharedPreferences
                        SharedPreferences prefs = getSharedPreferences("APP_LEADS_PREFS", MODE_PRIVATE);
                        prefs.edit()
                                .putString("ACCESS_TOKEN", accessToken)
                                .putString("REFRESH_TOKEN", refreshToken)
                                .apply();

                        // 5) Creamos el usuario y navegamos según rol
                        User user = new User(
                                usuarioResp,
                                nombre,
                                role,
                                isActive,
                                accessToken
                        );

                        Intent intent;
                        switch (role.toLowerCase()) {
                            case "admin":
                                intent = new Intent(this, activity_home_admin.class);
                                break;
                            case "ejecutivo":
                                intent = new Intent(this, activity_home_ejecutivo.class);
                                break;
                            case "subgerente":
                                intent = new Intent(this, activity_home_subgerente.class);
                                break;
                            default:
                                intent = new Intent(this, activity_home_ejecutivo.class);
                        }
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
                error -> Toast.makeText(this,
                        "Error de conexión: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show()
        ) {
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }
        };

        Volley.newRequestQueue(this).add(req);
    }
}
