# Spring Boot MQTT Sensor Streaming Application

This application demonstrates a simple implementation of MQTT sensor streaming using Spring Boot. It allows you to start and stop the streaming of sensor data and publish messages to an MQTT broker.

The `mqtt/subscribe` endpoint can be used to retrieve data from Topic

## Features

- `sensor/start` and `sensor/stop` streaming of sensor data.
- `mqtt/message` to publish messages to an MQTT topic. https://test.mosquitto.org/
- `mqtt/subscribe` to get stream messages from the MQTT broker.
- Tests are using https://github.com/reactor/reactor-core library

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
# MQTT Controller Endpoints

# 1. Publish Message to MQTT Topic
# Endpoint: POST /mqtt/message
# Description: Publishes a message to a specified MQTT topic.
# Parameters:
#   - message (required): The message to be published.
#   - topic (required): The MQTT topic to publish to.
#   - qos (optional, default: 1): Quality of Service level.
# Response: Success or error message.
curl -X POST "http://localhost:8080/mqtt/message?message=Hello&topic=test/topic&qos=1"

# 2. Stream Messages from MQTT Topic
# Endpoint: GET /mqtt/subscribe
# Description: Subscribes to a specified MQTT topic and streams incoming messages.
# Parameters:
#   - topic (required): The MQTT topic to subscribe to.
#   - qos (required): Quality of Service level.
# Response: Server-Sent Events (SSE) stream with MQTT messages.
curl "http://localhost:8080/mqtt/subscribe?topic=test/topic&qos=1"

# 3. Disconnect MQTT Client
# Endpoint: POST /mqtt/disconnect
# Description: Disconnects the MQTT client from the broker.
# Response: Success or error message.
curl -X POST "http://localhost:8080/mqtt/disconnect"

# 4. Reconnect MQTT Client
# Endpoint: POST /mqtt/reconnect
# Description: Reconnects the MQTT client to the broker if disconnected.
# Response: Success or error message.
curl -X POST "http://localhost:8080/mqtt/reconnect"


# Sensor Controller Endpoints

# 1. Start/Stop Sensor Data Streaming
# Endpoint: POST /sensor/{toggle}
# Description: Starts or stops the sensor data streaming process.
# Path Variables:
#   - toggle (required): Use 'start' to begin streaming or 'stop' to halt streaming.
# Response: Success or error message.
curl -X POST "http://localhost:8080/sensor/start"
curl -X POST "http://localhost:8080/sensor/stop"
   ```

### Running Tests
   ```bash 
   ./gradlew test 
   ```
[TestSubscribeToSensor.java](src%2Ftest%2Fjava%2Fcom%2Fgithub%2Frybalkin_an%2Fspring_mqtt%2Fmqtt_tests%2FTestSubscribeToSensor.java) is ensures the MQTT subscription and data integrity are functioning correctly:
- Matches UUID and timestamp patterns.
- Falls within the temperature range of -20.0 to 50.0.
- Uses StepVerifier to expect 50 valid sensor messages.
- Check the timestamp in the right order.

[TestMqttClient.java](src%2Ftest%2Fjava%2Fcom%2Fgithub%2Frybalkin_an%2Fspring_mqtt%2Fmqtt_tests%2FTestMqttClient.java) is ensures the MQTT client:
- Message Publishing Validation.
- Disconnect/reconnect.
- Validate delayed messages that exceed the timeout.