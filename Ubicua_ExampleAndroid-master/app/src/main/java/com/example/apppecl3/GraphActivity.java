package com.example.apppecl3;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class GraphActivity extends AppCompatActivity {

    private BarChart barChartTemp;
    private LineChart lineChartHumidity;
    private TextView tvTempAverage, tvHumidityAverage;
    private ArrayList<HistoricalDataItem> historyList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = getSharedPreferences("ThemePrefs", MODE_PRIVATE);
        String theme = prefs.getString("Theme", "light");
        AppCompatDelegate.setDefaultNightMode(theme.equals("dark") ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);

        setContentView(R.layout.activity_graph);

        Toolbar toolbar = findViewById(R.id.toolbarGraph);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        barChartTemp = findViewById(R.id.barChartTemp);
        lineChartHumidity = findViewById(R.id.lineChartHumidity);
        tvTempAverage = findViewById(R.id.tvTempAverage);
        tvHumidityAverage = findViewById(R.id.tvHumidityAverage);

        historyList = (ArrayList<HistoricalDataItem>) getIntent().getSerializableExtra("history_data");

        if (historyList == null || historyList.isEmpty()) {
            Toast.makeText(this, "No hay datos para mostrar en el gráfico", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupCharts();
        loadChartData();
    }

    private void setupCharts() {
        int textColor = isDarkTheme() ? Color.WHITE : Color.BLACK;
        ValueFormatter xAxisFormatter = new DateAxisValueFormatter(historyList);

        barChartTemp.getDescription().setEnabled(false);
        barChartTemp.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        barChartTemp.getXAxis().setGranularity(1f);
        barChartTemp.getXAxis().setValueFormatter(xAxisFormatter);
        barChartTemp.getXAxis().setTextColor(textColor);
        barChartTemp.getAxisLeft().setTextColor(textColor);
        barChartTemp.getAxisRight().setEnabled(false);
        barChartTemp.getLegend().setTextColor(textColor);

        lineChartHumidity.getDescription().setEnabled(false);
        lineChartHumidity.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        lineChartHumidity.getXAxis().setGranularity(1f);
        lineChartHumidity.getXAxis().setValueFormatter(xAxisFormatter);
        lineChartHumidity.getXAxis().setTextColor(textColor);
        lineChartHumidity.getAxisLeft().setTextColor(textColor);
        lineChartHumidity.getAxisRight().setEnabled(false);
        lineChartHumidity.getLegend().setTextColor(textColor);
    }

    private void loadChartData() {
        ArrayList<BarEntry> tempData = new ArrayList<>();
        ArrayList<Entry> humidityData = new ArrayList<>();
        double totalTemp = 0, totalHumidity = 0;
        int validTempCount = 0, validHumidityCount = 0;

        for (int i = 0; i < historyList.size(); i++) {
            HistoricalDataItem item = historyList.get(i);
            if (item != null && item.getData() != null) {
                HistoricalDataValues values = item.getData();

                // --- Lógica de carga segura ---
                try {
                    float temp = (float) values.getTemperature();
                    tempData.add(new BarEntry(i, temp));
                    totalTemp += temp;
                    validTempCount++;
                } catch (NumberFormatException e) { /* Ignorar si no es un número */ }

                try {
                    float humidity = values.getHumidity();
                    humidityData.add(new Entry(i, humidity));
                    totalHumidity += humidity;
                    validHumidityCount++;
                } catch (NumberFormatException e) { /* Ignorar si no es un número */ }
            }
        }

        if (validTempCount > 0) {
            double avgTemp = totalTemp / validTempCount;
            tvTempAverage.setText(String.format(Locale.getDefault(), "Media: %.1f °C", avgTemp));
        }

        if (validHumidityCount > 0) {
            double avgHumidity = totalHumidity / validHumidityCount;
            tvHumidityAverage.setText(String.format(Locale.getDefault(), "Media: %.1f %%", avgHumidity));
        }

        int textColor = isDarkTheme() ? Color.WHITE : Color.BLACK;
        int tempColor = isDarkTheme() ? Color.parseColor("#FF8A65") : Color.parseColor("#FF5722");
        int humidityColor = isDarkTheme() ? Color.parseColor("#64B5F6") : Color.parseColor("#2196F3");

        BarDataSet tempDataSet = new BarDataSet(tempData, "Temperatura");
        tempDataSet.setColor(tempColor);
        tempDataSet.setValueTextColor(textColor);
        BarData barData = new BarData(tempDataSet);
        barData.setBarWidth(0.5f);
        barChartTemp.setData(barData);
        barChartTemp.setFitBars(true);
        barChartTemp.invalidate();

        LineDataSet humidityDataSet = new LineDataSet(humidityData, "Humedad");
        humidityDataSet.setColor(humidityColor);
        humidityDataSet.setCircleColor(humidityColor);
        humidityDataSet.setDrawFilled(true);
        GradientDrawable gradient = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[]{humidityColor, Color.TRANSPARENT});
        humidityDataSet.setFillDrawable(gradient);
        humidityDataSet.setValueTextColor(textColor);
        LineData lineData = new LineData(humidityDataSet);
        lineChartHumidity.setData(lineData);
        lineChartHumidity.invalidate();
    }

    private boolean isDarkTheme() {
        int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        return currentNightMode == Configuration.UI_MODE_NIGHT_YES;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private static class DateAxisValueFormatter extends ValueFormatter {
        private final List<HistoricalDataItem> data;
        private final SimpleDateFormat apiFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
        private final SimpleDateFormat displayFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

        DateAxisValueFormatter(List<HistoricalDataItem> data) {
            this.data = data;
        }

        @Override
        public String getAxisLabel(float value, com.github.mikephil.charting.components.AxisBase axis) {
            int index = (int) value;
            if (index >= 0 && index < data.size()) {
                try {
                    Date date = apiFormat.parse(data.get(index).getTimestamp());
                    return displayFormat.format(date);
                } catch (ParseException e) {
                    return "";
                }
            }
            return "";
        }
    }
}
