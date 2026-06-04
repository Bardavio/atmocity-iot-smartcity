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

@WebServlet("/GetBySensor")
public class GetBySensor extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();
        try {
            // 1. Leemos el parámetro ?id=... de la URL
            String id = request.getParameter("id");
            
            if (id == null) {
                out.println("{\"error\": \"Falta el parametro 'id'\"}");
                return;
            }

            // 2. Pedimos los datos filtrados
            ArrayList<Measurement> values = Logic.getDataBySensor(id);
            
            // 3. Construimos el JSON (igual que en GetData)
            JsonObject root = new JsonObject();
            root.addProperty("filter", "sensor_id: " + id);
            JsonArray data = new JsonArray();
            
            for (Measurement m : values) {
                try {
                    JsonElement element = JsonParser.parseString(m.getJsonData());
                    if (element.isJsonObject()) data.add(element);
                } catch (Exception e) {}
            }
            root.add("data", data);
            
            out.println(new Gson().toJson(root));
            
        } catch (Exception e) {
            Log.log.error("Exception: " + e);
        } finally {
            out.close();
        }
    }
}