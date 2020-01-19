package com.frostflames.lighten.redis;

import com.frostflames.lighten.serialization.ByteSerializationWrapper;
import com.frostflames.lighten.thread.CommonUtil;
import io.lettuce.core.RedisChannelHandler;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisConnectionException;
import io.lettuce.core.RedisConnectionStateListener;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;
import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import io.lettuce.core.pubsub.api.reactive.RedisPubSubReactiveCommands;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class  LightenRedisClient <K, V> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final RedisClient redisClient;

    private StatefulRedisConnection<K, V> statefulRedisConnection;
    private StatefulRedisPubSubConnection<K, V> reactiveRedisConnection;

    private RedisCommands<K, V> redisCommands;
    private RedisAsyncCommands<K, V> redisAsyncCommands;
    private RedisPubSubReactiveCommands<K, V> redisPubSubReactiveCommands;


    public LightenRedisClient(String host, String password) {

        if (StringUtils.isAllEmpty(password)) {
            redisClient = RedisClient.create("redis://" + host + ":6379/0");
        } else {
            redisClient = RedisClient.create("redis://"+ password +"@"+ host + ":6379/0");
        }

        redisClient.addListener(new RedisConnectionStateListener() {
            @Override
            public void onRedisDisconnected(RedisChannelHandler<?, ?> redisChannelHandler) {
                logger.debug("onRedisDisconnected");
            }

            @Override
            public void onRedisExceptionCaught(RedisChannelHandler<?, ?> redisChannelHandler, Throwable throwable) {
                logger.error("", throwable);
            }
        });

        setupConnection();
    }

    private void setupConnection() {

        try {
            // key value connection
            statefulRedisConnection = redisClient.connect(new RedisByteCodec<>());
            redisCommands = statefulRedisConnection.sync();
            redisAsyncCommands = statefulRedisConnection.async();

            // pub sub
            reactiveRedisConnection = redisClient.connectPubSub(new RedisByteCodec<>());
            redisPubSubReactiveCommands = reactiveRedisConnection.reactive();

        } catch (RedisConnectionException ex) {
            logger.debug("RedisConnectionException, reconnect");
            CommonUtil.sleep(3, TimeUnit.SECONDS);
            setupConnection();
        }

    }

    @SafeVarargs
    public final Mono<Void> listenToChannels(K... channels) {
        return redisPubSubReactiveCommands.subscribe(channels);
    }

    @SafeVarargs
    public final Mono<Void> listenToPatterns(K... pattern) {
        return redisPubSubReactiveCommands.psubscribe(pattern);
    }

    public void publishToChannel(K channel, V msg) {
        redisPubSubReactiveCommands.publish(channel, msg).block();
    }

    public void put(K key, V value) {
        redisCommands.set(key, value);
    }

    public V get(K key) {
        return redisCommands.get(key);
    }

    private static class RedisByteCodec <K, V> implements RedisCodec <K, V> {

        private boolean isSerializable(Object object) {
            return object instanceof Serializable;
        }

        @Override
        public K decodeKey(ByteBuffer byteBuffer) {
            byte[] arr = new byte[byteBuffer.remaining()];
            byteBuffer.get(arr);
            K key = SerializationUtils.deserialize(arr);

            if (key instanceof ByteSerializationWrapper) {
                ByteSerializationWrapper<K> wrapper = (ByteSerializationWrapper<K>)key;
                return wrapper.getObject();
            } else {
                return key;
            }
        }

        @Override
        public V decodeValue(ByteBuffer byteBuffer) {
            byte[] arr = new byte[byteBuffer.remaining()];
            byteBuffer.get(arr);

            V value = SerializationUtils.deserialize(arr);
            if (value instanceof ByteSerializationWrapper) {
                ByteSerializationWrapper<V> wrapper = (ByteSerializationWrapper<V>)value;
                return wrapper.getObject();
            } else {
                return value;
            }
        }

        @Override
        public ByteBuffer encodeKey(K k) {
            if (isSerializable(k)) {
                return ByteBuffer.wrap(SerializationUtils.serialize((Serializable) k));
            }
            ByteSerializationWrapper<K> wrapper = new ByteSerializationWrapper<>(k);
            byte[] array = SerializationUtils.serialize(wrapper);
            return ByteBuffer.wrap(array);
        }

        @Override
        public ByteBuffer encodeValue(V v) {
            if (isSerializable(v)) {
                return ByteBuffer.wrap(SerializationUtils.serialize((Serializable) v));
            }
            ByteSerializationWrapper<V> wrapper = new ByteSerializationWrapper<>(v);
            byte[] array = SerializationUtils.serialize(wrapper);
            return ByteBuffer.wrap(array);
        }
    }
}
