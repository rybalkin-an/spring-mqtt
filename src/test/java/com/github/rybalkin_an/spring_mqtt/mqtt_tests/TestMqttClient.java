package com.github.rybalkin_an.spring_mqtt.mqtt_tests;

import com.github.rybalkin_an.spring_mqtt.config.MqttConfig;
import com.github.rybalkin_an.spring_mqtt.model.Sensor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.concurrent.TimeoutException;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TestMqttClient {

    private static final Logger logger = LoggerFactory.getLogger(TestMqttClient.class);
    private static final String host= "http://localhost:";
    private static final String mqttMessage= "/mqtt/message";
    private static final String mqttSubscribe= "/mqtt/subscribe";
    private static final String mqttDisconnect= "/mqtt/disconnect";
    private static final String mqttReconnect= "/mqtt/reconnect";

    @LocalServerPort
    private Integer randomServerPort;

    @Autowired
    private MqttConfig mqttConfig;

    private WebClient webClient;

    @BeforeEach
    void startStream() {
        webClient = WebClient.create(host + randomServerPort);
    }

    @Test()
    @DisplayName("Message Publishing Validation")
    void whenPublishingMessage_thenShouldBePublishedSuccessfully() {
        String messageToSend = "Message Publishing Validation";

        WebClient.ResponseSpec response = webClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path(mqttMessage)
                        .queryParam("message", messageToSend)
                        .queryParam("topic", mqttConfig.getTopic())
                        .queryParam("qos", mqttConfig.getQos())
                        .build())
                .retrieve();

        StepVerifier.create(response.bodyToMono(String.class))
                .expectNext("Message published to topic '" + mqttConfig.getTopic() + "': " + messageToSend)
                .verifyComplete();
    }

    @Test
    @DisplayName("Simulate reconnect")
    void whenDisconnect_thenMessageShouldBeRetainedAndRedelivered() {
        String messageToSend = "some message";
        String topic = mqttConfig.getTopic();
        int qos = mqttConfig.getQos();

        webClient.get().uri(mqttSubscribe)
                .retrieve()
                .bodyToFlux(Sensor.class);

        webClient.post().uri(mqttDisconnect)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        webClient.post().uri(mqttReconnect)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        WebClient.ResponseSpec response = webClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path(mqttMessage)
                        .queryParam("message", messageToSend)
                        .queryParam("topic", topic)
                        .queryParam("qos", qos)
                        .build())
                .retrieve();

        StepVerifier.create(response.bodyToMono(String.class))
                .expectNext("Message published to topic '" + topic + "': " + messageToSend)
                .verifyComplete();
    }

    @Test
    @DisplayName("Simulate delayed messages that exceed the timeout")
    void whenSubscribingWithTimeout_thenShouldHandleGracefully() {
        Flux<Sensor> responseStream = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(mqttSubscribe)
                        .queryParam("topic", mqttConfig.getTopic())
                        .queryParam("qos", mqttConfig.getQos())
                        .build())
                .retrieve()
                .bodyToFlux(Sensor.class)
                .delayElements(Duration.ofSeconds(65))  // delay longer than the timeout
                .timeout(Duration.ofSeconds(60));

        StepVerifier.create(responseStream)
                .expectError(TimeoutException.class)
                .verify();
    }
}
