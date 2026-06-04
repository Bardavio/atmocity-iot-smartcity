package com.example.apppecl3;

import com.google.gson.annotations.SerializedName;

public class LocationData {

    @SerializedName("latitude")
    private double latitude;

    @SerializedName("longitude")
    private double longitude;

    @SerializedName("address")
    private String address;

    @SerializedName("district")
    private String district;

    @SerializedName("neighborhood")
    private String neighborhood;

    // Constructor
    public LocationData(double latitude, double longitude, String address, String district, String neighborhood) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
        this.district = district;
        this.neighborhood = neighborhood;
    }

    // Getters y setters si los necesitas
}
