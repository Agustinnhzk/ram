# Backend de RAM para producción.
# Etapa 1: compila el .jar con el Maven del proyecto (mvnw). Los tests no corren acá porque necesitan Docker
# (Testcontainers); se corren en tu compu antes de pushear.
FROM eclipse-temurin:25-jdk AS compilacion
WORKDIR /fuente
COPY .mvn .mvn
COPY mvnw pom.xml ./
# Si el repo se clonó en Windows, mvnw puede venir con fin de línea \r\n y Linux no lo puede ejecutar.
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw
COPY src src
RUN ./mvnw -B -q -DskipTests package && cp target/*.jar /ram.jar

# Etapa 2: solo lo necesario para correr (sin Maven ni el código fuente).
FROM eclipse-temurin:25-jre
WORKDIR /app
RUN useradd --system --no-create-home ram
COPY --from=compilacion /ram.jar ram.jar
USER ram
# Java usa como máximo el 40% de la RAM del servidor; el resto queda para PostgreSQL y la tienda.
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=40"
EXPOSE 8080
# La configuración privada (dueño inicial, mail) llega montada en /app/config/application.yml.
ENTRYPOINT ["java", "-jar", "ram.jar"]
