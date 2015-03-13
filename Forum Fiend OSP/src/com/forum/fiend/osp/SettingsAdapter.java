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
import android.widget.TextView;

@SuppressLint({ "NewApi", "InflateParams" })
public class SettingsAdapter extends BaseAdapter {
	

	private ArrayList<Setting> data;
	
    Context c;
	
    SettingsAdapter (ArrayList<Setting> data, Context c){
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

	public View getView(int arg0, View arg1, ViewGroup arg2) {
		View v = arg1;
		if (v == null)
		{
			LayoutInflater vi = (LayoutInflater)c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			
			v = vi.inflate(R.layout.settings_item, null);
			
			
		}
		
		TextView tvSubject = (TextView) v.findViewById(R.id.settings_name);
		ImageView ivLogo = (ImageView)v.findViewById(R.id.settings_logo);
		TextView tvCounter = (TextView) v.findViewById(R.id.settings_counter);

		Setting s = data.get(arg0);

		tvSubject.setText(s.settingName);
		ivLogo.setImageResource(s.settingIcon);
		
		if(s.settingColor.contains("#")) {
			ivLogo.setColorFilter(Color.parseColor(s.settingColor));
		} else {
			ivLogo.setColorFilter(Color.parseColor("#000000"));
		}
		
		if(s.counterItem == 0) {
			tvCounter.setVisibility(View.GONE);
		} else {
			tvCounter.setText(Integer.toString(s.counterItem));
		}
		                   
		return v;
	}

}
