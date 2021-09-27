package example.topic;

import kafka.admin.TopicCommand;
import org.junit.jupiter.api.Test;

public class Main {

    @Test
    public void createTopic() {
        String[] options = new String[] {"--zookeeper", "bigdata01:2181/kafka",
        "--create",
        "--replication-factor", "3",
        "--partitions", "3",
        "--topic", "topic-create-api"};

        TopicCommand.main(options);
    }

    @Test
    public void listTopic() {
        String[] options = new String[] {"--zookeeper", "bigdata01:2181/kafka",
                "--list"};
        TopicCommand.main(options);
    }
}
