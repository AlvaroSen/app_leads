package com.example.app_leads.model;

import java.io.Serializable;

public class Lead implements Serializable {
    private int    id;
    private String ruc;
    private String empresa;

    public Lead(int id, String ruc, String empresa) {
        this.id = id;
        this.ruc = ruc;
        this.empresa = empresa;
    }

    public int getId() {
        return id;
    }

    public String getRuc() {
        return ruc;
    }

    public String getEmpresa() {
        return empresa;
    }
}
