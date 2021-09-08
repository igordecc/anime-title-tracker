package com.animetitletracker.kafka;

import com.animetitletracker.common.AppConfig;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.ObjectMapper;


import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;


public class KafkaProducerForANN {
    public static void runProducer(String data,  AppConfig appConfig) {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                appConfig.getKafkaBootstrapServers()); //"localhost:9092,localhost:9093,localhost:9094"
        props.put(ProducerConfig.CLIENT_ID_CONFIG, "KafkaProducerForOpenSky");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                LongSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class); // JsonSerializer for JSON data type

        ObjectMapper mapper = new ObjectMapper();

        KafkaProducer<Long, ANNSchema> producer = new KafkaProducer<>(props);


        try {
            List<ANNSchema> openSkyObjects = Arrays.asList(mapper.readValue(data,
                    ANNSchema[].class));

            for (int i = 0; i < openSkyObjects.size(); i ++) {
                long time = System.currentTimeMillis();
                ProducerRecord<Long, ANNSchema> record = new ProducerRecord<>(appConfig.getTopic(),
                        time + i, openSkyObjects.get(i));

                producer.send(record);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            producer.flush();
            producer.close();
        }
    }
}
