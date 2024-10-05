package com.github.rybalkin_an.spring_mqtt.controller;

import com.github.rybalkin_an.spring_mqtt.service.SensorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sensor")
public class SensorController {

    @Autowired
    private SensorService sensorService;

    @PostMapping("/{toggle}")
    public ResponseEntity<String> startStreaming(@PathVariable String toggle) {
        if (toggle.equals("start")) {
            sensorService.startStreaming();
            return ResponseEntity.ok("Sensor streaming started.");
        } else if (toggle.equals("stop")) {
            sensorService.stopStreaming();
            return ResponseEntity.ok("Sensor streaming stopped.");
        } else {
            return ResponseEntity.badRequest().body("Invalid toggle parameter. Use 'start' or 'stop'.");
        }
    }
}
