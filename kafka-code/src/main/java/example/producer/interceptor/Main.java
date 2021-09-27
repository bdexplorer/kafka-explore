package example.producer.interceptor;

import example.utils.FormatConfig;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Properties;
import java.util.concurrent.ExecutionException;

public class Main {
    public static final String topic = "test1";
    KafkaProducer<String, String> producer;

    @BeforeEach
    public void init() {
        Properties properties = FormatConfig.initProducerConfig();
        properties.put(ProducerConfig.ACKS_CONFIG, "0");
        properties.put(ProducerConfig.INTERCEPTOR_CLASSES_CONFIG, ProducerInterceptorPrefix.class.getName() + "," + ProducerInterceptorPrefix2.class.getName());
        producer = new KafkaProducer<>(properties);
    }

    @Test // 测试 发送100条消息
    public void test100MsgSend() {
        int i = 0;
        while (i < 100) {
            ProducerRecord<String, String> record = new ProducerRecord<>(topic, "msg" + i++);
            try {
                RecordMetadata recordMetadata = producer.send(record).get();
                System.out.println(recordMetadata.topic() + "-" + recordMetadata.partition() + "-" + recordMetadata.offset());
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
        producer.close();
    }
}
