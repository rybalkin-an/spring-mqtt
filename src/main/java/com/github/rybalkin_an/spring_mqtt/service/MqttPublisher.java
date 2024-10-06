package com.github.rybalkin_an.spring_mqtt.service;

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

    public void publish(String messageContent, String topic, int qos) throws MqttException {
        publishMessage(messageContent, topic, qos);
        logger.info("Message published to topic '{}': {}", topic, messageContent);
    }

    private void publishMessage(String messageContent, String topic, int qos) throws MqttException {
        MqttMessage message = new MqttMessage(messageContent.getBytes());
        message.setQos(qos);
        mqttClient.publish(topic, message);
    }
}
