package org.resouer.cloudfoundry.integration;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.springframework.integration.Message;
import org.springframework.integration.annotation.ServiceActivator;

/***
 *
 */
public class MessagePrintingServiceActivator {
    @ServiceActivator
    public void printOutNewMessagesArriving(Message<?> msg) throws Throwable {
        System.out.println(ToStringBuilder.reflectionToString(msg));
    }
}
