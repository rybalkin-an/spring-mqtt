package com.github.rybalkin_an.spring_mqtt.sse;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.rybalkin_an.spring_mqtt.model.Sensor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class TestSseFlux {

    private static final Logger logger = LoggerFactory.getLogger(TestSseFlux.class);

    private final WebClient webClient = WebClient.create("http://localhost:8080");
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void startStream() {
        String response = webClient.post()
                .uri("/api/sensor/start")
                .retrieve()
                .bodyToMono(String.class)
                .block();

        assertEquals("Sensor streaming started.", response);
    }

    @AfterEach
    void stopStream() {
        String response = webClient.post()
                .uri("/api/sensor/stop")
                .retrieve()
                .bodyToMono(String.class)
                .block();

        assertEquals("Sensor streaming stopped.", response);
    }

    @Test
    public void testStreamingApi() {
        Flux<String> responseStream = webClient.get()
                .uri("/mqtt/messages")
                .retrieve()
                .bodyToFlux(String.class);

        StepVerifier.create(responseStream)
                .expectNextMatches(json -> {
                    try {
                        Sensor actualResult = objectMapper.readValue(json, Sensor.class);

                        String uuidPattern = "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$";
                        assertThat(actualResult.getUuid().toString(), matchesPattern(uuidPattern));

                        String timestampPattern = "^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}$";
                        assertThat(actualResult.getTimestamp(), matchesPattern(timestampPattern));

                        assertThat(actualResult.getValue(), allOf(
                                greaterThanOrEqualTo(new BigDecimal("-20.0")),
                                lessThanOrEqualTo(new BigDecimal("50.0"))
                        ));
                        return true;
                    } catch (Exception e) {
                        logger.error("Error occurred: {}", e.getMessage());
                        return false;
                    }
                })
                .expectNextCount(50)
                .thenCancel()
                .verify();
    }
}
