package mqtt;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import logic.Log;
import logic.Logic;

public class MQTTSuscriber implements MqttCallback {

    private MqttClient client;
    private String brokerUrl;
    private String clientId;
    private String username;
    private String password;

    public MQTTSuscriber(MQTTBroker broker) {
        this.brokerUrl = broker.getBroker();
        this.clientId = broker.getClientId();
        this.username = broker.getUsername();
        this.password = broker.getPassword();
    }

    public void subscribeTopic(String topic) {
        try {
            MemoryPersistence persistence = new MemoryPersistence();
            client = new MqttClient(brokerUrl, MQTTBroker.getSubscriberClientId(), persistence);

            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setUserName(username);
            connOpts.setPassword(password.toCharArray());
            connOpts.setCleanSession(true);
            connOpts.setAutomaticReconnect(true);
            connOpts.setConnectionTimeout(10);

            client.setCallback(this);
            client.connect(connOpts);

            client.subscribe(topic, 1); 
            Log.logmqtt.info("Subscribed to {}", topic);

        } catch (MqttException e) {
            Log.logmqtt.error("Error subscribing to topic: {}", e);
        }
    }

    @Override
    public void connectionLost(Throwable cause) {
        Log.logmqtt.warn("MQTT Connection lost, cause: {}", cause.getMessage());
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) {
        String payload = message.toString();
        Log.logmqtt.info("JSON RECIBIDO [Topic: {}]", topic);

        Logic.setDataToDB(payload);
        
        Log.logmqtt.info("--> JSON guardado en BBDD correctamente.");
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
    }
    
    public void disconnect() {
        if (client != null && client.isConnected()) {
            try {
                client.disconnect();
                client.close();
                Log.logmqtt.info("Cliente MQTT desconectado limpiamente.");
            } catch (MqttException e) {
                Log.logmqtt.error("Error al desconectar MQTT: {}", e.getMessage());
            }
        }
    }
}