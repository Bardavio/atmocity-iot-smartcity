// Archivo: CallesResponse.java
package com.example.apppecl3;

import java.util.List;

public class CalleResponse {
    // Los nombres de las variables DEBEN coincidir con las claves del JSON
    private String info;
    private List<Calle> calles; // Esta es la lista que nos interesa

    // --- Genera los Getters y Setters ---

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public List<Calle> getCalles() {
        return calles;
    }

    public void setCalles(List<Calle> calles) {
        this.calles = calles;
    }
}
    