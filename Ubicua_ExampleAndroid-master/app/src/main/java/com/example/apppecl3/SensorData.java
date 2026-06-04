package com.example.apppecl3;

// Esta es la clase que el StreetAdapter espera.
public class SensorData {

    // Enum para definir los tipos de datos que podemos mostrar
    public enum DataType {
        TEMPERATURE,
        HUMIDITY,
        BRIGHTNESS,
        STATUS
    }

    private final String name;
    private final String value;
    private final DataType type;

    public SensorData(String name, String value, DataType type) {
        this.name = name;
        this.value = value;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public DataType getType() {
        return type;
    }
}
