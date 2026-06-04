package logic;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import Database.ConectionDDBB;

public class Logic {

    public static void setDataToDB(String jsonContent) {
        ConectionDDBB conector = new ConectionDDBB();
        Connection con = null;
        try {
            Log.log.info("DEBUG JSON RECIBIDO: " + jsonContent);

            JsonObject json = JsonParser.parseString(jsonContent).getAsJsonObject();
            
            // --- EXTRACCIÓN DE CAMPOS DEL JSON ---
            String sensorId = getSafeString(json, "sensor_id");
            String streetId = getSafeString(json, "street_id");
            String sensorType = getSafeString(json, "sensor_type");
            String timestampStr = getSafeString(json, "timestamp").replace("T", " ").replace("Z", "");
            
            JsonObject loc = json.has("location") ? json.getAsJsonObject("location") : new JsonObject();
            String address = getSafeString(loc, "address");
            String district = getSafeString(loc, "district");
            String neighborhood = getSafeString(loc, "neighborhood");
            double lat = getSafeDouble(loc, "latitude");
            double lon = getSafeDouble(loc, "longitude");
            
            JsonObject data = json.has("data") ? json.getAsJsonObject("data") : new JsonObject();
            double temp = getSafeDouble(data, "temperature_celsius");
            double hum = getSafeDouble(data, "humidity_percent");
            int bright = getSafeInt(data, "brightness_level");
            String status = getSafeString(data, "status_msg");

            // --- INSERCIONES SQL ---
            con = conector.obtainConnection(false); 

            // Insertar CALLE
            PreparedStatement psCalle = ConectionDDBB.InsertCalle(con);
            psCalle.setString(1, streetId);
            psCalle.setString(2, address);
            psCalle.setString(3, district);
            psCalle.setString(4, neighborhood);
            psCalle.setDouble(5, lat);
            psCalle.setDouble(6, lon);
            psCalle.executeUpdate();

            // Insertar DISPOSITIVO
            PreparedStatement psDev = ConectionDDBB.InsertDispositivo(con);
            psDev.setString(1, sensorId);
            psDev.setString(2, sensorType);
            psDev.setString(3, streetId);
            psDev.executeUpdate();

            // Insertar TELEMETRÍA
            PreparedStatement psTel = ConectionDDBB.InsertTelemetria(con);
            psTel.setString(1, sensorId);
            psTel.setString(2, timestampStr.isEmpty() ? null : timestampStr); 
            psTel.setDouble(3, temp);
            psTel.setDouble(4, hum);
            psTel.setInt(5, bright);
            psTel.setString(6, status);
            psTel.setString(7, jsonContent); 
            psTel.executeUpdate();

            con.commit(); 
            Log.log.info("Datos guardados correctamente para: " + sensorId);

        } catch (Exception e) {
            Log.log.error("Error guardando datos: " + e);
            e.printStackTrace(); 
            try { if(con!=null) con.rollback(); } catch(Exception ex){}
        } finally {
            conector.closeConnection(con);
        }
    }

    // --- MÉTODOS AUXILIARES SEGUROS (Evitan el NullPointerException) ---
    private static String getSafeString(JsonObject obj, String key) {
        if (obj.has(key) && !obj.get(key).isJsonNull()) {
            return obj.get(key).getAsString();
        }
        return "Desconocido"; 
    }

    private static double getSafeDouble(JsonObject obj, String key) {
        if (obj.has(key) && !obj.get(key).isJsonNull()) {
            return obj.get(key).getAsDouble();
        }
        return 0.0;
    }

    private static int getSafeInt(JsonObject obj, String key) {
        if (obj.has(key) && !obj.get(key).isJsonNull()) {
            return obj.get(key).getAsInt();
        }
        return 0;
    }

    // --- MÉTODO PARA RECUPERAR DATOS (JOIN) ---
    public static ArrayList<Measurement> getDataFromDB() {
        ArrayList<Measurement> values = new ArrayList<>();
        ConectionDDBB conector = new ConectionDDBB();
        Connection con = null;
        try {
            con = conector.obtainConnection(true);
            PreparedStatement ps = ConectionDDBB.GetAllJoined(con);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Measurement m = new Measurement();
                m.setJsonData(rs.getString("RAW_JSON"));
                m.setDate(rs.getTimestamp("TIMESTAMP"));
                values.add(m);
            }
        } catch (Exception e) { Log.log.error("Error get: " + e); }
        conector.closeConnection(con);
        return values;
    }
    
    // Nuevo Endpoin GetLast
    public static String getLastDataJSON() {
        ConectionDDBB conector = new ConectionDDBB();
        Connection con = null;
        String result = "{}";
        try {
            con = conector.obtainConnection(true);
            PreparedStatement ps = ConectionDDBB.GetLastOne(con);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                result = rs.getString("RAW_JSON");
            }
        } catch (Exception e) { Log.log.error("Error get last: " + e); }
        conector.closeConnection(con);
        return result;
    }

    // Nuevo Endpoin GetBySensor
    public static ArrayList<Measurement> getDataBySensor(String id) {
        ArrayList<Measurement> values = new ArrayList<>();
        ConectionDDBB conector = new ConectionDDBB();
        Connection con = null;
        try {
            con = conector.obtainConnection(true);
            // Llamamos a la nueva consulta pasándole el ID
            PreparedStatement ps = ConectionDDBB.GetBySensorID(con, id);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Measurement m = new Measurement();
                m.setJsonData(rs.getString("RAW_JSON"));
                m.setDate(rs.getTimestamp("TIMESTAMP"));
                values.add(m);
            }
        } catch (Exception e) { Log.log.error("Error get by sensor: " + e); }
        conector.closeConnection(con);
        return values;
    }

    // Nuevo Endpoin GetStats
    public static JsonObject getStats() {
        ConectionDDBB conector = new ConectionDDBB();
        Connection con = null;
        JsonObject stats = new JsonObject();
        
        try {
            con = conector.obtainConnection(true);
            PreparedStatement ps = ConectionDDBB.GetAggregatedStats(con);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                stats.addProperty("type", "global_statistics");
                stats.addProperty("total_records", rs.getInt("TOTAL_RECORDS"));
                
                // Si no hay datos, AVG devuelve 0 o null, lo gestionamos
                stats.addProperty("average_temperature", rs.getDouble("AVG_TEMP"));
                stats.addProperty("average_humidity", rs.getDouble("AVG_HUM"));
                stats.addProperty("average_brightness", rs.getInt("AVG_BRIGHT"));
            }
            
        } catch (Exception e) {
            Log.log.error("Error getting stats: " + e);
            stats.addProperty("error", "Could not calculate stats");
        } finally {
            conector.closeConnection(con);
        }
        return stats;
    }

    // Nuevo Endpoin GetByDate
    public static ArrayList<Measurement> getDataByDate(String date) {
        ArrayList<Measurement> values = new ArrayList<>();
        ConectionDDBB conector = new ConectionDDBB();
        Connection con = null;
        try {
            con = conector.obtainConnection(true);
            
            // Llamamos a la consulta de fecha
            PreparedStatement ps = ConectionDDBB.GetByDateBD(con, date);
            Log.log.info("Query Date=>" + ps.toString());
            
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Measurement m = new Measurement();
                m.setJsonData(rs.getString("RAW_JSON"));
                m.setDate(rs.getTimestamp("TIMESTAMP"));
                values.add(m);
            }
        } catch (Exception e) {
            Log.log.error("Error getting data by date: " + e);
        } finally {
            conector.closeConnection(con);
        }
        return values;
    }
    // --- NUEVO ENDPOINT: OBTENER LISTA DE CALLES ---
    public static com.google.gson.JsonArray getCallesList() {
        com.google.gson.JsonArray callesArray = new com.google.gson.JsonArray();
        ConectionDDBB conector = new ConectionDDBB();
        Connection con = null;
        
        try {
            con = conector.obtainConnection(true);
            // Llamamos a la función de ConectionDDBB
            PreparedStatement ps = ConectionDDBB.GetCalles(con);
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                com.google.gson.JsonObject calle = new com.google.gson.JsonObject();
                // Aquí cogemos solo ID y Address como querías
                calle.addProperty("id", rs.getString("STREET_ID"));
                calle.addProperty("nombre", rs.getString("ADDRESS"));
                callesArray.add(calle);
            }
        } catch (Exception e) {
            Log.log.error("Error getting streets list: " + e);
        } finally {
            conector.closeConnection(con);
        }
        return callesArray;
    }
}