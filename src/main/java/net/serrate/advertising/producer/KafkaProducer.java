package net.serrate.advertising.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;
import net.serrate.advertising.shared.Context;
import net.serrate.advertising.shared.ImpressionEvent;

import java.util.*;

/**
 * Created by mserrate on 06/03/16.
 */
public class KafkaProducer {
    private static final String BROKER_LIST = "kafka.broker.list";
    private static final String KAFKA_TOPIC = "kafka.advertising.topic";

    public static void run(Context context) throws Exception {
        // Producer properties
        Properties properties = new Properties();
        properties.put("metadata.broker.list", context.getString(BROKER_LIST));
        properties.put("serializer.class", "kafka.serializer.StringEncoder");
        properties.put("request.required.acks", "1");
        ProducerConfig producerConfig = new ProducerConfig(properties);

        final Producer<String, String> producer = new Producer<String, String>(producerConfig);

        ObjectMapper mapper = new ObjectMapper();
        Random random = new Random();
        Integer nrOfCookies = 10000;
        Integer nrOfCampaigns = 5;
        Integer nrOfProducts = 5;
        List<Double> probabilities = new ArrayList<>(5);
        probabilities.add(0.21);
        probabilities.add(0.87);
        probabilities.add(0.66);
        probabilities.add(0.43);
        probabilities.add(0.55);

        System.out.printf("Sending Messages...");
        Integer count = 0;

        while (count < 10000) {
            Integer rndCampaigns = random.nextInt(nrOfCampaigns);

            Long timestamp = System.currentTimeMillis();
            String cookie = String.format("cookie_%d", random.nextInt(nrOfCookies));
            String campaign = String.format("campaign_%d", rndCampaigns + 1);
            String product = String.format("product_%d", random.nextInt(nrOfProducts)) + 1;
            Boolean click =  Math.random() >= 1.0 - probabilities.get(rndCampaigns);
            ImpressionEvent event = new ImpressionEvent(cookie, campaign, product, click, timestamp);

            producer.send(new KeyedMessage<String, String>(context.getString(KAFKA_TOPIC), mapper.writeValueAsString(event)));

            count++;
        }

        System.out.printf("Sent %d messages\n", count);
        producer.close();
    }

    public static void main(String[] args) {
        try {
            String configFileLocation = args[0];

            Context context = new Context(configFileLocation);
            KafkaProducer.run(context);
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}