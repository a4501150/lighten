package com.frostflames.lighten.websocket;

import io.reactivex.disposables.Disposable;
import org.junit.jupiter.api.Test;

import java.time.Duration;

public class WebSocketManual {

    @Test
    // case 1 simple direct subscription
    private static void simpleDirectSubscription() throws InterruptedException {
        Duration duration = Duration.ofMinutes(1);
        LightenWebSocketClient lightenWebSocketClient
                = new LightenWebSocketClient("wss://stream.binance.com:9443/ws/btcusdt@depth10@100ms", duration);

        Disposable subscribtion =
                lightenWebSocketClient.listenToTopic("", false).subscribe(System.out::println);
        Thread.sleep(1000 * 5);
        subscribtion.dispose();
    }

    @Test
    // case 2 simple direct subscription with subtopic
    private static void simpleSubTopicSubscription() throws InterruptedException {
        Duration duration = Duration.ofMinutes(1);
        LightenWebSocketClient lightenWebSocketClient
                = new LightenWebSocketClient("wss://stream.binance.com:9443", duration);

        // case 1 simple direct subscription
        Disposable subscribtion =
                lightenWebSocketClient.listenToTopic("/ws/btcusdt@depth10@100ms", false).subscribe(System.out::println);
        Thread.sleep(1000 * 5);
        subscribtion.dispose();
    }

    @Test
    // case 3 subscription with subtopic and msg
    private static void subscriptionWithSubscriptionMsg() throws InterruptedException {
        Duration duration = Duration.ofMinutes(1);
        LightenWebSocketClient lightenWebSocketClient
                = new LightenWebSocketClient("wss://stream.binance.com:9443", duration);

        Disposable subscribtion =
                lightenWebSocketClient.listenToTopic("/ws/btcusdt@depth10@100ms", false).subscribe(System.out::println);

        lightenWebSocketClient.sendTextMsg("/ws/btcusdt@depth10@100ms", "{\n" +
                "  \"method\": \"SUBSCRIBE\"," +
                "  \"params\": [" +
                "    \"btcusdt@aggTrade\"" +
                "  ]," +
                "  \"id\": 1" +
                "}", true);

        Thread.sleep(1000 * 5);
        subscribtion.dispose();

    }

}
