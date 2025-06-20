// api_config.java
package com.example.app_leads.api;

import android.content.Context;
import android.content.SharedPreferences;

public class api_config {

    private api_config() {}

    public static final String BASE_URL = "https://lead.gtm.net.pe/api/";

    public static final String LOGIN                 = BASE_URL + "login/";
    public static final String LEADS_REGISTROS       = BASE_URL + "leads-registros/";
    public static final String CONTACTOS_POR_ID      = BASE_URL + "contactos/";
    public static final String ESTADOS               = BASE_URL + "estados/";
    public static final String SITUACIONES           = BASE_URL + "situaciones/";
    public static final String RESUMEN_LEADS         = BASE_URL + "resumen-leads/";
    public static final String ASIGNAR_EJECUTIVO     = BASE_URL + "leads/%s/asignar-ejecutivo/";
    public static final String LEAD_DETALLE_BASE     = BASE_URL + "leads/";
    public static final String ACTUALIZAR_LEAD       = BASE_URL + "actualizar-lead/%s/";

    // Leer token desde SharedPreferences
    public static String getAccessToken(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("APP_LEADS_PREFS", Context.MODE_PRIVATE);
        return prefs.getString("ACCESS_TOKEN", null);
    }
}
