package com.forum.fiend.osp;

import java.util.ArrayList;
import com.nostra13.universalimageloader.core.ImageLoader;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

@SuppressLint("NewApi")
public class UserCardAdapter extends BaseAdapter {
	
	private ArrayList<IgnoreItem> data;
    Context c;

    UserCardAdapter (ArrayList<IgnoreItem> data, Context c){
        this.data = data;
        this.c = c;
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
		
		String via = ii.ignoreItemDate;
		if(via.contentEquals("Index page")) {
			via = "Lurking...";
		}
		
		
		
		iiTimestamp.setText(via);
		
		iiUsername.setTextColor(Color.parseColor("#333333"));
    	iiTimestamp.setTextColor(Color.parseColor("#333333"));

		if(ii.ignoreItemAvatar != null && ii.ignoreItemAvatar.contains("http://")) {
			String imageUrl = ii.ignoreItemAvatar;

			ImageLoader.getInstance().displayImage(imageUrl, iiAvatar);
		} else {
			iiAvatar.setImageResource(R.drawable.no_avatar);
		}
	             
		return v;
	}

}
