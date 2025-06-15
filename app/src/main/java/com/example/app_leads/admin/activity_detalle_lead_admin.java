package com.example.app_leads.admin;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.app_leads.R;
import com.example.app_leads.model.Lead;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class activity_detalle_lead_admin extends AppCompatActivity {

    private TextView tvEmpresa, tvRuc, tvId, tvFecha, tvEstado, tvSituacion, tvDetalle, tvEjecutivo, tvSubgerente;
    private TextView tvContactoNombre, tvContactoEmail, tvContactoTelefono;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_detalle_lead_admin);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tvEmpresa = findViewById(R.id.tv_empresa);
        tvRuc = findViewById(R.id.tv_ruc);
        tvId = findViewById(R.id.tv_id);
        tvFecha = findViewById(R.id.tv_fecha);
        tvEstado = findViewById(R.id.tv_estado);
        tvSituacion = findViewById(R.id.tv_situacion);
        tvDetalle = findViewById(R.id.tv_detalle);
        tvEjecutivo = findViewById(R.id.tv_ejecutivo);
        tvSubgerente = findViewById(R.id.tv_subgerente);

        tvContactoNombre = findViewById(R.id.tv_contacto_nombre);
        tvContactoEmail = findViewById(R.id.tv_contacto_email);
        tvContactoTelefono = findViewById(R.id.tv_contacto_telefono);

        Lead lead = (Lead) getIntent().getSerializableExtra("lead");
        if (lead != null) {
            tvEmpresa.setText(lead.getEmpresa());
            tvRuc.setText("RUC: " + lead.getRuc());
            tvId.setText("ID: " + lead.getId());
            tvFecha.setText("Fecha: " + lead.getFechaRegistro());
            tvEstado.setText("Estado: " + lead.getEstado());
            tvSituacion.setText("Situación: " + lead.getSituacion());
            tvDetalle.setText("Detalle: " + lead.getDetalle());
            tvEjecutivo.setText("Ejecutivo: " + lead.getEjecutivoName());
            tvSubgerente.setText("Subgerente: " + lead.getSubgerenteName());

            obtenerDatosContacto(lead.getIdContacto());
        }
    }

    private void obtenerDatosContacto(String idContacto) {
        try {
            String idEncoded = URLEncoder.encode(idContacto, StandardCharsets.UTF_8.toString());

            SharedPreferences prefs = getSharedPreferences("APP_LEADS_PREFS", MODE_PRIVATE);
            String token = prefs.getString("ACCESS_TOKEN", null);

            if (token == null || token.isEmpty()) return;

            String url = "https://lead.gtm.net.pe/api/contactos/" + idEncoded + "/";

            JsonObjectRequest req = new JsonObjectRequest(
                    Request.Method.GET, url, null,
                    resp -> {
                        try {
                            if (!"ok".equalsIgnoreCase(resp.getString("status"))) return;

                            JSONArray arr = resp.getJSONArray("data");
                            if (arr.length() > 0) {
                                JSONObject obj = arr.getJSONObject(0);
                                tvContactoNombre.setText("Nombre contacto: " + obj.optString("nombre", ""));
                                tvContactoEmail.setText("Correo contacto: " + obj.optString("email", ""));
                                tvContactoTelefono.setText("Teléfono contacto: " + obj.optString("telefono", ""));
                            }
                        } catch (Exception e) {
                            Log.e("detalle_contacto", "JSON error", e);
                        }
                    },
                    err -> Log.e("detalle_contacto", "Volley error", err)
            ) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Authorization", "Bearer " + token);
                    headers.put("Content-Type", "application/json; charset=utf-8");
                    return headers;
                }
            };

            Volley.newRequestQueue(this).add(req);

        } catch (Exception e) {
            Log.e("detalle_contacto", "Encoding error", e);
        }
    }
}