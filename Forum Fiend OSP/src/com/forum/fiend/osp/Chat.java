package com.forum.fiend.osp;

public class Chat {
	private String postid;
	private String chatid;
	private String timestamp;
	private String displayname;
	private String displayavatar;
	private String postbody;
	private String displaycolor;
	
	public void setPostid(String value) {
		postid = value;
	}
	
	public void setChatid(String value) {
		chatid = value;
	}
	
	public void setTimestamp(String value) {
		timestamp = value;
	}
	
	public void setDisplayname(String value) {
		displayname = value;
	}
	
	public void setDisplayavatar(String value) {
		displayavatar = value;
	}
	
	public void setPostbody(String value) {
		postbody = value;
	}
	
	public void setDisplaycolor(String value) {
		displaycolor = value;
	}
	
	public String getPostid() {
		return postid;
	}
	
	public String getChatid() {
		return chatid;
	}
	
	public String getTimestamp() {
		return timestamp;
	}
	
	public String getDisplayname() {
		return displayname;
	}
	
	public String getDisplayavatar() {
		return displayavatar;
	}
	
	public String getPostbody() {
		return postbody;
	}
	
	public String getDisplaycolor() {
		return displaycolor;
	}
}
