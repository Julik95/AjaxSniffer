package sniffer.main.socket;

import java.util.List;

import sniffer.main.utils.Constants;
import sniffer.main.utils.Utils;

public class MessageConsumer extends Thread{
	
	private List<String> messages;
	private boolean consume;
	
	public MessageConsumer(List<String> packets) {
		this.messages = packets;
		this.setDaemon(true);
		this.setName(Constants.MESSAGE_CONSUMER_THREAD_NAME);
		consume = true;
	}
	
	public void run() {
		try {
			consume();
		} catch (InterruptedException e) {
			Utils.getInstance().doWhenExceptionOccurs(e, "Interrupt occured in the consumer thread");
			e.printStackTrace();
		}
	}
	
	
	private synchronized void consume() throws InterruptedException {
		while(consume) {
			while(messages.size() == 0 && consume)
				wait();
			
			while(messages.size() > 0) {
				final String message = messages.remove(0);
				Utils.getInstance().tryToGetImagesFromPacket(message);
			}
		}
	}
	
	public void setConsume(boolean consume) {
		this.consume = consume;
	}

}
