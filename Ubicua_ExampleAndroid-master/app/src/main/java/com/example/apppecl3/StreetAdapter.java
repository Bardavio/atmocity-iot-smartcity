package com.example.apppecl3;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class StreetAdapter extends RecyclerView.Adapter<StreetAdapter.SensorViewHolder> {

    private static final String TAG = "SensorAdapter";
    private final List<SensorData> dataList;
    private final Context context;

    public StreetAdapter(List<SensorData> dataList, Context context) {
        this.dataList = dataList != null ? dataList : new ArrayList<>();
        this.context = context;
        Log.d(TAG, "Adaptador creado.");
    }

    @NonNull
    @Override
    public SensorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_street_data, parent, false);
        return new SensorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SensorViewHolder holder, int position) {
        SensorData currentItem = dataList.get(position);

        holder.keyTextView.setText(currentItem.getName());

        String value = currentItem.getValue();
        String formattedValue = value;

        // Asigna un icono y formato específico según el tipo de dato.
        switch (currentItem.getType()) {
            case TEMPERATURE:
                holder.iconImageView.setImageResource(R.drawable.ic_temperature);
                formattedValue = value.equals("null") ? "--" : value + " °C";
                break;

            case HUMIDITY:
                holder.iconImageView.setImageResource(R.drawable.ic_humidity);
                formattedValue = value.equals("null") ? "--" : value + " %";
                break;

            case BRIGHTNESS:
                holder.iconImageView.setImageResource(R.drawable.ic_brightness);
                formattedValue = value.equals("null") ? "--" : value + " lux";
                break;
        }

        holder.valueTextView.setText(formattedValue);
        holder.descriptionTextView.setVisibility(View.GONE);
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public void updateData(List<SensorData> newData) {
        dataList.clear();
        if (newData != null) {
            dataList.addAll(newData);
        }
        notifyDataSetChanged();
    }

    public static class SensorViewHolder extends RecyclerView.ViewHolder {
        public ImageView iconImageView;
        public TextView keyTextView;
        public TextView descriptionTextView;
        public TextView valueTextView;

        public SensorViewHolder(@NonNull View itemView) {
            super(itemView);
            iconImageView = itemView.findViewById(R.id.ivIcon);
            keyTextView = itemView.findViewById(R.id.tvDataKey);
            descriptionTextView = itemView.findViewById(R.id.tvDataDescription);
            valueTextView = itemView.findViewById(R.id.tvDataValue);
        }
    }
}
