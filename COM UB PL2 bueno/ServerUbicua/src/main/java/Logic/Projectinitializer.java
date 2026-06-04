package logic;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import mqtt.MQTTBroker;
import mqtt.MQTTSuscriber;

@WebListener
public class Projectinitializer implements ServletContextListener {
    private MQTTSuscriber suscriber;

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        Log.log.info("--> Deteniendo la aplicación y cerrando MQTT <--");
        if (suscriber != null) {
            suscriber.disconnect();
        }
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        Log.log.info("--> Suscribiendo a Topics MQTT <--");
        MQTTBroker broker = new MQTTBroker();
        suscriber = new MQTTSuscriber(broker);
        
        suscriber.subscribeTopic("Sensors/+/sensor/telemetry");
        
        Log.log.info("--> Sistema Iniciado y escuchando JSONs <--");
    }
}