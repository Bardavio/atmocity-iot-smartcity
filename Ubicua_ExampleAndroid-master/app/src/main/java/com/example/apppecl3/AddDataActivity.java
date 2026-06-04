package com.example.apppecl3;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddDataActivity extends AppCompatActivity {

    private TextInputEditText etSensorId, etSensorType, etStreetId, etAddress, etDistrict, etNeighborhood,
            etLatitude, etLongitude, etTemperature, etHumidity, etBrightness, etStatusMsg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_data);

        Toolbar toolbar = findViewById(R.id.toolbarAddData);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        etSensorId = findViewById(R.id.etSensorId);
        etSensorType = findViewById(R.id.etSensorType);
        etStreetId = findViewById(R.id.etStreetId);
        etAddress = findViewById(R.id.etAddress);
        etDistrict = findViewById(R.id.etDistrict);
        etNeighborhood = findViewById(R.id.etNeighborhood);
        etLatitude = findViewById(R.id.etLatitude);
        etLongitude = findViewById(R.id.etLongitude);
        etTemperature = findViewById(R.id.etTemperature);
        etHumidity = findViewById(R.id.etHumidity);
        etBrightness = findViewById(R.id.etBrightness);
        etStatusMsg = findViewById(R.id.etStatusMsg);

        MaterialButton submitButton = findViewById(R.id.buttonSubmit);
        submitButton.setOnClickListener(v -> submitData());
    }

    private void submitData() {
        if (etSensorId.getText().toString().isEmpty() || etStreetId.getText().toString().isEmpty() || etLatitude.getText().toString().isEmpty()) {
            Toast.makeText(this, "Los campos Sensor ID, Street ID y Latitud son obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // --- Validación de Humedad ---
            double humidity = Double.parseDouble(etHumidity.getText().toString());
            if (humidity < 0 || humidity > 100) {
                Toast.makeText(this, "Error: La humedad debe estar entre 0 y 100.", Toast.LENGTH_LONG).show();
                return;
            }

            // --- Validación de Luminosidad ---
            int brightness = Integer.parseInt(etBrightness.getText().toString());
            if (brightness != 0 && brightness != 100) {
                Toast.makeText(this, "Error: La luminosidad solo puede ser 0 o 100.", Toast.LENGTH_LONG).show();
                return;
            }

            // --- Recolección del resto de datos ---
            String sensorId = etSensorId.getText().toString();
            String sensorType = etSensorType.getText().toString();
            String streetId = etStreetId.getText().toString();
            String timestamp = Instant.now().atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT);
            double latitude = Double.parseDouble(etLatitude.getText().toString());
            double longitude = Double.parseDouble(etLongitude.getText().toString());
            String address = etAddress.getText().toString();
            String district = etDistrict.getText().toString();
            String neighborhood = etNeighborhood.getText().toString();
            double temperature = Double.parseDouble(etTemperature.getText().toString());
            String statusMsg = etStatusMsg.getText().toString();

            // --- Construcción del Payload ---
            LocationData location = new LocationData(latitude, longitude, address, district, neighborhood);
            PostSensorData postSensorData = new PostSensorData(temperature, humidity, brightness, statusMsg);
            DataPayload payload = new DataPayload(sensorId, sensorType, streetId, timestamp, location, postSensorData);

            sendDataToServer(payload);

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Error de formato en un campo numérico. Asegúrate de rellenarlos todos.", Toast.LENGTH_LONG).show();
        }
    }

    private void sendDataToServer(DataPayload payload) {
        ApiService apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);
        Call<Void> call = apiService.setData(payload);

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(AddDataActivity.this, "Datos enviados con éxito", Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    Toast.makeText(AddDataActivity.this, "Error del servidor: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Toast.makeText(AddDataActivity.this, "Fallo de conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
