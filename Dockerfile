FROM ghcr.io/cirruslabs/android-sdk:33
WORKDIR /app
COPY . .
RUN echo "sdk.dir=/opt/android-sdk" > local.properties
RUN chmod +x gradlew
RUN ./gradlew assembleDebug
CMD ["./gradlew", "assembleDebug"]
