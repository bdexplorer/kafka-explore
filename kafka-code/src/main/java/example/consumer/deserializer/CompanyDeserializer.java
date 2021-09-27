package example.consumer.deserializer;

import com.alibaba.fastjson.JSON;
import example.producer.seriablizer.custom.bean.Company;
import org.apache.kafka.common.serialization.Deserializer;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

public class CompanyDeserializer implements Deserializer<Company> {
    @Override
    public Company deserialize(String topic, byte[] data) {
        if(data == null){
            return null;
        }
        if(data.length<8){
            // throw Exception
        }
        String name = null;
        String address = null;
        ByteBuffer buffer = ByteBuffer.wrap(data);
        int nameLength = buffer.getInt();
        byte[] nameBts = new byte[nameLength];
        buffer.get(nameBts);

        int addressLen = buffer.getInt();
        byte[] addBts = new byte[addressLen];
        buffer.get(addBts);

        try {
            name = new String(nameBts, "UTF-8");
            address = new String(addBts,"UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        Company company = new Company();
        company.setName(name);
        company.setAddress(address);
        return company;
        //return JSON.parseObject(data, Company.class);
    }
}