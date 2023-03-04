package marz.kafka.lean_workflow;


import org.apache.kafka.clients.consumer.ConsumerRecord;

@FunctionalInterface
public interface KafkaMessageHandler {

    void processMessage(String topicName, ConsumerRecord<String, String> message) throws Exception;
}