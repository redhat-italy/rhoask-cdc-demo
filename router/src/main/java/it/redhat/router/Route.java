package it.redhat.router;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.kafka.KafkaConstants;
import org.apache.camel.support.builder.Namespaces;
import org.w3c.dom.NodeList;

import java.net.ConnectException;

public class Route extends RouteBuilder {
    @Override
    public void configure() throws Exception {

        from("kafka:{{kafka.source.topic.name}}")
        .routeId("From.{{kafka.source.topic.name}}2Delivery")
        .log("Received Message From: {{kafka.source.topic.name}}  ${body}")

        .choice()
        .when(body().contains("pickup"))
            .log("pickup send to pickup")
            .to("kafka:{{kafka.sync-pickup.topic.name}}")
        .when(body().contains("shipment"))
            .log("shipment send to distributor")
            .to("kafka:{{kafka.sync-shipment.topic.name}}")
        .otherwise()
            .log("Not Found");


        from("rest:get:pickup")
                .setBody(constant("{  \"type\": \"fruits\",  \"size\": \"small\",  \"delivery\": \"pickup\"}\n"))          // Message to send
                //.setHeader(KafkaConstants.KEY, constant("router")) // Key of the message
                .to("kafka:{{kafka.source.topic.name}}");
        from("rest:get:ship")
                .setBody(constant("{  \"type\": \"fruits\",  \"size\": \"small\",  \"delivery\": \"shipment\"}\n"))          // Message to send
                //.setHeader(KafkaConstants.KEY, constant("router")) // Key of the message
                .to("kafka:{{kafka.source.topic.name}}");
    }
}
