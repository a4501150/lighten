package com.frostflames.lighten.kafka;

import com.frostflames.lighten.thread.CommonUtil;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.processors.PublishProcessor;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class LightenKafkaClient {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private String bootstrapServers;

    // consumer
    private Consumer<String, String> consumer;
    private String groupID, offsetResetType;
    private boolean shouldCommit, autoCommit;
    private AtomicBoolean newSubscriptionEntered;
    private final Map<String, PublishProcessor<String>> topic2streamMap;

    // producer
    private Producer<String, String> producer;
    private String clientID;


    public LightenKafkaClient(String bootstrapServers) {
        this.bootstrapServers = bootstrapServers;
        this.newSubscriptionEntered = new AtomicBoolean(false);
        this.topic2streamMap = new ConcurrentHashMap<>();
    }

    // Consumer Code start
    public void initConsumer(String groupID,
                             String offsetResetType,
                             boolean shouldCommit,
                             boolean autoCommit) {

        if (consumer != null) {
            logger.debug("Please dont re-initialize consumer");
            return;
        }

        this.groupID = groupID;
        this.offsetResetType = offsetResetType;
        this.shouldCommit = shouldCommit;
        this.autoCommit = autoCommit;

        consumer = new KafkaConsumer<>(getConsumerProperties());
        Executors.privilegedThreadFactory().newThread(this::consumerLoop).start();
    }

    public Flowable<String> listenToTopic(String topic) {
        if (consumer == null) {
            logger.debug("please initialize consumer");
            throw new RuntimeException("consumer not initialize");
        }

        if (topic2streamMap.containsKey(topic)) {
            return topic2streamMap.get(topic);
        }

        // new topic entered.
        topic2streamMap.put(topic, PublishProcessor.create());
        this.newSubscriptionEntered.set(true);

        return topic2streamMap.get(topic)
                .toObservable()
                .doOnDispose(() -> freeResource(topic))
                .toFlowable(BackpressureStrategy.ERROR);
    }

    private void freeResource(String topic) {
        topic2streamMap.remove(topic);
    }

    private Properties getConsumerProperties() {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupID);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 1);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, autoCommit+"");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, offsetResetType);

        props.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, 100);
        props.put(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, 1);
        props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, 30000000);

        //props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "1");
        return props;
    }

    private void consumerLoop() {

        Thread.currentThread().setName("consumerLoop thread");

        while (true) {

            if (newSubscriptionEntered.get()) {
                logger.info("newSubscriptionEntered!");
                consumer.subscribe(topic2streamMap.keySet());
                newSubscriptionEntered.set(false);
            }

            if (consumer.subscription().isEmpty()) {
                logger.info("No subscription for one second");
                CommonUtil.sleep(1, TimeUnit.SECONDS);
                continue;
            }

            ConsumerRecords<String, String> consumerRecords = consumer.poll(Duration.ofMillis(1000));
            topic2streamMap.forEach((topic, streamPublisher) -> {
                consumerRecords.records(topic).forEach(record -> {
                    if (!streamPublisher.hasSubscribers()) {
                        return;
                    }
                    streamPublisher.onNext(record.value());

                    if ((!autoCommit) && shouldCommit) {
                        consumer.commitAsync();
                    }
                });
            });
        }
    }

    // Consumer Code End

    // Producer Code Start
    public void initProducer(String clientID) {
        if (producer != null) {
            logger.debug("Please dont re-initialize producer");
            return;
        }

        this.clientID = clientID;
        this.producer = new KafkaProducer<>(getProducerProperties());
    }

    private Properties getProducerProperties() {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.CLIENT_ID_CONFIG, clientID);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return props;
    }

    /**
     * This is async call. gPRC is better for immediately sending
     * @param topic
     * @param payload
     */
    public void publishMessage(String topic, String payload) {
        if (producer == null) {
            logger.debug("please initialize producer first");
        }
        this.producer.send(new ProducerRecord<>(topic, payload));
    }

}
