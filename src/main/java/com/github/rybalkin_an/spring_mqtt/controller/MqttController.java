package com.github.rybalkin_an.spring_mqtt.controller;

import com.github.rybalkin_an.spring_mqtt.config.MqttConfig;
import com.github.rybalkin_an.spring_mqtt.service.MqttMessageSubscriber;
import com.github.rybalkin_an.spring_mqtt.service.MqttSubscriber;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.time.Duration;

@RestController
@RequestMapping("/mqtt")
public class MqttController {

    @Autowired
    private MqttClient mqttClient;

    @Autowired
    private MqttSubscriber mqttSubscriber;

    @Autowired
    private MqttConfig mqttConfig;

    @PostMapping("/publish")
    public ResponseEntity<String> publishMessage(@RequestParam String message) {
        try {
            MqttMessage mqttMessage = new MqttMessage(message.getBytes());
            mqttMessage.setQos(mqttConfig.getQos());
            mqttClient.publish(mqttConfig.getTopic(), mqttMessage);
            return ResponseEntity.ok("Message published: " + message);

        } catch (MqttException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error publishing message: " + e.getMessage());
        }
    }

    @GetMapping(value = "/messages", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Object> streamMessages() {
        return Flux.create(sink -> {
            mqttSubscriber.subscribeToMessages(new MqttMessageSubscriber(sink::next));
        }).delayElements(Duration.ofMillis(100));
    }

}

