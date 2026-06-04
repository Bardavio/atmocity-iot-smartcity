// Archivo: ApiResponse.java (NUEVO ARCHIVO
package com.example.apppecl3;

import java.util.List;

/**
 * Esta clase representa la respuesta del servidor cuando se piden los datos de los sensores de una calle.
 * Corresponde a la estructura JSON: {"sensor_type": "...", "data": [...]}
 */
public class ApiResponse {

    // Los nombres de las variables deben coincidir con las claves del JSON.
    private String sensor_type;

    // La clave "data" contiene una lista de objetos "Street".
    // La clase "Street" es la que tiene "idTopic" y "value".
    private List<Street> data;

    // --- Genera los Getters y Setters ---
    // Haz clic derecho aquí -> Generate -> Getters and Setters -> Selecciona ambos -> OK

    public String getSensor_type() {
        return sensor_type;
    }

    public void setSensor_type(String sensor_type) {
        this.sensor_type = sensor_type;
    }

    public List<Street> getData() {
        return data;
    }

    public void setData(List<Street> data) {
        this.data = data;
    }
}
