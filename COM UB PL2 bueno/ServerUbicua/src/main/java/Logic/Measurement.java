package logic;

import java.sql.Timestamp;

public class Measurement {
    // Cambiamos 'int value' por 'String jsonData'
    private String jsonData; 
    private Timestamp date;
 
    public Measurement() {
        this.jsonData = "";
        this.date = null;
    }

    public String getJsonData() {
        return jsonData;
    }

    public void setJsonData(String jsonData) {
        this.jsonData = jsonData;
    }

    public Timestamp getDate() {
        return date;
    }

    public void setDate(Timestamp date) {
        this.date = date;
    }
}