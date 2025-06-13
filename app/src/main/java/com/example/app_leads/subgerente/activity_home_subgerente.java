package com.example.app_leads.subgerente;

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

public class activity_home_subgerente extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home_subgerente);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_subgerente), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Recuperar el objeto User
        User user = (User) getIntent().getSerializableExtra("user");

        // Referencias UI
        TextView tvWelcome    = findViewById(R.id.tv_welcome);
        TextView tvRole       = findViewById(R.id.tv_role);
        MaterialButton btnVerLeads = findViewById(R.id.btn_ver_leads);
        MaterialButton btnLogout   = findViewById(R.id.btn_logout);

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

        // Cerrar sesión
        btnLogout.setOnClickListener(v -> {
            // Borrar token de SharedPreferences
            getSharedPreferences("prefs", MODE_PRIVATE)
                    .edit()
                    .remove("auth_token")
                    .apply();

            // Volver al login y limpiar la pila
            Intent i = new Intent(this, activity_login.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
            finish();
        });
    }
}