package com.example.apppecl3;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class HistoryResponse {

    @SerializedName("data")
    private List<HistoricalDataItem> data;

    public List<HistoricalDataItem> getData() {
        return data;
    }
}
