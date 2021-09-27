package example.producer.partitioner;

import example.utils.FormatConfig;
import example.producer.seriablizer.custom.bean.Company;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Properties;

public class Main {
    public static final String topic = "test1";
    KafkaProducer<String, Company> producer;

    @BeforeEach
    public void init() {
        Properties properties = FormatConfig.initProducerConfig();
        properties.put(ProducerConfig.PARTITIONER_CLASS_CONFIG, DemoPartitioner.class.getName());
        producer = new KafkaProducer<>(properties);
    }

    @Test
    public void testSerializer() {
        Company company = Company.builder().name("hiddenkafka").address("China").build();
        ProducerRecord<String, Company> producerRecord = new ProducerRecord<>(topic, company);
        producer.send(producerRecord);
    }
}
