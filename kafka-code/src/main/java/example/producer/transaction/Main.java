package example.producer.transaction;

import example.utils.FormatConfig;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Properties;

public class Main {
    /**
     * KafkaProducer提供了5个与事务相关的方法，
     * void initTransactions()
     * void beginTransaction()
     * void sendOffsetToTransaction(Map<TopicPartition, OffsetAndMetadata> offsets, String consumerGroupId)
     * void commitTransaction()
     * void abortTransaction()
     */

    public static final String topic = "test1";
    KafkaProducer<String, String> producer;

    @BeforeEach
    public void init() {
        Properties properties = FormatConfig.initProducerConfig();
        producer = new KafkaProducer<>(properties);
    }

    //1. KafkaProducer是线程安全的，可以在多个线程中共享单个Producer实例。

    //2. kafka发送消息有三种模式：发后即忘（fire-and-forgot）、同步（sync）、异步（async）
    @Test
    public void testFireAndForget() {
        ProducerRecord<String, String> producerRecord = new ProducerRecord<>(topic, "hello Kafka!");
        producer.initTransactions();
        producer.beginTransaction();
        try {
            producer.send(producerRecord);
            producer.commitTransaction();
        } catch (Exception ex) {
            producer.abortTransaction();
        }
        producer.close();
    }
}
