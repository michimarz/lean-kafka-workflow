package marz.kafka.lean_workflow;


import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.json.simple.JSONObject;

import java.util.Locale;
import java.util.logging.Logger;


public class LeanKafkaWorkflow {
    private static final int NUM_OF_RECORD = 10;

    private static class ApplicationMessageHandlerImpl implements KafkaMessageHandler {

        static Logger log = Logger.getLogger(ApplicationMessageHandlerImpl.class.getName());

        @Override
        public void processMessage(String topicName, ConsumerRecord<String, String> message) throws Exception {
            String source = KafkaMessageHandlerImpl.class.getName();
            JSONObject obj = MessageHelper.getMessageLogEntryJSON(source, topicName,message.key(),message.value());
            System.out.println(obj.toJSONString());
            log.info(obj.toJSONString());
        }
    }

    public static void main(String[] args) throws Exception {
        String errorStr = "ERROR: You need to declare the first parameter as Producer or Consumer, " +
                "the second parameter is the topic name";

        if (args.length != 2){
            System.out.println(errorStr);
            return;
        }

        String mode = args[0];
        String topicName = args[1];
        switch(mode.toLowerCase(Locale.ROOT)) {
            case "producer":
                System.out.println("Starting the Producer\n");
                new SimpleProducer().runAlways(topicName, new ApplicationMessageHandlerImpl());
                break;
            case "consumer":
                System.out.println("Starting the Consumer\n");
                new SimpleConsumer().runAlways(topicName, new ApplicationMessageHandlerImpl() );
                break;
            default:
                System.out.println(errorStr);
        }


    }

}

