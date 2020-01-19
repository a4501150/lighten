package com.frostflames.lighten.kafka;

import io.reactivex.Flowable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LightenKafkaClientTest {

    private LightenKafkaClient lightenKafkaClientUnderTest;

    @BeforeEach
    void setUp() {
        lightenKafkaClientUnderTest = new LightenKafkaClient("bootstrapServers");
    }

    @Test
    void testInitConsumer() {
        // Setup

        // Run the test
        lightenKafkaClientUnderTest.initConsumer("groupID", "offsetResetType", false, false);

        // Verify the results
    }

    @Test
    void testListenToTopic() {
        // Setup

        // Run the test
        final Flowable<String> result = lightenKafkaClientUnderTest.listenToTopic("topic");

        // Verify the results
    }

    @Test
    void testInitProducer() {
        // Setup

        // Run the test
        lightenKafkaClientUnderTest.initProducer("clientID");

        // Verify the results
    }

    @Test
    void testPublishMessage() {
        // Setup

        // Run the test
        lightenKafkaClientUnderTest.publishMessage("topic", "payload");

        // Verify the results
    }
}
