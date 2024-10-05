package com.github.rybalkin_an.spring_mqtt.service;

import com.github.rybalkin_an.spring_mqtt.config.MqttConfig;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MqttPublisher {

    private static final Logger logger = LoggerFactory.getLogger(MqttPublisher.class);

    @Autowired
    private MqttClient mqttClient;

    @Autowired
    private MqttConfig mqttConfig;

    public void publish(String messageContent) throws MqttException {
        try {
            publishMessage(messageContent);
            logger.info("Message published: {}", messageContent);
        } catch (MqttException e) {
            logger.error("Failed to publish message: {}", messageContent, e);
            throw e;
        }
    }

    private void publishMessage(String messageContent) throws MqttException {
        MqttMessage message = new MqttMessage(messageContent.getBytes());
        message.setQos(mqttConfig.getQos());
        mqttClient.publish(mqttConfig.getTopic(), message);
    }
}
