package com.example.apppecl3;

import com.google.gson.annotations.SerializedName;

// CLASE RENOMBRADA PARA EVITAR CONFLICTOS
public class PostSensorData {

    @SerializedName("temperature_celsius")
    private double temperature;

    @SerializedName("humidity_percent")
    private double humidity;

    @SerializedName("brightness_level")
    private int brightness;

    @SerializedName("status_msg")
    private String statusMsg;

    public PostSensorData(double temperature, double humidity, int brightness, String statusMsg) {
        this.temperature = temperature;
        this.humidity = humidity;
        this.brightness = brightness;
        this.statusMsg = statusMsg;
    }
}
