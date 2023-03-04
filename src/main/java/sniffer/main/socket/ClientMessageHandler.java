package sniffer.main.socket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Scanner;

import sniffer.main.utils.Constants;
import sniffer.main.utils.LogStyle;
import sniffer.main.utils.Utils;

public class ClientMessageHandler extends Thread{
	
	private Messages messages;
	private BufferedReader clientScanner;
	private Socket client;
	private MessageConsumer messageConsumer;
	
	private volatile boolean listeningNewMessages;
	
	
	public ClientMessageHandler(Socket client) {
		this.client = client;
		this.setName(Constants.MESSAGE_HANDLER_THREAD_NAME);
		this.setDaemon(true);
		try {
			this.clientScanner = new BufferedReader(new InputStreamReader(client.getInputStream()));
			this.messages = new Messages();
			messageConsumer = new MessageConsumer(messages);
			listeningNewMessages = true;
			Utils.getInstance().appendInfo(String.format("Client connected using remote port %d", client.getPort()), LogStyle.SUCCESS);
		    Utils.getInstance().changePortLabelColorByLogStyle(LogStyle.SUCCESS);
		} catch (IOException e) {
			Utils.getInstance().doWhenExceptionOccurs(e, "IOException occured during client input stream initialization");
		}
		
		
	}
	
	
	public void run() {
		if(clientScanner != null) {
			messageConsumer.start();
			while(listeningNewMessages) {
				try {
					messages.register(clientScanner.readLine());
				} catch (IOException e) {
					Utils.getInstance().appendInfo("Input stream has been interrupted", LogStyle.WARN);
				}
			}
			try {
				client.close();
				clientScanner.close();
				messageConsumer.setConsume(false);
				notifyAll();
				Utils.getInstance().appendInfo("Client has been successefully terminated", LogStyle.SUCCESS);
			} catch (IOException e) {
				Utils.getInstance().doWhenExceptionOccurs(e, "IOException occured during closing client socket");
			}
		}else {
			Utils.getInstance().appendInfo("There was an attempt to start client message handler but client input stream is null", LogStyle.ERROR);
		}
	}
	
	public void setListeningNewMessages(boolean listeningNewMessages) {
		this.listeningNewMessages = listeningNewMessages;
		
	}
}
