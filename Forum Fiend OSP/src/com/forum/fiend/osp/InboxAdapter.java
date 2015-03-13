package com.forum.fiend.osp;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Vector;
import com.nostra13.universalimageloader.core.ImageLoader;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

@SuppressLint({ "NewApi", "InflateParams" })
public class InboxAdapter extends BaseAdapter {
	
	private ArrayList<InboxItem> data;
    Context c;

    private boolean useShading = false;
	private boolean useOpenSans = false;
	@SuppressWarnings("unused")
	private int fontSize = 20;
	
	private ForumFiendApp application;
    
    InboxAdapter (ArrayList<InboxItem> data, Context c,ForumFiendApp application){
        this.data = data;
        this.c = c;
        this.application = application;
		
		SharedPreferences app_preferences = c.getSharedPreferences("prefs", 0);

        useShading = app_preferences.getBoolean("use_shading", false);
		useOpenSans = app_preferences.getBoolean("use_opensans", false);
		fontSize = app_preferences.getInt("font_size", 16);
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
		if (v == null) {
			LayoutInflater vi = (LayoutInflater)c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			
			v = vi.inflate(R.layout.inbox_item, null);
			
			
		}
		
		LinearLayout ll_border_background = (LinearLayout) v.findViewById(R.id.ll_border_background);
		LinearLayout ll_color_background = (LinearLayout) v.findViewById(R.id.ll_color_background);

		String textColor = c.getString(R.string.default_text_color);
		
		if(application.getSession().getServer().serverTextColor.contains("#")) {
			textColor = application.getSession().getServer().serverTextColor;
		}
		
		String boxColor = c.getString(R.string.default_element_background);
		
		if(application.getSession().getServer().serverBoxColor != null) {
			boxColor = application.getSession().getServer().serverBoxColor;
		}
		
		if(boxColor.contains("#")) {
			ll_color_background.setBackgroundColor(Color.parseColor(boxColor));
		} else {
			ll_color_background.setBackgroundColor(Color.TRANSPARENT);
		}
		
		
		String boxBorder = c.getString(R.string.default_element_border);
		
		if(application.getSession().getServer().serverBoxBorder != null) {
			boxBorder = application.getSession().getServer().serverBoxBorder;
		}
		
		if(boxBorder.contentEquals("1")) {
			ll_border_background.setBackgroundResource(R.drawable.element_border);
		} else {
			ll_border_background.setBackgroundColor(Color.TRANSPARENT);
		}
		
		TextView tvSubject = (TextView) v.findViewById(R.id.inbox_subject);
		TextView tvUpdated = (TextView) v.findViewById(R.id.inbox_sender);
		TextView tvTimestamp = (TextView) v.findViewById(R.id.inbox_timestamp);
		ImageView ivSubforumIndicator = (ImageView) v.findViewById(R.id.inbox_avatar);
		
		Typeface opensans = Typeface.createFromAsset(c.getAssets(), "fonts/opensans.ttf");
		
		if(useOpenSans) {
			tvSubject.setTypeface(opensans);
			tvUpdated.setTypeface(opensans);
		}

		InboxItem ii = data.get(arg0);
		
		if(ii.senderAvatar.contains("http")) {
			String imageUrl = ii.senderAvatar;
			ImageLoader.getInstance().displayImage(imageUrl, ivSubforumIndicator);
		} else {
			ivSubforumIndicator.setImageResource(R.drawable.no_avatar);
		}
		
		if(boxColor != null && boxColor.contains("#") && boxColor.length() == 7) {
			ImageView category_subforum_indicator_frame = (ImageView)v.findViewById(R.id.inbox_avatar_frame);
			category_subforum_indicator_frame.setColorFilter(Color.parseColor(boxColor));
		} else {
			ImageView category_subforum_indicator_frame = (ImageView)v.findViewById(R.id.inbox_avatar_frame);
			category_subforum_indicator_frame.setVisibility(View.GONE);
		}
		
		tvSubject.setTextColor(Color.parseColor(textColor));
    	tvUpdated.setTextColor(Color.parseColor(textColor));
    	tvTimestamp.setTextColor(Color.parseColor(textColor));
    	
    	if(useShading) {
    		tvSubject.setShadowLayer(2, 0, 0, Color.parseColor(textColor.replace("#", "#66")));
    		tvUpdated.setShadowLayer(2, 0, 0, Color.parseColor(textColor.replace("#", "#66")));
    		tvTimestamp.setShadowLayer(2, 0, 0, Color.parseColor(textColor.replace("#", "#66")));
    	}

		tvSubject.setText(ii.inbox_sender);
		tvUpdated.setText(ii.inbox_moderator);
		
		if(ii.isUnread) {
			
			if(ii.inbox_sender_color.contains("#")) {
				tvSubject.setTextColor(Color.parseColor(ii.inbox_sender_color));
				
				if(useShading) {
					tvSubject.setShadowLayer(2, 0, 0, Color.parseColor(ii.inbox_sender_color.replace("#", "#66")));
				}
			} else {
				tvSubject.setTextColor(Color.RED);
				
				if(useShading) {
					tvSubject.setShadowLayer(2, 0, 0, Color.parseColor("#66ff0000"));
				}
			}
			
			tvSubject.setTypeface(null, Typeface.BOLD);
			
			
		} else {
			tvSubject.setTextColor(Color.parseColor(textColor));
			
			tvSubject.setTypeface(null, Typeface.NORMAL);
        	
        	if(useShading) {
        		tvSubject.setShadowLayer(2, 0, 0, Color.parseColor(textColor.replace("#", "#66")));
        	}
		}
		
		String timeString = ii.inbox_unread;
		
		try {
			Date date = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy",Locale.ENGLISH).parse(ii.inbox_unread);
			
			Date now = new Date();
			
			long difference = now.getTime() - date.getTime();
			
			long seconds = difference / 1000;
			
			timeString = seconds + "s";
			
			if(seconds > 59) {
				long minutes = seconds / 60;
				timeString = minutes + "m";
				
				if(minutes > 59) {
					long hours = minutes / 60;
					timeString = hours + "h";
					
					if(hours > 23) {
						long days = hours / 24;
						timeString = days + "d";
					}
				}
			}
			
			tvTimestamp.setText(timeString);
		} catch(Exception ex) {
			tvTimestamp.setVisibility(View.GONE);
		}
		
		ImageView inbox_delete = (ImageView)v.findViewById(R.id.inbox_delete);
		inbox_delete.setTag(arg0);
		inbox_delete.setOnClickListener(deleteClicked);
		
		return v;
	}
	
	private OnClickListener deleteClicked = new OnClickListener() {

		@Override
		public void onClick(View arg0) {
			int itemId = (Integer) arg0.getTag();
			InboxItem ii = data.get(itemId);

			if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
				  new messageDeleter().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,ii);
			} else {
				  new messageDeleter().execute(ii);
			}
			
			ii.isDeleted = true;
			data.remove(itemId);
			
			InboxAdapter.this.notifyDataSetChanged();
			
			
		}
		
	};
	
	private class messageDeleter extends AsyncTask<InboxItem, Void, Object[]> {
		@SuppressWarnings({ "unchecked", "rawtypes" })
		@Override
		protected Object[] doInBackground(InboxItem... params) {

			Object[] result = new Object[50];

			InboxItem item = params[0];
			
			try {
			    Vector paramz = new Vector();
			    paramz.addElement(item.sender_id);
			    paramz.addElement(item.inboxId);

			    result[0] = application.getSession().performSynchronousCall("delete_message", paramz);

			} catch(Exception e) {
				Log.w("Forum Fiend",e.getMessage());
				return null;
			}
			return result;
		}
		
		protected void onPostExecute(final Object[] result) {
			// nothing to do here really
		}
    }

}
