package sniffer.main.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import sniffer.main.controller.MainSceneController;

public class Utils {
	
	private final static Utils INSTANCE = new Utils();
	private Utils() {}
	public static Utils getInstance() {
		return INSTANCE;
	}
	
	private MainSceneController controller;
	
	private Properties getLocalProperties(String path) {
		try {
			InputStream is = getClass().getResourceAsStream(path);
			Properties appProps = new Properties();
			appProps.load(is);
			return appProps;
		} catch (Exception ex) {
			doWhenExceptionOccurs(ex, String.format("Error occured with properties file: %s ", ex.getMessage()));
			return null;
		}
		
	}
	
	public Integer getSocketPort(String args[]) {
		Integer port = null;
		if(args != null && args.length == 2) {
			if(Constants.PORT_PARAM.equalsIgnoreCase(args[0])) {
				port = Utils.getInstance().parseToInt(args[1]);
				System.out.println(String.format("Port %d in use from params.", port));
			}else {
				System.out.println("Use parameter -p to specify the listen port.");
			}
		}else {
			Properties props = getLocalProperties(Constants.PROPERTIES_PATH);
			if(props != null && !StringUtils.isEmpty(props.getProperty("socketPort"))) {
				port = Utils.getInstance().parseToInt(props.getProperty("socketPort"));
				appendInfo(String.format("Port %d in use from properties.", port), LogStyle.INFO);
			}
		}
		return port;
	}
	
	public Integer getPortFromPropsFile(File file) {
		Integer port = null;
		try {
			InputStream is = new FileInputStream(file);
			Properties appProps = new Properties();
			appProps.load(is);
			if(appProps != null && !StringUtils.isEmpty(appProps.getProperty(Constants.SOCKER_PORT_PROP))) {
				port = Utils.getInstance().parseToInt(appProps.getProperty(Constants.SOCKER_PORT_PROP));
				appendInfo(String.format("Port %d in use from properties.", port), LogStyle.INFO);
			}
			
		} catch (FileNotFoundException ex) {
			doWhenExceptionOccurs(ex, String.format("Properties file not found %s ", ex.getMessage()));
			ex.printStackTrace();
		} catch (IOException ex) {
			doWhenExceptionOccurs(ex, String.format("Error occured %s ", ex.getMessage()));
			ex.printStackTrace();
		}
		return port;
	}
	
	public void tryToGetImagesFromPacket(String line){
		appendInfo("Recieved from client: "+line, LogStyle.INFO);
		Pattern urlPattern = Pattern.compile(Constants.URL_REGEXP, Pattern.CASE_INSENSITIVE);
		Matcher urlsMatcher = urlPattern.matcher(line);
		if(urlsMatcher.find()){
			String[] urls = urlsMatcher.group().split(",");
			for(String url: urls) {
				try {
					Pattern metas = Pattern.compile(Constants.META_DATA_REGEXP, Pattern.CASE_INSENSITIVE);
					Matcher metasMatcher = metas.matcher(line);
					if(metasMatcher.find()) {
						String imageSavedPath = downloadImageToLocal(new URL(url), metasMatcher.group());
						if(!StringUtils.isEmpty(imageSavedPath)) {
							controller.appendImageDownloadedInfo(String.format( "Image %s has been successfully saved!", imageSavedPath), imageSavedPath);
						}
					}else {
						appendInfo("Uknown customer..", LogStyle.WARN);
					}
					
				} catch (MalformedURLException ex) {
					doWhenExceptionOccurs(ex, String.format("Error occured during url(%s) parsing: %s", url, ex.getMessage()));
				} catch (IOException ex) {	
					doWhenExceptionOccurs(ex, String.format("Ununable to create image path: ",ex.getMessage()));
				}
			}
	    }
	}
	
	public void doWhenExceptionOccurs(Exception ex, String message) {
		if(controller != null) {
			controller.handleError(ex, message);
		}
	}
	
	public void setMainSceneControler(MainSceneController controller) {
		this.controller = controller;
	}
	
	public void appendInfo(String message, LogStyle logStyle) {
		controller.appendInfo(message, logStyle);
	}
	
	
	public void emptyImegesDir() {
		File dir = new File(Constants.IMEGES_DIR);
		recursionDirCleaning(dir.listFiles());
	}
	
	public void changePortLabelColorByLogStyle(LogStyle logStyle) {
		controller.applyStyletoPortNumLabel(logStyle);
	}
	
	private void recursionDirCleaning(File[] files) {
		for (File file : files) {
            if (file.isDirectory()) {
            	appendInfo(String.format("Directory: %s is going to be analyzed to remove old imeges.", Constants.IMEGES_DIR + file.getName()), LogStyle.INFO);
                recursionDirCleaning(file.listFiles());
            } else {
            	Date creationDate = getFileCreationDate(file);
            	if(creationDate != null && checkIfDateIsOld(creationDate)) {
            		if(file.delete())
						appendInfo(String.format("File: %s has been removed", file.getName()), LogStyle.WARN);
            	}
            }
        }
	}
	
	public Date getFileCreationDate(File file) {
		BasicFileAttributes attr;
		try {
		    attr = Files.readAttributes(Paths.get( file.getAbsolutePath()), BasicFileAttributes.class);
		    return new Date(attr.creationTime().toMillis());
	    } catch (IOException e) {
	    	doWhenExceptionOccurs(e, String.format("Ununable to get file creation date %s", e.getMessage()));
	    }
		return null;
	}
	
	private String downloadImageToLocal(URL url, String metas) throws IOException {
		if(url != null && !StringUtils.isEmpty(metas) && metas.contains("#") && metas.contains("|")) {
			String customerId = metas.substring(metas.indexOf("#")+1, metas.indexOf("|"));
			appendInfo(String.format("Start downloading image from %s for customer %s", url.toExternalForm(), customerId), LogStyle.INFO);
			Pattern imageNamePattern = Pattern.compile(Constants.IMAGE_NAME_REGEXP, Pattern.CASE_INSENSITIVE);
			Matcher imageNameMatcher = imageNamePattern.matcher(url.toExternalForm());
			if(imageNameMatcher.find()) {
				String path = Constants.IMEGES_DIR+customerId;
				Files.createDirectories(Paths.get(path));
				String imgName = imageNameMatcher.group(1);
				path = path + File.separatorChar+imgName;
				try(InputStream in = url.openStream()){
				    Files.copy(in, Paths.get(path));
				}catch(Exception ex) {
					doWhenExceptionOccurs(ex, String.format("Ununable to download an image: %s", url.toExternalForm()));
				}
				return path;
			}
		}
		return null;
		
	}
	
	private boolean checkIfDateIsOld(Date date) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		//c.add(Calendar.DATE, Constants.DAYS_TO_KEEP_IMAGE);
		c.add(Calendar.HOUR_OF_DAY, 4);
		return new Date().after(c.getTime());
	}
	
	
	private Integer parseToInt(String value) {
		Integer result = null;
		try {
			result = Integer.parseInt(value);
		}catch(NumberFormatException ex) {
			doWhenExceptionOccurs(ex, String.format("Value '%s' is not a valid port", value));
		}
		return result;
	}
	
	
}
