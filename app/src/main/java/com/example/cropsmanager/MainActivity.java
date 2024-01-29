package com.example.cropsmanager;


import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cropsmanager.REST.RestRequests;
import com.example.cropsmanager.REST.ServiceGenerator;
import com.google.gson.JsonObject;

import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //check if the log in has been done
        SharedPreferences sharedPref =  PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String tokenString = sharedPref.getString("token", null);
        if (tokenString == null) {
            Intent intent = new Intent(this, login_activity.class);
            startActivity(intent);
        }

        Button b = findViewById(R.id.updateButton);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getSensorsValues();
            }
        });

        b.callOnClick();
        getSensorsValues();


    }





    public void getSensorsValues(){
        RestRequests rest = ServiceGenerator.createService(RestRequests.class);


        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        String tokenString = sharedPref.getString("token", null);


        Call<JsonObject> resp = rest.getSensorValues(tokenString);
        resp.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if(response.code() == 200){
                    try{

                        JSONObject js = new JSONObject(response.body().toString());
                        JSONObject values = js.getJSONObject("shared");

                        TextView temperatureTV = findViewById(R.id.temperatureValueValue);
                        TextView soilmoistureTV = findViewById(R.id.soilMoistureValueValue);
                        TextView distanceTV = findViewById(R.id.distanceValue);
                        TextView phvalueTV = findViewById(R.id.phValue);
                        TextView humidityTV = findViewById(R.id.HumidityValue);
                        TextView precipitationsTV = findViewById(R.id.weatherValue);

                        Double temperature = values.getDouble("temperature");
                        Double soilmoisture = values.getDouble("soilmoisture");
                        Double distance = values.getDouble("distance");
                        Double phvalue = values.getDouble("phvalue");
                        Double humidity = values.getDouble("humidity");
                        Double precipitation = values.getDouble("precipitate");

                        temperatureTV.setText(String.format("%.1f", temperature) + " ºC");
                        soilmoistureTV.setText(String.format("%.0f", soilmoisture) + " %");
                        distanceTV.setText(String.format("%.1f", distance) + " cm");
                        phvalueTV.setText(String.format("%.0f", phvalue) + " pH");
                        humidityTV.setText(String.format("%.0f", humidity) + " %");
                        precipitationsTV.setText(String.format("%.1f", precipitation) + " mm");


                    }catch (Exception ex){
                        ex.printStackTrace();
                    }

                }else{
                    Toast.makeText(getApplicationContext(), "Error al recivir el dato", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Toast.makeText(getApplicationContext(), "Failure", Toast.LENGTH_SHORT).show();
                Log.d("Failure", t.toString());
            }
        });
    }

}