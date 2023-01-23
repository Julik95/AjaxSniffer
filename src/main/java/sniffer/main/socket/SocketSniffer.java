package sniffer.main.socket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import sniffer.main.utils.LogStyle;
import sniffer.main.utils.Utils;

public class SocketSniffer {
	
	
	 private volatile boolean stopped = false;
	 
	 public void acceptClients(Integer port) {
		 try (ServerSocket server = new ServerSocket(port)) {
			 Utils.getInstance().appendInfo(String.format("Server ready to accept connections on port %d", server.getLocalPort()), LogStyle.INFO);
		     final Socket client = server.accept();
		     Utils.getInstance().appendInfo(String.format("Client connected using remote port %d", client.getPort()), LogStyle.SUCCESS);
		     final Thread t = new Thread(() -> {
		    	 try {
		    		 try (InputStream clientIn = client.getInputStream()) {
		    			 try (OutputStream clientOut = client.getOutputStream()) {
		    				 echo(clientIn, clientOut);
		    			 }
		    		 }
		    	 } catch (IOException ioe) {
		    		 Utils.getInstance().doWhenExceptionOccurs(ioe, String.format("Something gone wrang: ", ioe.getMessage()));
		    		 stopped = true;
		    	 }
		      });
		      t.start();
		      while (!stopped) {
		        Thread.sleep(10);
		      }
		      Utils.getInstance().appendInfo("Client exited", LogStyle.WARN);

		 } catch (IOException e) {
			 Utils.getInstance().doWhenExceptionOccurs(e, String.format("Error during openning of Socket %s", e.getMessage()));
		} catch (InterruptedException e) {
			Utils.getInstance().doWhenExceptionOccurs(e, String.format("Excecution was interrupted %s", e.getMessage()));
		}
	 }
	 
	
	 private void echo(InputStream clientIn, OutputStream clientOut) throws IOException {
		 try (Scanner clientScan = new Scanner(clientIn)) {
			 while (!stopped) {
				 final String fromClient = clientScan.nextLine();
				 Utils.getInstance().tryToGetImagesFromPacket(fromClient);
			 }
		 }catch(Exception ex) {
			 Utils.getInstance().doWhenExceptionOccurs(ex, String.format("Error occured durind reading packet from client: ", ex.getMessage()));
			 stopped = true;
		 }
	    
	  }

	 public boolean isStopped() {
		 return stopped;
	 }

	 public void setStopped(boolean stopped) {
		 this.stopped = stopped;
	 }
	 
	 
	 
	 
}
