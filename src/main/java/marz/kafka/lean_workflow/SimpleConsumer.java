package marz.kafka.lean_workflow;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.log4j.Logger;

import java.util.*;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;


class SimpleConsumer extends AbstractSimpleKafka{

    private final int TIME_OUT_MS = 5000;
    private KafkaConsumer<String, String> kafkaConsumer = null;
    private final AtomicBoolean closed = new AtomicBoolean(false);


    static Logger log = Logger.getLogger(SimpleConsumer.class.getName());


    public SimpleConsumer() throws Exception {
    }


    void run(String topicName, KafkaMessageHandler callback, Integer numberOfRecords) throws Exception {
        Properties props = PropertiesHelper.getProperties();
        //See if the number of records is provided
        Optional<Integer> recs = Optional.ofNullable(numberOfRecords);

        //adjust the number of records to get accordingly
        Integer numOfRecs = recs.orElseGet(() -> Integer.parseInt(props.getProperty("max.poll.records")));
        props.setProperty("max.poll.records", String.valueOf(numOfRecs));

        // create the consumer
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);

        //make the consumer available for graceful shutdown
        setKafkaConsumer(consumer);
        consumer.assign(Collections.singleton(new TopicPartition(topicName, 0)));

        int recNum = numOfRecs;
        while (recNum > 0) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(TIME_OUT_MS));
            recNum = records.count();
            if (recNum == 0) {
                log.info(MessageHelper.getSimpleJSONObject("No records retrieved"));
                break;
            }

            for (ConsumerRecord<String, String> record : records) {
                callback.processMessage(topicName, record);
                recNum--;
            }
        }

        consumer.close();
    }

    private void close() throws Exception {
        if (this.getKafkaConsumer() == null){
            log.info(MessageHelper.getSimpleJSONObject("The internal consumer is NULL"));
            return;
        }
        log.info(MessageHelper.getSimpleJSONObject("Closing consumer"));
        if( this.getKafkaConsumer() != null) this.getKafkaConsumer().close();
    }


    public void runAlways(String topicName, KafkaMessageHandler callback) throws Exception {
        Properties props = PropertiesHelper.getProperties();
        //make the consumer available for graceful shutdown
        setKafkaConsumer(new KafkaConsumer<>(props));

        //keep running forever or until shutdown() is called from another thread.
        try {
            getKafkaConsumer().subscribe(List.of(topicName));
            while (!closed.get()) {
                ConsumerRecords<String, String> records =
                        getKafkaConsumer().poll(Duration.ofMillis(TIME_OUT_MS));
                if (records.count() == 0) {
                    log.info(MessageHelper.getSimpleJSONObject("No records retrieved"));
                }

                for (ConsumerRecord<String, String> record : records) {
                    callback.processMessage(topicName, record);
                }
            }
        } catch (WakeupException e) {
            // Ignore exception if closing
            if (!closed.get()) throw e;
        }
    }

    public void shutdown() throws Exception {
        closed.set(true);
        log.info(MessageHelper.getSimpleJSONObject("Shutting down consumer"));
        getKafkaConsumer().wakeup();
    }

    public KafkaConsumer<String, String> getKafkaConsumer() {
        return kafkaConsumer;
    }

    public void setKafkaConsumer(KafkaConsumer<String, String> kafkaConsumer) {
        this.kafkaConsumer = kafkaConsumer;
    }
}