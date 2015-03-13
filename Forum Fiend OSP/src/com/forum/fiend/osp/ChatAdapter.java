package com.forum.fiend.osp;

import java.util.ArrayList;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

@SuppressLint("NewApi")
public class ChatAdapter extends BaseAdapter {
	
	private ArrayList<Chat> data;
    Context c;
    
    private boolean useShading = false;
	private boolean useOpenSans = false;
	//private int fontSize = 20;
	private boolean currentAvatarSetting = false;
	
	private ForumFiendApp application;

	ChatAdapter (ArrayList<Chat> data, Context c,ForumFiendApp app){
        this.data = data;
        this.c = c;
        application = app;
        
        if(c == null) {
        	return;
        }

		SharedPreferences app_preferences = c.getSharedPreferences("prefs", 0);


		useShading = app_preferences.getBoolean("use_shading", false);
		useOpenSans = app_preferences.getBoolean("use_opensans", false);
		//fontSize = app_preferences.getInt("font_size", 16);
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
		
		Chat ch = data.get(arg0);
		
		View v = arg1;

		LayoutInflater vi = (LayoutInflater)c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		if(ch.getDisplayname().trim().contentEquals(application.getSession().getServer().serverUserName.trim())) {
			v = vi.inflate(R.layout.chat_post_me, null);
		} else {
			v = vi.inflate(R.layout.chat_post, null);
		}            
		
		TextView author = (TextView)v.findViewById(R.id.chat_post_author);
		TextView timestamp = (TextView)v.findViewById(R.id.chat_post_timestamp);
		TextView post = (TextView)v.findViewById(R.id.chat_post_body);

		Typeface opensans = Typeface.createFromAsset(c.getAssets(), "fonts/opensans.ttf");
		
		LinearLayout ll_color_background = (LinearLayout) v.findViewById(R.id.chat_background);

		String textColor = c.getString(R.string.default_text_color);
		
		if(application.getSession().getServer().serverTextColor.contains("#")) {
			textColor = application.getSession().getServer().serverTextColor;
		}
		
		textColor = "#000000";
		
		String boxColor = c.getString(R.string.default_element_background);
		//String bgColor = c.getString(R.string.default_background);
		
		if(application.getSession().getServer().serverBoxColor != null) {
			boxColor = application.getSession().getServer().serverBoxColor;
		}
		
		/*
		if(application.getSession().getServer().serverBackground != null) {
			bgColor = application.getSession().getServer().serverBackground;
		}
		*/
		
		if(ch.getDisplaycolor().contains("#")) {
			boxColor = ch.getDisplaycolor().replace("#", "#33");
		}
		
		ImageView chat_ting = (ImageView)v.findViewById(R.id.chat_ting);
		
		if(boxColor.contains("#")) {
			ll_color_background.setBackgroundColor(Color.parseColor(boxColor));
			chat_ting.setColorFilter(Color.parseColor(boxColor.replace("#33", "#")));
		} else {
			ll_color_background.setBackground(null);
			chat_ting.setVisibility(View.GONE);
		}


		if(useOpenSans) {
			author.setTypeface(opensans);
			post.setTypeface(opensans);
			timestamp.setTypeface(opensans);
		}
		
		if(useShading) {
			author.setShadowLayer(2, 0, 0, Color.parseColor("#66000000"));
			post.setShadowLayer(2, 0, 0, Color.parseColor("#66000000"));
			timestamp.setShadowLayer(2, 0, 0, Color.parseColor("#66000000"));
		}

		ImageView avatar = (ImageView)v.findViewById(R.id.chat_avatar);
		
		ImageView chat_avatar_frame = (ImageView)v.findViewById(R.id.chat_avatar_frame);
		chat_avatar_frame.setColorFilter(Color.parseColor("#dddddd"));
		
		author.setText(ch.getDisplayname());
		timestamp.setText(ch.getTimestamp());
		post.setText(ch.getPostbody());
		
		author.setTextColor(Color.parseColor(textColor));
		timestamp.setTextColor(Color.parseColor(textColor));
		post.setTextColor(Color.parseColor(textColor));


        if(currentAvatarSetting) {
			if(ch.getDisplayavatar().contains("http://")) {
				String imageUrl = ch.getDisplayavatar();
				ImageLoader.getInstance().displayImage(imageUrl, avatar);
			} else {
				avatar.setImageResource(R.drawable.no_avatar);
			}
        } else {
        	avatar.setVisibility(View.GONE);
        	
        	chat_avatar_frame = (ImageView)v.findViewById(R.id.post_avatar_frame);
			chat_avatar_frame.setVisibility(View.GONE);
        }

		                   
		return v;
	}
	
	

}
