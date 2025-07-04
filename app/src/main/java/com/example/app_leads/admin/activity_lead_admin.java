package com.example.app_leads.admin;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class activity_lead_admin extends AppCompatActivity {
    private static final String TAG = "LeadAdmin";
    private static final int    REQ_ACTUALIZAR = 100;

    private RecyclerView rvLeads;
    private Lead_adapter adapter;
    private List<Lead>   allLeads   = new ArrayList<>();
    private List<Lead>   listaLeads = new ArrayList<>();

    private EditText etFilterDate;
    private EditText etSearchId;       // NUEVO
    private Spinner  spinnerEstado;
    private ProgressBar progressBar;

    private String selectedDate   = "";
    private String selectedEstado = "Todos";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_lead_admin);

        // edge-to-edge padding
        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.main_leads),
                (v, insets) -> {
                    Insets sys = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                    v.setPadding(sys.left, sys.top, sys.right, sys.bottom);
                    return insets;
                }
        );

        etFilterDate  = findViewById(R.id.et_filter_date);
        etSearchId    = findViewById(R.id.et_search_id);
        spinnerEstado = findViewById(R.id.spinner_estado);
        progressBar   = findViewById(R.id.progressBar);
        rvLeads       = findViewById(R.id.rv_leads);

        // Date picker
        etFilterDate.setOnClickListener(v -> showDatePicker());

        // TextWatcher para buscar por ID
        etSearchId.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s,int st,int c,int a){}
            @Override public void onTextChanged(CharSequence s,int st,int b,int c) {
                applyFilters();
            }
            @Override public void afterTextChanged(Editable s){}
        });

        // RecyclerView + adapter
        rvLeads.setLayoutManager(new LinearLayoutManager(this));
        adapter = new Lead_adapter(
                listaLeads,
                R.layout.item_lead,
                this::onLeadSelected
        );
        rvLeads.setAdapter(adapter);

        fetchLeads();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_ACTUALIZAR && resultCode == RESULT_OK) {
            fetchLeads();
        }
    }

    private void showDatePicker() {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(
                this,
                (DatePicker dp, int year, int month, int day) -> {
                    selectedDate = String.format("%04d-%02d-%02d", year, month+1, day);
                    etFilterDate.setText(selectedDate);
                    applyFilters();
                },
                c.get(Calendar.YEAR),
                c.get(Calendar.MONTH),
                c.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    private void applyFilters() {
        String queryId = etSearchId.getText().toString().trim().toLowerCase();

        listaLeads.clear();
        for (Lead lead : allLeads) {
            boolean matchDate   = selectedDate.isEmpty()
                    || selectedDate.equals(lead.getFechaRegistro());
            boolean matchEstado = selectedEstado.equals("Todos")
                    || selectedEstado.equalsIgnoreCase(lead.getEstado());
            boolean matchId     = queryId.isEmpty()
                    || lead.getId().toLowerCase().contains(queryId);

            if (matchDate && matchEstado && matchId) {
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
        spinnerEstado.setSelection(estados.indexOf(selectedEstado));
        spinnerEstado.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(android.widget.AdapterView<?> parent, View view, int pos, long id) {
                selectedEstado = estados.get(pos);
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
            Toast.makeText(this,
                    "Token no encontrado, inicia sesión de nuevo",
                    Toast.LENGTH_LONG
            ).show();
            startActivity(new Intent(this, activity_login.class));
            finish();
            return;
        }

        String url = api_config.LEADS_REGISTROS;
        JsonObjectRequest req = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    progressBar.setVisibility(View.GONE);
                    try {
                        if (!"ok".equalsIgnoreCase(response.getString("status"))) {
                            Toast.makeText(this,
                                    response.optString("mensaje","Error al obtener leads"),
                                    Toast.LENGTH_SHORT
                            ).show();
                            return;
                        }

                        allLeads.clear();
                        JSONArray arr = response.getJSONArray("data");
                        for (int i = 0; i < arr.length(); i++) {
                            JSONObject L = arr.getJSONObject(i);
                            allLeads.add(new Lead(
                                    L.optString("id",""),
                                    L.optString("fecha",""),
                                    L.optString("ruc",""),
                                    L.optString("empresa",""),
                                    L.optString("ejecutivo_crm",""),
                                    L.optString("subgerente_crm",""),
                                    L.optString("ejecutivo",""),
                                    L.optString("subgerente",""),
                                    L.optString("estado",""),
                                    L.optString("situacion",""),
                                    L.optString("detalle_lead",""),
                                    L.optString("id_contacto","")
                            ));
                        }

                        // spinner de estados
                        List<String> estados = new ArrayList<>();
                        estados.add("Todos");
                        for (Lead l : allLeads) {
                            if (!estados.contains(l.getEstado())) {
                                estados.add(l.getEstado());
                            }
                        }
                        selectedEstado = "Todos";
                        setupEstadoSpinner(estados);

                        // reset fecha y búsqueda
                        selectedDate = "";
                        etFilterDate.setText("");
                        etSearchId.setText("");
                        applyFilters();

                    } catch (Exception e) {
                        Log.e(TAG, "JSON parse error", e);
                        Toast.makeText(this,
                                "JSON parse error: " + e.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                },
                error -> {
                    progressBar.setVisibility(View.GONE);
                    Log.e(TAG, "Volley error", error);
                    String msg = (error.networkResponse != null)
                            ? "HTTP " + error.networkResponse.statusCode
                            : error.getMessage();
                    Toast.makeText(this,
                            "Error de red: " + msg,
                            Toast.LENGTH_LONG
                    ).show();
                }
        ) {
            @Override
            public Map<String,String> getHeaders() throws AuthFailureError {
                Map<String,String> h = new HashMap<>();
                h.put("Content-Type","application/json; charset=utf-8");
                h.put("Authorization","Bearer "+token);
                return h;
            }
        };

        Volley.newRequestQueue(this).add(req);
    }

    private void onLeadSelected(Lead lead) {
        Intent i = new Intent(this, activity_detalle_lead_admin.class);
        i.putExtra("lead", lead);
        startActivityForResult(i, REQ_ACTUALIZAR);
    }
}
