package com.example.cropsmanager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TimePicker;
import android.widget.Toast;
import android.content.Context;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.json.JSONException;
import org.json.JSONObject;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cropsmanager.REST.RestRequests;
import com.example.cropsmanager.REST.ServiceGenerator;
import com.google.gson.JsonObject;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.util.Calendar;


public class MainActivity extends AppCompatActivity implements TimePickerFragment.TimePickerListener  {

    private boolean isUserInitiatedChange = false;
    final String subscriptionTopic = "v1/devices/me/rpc/request/+";
    //v1/devices/me/rpc/request/+"
    private final String username = "4BvQDriVmbEV28nxIMww";

    private static final String BROKER_URL = "ssl://srv-iot.diatel.upm.es:8883";
    private static final String CLIENT_ID = "ASP_DEMO_KIMIYA";

    private MqttClient client;

    int qos = 1;
    String buzzer;
    String irrigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Switch buzzer_switch = findViewById(R.id.buzzer_switch);
        Switch irrigation_switch = findViewById(R.id.Irrigation_switch);

        try {
            // Set up the persistence layer
            MemoryPersistence persistence = new MemoryPersistence();

            // Initialize the MQTT client
            client = new MqttClient(BROKER_URL, CLIENT_ID, persistence);

            // Set up the connection options
            MqttConnectOptions connectOptions = new MqttConnectOptions();
            //connectOptions.getKeepAliveInterval();
            connectOptions.setAutomaticReconnect(true);
            connectOptions.setCleanSession(true);
            connectOptions.setUserName(username);
            // Connect to the broker
            client.connect(connectOptions);
            Log.d("TAG", "connected!!!");
            Toast.makeText(this, "connected", Toast.LENGTH_SHORT).show();
            // Subscribe to the topic
            client.subscribe(subscriptionTopic, qos);
            Log.d("TAG", "Subscribed to topic: " + subscriptionTopic);
            Toast.makeText(this, "Subscribing to topic "+ subscriptionTopic, Toast.LENGTH_SHORT).show();
            client.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    Log.d("TAG", "Connection lost");
                    try {
                            client.connect();
                            Log.d("TAG", "Reconnected to the broker");
                    } catch (MqttException e) {
                        Log.e("TAG", "Failed to reconnect to the broker: " + e.getMessage());
                    }

                }

                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    Log.d("TAG", "A message arrived");
                    // Process incoming message
                    String payload = new String(message.getPayload());
                    try {
                        JSONObject jsonObject = new JSONObject(payload);
                        Log.d("TAG", "payload is" + payload);

                        // Show notification based on the method
                        // Temperature alert
                        if (jsonObject.has("method") && jsonObject.getString("method").equals("alert_temperature")) {
                            JSONObject params = jsonObject.getJSONObject("params");
                            if (params.has("temperature")) {
                                double temperature = params.getDouble("temperature");
                                Log.d("TAG", "Temperature received: " + temperature);
                                showNotification("Temperature Alert", "Temperature: " + temperature);
                            }
                        }
                        //Water PH Value alert
                        else if (jsonObject.has("method") && jsonObject.getString("method").equals("alert_phvalue")) {
                            JSONObject params = jsonObject.getJSONObject("params");
                            if (params.has("phvalue")) {
                                double phvalue = params.getDouble("phvalue");
                                Log.d("TAG", "phvalue received: " + phvalue);
                                showNotification("phvalue Alert", "phvalue: " + phvalue);
                            }
                        }
                        //Soil Moisture alert
                        else if (jsonObject.has("method") && jsonObject.getString("method").equals("alert_soilmoisture")) {
                            JSONObject params = jsonObject.getJSONObject("params");
                            if (params.has("soilmoisture")) {
                                double phvalue = params.getDouble("soilmoisture");
                                Log.d("TAG", "soilmoisture received: " + phvalue);
                                showNotification("soilmoisture Alert", "soilmoisture: " + phvalue);
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    Log.d("TAG", "Delivery complete");
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
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

        // Send Buzzer Commands
        buzzer_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // The toggle is enabled
                    sendCommand("start_buzzer");
                }else {
                    // The toggle is disabled
                    sendCommand("stop_buzzer");
                }
            }
        });


        // Send Irrigating Commands
        irrigation_switch.setOnCheckedChangeListener(null); // remove listener to prevent onCheckedChanged from being called
        irrigation_switch.setChecked(isUserInitiatedChange ); // Set the initial value of the switch
        irrigation_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isUserInitiatedChange) {
                    if (isChecked) {
                        // The toggle is enabled
                        //sendCommand("start_irrigation");
                        showTimePickerDialog();
                    } else {
                        // The toggle is disabled
                        sendCommand("stop_irrigation");
                    }
                }
                isUserInitiatedChange = true;
            }
        });

        Button treshholdActivity = findViewById(R.id.nextPage);
        treshholdActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent intent = new Intent(MainActivity.this, Change_Treshold.class);
                    startActivity(intent);
                    Log.e("TAG", "Activity started");
                } catch (Exception e) {
                    e.printStackTrace(); // Print the stack trace of the exception
                    Log.e("MainActivity", "Error starting Change_Treshold activity: " + e.getMessage());
                }
            }
        });
    }
    @Override
    protected void onDestroy() {
        try {
            Log.d("TAG", "DISCONNECTED");
            client.disconnect();
        } catch (MqttException e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }
    private void showNotification(String title, String message) {
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "default",
                    "Channel name",
                    NotificationManager.IMPORTANCE_HIGH
            );
            notificationManager.createNotificationChannel(channel);
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "default")
                .setSmallIcon(R.drawable.baseline_dangerous_24)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(
                this,
                0,
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );
        builder.setContentIntent(contentIntent);
        // Add as notification
        notificationManager.notify(0, builder.build());
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

                        //Recieving Status of Irrigation system and the Buzzer

                         buzzer = values.getString("BuzzerSystem");
                         irrigation = values.getString("IrrigationSystem");

                        Switch buzzer_switch = findViewById(R.id.buzzer_switch);
                        Switch irrigation_switch = findViewById(R.id.Irrigation_switch);

                        if (buzzer.equals("start_buzzer")){
                            buzzer_switch.setChecked(true);
                        }if (irrigation.equals("start_irrigation")){
                            irrigation_switch.setChecked(true);
                        }

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
                    Toast.makeText(getApplicationContext(), "Error getting values", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Toast.makeText(getApplicationContext(), "Failure: check internet connection", Toast.LENGTH_SHORT).show();
                Log.d("Failure", t.toString());
            }
        });
    }

    public void sendCommand(String command){
        RestRequests rest = ServiceGenerator.createService(RestRequests.class);


        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        String tokenString = sharedPref.getString("token", null);

        JsonObject commandjson = new JsonObject();
        commandjson.addProperty("command", command);
        Call<Void> resp = rest.sendCommand(commandjson, tokenString);

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

    private void showTimePickerDialog()  {
        TimePickerFragment timePickerFragment = new TimePickerFragment();
        timePickerFragment.setListener(this);
        timePickerFragment.show(getSupportFragmentManager(), "timePicker");
    }

    @Override
    public void onTimeSet(int minutes, int seconds) {
        // Handle the selected time (hours, minutes, seconds)
        // Update your UI or perform any other actions
        sendIrrigationTime(minutes* 60 + seconds);

    }

    public void sendIrrigationTime(int seconds){
        RestRequests rest = ServiceGenerator.createService(RestRequests.class);


        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        String tokenString = sharedPref.getString("token", null);

        JsonObject commandjson = new JsonObject();
        commandjson.addProperty("command", "start_irrigation");
        commandjson.addProperty("seconds", seconds);
        Call<Void> resp = rest.sendCommand(commandjson, tokenString);

        resp.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if(response.code() == 200){
                    try{
                        Toast.makeText(getApplicationContext(), "Irrigation time sent successfully", Toast.LENGTH_SHORT).show();

                    }catch (Exception ex){
                        ex.printStackTrace();
                    }

                }else{
                    Toast.makeText(getApplicationContext(), "Error sending the irrigation time", Toast.LENGTH_SHORT).show();
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