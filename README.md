# lighten [![Build Status](https://travis-ci.com/a4501150/lighten.svg?branch=master)](https://travis-ci.com/a4501150/lighten)

lighten is a set of comprehensive brainless Java 8 libraries covers WebSocket, Kafka, Redis and more

You need JRE 1.8 to make it work

## IMPORTANT WARNINGS

1.  This library is designed for shorten development time, 
    so It enforces you to use some libraries like rxJava.

2.  The performance is the second class in this library, brainless and convenient for use is first class

## Websocket

This component can auto handle failure and reconnect to your websocket in the fly (including re-send subscription message)

case1 simple direct subscription

`

    private static void simpleDirectSubscription() throws InterruptedException {         
         Duration duration = Duration.ofMinutes(1);
         LightenWebSocketClient lightenWebSocketClient
                 = new LightenWebSocketClient("wss://stream.binance.com:9443/ws/btcusdt@depth10@100ms", duration);
         Disposable subscribtion =
                 lightenWebSocketClient.listenToTopic("", false).subscribe(System.out::println);
         Thread.sleep(1000 * 5);
         subscribtion.dispose();
     }
`

case2 simple direct subscription with subtopic

`

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
`

case3 subscription with subtopic and msg

`

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
`

## Kafka

## Redis

A wrapper for lettuce, but with auto byte conversion.

## Database
