package com.forum.fiend.osp;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

@SuppressLint("NewApi")
public class PostAdapter extends BaseAdapter {
	
	private ArrayList<Post> data;
    Context c;
    
    private boolean useShading = false;
	private boolean useOpenSans = false;
	private int fontSize = 20;
	private boolean currentAvatarSetting = false;
	private int page;
	
	private ForumFiendApp application;

	PostAdapter (ArrayList<Post> data, Context c,ForumFiendApp app,int pageNumber){
        this.data = data;
        this.c = c;
        application = app;
        page = pageNumber;
        
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
	public View getView(final int arg0, View arg1, ViewGroup arg2) {
		View v = arg1;
		if (v == null) {
			LayoutInflater vi = (LayoutInflater)c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			
			v = vi.inflate(R.layout.post, null);
			
		}
		
		Post po = data.get(arg0);
		
		v = ElementRenderer.renderPost(v,application,page,c,arg0,useOpenSans,useShading,po,fontSize,currentAvatarSetting);

		                   
		return v;
	}
	
	

}
