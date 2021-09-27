package example.consumer.mutil.thread;

import example.utils.FormatConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class MultiConsumerThreadDemo2 {
    public static final String TOPIC = "test1";


    public static void main(String[] args) {
        Properties properties = FormatConfig.initConsumerConfig();
        int consumerThreads = 4;
        for(int i=0; i<consumerThreads; i++){
            Thread t = new Thread(new KafkaConsumerThread1(properties, TOPIC, 5));
            t.start();
        }
    }

    private static class KafkaConsumerThread1 implements Runnable {
        private KafkaConsumer<String, String> kafkaConsumer;
        private ExecutorService executorService;
        private int threadNum;

        public KafkaConsumerThread1(Properties properties, String topic, int threadNum) {
            this.kafkaConsumer = new KafkaConsumer<>(properties);
            kafkaConsumer.subscribe(Collections.singletonList(topic));
            this.threadNum = threadNum;
            executorService = new ThreadPoolExecutor(
                    threadNum, 
                    threadNum, 
                    0L, 
                    TimeUnit.MILLISECONDS,
                    new ArrayBlockingQueue<>(1000),
                    new ThreadPoolExecutor.CallerRunsPolicy());
        }

        @Override
        public void run() {
            while (true){
                ConsumerRecords<String, String> records = kafkaConsumer.poll(Duration.ofMillis(1000));
                if (!records.isEmpty()){
                    executorService.submit(new RecordHandler(records));
                }
            }
        }
    }

    private static class RecordHandler implements Runnable{
        private final ConsumerRecords<String, String> records;

        public RecordHandler(ConsumerRecords<String, String> records) {
            this.records = records;
        }

        @Override
        public void run() {
            // 业务逻辑
        }
    }
}