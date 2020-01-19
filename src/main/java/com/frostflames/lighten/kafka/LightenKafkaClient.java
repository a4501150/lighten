package com.frostflames.lighten.kafka;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.producer.Producer;

public class LightenKafkaClient {

    private Consumer<String, String> consumer;
    private Producer<String, String> producer;
    private String bootstrapServers;

    public LightenKafkaClient(String bootstrapServers) {

    }
}
