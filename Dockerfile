# Use an official JDK image as the base
FROM openjdk:17-jdk-slim

# Install required dependencies (Gradle, Android SDK)
RUN apt-get update && apt-get install -y \
    unzip \
    wget \
    git \
    && rm -rf /var/lib/apt/lists/*

# Set working directory inside the container
WORKDIR /app

# Copy the Android project files into the container
COPY . .

# Grant permission to execute Gradle
RUN chmod +x gradlew

# Build the Android project
RUN ./gradlew assembleDebug

# Define the default command (optional)
CMD ["./gradlew", "assembleDebug"]

