package com.forum.fiend.osp;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

@SuppressLint("NewApi")
public class CategoryAdapter extends BaseAdapter {
	
	private ArrayList<Category> data;
    Context c;

    private boolean useShading = false;
	private boolean useOpenSans = false;
	private boolean currentAvatarSetting = false;
	
	@SuppressWarnings("unused")
	private int fontSize = 20;
	
	private ForumFiendApp application;

    CategoryAdapter (ArrayList<Category> data, Context c,ForumFiendApp app){
        this.data = data;
        this.c = c;
        application = app;
        
        if(c == null) {
        	return;
        }
		
		SharedPreferences app_preferences = c.getSharedPreferences("prefs", 0);

        useShading = app_preferences.getBoolean("use_shading", false);
		useOpenSans = app_preferences.getBoolean("use_opensans", false);
		fontSize = app_preferences.getInt("font_size", 16);
		currentAvatarSetting = app_preferences.getBoolean("show_images",true);
    }

	public int getCount() {
		// TODO Auto-generated method stub
		return data.size();
	}

	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return data.get(arg0);
	}

	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return arg0;
	}

	@SuppressLint("InflateParams")
	public View getView(int arg0, View arg1, ViewGroup arg2) {
		
		Category ca = data.get(arg0);
		
		View v = arg1;

		LayoutInflater vi = (LayoutInflater)c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		if(ca.categoryType.contentEquals("S")) {
			v = vi.inflate(R.layout.category, null);
		} else {
			v = vi.inflate(R.layout.thread, null);
		}

		
		v = ElementRenderer.renderCategory(v,application,c,useOpenSans,useShading,ca,currentAvatarSetting);
		
		
		                   
		return v;
	}
	
	public int dpToPx(int dp) {
	    DisplayMetrics displayMetrics = c.getResources().getDisplayMetrics();
	    int px = Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));       
	    return px;
	}

}
