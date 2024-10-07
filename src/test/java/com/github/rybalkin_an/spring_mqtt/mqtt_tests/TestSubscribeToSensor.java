package com.github.rybalkin_an.spring_mqtt.mqtt_tests;

import com.github.rybalkin_an.spring_mqtt.config.MqttConfig;
import com.github.rybalkin_an.spring_mqtt.model.Sensor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TestSubscribeToSensor {

    private static final String host = "http://localhost:";
    private static final String mqttSubscribe = "/mqtt/subscribe";

    @LocalServerPort
    private Integer randomServerPort;

    @Autowired
    private MqttConfig mqttConfig;

    private WebClient webClient;

    @BeforeEach
    void startStream() {
        webClient = WebClient.create(host + randomServerPort);
        assertEquals("Sensor streaming started.", webClient.post()
                .uri("/sensor/start")
                .retrieve()
                .bodyToMono(String.class)
                .block()
        );
    }

    @AfterEach
    void stopStream() {
        assertEquals("Sensor streaming stopped.", webClient.post()
                .uri("/sensor/stop")
                .retrieve()
                .bodyToMono(String.class)
                .block()
        );
    }

    @Test
    @DisplayName("Subscribing to messages and receiving sensor data")
    void whenRequestingMqttMessages_thenShouldReceiveValidSensorData() {
        Flux<Sensor> responseStream = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(mqttSubscribe)
                        .queryParam("topic", mqttConfig.getTopic())
                        .queryParam("qos", mqttConfig.getQos())
                        .build())
                .retrieve()
                .bodyToFlux(Sensor.class);

        StepVerifier.create(responseStream)
                .expectNextMatches(expectedResultFromSensor -> {

                    String uuidPattern = "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$";
                    assertThat(expectedResultFromSensor.getUuid().toString(), matchesPattern(uuidPattern));

                    String timestampPattern = "^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}$";
                    assertThat(expectedResultFromSensor.getTimestamp(), matchesPattern(timestampPattern));

                    assertThat(expectedResultFromSensor.getValue(), greaterThanOrEqualTo(new BigDecimal("-20.0")));

                    assertThat(expectedResultFromSensor.getValue(), lessThanOrEqualTo(new BigDecimal("50.0")));
                    return true;
                })
                .expectNextCount(50)
                .thenCancel()
                .verify();
    }

    @Test
    @DisplayName("Timestamps should be in order")
    void whenReceivingSensorData_thenTimestampsShouldBeInOrder() {
        Flux<Sensor> responseStream = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(mqttSubscribe)
                        .queryParam("topic", mqttConfig.getTopic())
                        .queryParam("qos", mqttConfig.getQos())
                        .build())
                .retrieve()
                .bodyToFlux(Sensor.class);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        final LocalDateTime[] previousTimestamp = {null};

        StepVerifier.create(responseStream)
                .expectNextMatches(sensor -> {
                    LocalDateTime currentTimestamp = LocalDateTime.parse(sensor.getTimestamp(), formatter);

                    if (previousTimestamp[0] != null) {
                        assertThat(currentTimestamp, greaterThan(previousTimestamp[0]));
                    }
                    previousTimestamp[0] = currentTimestamp;

                    return true;
                })
                .expectNextCount(9)
                .thenCancel()
                .verify();
    }

}
