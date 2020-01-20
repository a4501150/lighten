package com.frostflames.lighten.kafka;

import io.reactivex.Flowable;
import io.reactivex.Maybe;
import org.junit.jupiter.api.Assertions;
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

        lightenKafkaClientUnderTest.publishMessage("topic.1", "payload");

        // Verify the results
        Maybe<String> returned = result.firstElement();

        Assertions.assertEquals("payload", returned.blockingGet());
    }

    @Test
    void testPublishMessage() {
        // Setup
        // Run the test
        final Flowable<String> result = lightenKafkaClientUnderTest.listenToTopic("topic.1");

        lightenKafkaClientUnderTest.publishMessage("topic.1", "payload");

        // Verify the results
        Maybe<String> returned = result.firstElement();

        Assertions.assertEquals("payload", returned.blockingGet());
    }
}
