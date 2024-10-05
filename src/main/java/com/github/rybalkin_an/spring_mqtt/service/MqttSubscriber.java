package com.github.rybalkin_an.spring_mqtt.service;

import com.github.rybalkin_an.spring_mqtt.config.MqttConfig;
import jakarta.annotation.PostConstruct;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;

@Component
public class MqttSubscriber {

    private static final Logger logger = LoggerFactory.getLogger(MqttSubscriber.class);

    @Autowired
    private MqttClient mqttClient;

    @Autowired
    private MqttConfig mqttConfig;

    private final SubmissionPublisher<String> publisher = new SubmissionPublisher<>();

    @PostConstruct
    public void subscribe() throws Exception {
        try {
            mqttClient.subscribe(mqttConfig.getTopic());
            logger.info("Subscribed to topic: {}", mqttConfig.getTopic());

            mqttClient.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    logger.warn("Connection lost: {}", cause.getMessage());
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) {
                    String receivedMessage = new String(message.getPayload());
                    logger.info("Message received from topic {}: {}", topic, receivedMessage);
                    publisher.submit(receivedMessage);
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                }
            });

        } catch (Exception e) {
            logger.error("Error during MQTT subscription: {}", e.getMessage(), e);
        }
    }

    public void subscribeToMessages(Flow.Subscriber<String> subscriber) {
        publisher.subscribe(subscriber);
    }
}
