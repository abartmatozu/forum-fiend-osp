package com.forum.fiend.osp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.TextView;


public class ForumSettingsFragment extends Fragment {
	
	private ForumFiendApp application;
	
	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		
		application = (ForumFiendApp)getActivity().getApplication();
	}
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
        View v = inflater.inflate(R.layout.forum_settings_layout, container, false);
        return v;
    }

	@Override
	public void onStart() {
		
		super.onStart();
  
		setupHandlers();
	}
	
	@Override
	public void onDestroy() {

		super.onDestroy();
	}
	
	@Override
	public void onPause() { 

		super.onPause();
	}
	
	
	@Override
	public void onResume() {

        super.onResume();   
        
        if(application.getForceRefresh()) {
        	application.setForceRefresh(false);
        	
        	getActivity().finish();
    		getActivity().startActivity(getActivity().getIntent());
        }

    }
	
	
	@Override
	public void onStop() {
		super.onStop();

	}
	
	private void setupHandlers() {
		
		SharedPreferences app_preferences = getActivity().getSharedPreferences("prefs", 0);
		
		//Signature button
		LinearLayout forum_setting_tagline = (LinearLayout)getActivity().findViewById(R.id.forum_setting_tagline);
		
		if(application.getSession().getServer().serverUserName.contentEquals("0")) {
			forum_setting_tagline.setVisibility(View.GONE);
		} else {
			LinearLayout forum_setting_tagline_body_builder = (LinearLayout)getActivity().findViewById(R.id.forum_setting_tagline_body_builder);
			
			
	
			boolean useShading = app_preferences.getBoolean("use_shading", false);
			boolean useOpenSans = app_preferences.getBoolean("use_opensans", false);
			int fontSize = app_preferences.getInt("font_size", 16);
			Typeface opensans = Typeface.createFromAsset(getActivity().getAssets(), "fonts/opensans.ttf");
			
			BBCodeParser.parseCode(getActivity(), forum_setting_tagline_body_builder, application.getSession().getServer().serverTagline, opensans, useOpenSans, useShading, null, fontSize,false,"#333333",application);
			
			forum_setting_tagline.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent myIntent = new Intent(getActivity(), New_Post.class);
					
					Bundle bundle = new Bundle();
					bundle.putString("postid","0");
					bundle.putString("parent","0");
					bundle.putString("category","0");
					bundle.putString("subforum_id",(String) "0");
					bundle.putString("original_text",(String) "");
					bundle.putString("boxTitle",(String) "Signature Editor");
					bundle.putString("picture",(String) "0");
					bundle.putString("color",(String) application.getSession().getServer().serverColor);
					bundle.putString("subject",(String) "");
					bundle.putInt("post_type",(Integer) 6);
					myIntent.putExtras(bundle);
	
					startActivity(myIntent);
				}
			});
		}
		
		//Theme button
		LinearLayout forum_setting_theme = (LinearLayout)getActivity().findViewById(R.id.forum_setting_theme);
		forum_setting_theme.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent themeIntent = new Intent(getActivity(), ThemeEditor.class);
				startActivity(themeIntent);
			}
		});


		//Home Page button
		LinearLayout forum_setting_home = (LinearLayout)getActivity().findViewById(R.id.forum_setting_home);
		forum_setting_home.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				PopupMenu popup = new PopupMenu(getActivity(),v);
				MenuInflater inflater = popup.getMenuInflater();
			    inflater.inflate(R.menu.home_page_selection, popup.getMenu());
			    popup.setOnMenuItemClickListener(forumHomeSelected);
			    popup.show();
			}
		});
		
		//Home Page Display Text
		String currentServerId = application.getSession().getServer().serverId;
		String keyName = currentServerId + "_home_page";
		String valueName = getString(R.string.subforum_id);
		String displayName = "Forum Index";
		
		valueName = app_preferences.getString(keyName, getString(R.string.subforum_id));
		
		if(valueName.contentEquals(getString(R.string.subforum_id))) {
			displayName = "Forum Index";
		}
		
		if(valueName.contentEquals("forum_favs")) {
			displayName = "Favorites";
		}
		
		if(valueName.contentEquals("timeline")) {
			displayName = "Timeline";
		}
		
		if(valueName.contentEquals("participated")) {
			displayName = "Participated Topics";
		}
		
		if(valueName.contentEquals("favs")) {
			displayName = "Subscribed Topics";
		}
		
		if(valueName.contentEquals("unread")) {
			displayName = "Unread Topics";
		}
		
		TextView forum_setting_home_current = (TextView)getActivity().findViewById(R.id.forum_setting_home_current);
		forum_setting_home_current.setText(displayName);

		
		
		//Avatars and Icons button
		LinearLayout forum_setting_show_images = (LinearLayout)getActivity().findViewById(R.id.forum_setting_show_images);
		forum_setting_show_images.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				SharedPreferences app_preferences = getActivity().getSharedPreferences("prefs", 0);
				boolean currentAvatarSetting = app_preferences.getBoolean("show_images",true);
				
				TextView forum_setting_show_images_readout = (TextView)getActivity().findViewById(R.id.forum_setting_show_images_readout);
				
				if(currentAvatarSetting) {
					forum_setting_show_images_readout.setText("Off");
					currentAvatarSetting = false;
				} else {
					forum_setting_show_images_readout.setText("On");
					currentAvatarSetting = true;
				}
				
				SharedPreferences.Editor editor = app_preferences.edit();
				editor.putBoolean("show_images", currentAvatarSetting);
				editor.commit();
			}
		});

		boolean currentAvatarSetting = app_preferences.getBoolean("show_images",true);

		TextView forum_setting_show_images_readout = (TextView)getActivity().findViewById(R.id.forum_setting_show_images_readout);
		
		if(currentAvatarSetting) {
			forum_setting_show_images_readout.setText("On");
		} else {
			forum_setting_show_images_readout.setText("Off");
		}
		
		
		
		
		//Sidebar Setting
		LinearLayout forum_setting_sidebar = (LinearLayout)getActivity().findViewById(R.id.forum_setting_sidebar);
		forum_setting_sidebar.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				SharedPreferences app_preferences = getActivity().getSharedPreferences("prefs", 0);
				boolean currentSidebarSetting = app_preferences.getBoolean("show_sidebar",true);
				
				TextView forum_setting_sidebar_setting = (TextView)getActivity().findViewById(R.id.forum_setting_sidebar_setting);
				
				if(currentSidebarSetting) {
					forum_setting_sidebar_setting.setText("Off");
					currentSidebarSetting = false;
				} else {
					forum_setting_sidebar_setting.setText("On");
					currentSidebarSetting = true;
				}
				
				SharedPreferences.Editor editor = app_preferences.edit();
				editor.putBoolean("show_sidebar", currentSidebarSetting);
				editor.commit();
			}
		});
		boolean sidebarSetting = app_preferences.getBoolean("show_sidebar",true);
		TextView forum_setting_sidebar_setting = (TextView)getActivity().findViewById(R.id.forum_setting_sidebar_setting);
		if(sidebarSetting) {
			forum_setting_sidebar_setting.setText("On");
		} else {
			forum_setting_sidebar_setting.setText("Off");
		}
		
		
		
		
		
		
		
		//Quick Reply Setting
		LinearLayout forum_setting_quick_reply = (LinearLayout)getActivity().findViewById(R.id.forum_setting_quick_reply);
		forum_setting_quick_reply.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				SharedPreferences app_preferences = getActivity().getSharedPreferences("prefs", 0);
				boolean quickReplySetting = app_preferences.getBoolean("show_quick_reply",true);
				
				TextView forum_setting_quick_reply_setting = (TextView)getActivity().findViewById(R.id.forum_setting_quick_reply_setting);
				
				if(quickReplySetting) {
					forum_setting_quick_reply_setting.setText("Off");
					quickReplySetting = false;
				} else {
					forum_setting_quick_reply_setting.setText("On");
					quickReplySetting = true;
				}
				
				SharedPreferences.Editor editor = app_preferences.edit();
				editor.putBoolean("show_quick_reply", quickReplySetting);
				editor.commit();
			}
		});
		boolean quickReplySetting = app_preferences.getBoolean("show_quick_reply",true);
		TextView forum_setting_quick_reply_setting = (TextView)getActivity().findViewById(R.id.forum_setting_quick_reply_setting);
		if(quickReplySetting) {
			forum_setting_quick_reply_setting.setText("On");
		} else {
			forum_setting_quick_reply_setting.setText("Off");
		}
		
		
		
		
		
		
		
		//Display name button
		LinearLayout forum_setting_display_name = (LinearLayout)getActivity().findViewById(R.id.forum_setting_display_name);
		forum_setting_display_name.setVisibility(View.GONE);
		
		//Text settings button
		LinearLayout forum_setting_text = (LinearLayout)getActivity().findViewById(R.id.forum_setting_text);
		forum_setting_text.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				TextDialogFragment newFragment = TextDialogFragment.newInstance();
			    newFragment.show(getActivity().getSupportFragmentManager(), "dialog");
			}
		});
		
		//Clear cache button
		LinearLayout forum_setting_cache = (LinearLayout)getActivity().findViewById(R.id.forum_setting_cache);
		forum_setting_cache.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				CacheNuker.NukeCache(getActivity());
				
				Intent intro = new Intent(getActivity(),IntroScreen.class);
				
				getActivity().finish();
				
				if(!getString(R.string.server_location).contentEquals("0")) {
					getActivity().startActivity(intro);
				}
			}
		});
		
		//About button
		LinearLayout forum_setting_about = (LinearLayout)getActivity().findViewById(R.id.forum_setting_about);
		forum_setting_about.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent aboutIntent = new Intent(getActivity(), About.class);
				startActivity(aboutIntent);
			}
		});
	}
	
	private OnMenuItemClickListener forumHomeSelected = new OnMenuItemClickListener() {

		@Override
		public boolean onMenuItemClick(MenuItem arg0) {
			String currentServerId = application.getSession().getServer().serverId;
			String keyName = currentServerId + "_home_page";
			String valueName = getString(R.string.subforum_id);
			String displayName = "Forum Index";
			
		    switch (arg0.getItemId()) {
		        case R.id.menu_home_favs:
		        	valueName = "forum_favs";
		        	displayName = "Favorites";
		        	break;
		        case R.id.menu_home_index:
		        	valueName = getString(R.string.subforum_id);
		        	displayName = "Forum Index";
		        	break;
		        case R.id.menu_home_participated:
		        	valueName = "participated";
		        	displayName = "Participated Topics";
		        	break;
		        case R.id.menu_home_subscribed:
		        	valueName = "favs";
		        	displayName = "Subscribed Topics";
		        	break;
		        case R.id.menu_home_unread:
		        	valueName = "unread";
		        	displayName = "Unread Topics";
		        	break;
		        case R.id.menu_home_timeline:
		        	valueName = "timeline";
		        	displayName = "Timeline";
		        	break;
		    }
		    
		    SharedPreferences app_preferences = getActivity().getSharedPreferences("prefs", 0);
		    SharedPreferences.Editor editor = app_preferences.edit();
			editor.putString(keyName, valueName);
			editor.commit();
			
			TextView forum_setting_home_current = (TextView)getActivity().findViewById(R.id.forum_setting_home_current);
			forum_setting_home_current.setText(displayName);
			
			return true;
		}
		
	};

	
}
