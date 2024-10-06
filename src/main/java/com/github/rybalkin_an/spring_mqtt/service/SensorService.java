package com.github.rybalkin_an.spring_mqtt.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.rybalkin_an.spring_mqtt.config.MqttConfig;
import com.github.rybalkin_an.spring_mqtt.model.Sensor;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.util.UUID.randomUUID;

@Component
public class SensorService {

    private static final Logger logger = LoggerFactory.getLogger(SensorService.class);

    @Autowired
    private MqttPublisher mqttPublisher;

    @Autowired
    private MqttConfig mqttConfig;

    private final AtomicBoolean isStreaming = new AtomicBoolean(false);

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private Sensor create() {
        Sensor sensor = new Sensor();
        sensor.setUuid(randomUUID());
        return sensor;
    }

    /**
     * Generates a random temperature value
     * @return BigDecimal value randomly selected from the range -20.0 to 50.0.
     */
    private BigDecimal getRandomTemperature() {
        return BigDecimal.valueOf(-20 + (Math.random() * 70));
    }

    private Sensor setSensorTemp(Sensor sensor) {
        LocalDateTime timeStamp = LocalDateTime.now();
        sensor.setTimestamp(timeStamp.format(formatter));
        sensor.setValue(getRandomTemperature());
        return sensor;
    }

    private void streamSensorValues(AtomicBoolean isStreaming) {
        Sensor sensor = create();
        ObjectMapper objectMapper = new ObjectMapper();
        while (isStreaming.get()) {
            setSensorTemp(sensor);

            try {
                String sensorData = objectMapper.writeValueAsString(sensor);
                mqttPublisher.publish(sensorData, mqttConfig.getTopic(), mqttConfig.getQos());
                logger.info("Published sensor data: {}", sensorData);
            } catch (JsonProcessingException e) {
                logger.error("Failed to serialize sensor data: {}", e.getMessage(), e);
                break;
            } catch (MqttException e) {
                logger.error("Failed to publish sensor data: {}", e.getMessage(), e);
                break;
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.warn("Streaming interrupted: {}", e.getMessage());
                break;
            }
        }
    }

    public void startStreaming() {
        if (!isStreaming.get()) {
            isStreaming.set(true);
            Thread streamingThread = new Thread(() -> streamSensorValues(isStreaming));
            streamingThread.start();
            logger.info("Sensor streaming started.");
        } else {
            logger.warn("Sensor streaming is already running.");
        }
    }

    public void stopStreaming() {
        if (isStreaming.get()) {
            isStreaming.set(false);
            logger.info("Sensor streaming stopping...");
        } else {
            logger.warn("Sensor streaming is not running.");
        }
    }
}
