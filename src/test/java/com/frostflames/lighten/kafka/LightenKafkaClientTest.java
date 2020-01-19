package com.frostflames.lighten.kafka;

import io.reactivex.Flowable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LightenKafkaClientTest {

    private LightenKafkaClient lightenKafkaClientUnderTest;

    @BeforeEach
    void setUp() {
        lightenKafkaClientUnderTest = new LightenKafkaClient("127.0.0.1:9092");
        lightenKafkaClientUnderTest.initProducer("clientID");
        lightenKafkaClientUnderTest.initConsumer("groupID", "latest", false, false);
    }

    @Test
    void testListenToTopic() {
        // Setup

        // Run the test
        final Flowable<String> result = lightenKafkaClientUnderTest.listenToTopic("topic.1");

        // Verify the results
        result.subscribe().dispose();
    }

    @Test
    void testPublishMessage() {
        // Setup

        // Run the test
        lightenKafkaClientUnderTest.publishMessage("topic", "payload");

        // Verify the results
    }
}
