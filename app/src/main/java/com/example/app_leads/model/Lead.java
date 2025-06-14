package com.example.app_leads.model;

import java.io.Serializable;

public class Lead implements Serializable {
    private String id;
    private String rawFecha;          // la fecha completa ISO
    private String ruc;
    private String empresa;
    private String ejecutivoCrm;
    private String subgerenteCrm;
    private String ejecutivoName;
    private String subgerenteName;
    private String estado;
    private String situacion;
    private String detalle;           // puede ser null

    public Lead(
            String id,
            String rawFecha,
            String ruc,
            String empresa,
            String ejecutivoCrm,
            String subgerenteCrm,
            String ejecutivoName,
            String subgerenteName,
            String estado,
            String situacion,
            String detalle
    ) {
        this.id              = id;
        this.rawFecha        = rawFecha;
        this.ruc             = ruc;
        this.empresa         = empresa;
        this.ejecutivoCrm    = ejecutivoCrm;
        this.subgerenteCrm   = subgerenteCrm;
        this.ejecutivoName   = ejecutivoName;
        this.subgerenteName  = subgerenteName;
        this.estado          = estado;
        this.situacion       = situacion;
        this.detalle         = detalle;
    }

    public String getId()                 { return id; }
    public String getRawFecha()           { return rawFecha; }
    /** Formatea yyyy-MM-dd extraído de la ISO */
    public String getFechaRegistro() {
        return rawFecha.contains("T")
                ? rawFecha.split("T")[0]
                : rawFecha;
    }
    public String getRuc()                { return ruc; }
    public String getEmpresa()            { return empresa; }
    public String getEjecutivoCrm()       { return ejecutivoCrm; }
    public String getSubgerenteCrm()      { return subgerenteCrm; }
    public String getEjecutivoName()      { return ejecutivoName; }
    public String getSubgerenteName()     { return subgerenteName; }
    public String getEstado()             { return estado; }
    public String getSituacion()          { return situacion; }
    public String getDetalle()            { return detalle; }
}
