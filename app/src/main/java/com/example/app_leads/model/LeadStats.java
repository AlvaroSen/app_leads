package com.example.app_leads.model;

public class LeadStats {
    private String fecha;
    private String ejecutivo;
    private String subgerente;
    private int pendientes;
    private int atendidos;
    private int total;

    // Constructor
    public LeadStats(String fecha, String ejecutivo, String subgerente, int pendientes, int atendidos, int total) {
        this.fecha = fecha;
        this.ejecutivo = ejecutivo;
        this.subgerente = subgerente;
        this.pendientes = pendientes;
        this.atendidos = atendidos;
        this.total = total;
    }

    // Getters
    public String getFecha() { return fecha; }
    public String getEjecutivo() { return ejecutivo; }
    public String getSubgerente() { return subgerente; }
    public int getPendientes() { return pendientes; }
    public int getAtendidos() { return atendidos; }
    public int getTotal() { return total; }
}
