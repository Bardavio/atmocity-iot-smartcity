package com.example.apppecl3;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class HistoricalDataValues implements Serializable {

    @SerializedName("temperature_celsius")
    private String temperature;

    @SerializedName("humidity_percent")
    private String humidity;

    @SerializedName("brightness_level")
    private String brightness;

    // Métodos para obtener los valores como String (para Gson)
    public String getTemperatureString() {
        return temperature;
    }

    public String getHumidityString() {
        return humidity;
    }

    public String getBrightnessString() {
        return brightness;
    }

    // Métodos de conveniencia para obtener los valores como números de forma segura
    public double getTemperature() {
        try {
            return Double.parseDouble(temperature);
        } catch (NumberFormatException | NullPointerException e) {
            return 0.0; // Valor por defecto en caso de error
        }
    }

    public int getHumidity() {
        try {
            // Quitamos los decimales si los hubiera antes de convertir
            if (humidity != null && humidity.contains(".")) {
                humidity = humidity.substring(0, humidity.indexOf('.'));
            }
            return Integer.parseInt(humidity);
        } catch (NumberFormatException | NullPointerException e) {
            return 0; // Valor por defecto en caso de error
        }
    }

    public int getBrightness() {
        try {
             // Quitamos los decimales si los hubiera antes de convertir
            if (brightness != null && brightness.contains(".")) {
                brightness = brightness.substring(0, brightness.indexOf('.'));
            }
            return Integer.parseInt(brightness);
        } catch (NumberFormatException | NullPointerException e) {
            return 0; // Valor por defecto en caso de error
        }
    }
}
