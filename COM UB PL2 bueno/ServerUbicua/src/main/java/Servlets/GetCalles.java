package servlets;

import java.io.IOException;
import java.io.PrintWriter;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import logic.Log;
import logic.Logic;

@WebServlet("/GetCalles")
public class GetCalles extends HttpServlet {
    private static final long serialVersionUID = 1L;

    public GetCalles() {
        super();
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Log.log.info("-- Get List of Streets --");

        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        try {
            // 1. Pedimos la lista ya cocinada a la Lógica
            JsonArray listaCalles = Logic.getCallesList();
            
            // 2. Montamos el JSON de respuesta
            JsonObject rootResponse = new JsonObject();
            rootResponse.addProperty("info", "Listado de calles");
            rootResponse.add("calles", listaCalles);
            
            // 3. Enviamos
            String finalJson = new Gson().toJson(rootResponse);
            out.println(finalJson);

        } catch (Exception e) {
            Log.log.error("Error en Servlet GetCalles: " + e);
            out.println("{\"error\": \"" + e.getMessage() + "\"}");
        } finally {
            out.close();
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }
}