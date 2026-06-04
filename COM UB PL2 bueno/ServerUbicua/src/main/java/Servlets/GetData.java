package servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import logic.Log;
import logic.Logic;
import logic.Measurement;

/**
 * Servlet implementation class GetData
 */
@WebServlet("/GetData")
public class GetData extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    public GetData() {
        super();
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Log.log.info("--Get values form DB and formatting JSON--");
        
        response.setContentType("application/json;charset=UTF-8");
        
        PrintWriter out = response.getWriter();
        try 
        {
            // 1. Recuperamos los datos crudos de la BBDD
            ArrayList<Measurement> values = Logic.getDataFromDB();
            
            // 2. Creamos el Objeto Raíz que quieres
            JsonObject rootResponse = new JsonObject();
            // Añadimos la etiqueta de tipo de sensor al principio
            rootResponse.addProperty("sensor_type", "information_display");
            
            // 3. Creamos el Array "data"
            JsonArray dataArray = new JsonArray();
            
            // 4. Recorremos cada medición de la BBDD
            for (Measurement m : values) {
                try {
                    String rawJson = m.getJsonData();
                    if (rawJson != null && !rawJson.isEmpty()) {
                        JsonElement element = JsonParser.parseString(rawJson);
                        if (element.isJsonObject()) {
                            dataArray.add(element);
                        }
                    }
                } catch (Exception e) {
                    Log.log.warn("Error al parsear un JSON de la BBDD: " + e.getMessage());
                }
            }
            
            // 5. Metemos el array dentro del objeto raíz
            rootResponse.add("data", dataArray);
            
            // 6. Convertimos todo a texto y lo enviamos
            String finalJson = new Gson().toJson(rootResponse);
            Log.log.info("Response=>" + finalJson);
            out.println(finalJson);
            
        } catch (Exception e) 
        {
            JsonObject errorObj = new JsonObject();
            errorObj.addProperty("error", "Error obteniendo datos: " + e.getMessage());
            out.println(new Gson().toJson(errorObj));
            Log.log.error("Exception: " + e);
        } finally 
        {
            out.close();
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }
}