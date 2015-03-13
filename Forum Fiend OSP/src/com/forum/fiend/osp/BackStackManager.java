package com.forum.fiend.osp;

import java.util.ArrayList;

import android.os.Bundle;
import android.util.Log;

public class BackStackManager {
	
	private ArrayList<ArrayList<BackStackItem>> backstackArray;
	
	public BackStackManager() {
		clearAllStacks();
	}
	
	public void clearAllStacks() {
		backstackArray = new  ArrayList<ArrayList<BackStackItem>>();
	}
	
	public int createBackstack() {
		ArrayList<BackStackItem> freshBackstack = new ArrayList<BackStackItem>();
		backstackArray.add(freshBackstack);
		return backstackArray.size() - 1;
	}
	
	public int getBackStackSize(int backstackId) {
		return backstackArray.get(backstackId).size();
	}
	
	public void addToBackstack(int backstackId,int type,Bundle bundle) {
		Log.i("Forum Fiend","Backstack Adding Item " + type);
		BackStackItem item = new BackStackItem(type,bundle);
		backstackArray.get(backstackId).add(item);
	}
	
	public BackStackItem getActiveItem(int backstackId) {
		if(backstackArray.get(backstackId).size() == 0) {
			return null;
		}
		
		return backstackArray.get(backstackId).get(backstackArray.get(backstackId).size() - 1);
	}
	
	public BackStackItem getActiveItemAndRemove(int backstackId) {
		if(backstackArray.get(backstackId).size() == 0) {
			return null;
		}
		
		BackStackItem returnItem = backstackArray.get(backstackId).get(backstackArray.get(backstackId).size() - 1);
		backstackArray.get(backstackId).remove(backstackArray.get(backstackId).size() - 1);
		
		return returnItem;
	}
	
	public BackStackItem navigateToBase(int backstackId) {
		if(backstackArray.get(backstackId).size() == 0) {
			return null;
		}
		
		BackStackItem returnItem = backstackArray.get(backstackId).get(0);
		backstackArray.get(backstackId).clear();
		return returnItem;
	}
	
	public BackStackItem navigateBack(int backstackId) {
		
		if(backstackArray.get(backstackId).size() == 0) {
			return null;
		}
		
		backstackArray.get(backstackId).remove(backstackArray.get(backstackId).size() - 1);
		
		BackStackItem returnItem = backstackArray.get(backstackId).get(backstackArray.get(backstackId).size() - 1);
		
		backstackArray.get(backstackId).remove(backstackArray.get(backstackId).size() - 1);
		
		return returnItem;
	}
	
	public class BackStackItem {
		
		public static final int BACKSTACK_TYPE_FORUM = 1;
		public static final int BACKSTACK_TYPE_TOPIC = 2;
		public static final int BACKSTACK_TYPE_PROFILE = 3;
		public static final int BACKSTACK_TYPE_SETTINGS = 4;
		
		private int type;
		private Bundle params;
		
		public BackStackItem(int itemType,Bundle itemBundle) {
			type = itemType;
			params = itemBundle;
		}
		
		public int getType() {
			return type;
		}
		
		public Bundle getBundle() {
			return params;
		}
	}
}
