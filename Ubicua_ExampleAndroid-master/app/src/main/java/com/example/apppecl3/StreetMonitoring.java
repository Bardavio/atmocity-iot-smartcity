// Archivo: app/src/main/java/com/example/apppecl3/StreetMonitoring.java (VERSIÓN CON IP CORREGIDA PARA DISPOSITIVO FÍSICO)
package com.example.apppecl3;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StreetMonitoring extends AppCompatActivity implements MqttHandler.MqttMessageListener, OnMapReadyCallback {

    private static final String TAG = "StreetMonitoring";
    // Usamos la IP de tu ordenador en la red local
    private static final String MQTT_BROKER_HOST = "192.168.0.28";
    private static final int MQTT_BROKER_PORT = 1883;

    private MqttHandler mqttHandler;
    private GoogleMap googleMap;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private TextView tvTemperatureValue, tvHumidityValue, tvBrightnessValue, tvStatusMessage;
    private double latitude = 0.0, longitude = 0.0;
    private String streetId, streetName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = getSharedPreferences("ThemePrefs", MODE_PRIVATE);
        String theme = prefs.getString("Theme", "light");
        AppCompatDelegate.setDefaultNightMode(theme.equals("dark") ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);

        setContentView(R.layout.activity_street_monitoring);

        Intent intent = getIntent();
        streetId = intent.getStringExtra("street_id");
        streetName = intent.getStringExtra("street_name");

        if (streetId == null || streetName == null) {
            Toast.makeText(this, "Error: No se recibió la información de la calle.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        Toolbar toolbar = findViewById(R.id.toolbarMonitoring);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        TextView toolbarTitle = findViewById(R.id.toolbar_title);
        toolbarTitle.setText(streetName);

        // Referenciar Vistas
        tvTemperatureValue = findViewById(R.id.tvTemperatureValue);
        tvHumidityValue = findViewById(R.id.tvHumidityValue);
        tvBrightnessValue = findViewById(R.id.tvBrightnessValue);
        tvStatusMessage = findViewById(R.id.tvStatusMessage);
        Button buttonHistory = findViewById(R.id.buttonHistory);
        Button buttonSendMessage = findViewById(R.id.buttonSendMessage); // Nuevo botón

        // Inicializar Vistas
        tvTemperatureValue.setText("-- °C");
        tvHumidityValue.setText("-- %");
        tvBrightnessValue.setText("-- lux");
        tvStatusMessage.setText("Esperando datos...");

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // --- LÓGICA DEL BOTÓN DE HISTORIAL ---
        buttonHistory.setOnClickListener(v -> {
            Intent historyIntent = new Intent(StreetMonitoring.this, HistoryActivity.class);
            historyIntent.putExtra("street_id", streetId);
            startActivity(historyIntent);
        });

        // --- LÓGICA DEL BOTÓN DE ENVIAR MENSAJE ---
        buttonSendMessage.setOnClickListener(v -> showSendMessageDialog());

        iniciarConexionMqtt(streetId);
    }

    private void showSendMessageDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enviar Mensaje a Pantalla");

        // Usamos un contenedor para poder añadir márgenes al EditText
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(48, 0, 48, 0); // Márgenes horizontales

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setHint("Escribe tu mensaje aquí");
        input.setLayoutParams(lp);

        container.addView(input);
        builder.setView(container);

        // Configurar botones del diálogo
        builder.setPositiveButton("Enviar", (dialog, which) -> {
            String message = input.getText().toString().trim();
            if (!message.isEmpty()) {
                sendMessageToServer(message);
            } else {
                Toast.makeText(StreetMonitoring.this, "El mensaje no puede estar vacío", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void sendMessageToServer(String message) {
        // Obtenemos la instancia de Retrofit y creamos el servicio
        ApiService apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);

        // Creamos la llamada a la API
        Call<Void> call = apiService.sendMessage(message);

        // Ejecutamos la llamada de forma asíncrona
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(StreetMonitoring.this, "Mensaje enviado con éxito", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(StreetMonitoring.this, "Error al enviar mensaje: " + response.code(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error en la respuesta del servidor: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Toast.makeText(StreetMonitoring.this, "Fallo de conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Fallo al conectar con el servidor", t);
            }
        });
    }

    private void iniciarConexionMqtt(String streetId) {
        executorService.execute(() -> {
            mqttHandler = new MqttHandler(MQTT_BROKER_HOST, MQTT_BROKER_PORT);
            mqttHandler.setMessageListener(this);
            mqttHandler.connect();
            String cleanStreetId = streetId.replace(" ", "_");
            String topic = "Sensors/" + cleanStreetId + "/sensor/telemetry";
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (mqttHandler != null) mqttHandler.subscribeToTopic(topic);
            }, 2000);
        });
    }

    @Override
    public void onMqttMessageReceived(String topic, String payload) {
        runOnUiThread(() -> {
            try {
                JSONObject jsonPayload = new JSONObject(payload);
                if (jsonPayload.has("location")) {
                    JSONObject locationObject = jsonPayload.getJSONObject("location");
                    latitude = locationObject.optDouble("latitude", 0.0);
                    longitude = locationObject.optDouble("longitude", 0.0);
                    actualizarUbicacionEnMapa();
                }
                if (jsonPayload.has("data")) {
                    JSONObject dataObject = jsonPayload.getJSONObject("data");
                    if (dataObject.has("temperature_celsius")) tvTemperatureValue.setText(dataObject.getString("temperature_celsius") + " °C");
                    if (dataObject.has("humidity_percent")) tvHumidityValue.setText(dataObject.getString("humidity_percent") + " %");
                    if (dataObject.has("brightness_level")) tvBrightnessValue.setText(dataObject.getString("brightness_level") + " lux");
                    if (dataObject.has("status_msg")) tvStatusMessage.setText(dataObject.getString("status_msg"));
                }
            } catch (JSONException e) {
                Log.e(TAG, "Error al parsear el JSON de MQTT", e);
            }
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        this.googleMap = map;
        googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        if (latitude != 0.0 && longitude != 0.0) actualizarUbicacionEnMapa();
    }

    private void actualizarUbicacionEnMapa() {
        if (googleMap != null && latitude != 0.0 && longitude != 0.0) {
            LatLng ubicacionCalle = new LatLng(latitude, longitude);
            googleMap.clear();
            googleMap.addMarker(new MarkerOptions().position(ubicacionCalle).title(streetName));
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ubicacionCalle, 17f));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mqttHandler != null) mqttHandler.disconnect();
        if (executorService != null) executorService.shutdown();
    }
}
