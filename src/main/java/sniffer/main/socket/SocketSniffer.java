package sniffer.main.socket;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.NoSuchElementException;
import java.util.Scanner;

import sniffer.main.utils.LogStyle;
import sniffer.main.utils.Utils;

public class SocketSniffer {
	
	
	 private volatile boolean stoppedReading = false;
	 private volatile boolean stoppedListening = false;
	 
	 
	 public void acceptClients(Integer port, boolean alreadyListening) {
		 try(ServerSocket server = new ServerSocket(port)){
			 if(!alreadyListening) {
				 Utils.getInstance().appendInfo(String.format("Server ready to accept connections on port %d", server.getLocalPort()), LogStyle.INFO);
			 }
			 
		     final Socket client = server.accept();
		     Utils.getInstance().appendInfo(String.format("Client connected using remote port %d", client.getPort()), LogStyle.SUCCESS);
		     Utils.getInstance().changePortLabelColorByLogStyle(LogStyle.SUCCESS);
		     try (InputStream clientIn = client.getInputStream()) {
    			 echo(clientIn);
    		 }catch (IOException ioe) {
	    		 Utils.getInstance().doWhenExceptionOccurs(ioe, String.format("Something gone wrang: ", ioe.getMessage()));
	    		 Utils.getInstance().changePortLabelColorByLogStyle(LogStyle.ERROR);
	    		 stoppedReading = true;
	    	 }
		     while (!stoppedReading) {
		    	 Thread.sleep(1000);
		     }
		     if(!client.isClosed())
		    	 client.close();
	    	 if(!server.isClosed())
	    		 server.close();
		     if(!stoppedListening) {
		    	 stoppedReading = false;
		    	 acceptClients(port, true);
		     }
		     if(!alreadyListening) {
		    	 Utils.getInstance().appendInfo("Socket port has been closed", LogStyle.WARN);
		    	 Utils.getInstance().changePortLabelColorByLogStyle(LogStyle.ERROR);
		     }

		 } catch (IOException e) {
			 Utils.getInstance().doWhenExceptionOccurs(e, String.format("Error during openning of Socket %s", e.getMessage()));
		} catch (InterruptedException e) {
			Utils.getInstance().doWhenExceptionOccurs(e, String.format("Excecution was interrupted %s", e.getMessage()));
		}
	 }
	 
	
	 private void echo(InputStream clientIn) throws IOException {
		 try (Scanner clientScan = new Scanner(clientIn)) {
			 while (!stoppedReading) {
				 final String fromClient = clientScan.nextLine();
				 Thread executor = new Thread(() -> {
					 Utils.getInstance().tryToGetImagesFromPacket(fromClient); 
				 });
				 executor.setDaemon(true);
				 executor.start();
			 }
		 }catch(NoSuchElementException ex) {
			 Utils.getInstance().appendInfo("Client exited but socket port still listenning to new connections...", LogStyle.WARN);
			 Utils.getInstance().changePortLabelColorByLogStyle(LogStyle.WARN);
			 stoppedReading = true;
		 }catch(Exception ex) {
			 Utils.getInstance().doWhenExceptionOccurs(ex, String.format("Error occured durind reading packet from client: %s.", ex.getMessage()));
			 Utils.getInstance().changePortLabelColorByLogStyle(LogStyle.ERROR);
			 stoppedReading = true;
			 stoppedListening = true;
		 }
	    
	  }

	 public boolean isStoppedReading() {
		 return stoppedReading;
	 }

	 public void setStopped(boolean stopped) {
		 this.stoppedReading = stopped;
		 stoppedListening = stopped;
		 
	 }
	 
	 
	 
}
