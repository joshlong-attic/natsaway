package org.resouer.cloudfoundry.integration;


import nats.client.Message;
import nats.client.MessageHandler;
import nats.client.Nats;
import nats.client.Subscription;
import org.springframework.integration.endpoint.MessageProducerSupport;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * todo add a MessageConverter strategy
 *
 * @author Lei Zhang
 * @author Josh Long
 */
public class NatsInboundChannelAdapter extends MessageProducerSupport {

    private Nats nats;
    private String topic;
    private int maxMessages = -1;
    private String queueGroup;
    private Subscription subscription;

    public void setQueueGroup(String queueGroup) {
        this.queueGroup = queueGroup;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public void setMaxMessages(int maxMessages) {
        this.maxMessages = maxMessages;
    }

    public void setNats(Nats n) {
        this.nats = n;
    }

    protected Subscription buildSubscription() {
        Assert.hasText(this.topic, "you must provide a valid 'topic' value");
        boolean hasMaxMessages = this.maxMessages > 0;
        boolean hasQueueGroup = StringUtils.hasText(this.queueGroup);
        Subscription subscription;
        if (!hasMaxMessages && !hasQueueGroup) {
            subscription = nats.subscribe(this.topic);
        } else if (hasMaxMessages && hasQueueGroup) {
            subscription = nats.subscribe(this.topic, this.queueGroup, this.maxMessages);
        } else if (hasMaxMessages && !hasQueueGroup) {
            subscription = nats.subscribe(this.topic, maxMessages);
        } else {
            subscription = nats.subscribe(this.topic, queueGroup);
        }
        return subscription;
    }


    @Override
    protected void doStart() {
        Assert.notNull(this.nats, "nats can't be null");
        Subscription s = buildSubscription();
        s.addMessageHandler(new MessageHandler() {
            @Override
            public void onMessage(Message message) {

                org.springframework.integration.Message<Message> msgForSpringIntegration =
                        MessageBuilder.withPayload(message)
                                .setHeader(NatsMessageHeaders.REPLY_TO, message.getReplyTo())
                                .setHeader(NatsMessageHeaders.SUBJECT, message.getSubject())
                                .setHeader(NatsMessageHeaders.SUBSCRIPTION, message.getSubject())
                                .build();

                sendMessage(msgForSpringIntegration);
            }
        });
        this.subscription = s;
    }

    @Override
    protected void doStop() {
        if (this.nats.getConnectionStatus().isConnected() && this.nats.getConnectionStatus().isServerReady()) {
            this.nats.close();
        }
    }

}
