package example.consumer.manaual.commit;

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
        consumer.subscribe(Pattern.compile("test.*"));
        while (IS_RUNNING.get()) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));
            for (ConsumerRecord<String, String> record : records) {
                System.out.println("value: " + record.value());
            }
            System.out.println("test commit");
            consumer.commitSync();
            System.out.println("end test commit");
        }
    }

    // 批量处理加批量提交，降低提交频率。（但是这种提交方式需要注意消息的延迟，最好价格超时参数。）
    @Test
    public void testCommitSync1() {
        final int minBatchSize = 200;
        List<ConsumerRecord> buffer = new ArrayList<>();
        while (IS_RUNNING.get()) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));
            for (ConsumerRecord<String, String> record : records) {
                buffer.add(record);
            }
            if (buffer.size() > minBatchSize) {
                for (ConsumerRecord record : buffer) {
                    System.out.println("value: " + record.value());
                }
                consumer.commitSync();
                buffer.clear();
            }
        }
    }

    // 带参数的同步位移提交，可以指定分区的位移，每处理一条消息提交一次。
    @Test
    public void testCommitSync2() {
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

    // 按分区粒度提交消费位移
    @Test
    public void testCommitSync3() {
        ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));
        for (TopicPartition partition : records.partitions()) {
            List<ConsumerRecord<String, String>> partitionRecords = records.records(partition);
            for (ConsumerRecord<String, String> partitionRecord : partitionRecords) {
                System.out.println(partitionRecord.value());
            }
            long lastConsumerOffset = partitionRecords.get(partitionRecords.size()-1).offset();
            consumer.commitSync(Collections.singletonMap(partition, new OffsetAndMetadata(lastConsumerOffset + 1)));
        }
    }

    /**
     * OffsetCommitCallback是异步提交的的回调方法，当位移提交完成后会调用OffsetCommitCallback的onComplete()方法。
     *
     * 异步提交失败不建议重试，如果某次异步提交x失败了，但是下一次异步提交x+4成功了，重试机制进行异步提交会覆盖掉x+4。
     *
     * 可以设置一个递增的序号维护异步提交的顺序，每次异步提交之后就增大序号所对应的值，遇到提交失败需要重试时，检测序号值和offset，如果序号值大于offset，则取消重试。
     *
     * 一般情况下，位移提交失败的情况很少发生，不重试也没有关系，后面会有成功的。如果消费者正常退出或者发生再均衡的情况，那么可以在退出或再均衡执行之前使用同步提交的方式做最后把关。
     */
    @Test
    public void testCommitAsync() {
        try {
            while(IS_RUNNING.get()) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));
                //业务逻辑
                consumer.commitAsync(new OffsetCommitCallback(){
                    @Override
                    public void onComplete(Map<TopicPartition, OffsetAndMetadata> offsets, Exception exception){
                        if(exception == null){
                            System.out.println(offsets);
                        } else {
                            System.err.println("commit Fail");
                        }
                    }
                });
            }
        } finally {
            // 保证异常退出时可以提交消费位移
            consumer.commitSync();
        }

    }
}
