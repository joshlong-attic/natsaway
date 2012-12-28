package org.resouer.cloudfoundry.integration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class NatsServer3 {

	private final static String AWAITING_MSG_PAYLOAD = "AWAITING_MSG_PAYLOAD";
	private final static String AWAITING_CONTROL_LINE = "AWAITING_CONTROL_LINE";
	private static String parseState = AWAITING_CONTROL_LINE;

	public static void main(String[] args) throws IOException {
		ServerSocket serverSocket = null;

		try {
			serverSocket = new ServerSocket(10000);
		} catch (IOException e) {
			System.err.println("Could not listen on port: 10000.");
			System.exit(1);
		}

		Socket clientSocket = null;
		System.out.println("Waiting for connection.....");

		try {
			clientSocket = serverSocket.accept();
		} catch (IOException e) {
			System.err.println("Accept failed.");
			System.exit(1);
		}

		System.out.println("Connection successful. Waiting for input.....");

		PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
		BufferedReader in = new BufferedReader(new InputStreamReader(
				clientSocket.getInputStream()));

		// NOTE: whenever connection established, send info like below:
		// out.println("INFO {\"server_id\":\"4eda3def6c772d087d01c0b7ae\",\"host\":\"0.0.0.0\",\"port\":10000,\"version\":\"0.4.28\",\"auth_required\":false,\"ssl_required\":false,\"max_payload\":1048576}");
		String inputLine;

		// TODO we need a queue to send response and a buffer to receive
		// requests.
		while ((inputLine = in.readLine()) != null) {
			System.out.println("From client: " + inputLine);

			if (AWAITING_MSG_PAYLOAD.equals(parseState)) {
				parseState = AWAITING_CONTROL_LINE;
				System.out.println("Payload recieved : " + inputLine);
				// route to subscriber
				out.println("+OK\r");
			} else {
				if (inputLine.startsWith("PUB")) {
					parseState = AWAITING_MSG_PAYLOAD;
				} else if (inputLine.startsWith("SUB")) {
					System.out.println("Sending SUBs ok");
					// do subscribe
					out.println("+OK\r");
				} else if (inputLine.equals("UNSUB")) {
					// do unsubscribe
					out.println("+OK\r");
				} else if (inputLine.equals("PING")) {
					System.out.println("Sending PONG");
					out.println("PONG\r");
				} else if (inputLine.equals("PONG")) {
					// Do nothing
				} else if (inputLine.startsWith("CONNECT")) {
					// Do nothing
				} else if (inputLine.startsWith("INFO")) {
					System.out.println("Sending server info");
					out.println("INFO {\"server_id\":\"4eda3def6c772d087d01c0b7ae\",\"host\":\"0.0.0.0\",\"port\":10000,\"version\":\"0.4.28\",\"auth_required\":false,\"ssl_required\":false,\"max_payload\":1048576}");
				} else {
					System.out.println("Unkonw:" + inputLine);
				}
			}
		}

		out.close();
		in.close();
		clientSocket.close();
		serverSocket.close();
	}
}
