package com.example.apppecl3;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiService {

    @GET("GetCalles")
    Call<CalleResponse> getCalles();

    @GET("GetData")
    Call<HistoryResponse> getHistory();

    @GET("GetByDate")
    Call<HistoryResponse> getHistoryByDate(@Query("date") String date);

    @GET("SendMessage")
    Call<Void> sendMessage(@Query("msg") String message);

    // --- NUEVO ENDPOINT ---
    @POST("SetData")
    Call<Void> setData(@Body DataPayload payload);
}
