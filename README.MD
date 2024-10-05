# Spring Boot MQTT Sensor Streaming Application

This application demonstrates a simple implementation of MQTT sensor streaming using Spring Boot. It allows you to start and stop the streaming of sensor data and publish messages to an MQTT broker.

## Features

- Start and stop streaming of sensor data.
- Publish messages to an MQTT topic. https://test.mosquitto.org/
- Stream messages from the MQTT broker.
- Test using **io.projectreactor:reactor-test** library

## Technologies Used

- Java 17
- Spring Boot
- MQTT (Eclipse Paho)
- Gradle

## Getting Started

### Prerequisites

- JDK 17 or higher
- Gradle 7.0 or higher
- An MQTT broker (e.g., https://test.mosquitto.org/)

### Installation

1. **Clone the repository:**

   ```bash
   git clone https://github.com/rybalkin-an/spring-mqtt.git
   
2. Navigate to the project directory.

3. Build the project:
   ```bash
   ./gradlew build
   ```
   
4. Run the application:
   ```bash
   ./gradlew bootRun
   ```

### Configuration
You can configure the MQTT broker URL and other parameters in the application.properties file.
   ```properties
mqtt.broker.url=tcp://test.mosquitto.org:1883
mqtt.topic=test5555868/topic
mqtt.client.id=mqttSpringClient
mqtt.qos=2
   ```
    
### API Endpoints

   ```bash
   # Start streaming
   curl -X POST http://localhost:8080/sensor/start
  
   # Stop streaming
   curl -X POST http://localhost:8080/sensor/stop
  
   # Publish a Message
   curl -X POST http://localhost:8080/mqtt/message?message=12345

   #Subscribe to streamed messages from the MQTT broker:
   curl http://localhost:8080/api/sensor/subscribe
   ```
### Running Tests
   ```bash 
   ./gradlew test 
   ```
TestSubscribeToMessages is ensures the MQTT subscription and data integrity are functioning correctly:

- Uses @SpringBootTest to run the application on a random port.
- Starts and stops sensor streaming before and after each test.
- Sends a GET request to subscribe to MQTT messages and checks that received sensor data:
  - Matches UUID and timestamp patterns.
  - Falls within the temperature range of -20.0 to 50.0.
  - Uses StepVerifier to expect 50 valid sensor messages.