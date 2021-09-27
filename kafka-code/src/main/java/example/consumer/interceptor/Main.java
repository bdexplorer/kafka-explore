package example.consumer.interceptor;

import example.utils.FormatConfig;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

public class Main {
    public static final String TOPIC = "test1";
    public static final AtomicBoolean IS_RUNNING = new AtomicBoolean(true);

    private KafkaConsumer<String, String> consumer;

    @BeforeEach
    public void testConsumer() {
        Properties properties = FormatConfig.initConsumerConfig();
        properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        properties.put(ConsumerConfig.INTERCEPTOR_CLASSES_CONFIG, ConsumerInterceptorTTL.class.getName());
        consumer = new KafkaConsumer<>(properties);
       // consumer.subscribe(Pattern.compile("test.*"));
    }

    @AfterEach
    public void close() {
        consumer.close();
    }

    @Test
    public void testCommitSync() {
        consumer.subscribe(Pattern.compile("test.*"));
        while (IS_RUNNING.get()) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));
            for (ConsumerRecord<String, String> record : records) {
                System.out.println("value: " + record.value());
                long offset = record.offset();
                TopicPartition topicPartition = new TopicPartition(record.topic(), record.partition());
                consumer.commitSync(Collections.singletonMap(topicPartition, new OffsetAndMetadata(offset + 1)));
            }
        }
    }
}
