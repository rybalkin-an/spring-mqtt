package com.github.rybalkin_an.spring_mqtt.mqtt;

import com.github.rybalkin_an.spring_mqtt.model.Sensor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TestSubscribeToMessages {

    private static final Logger logger = LoggerFactory.getLogger(TestSubscribeToMessages.class);

    @LocalServerPort
    private Integer randomServerPort;

    private WebClient webClient;

    @BeforeEach
    void startStream() {
        webClient = WebClient.create("http://localhost:" + randomServerPort);
        assertEquals("Sensor streaming started.", webClient.post()
                .uri("/api/sensor/start")
                .retrieve()
                .bodyToMono(String.class)
                .block()
        );
    }

    @AfterEach
    void stopStream() {
        assertEquals("Sensor streaming stopped.", webClient.post()
                .uri("/api/sensor/stop")
                .retrieve()
                .bodyToMono(String.class)
                .block()
        );
    }

    @Test
    void whenRequestingMqttMessages_thenShouldReceiveValidSensorData() {
        Flux<Sensor> responseStream = webClient.get()
                .uri("/mqtt/subscribe")
                .retrieve()
                .bodyToFlux(Sensor.class);

        StepVerifier.create(responseStream)
                .expectNextMatches(expectedResultFromSensor -> {
                    try {

                        String uuidPattern = "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$";
                        assertThat(expectedResultFromSensor.getUuid().toString(), matchesPattern(uuidPattern));

                        String timestampPattern = "^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}$";
                        assertThat(expectedResultFromSensor.getTimestamp(), matchesPattern(timestampPattern));

                        assertThat(expectedResultFromSensor.getValue(), allOf(
                                greaterThanOrEqualTo(new BigDecimal("-20.0")),
                                lessThanOrEqualTo(new BigDecimal("50.0"))
                        ));
                        return true;
                    } catch (Exception e) {
                        logger.error("Error occurred: {}", e.getMessage(), e);
                        return false;
                    }
                })
                .expectNextCount(50)
                .thenCancel()
                .verify();
    }
}
