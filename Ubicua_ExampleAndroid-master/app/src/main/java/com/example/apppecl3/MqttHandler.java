// Archivo: MqttHandler.java (NUEVA VERSIÓN MODERNA)
package com.example.apppecl3;

import android.util.Log;

import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient;

import java.util.UUID;

public class MqttHandler {

    private static final String TAG = "MqttHandler";
    private final Mqtt3AsyncClient client;

    // Interfaz para devolver los mensajes a la Activity
    public interface MqttMessageListener {
        void onMqttMessageReceived(String topic, String payload);
    }
    private MqttMessageListener messageListener;

    public void setMessageListener(MqttMessageListener listener) {
        this.messageListener = listener;
    }

    public MqttHandler(String serverHost, int serverPort) {
        client = MqttClient.builder()
                .useMqttVersion3()
                .identifier(UUID.randomUUID().toString())
                .serverHost(serverHost)
                .serverPort(serverPort)
                .buildAsync();
    }

    public void connect() {
        client.connect()
                .whenComplete((connAck, throwable) -> {
                    if (throwable != null) {
                        Log.e(TAG, "Fallo en la conexión!", throwable);
                    } else {
                        Log.d(TAG, "Conexión exitosa!");
                    }
                });
    }

    public void disconnect() {
        client.disconnect().whenComplete((unused, throwable) -> Log.d(TAG, "Desconectado."));    }

    public void subscribeToTopic(String topic) {
        client.subscribeWith()
                .topicFilter(topic)
                .callback(publish -> {
                    String payload = new String(publish.getPayloadAsBytes());
                    Log.d(TAG, "Mensaje recibido en '" + topic + "': " + payload);
                    if (messageListener != null) {
                        messageListener.onMqttMessageReceived(topic, payload);
                    }
                })
                .send()
                .whenComplete((subAck, throwable) -> {
                    if (throwable != null) {
                        Log.e(TAG, "Fallo al suscribirse a " + topic, throwable);
                    } else {
                        Log.d(TAG, "Suscrito al topic: " + topic);
                    }
                });
    }
}
