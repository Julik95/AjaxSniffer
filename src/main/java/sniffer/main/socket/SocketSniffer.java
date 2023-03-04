package sniffer.main.socket;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import sniffer.main.utils.LogStyle;
import sniffer.main.utils.Utils;

public class SocketSniffer{
	
	private Integer port;
	private ServerSocket serverSocket;
	private ClientMessageHandler messageHandler;
	
	public SocketSniffer(Integer port) throws IOException{
		this.port = port;
	}
	
	public void acceptClients(){
		Socket client = null;
		try {
			Utils.getInstance().appendInfo(String.format("Server ready to accept new client on port %d", serverSocket.getLocalPort()), LogStyle.INFO);
			client = serverSocket.accept();
			messageHandler = new ClientMessageHandler(client);
			messageHandler.start();
		}catch(Exception ex) {
			Utils.getInstance().doWhenExceptionOccurs(ex, "Some Errore occured during accepting new clints");
		}
	}
	
	
	public void closeCurrentClient() {
		if(messageHandler != null) {
			messageHandler.setListeningNewMessages(false);
		}
	}
	
	
	public void closeServerSocket() {
		closeCurrentClient();
		try {
			serverSocket.close();
		} catch (IOException e) {
			Utils.getInstance().doWhenExceptionOccurs(e, "IOException occured during closing server socket");
		}
	}
	 
	 
	 
}
