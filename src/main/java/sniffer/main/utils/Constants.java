package sniffer.main.utils;

import java.io.File;

public interface Constants {

	final String PORT_PARAM = "-p";
	final String PROPERTIES_PATH = "/settings/clientSettings.properties";
	final String URL_REGEXP = "(http|ftp|https):\\/\\/([\\w_-]+(?:(?:\\.[\\w_-]+)+))([\\w.,@?^=%&:\\/~+#-]*[\\w@?^=%&\\/~+#-])";
	final String META_DATA_REGEXP = "\\[#([0-9A-Za-z|\\s])+\\]";
	final String IMAGE_NAME_REGEXP = "\\/images\\/(.+)\\?";
	final String IMAGE_DTAE_REGEXP = "_t_([0-9]{2}\\-[0-9]{2}\\-[0-9]{4}_[0-9]{2}\\-[0-9]{2}\\-[0-9]{2})";
	final String IMEGES_DIR = System.getProperty("user.home") + File.separatorChar + "Ajax_immagini"+File.separatorChar;
	final String SOCKER_PORT_PROP = "socketPort";
	final String APP_TITLE = "Scarica immagini";
	final String DATE_FORMAT = "dd-MM-yyyy_HH-mm-ss";
	final Integer DAYS_TO_KEEP_IMAGE = 10;
	
	final String MESSAGE_HANDLER_THREAD_NAME = "MessageHandlerThread";
	final String MESSAGE_CONSUMER_THREAD_NAME = "MessageConsumerThread";
}
