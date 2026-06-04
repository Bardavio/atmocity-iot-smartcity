// Archivo: Street.java (VERSIÓN CORREGIDA Y COMPLETA)
package com.example.apppecl3;

public class Street {
    private String idTopic;
    private String value;

    // Constructor por defecto (necesario para algunas librerías)
    public Street() {
    }

    // Constructor con argumentos (el que usamos en StreetMonitoring)
    public Street(String idTopic, String value) {
        this.idTopic = idTopic;
        this.value = value;
    }

    // --- GETTERS Y SETTERS CORREGIDOS Y COMPLETOS ---

    public String getIdTopic() {
        return idTopic;
    }

    public void setIdTopic(String idTopic) {
        this.idTopic = idTopic;
    }

    // Este es el método que soluciona el error
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
