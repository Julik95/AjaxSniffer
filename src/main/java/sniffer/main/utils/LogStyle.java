package sniffer.main.utils;

public enum LogStyle {

	NONE("none"),
	SUCCESS("success"),
	ERROR("error"),
	INFO("info"),
	WARN("warn");
	
	private String style;
	
	LogStyle(String style) {
		this.style = style;
	}
	
	public String getStyle() {
		return style;
	}
}
