package com.example.app_leads.ejecutivo;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.app_leads.R;
import com.example.app_leads.api.api_config;
import com.example.app_leads.model.Lead;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class activity_detalle_lead_ejecutivo extends AppCompatActivity {

    private static final String TAG = "DetalleLeadEje";

    private TextView tvEmpresa, tvRuc, tvId, tvFecha, tvSubgerente;
    private TextView tvContactoNombre, tvContactoEmail, tvContactoTelefono;
    private Spinner   spEstado, spSituacion;
    private EditText  etDetalle;
    private Button    btnGuardar;
    private Lead      lead;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_detalle_lead_ejecutivo);

        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.main),
                (v, insets) -> {
                    Insets sb = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                    v.setPadding(sb.left, sb.top, sb.right, sb.bottom);
                    return insets;
                }
        );

        tvEmpresa          = findViewById(R.id.tv_empresa);
        tvRuc              = findViewById(R.id.tv_ruc);
        tvId               = findViewById(R.id.tv_id);
        tvFecha            = findViewById(R.id.tv_fecha);
        tvSubgerente       = findViewById(R.id.tv_subgerente);
        tvContactoNombre   = findViewById(R.id.tv_contacto_nombre);
        tvContactoEmail    = findViewById(R.id.tv_contacto_email);
        tvContactoTelefono = findViewById(R.id.tv_contacto_telefono);
        spEstado           = findViewById(R.id.spinner_estado);
        spSituacion        = findViewById(R.id.spinner_situacion);
        etDetalle          = findViewById(R.id.et_detalle);
        btnGuardar         = findViewById(R.id.btn_guardar);

        lead = (Lead) getIntent().getSerializableExtra("lead");
        if (lead == null) {
            Toast.makeText(this, "Lead inválido", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        rellenarCampos();
        btnGuardar.setOnClickListener(v -> enviarActualizacion());
    }

    private void rellenarCampos() {
        tvEmpresa.setText(lead.getEmpresa());
        tvRuc.setText("RUC: " + lead.getRuc());
        tvId.setText("ID: " + lead.getId());
        tvFecha.setText("Fecha: " + lead.getFechaRegistro());
        tvSubgerente.setText("Subgerente: " + lead.getSubgerenteName());
        etDetalle.setText(lead.getDetalle() == null ? "" : lead.getDetalle());

        fetchEstados();
        fetchSituaciones();
        obtenerContacto(lead.getIdContacto());
    }

    private void fetchEstados() {
        String url = api_config.BASE_URL + "estados/";
        SharedPreferences prefs = getSharedPreferences("APP_LEADS_PREFS", MODE_PRIVATE);

        JsonObjectRequest req = new JsonObjectRequest(
                Request.Method.GET, url, null,
                response -> {
                    try {
                        JSONArray arr = response.getJSONArray("data");
                        List<String> items = new ArrayList<>();
                        for (int i = 0; i < arr.length(); i++) {
                            items.add(arr.getJSONObject(i).getString("estado"));
                        }
                        ArrayAdapter<String> adapter =
                                new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, items);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spEstado.setAdapter(adapter);
                        int idx = items.indexOf(lead.getEstado());
                        if (idx >= 0) spEstado.setSelection(idx);
                    } catch (Exception e) {
                        Log.e(TAG, "Error parseando estados", e);
                    }
                },
                error -> {
                    Log.e(TAG, "Error red estados", error);
                    Toast.makeText(this, "Error de red", Toast.LENGTH_SHORT).show();
                }
        ) {
            @Override
            public Map<String,String> getHeaders() throws AuthFailureError {
                Map<String,String> h = new HashMap<>();
                h.put("Authorization","Bearer " + prefs.getString("ACCESS_TOKEN",""));
                h.put("Content-Type","application/json");
                return h;
            }
        };
        req.setRetryPolicy(new DefaultRetryPolicy(5000,1,1f));
        Volley.newRequestQueue(this).add(req);
    }

    private void fetchSituaciones() {
        String url = api_config.BASE_URL + "situaciones/";
        SharedPreferences prefs = getSharedPreferences("APP_LEADS_PREFS", MODE_PRIVATE);

        JsonObjectRequest req = new JsonObjectRequest(
                Request.Method.GET, url, null,
                response -> {
                    try {
                        JSONArray arr = response.getJSONArray("data");
                        List<String> items = new ArrayList<>();
                        for (int i = 0; i < arr.length(); i++) {
                            items.add(arr.getJSONObject(i).getString("situacion"));
                        }
                        ArrayAdapter<String> adapter =
                                new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, items);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spSituacion.setAdapter(adapter);
                        int idx = items.indexOf(lead.getSituacion());
                        if (idx >= 0) spSituacion.setSelection(idx);
                    } catch (Exception e) {
                        Log.e(TAG, "Error parseando situaciones", e);
                    }
                },
                error -> {
                    Log.e(TAG, "Error red situaciones", error);
                    Toast.makeText(this, "Error de red", Toast.LENGTH_SHORT).show();
                }
        ) {
            @Override
            public Map<String,String> getHeaders() throws AuthFailureError {
                Map<String,String> h = new HashMap<>();
                h.put("Authorization","Bearer " + prefs.getString("ACCESS_TOKEN",""));
                h.put("Content-Type","application/json");
                return h;
            }
        };
        req.setRetryPolicy(new DefaultRetryPolicy(5000,1,1f));
        Volley.newRequestQueue(this).add(req);
    }

    private void obtenerContacto(String idContacto) {
        try {
            String idEnc = URLEncoder.encode(idContacto, StandardCharsets.UTF_8.name());
            String url   = api_config.BASE_URL + "contactos/" + idEnc + "/";

            SharedPreferences prefs = getSharedPreferences("APP_LEADS_PREFS", MODE_PRIVATE);

            JsonObjectRequest req = new JsonObjectRequest(
                    Request.Method.GET, url, null,
                    response -> {
                        try {
                            JSONArray arr = response.getJSONArray("data");
                            if (arr.length()>0) {
                                JSONObject o = arr.getJSONObject(0);
                                tvContactoNombre.setText("Nombre: " + o.optString("nombre"));
                                tvContactoEmail.setText("Email: " + o.optString("email"));
                                tvContactoTelefono.setText("Teléfono: " + o.optString("telefono"));
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error parseando contacto", e);
                        }
                    },
                    error -> {
                        Log.e(TAG, "Error red contacto", error);
                        Toast.makeText(this, "Error de red", Toast.LENGTH_SHORT).show();
                    }
            ) {
                @Override
                public Map<String,String> getHeaders() throws AuthFailureError {
                    Map<String,String> h = new HashMap<>();
                    h.put("Authorization","Bearer " + prefs.getString("ACCESS_TOKEN",""));
                    h.put("Content-Type","application/json");
                    return h;
                }
            };
            req.setRetryPolicy(new DefaultRetryPolicy(5000,1,1f));
            Volley.newRequestQueue(this).add(req);

        } catch (Exception e) {
            Log.e(TAG, "Error encoding contacto", e);
        }
    }

    private void enviarActualizacion() {
        try {
            String idEnc = URLEncoder.encode(lead.getId(), StandardCharsets.UTF_8.name());
            String url   = api_config.ACTUALIZAR_LEAD.replace("%s", idEnc);

            JSONObject body = new JSONObject();
            body.put("estado",    spEstado.getSelectedItem().toString());
            body.put("situacion", spSituacion.getSelectedItem().toString());
            body.put("detalle",   etDetalle.getText().toString());

            SharedPreferences prefs = getSharedPreferences("APP_LEADS_PREFS", MODE_PRIVATE);
            String token = prefs.getString("ACCESS_TOKEN","");

            JsonObjectRequest req = new JsonObjectRequest(
                    Request.Method.PUT,
                    url,
                    body,
                    response -> {
                        Toast.makeText(this,"Lead actualizado",Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    },
                    error -> {
                        Log.e(TAG,"Error al actualizar",error);
                        Toast.makeText(this,"Error de red",Toast.LENGTH_SHORT).show();
                    }
            ) {
                @Override
                public Map<String,String> getHeaders() throws AuthFailureError {
                    Map<String,String> h = new HashMap<>();
                    h.put("Authorization","Bearer "+token);
                    h.put("Content-Type","application/json");
                    return h;
                }
            };
            req.setRetryPolicy(new DefaultRetryPolicy(5000,1,1f));
            Volley.newRequestQueue(this).add(req);

        } catch (Exception e) {
            Log.e(TAG,"Error interno",e);
            Toast.makeText(this,"Error interno",Toast.LENGTH_SHORT).show();
        }
    }
}
