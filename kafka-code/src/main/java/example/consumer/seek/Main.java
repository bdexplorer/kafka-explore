package example.consumer.seek;

import example.utils.FormatConfig;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

public class Main {
    public static final String TOPIC = "test1";
    public static final AtomicBoolean IS_RUNNING = new AtomicBoolean(true);

    private KafkaConsumer<String, String> consumer;

    @BeforeEach
    public void testConsumer() {
        Properties properties = FormatConfig.initConsumerConfig();
        consumer = new KafkaConsumer<>(properties);
        consumer.subscribe(Pattern.compile("test.*"));

    }

    @AfterEach
    public void close() {
        consumer.close();
    }

    /**
     * 执行seek方法之前需要先执行一次poll方法，等分配到分区之后才可以重置消费位置。poll方法会分配分区。
     */
    @Test
    public void testSeek(){
        consumer.subscribe(Arrays.asList(TOPIC));
        consumer.poll(Duration.ofMillis(1000));
        Set<TopicPartition> assignments = consumer.assignment();
        while (assignments.size() == 0) {
            consumer.poll(Duration.ofMillis(100));
            assignments = consumer.assignment();
        }
        for(TopicPartition assignment : assignments) {
            consumer.seek(assignment, 10);
        }
        while (IS_RUNNING.get()) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));
            for (ConsumerRecord<String, String> record : records) {
                System.out.println("value: " + record.value());
            }
        }
    }

    @Test
    public void testSeekToEnd() {
        consumer.subscribe(Arrays.asList(TOPIC));
        consumer.poll(Duration.ofMillis(1000));
        Set<TopicPartition> assignments = consumer.assignment();
        while (assignments.size() == 0) {
            consumer.poll(Duration.ofMillis(100));
            assignments = consumer.assignment();
        }
        consumer.seekToEnd(assignments);
    }

    @Test
    public void testSeekToTime() {
        consumer.subscribe(Arrays.asList(TOPIC));
        consumer.poll(Duration.ofMillis(1000));
        Set<TopicPartition> assignments = consumer.assignment();
        Map<TopicPartition, Long> timestampToSearch = new HashMap<>();
        for(TopicPartition tp : assignments) {
            timestampToSearch.put(tp, System.currentTimeMillis()-1*24*3600*3600);
        }
        Map<TopicPartition, OffsetAndTimestamp> offsets = consumer.offsetsForTimes(timestampToSearch);
        for(TopicPartition tp : assignments) {
            OffsetAndTimestamp offsetAndTimestamp = offsets.get(tp);
            if(offsetAndTimestamp != null) {
                consumer.seek(tp, offsetAndTimestamp.offset());
            }
        }
    }
    @Test
    public void testOffsetToDB() {
        // 将消费位移保存到DB中
    }
}
