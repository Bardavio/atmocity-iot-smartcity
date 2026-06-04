package servlets;

import java.io.IOException;
import java.io.PrintWriter;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import logic.Log;
import logic.Logic;

@WebServlet("/GetStats")
public class GetStats extends HttpServlet {
    private static final long serialVersionUID = 1L;

    public GetStats() {
        super();
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Log.log.info("-- Get Statistics --");
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();
        
        try {
            // Llamamos a la lógica
            JsonObject stats = Logic.getStats();
            
            // Enviamos el JSON
            out.println(new Gson().toJson(stats));
            
        } catch (Exception e) {
            Log.log.error("Exception in GetStats: " + e);
        } finally {
            out.close();
        }
    }
    
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }
}