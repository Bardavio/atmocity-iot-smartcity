package com.example.apppecl3;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        RadioGroup themeRadioGroup = findViewById(R.id.themeRadioGroup);
        RadioButton dayThemeButton = findViewById(R.id.buttonDayTheme);
        RadioButton nightThemeButton = findViewById(R.id.buttonNightTheme);

        // --- LÓGICA PARA MARCAR EL BOTÓN CORRECTO ---
        SharedPreferences prefs = getSharedPreferences("ThemePrefs", MODE_PRIVATE);
        String currentTheme = prefs.getString("Theme", "light");

        if (currentTheme.equals("dark")) {
            nightThemeButton.setChecked(true);
        } else {
            dayThemeButton.setChecked(true);
        }
        // --- FIN DE LA LÓGICA ---

        themeRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.buttonDayTheme) {
                setTheme(AppCompatDelegate.MODE_NIGHT_NO, "light");
            } else if (checkedId == R.id.buttonNightTheme) {
                setTheme(AppCompatDelegate.MODE_NIGHT_YES, "dark");
            }
        });
    }

    private void setTheme(int themeMode, String themeName) {
        AppCompatDelegate.setDefaultNightMode(themeMode);
        SharedPreferences.Editor editor = getSharedPreferences("ThemePrefs", MODE_PRIVATE).edit();
        editor.putString("Theme", themeName);
        editor.apply();
    }
}
