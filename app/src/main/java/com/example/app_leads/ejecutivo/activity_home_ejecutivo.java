package com.example.app_leads.ejecutivo;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.app_leads.R;
import com.example.app_leads.activity_login;
import com.example.app_leads.admin.activity_lead_admin;
import com.example.app_leads.model.User;
import com.google.android.material.button.MaterialButton;

public class activity_home_ejecutivo extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Edge-to-edge
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home_ejecutivo);

        // Aplica insets al root con el ID correcto
        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.main_ejecutivo),    // ← aquí el ID coincide con el XML
                (v, insets) -> {
                    Insets sys = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                    v.setPadding(sys.left, sys.top, sys.right, sys.bottom);
                    return insets;
                }
        );

        // Recuperar el objeto User pasado
        User user = (User) getIntent().getSerializableExtra("user");

        // Referencias a la UI
        TextView tvWelcome    = findViewById(R.id.tv_welcome);
        TextView tvRole       = findViewById(R.id.tv_role);
        MaterialButton btnVerLeads = findViewById(R.id.btn_ver_leads);
        MaterialButton btnLogout   = findViewById(R.id.btn_logout);

        // Mostrar nombre y rol dinámicamente
        if (user != null) {
            tvWelcome.setText("Bienvenido " + user.getFullName());
            tvRole   .setText("Rol: " + user.getRole());
        }

        // Acción del botón Ver Leads
        btnVerLeads.setOnClickListener(v -> {
            Intent i = new Intent(this, activity_lead_admin.class);
            i.putExtra("user", user);
            startActivity(i);
        });

        // Acción del botón Cerrar sesión
        btnLogout.setOnClickListener(v -> {
            getSharedPreferences("prefs", MODE_PRIVATE)
                    .edit()
                    .remove("auth_token")
                    .apply();

            Intent i = new Intent(this, activity_login.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
            finish();
        });
    }
}
