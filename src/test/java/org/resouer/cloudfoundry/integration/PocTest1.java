package org.resouer.cloudfoundry.integration;

import java.util.ArrayList;
import java.util.Arrays;

import javax.inject.Inject;

import nats.client.spring.NatsFactoryBean;
import nats.client.spring.SubscriptionConfig;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.integration.MessageChannel;

/**
 *
 */
public class PocTest1 {

	@Configuration
	@ImportResource("/org/resouer/cloudfoundry/integration/poc1.xml")
	public static class NatsIntegrationConfiguration {

		private final String topic = "cats";

		private final String hostUri = "nats://localhost:4222";

		@Inject
		@Qualifier("inbound-nats-messages")
		private MessageChannel messageChannel;

		@Bean
		public NatsFactoryBean nats() {
			NatsFactoryBean natsFactoryBean = new NatsFactoryBean();
			natsFactoryBean.setAutoReconnect(true);
			natsFactoryBean
					.setSubscriptions(new ArrayList<SubscriptionConfig>());
			natsFactoryBean.setHostUris(Arrays.asList(hostUri));
			return natsFactoryBean;
		}

		@Bean
		public MessagePrintingServiceActivator messagePrintingServiceActivator() {
			return new MessagePrintingServiceActivator();
		}

		@Bean
		public NatsInboundChannelAdapter natsInboundChannelAdapter2()
				throws Throwable {
			NatsInboundChannelAdapter channelAdapter = new NatsInboundChannelAdapter();
			channelAdapter.setNats(nats().getObject());
			channelAdapter.setOutputChannel(this.messageChannel);
			channelAdapter.setTopic(this.topic);
			return channelAdapter;
		}

	}

	public static void main(String args[]) throws Throwable {

		AnnotationConfigApplicationContext ac = new AnnotationConfigApplicationContext(
				NatsIntegrationConfiguration.class);

	}
}
