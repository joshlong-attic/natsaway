package org.resouer.cloudfoundry.integration;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class NatsServer {
	public static void main(String[] args) throws Exception {

		ApplicationContext context = new ClassPathXmlApplicationContext(
				"/org/resouer/cloudfoundry/integration/nats-server.xml");

		System.out.println("Started the server...");

	}
}
