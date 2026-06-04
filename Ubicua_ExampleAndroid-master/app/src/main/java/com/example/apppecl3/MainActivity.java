package com.example.apppecl3;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

public class MainActivity extends AppCompatActivity {

    private static final int SPLASH_DURATION = 4000;
    private static final int SPINNER_DELAY = 1500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = getSharedPreferences("ThemePrefs", MODE_PRIVATE);
        String theme = prefs.getString("Theme", "light");
        if (theme.equals("dark")) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        setContentView(R.layout.activity_main);

        // Ocultar la barra de acción para una pantalla de carga inmersiva
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Referenciar los elementos de la vista
        ImageView logo = findViewById(R.id.imageViewLogo);
        ProgressBar progressBar = findViewById(R.id.progressBar);
        TextView tvWait = findViewById(R.id.tvWait);
        logo.setVisibility(View.VISIBLE);

        // Handler para mostrar el spinner y el texto de espera
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            progressBar.setVisibility(View.VISIBLE);
            tvWait.setVisibility(View.VISIBLE);
        }, SPINNER_DELAY);

        // Handler para pasar a la siguiente actividad
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent = new Intent(MainActivity.this, StreetSelection.class);
            startActivity(intent);
            finish();
        }, SPLASH_DURATION);
    }
}
