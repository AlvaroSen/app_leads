package com.example.app_leads.api;

public class api_config {
    // Evita instanciación
    private api_config() {}

    // Base URL común a todos los endpoints
    public static final String BASE_URL            = "https://lead.gtm.net.pe/api/";

    // Endpoints
    public static final String LOGIN               = BASE_URL + "login/";
    public static final String OBTENER_LEADS       = BASE_URL + "leads/";
    /** Para PUT /api/leads/{ID}/asignar-ejecutivo/ */
    public static final String ASIGNAR_EJECUTIVO   = BASE_URL + "leads";
}
