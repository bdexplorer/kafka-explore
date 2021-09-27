package example.consumer.rebalance;

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
        properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        consumer = new KafkaConsumer<>(properties);
        consumer.subscribe(Pattern.compile("test.*"));

    }

    @AfterEach
    public void close() {
        consumer.close();
    }

    /**
     * commitSync方法会根据poll方法拉取的最新位移来进行提交。只要不发生不可恢复的错误，它就会阻塞直至提交完成。
     */

    @Test
    public void testCommitSync() {
        Map<TopicPartition, OffsetAndMetadata> currentOffsets = new HashMap<>();
        consumer.subscribe(Collections.singletonList(TOPIC), new ConsumerRebalanceListener() {
            @Override
            public void onPartitionsRevoked(Collection<TopicPartition> partitions) {
                consumer.commitSync(currentOffsets);
                currentOffsets.clear();
            }

            @Override
            public void onPartitionsAssigned(Collection<TopicPartition> partitions) {
                // do noting
            }
        });

        final int minBatchSize = 200;
        List<ConsumerRecord> buffer = new ArrayList<>();
        while (IS_RUNNING.get()) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));
            for (ConsumerRecord<String, String> record : records) {

                buffer.add(record);
            }
            if (buffer.size() > minBatchSize) {
                for (ConsumerRecord record : buffer) {
                    currentOffsets.put(new TopicPartition(record.topic(), record.partition()), new OffsetAndMetadata(record.offset() + 1));
                    System.out.println("value: " + record.value());
                }
                buffer.clear();
            }
        }
    }

    // 批量处理加批量提交，降低提交频率。（但是这种提交方式需要注意消息的延迟，最好价格超时参数.
}
