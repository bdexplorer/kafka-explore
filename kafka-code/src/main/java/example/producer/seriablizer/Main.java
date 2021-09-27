package example.producer.seriablizer;

import example.utils.FormatConfig;
import example.producer.seriablizer.custom.CompanySerializer;
import example.producer.seriablizer.custom.bean.Company;
import org.apache.kafka.clients.producer.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Properties;
import java.util.concurrent.ExecutionException;

public class Main {
    public static final String topic = "test3";
    KafkaProducer<String, Company> producer;

    @BeforeEach
    public void init() {
        Properties properties = FormatConfig.initProducerConfig();
        //properties.replace(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, CompanySerializer.class.getName());
        properties.replace(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ProtoSerializer.class.getName());
        properties.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, "1000");
        producer = new KafkaProducer<>(properties);
    }

    @Test
    public void testSerializer() throws ExecutionException, InterruptedException {
        Company company = Company.builder().name("hiddenkafka").address("China").build();
        System.out.println(company);
        ProducerRecord<String, Company> producerRecord = new ProducerRecord<>(topic, company);
        producer.send(producerRecord).get();
    }
}
