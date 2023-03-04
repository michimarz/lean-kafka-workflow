package marz.kafka.lean_workflow;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


public class PropertiesHelper {

    public static Properties getProperties() throws Exception {

        Properties props = null;
        //try to load the file config.properties
        try (InputStream input = SimpleProducer.class.getClassLoader().getResourceAsStream("config.properties")) {

            props = new Properties();

            if (input == null) {
                throw new Exception("Sorry, unable to find config.properties");
            }

            //load a properties file from class path, inside static method
            props.load(input);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        
        return props;
    }

}