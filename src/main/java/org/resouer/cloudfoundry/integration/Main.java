package org.resouer.cloudfoundry.integration;

import nats.HandlerRegistration;
import nats.client.*;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.reflect.FieldUtils;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 *
 */
public class Main {



    static Nats nats() {
        return new NatsConnector().addHost("nats://localhost:4222").connect();
    }

    static private void receiveAMessage(String topic) throws Throwable {
        Nats n = nats();
        Subscription subscription = n.subscribe(topic);

        HandlerRegistration handlerRegistration = subscription.addMessageHandler(
                new MessageHandler() {
                    @Override
                    public void onMessage(Message message) {
                        System.out.println(ToStringBuilder.reflectionToString(message));
                    }
                }
        );

        System.out.println( "at the end of the line");

    }

    static private void sendAMessage(String topic, String payload) throws Throwable {
        Nats n = nats();
        n.publish(topic, payload);
    }

    static public void main(String args[]) throws Throwable {

        final String topic = "cats";
        receiveAMessage(topic);

        Thread.sleep(1000L);

        for (int i = 0; i < 100; i++)
            sendAMessage(topic, "Nihao");
        /*   Executor ex = Executors.newFixedThreadPool(1);
        ex.execute(new Runnable() {
            @Override
            public void run() {
                try {

                } catch (Throwable throwable) {
                    throw new RuntimeException(throwable);
                }
            }
        });*/


    }


}
