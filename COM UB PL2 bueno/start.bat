@echo off
echo --- Levantando Infraestructura IoT (Tomcat + MariaDB + MQTT) ---

:: 1. Levantar Docker en segundo plano 
docker compose up -d --build

:: 2. Abrir NetBeans con tu proyecto
start "" "C:\Program Files\NetBeans-24\netbeans\bin\netbeans64.exe" --open "C:\Users\SERGIO\OneDrive - Universidad de Alcala\Escritorio\COM UB PL2\ServerUbicua"

:: 3. Abrir MQTT Explorer 
start "" "C:\Users\SERGIO\Downloads\MQTT-Explorer-0.3.5.exe"

:: 4. Esperar a que Tomcat arranque totalmente (15 segundos)
echo Esperando a que Tomcat despliegue el .war...
timeout /t 15

:: 5. ABRIR ENDPOINTS DE CONSULTA (GET)
echo Abriendo endpoints de consulta en el navegador...

start http://localhost:8080/ServerExampleUbicomp/GetData
start http://localhost:8080/ServerExampleUbicomp/GetLast
start http://localhost:8080/ServerExampleUbicomp/GetStats
start http://localhost:8080/ServerExampleUbicomp/GetBySensor?id=ST_0155
start http://localhost:8080/ServerExampleUbicomp/GetByDate?date=2025-12-05

:: 6. ABRIR ACTUADOR (Prueba de Bidireccionalidad)
start http://localhost:8080/ServerExampleUbicomp/SendMessage?msg=Test_Script_Inicio

:: 7. HERRAMIENTAS DE INSERCIÓN (POST)
echo Abriendo Postman para pruebas de SetData...
start https://www.postman.com/

echo --- ¡Sistema Listo y Navegador Abierto! ---
pause