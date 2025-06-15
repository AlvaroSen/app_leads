package com.example.app_leads.admin;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.app_leads.R;
import com.example.app_leads.activity_login;
import com.example.app_leads.api.api_config;
import com.example.app_leads.model.Lead;
import com.example.app_leads.model.Lead_adapter;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class activity_asignar_atencion extends AppCompatActivity {

    private EditText       etBuscarIdLead;
    private RecyclerView   rvLeads;
    private Lead_adapter   adapter;
    private List<Lead>     allLeads      = new ArrayList<>();
    private List<Lead>     filteredLeads = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asignar_atencion);

        etBuscarIdLead = findViewById(R.id.et_buscar_id_lead);
        rvLeads        = findViewById(R.id.rv_leads);

        // 1) Configura RecyclerView con adapter que maneja clics
        rvLeads.setLayoutManager(new LinearLayoutManager(this));
        adapter = new Lead_adapter(
                filteredLeads,
                R.layout.item_lead,
                this::onLeadSelected
        );
        rvLeads.setAdapter(adapter);

        // 2) Filtrado en tiempo real al escribir
        etBuscarIdLead.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filtrarLeads(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        // 3) Carga inicial de datos
        cargarLeads();
    }

    private void cargarLeads() {
        SharedPreferences prefs = getSharedPreferences("APP_LEADS_PREFS", MODE_PRIVATE);
        String token = prefs.getString("ACCESS_TOKEN", null);
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Token no encontrado, inicia sesión nuevamente", Toast.LENGTH_LONG).show();
            startActivity(new Intent(this, activity_login.class));
            finish();
            return;
        }

        JsonObjectRequest req = new JsonObjectRequest(
                Request.Method.GET,
                api_config.OBTENER_LEADS,
                null,
                resp -> {
                    try {
                        if (!"ok".equalsIgnoreCase(resp.getString("status"))) {
                            Toast.makeText(this,
                                    resp.optString("mensaje", "Error al obtener leads"),
                                    Toast.LENGTH_SHORT
                            ).show();
                            return;
                        }
                        allLeads.clear();
                        JSONArray arr = resp.getJSONArray("data");
                        for (int i = 0; i < arr.length(); i++) {
                            JSONObject L = arr.getJSONObject(i);
                            allLeads.add(new Lead(
                                    L.optString("id", ""),
                                    L.optString("fecha", ""),
                                    L.optString("ruc", ""),
                                    L.optString("empresa", ""),
                                    L.optString("ejecutivo_crm", ""),
                                    L.optString("subgerente_crm", ""),
                                    L.optString("ejecutivo", ""),
                                    L.optString("subgerente", ""),
                                    L.optString("estado", ""),
                                    L.optString("situacion", ""),
                                    L.optString("detalle", null),
                                    L.optString("id_contacto", "")
                            ));
                        }
                        // Actualiza la vista con el filtro actual
                        filtrarLeads(etBuscarIdLead.getText().toString());
                    } catch (Exception e) {
                        Log.e("cargarLeads", "JSON parse error", e);
                        Toast.makeText(this, "Error al procesar datos", Toast.LENGTH_LONG).show();
                    }
                },
                err -> {
                    Log.e("cargarLeads", "Volley error", err);
                    Toast.makeText(this, "Error de red: " + err.getMessage(), Toast.LENGTH_LONG).show();
                }
        ) {
            @Override
            public Map<String,String> getHeaders() throws AuthFailureError {
                Map<String,String> h = new HashMap<>();
                h.put("Content-Type", "application/json; charset=utf-8");
                h.put("Authorization", "Bearer " + token);
                return h;
            }
        };

        Volley.newRequestQueue(this).add(req);
    }

    private void filtrarLeads(String query) {
        filteredLeads.clear();
        for (Lead lead : allLeads) {
            if (lead.getId().toLowerCase().contains(query.toLowerCase())) {
                filteredLeads.add(lead);
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void onLeadSelected(Lead lead) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("Asignar Ejecutivo CRM");

        EditText input = (EditText) LayoutInflater
                .from(this)
                .inflate(R.layout.dialog_input_text, null);
        input.setHint("Nuevo nombre de ejecutivo");
        builder.setView(input);

        builder.setPositiveButton("Guardar", (DialogInterface d, int w) -> {
                    String nuevo = input.getText().toString().trim();
                    if (nuevo.isEmpty()) {
                        Toast.makeText(this, "Debe ingresar un nombre", Toast.LENGTH_SHORT).show();
                    } else {
                        actualizarEjecutivo(lead, nuevo);
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void actualizarEjecutivo(Lead lead, String nuevo) {
        try {
            String idEnc = URLEncoder.encode(lead.getId(), StandardCharsets.UTF_8.name());
            // Construye: https://.../api/leads/{ID}/asignar-ejecutivo/
            String url = api_config.ASIGNAR_EJECUTIVO + "/" + idEnc + "/asignar-ejecutivo/";
            JSONObject body = new JSONObject();
            body.put("ejecutivo_crm", nuevo);

            JsonObjectRequest req = new JsonObjectRequest(
                    Request.Method.PUT,
                    url,
                    body,
                    resp -> {
                        if ("ok".equalsIgnoreCase(resp.optString("status"))) {
                            lead.setEjecutivoCrm(nuevo);
                            adapter.notifyDataSetChanged();
                            Toast.makeText(this, "Ejecutivo asignado", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Error: " + resp.optString("mensaje"), Toast.LENGTH_SHORT).show();
                        }
                    },
                    (VolleyError err) -> {
                        Log.e("actualizarEjecutivo", err.toString());
                        Toast.makeText(this, "Error de red", Toast.LENGTH_SHORT).show();
                    }
            ) {
                @Override
                public Map<String,String> getHeaders() throws AuthFailureError {
                    String token = getSharedPreferences("APP_LEADS_PREFS", MODE_PRIVATE)
                            .getString("ACCESS_TOKEN", "");
                    Map<String,String> h = new HashMap<>();
                    h.put("Content-Type", "application/json; charset=utf-8");
                    h.put("Authorization", "Bearer " + token);
                    return h;
                }
            };

            Volley.newRequestQueue(this).add(req);
        } catch (Exception e) {
            Log.e("actualizarEjecutivo", e.toString());
            Toast.makeText(this, "Error al armar petición", Toast.LENGTH_SHORT).show();
        }
    }
}
