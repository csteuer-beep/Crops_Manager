package com.example.cropsmanager;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.cropsmanager.REST.RestRequests;
import com.example.cropsmanager.REST.ServiceGenerator;
import com.google.gson.JsonObject;

import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Change_Treshold extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {



        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_treshold);
        getThresholdsValues();

        Button submitBtn = findViewById(R.id.submitBtn);

        Button returnBtn = findViewById(R.id.returnBtn);

        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                EditText minSoilMoisture = findViewById(R.id.minSoilmoisture);
                EditText maxSoilMoisture= findViewById(R.id.maxSoilmoisture);
                EditText minPH = findViewById(R.id.minPH);
                EditText maxPH = findViewById(R.id.maxPH);
                EditText minTemp = findViewById(R.id.temperatureMin);
                EditText maxTemp = findViewById(R.id.temperatureMax);

                try {
                    // Soil & Moisture
                    String minSoilMoistureString = minSoilMoisture.getText().toString();
                    minSoilMoistureString = minSoilMoistureString.replace(',', '.');
                    Float minSoilMoistureValue = Float.parseFloat(minSoilMoistureString);
                    String maxSoilMoistureString = maxSoilMoisture.getText().toString();
                    maxSoilMoistureString = maxSoilMoistureString.replace(',', '.');
                    Float maxSoilMoistureValue = Float.parseFloat(maxSoilMoistureString);
                    // PH
                    String minPHString = minPH.getText().toString();
                    minPHString = minPHString.replace(',', '.');
                    Float minPHValue = Float.parseFloat(minPHString);
                    String maxPHString = maxPH.getText().toString();
                    maxPHString = maxPHString.replace(',', '.');
                    Float maxPHValue = Float.parseFloat(maxPHString);
                    // Temperature
                    String minTempString = minTemp.getText().toString();
                    minTempString = minTempString.replace(',', '.');
                    Float minTempValue = Float.parseFloat(minTempString);
                    String maxTempString = maxTemp.getText().toString();
                    maxTempString = maxTempString.replace(',', '.');
                    Float maxTempValue = Float.parseFloat(maxTempString);
                    sendThresholdsValues(minPHValue,maxPHValue,minSoilMoistureValue,maxSoilMoistureValue,minTempValue,maxTempValue);
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "Invalid input", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();  // Print the stack trace or log the exception
                }

            }
        });

        returnBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Change_Treshold.this,MainActivity.class);
                startActivity(intent);
            }
        });
    }
    public void getThresholdsValues(){
        RestRequests rest = ServiceGenerator.createService(RestRequests.class);
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        String tokenString = sharedPref.getString("token", null);
        Call<JsonObject> resp = rest.getThresholdsValues(tokenString);
        resp.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if(response.code() == 200){
                    try{
                        JSONObject js = new JSONObject(response.body().toString());
                        JSONObject values = js.getJSONObject("shared");

                        Double treshold_ph_max = values.getDouble("treshold_ph_max");
                        Double treshold_ph_min = values.getDouble("treshold_ph_min");
                        Double treshold_soil_max = values.getDouble("treshold_soil_max");
                        Double treshold_soil_min = values.getDouble("treshold_soil_min");
                        Double treshold_temp_max = values.getDouble("treshold_temp_max");
                        Double treshold_temp_min = values.getDouble("treshold_temp_min");

                        EditText minSoilMoisture = findViewById(R.id.minSoilmoisture);
                        EditText maxSoilMoisture= findViewById(R.id.maxSoilmoisture);
                        EditText minPH = findViewById(R.id.minPH);
                        EditText maxPH = findViewById(R.id.maxPH);
                        EditText minTemp = findViewById(R.id.temperatureMin);
                        EditText maxTemp = findViewById(R.id.temperatureMax);


                        maxSoilMoisture.setText(String.format("%.1f", treshold_soil_max));
                        minSoilMoisture.setText(String.format("%.1f", treshold_soil_min));

                        maxPH.setText(String.format("%.1f", treshold_ph_max));
                        minPH.setText(String.format("%.1f", treshold_ph_min));

                        maxTemp.setText(String.format("%.1f", treshold_temp_max));
                        minTemp.setText(String.format("%.1f", treshold_temp_min));



                    }catch (Exception ex){
                        ex.printStackTrace();
                    }
                }else{
                    Toast.makeText(getApplicationContext(), "Error getting threshold values", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Toast.makeText(getApplicationContext(), "Failure: check internet connection", Toast.LENGTH_SHORT).show();
                Log.d("Failure", t.toString());
            }
        });
    }

    public void sendThresholdsValues(float treshold_ph_min, float treshold_ph_max,float treshold_soil_min, float treshold_soil_max, float treshold_temp_min, float treshold_temp_max){
        RestRequests rest = ServiceGenerator.createService(RestRequests.class);


        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        String tokenString = sharedPref.getString("token", null);

        JsonObject thresholdjson = new JsonObject();
        thresholdjson.addProperty("treshold_ph_max", treshold_ph_max);
        thresholdjson.addProperty("treshold_ph_min", treshold_ph_min);
        thresholdjson.addProperty("treshold_soil_max", treshold_soil_max);
        thresholdjson.addProperty("treshold_soil_min", treshold_soil_min);
        thresholdjson.addProperty("treshold_temp_max", treshold_temp_max);
        thresholdjson.addProperty("treshold_temp_min", treshold_temp_min);
        Call<Void> resp = rest.sendThresholdsValues(thresholdjson, tokenString);

        resp.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if(response.code() == 200){
                    try{
                        Toast.makeText(getApplicationContext(), "Command sent successfully", Toast.LENGTH_SHORT).show();
                    }catch (Exception ex){
                        ex.printStackTrace();
                    }
                }else{
                    Toast.makeText(getApplicationContext(), "Error sending the command", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(getApplicationContext(), "Failure: check internet connection", Toast.LENGTH_SHORT).show();
                Log.d("Failure", t.toString());
            }
        });
    }

}