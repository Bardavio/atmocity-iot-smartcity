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

@WebServlet("/GetByDate")
public class GetByDate extends HttpServlet {
    private static final long serialVersionUID = 1L;

    public GetByDate() {
        super();
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Log.log.info("-- Get Data By Date --");
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();
        
        try {
            // 1. Leemos el parámetro ?date=YYYY-MM-DD
            String dateParam = request.getParameter("date");
            
            if (dateParam == null || dateParam.isEmpty()) {
                out.println("{\"error\": \"Falta el parametro 'date'. Ejemplo: ?date=2025-11-30\"}");
                return;
            }

            // 2. Pedimos los datos
            ArrayList<Measurement> values = Logic.getDataByDate(dateParam);
            
            // 3. Construimos el JSON
            JsonObject root = new JsonObject();
            root.addProperty("filter_date", dateParam);
            root.addProperty("count", values.size());
            
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
            out.println("{\"error\": \"" + e.getMessage() + "\"}");
        } finally {
            out.close();
        }
    }
}