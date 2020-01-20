package com.frostflames.lighten.kafka;

import com.frostflames.lighten.thread.CommonUtil;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

class LightenKafkaClientTest {

    private LightenKafkaClient lightenKafkaClientUnderTest;

    @BeforeEach
    void setUp() {
        lightenKafkaClientUnderTest = new LightenKafkaClient("127.0.0.1:9092");
        lightenKafkaClientUnderTest.initProducer("clientID");
        lightenKafkaClientUnderTest.initConsumer("groupID", "earliest", false, false);
    }

    @Test
    void testListenToTopic() {
        // Setup

        // Run the test
        final Flowable<String> result = lightenKafkaClientUnderTest.listenToTopic("topic.1");
        lightenKafkaClientUnderTest.publishMessage("topic.1", "payload");
        AtomicReference<String> returned = new AtomicReference<>();
        result.subscribe(s -> {
           returned.set(s);
        });
        while (returned.get() == null) {
            CommonUtil.sleep(1, TimeUnit.SECONDS);
        }
        Assertions.assertEquals("payload", returned.get());
    }

}
