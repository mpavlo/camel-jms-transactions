import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.RedeliveryPolicy;
import org.apache.activemq.camel.component.ActiveMQComponent;
import org.apache.camel.CamelContext;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jms.JmsComponent;
import org.apache.camel.component.jms.JmsConfiguration;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.SimpleRegistry;
import org.springframework.jms.connection.JmsTransactionManager;

public class JMSTest{
    public static void main(String[] args) throws Exception {
        ActiveMQConnectionFactory jmsConnectionFactory =
                new ActiveMQConnectionFactory(
"tcp://localhost:61616?jms.redeliveryPolicy.maximumRedeliveries=-1&jms.redeliveryPolicy.useExponentialBackOff=true");
        JmsTransactionManager jmsTransactionManager = new JmsTransactionManager(jmsConnectionFactory);

        SimpleRegistry registry = new SimpleRegistry();
        registry.put("transactionManager", jmsTransactionManager);

        CamelContext context = new DefaultCamelContext(registry);
        ActiveMQComponent activeMQComponent = new ActiveMQComponent();
        activeMQComponent.setConnectionFactory(jmsConnectionFactory);
        activeMQComponent.setTransactionManager(jmsTransactionManager);

        context.addComponent("activemq", activeMQComponent);

        context.addRoutes(new RouteBuilder() {
            public void configure() throws Exception {
                from("activemq:queue:test?transacted=true&acknowledgementModeName=CLIENT_ACKNOWLEDGE")
                        .process(exchange -> exchange.getIn().getBody())
                        .log("test123")
                        .process(exchange -> {throw new Exception("Hallo Welt");})
                        .to("activemq:queue:test2");

            }
        });

        context.start();
        Thread.sleep(10000);
        context.stop();
    }
}
