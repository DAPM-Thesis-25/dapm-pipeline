package communication.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.handler.annotation.support.DefaultMessageHandlerMethodFactory;

@Configuration
public class MessageHandlerConfig {

    @Bean
    public DefaultMessageHandlerMethodFactory messageHandlerMethodFactory() {
        return new DefaultMessageHandlerMethodFactory();
    }
}

