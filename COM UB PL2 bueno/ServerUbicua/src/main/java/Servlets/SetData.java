package servlets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import logic.Log;
import logic.Logic;

@WebServlet("/SetData")
public class SetData extends HttpServlet {
    private static final long serialVersionUID = 1L;

    public SetData() {
        super();
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Log.log.info("--Set values into the DB (via HTTP POST)--");
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        try {
            // 1. Intentamos leer el cuerpo completo de la petición (JSON Raw)
            StringBuilder buffer = new StringBuilder();
            BufferedReader reader = request.getReader();
            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }
            String payload = buffer.toString();

            // 2. Si el cuerpo está vacío, intentamos leer el parámetro 'value' (Compatibilidad)
            if (payload.isEmpty()) {
                payload = request.getParameter("value");
            }

            // 3. Validamos que tenemos algo
            if (payload == null || payload.trim().isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST); // Error 400
                out.println("{\"error\": \"No se han recibido datos. Envia un JSON en el body.\"}");
                return;
            }

            // 4. Enviamos a la lógica
            Logic.setDataToDB(payload);

            // 5. Respuesta de éxito
            out.println("{\"status\": \"OK\", \"message\": \"Dato recibido y procesado\"}");

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); // Error 500
            out.println("{\"error\": \"" + e.getMessage() + "\"}");
            Log.log.error("Exception in SetData: " + e);
        } finally {
            out.close();
        }
    }

    // Redirigimos GET a POST por si acaso, aunque lo ideal es usar POST
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }
}