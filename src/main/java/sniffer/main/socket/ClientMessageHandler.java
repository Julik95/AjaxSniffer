package sniffer.main.socket;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;

import sniffer.main.utils.Constants;
import sniffer.main.utils.LogStyle;
import sniffer.main.utils.Utils;

public class ClientMessageHandler extends Thread{
	
	private List<String> messages;
	private DataInputStream clientInputStream;
	private Socket client;
	private MessageConsumer messageConsumer;
	
	private boolean listeningNewMessages;
	
	
	public ClientMessageHandler(Socket client) {
		this.client = client;
		this.setName(Constants.MESSAGE_HANDLER_THREAD_NAME);
		this.setDaemon(true);
		try {
			this.clientInputStream = new DataInputStream(client.getInputStream());
			this.messages = new LinkedList<>();
			messageConsumer = new MessageConsumer(messages);
			listeningNewMessages = true;
			Utils.getInstance().appendInfo(String.format("New client connected using remote port %d", client.getPort()), LogStyle.SUCCESS);
		    Utils.getInstance().changePortLabelColorByLogStyle(LogStyle.SUCCESS);
		} catch (IOException e) {
			Utils.getInstance().doWhenExceptionOccurs(e, "IOException occured during client input stream initialization");
		}
		
		
	}
	
	
	public void run() {
		if(clientInputStream != null) {
			Utils.getInstance().appendInfo("Start handling client messages", LogStyle.INFO);
			messageConsumer.start();
			while(listeningNewMessages) {
				try {
					final String fromClient = clientInputStream.readUTF();
					synchronized(this) {
						messages.add(fromClient);
						notifyAll();
					}
				} catch (IOException e) {
					Utils.getInstance().doWhenExceptionOccurs(e, "IOException occured during reading new message from client socket");
				}
			}
			try {
				client.close();
				clientInputStream.close();
				messageConsumer.setConsume(false);
				this.messages.clear();
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
