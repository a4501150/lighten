package com.frostflames.lighten.redis;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.util.concurrent.ConcurrentHashMap;

class LightenRedisClientTest {


    @Test
    void testPut() {

        LightenRedisClient<String, String> lightenRedisClientUnderTest = new LightenRedisClient<>("127.0.0.1", "");

        // Setup
        final String key = "key";
        final String value = "value";

        // Run the test
        lightenRedisClientUnderTest.put(key, value);

        // Verify the results
        Assertions.assertEquals(value, lightenRedisClientUnderTest.get(key));

        final ConcurrentHashMap<String, ConcurrentHashMap<String, String>> map = new ConcurrentHashMap<>();
        map.put("test", new ConcurrentHashMap<>());
        map.get("test").put("inner", "innerValue");

        LightenRedisClient<String, ConcurrentHashMap<String, ConcurrentHashMap<String, String>>> lightenRedisClientUnderTest2 = new LightenRedisClient<>("127.0.0.1", "");

        lightenRedisClientUnderTest2.put("mapTesting", map);

        ConcurrentHashMap<String, ConcurrentHashMap<String, String>> obtainedMap = lightenRedisClientUnderTest2.get("mapTesting");

        Assertions.assertEquals(map.toString(), obtainedMap.toString());
    }

    @Test
    void testGet() {
        LightenRedisClient<String, String> lightenRedisClientUnderTest = new LightenRedisClient<>("127.0.0.1", "");

        // Setup
        final String key = "key";
        final String value = "value";

        // Run the test
        lightenRedisClientUnderTest.put(key, value);

        // Verify the results
        Assertions.assertEquals(value, lightenRedisClientUnderTest.get(key));
    }
}
