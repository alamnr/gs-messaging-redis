package com.example.gsmessagingredis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

import java.util.Arrays;

@SpringBootApplication
public class GsMessagingRedisApplication {


    private static final Logger LOGGER = LoggerFactory.getLogger(GsMessagingRedisApplication.class);
    @Bean
    Receiver receiver(){
        return new Receiver();
    }

    @Bean
    MessageListenerAdapter  listenerAdapter(Receiver receiver){
        return new MessageListenerAdapter(receiver,"receiveMessage");
    }


    @Bean
    RedisMessageListenerContainer container(RedisConnectionFactory connectionFactory,
                                            MessageListenerAdapter listenerAdapter){
        RedisMessageListenerContainer container  = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(listenerAdapter, new PatternTopic("chat"));
        return container;
    }

    @Bean
    StringRedisTemplate  template(RedisConnectionFactory connectionFactory){
        return new StringRedisTemplate(connectionFactory);
    }

    public static void main(String[] args) throws InterruptedException {


        ApplicationContext ctx =  SpringApplication.run(GsMessagingRedisApplication.class, args);

        Arrays.stream(ctx.getBeanDefinitionNames()).forEach(bean-> System.out.println(bean));
        System.out.println("beans count = " + ctx.getBeanDefinitionCount());
        if(ctx.getBean(RedisConnectionFactory.class) instanceof RedisConnectionFactory){

            System.out.println("args = " + ctx.getBean(RedisConnectionFactory.class).getClass().getName());
        }

        StringRedisTemplate template = ctx.getBean(StringRedisTemplate.class);
        Receiver receiver = ctx.getBean(Receiver.class);

        while (receiver.getCount() == 0) {

            LOGGER.info("Sending message...");
            template.convertAndSend("chat", "Hello from Redis!");
            template.convertAndSend("chat", "Hello from Redis!");
            template.convertAndSend("chat", "Hello from Redis!");
            Thread.sleep(500L);
            System.out.println("message count = " + receiver.getCount());
        }

        System.exit(0);


    }

}
