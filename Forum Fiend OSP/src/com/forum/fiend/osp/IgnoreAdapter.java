package com.forum.fiend.osp;

import java.util.ArrayList;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

@SuppressLint("NewApi")
public class IgnoreAdapter extends BaseAdapter {
	
	private ArrayList<IgnoreItem> data;
    Context c;
    private String theme;
    private ImageLoader imageLoader;
    
    @SuppressWarnings("deprecation")
	IgnoreAdapter (ArrayList<IgnoreItem> data, Context c){
        this.data = data;
        this.c = c;

		SharedPreferences app_preferences = c.getSharedPreferences("prefs", 0);
		String serverPrefix = "";
        String server_address = app_preferences.getString("server_address", c.getString(R.string.server_location));
        if(c.getString(R.string.server_location).contentEquals("0")) {
			serverPrefix = server_address + "_";
        }
        theme = Integer.toString(app_preferences.getInt(serverPrefix + "loggedThemeInt", Integer.parseInt(c.getString(R.string.default_theme))));
        
        imageLoader = ImageLoader.getInstance();
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder().cacheInMemory().cacheOnDisc().build();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(c).defaultDisplayImageOptions(defaultOptions).build();
        ImageLoader.getInstance().init(config);

        //imageLoader.displayImage(imageUrl, imageView);
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

	public View getView(final int arg0, View arg1, ViewGroup arg2) {
		View v = arg1;
		if (v == null)
		{
			LayoutInflater vi = (LayoutInflater)c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			
			v = vi.inflate(R.layout.ignore_item, null);
			
			
		}
		
		TextView iiUsername = (TextView)v.findViewById(R.id.ignore_item_username);
		TextView iiTimestamp = (TextView)v.findViewById(R.id.ignore_item_timestamp);
		ImageView iiAvatar = (ImageView)v.findViewById(R.id.ignore_item_avatar);
		
		IgnoreItem ii = data.get(arg0);
		
		iiUsername.setText(ii.ignoreItemUsername);
		iiTimestamp.setText("Ignored on " + ii.ignoreItemDate);
		
		if(theme.contentEquals("1") || theme.contentEquals("2") ||theme.contentEquals("3") || theme.contentEquals("6")){
			iiUsername.setTextColor(Color.parseColor("#ffffff"));
			iiTimestamp.setTextColor(Color.parseColor("#ffffff"));
        } else {
        	iiUsername.setTextColor(Color.parseColor("#333333"));
        	iiTimestamp.setTextColor(Color.parseColor("#333333"));
        }

		if(ii.ignoreItemAvatar.contains("http://")) {
			String imageUrl = ii.ignoreItemAvatar;
			imageLoader.displayImage(imageUrl, iiAvatar);
		} else {
			iiAvatar.setImageResource(R.drawable.no_avatar);
		}
	             
		return v;
	}

}
