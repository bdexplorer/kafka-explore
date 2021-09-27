package example;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;

public class SimpleProducer {
    private static final String brokerList = "192.168.1.202:9092";
    private static final String topic = "test1";

    public static void main(String[] args) {
        Properties props = new Properties();
        props.setProperty("key.serializer", StringSerializer.class.getName());
        props.setProperty("value.serializer", StringSerializer.class.getName());
        props.setProperty("bootstrap.servers", brokerList);

        KafkaProducer<String, String> producer = new KafkaProducer<>(props);
        ProducerRecord<String, String> producerRecord = new ProducerRecord<>(topic, "hello Kafka!");
        producer.send(producerRecord);
        producer.close();
    }
}
