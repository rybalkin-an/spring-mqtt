package com.github.rybalkin_an.spring_mqtt.controller;

import com.github.rybalkin_an.spring_mqtt.service.MqttMessageSubscriber;
import com.github.rybalkin_an.spring_mqtt.service.MqttPublisher;
import com.github.rybalkin_an.spring_mqtt.service.MqttSubscriber;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.time.Duration;

@RestController
@RequestMapping("/mqtt")
public class MqttController {

    private static final Logger logger = LoggerFactory.getLogger(MqttController.class);

    @Autowired
    private MqttSubscriber mqttSubscriber;

    @Autowired
    private MqttPublisher mqttPublisher;

    @Autowired
    private MqttClient mqttClient;

    @PostMapping("/message")
    public ResponseEntity<String> publishMessage(
            @RequestParam String message,
            @RequestParam String topic,
            @RequestParam(defaultValue = "1") int qos) {
        try {
            mqttPublisher.publish(message, topic, qos);
            return ResponseEntity.ok("Message published to topic '" + topic + "': " + message);
        } catch (Exception e) {
            logger.error("Error publishing message: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Error publishing message: " + e.getMessage());
        }
    }

    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Object> streamMessages(@RequestParam String topic, @RequestParam int qos) {
        try {
            mqttSubscriber.subscribe(topic, qos);
        } catch (Exception e) {
            logger.error("Failed to subscribe to topic {} with QoS {}: {}", topic, qos, e.getMessage());
            return Flux.error(new RuntimeException("Subscription failed: " + e.getMessage()));
        }
        return Flux.create(sink -> {
            try {
                mqttSubscriber.subscribeToMessages(new MqttMessageSubscriber(sink::next));
            } catch (Exception e) {
                logger.error("Error while subscribing to messages from topic {}: {}", topic, e.getMessage());
                sink.error(new RuntimeException("Error subscribing to messages: " + e.getMessage()));
            }
        }).delayElements(Duration.ofMillis(100));
    }

    @PostMapping("/disconnect")
    public ResponseEntity<String> disconnect() {
        try {
            mqttClient.disconnect();
            logger.info("MQTT client disconnected successfully.");
            return ResponseEntity.ok("MQTT client disconnected.");
        } catch (MqttException e) {
            logger.error("Error while disconnecting MQTT client: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("Failed to disconnect MQTT client.");
        }
    }

    @PostMapping("/reconnect")
    public ResponseEntity<String> reconnect() {
        try {
            if (mqttClient == null) {
                logger.error("MQTT client is not initialized.");
                return ResponseEntity.status(500).body("MQTT client is not initialized.");
            }

            if (!mqttClient.isConnected()) {
                mqttClient.connect();
            }

            logger.info("MQTT client connected: {}", mqttClient.isConnected());

            if (mqttClient.isConnected()) {
                logger.info("MQTT client reconnected successfully.");
                return ResponseEntity.ok("MQTT client reconnected.");
            } else {
                logger.error("MQTT client failed to reconnect.");
                return ResponseEntity.status(500).body("Failed to reconnect MQTT client.");
            }

        } catch (MqttException e) {
            logger.error("Failed to reconnect to MQTT broker: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Error while reconnecting MQTT client: " + e.getMessage());
        }
    }

}

