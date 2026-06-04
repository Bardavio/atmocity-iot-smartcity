package com.example.apppecl3;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {

    private List<HistoricalDataItem> dataList;

    public HistoryAdapter(List<HistoricalDataItem> dataList) {
        this.dataList = dataList != null ? dataList : new ArrayList<>();
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        HistoricalDataItem item = dataList.get(position);
        if (item != null && item.getData() != null) {
            holder.bind(item);
        }
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateList(List<HistoricalDataItem> newList) {
        dataList.clear();
        if (newList != null) {
            dataList.addAll(newList);
        }
        notifyDataSetChanged();
    }

    // --- MÉTODO NUEVO ---
    public List<HistoricalDataItem> getCurrentList() {
        return this.dataList;
    }
    // --- FIN DEL MÉTODO ---

    static class HistoryViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvTimestamp, tvTemp, tvHumidity, tvBrightness;

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTimestamp = itemView.findViewById(R.id.tvHistoryTimestamp);
            tvTemp = itemView.findViewById(R.id.tvHistoryTemp);
            tvHumidity = itemView.findViewById(R.id.tvHistoryHumidity);
            tvBrightness = itemView.findViewById(R.id.tvHistoryBrightness);
        }

        public void bind(HistoricalDataItem item) {
            try {
                DateTimeFormatter inputFormatter = DateTimeFormatter.ISO_DATE_TIME;
                LocalDateTime dateTime = LocalDateTime.parse(item.getTimestamp(), inputFormatter);
                DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
                tvTimestamp.setText(dateTime.format(outputFormatter));
            } catch (Exception e) {
                tvTimestamp.setText(item.getTimestamp());
            }

            HistoricalDataValues values = item.getData();

            // Temperatura
            String tempValue = values.getTemperatureString();
            if (tempValue != null && !tempValue.trim().isEmpty()) {
                try {
                    double temp = Double.parseDouble(tempValue);
                    tvTemp.setText(String.format(Locale.getDefault(), "%.1f°C", temp));
                } catch (NumberFormatException e) {
                    tvTemp.setText("N/A");
                }
            } else {
                tvTemp.setText("N/A");
            }

            // Humedad
            String humidityValue = values.getHumidityString();
            if (humidityValue != null && !humidityValue.trim().isEmpty()) {
                try {
                    if (humidityValue.contains(".")) {
                        humidityValue = humidityValue.substring(0, humidityValue.indexOf('.'));
                    }
                    int humidity = Integer.parseInt(humidityValue);
                    tvHumidity.setText(String.format(Locale.getDefault(), "%d%%", humidity));
                } catch (NumberFormatException e) {
                    tvHumidity.setText("N/A");
                }
            } else {
                tvHumidity.setText("N/A");
            }

            // Luminosidad
            String brightnessValue = values.getBrightnessString();
            if (brightnessValue != null && !brightnessValue.trim().isEmpty()) {
                try {
                    if (brightnessValue.contains(".")) {
                        brightnessValue = brightnessValue.substring(0, brightnessValue.indexOf('.'));
                    }
                    int brightness = Integer.parseInt(brightnessValue);
                    tvBrightness.setText(String.format(Locale.getDefault(), "%d lux", brightness));
                } catch (NumberFormatException e) {
                    tvBrightness.setText("N/A");
                }
            } else {
                tvBrightness.setText("N/A");
            }
        }
    }
}
