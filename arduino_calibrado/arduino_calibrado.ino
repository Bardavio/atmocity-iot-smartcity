/***** Pantalla de información con ESP32 + DHT11 + LUZ DIGITAL (DO) + LCD 20x4 y conexión MQTT *****/
// SIMULACIÓN MULTI-SENSOR PARA RELLENAR BASE DE DATOS

#include <WiFi.h>
#include <PubSubClient.h>
#include <Wire.h>
#include <LiquidCrystal_I2C.h>
#include "DHT.h"
#include <ArduinoJson.h> 

/* ================== HARDWARE ================== */
#define DHTPIN   17
#define DHTTYPE  DHT11
#define SDA_PIN  21
#define SCL_PIN  22
#define LCD_ADDR 0x27
#define LCD_COLS 20
#define LCD_ROWS 4
#define LUZ_PIN         25        
#define LUZ_ACTIVE_HIGH 0         

LiquidCrystal_I2C lcd(LCD_ADDR, LCD_COLS, LCD_ROWS);
DHT dht(DHTPIN, DHTTYPE);

/* ================== WIFI/MQTT ================== */
const char* ssid     = "POCO X7 Pro";
const char* password = "Bardavio";

const char* mqtt_server = "192.168.0.28"; 
const int   mqtt_port   = 1883;
const char* mqtt_user   = "";
const char* mqtt_pass   = "";

WiFiClient espClient;
PubSubClient client(espClient);

/* ================== CONFIGURACIÓN DE SIMULACIÓN ================== */

// Array de configuraciones para simular 3 sensores distintos
struct SensorConfig {
  const char* sensor_id;
  const char* street_id;
  const char* address;
  const char* district;     // Añadido para completar info
  const char* neighborhood; // Añadido para completar info
  double lat;
  double lon;
  const char* topic_pub;    // Topic donde publica este sensor
};

SensorConfig sensores[] = {
  // 1. TU CALLE ORIGINAL (Vallecas)
  { "ST_0155_ESP32_01", "ST_0155", "Calle de los Montes Alberes", "Villa de Vallecas", "Ensanche", 40.3738099, -3.6189204, "Sensors/ST_0155/sensor/telemetry" },
  
  // 2. NUEVA CALLE: ALCALÁ DE HENARES (Calle Pedro Gumiel)
  { "ST_0156_ESP32_01", "ST_0156", "Calle Pedro Gumiel", "Alcala de Henares", "Campus", 40.4826896, -3.3637838, "Sensors/ST_0156/sensor/telemetry" },
  
  // 3. NUEVA CALLE: MADRID CENTRO (Paseo del Prado)
  { "ST_0157_ESP32_01", "ST_0157", "Paseo Del Prado", "Retiro", "Jerónimos", 40.4138118, -3.6926035, "Sensors/ST_0157/sensor/telemetry" }
};

// Topics para suscribirse (escuchar comandos)
const char* T_CMD_MENSAJE = "Sensors/+/actuador/mensaje"; // Escuchamos de TODOS los sensores (+)
const char* BASE_LCD      = "Sensors/+/sensor/lcd/#"; 

/* ================== UTILS ================== */
void printLine(uint8_t row, String text) {
  if (row >= LCD_ROWS) return;
  String padding = "";
  for(int i=text.length(); i<LCD_COLS; i++) padding += " ";
  lcd.setCursor(0, row); 
  lcd.print(text + padding);
}

String iso8601now() {
  time_t now = time(nullptr);
  struct tm * t = gmtime(&now);
  char buf[25];
  if (t) strftime(buf, sizeof(buf), "%Y-%m-%dT%H:%M:%SZ", t);
  else   snprintf(buf, sizeof(buf), "1970-01-01T00:00:00Z");
  return String(buf);
}

/* ================== MQTT CALLBACK ================== */
void callback(char* topic, byte* message, unsigned int length) {
  String t = topic;
  String messageStr = "";
  for (unsigned int i = 0; i < length; i++) messageStr += (char)message[i];

  Serial.print("RX ["); Serial.print(t); Serial.print("]: "); Serial.println(messageStr);

  // IMPORTANTE: Como simulamos 3 sensores, solo procesamos mensajes dirigidos a UNO de ellos
  // para no volver loca la pantalla LCD. Vamos a elegir el ST_0155 como el "Principal" para mostrar en pantalla.
  
  if (t.indexOf("ST_0155") != -1) { // Solo actualizamos LCD si es para el sensor 1
      if (t.endsWith("/telemetry")) {
         // Lógica de mostrar datos en LCD (Igual que antes)
         JsonDocument doc; 
         DeserializationError error = deserializeJson(doc, messageStr);
         if (!error) {
             float temp = doc["data"]["temperature_celsius"]; 
             float hum  = doc["data"]["humidity_percent"];
             const char* statusMsg = doc["data"]["status_msg"]; 
             int bright = doc["data"]["brightness_level"];
             
             String strTemp = doc["data"]["temperature_celsius"].isNull() ? "--" : String(temp, 1);
             String strHum  = doc["data"]["humidity_percent"].isNull() ? "--" : String(hum, 0);
             String strStat = statusMsg ? String(statusMsg) : "Sin estado";
             
             printLine(0, "T:" + strTemp + "C H:" + strHum + "%");
             printLine(1, strStat);
             String luzTxt = (bright > 0) ? "Luz: ON" : "Luz: OFF";
             printLine(2, luzTxt);
             const char* ts = doc["timestamp"];
             String hora = String(ts).substring(11, 19); 
             printLine(3, "Update: " + hora);
         }
      } else if (t.endsWith("/mensaje")) {
          // Mensaje Actuador
          printLine(3, "SRV: " + messageStr);
          for(int i=0; i<3; i++){ lcd.noBacklight(); delay(200); lcd.backlight(); delay(200); }
      }
  }
}

/* ================== WIFI / MQTT SETUP ================== */
void setup_wifi() {
  WiFi.mode(WIFI_STA);
  WiFi.setSleep(false); 
  Serial.print("Conectando a "); Serial.println(ssid);
  WiFi.begin(ssid, password);
  while (WiFi.status() != WL_CONNECTED) { delay(500); Serial.print("."); }
  Serial.println("\nWiFi conectado.");
}

void reconnect() {
  while (!client.connected()) {
    Serial.print("Intentando conexión MQTT...");
    String clientId = "ESP32-MultiSim-" + String((uint32_t)ESP.getEfuseMac(), HEX);

    if (client.connect(clientId.c_str(), mqtt_user, mqtt_pass)) {
      Serial.println("conectado");
      // Nos suscribimos a topics con comodín (+) para escuchar a cualquiera de los 3 simulados
      client.subscribe("Sensors/+/sensor/telemetry"); 
      client.subscribe("Sensors/+/actuador/mensaje");
    } else {
      Serial.print("falló, rc="); Serial.print(client.state());
      Serial.println(" reintento en 5s");
      delay(5000);
    }
  }
}

/* ================== SETUP ================== */
void setup() {
  Serial.begin(115200);
  Wire.begin(SDA_PIN, SCL_PIN);
  lcd.init(); lcd.backlight(); lcd.clear();
  printLine(0, "Simulando 3 Sensores");

  dht.begin();
  pinMode(LUZ_PIN, INPUT_PULLUP); 
  configTime(0, 0, "pool.ntp.org", "time.nist.gov");

  setup_wifi();
  client.setServer(mqtt_server, mqtt_port);
  client.setBufferSize(4096); // Aumentamos buffer por si acaso
  client.setCallback(callback);
}

/* ================== LOOP (LA MAGIA ESTÁ AQUÍ) ================== */
void loop() {
  if (WiFi.status() != WL_CONNECTED) { delay(500); return; }
  if (!client.connected()) reconnect();
  client.loop(); 

  static unsigned long last = 0;
  if (millis() - last < 10000) return; // Publicamos cada 10 segundos (para no saturar)
  last = millis();

  // 1. LEER DATOS REALES (Los usamos para los 3 sensores simulados)
  float tempReal = dht.readTemperature();
  float humReal  = dht.readHumidity();
  int nivel = digitalRead(LUZ_PIN); 
  bool hayLuz = LUZ_ACTIVE_HIGH ? (nivel == HIGH) : (nivel == LOW);
  int brightReal = hayLuz ? 100 : 0;

  // 2. BUCLE PARA PUBLICAR LOS 3 SENSORES SEGUIDOS
  for (int i = 0; i < 3; i++) {
    
    // Pequeña variación aleatoria para que no sean idénticos
    float tempSim = isnan(tempReal) ? 0 : tempReal + random(-10, 10) / 10.0; // +/- 1.0 grado
    float humSim  = isnan(humReal)  ? 0 : humReal  + random(-20, 20) / 10.0; // +/- 2.0 %
    
    // Estado Lógico
    String estado;
    if (tempSim > 30.0) estado = "Calor";
    else if (tempSim < 15.0) estado = "Frio";
    else estado = "Confort";

    // Construir JSON
    String ts = iso8601now();
    String j  = "{";
    j += "\"sensor_id\":\""    + String(sensores[i].sensor_id) + "\",";
    j += "\"sensor_type\":\"information_display\","; 
    j += "\"street_id\":\""    + String(sensores[i].street_id) + "\",";
    j += "\"timestamp\":\""    + ts + "\",";

    j += "\"location\":{";
      j += "\"latitude\":"     + String(sensores[i].lat, 7) + ",";
      j += "\"longitude\":"    + String(sensores[i].lon, 7) + ",";
      j += "\"address\":\""    + String(sensores[i].address) + "\",";
      j += "\"district\":\""   + String(sensores[i].district) + "\",";
      j += "\"neighborhood\":\"" + String(sensores[i].neighborhood) + "\"";
    j += "},";

    j += "\"data\":{";
      j += "\"display_status\":\"active\","; 
      j += "\"temperature_celsius\":" + String(tempSim, 1) + ","; 
      j += "\"humidity_percent\":" + String(humSim, 1) + ",";
      j += "\"brightness_level\":" + String(brightReal) + ","; 
      j += "\"status_msg\":\"" + estado + "\""; 
    j += "}";
    j += "}";

    // Publicar al topic específico de cada sensor
    client.publish(sensores[i].topic_pub, j.c_str());
    
    Serial.print("TX Simulado ["); Serial.print(sensores[i].street_id); Serial.println("]: OK");
    delay(1500); // Pequeña pausa entre mensajes para no atragantar al Broker
  }
}