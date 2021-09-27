package example.producer.seriablizer.custom;

import com.alibaba.fastjson.JSON;
import example.producer.seriablizer.custom.bean.Company;
import org.apache.kafka.common.serialization.Serializer;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

/**
 * 自定义序列化器
 */
public class CompanySerializer implements Serializer<Company> {
    @Override
    public byte[] serialize(String topic, Company data) {
        if (data == null)
            return null;
        byte[] name, address;
        try {
            if (data.getName() != null) {
                name = data.getName().getBytes("UTF-8");
            } else {
                name = new byte[0];
            }

            if (data.getAddress() != null) {
                address = data.getAddress().getBytes("UTF-8");
            } else {
                address = new byte[0];
            }

            ByteBuffer buffer = ByteBuffer.allocate(4 + 4 + name.length + address.length);
            buffer.putInt(name.length);
            buffer.put(name);
            buffer.putInt(address.length);
            buffer.put(address);
            if(buffer.hasArray()) {
                return buffer.array();
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return new byte[0];
       // return JSON.toJSONBytes(data);
    }
}
