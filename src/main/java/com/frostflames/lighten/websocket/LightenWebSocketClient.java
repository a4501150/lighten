package com.frostflames.lighten.websocket;

import io.reactivex.*;
import io.reactivex.processors.PublishProcessor;
import okhttp3.*;
import okio.ByteString;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.UrlValidator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

public class LightenWebSocketClient {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final OkHttpClient okHttpClient;
    private final String endpoint;

    private final Map<String, PublishProcessor<String>> topic2streamMap;
    private final Map<String, WebSocket> topic2websocketMap;

    private final Map<String, String> topic2subscribtionTextMsgMap;
    private final Map<String, ByteString> topic2subscribtionBinaryMsgMap;

    private final Set<Integer> nonAutoReconnectCodeSet;

    /**
     * Please refer to Manual for help and usage.
     * @param endpoint base endpoint
     * @param pintInterval ping interval
     */
    public LightenWebSocketClient(String endpoint, Duration pintInterval) {

        UrlValidator urlValidator = new UrlValidator(new String[]{"ws", "wss"});

        if (!urlValidator.isValid(endpoint)) {
            throw new IllegalArgumentException("url is not valid " + endpoint);
        }

        okHttpClient = new OkHttpClient
                .Builder()
                .pingInterval(pintInterval)
                .build();

        this.endpoint = endpoint;
        topic2streamMap = new HashMap<>();
        topic2websocketMap = new HashMap<>();
        topic2subscribtionTextMsgMap = new HashMap<>();
        topic2subscribtionBinaryMsgMap = new HashMap<>();

        nonAutoReconnectCodeSet = new HashSet<>();
        nonAutoReconnectCodeSet.add(400);
        nonAutoReconnectCodeSet.add(1000);

        logger.info(endpoint + " init");
    }

    /**
     *
     * @param topic can be null, in case of we only use the endpoint. Otherwise, it is the topic we want to sub
     * @param isBinaryType return payload from websocket is binary ? if not this should be false
     * @return Flowable<String> which is the stream of subscribtion
     */
    public Flowable<String> listenToTopic(String topic, boolean isBinaryType) {
        topic = normalizeTopic(topic);

        if (topic2streamMap.containsKey(topic)) {
            return topic2streamMap.get(topic);
        }

        AtomicReference<String> stringAtomicReference = new AtomicReference<>(topic);
        PublishProcessor<String> publishProcessor = PublishProcessor.create();
        topic2streamMap.put(topic, publishProcessor);
        subscribeHelper(topic, isBinaryType);

        return publishProcessor
                .toObservable()
                .doOnDispose(() -> freeResource(stringAtomicReference.get(), isBinaryType))
                .toFlowable(BackpressureStrategy.LATEST);
    }

    public void sendTextMsg(String topic, String payload, boolean shouldNormalize) {
        if (shouldNormalize) {
            topic = normalizeTopic(topic);
        }

        this.topic2websocketMap.get(topic).send(payload);
        this.topic2subscribtionTextMsgMap.put(topic, payload);
    }

    public void sendBinaryMsg(String topic, byte[] payload, boolean shouldNormalize) {
        if (shouldNormalize) {
            topic = normalizeTopic(topic);
        }

        ByteString byteString = new ByteString(payload);
        this.topic2websocketMap.get(topic).send(byteString);
        this.topic2subscribtionBinaryMsgMap.put(topic,byteString);
    }

    private void subscribeHelper(String topic, boolean isBinaryType) {
        Request request = (new Request.Builder()).url(topic).build();
        RxJavaStreamListener listener = new RxJavaStreamListener(topic, isBinaryType);
        WebSocket webSocket = okHttpClient.newWebSocket(request, listener);
        topic2websocketMap.put(topic, webSocket);
    }

    private void freeResource(String topic, boolean isBinaryType) {
        logger.debug(topic + " disposed, close websocket");
        topic2websocketMap.get(topic).close(1000,"Manual Close");
        if (isBinaryType) {
            topic2subscribtionBinaryMsgMap.remove(topic);
        } else {
            topic2subscribtionTextMsgMap.remove(topic);
        }
    }

    private String normalizeTopic(String topic) {
        if (StringUtils.isAllBlank(topic)) {
            topic = endpoint;
            logger.info("Auto assign endpoint as topic");
        } else {
            topic = endpoint + topic;
            logger.info("Auto concat endpoint and topic, final " + topic);
        }
        return topic;
    }

    class RxJavaStreamListener extends WebSocketListener {

        private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
        private final boolean isBinary;
        private final String topic;
        private boolean isReconnectScheduled = false;

        RxJavaStreamListener(String topic, boolean isBinary) {
            this.isBinary = isBinary;
            this.topic = topic;
        }

        private void reconnect() {
            if (isReconnectScheduled) {
                return;
            }
            isReconnectScheduled = true;
            LOGGER.debug("reconnect");
            try {
                Thread.sleep(3000);
            } catch (InterruptedException ex) {
                LOGGER.warn("Interrupted! {}", ex.getMessage());
                // Restore interrupted state...
                Thread.currentThread().interrupt();
            } finally {
                subscribeHelper(topic,isBinary);
                if (isBinary) {
                    topic2subscribtionBinaryMsgMap.forEach((topic, msg) -> {
                        sendBinaryMsg(topic, msg.toByteArray(), false);
                    });
                } else {
                    topic2subscribtionTextMsgMap.forEach((topic, msg) -> {
                        sendTextMsg(topic, msg, false);
                    });
                }
            }
        }

        @Override
        public void onClosed(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
            super.onClosed(webSocket, code, reason);
            LOGGER.debug("onClosed {} {} {}",code, reason, topic);
            if (!nonAutoReconnectCodeSet.contains(code)) {
                reconnect();
            }
        }

        @Override
        public void onClosing(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
            super.onClosing(webSocket, code, reason);
            LOGGER.debug("onClosing {} {} {}",code, reason, topic);
            if (!nonAutoReconnectCodeSet.contains(code)) {
                reconnect();
            }
        }

        @Override
        public void onFailure(@NotNull WebSocket webSocket, @NotNull Throwable t, @Nullable Response response) {
            super.onFailure(webSocket, t, response);
            LOGGER.error("onFailure", t);
            LOGGER.debug("onFailure reason {}\n{}\ntopic {}",t.getMessage(), response, topic);
            if (response == null || !nonAutoReconnectCodeSet.contains(response.code())) {
                reconnect();
            }
        }

        @Override
        public void onMessage(@NotNull WebSocket webSocket, @NotNull String text) {
            super.onMessage(webSocket, text);
            PublishProcessor<String> textStream = topic2streamMap.get(topic);
            if (textStream.hasSubscribers()) {
                textStream.onNext(text);
            }
        }

        @Override
        public void onMessage(@NotNull WebSocket webSocket, @NotNull ByteString bytes) {
            super.onMessage(webSocket, bytes);
            PublishProcessor<String> textStream = topic2streamMap.get(topic);
            if (textStream.hasSubscribers()) {
                textStream.onNext(bytes.utf8());
            }
        }

        @Override
        public void onOpen(@NotNull WebSocket webSocket, @NotNull Response response) {
            super.onOpen(webSocket, response);
            LOGGER.debug("onOpen {} {}", response, topic);
        }
    }
}
