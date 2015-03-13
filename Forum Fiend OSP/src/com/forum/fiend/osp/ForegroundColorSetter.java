package com.forum.fiend.osp;

public class ForegroundColorSetter {
	
	public static String getForeground(String hexColor) {
		if(getForegroundDark(hexColor)) {
			return "#000000";
		} else {
			return "#ffffff";
		}
	}
	
	public static boolean getForegroundDark(String hexColor) {
		
		if(hexColor == null) {
			return false;
		}
		
		if(!hexColor.contains("#")) {
			return false;
		}
		
		if(hexColor.length() != 7) {
			return false;
		}
		
		int red = Integer.valueOf( hexColor.substring( 1, 3 ), 16 );
		int green = Integer.valueOf( hexColor.substring( 3, 5 ), 16 );
		int blue = Integer.valueOf( hexColor.substring( 5, 7 ), 16 );
		
		int totalColor = red + green + blue;
		
		if(totalColor > 382) {
			return true;
		} else {
			return false;
		}
	}
}
