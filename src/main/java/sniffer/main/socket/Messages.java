package sniffer.main.socket;

import java.util.LinkedList;

import sniffer.main.utils.Utils;

public class Messages {

	LinkedList<String> messages;
	
	public Messages() {
		messages = new LinkedList<>();
	}
	
	public synchronized void register(String message) {
		this.messages.add(message);
		notify();
	}
	
	public synchronized void consumeAll() {
		while(messages.size() == 0) {
			try {
				wait();
			} catch (InterruptedException e) {}
		}
		while(messages.size() > 0) {
			final String message = messages.removeFirst();
			Utils.getInstance().tryToGetImagesFromPacket(message);
		}
	}
}
