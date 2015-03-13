package com.forum.fiend.osp;

import java.util.ArrayList;

import android.util.Log;

import com.google.gson.internal.LinkedTreeMap;

public class CategoryParser {
	
	@SuppressWarnings("rawtypes")
	public static final ArrayList<Category> parseCategories(Object [] data,String subforum_id,String background) {
		ArrayList<Category> categories = new ArrayList<Category>();
		
		for(Object o:data) {
			if(o != null) {
				LinkedTreeMap map = (LinkedTreeMap) o;
				
				Category ca = new Category();
				ca.category_name = (String) map.get("forum_name");
				ca.subforum_id = subforum_id;
				ca.category_id = (String) map.get("forum_id");
				ca.categoryType = "S";
				ca.categoryColor = background;
				
				if(map.containsKey("logo_url")) {
					if(map.get("logo_url") != null) {
						ca.categoryIcon = (String) map.get("logo_url");
					}
				}
				
				if(map.containsKey("url")) {
					if(map.get("url") != null) {
						ca.category_URL = (String) map.get("url");
					}
				}
				
				if(map.get("is_subscribed") != null) {
					ca.isSubscribed = (Boolean) map.get("is_subscribed");
				}
				
				if(map.get("can_subscribe") != null) {
					ca.canSubscribe = (Boolean) map.get("can_subscribe");
				}
				
				if(map.get("new_post") != null) {
					ca.hasNewTopic = (Boolean) map.get("new_post");
				}
				
				Boolean subOnly = false;
				
				if(map.containsKey("sub_only")) {
					if(map.get("sub_only") != null) {
						subOnly = (Boolean) map.get("sub_only");
						ca.hasChildren = true;
						if(ca.hasChildren) {
							Log.e("Forum Fiend","aaa sub only on " + ca.category_id);
						}
					}
				}
				
				if(subOnly) {

					if(map.containsKey("child")) {

						if(map.get("child") != null) {

							ca.category_id = subforum_id + "###" + (String) map.get("forum_id");
							
							ArrayList childArray = (ArrayList) map.get("child");
							
							Object[] objArray = new Object[childArray.size()];
							
							int i = 0;
							
							for(Object childForum : childArray) {

								if(childForum != null) {
									LinkedTreeMap childMap = (LinkedTreeMap) childForum;
									objArray[i] = childMap;
								}
								i++;
							}
							
							ca.children = parseCategories(objArray,ca.category_id,background);

						}
					}
				}
				
				categories.add(ca);
			}
		}
		
		return categories;
	}
	
}
