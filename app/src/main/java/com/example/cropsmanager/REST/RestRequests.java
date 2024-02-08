package com.example.cropsmanager.REST;


import com.google.gson.JsonObject;


import retrofit2.*;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface RestRequests {
    //interface where the rest request mus be defined

    //request to get the user token
    @Headers({"Accept: application/json", "Content-Type: application/json"})
    @POST("auth/login")
    Call<JsonObject> getToken(@Body JsonObject user); // must be sent the user credential in a json

    //request to get the sensor values stored in thingsboard
    @Headers({"Accept: application/json"})
    @GET("v1/bfALtJHAbqRZoN4kV1Ib/attributes?sharedKeys=phvalue,precipitate,soilmoisture,temperature,temperatureaverage,humidity,distance,BuzzerSystem,IrrigationSystem")
    Call<JsonObject> getSensorValues(@Header("X-Authoritation") String token);

    //request to send commnands to thingsboard
    @Headers({"Accept: application/json", "Content-Type: application/json"})
    @POST("v1/bfALtJHAbqRZoN4kV1Ib/telemetry")
    Call<Void> sendCommand(@Body JsonObject command, @Header("X-Authoritation") String token); //must be set the command in a json

    //request to get the values set for the threasholds for the temperature, ph and soil moisture
    @Headers({"Accept: application/json"})
    @GET("v1/bfALtJHAbqRZoN4kV1Ib/attributes?sharedKeys=treshold_ph_max,treshold_ph_min,treshold_soil_max,treshold_soil_min,treshold_temp_max,treshold_temp_min")
    Call<JsonObject> getThresholdsValues(@Header("X-Authoritation") String token);

    //request to send the new thresholds values to thingsboard
    @Headers({"Accept: application/json", "Content-Type: application/json"})
    @POST("v1/bfALtJHAbqRZoN4kV1Ib/telemetry")
    Call<Void> sendThresholdsValues(@Body JsonObject command, @Header("X-Authoritation") String token);

}
