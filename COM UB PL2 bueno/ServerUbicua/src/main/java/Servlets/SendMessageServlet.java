package Servlets; 


import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import logic.Log;
import mqtt.MQTTBroker;
import mqtt.MQTTPublisher;

@WebServlet("/SendMessage")
public class SendMessageServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        
        // 1. Recoger el mensaje de los parámetros de la URL (?msg=Hola)
        String mensaje = req.getParameter("msg");
        
        // Validación básica
        if (mensaje == null || mensaje.trim().isEmpty()) {
            resp.getWriter().write("ERROR: Debes indicar un mensaje. Ejemplo: /SendMessage?msg=HolaMundo");
            return;
        }

        // 2. Definir el Topic 
        String topic = "Sensors/ST_0155/actuador/mensaje";

        try {
            Log.log.info("--> Iniciando envio de mensaje al ESP32: " + mensaje);
            
            // 3. Usar tu clase MQTTPublisher 
            MQTTBroker brokerInstance = new MQTTBroker(); 
            
            MQTTPublisher.publish(brokerInstance, topic, mensaje);
            
            // 4. Responder al navegador
            resp.getWriter().write("EXITO: Mensaje enviado al topic " + topic + " | Contenido: " + mensaje);
            Log.log.info("--> Mensaje enviado correctamente.");

        } catch (Exception e) {
            Log.log.error("Error enviando mensaje: " + e.getMessage());
            resp.sendError(500, "Error enviando mensaje MQTT: " + e.getMessage());
        }
    }
}