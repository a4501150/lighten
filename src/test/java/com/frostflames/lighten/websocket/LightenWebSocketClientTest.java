package com.frostflames.lighten.websocket;

import io.reactivex.Flowable;
import io.reactivex.Single;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;

class LightenWebSocketClientTest {

    private LightenWebSocketClient lightenWebSocketClientUnderTest;

    @Test
    void testListenToTopic() {
        // Setup
        lightenWebSocketClientUnderTest = new LightenWebSocketClient("wss://stream.binance.com:9443/ws/btcusdt@depth10@100ms", Duration.ofSeconds(1L));

        // Run the test
        final Flowable<String> result = lightenWebSocketClientUnderTest.listenToTopic("", false);

        // Verify the results
        Assertions.assertFalse(StringUtils.isAllEmpty(result.blockingFirst()));
    }

    @Test
    void testSendTextMsg() {
        // Setup
        lightenWebSocketClientUnderTest = new LightenWebSocketClient("wss://echo.websocket.org", Duration.ofSeconds(1L));

        Single<String> back = lightenWebSocketClientUnderTest.listenToTopic("", false).first("");

        // Run the test
        lightenWebSocketClientUnderTest.sendTextMsg("", "payload", true);

        // Verify the results
        Assertions.assertEquals("payload", back.blockingGet());
    }

    @Test
    void testSendBinaryMsg() {
        // Setup
        lightenWebSocketClientUnderTest = new LightenWebSocketClient("wss://echo.websocket.org", Duration.ofSeconds(1L));

        Single<String> back = lightenWebSocketClientUnderTest.listenToTopic("", true).first("");

        // Run the test
        lightenWebSocketClientUnderTest.sendBinaryMsg("", "payload".getBytes(), true);

        // Verify the results
        Assertions.assertEquals("payload", back.blockingGet());
    }
}
