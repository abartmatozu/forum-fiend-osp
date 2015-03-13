package com.forum.fiend.osp;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.util.TypedValue;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class ConversationParticipant extends ImageView {
	
	private ImageLoader imageLoader;
	
	private String username;
	private String userid;
	private String userColor;
	private String userStatus;
	
	@SuppressWarnings("deprecation")
	public ConversationParticipant(Context context) {
		super(context);
		
		imageLoader = ImageLoader.getInstance();
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder().cacheInMemory().cacheOnDisc().build();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context).defaultDisplayImageOptions(defaultOptions).build();
        ImageLoader.getInstance().init(config);
        
        Resources r = getResources();
        
        int margin = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 6, r.getDisplayMetrics());
        int size = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48, r.getDisplayMetrics());
        
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(size, size);
        lp.setMargins(margin, margin, margin, margin);
        setLayoutParams(lp);
        
        setScaleType(ScaleType.CENTER_CROP);
	}
	
	public void setUsername(String username) {
		this.username = username;
	}
	
	public void setUserid(String userid) {
		this.userid = userid;
	}
	
	public void setUserColor(String color) {
		this.userColor = color;
	}
	
	@SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	public void setUserStatus(String status) {
		this.userStatus = status;
		
		if(status.contentEquals("O")) {
			if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
				this.setAlpha(100);
			} else {
				this.setImageAlpha(100);
			}
		}
	}
	
	public String getUsername() {
		return username;
	}
	
	public String getUserId() {
		return userid;
	}
	
	public String getUserColor() {
		return userColor;
	}
	
	public String getUserStatus() {
		return userStatus;
	}
	
	public void setImage(String imageURL) {
		if(imageURL.contains("http://")) {
			imageLoader.displayImage(imageURL, this);
		} else {
			setImageResource(R.drawable.no_avatar);
		}
	}

}
