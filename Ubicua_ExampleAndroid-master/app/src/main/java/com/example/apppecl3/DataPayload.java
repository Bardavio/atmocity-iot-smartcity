package com.example.apppecl3;

import com.google.gson.annotations.SerializedName;

public class DataPayload {

    @SerializedName("sensor_id")
    private String sensorId;

    @SerializedName("sensor_type")
    private String sensorType;

    @SerializedName("street_id")
    private String streetId;

    @SerializedName("timestamp")
    private String timestamp;

    @SerializedName("location")
    private LocationData location;

    // --- CAMBIO REALIZADO AQUÍ ---
    @SerializedName("data")
    private PostSensorData data;

    // Constructor actualizado para usar la nueva clase
    public DataPayload(String sensorId, String sensorType, String streetId, String timestamp, LocationData location, PostSensorData data) {
        this.sensorId = sensorId;
        this.sensorType = sensorType;
        this.streetId = streetId;
        this.timestamp = timestamp;
        this.location = location;
        this.data = data;
    }
}
