package com.example.apppecl3;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HistoryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView tvNoHistory;
    private HistoryAdapter adapter;
    private List<HistoricalDataItem> originalHistoryList = new ArrayList<>();
    private String currentStreetId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences prefs = getSharedPreferences("ThemePrefs", MODE_PRIVATE);
        String theme = prefs.getString("Theme", "light");
        AppCompatDelegate.setDefaultNightMode(theme.equals("dark") ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);

        setContentView(R.layout.activity_history);

        Toolbar toolbar = findViewById(R.id.toolbarHistory);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        recyclerView = findViewById(R.id.recyclerViewHistory);
        progressBar = findViewById(R.id.progressBarHistory);
        tvNoHistory = findViewById(R.id.tvNoHistory);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        currentStreetId = getIntent().getStringExtra("street_id");
        if (currentStreetId != null && !currentStreetId.isEmpty()) {
            fetchHistoryData(currentStreetId);
        } else {
            Toast.makeText(this, "No se pudo obtener el ID de la calle.", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
            tvNoHistory.setVisibility(View.VISIBLE);
        }
    }

    private void fetchHistoryData(String streetId) {
        showLoading(true);
        ApiService apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);
        Call<HistoryResponse> call = apiService.getHistory();

        call.enqueue(new Callback<HistoryResponse>() {
            @Override
            public void onResponse(@NonNull Call<HistoryResponse> call, @NonNull Response<HistoryResponse> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    originalHistoryList.clear();
                    for (HistoricalDataItem item : response.body().getData()) {
                        if (item.getStreetId() != null && item.getStreetId().equals(streetId)) {
                            originalHistoryList.add(item);
                        }
                    }
                    updateRecyclerView(originalHistoryList);
                } else {
                    handleApiError(response);
                }
            }

            @Override
            public void onFailure(@NonNull Call<HistoryResponse> call, @NonNull Throwable t) {
                showLoading(false);
                handleConnectionError(t);
            }
        });
    }

    private void fetchHistoryByDate(String date) {
        showLoading(true);
        ApiService apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);
        Call<HistoryResponse> call = apiService.getHistoryByDate(date);

        call.enqueue(new Callback<HistoryResponse>() {
            @Override
            public void onResponse(@NonNull Call<HistoryResponse> call, @NonNull Response<HistoryResponse> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    List<HistoricalDataItem> filteredList = new ArrayList<>();
                    for (HistoricalDataItem item : response.body().getData()) {
                        if (item.getStreetId() != null && item.getStreetId().equals(currentStreetId)) {
                            filteredList.add(item);
                        }
                    }
                    updateRecyclerView(filteredList);
                } else {
                    handleApiError(response);
                }
            }

            @Override
            public void onFailure(@NonNull Call<HistoryResponse> call, @NonNull Throwable t) {
                showLoading(false);
                handleConnectionError(t);
            }
        });
    }

    private void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(isLoading ? View.GONE : View.VISIBLE);
        if(isLoading) tvNoHistory.setVisibility(View.GONE);
    }

    private void updateRecyclerView(List<HistoricalDataItem> list) {
        if (list != null && !list.isEmpty()) {
            if (adapter == null) {
                adapter = new HistoryAdapter(new ArrayList<>(list));
                recyclerView.setAdapter(adapter);
            } else {
                adapter.updateList(new ArrayList<>(list));
            }
            tvNoHistory.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        } else {
            tvNoHistory.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            if (adapter != null) adapter.updateList(new ArrayList<>());
        }
    }

    private void handleApiError(Response<?> response) {
        tvNoHistory.setVisibility(View.VISIBLE);
        Toast.makeText(HistoryActivity.this, "Error del servidor: " + response.code(), Toast.LENGTH_SHORT).show();
        Log.e("HistoryActivity", "API Error: " + response.code() + " " + response.message());
    }

    private void handleConnectionError(Throwable t) {
        tvNoHistory.setVisibility(View.VISIBLE);
        Toast.makeText(HistoryActivity.this, "Error de conexión", Toast.LENGTH_LONG).show();
        Log.e("HistoryActivity", "Connection Failure", t);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.history_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (itemId == R.id.action_sort_history) {
            showSortDialog();
            return true;
        } else if (itemId == R.id.action_show_chart) {
            if (originalHistoryList.isEmpty()) {
                Toast.makeText(this, "No hay datos para mostrar", Toast.LENGTH_SHORT).show();
            } else {
                Intent intent = new Intent(this, GraphActivity.class);
                intent.putExtra("history_data", (Serializable) originalHistoryList);
                startActivity(intent);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showSortDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_sort_history, null);
        builder.setView(dialogView);

        RadioGroup groupSortBy = dialogView.findViewById(R.id.radioGroupSortBy);
        RadioGroup groupSortOrder = dialogView.findViewById(R.id.radioGroupSortOrder);
        RadioGroup groupLimit = dialogView.findViewById(R.id.radioGroupLimit); // --- NUEVO GRUPO ---
        Button btnApply = dialogView.findViewById(R.id.buttonApplySort);
        Button btnPickDate = dialogView.findViewById(R.id.buttonPickDate);
        Button btnReset = dialogView.findViewById(R.id.buttonResetFilters);

        AlertDialog dialog = builder.create();

        btnApply.setOnClickListener(v -> {
            int selectedSortById = groupSortBy.getCheckedRadioButtonId();
            int selectedSortOrderId = groupSortOrder.getCheckedRadioButtonId();
            int selectedLimitId = groupLimit.getCheckedRadioButtonId(); // --- OBTENER LÍMITE ---

            if (selectedSortById == -1 || selectedSortOrderId == -1) {
                Toast.makeText(this, "Selecciona un criterio y un orden", Toast.LENGTH_SHORT).show();
                return;
            }

            applyFilters(selectedSortById, selectedSortOrderId, selectedLimitId);
            dialog.dismiss();
        });

        btnPickDate.setOnClickListener(v -> {
            showDatePickerDialog();
            dialog.dismiss();
        });

        btnReset.setOnClickListener(v -> {
            fetchHistoryData(currentStreetId);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void applyFilters(int sortBy, int sortOrder, int limit) {
        List<HistoricalDataItem> listToFilter = new ArrayList<>(originalHistoryList);
        boolean ascending = sortOrder == R.id.radioSortAsc;

        // 1. Ordenar la lista
        if (sortBy == R.id.radioSortByDate) {
            Collections.sort(listToFilter, (o1, o2) -> {
                int cmp = o1.getTimestamp().compareTo(o2.getTimestamp());
                return ascending ? cmp : -cmp;
            });
        } else if (sortBy == R.id.radioSortByTemp) {
            Collections.sort(listToFilter, (o1, o2) -> {
                int cmp = Double.compare(o1.getData().getTemperature(), o2.getData().getTemperature());
                return ascending ? cmp : -cmp;
            });
        } else if (sortBy == R.id.radioSortByHumidity) {
            Collections.sort(listToFilter, (o1, o2) -> {
                int cmp = Integer.compare(o1.getData().getHumidity(), o2.getData().getHumidity());
                return ascending ? cmp : -cmp;
            });
        }

        // 2. Aplicar el límite de resultados
        List<HistoricalDataItem> limitedList = listToFilter;
        if (limit == R.id.radioLimit10) {
            limitedList = listToFilter.subList(0, Math.min(10, listToFilter.size()));
        } else if (limit == R.id.radioLimit100) {
            limitedList = listToFilter.subList(0, Math.min(100, listToFilter.size()));
        }

        // 3. Actualizar la UI con la lista final
        updateRecyclerView(limitedList);
    }

    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    LocalDate selectedDate = LocalDate.of(year, month + 1, dayOfMonth);
                    String formattedDate = selectedDate.format(DateTimeFormatter.ISO_LOCAL_DATE);
                    fetchHistoryByDate(formattedDate);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }
}
