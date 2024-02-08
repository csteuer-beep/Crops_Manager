package com.example.cropsmanager.REST;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ServiceGenerator {
    //Class to get an intance of the retrofit instance needed to execute the rest function defines in RestRequest.java interface
    //Base uri for all the requests
    private static final String BASE_URI = "https://srv-iot.diatel.upm.es/api/";
    private static Retrofit.Builder builder = new Retrofit.Builder()
            .baseUrl(BASE_URI)
            .client(new OkHttpClient.Builder().addInterceptor((
                    new HttpLoggingInterceptor()).setLevel(HttpLoggingInterceptor.Level.BODY)).build())
            .addConverterFactory(GsonConverterFactory.create());

    //static function to get the retrofit instance
    public static <S> S createService(Class <S> serviceClass){
        Retrofit adapter = builder.build();
        return  adapter.create(serviceClass);
    }



}
