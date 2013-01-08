package org.resouer.cloudfoundry.integration;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.springframework.integration.Message;
import org.springframework.integration.annotation.ServiceActivator;

public class ErrorHandler {
	@ServiceActivator
	public void handleErrorMessages(Message<?> msg) throws Throwable {
		System.out.println(ToStringBuilder.reflectionToString(msg));
	}
}
