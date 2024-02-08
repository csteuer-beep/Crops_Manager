package com.example.cropsmanager;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
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

public class login_activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        Button b = findViewById(R.id.loginButton);
        b.setOnClickListener(new View.OnClickListener() {
            //set listener to the login button
            @Override
            public void onClick(View view) {
                login();
            }
        });
    }


    public void login(){ //executed when login button is pressed
        //get instance of the rest service class
        RestRequests rest = ServiceGenerator.createService(RestRequests.class);
        //create json to send the user and password
        JsonObject user = new JsonObject();
        EditText username = findViewById(R.id.username);
        EditText password = findViewById(R.id.password);
        user.addProperty("username", username.getText().toString());
        user.addProperty("password", password.getText().toString());
        //execute the rest function to login
        Call<JsonObject> resp = rest.getToken(user);
        resp.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if(response.code() == 200){
                    //if it was a successfully request
                    try{
                        // get json in the body of the response message
                        JSONObject js = new JSONObject(response.body().toString());
                        //get token in the json
                        String token = js.getString("token");
                        //store json in the shared preferences
                        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putString("token", token);
                        editor.apply();

                        Toast.makeText(getApplicationContext(), "Token saved", Toast.LENGTH_SHORT).show();

                        SharedPreferences shared = getPreferences(Context.MODE_PRIVATE);
                        String tokenString = shared.getString("token", null);

                        //move to main activity
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(intent);

                    }catch (Exception ex){
                        ex.printStackTrace();
                    }

                }else{

                    //if code is not 200, then display a dialog warning the failure
                    new AlertDialog.Builder(login_activity.this)
                            .setTitle("Error when log-in")
                            .setMessage("Authetication failed: error code " + response.code())
                            // A null listener allows the button to dismiss the dialog and take no further action.
                            .setNegativeButton("Close", null)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
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