# ==========================================
# ETAPA 1: Construcción (Build) con Maven
# ==========================================
# Usamos una imagen que tiene Maven y Java 17 instalados
FROM maven:3.9.6-eclipse-temurin-17 AS builder

# Establecemos el directorio de trabajo dentro del contenedor temporal
WORKDIR /app

# Copiamos el archivo de configuración de dependencias
COPY pom.xml .

# (Opcional) Descargar dependencias para aprovechar la caché de Docker
# RUN mvn dependency:go-offline

# Copiamos todo el código fuente de tu proyecto
COPY src ./src

# Compilamos el proyecto y generamos el .war
# (Esto hace lo mismo que cuando tú escribías 'mvn clean package')
RUN mvn clean package -DskipTests

# ==========================================
# ETAPA 2: Ejecución con Tomcat
# ==========================================
# Usamos la imagen oficial de Tomcat 17 (la misma que usábamos antes)
FROM tomcat:11.0-jdk17

# Borramos las aplicaciones por defecto de Tomcat para que esté limpio
RUN rm -rf /usr/local/tomcat/webapps/*

# COPIAMOS el .war que generamos en la Etapa 1 a la carpeta de Tomcat
# Fíjate que lo cogemos "from=builder"
COPY --from=builder /app/target/ServerExampleUbicomp.war /usr/local/tomcat/webapps/ServerExampleUbicomp.war

# Exponemos el puerto 8080
EXPOSE 8080

# Arrancamos Tomcat
CMD ["catalina.sh", "run"]