package com.example.app_leads.admin;

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
import com.example.app_leads.model.User;
import com.google.android.material.button.MaterialButton;

public class activity_home_admin extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home_admin);

        // Insets para pantalla completa
        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.main_admin),
                (v, insets) -> {
                    Insets sys = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                    v.setPadding(sys.left, sys.top, sys.right, sys.bottom);
                    return insets;
                }
        );

        // Recuperar el objeto User
        User user = (User) getIntent().getSerializableExtra("user");

        // Referencias UI
        TextView tvWelcome         = findViewById(R.id.tv_welcome);
        TextView tvRole            = findViewById(R.id.tv_role);
        MaterialButton btnVerLeads = findViewById(R.id.btn_ver_leads);
        MaterialButton btnAsignarAtencion = findViewById(R.id.btn_asignar_atencion);
        MaterialButton btnLogout         = findViewById(R.id.btn_logout);

        // Mostrar nombre y rol
        if (user != null) {
            tvWelcome.setText("Bienvenido " + user.getFullName());
            tvRole   .setText("Rol: " + user.getRole());
        }

        // Navegar a la pantalla de leads
        btnVerLeads.setOnClickListener(v -> {
            Intent i = new Intent(this, activity_lead_admin.class);
            i.putExtra("user", user);
            startActivity(i);
        });

        // Navegar a la pantalla de asignar atención
        btnAsignarAtencion.setOnClickListener(v -> {
            Intent i = new Intent(this, activity_asignar_atencion.class);
            i.putExtra("user", user);
            startActivity(i);
        });

        // Cerrar sesión
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
