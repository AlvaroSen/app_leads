package com.example.app_leads.model;

import java.io.Serializable;

/**
 * Modelo de dominio para representar un usuario autenticado,
 * con todos los datos que el backend devuelve tras el login.
 */
public class User implements Serializable {

    private String username;   // campo "usuario" del API
    private String fullName;   // campo "nombre" del API
    private String role;       // campo "role" del API
    private boolean active;    // campo "is_active" del API
    private String token;      // campo "token" del API

    public User(String username,
                String fullName,
                String role,
                boolean active,
                String token) {
        this.username = username;
        this.fullName = fullName;
        this.role     = role;
        this.active   = active;
        this.token    = token;
    }

    // Getters
    public String getUsername() {
        return username;
    }

    public String getFullName() {
        return fullName;
    }

    public String getRole() {
        return role;
    }

    public boolean isActive() {
        return active;
    }

    public String getToken() {
        return token;
    }

    // Setters (por si en alguna pantalla necesitas actualizar el estado o el token)
    public void setUsername(String username) {
        this.username = username;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
