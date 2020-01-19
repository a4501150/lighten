# lighten [![Build Status](https://travis-ci.com/a4501150/lighten.svg?branch=master)](https://travis-ci.com/a4501150/lighten) [![Coverage Status](https://coveralls.io/repos/github/a4501150/lighten/badge.svg?branch=master)](https://coveralls.io/github/a4501150/lighten?branch=master)

lighten is a set of comprehensive brainless Java 8 libraries covers WebSocket, Kafka, Redis and more

You need JRE 1.8 to make it work

## IMPORTANT WARNINGS

1.  This library is designed for shorten development time, 
    so It enforces you to use some libraries like rxJava.

2.  The performance is the second class in this library, brainless and convenient for use is first class

## Websocket

This component can auto handle failure and reconnect to your websocket in the fly (including re-send subscription message)

case1 simple direct subscription

```java


    private static void simpleDirectSubscription() throws InterruptedException {         
         Duration duration = Duration.ofMinutes(1);
         LightenWebSocketClient lightenWebSocketClient
                 = new LightenWebSocketClient("wss://stream.binance.com:9443/ws/btcusdt@depth10@100ms", duration);
         Disposable subscribtion =
                 lightenWebSocketClient.listenToTopic("", false).subscribe(System.out::println);
         Thread.sleep(1000 * 5);
         subscribtion.dispose();
     }
```

case2 simple direct subscription with subtopic

```java


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
```

case3 subscription with subtopic and msg

```java

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
```

## Kafka

A wrapper on top of official producer / consumer API.

```java
LightenKafkaClient client = new LightenKafkaClient("127.0.0.1");

// consumer
client.initConsumer("groupID", "latest", false, false);
Flowable<String> result = lightenKafkaClientUnderTest.listenToTopic("topic");

result.dispose(); // free resource and cutoff stream

// producer
client.initProducer("clientID");
client.publishMessage("topic", "payload");


```


## Redis

A wrapper for lettuce, but with auto byte conversion.

So you can use it just like a hashmap.

```java


        final ConcurrentHashMap<String, ConcurrentHashMap<String, String>> map = new ConcurrentHashMap<>();
        map.put("test", new ConcurrentHashMap<>());
        map.get("test").put("inner", "innerValue");

        LightenRedisClient<String, ConcurrentHashMap<String, ConcurrentHashMap<String, String>>> lightenRedisClientUnderTest2 = new LightenRedisClient<>("127.0.0.1", "");

        lightenRedisClientUnderTest2.put("mapTesting", map);

        ConcurrentHashMap<String, ConcurrentHashMap<String, String>> obtainedMap = lightenRedisClientUnderTest2.get("mapTesting");

        Assertions.assertEquals(map.toString(), obtainedMap.toString());
```


## Database
