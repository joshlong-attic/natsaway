package org.resouer.cloudfoundry.integration;

import java.io.UnsupportedEncodingException;

import org.springframework.integration.Message;
import org.springframework.integration.annotation.ServiceActivator;

public class NatsProxyService {
	private final static String AWAITING_MSG_PAYLOAD = "AWAITING_MSG_PAYLOAD";
	private final static String AWAITING_CONTROL_LINE = "AWAITING_CONTROL_LINE";
	private static String parseState = AWAITING_CONTROL_LINE;

	// TODO read the nats server to find more things:
	// no ssl, CTRL_C, CTRL_D
	@ServiceActivator
	public Object handleIncomingNatsMessage(Message<?> msgFromnats) {

		Object response = null;
		Object payload = msgFromnats.getPayload();

		String payloadStr = "";

		if (payload instanceof byte[]) {
			byte[] bytes = (byte[]) payload;
			try {
				payloadStr = new String(bytes, "UTF-8");
				System.out.println("From client:" + payloadStr);
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (AWAITING_MSG_PAYLOAD.equals(parseState)) {
				parseState = AWAITING_CONTROL_LINE;
				System.out.println("Payload recieved : " + payloadStr);
				// route to subscriber
				response = "+OK\r";
			} else {
				if (payloadStr.startsWith("PUB")) {
					parseState = AWAITING_MSG_PAYLOAD;
				} else if (payloadStr.startsWith("SUB")) {
					System.out.println("Sending SUBs ok");
					// do subscribe
					response = "+OK\r";
				} else if (payloadStr.equals("UNSUB")) {
					// do unsubscribe
					response = "+OK\r";
				} else if (payloadStr.equals("PING")) {
					System.out.println("Sending PONG");
					response = "PONG\r";
				} else if (payloadStr.equals("PONG")) {
					// Do nothing
				} else if (payloadStr.startsWith("CONNECT")) {
					// Do nothing
				} else if (payloadStr.startsWith("INFO")) {
					System.out.println("Sending server info");
					response = "INFO {\"server_id\":\"4eda3def6c772d087d01c0b7ae\",\"host\":\"0.0.0.0\",\"port\":10000,\"version\":\"0.4.28\",\"auth_required\":false,\"ssl_required\":false,\"max_payload\":1048576}";
				} else {
					System.out.println("Unkonwn:" + payloadStr);
				}
			}

		}

		return response;
	}
}
