package com.forum.fiend.osp;

import java.util.ArrayList;

import android.view.View;

public class Post
{
	public String post_tagline = "tagline";
	public String post_author = "Author";
	public String post_body = "Post body goes here!";
	public String post_avatar = "n/a";
	
	public String post_id = "0";
	public String category_id = "0";
	public String subforum_id = "0";
	public String thread_id = "0";
	public String post_author_id = "0";
	public String post_timestamp = "00-00-0000";
	public String post_color = "#000000";
	public String post_author_level = "0";
	public String post_picture = "0";
	public String post_parent = "0";
	public View subforum_seperator;
	public String categoryModerator = "0";
	public String attachmentExtension = "jpg";
	
	public boolean userOnline = false;
	public boolean userBanned = false;
	public boolean canBan = false;
	public boolean canDelete = false;
	public boolean canEdit = false;
	public boolean canThank = false;
	public boolean canLike = false;
	
	public int thanksCount = 0;
	public int likeCount = 0;
	
	public ArrayList<PostAttachment> attachmentList;
	
	public Post() {
		attachmentList = new ArrayList<PostAttachment>();
	}
	


}
