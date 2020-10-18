
package br.com.zup.bank.account.api.email.configs;

import br.com.zup.bank.account.api.email.models.EmailInfo;
import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;

public class SenderConfig {
    private Map<String, Object> props = new HashMap<>();
    private KafkaTemplate<String, String> template;
    private static SenderConfig instance;
    
    //@Value("${KAFKA_BOOTSTRAP_SERVERS}")
    //private String bootstrapServers;

    private SenderConfig() {
        propsConfig();
    }

    private void propsConfig(){
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "broker:29092,localhost:9092");
        props.put(ProducerConfig.RETRIES_CONFIG, 0);
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
        props.put(ProducerConfig.LINGER_MS_CONFIG, 1);
        props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
                
        DefaultKafkaProducerFactory<String, String> pf = new DefaultKafkaProducerFactory<>(props);
        template = new KafkaTemplate<>(pf, true);
    }
    
    synchronized public static SenderConfig getInstance(){
        if(instance == null) {
            instance = new SenderConfig();  
        }
        return instance;
    }
    
    public KafkaTemplate<String,String> getKafkaTemplate(){
        return template;
    }
}
