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

//    nats.publish("foo", "Hello world!");


    //    Nats nats = new NatsConnector().addHost("nats://localhost:4222").connect();
//
//// Simple subscriber
//    nats.subscribe("foo").addMessageHandler(new MessageHandler() {
//        @Override
//        public void onMessage(Message message) {
//            System.out.println("Received: " + message);
//        }
//    });
//
//// Simple publisher
    private static ThreadLocal<ChannelFuture> currentWriteOperationReturnValue =
            new ThreadLocal<ChannelFuture>();

    static Nats nats() {
        return new NatsConnector().addHost("nats://localhost:4222").connect();
    }

    static Nats provideSmartChannel(Nats nats, final ThreadLocal<ChannelFuture> futureForInvocationOfWriteMethod) throws Throwable {


        String fieldName = "channel";


        Class<?> classOfImpl = nats.getClass() ;
        Field fieldForChannel =classOfImpl.getDeclaredField( fieldName);

        boolean isPrivate = fieldForChannel.isAccessible() ;
        fieldForChannel.setAccessible( true );

        Object obj = fieldForChannel.get(nats);
        assert obj instanceof Channel : "the channel field in the class should be an instance of a Netty channel";
        Channel channel = (Channel) obj;


        ProxyFactory proxyFactory = new ProxyFactory();
        proxyFactory.setTarget(channel);
        proxyFactory.addAdvice(new MethodInterceptor() {
            @Override
            public Object invoke(MethodInvocation invocation) throws Throwable {
                Method m = invocation.getMethod();
                boolean isOurWriteMethod = m.getName().equals("write") && m.getParameterTypes().length == 1;

                if (isOurWriteMethod) {
                    Object returnValue = invocation.proceed();
                    assert returnValue instanceof ChannelFuture : "the returned value should be an instance of ChannelFuture";
                    ChannelFuture channelFuture = (ChannelFuture) returnValue;
                    futureForInvocationOfWriteMethod.set(channelFuture);
                    return returnValue;
                } else {
                    // invoke the methods on the channel normally
                    return invocation.proceed();
                }
            }
        });

        for(  Class<?> interfaceForChannel:  channel  .getClass().getInterfaces())
        proxyFactory.addInterface(interfaceForChannel);

        Channel improvedChannel = (Channel) proxyFactory.getProxy();

        fieldForChannel.set(nats, improvedChannel);

//        FieldUtils.writeDeclaredField(nats, fieldName, improvedChannel);


        // remember to restore permissions
        fieldForChannel.setAccessible( isPrivate);

        return nats;
    }

    static private void receiveAMessage(String topic) throws Throwable {
        Nats n = nats();

        provideSmartChannel(n, currentWriteOperationReturnValue);


        Subscription subscription = n.subscribe(topic);

        ChannelFuture channelFuture = currentWriteOperationReturnValue.get();

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
