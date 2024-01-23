package com.example.cropsmanager.REST;


import com.google.gson.JsonObject;


import retrofit2.*;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface RestRequests {



    @Headers({"Accept: application/json", "Content-Type: application/json"})
    @POST("auth/login")
    Call<JsonObject> getToken(@Body JsonObject user);


    @Headers({"Accept: application/json"})
    @GET("v1/{device_access_token}/attributes?sharedKeys=phvalue")
    Call<JsonObject> getPhLevel(@Header("X-Authoritation") String token, @Path("device_access_token") String device_access_token);

    /*
    @Headers({"Accept: text/plain", "Content-Type: application/json"})
    @POST("v1/{device_access_token}/telemetry")
    Call<JsonObject> sendPhLevel(@Body JsonObject ph, @Path("device_access_token") String device_access_token);
     */
}
