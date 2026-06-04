package com.example.apppecl3;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable; // <-- NUEVA IMPORTACIÓN

public class HistoricalDataItem implements Serializable { // <-- IMPLEMENTS SERIALIZABLE

    @SerializedName("street_id")
    private String streetId;

    @SerializedName("timestamp")
    private String timestamp;

    @SerializedName("data")
    private HistoricalDataValues data;

    public String getStreetId() {
        return streetId;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public HistoricalDataValues getData() {
        return data;
    }
}
