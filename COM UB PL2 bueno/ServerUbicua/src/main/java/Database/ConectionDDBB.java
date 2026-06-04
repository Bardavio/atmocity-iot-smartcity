package Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import logic.Log;

public class ConectionDDBB {

    public Connection obtainConnection(boolean autoCommit) throws NullPointerException {
        Connection con = null;
        int intentos = 5;
        for (int i = 0; i < intentos; i++) {
            Log.log.info("Attempt " + i + " to connect to the database");
            try {
                Context ctx = new InitialContext();
                DataSource ds = (DataSource) ctx.lookup("java:/comp/env/jdbc/ubicomp");
                con = ds.getConnection();
                Calendar calendar = Calendar.getInstance();
                java.sql.Date date = new java.sql.Date(calendar.getTime().getTime());
                Log.log.debug("Connection creation. Bd connection identifier: " + con.toString() + " obtained in " + date.toString());
                con.setAutoCommit(autoCommit);
                Log.log.info("Conection obtained in the attempt: " + i);
                i = intentos;
            } catch (NamingException ex) {
                Log.log.error("Error getting connection while trying: " + i + " = " + ex);
            } catch (SQLException ex) {
                Log.log.error("ERROR sql getting connection while trying: " + i + " = " + ex.getSQLState() + "\n" + ex.toString());
                throw (new NullPointerException("SQL connection is null"));
            }
        }
        return con;
    }

    public void closeTransaction(Connection con) {
        try {
            con.commit();
            Log.log.debug("Transaction closed");
        } catch (SQLException ex) {
            Log.log.error("Error closing the transaction: " + ex);
        }
    }

    public void cancelTransaction(Connection con) {
        try {
            con.rollback();
            Log.log.debug("Transaction canceled");
        } catch (SQLException ex) {
            Log.log.error("ERROR sql when canceling the transation: " + ex.getSQLState() + "\n" + ex.toString());
        }
    }

    public void closeConnection(Connection con) {
        try {
            Log.log.info("Closing the connection");
            if (null != con) {
                Calendar calendar = Calendar.getInstance();
                java.sql.Date date = new java.sql.Date(calendar.getTime().getTime());
                Log.log.debug("Connection closed. Bd connection identifier: " + con.toString() + " obtained in " + date.toString());
                con.close();
            }
            Log.log.info("The connection has been closed");
        } catch (SQLException e) {
            Log.log.error("ERROR sql closing the connection: " + e);
            e.printStackTrace();
        }
    }

    public static PreparedStatement getStatement(Connection con, String sql) {
        PreparedStatement ps = null;
        try {
            if (con != null) {
                ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            }
        } catch (SQLException ex) {
            Log.log.warn("ERROR sql creating PreparedStatement: " + ex.toString());
        }
        return ps;
    }


    // 1. Insertar Calle (Usa ON DUPLICATE KEY UPDATE para actualizar datos si cambia el nombre o ubicación)
    public static PreparedStatement InsertCalle(Connection con) {
        String sql = "INSERT IGNORE INTO CALLE (STREET_ID, ADDRESS, DISTRICT, NEIGHBORHOOD, LATITUDE, LONGITUDE) " +
                     "VALUES (?,?,?,?,?,?) " +
                     "ON DUPLICATE KEY UPDATE " +
                     "ADDRESS = VALUES(ADDRESS), " +
                     "DISTRICT = VALUES(DISTRICT), " +
                     "NEIGHBORHOOD = VALUES(NEIGHBORHOOD), " +
                     "LATITUDE = VALUES(LATITUDE), " +
                     "LONGITUDE = VALUES(LONGITUDE)";
                     
        return getStatement(con, sql);
    }

    // 2. Insertar Dispositivo (Actualiza la info si el sensor ya existe)
    public static PreparedStatement InsertDispositivo(Connection con) {
        String sql = "INSERT IGNORE INTO DISPOSITIVO (SENSOR_ID, SENSOR_TYPE, STREET_ID) " +
                     "VALUES (?,?,?) " +
                     "ON DUPLICATE KEY UPDATE " +
                     "SENSOR_TYPE = VALUES(SENSOR_TYPE), " +
                     "STREET_ID = VALUES(STREET_ID)";
                     
        return getStatement(con, sql);
    }

    // 3. Insertar Telemetría (Siempre guarda el dato histórico)
    public static PreparedStatement InsertTelemetria(Connection con) {
        return getStatement(con, "INSERT INTO TELEMETRIA (SENSOR_ID, TIMESTAMP, TEMP, HUM, BRIGHTNESS, STATUS_MSG, RAW_JSON) VALUES (?,?,?,?,?,?,?)");
    }

    // Se usa para el endpoint principal /GetData
    public static PreparedStatement GetAllJoined(Connection con) {
        String sql = "SELECT T.*, D.SENSOR_TYPE, C.STREET_ID, C.ADDRESS, C.DISTRICT, C.NEIGHBORHOOD, C.LATITUDE, C.LONGITUDE " +
                     "FROM TELEMETRIA T " +
                     "JOIN DISPOSITIVO D ON T.SENSOR_ID = D.SENSOR_ID " +
                     "JOIN CALLE C ON D.STREET_ID = C.STREET_ID " +
                     "ORDER BY T.TIMESTAMP DESC";
        return getStatement(con, sql);
    }

    // Se usa para el nuevo endpoint /GetLast
    public static PreparedStatement GetLastOne(Connection con) {
        String sql = "SELECT T.*, D.SENSOR_TYPE, C.STREET_ID, C.ADDRESS, C.DISTRICT, C.NEIGHBORHOOD, C.LATITUDE, C.LONGITUDE " +
                     "FROM TELEMETRIA T " +
                     "JOIN DISPOSITIVO D ON T.SENSOR_ID = D.SENSOR_ID " +
                     "JOIN CALLE C ON D.STREET_ID = C.STREET_ID " +
                     "ORDER BY T.TIMESTAMP DESC LIMIT 1";
        return getStatement(con, sql);
    }

    // Se usa para el nuevo endpoint /GetBySensor?id=XXX
    public static PreparedStatement GetBySensorID(Connection con, String sensorId) throws SQLException {
        String sql = "SELECT T.*, D.SENSOR_TYPE, C.STREET_ID, C.ADDRESS, C.DISTRICT, C.NEIGHBORHOOD, C.LATITUDE, C.LONGITUDE " +
                     "FROM TELEMETRIA T " +
                     "JOIN DISPOSITIVO D ON T.SENSOR_ID = D.SENSOR_ID " +
                     "JOIN CALLE C ON D.STREET_ID = C.STREET_ID " +
                     "WHERE T.SENSOR_ID = ? " +  // <-- EL FILTRO
                     "ORDER BY T.TIMESTAMP DESC";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setString(1, sensorId); // Inyectamos el ID de forma segura
        return ps;
    }

    // Se usa para el nuevo endpoint /GetStats
    public static PreparedStatement GetAggregatedStats(Connection con) {
        // Usamos ROUND(..., 2) para que no salgan 2 decimales
        String sql = "SELECT " +
                     "ROUND(AVG(TEMP), 2) as AVG_TEMP, " +
                     "ROUND(AVG(HUM), 2) as AVG_HUM, " +
                     "ROUND(AVG(BRIGHTNESS), 0) as AVG_BRIGHT, " +
                     "COUNT(*) as TOTAL_RECORDS " +
                     "FROM TELEMETRIA";
        return getStatement(con, sql);
    }

    // Se usa para el nuevo endpoint /GetByDate?date=YYYY-MM-DD
    public static PreparedStatement GetByDateBD(Connection con, String dateString) {
        // Usamos la función DATE() para ignorar la hora exacta
        String sql = "SELECT RAW_JSON, TIMESTAMP FROM TELEMETRIA " +
                     "WHERE DATE(TIMESTAMP) = ? " +
                     "ORDER BY TIMESTAMP DESC";
        
        PreparedStatement ps = null;
        try {
            ps = con.prepareStatement(sql);
            ps.setString(1, dateString);
        } catch (SQLException e) {
            Log.log.error("Error creating PreparedStatement: " + e);
        }
        return ps;
    }
    // --- NUEVO: Obtener lista de calles ---
    // 6. Obtener listado de calles (Solo ID y Nombre)
    public static PreparedStatement GetCalles(Connection con) {
        String sql = "SELECT STREET_ID, ADDRESS FROM CALLE"; 
        return getStatement(con, sql);
    }
}
