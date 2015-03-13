package com.forum.fiend.osp;

import android.content.Context;
import android.content.SharedPreferences;

import com.nostra13.universalimageloader.core.ImageLoader;

public class CacheNuker {
	
	public static void NukeCache(Context c) {
		SharedPreferences app_preferences = c.getSharedPreferences("prefs", 0);
		SharedPreferences.Editor editor = app_preferences.edit();
		editor.clear();
		editor.commit();
		//TrimCache trimmer = new TrimCache(c);
		//trimmer.trim();
		ImageLoader imageLoader = ImageLoader.getInstance();
		imageLoader.clearDiskCache();
		imageLoader.clearMemoryCache();
	}
	
}
