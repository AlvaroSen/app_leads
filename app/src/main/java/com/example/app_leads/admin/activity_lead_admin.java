package com.example.app_leads.admin;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.app_leads.R;
import com.example.app_leads.api.api_config;
import com.example.app_leads.model.Lead;
import com.example.app_leads.model.Lead_adapter;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class activity_lead_admin extends AppCompatActivity {

    private RecyclerView    rvLeads;
    private Lead_adapter     adapter;
    private List<Lead>      listaLeads = new ArrayList<>();
    private RequestQueue    queue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_lead_admin);

        // Insets para pantalla completa
        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.main_leads),
                (v, insets) -> {
                    Insets sys = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                    v.setPadding(sys.left, sys.top, sys.right, sys.bottom);
                    return insets;
                }
        );

        // 1) RecyclerView + Adapter
        rvLeads = findViewById(R.id.rv_leads);
        rvLeads.setLayoutManager(new LinearLayoutManager(this));
        adapter = new Lead_adapter(listaLeads);
        rvLeads.setAdapter(adapter);

        // 2) Cola de Volley
        queue = Volley.newRequestQueue(this);

        // 3) Petición al API
        fetchLeads();
    }

    private void fetchLeads() {
        // Usamos la constante de ApiConfig
        String url   = api_config.OBTENER_LEADS;
        String token = getSharedPreferences("prefs", MODE_PRIVATE)
                .getString("auth_token", "");

        JsonObjectRequest req = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                resp -> {
                    try {
                        if (!"ok".equalsIgnoreCase(resp.getString("status"))) {
                            Toast.makeText(this,
                                    resp.optString("mensaje", "Error al obtener leads"),
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }
                        // Parseo
                        listaLeads.clear();
                        JSONArray arr = resp.getJSONArray("data");
                        for (int i = 0; i < arr.length(); i++) {
                            JSONObject L   = arr.getJSONObject(i);
                            int        id  = L.getInt("id");
                            String     ruc = L.optString("ruc");
                            String     emp = L.optString("empresa");
                            listaLeads.add(new Lead(id, ruc, emp));
                        }
                        adapter.notifyDataSetChanged();
                    } catch (Exception e) {
                        Log.e("fetchLeads", "JSON parse error", e);
                        Toast.makeText(this,
                                "JSON parse error", Toast.LENGTH_SHORT).show();
                    }
                },
                err -> {
                    Log.e("fetchLeads", "Volley error", err);
                    String msg = err.getMessage() != null
                            ? err.getMessage()
                            : (err.networkResponse != null
                            ? "HTTP " + err.networkResponse.statusCode
                            : "Desconocido");
                    Toast.makeText(this,
                            "Error de red: " + msg,
                            Toast.LENGTH_LONG).show();
                }
        ) {
            @Override
            public java.util.Map<String, String> getHeaders() {
                java.util.Map<String,String> h = new java.util.HashMap<>();
                h.put("Authorization", "Bearer " + token);
                return h;
            }
        };

        queue.add(req);
    }
}
