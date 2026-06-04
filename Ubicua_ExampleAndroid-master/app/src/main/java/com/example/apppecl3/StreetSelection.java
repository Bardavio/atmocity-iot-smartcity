package com.example.apppecl3;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.button.MaterialButton;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StreetSelection extends AppCompatActivity {

    private AutoCompleteTextView spinnerAutoComplete;
    private MaterialButton botonContinuar, botonAnadirCalle; // <-- BOTÓN NUEVO
    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeRefreshLayout;

    private List<Calle> listaDeCalles;
    private int selectedStreetIndex = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = getSharedPreferences("ThemePrefs", MODE_PRIVATE);
        String theme = prefs.getString("Theme", "light");
        AppCompatDelegate.setDefaultNightMode(theme.equals("dark") ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);

        setContentView(R.layout.activity_street_selection);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(this::obtenerDatosDelServidor);

        spinnerAutoComplete = findViewById(R.id.spinnerAutoComplete);
        botonContinuar = findViewById(R.id.buttonContinue);
        botonAnadirCalle = findViewById(R.id.buttonAddStreet);
        progressBar = findViewById(R.id.progressBarSelection);

        botonContinuar.setEnabled(false);

        botonContinuar.setOnClickListener(v -> {
            if (selectedStreetIndex != -1) {
                Calle selectedCalle = listaDeCalles.get(selectedStreetIndex);
                Intent intent = new Intent(StreetSelection.this, StreetMonitoring.class);
                intent.putExtra("street_id", selectedCalle.getId());
                intent.putExtra("street_name", selectedCalle.getNombre());
                startActivity(intent);
            } else {
                Toast.makeText(this, "Por favor, selecciona una calle", Toast.LENGTH_SHORT).show();
            }
        });


        botonAnadirCalle.setOnClickListener(v -> {
            Intent intent = new Intent(StreetSelection.this, AddDataActivity.class);
            startActivity(intent);
        });

        obtenerDatosDelServidor();
    }

    private void obtenerDatosDelServidor() {
        if (!swipeRefreshLayout.isRefreshing()) {
            progressBar.setVisibility(View.VISIBLE);
        }
        spinnerAutoComplete.setVisibility(View.GONE);

        ApiService apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);
        Call<CalleResponse> call = apiService.getCalles();

        call.enqueue(new Callback<CalleResponse>() {
            @Override
            public void onResponse(@NonNull Call<CalleResponse> call, @NonNull Response<CalleResponse> response) {
                progressBar.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);
                spinnerAutoComplete.setVisibility(View.VISIBLE);

                if (response.isSuccessful() && response.body() != null) {
                    listaDeCalles = response.body().getCalles();
                    if (listaDeCalles != null && !listaDeCalles.isEmpty()) {
                        mostrarListaEnSpinner(listaDeCalles);
                        botonContinuar.setEnabled(true);
                    } else {
                        Toast.makeText(StreetSelection.this, "El servidor no devolvió ninguna calle.", Toast.LENGTH_LONG).show();
                    }
                } else {
                    String errorBody = "Cuerpo del error no disponible";
                    try {
                        if (response.errorBody() != null) {
                            errorBody = response.errorBody().string();
                        }
                    } catch (IOException e) {
                        Log.e("ubicua", "Error al leer el cuerpo del error", e);
                    }
                    Log.e("ubicua", "Error del servidor al obtener calles. Código: " + response.code() + ". Mensaje: " + response.message() + ". Cuerpo: " + errorBody);
                    Toast.makeText(StreetSelection.this, "Error al cargar las calles (Código: " + response.code() + ")", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<CalleResponse> call, @NonNull Throwable t) {
                progressBar.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);
                spinnerAutoComplete.setVisibility(View.VISIBLE);
                Log.e("ubicua", "Error de conexión a la red: " + t.getMessage(), t);
                Toast.makeText(StreetSelection.this, "Error de conexión. Revisa la IP y que estés en la misma WiFi.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void mostrarListaEnSpinner(List<Calle> lista) {
        ArrayList<String> nombres = new ArrayList<>();
        for (Calle item : lista) {
            nombres.add(item.getNombre());
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, nombres);
        spinnerAutoComplete.setAdapter(adapter);
        spinnerAutoComplete.setOnItemClickListener((parent, view, position, id) -> selectedStreetIndex = position);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.settings_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
