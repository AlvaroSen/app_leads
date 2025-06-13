package com.example.app_leads.api;

public class api_config {
    // Evita instanciación
    private api_config() { }

    // Base URL común a todos los endpoints
    public static final String BASE_URL = "https://lead.gtm.net.pe/api/";

    // Endpoints
    public static final String LOGIN             = BASE_URL + "login/";
    public static final String OBTENER_LEADS     = BASE_URL + "leads/";
    public static final String ACTUALIZAR_ESTADO = BASE_URL + "leads/update/";
    // Añade aquí más rutas según tus necesidades:
    // public static final String OTRO_ENDPOINT = BASE_URL + "otro/";

}
