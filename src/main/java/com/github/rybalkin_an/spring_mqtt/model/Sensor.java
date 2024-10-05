package com.github.rybalkin_an.spring_mqtt.model;

import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class Sensor {

    private UUID uuid;
    private String timestamp;
    private BigDecimal value;

}
