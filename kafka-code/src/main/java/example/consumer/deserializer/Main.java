package example.consumer.deserializer;

import example.producer.seriablizer.custom.bean.Company;
import example.utils.FormatConfig;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

public class Main {
    public static final String TOPIC = "test3";
    public static final AtomicBoolean IS_RUNNING = new AtomicBoolean(true);

    private KafkaConsumer<String, Company> consumer;

    @BeforeEach
    public void testConsumer() {
        Properties properties = FormatConfig.initConsumerConfig();
        //properties.replace(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, CompanyDeserializer.class.getName());
        properties.replace(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ProtoDeserializer.class.getName());
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
            ConsumerRecords<String, Company> records = consumer.poll(Duration.ofMillis(1000));
            for (ConsumerRecord<String, Company> record : records) {
                System.out.println("value: " + record.value());
            }
        }
    }
}
