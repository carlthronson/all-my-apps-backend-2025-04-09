# Build stage
FROM maven:3.8.2-openjdk-17 as build

# Set the working directory in the container
WORKDIR /app

# Copy the pom.xml file
COPY pom.xml .

# Download project dependencies
RUN mvn dependency:go-offline -B

# Copy your source code
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jdk

# Set the working directory in the container
WORKDIR /app

# --- DJL native dependencies for PyTorch/HuggingFace ---
# RUN apt-get update && \
#     apt-get install -y --no-install-recommends \
#         libgomp1 \
#         libopenblas-base \
#         libstdc++6 \
#     && rm -rf /var/lib/apt/lists/*

# Copy the jar file from the build stage
COPY --from=build /app/target/*.jar app.jar

# Command to run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
