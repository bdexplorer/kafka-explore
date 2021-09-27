package example.consumer;

import example.utils.FormatConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

public class KafkaConsumerDemo {
    /**
     * subscribe:
     * 如果前后两次订阅了不同的主题，那么消费者按最后一次为准。
     * 如果消费者采用正则表达式订阅，在之后的过程中，如果新建主题，并且主题的名称与正则表达式匹配，那么多个消费者可以消费到新添加主题中的消息。
     */

    public static final String TOPIC = "test1";
    public static final AtomicBoolean IS_RUNNING = new AtomicBoolean(true);

    private KafkaConsumer<String, String> consumer;

    @BeforeEach
    public void testConsumer() {
        Properties properties = FormatConfig.initConsumerConfig();
        consumer = new KafkaConsumer<>(properties);
    }

    @AfterEach
    public void close() {
        consumer.close();
    }

    @Test
    public void testConsumer1() {
        consumer.subscribe(Arrays.asList(TOPIC));
        while (IS_RUNNING.get()) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));
            for (ConsumerRecord<String, String> record : records) {
                System.out.println("topic: " + record.topic());
                System.out.println("partition: " + record.partition());
                System.out.println("offset: " + record.offset());
                System.out.println("key: " + record.key());
                System.out.println("value: " + record.value());
            }
        }
    }

    @Test
    public void testPatternConsumer1() {
        consumer.subscribe(Pattern.compile("test.*"));
        while (IS_RUNNING.get()) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));
            for (ConsumerRecord<String, String> record : records) {
                System.out.println("value: " + record.value());
            }
        }
    }

    @Test
    public void testAssignConsumer() {
        List<TopicPartition> partitions = new ArrayList<>();

        List<PartitionInfo> partitionInfos = consumer.partitionsFor(TOPIC);
        while (partitionInfos == null) {
            partitionInfos = consumer.partitionsFor(TOPIC);
        }
        for (PartitionInfo partitionInfo : partitionInfos) {
            partitions.add(new TopicPartition(partitionInfo.topic(), partitionInfo.partition()));
        }
        consumer.assign(partitions);
        while (IS_RUNNING.get()) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));
            for (ConsumerRecord<String, String> record : records) {
                System.out.println("value: " + record.value());
            }
        }
    }
}
