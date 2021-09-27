package example.consumer.mutil.thread;

import example.utils.FormatConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;

public class MultiConsumerThreadDemo {
    public static final String TOPIC = "test1";

    public static void main(String[] args) {
        Properties properties = FormatConfig.initConsumerConfig();
        int consumerThreads = 4;
        for(int i=0; i<consumerThreads; i++){
            Thread t = new Thread(new KafkaConsumerThread(properties, TOPIC));
            t.start();
        }
    }

    private static class KafkaConsumerThread implements Runnable {
        private KafkaConsumer<String, String> kafkaConsumer;

        public KafkaConsumerThread(Properties properties, String topic) {
            this.kafkaConsumer = new KafkaConsumer<>(properties);
            this.kafkaConsumer.subscribe(Arrays.asList(topic));
        }

        @Override
        public void run() {
            while (true){
                ConsumerRecords<String, String> records = kafkaConsumer.poll(Duration.ofMillis(1000));
                for (ConsumerRecord<String, String> record : records) {
                    // 业务逻辑
                }
            }
        }
    }
}