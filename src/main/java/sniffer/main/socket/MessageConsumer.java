package sniffer.main.socket;

import sniffer.main.utils.Constants;
import sniffer.main.utils.LogStyle;
import sniffer.main.utils.Utils;

public class MessageConsumer extends Thread{
	
	private Messages messages;
	private boolean consume;
	
	public MessageConsumer(Messages messages) {
		this.messages = messages;
		this.setDaemon(true);
		this.setName(Constants.MESSAGE_CONSUMER_THREAD_NAME);
		consume = true;
	}
	
	public void run() {
		Utils.getInstance().appendInfo("Start handling client messages", LogStyle.INFO);
		while(consume) {
			messages.consumeAll();
		}
	}
	
	public void setConsume(boolean consume) {
		this.consume = consume;
	}

}
