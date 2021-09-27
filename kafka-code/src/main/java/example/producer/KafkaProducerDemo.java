package example.producer;


import example.utils.FormatConfig;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class KafkaProducerDemo {
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
        producer.send(producerRecord);
    }

    @Test
    public void testSync() {
        ProducerRecord<String, String> producerRecord = new ProducerRecord<>(topic, "kafka sync send!");
        try {
            producer.send(producerRecord).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    @Test // 获取元数据信息
    public void testAnotherSync() {
        ProducerRecord<String, String> producerRecord = new ProducerRecord<>(topic, "kafka another sync send!");
        Future<RecordMetadata> future = producer.send(producerRecord);
        try {
            RecordMetadata recordMetadata = future.get();
            System.out.println(recordMetadata.topic() + "-" + recordMetadata.partition() + "-" + recordMetadata.offset());
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

    }

    @Test // 通过Callback回调
    public void testAsync() {
        ProducerRecord<String, String> producerRecord = new ProducerRecord<>(topic, "kafka async send!");
        producer.send(producerRecord, new Callback() {
            @Override
            public void onCompletion(RecordMetadata metadata, Exception exception) {
                if (exception != null) { // 发送失败
                    exception.printStackTrace();
                } else { // 发送成功
                    System.out.println(metadata.topic() + "-" + metadata.partition() + "-" + metadata.offset());
                }
            }
        });
    }

    @Test // 测试 发送100条消息
    public void test100MsgSend() {
        int i = 0;
        while (i < 100) {
            ProducerRecord<String, String> record = new ProducerRecord<>(topic, "msg" + i++);
            try {
                producer.send(record).get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

    @AfterEach
    public void close() {
        // producer.close();
        // 还有一个带超时参数的close
        producer.close(Duration.ofMillis(1000));
    }


}
