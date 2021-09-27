package example.consumer.deserializer;

import example.producer.seriablizer.custom.bean.Company;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;
import org.apache.kafka.common.serialization.Deserializer;

public class ProtoDeserializer implements Deserializer<Company> {
    public Company deserialize(String topic, byte[] data) {
        if (data == null) {
            return null;
        }
        Schema<Company> schema = RuntimeSchema.getSchema(Company.class);
        Company company = new Company();
        ProtostuffIOUtil.mergeFrom(data, company, schema);
        return company;
    }
}