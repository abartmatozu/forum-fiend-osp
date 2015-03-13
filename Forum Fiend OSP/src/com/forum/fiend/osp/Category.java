package com.forum.fiend.osp;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.view.View;

@SuppressLint({ "NewApi", "NewApi" })
public class Category {
	public String category_description = "Category Description";
	public String category_name = "Category Name";
	public String category_id = "0";
	public String subforum_id = "0";
	public String category_lastupdate = "00-00-0000";
	public String category_lastthread = "Thread Name";
	public String thread_count = "0";
	public String view_count = "0";
	public View subforum_seperator;
	public String categoryModerator;
	public String categoryColor = "#000000";
	public String categoryIcon = "n/a";
	public String categoryMature = "N";
	public String categoryType = "C";
	public String categoryOnUnified = "Y";
	
	public boolean canSticky = false;
	public boolean canLock = false;
	public boolean canDelete = false;
	
	public boolean canSubscribe = false;
	public boolean isSubscribed = false;
	
	
	public boolean isLocked = false;
	
	public String category_URL = "n/a";
	
	public boolean hasNewTopic = false;
	
	public boolean hasChildren = false;
	
	public String topicSticky = "N";
	
	public ArrayList<Category> children;
}
