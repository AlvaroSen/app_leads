package com.example.app_leads.ejecutivo;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
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
import com.example.app_leads.model.Lead;
import com.example.app_leads.model.Lead_adapter;
import com.example.app_leads.model.User;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class activity_lead_historial_ejecutivo extends AppCompatActivity {

    private RecyclerView rvLeads;
    private Lead_adapter adapter;
    private List<Lead> allLeads   = new ArrayList<>();
    private List<Lead> listaLeads = new ArrayList<>();

    private EditText    etFilterDate;
    private Spinner     spinnerEstado;
    private ProgressBar progressBar;

    private String selectedDate   = "";
    private String selectedEstado = "Todos";
    private String currentCrmUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_lead_historial_ejecutivo);

        // Edge-to-edge insets
        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.main_historial),
                (v, insets) -> {
                    Insets sys = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                    v.setPadding(sys.left, sys.top, sys.right, sys.bottom);
                    return insets;
                }
        );

        // Usuario logeado
        User user = (User) getIntent().getSerializableExtra("user");
        if (user == null) {
            startActivity(new Intent(this, activity_login.class));
            finish();
            return;
        }
        currentCrmUser = user.getUsername().toUpperCase();

        // Referencias UI
        etFilterDate   = findViewById(R.id.et_filter_date);
        spinnerEstado  = findViewById(R.id.spinner_estado);
        progressBar    = findViewById(R.id.progressBar);
        rvLeads        = findViewById(R.id.rv_leads);

        etFilterDate.setOnClickListener(v -> showDatePicker());

        rvLeads.setLayoutManager(new LinearLayoutManager(this));
        adapter = new Lead_adapter(listaLeads, R.layout.item_lead);
        rvLeads.setAdapter(adapter);

        fetchLeads();
    }

    private void showDatePicker() {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(
                this,
                (DatePicker dp, int y, int m, int d) -> {
                    selectedDate = String.format("%04d-%02d-%02d", y, m + 1, d);
                    etFilterDate.setText(selectedDate);
                    applyFilters();
                },
                c.get(Calendar.YEAR),
                c.get(Calendar.MONTH),
                c.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    private void applyFilters() {
        listaLeads.clear();
        for (Lead lead : allLeads) {
            boolean matchDate   = selectedDate.isEmpty()
                    || selectedDate.equals(lead.getFechaRegistro());
            boolean matchEstado = selectedEstado.equals("Todos")
                    || selectedEstado.equalsIgnoreCase(lead.getEstado());
            if (matchDate && matchEstado) {
                listaLeads.add(lead);
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void setupEstadoSpinner(List<String> estados) {
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, estados
        );
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEstado.setAdapter(spinnerAdapter);
        int pos = estados.indexOf(selectedEstado);
        if (pos >= 0) spinnerEstado.setSelection(pos);
        spinnerEstado.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                selectedEstado = estados.get(position);
                applyFilters();
            }
            @Override public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });
    }

    private void fetchLeads() {
        progressBar.setVisibility(View.VISIBLE);
        SharedPreferences prefs = getSharedPreferences("APP_LEADS_PREFS", MODE_PRIVATE);
        String token = prefs.getString("ACCESS_TOKEN", null);
        if (token == null || token.isEmpty()) {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, "Sesión expirada, vuelve a iniciar sesión", Toast.LENGTH_LONG).show();
            startActivity(new Intent(this, activity_login.class));
            finish();
            return;
        }

        // Usa el endpoint correcto para la lista de registros
        String url = api_config.LEADS_REGISTROS;

        JsonObjectRequest req = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                resp -> {
                    progressBar.setVisibility(View.GONE);
                    try {
                        if (!"ok".equalsIgnoreCase(resp.getString("status"))) {
                            Toast.makeText(this,
                                    resp.optString("mensaje","Error al obtener leads"),
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }
                        allLeads.clear();
                        JSONArray arr = resp.getJSONArray("data");
                        for (int i = 0; i < arr.length(); i++) {
                            JSONObject L = arr.getJSONObject(i);
                            String crm    = L.optString("ejecutivo_crm","").toUpperCase();
                            String estado = L.optString("estado","");

                            // Solo históricos: no "Pendiente"
                            if (!crm.equals(currentCrmUser) || "Pendiente".equalsIgnoreCase(estado)) {
                                continue;
                            }

                            allLeads.add(new Lead(
                                    L.optString("id",""),
                                    L.optString("fecha",""),
                                    L.optString("ruc",""),
                                    L.optString("empresa",""),
                                    crm,
                                    L.optString("subgerente_crm",""),
                                    L.optString("ejecutivo",""),
                                    L.optString("subgerente",""),
                                    estado,
                                    L.optString("situacion",""),
                                    L.optString("detalle_lead",""),  // usa "detalle_lead"
                                    L.optString("id_contacto","")
                            ));
                        }

                        // Inicializar filtros
                        selectedDate   = "";
                        selectedEstado = "Todos";
                        listaLeads.clear();
                        listaLeads.addAll(allLeads);
                        adapter.notifyDataSetChanged();

                        // Poblar spinner de estados
                        List<String> estados = new ArrayList<>();
                        estados.add("Todos");
                        for (Lead l : allLeads) {
                            if (!estados.contains(l.getEstado())) {
                                estados.add(l.getEstado());
                            }
                        }
                        setupEstadoSpinner(estados);

                    } catch (Exception e) {
                        Log.e("fetchLeads","JSON parse error",e);
                        Toast.makeText(this,"Error al procesar datos",Toast.LENGTH_LONG).show();
                    }
                },
                err -> {
                    progressBar.setVisibility(View.GONE);
                    Log.e("fetchLeads","Volley error",err);
                    Toast.makeText(this,"Error de red",Toast.LENGTH_LONG).show();
                }
        ) {
            @Override
            public Map<String,String> getHeaders() throws AuthFailureError {
                Map<String,String> h = new HashMap<>();
                h.put("Content-Type","application/json; charset=utf-8");
                h.put("Authorization","Bearer " + token);
                return h;
            }
        };

        Volley.newRequestQueue(this).add(req);
    }
}
