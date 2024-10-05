package com.github.rybalkin_an.spring_mqtt.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Flow;

public class MqttMessageSubscriber implements Flow.Subscriber<String> {

    private static final Logger logger = LoggerFactory.getLogger(MqttMessageSubscriber.class);

    private Flow.Subscription subscription;

    private final java.util.function.Consumer<String> messageConsumer;

    public MqttMessageSubscriber(java.util.function.Consumer<String> messageConsumer) {
        this.messageConsumer = messageConsumer;
    }

    @Override
    public void onSubscribe(Flow.Subscription subscription) {
        this.subscription = subscription;
        subscription.request(1);
        logger.info("Subscribed to the message stream.");
    }

    @Override
    public void onNext(String item) {
        logger.info("Received message: {}", item);
        messageConsumer.accept(item);
        subscription.request(1);
    }

    @Override
    public void onError(Throwable throwable) {
        logger.error("Error occurred: {}", throwable.getMessage(), throwable);
    }

    @Override
    public void onComplete() {
        logger.info("Message stream complete.");
    }
}
