package com.github.rybalkin_an.spring_mqtt.controller;

import com.github.rybalkin_an.spring_mqtt.service.MqttMessageSubscriber;
import com.github.rybalkin_an.spring_mqtt.service.MqttPublisher;
import com.github.rybalkin_an.spring_mqtt.service.MqttSubscriber;
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


    @PostMapping("/message")
    public ResponseEntity<String> publishMessage(@RequestParam String message) {
        try {
            mqttPublisher.publish(message);
            return ResponseEntity.ok("Message published: " + message);
        } catch (Exception e) {
            logger.error("Error publishing message: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Error publishing message: " + e.getMessage());
        }
    }

    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Object> streamMessages() {
        return Flux.create(sink -> {
            mqttSubscriber.subscribeToMessages(new MqttMessageSubscriber(sink::next));
        }).delayElements(Duration.ofMillis(100));
    }
}

