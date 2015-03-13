package com.forum.fiend.osp;

import java.util.ArrayList;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.nostra13.universalimageloader.core.ImageLoader;

@SuppressLint("ViewHolder")
public class ServerAdapter extends BaseAdapter {
	
	private ArrayList<Server> data;
    Context c;
    
    ServerAdapter (ArrayList<Server> data, Context c){
        this.data = data;
        this.c = c;
    }

	public int getCount() {
		return data.size();
	}

	public Object getItem(int arg0) {
		return data.get(arg0);
	}

	public long getItemId(int arg0) {
		return arg0;
	}

	public View getView(int arg0, View arg1, ViewGroup arg2) {
		View v = arg1;
		LayoutInflater vi = (LayoutInflater)c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		v = vi.inflate(R.layout.server, null);
		
		TextView serverAddress = (TextView) v.findViewById(R.id.server_address);
		TextView serverUsername = (TextView) v.findViewById(R.id.server_username);
		ImageView serverUserAvater = (ImageView) v.findViewById(R.id.server_user_avatar);
		RelativeLayout serverTabColor = (RelativeLayout) v.findViewById(R.id.server_tab_color);

		Server s = data.get(arg0);

		if(s.serverName.contentEquals("0")) {
			serverAddress.setText(s.serverAddress.replace("http://", ""));
		} else {
			serverAddress.setText(s.serverName);
		}
		
		
		
		if(s.serverUserName.contentEquals("0")) {
			serverUsername.setText("Guest");
		} else {
			serverUsername.setText(s.serverUserName);
		}
		
		if(s.serverAvatar.contains("http")) {
			ImageLoader.getInstance().displayImage(s.serverAvatar, serverUserAvater);
		} else {
			if(s.serverTagline.contentEquals("[*WEBVIEW*]")) {
				serverUserAvater.setImageResource(R.drawable.webview_forum);
			} else {
				serverUserAvater.setImageResource(R.drawable.no_avatar);
			}
		}
		
		if(s.serverColor.contains("#")) {
			serverTabColor.setBackgroundColor(Color.parseColor(s.serverColor));
		} else {
			serverTabColor.setBackgroundColor(Color.parseColor(c.getString(R.string.default_color)));
		}
		
		
		ImageView serverIcon = (ImageView) v.findViewById(R.id.server_server_icon);
		
		if(s.serverIcon.contains("http")) {
			serverIcon.setVisibility(View.VISIBLE);
			ImageLoader.getInstance().displayImage(s.serverIcon, serverIcon);
		} else {
			serverIcon.setVisibility(View.GONE);
		}
                
		return v;
	}

}
