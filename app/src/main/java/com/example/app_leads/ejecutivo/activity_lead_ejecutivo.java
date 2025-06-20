package com.example.app_leads.ejecutivo;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
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

public class activity_lead_ejecutivo extends AppCompatActivity {
    private static final String TAG = "LeadEjecutivo";
    private static final int    REQ_UPDATE    = 200;

    private RecyclerView rvLeads;
    private Lead_adapter adapter;
    private List<Lead>   allLeads   = new ArrayList<>();
    private List<Lead>   listaLeads = new ArrayList<>();

    private EditText    etFilterDate;
    private EditText    etSearchId;
    private ProgressBar progressBar;

    private String selectedDate   = "";
    private String currentCrmUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_lead_ejecutivo);

        // edge-to-edge padding
        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.main_leads),
                (v, insets) -> {
                    Insets sys = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                    v.setPadding(sys.left, sys.top, sys.right, sys.bottom);
                    return insets;
                }
        );

        // UI refs
        etFilterDate = findViewById(R.id.et_filter_date);
        etSearchId   = findViewById(R.id.et_search_id);
        progressBar  = findViewById(R.id.progressBar);
        rvLeads      = findViewById(R.id.rv_leads);

        // Usuario actual
        User user = (User) getIntent().getSerializableExtra("user");
        if (user == null) {
            Toast.makeText(this, "Sesión inválida", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, activity_login.class));
            finish();
            return;
        }
        currentCrmUser = user.getUsername().toUpperCase();

        // Date picker
        etFilterDate.setOnClickListener(v -> showDatePicker());

        // Buscador ID
        etSearchId.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s,int st,int b,int c){}
            @Override public void onTextChanged(CharSequence s,int st,int b,int c){
                applyFilters();
            }
            @Override public void afterTextChanged(Editable s){}
        });

        // RecyclerView + adapter
        rvLeads.setLayoutManager(new LinearLayoutManager(this));
        adapter = new Lead_adapter(listaLeads, R.layout.item_lead, lead -> {
            Intent i = new Intent(this, activity_detalle_lead_ejecutivo.class);
            i.putExtra("lead", lead);
            startActivityForResult(i, REQ_UPDATE);
        });
        rvLeads.setAdapter(adapter);

        fetchLeads();
    }

    @Override
    protected void onActivityResult(int req, int res, Intent data) {
        super.onActivityResult(req, res, data);
        if (req == REQ_UPDATE && res == RESULT_OK) {
            fetchLeads();
        }
    }

    private void showDatePicker() {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(
                this,
                (DatePicker dp, int y, int m, int d) -> {
                    selectedDate = String.format("%04d-%02d-%02d", y, m+1, d);
                    etFilterDate.setText(selectedDate);
                    applyFilters();
                },
                c.get(Calendar.YEAR),
                c.get(Calendar.MONTH),
                c.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    private void applyFilters() {
        String qDate = selectedDate;
        String qId   = etSearchId.getText().toString().trim().toLowerCase();

        listaLeads.clear();
        for (Lead lead : allLeads) {
            boolean byDate = qDate.isEmpty() || qDate.equals(lead.getFechaRegistro());
            boolean byCrm  = lead.getEjecutivoCrm().toUpperCase().equals(currentCrmUser);
            boolean byPend = "Pendiente".equalsIgnoreCase(lead.getEstado());
            boolean byId   = qId.isEmpty() || lead.getId().toLowerCase().contains(qId);

            if (byDate && byCrm && byPend && byId) {
                listaLeads.add(lead);
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void fetchLeads() {
        progressBar.setVisibility(View.VISIBLE);
        SharedPreferences prefs = getSharedPreferences("APP_LEADS_PREFS", MODE_PRIVATE);
        String token = prefs.getString("ACCESS_TOKEN", "");
        if (token.isEmpty()) {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, "Token expirado", Toast.LENGTH_LONG).show();
            startActivity(new Intent(this, activity_login.class));
            finish();
            return;
        }

        JsonObjectRequest req = new JsonObjectRequest(
                Request.Method.GET,
                api_config.LEADS_REGISTROS,
                null,
                resp -> {
                    progressBar.setVisibility(View.GONE);
                    try {
                        if (!"ok".equalsIgnoreCase(resp.getString("status"))) {
                            Toast.makeText(this, resp.optString("mensaje"), Toast.LENGTH_SHORT).show();
                            return;
                        }
                        allLeads.clear();
                        JSONArray arr = resp.getJSONArray("data");
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
                        // aplicar filtro inicial
                        selectedDate = "";
                        etFilterDate.setText("");
                        etSearchId.setText("");
                        applyFilters();

                    } catch (Exception e) {
                        Log.e(TAG, "JSON parse error", e);
                        Toast.makeText(this, "Error al procesar datos", Toast.LENGTH_LONG).show();
                    }
                },
                err -> {
                    progressBar.setVisibility(View.GONE);
                    Log.e(TAG, "Volley error", err);
                    Toast.makeText(this, "Error de red", Toast.LENGTH_LONG).show();
                }
        ) {
            @Override public Map<String,String> getHeaders() throws AuthFailureError {
                Map<String,String> h = new HashMap<>();
                h.put("Authorization","Bearer "+token);
                h.put("Content-Type","application/json");
                return h;
            }
        };

        Volley.newRequestQueue(this).add(req);
    }
}
