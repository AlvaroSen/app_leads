package com.example.app_leads.admin;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.app_leads.R;
import com.example.app_leads.activity_login;
import com.example.app_leads.api.api_config;
import com.example.app_leads.model.LeadStats;
import com.example.app_leads.model.StatsAdapter;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class activity_estadisticas_admin extends AppCompatActivity {

    private RecyclerView rvStats;
    private StatsAdapter adapter;
    private List<LeadStats> listaStats = new ArrayList<>();
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_estadisticas_admin);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets sys = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(sys.left, sys.top, sys.right, sys.bottom);
            return insets;
        });

        rvStats = findViewById(R.id.rv_stats);
        progressBar = findViewById(R.id.progressBar);
        adapter = new StatsAdapter(listaStats);
        rvStats.setLayoutManager(new LinearLayoutManager(this));
        rvStats.setAdapter(adapter);

        cargarEstadisticas();
    }

    private void cargarEstadisticas() {
        progressBar.setVisibility(View.VISIBLE);
        String token = api_config.getAccessToken(this);

        if (token == null || token.isEmpty()) {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, "Token no encontrado, inicia sesión de nuevo", Toast.LENGTH_LONG).show();
            startActivity(new Intent(this, activity_login.class));
            finish();
            return;
        }

        JsonObjectRequest req = new JsonObjectRequest(
                Request.Method.GET,
                api_config.RESUMEN_LEADS,
                null,
                response -> {
                    progressBar.setVisibility(View.GONE);
                    try {
                        if (!"ok".equalsIgnoreCase(response.getString("status"))) {
                            Toast.makeText(this, response.optString("mensaje", "Error al obtener estadísticas"), Toast.LENGTH_SHORT).show();
                            return;
                        }

                        JSONArray arr = response.getJSONArray("data");
                        listaStats.clear();
                        for (int i = 0; i < arr.length(); i++) {
                            JSONObject s = arr.getJSONObject(i);
                            listaStats.add(new LeadStats(
                                    s.optString("fecha", ""),
                                    s.optString("ejecutivo", ""),
                                    s.optString("subgerente", ""),
                                    s.optInt("pendientes", 0),
                                    s.optInt("atendidos", 0),
                                    s.optInt("total", 0)
                            ));
                        }
                        adapter.notifyDataSetChanged();

                    } catch (Exception e) {
                        Log.e("EstadisticasAdmin", "Error parseando JSON", e);
                        Toast.makeText(this, "Error parseando JSON", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    progressBar.setVisibility(View.GONE);
                    Log.e("EstadisticasAdmin", "Volley error", error);
                    Toast.makeText(this, "Error de red", Toast.LENGTH_LONG).show();
                }
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
    }
}
