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
import android.widget.Toast;

import com.example.cropsmanager.REST.RestRequests;
import com.example.cropsmanager.REST.ServiceGenerator;
import com.google.gson.JsonObject;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.json.JSONException;
import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class MainActivity extends AppCompatActivity {

    final String subscriptionTopic = "v1/devices/me/microcontroller/telemetry";
    private final String username = "bfALtJHAbqRZoN4kV1Ib";

    private static final String BROKER_URL = "ssl://srv-iot.diatel.upm.es:8883";
    private static String CLIENT_ID = "ASP_DEMO_KIMIYA";
    MqttAndroidClient mqttAndroidClient;
    private MqttClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            // Set up the persistence layer
            MemoryPersistence persistence = new MemoryPersistence();

            // Initialize the MQTT client
            client = new MqttClient(BROKER_URL, CLIENT_ID, persistence);

            // Set up the connection options
            MqttConnectOptions connectOptions = new MqttConnectOptions();
            connectOptions.setCleanSession(true);
            connectOptions.setUserName(username);

            // Connect to the broker
            client.connect(connectOptions);
            Log.d("TAG", "connected !!! ");
            Toast.makeText(this, "connected", Toast.LENGTH_SHORT).show();
            client.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    Log.d("TAG", "Connection lost");
                }
                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    // Process incoming message
                    Log.d("TAG", "Msg arrived");
                    String payload = new String(message.getPayload());
                    JSONObject jsonObject = new JSONObject(payload);
                    Log.d("TAG", "Msg arrived");

                    if (jsonObject.has("method")) {
                        String method = jsonObject.getString("method");
                        switch (method) {
                            case "alert_temperature":
                                handleTemperature(jsonObject.getJSONObject("params"));
                                break;
                            case "alert_phvalue":
                                // handlePH(jsonObject.getJSONObject("params"));
                                break;
                        }
                    }
                }
                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    Log.d("TAG", "Delivery complete");
                }
            });

            // Subscribe to the topic

            client.subscribe(subscriptionTopic);
            Toast.makeText(this, "Subscribing to topic "+ subscriptionTopic, Toast.LENGTH_SHORT).show();
            Log.d("TAG", "Subscribed to topic: " + subscriptionTopic);
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
                getPh();
            }
        });

    }
    public void getPh(){
        RestRequests rest = ServiceGenerator.createService(RestRequests.class);


        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        String tokenString = sharedPref.getString("token", null);


        Call<JsonObject> resp = rest.getPhLevel(tokenString);
        resp.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if(response.code() == 200){
                    try{

                        JSONObject js = new JSONObject(response.body().toString());
                        Double value = js.getJSONObject("shared").getDouble("phvalue");
                        Toast.makeText(getApplicationContext(), "Ph value:" + value, Toast.LENGTH_SHORT).show();


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
    private void handleTemperature(JSONObject params) throws JSONException {
        if (params.has("temperature")){
            int temperature = params.getInt("temperature");
            String message = " CAUTION " +
                    "Temperature is :" + temperature + "degrees";

            Toast.makeText(this, "Subscribing to topic "+ message, Toast.LENGTH_SHORT).show();
            //showNotification(context , message);
        }
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
    private void handlePH(JSONObject params) throws JSONException {
        if (params.has("phvalue")){
            int phvalue = params.getInt("phvalue");
            String message = " CAUTION " +
                    "Water PH is :" + phvalue;
            Log.d("TAG", "PH IS : " + message);
            //showNotification(message);
        }
    }
    /*

    private void showNotification( Context context ,String message) {

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        String channelID = "channel_id";
        String channelName = "channel_name";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel(channelID, channelName, NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelID)
                //.setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle("NEW ALARM!")
                .setContentText(message)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);
        Notification notification = builder.build();
        notificationManager.notify(0, notification);
    }

 */
    public void subscribeToTopic() {
        try {
            mqttAndroidClient.subscribe(subscriptionTopic, 0, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.d("TAG", "Subscribed to topic ");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.d("TAG", "Failed to subscribe ");
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }

    }


}