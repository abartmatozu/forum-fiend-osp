package com.forum.fiend.osp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;

public class SettingsFragment extends Fragment {
	
	private boolean editingProfile = false;
	
	private String storagePrefix = "";
	private String server_address;
	
	private int unreadMail = 0;
	
	private ForumFiendApp application;
	
	private String background;
	
	private ListView lvMain;
	
	private ArrayList<Setting> settingsOptions;
	
	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		
		application = (ForumFiendApp)getActivity().getApplication();
		
		setHasOptionsMenu(true);
	}
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {

		View v = inflater.inflate(R.layout.settings_fragment, container, false);
        return v;
    }
	
	@Override
	public void onStart() {
		super.onStart();
		
		juiceUpMenu();
	}
	
	public void onResume() {
		unreadMail = 0;
		
		if(editingProfile) {
			getActivity().finish();
			getActivity().startActivity(getActivity().getIntent());
		}
		
		super.onResume();
		
		if(!application.getSession().getServer().serverUserId.contentEquals("0")) {
			new checkUnreadMail().execute();
		}
	}

	private void theme_changer() {
		SharedPreferences app_preferences = getActivity().getSharedPreferences("prefs", 0);
		int themeInt = app_preferences.getInt(storagePrefix + "loggedThemeInt", Integer.parseInt(getString(R.string.default_theme)));

		themeInt ++;
		if(themeInt > 6) {
			themeInt = 4;
		}

		application.getSession().getServer().serverTheme = Integer.toString(themeInt);
		application.getSession().updateServer();

		getActivity().finish();
		getActivity().startActivity(getActivity().getIntent());
	}
	
	private void changeTextSettings() {
		TextDialogFragment newFragment = TextDialogFragment.newInstance();
	    newFragment.show(getActivity().getSupportFragmentManager(), "dialog");
	}
	
	private void setAccentColor() {
		ColorPickerDialogFragment newFragment = ColorPickerDialogFragment.newInstance();
		newFragment.setOnColorSelectedListener(new ColorPickerDialogFragment.onColorSelectedListener() {
			
			public void onColorSelected(String color) {
				setColor(color);
			}
		});
	    newFragment.show(getActivity().getSupportFragmentManager(), "dialog");
	}
	
	private void setColor(String color) {
		
		application.getSession().getServer().serverColor = color;
		application.getSession().updateServer();
		
		getActivity().finish();
		getActivity().startActivity(getActivity().getIntent());
	}
	
	private void clearCache() {
		CacheNuker.NukeCache(getActivity());
		
		Intent intro = new Intent(getActivity(),IntroScreen.class);
		
		getActivity().finish();
		
		if(!getString(R.string.server_location).contentEquals("0")) {
			getActivity().startActivity(intro);
		}
		
		
	}
	
	@SuppressWarnings("rawtypes")
	private void doLogout() {
		
		final SharedPreferences preferences = getActivity().getSharedPreferences("prefs", 0);			

		try
		{

		    Vector paramz = new Vector();

		    application.getSession().performSynchronousCall("logout_user", paramz);

		}
		catch(Exception e)
		{
			//Log.w("Discussions",e.getMessage());
		}
		
		SharedPreferences.Editor editor = preferences.edit();
		editor.putInt("cookie_count", 0);
    	editor.putString(storagePrefix + "logged_userid", "0");
    	editor.putString(storagePrefix + "logged_password", "0");
    	editor.putString(storagePrefix + "logged_userlevel", "0");
    	editor.putString(storagePrefix + "logged_modpower", "0");
    	editor.putString(storagePrefix + "logged_postcount", "0");
    	editor.putString(storagePrefix + "logged_bgColor", getString(R.string.default_color));
    	editor.putInt(storagePrefix + "loggedThemeInt", Integer.parseInt(getString(R.string.default_theme)));
    	editor.putInt(storagePrefix + "last_main_tab", 0);
    	editor.commit();
    	getActivity().finish();
    	getActivity().startActivity(getActivity().getIntent());
	}
	
	private void taglineEditor() {

	Intent myIntent = new Intent(getActivity(), New_Post.class);
		
		Bundle bundle = new Bundle();
		bundle.putString("postid","0");
		bundle.putString("parent","0");
		bundle.putString("category","0");
		bundle.putString("subforum_id",(String) "0");
		bundle.putString("original_text",(String) "");
		bundle.putString("boxTitle",(String) "Signature Editor");
		bundle.putString("picture",(String) "0");
		bundle.putString("color",(String) background);
		bundle.putString("subject",(String) "");
		bundle.putInt("post_type",(Integer) 6);
		myIntent.putExtras(bundle);

		startActivity(myIntent);
    }

	private void launchUsersList() {
		
		SharedPreferences app_preferences = getActivity().getSharedPreferences("prefs", 0);

		String accent;
        if(app_preferences.getString(storagePrefix + "logged_bgColor", getString(R.string.default_color)).contains("#")) {
        	accent = app_preferences.getString(storagePrefix + "logged_bgColor", getString(R.string.default_color));
        } else {
        	accent = getString(R.string.default_color);
        }
		
		
		Intent myIntent = new Intent(getActivity(), ActiveUsersActivity.class);
		Bundle bundle = new Bundle();
		bundle.putString("background",(String) accent);
		myIntent.putExtras(bundle);
		startActivity(myIntent);
	}
	
	private void loadMyWall() {

		String userid = application.getSession().getServer().serverUserId;
		String username = application.getSession().getServer().serverUserName;
		
		if(profileSelected != null) {
			profileSelected.onProfileSelected(username, userid);
		}
	}
	
	private void setupUserCard() {
		
		if(getActivity() == null) {
			return;
		}
		
		LinearLayout userLayout = (LinearLayout)getActivity().findViewById(R.id.settings_user_box);
		
		
		
		if(application.getSession().getServer().serverUserId.contentEquals("0")) {
			userLayout.setVisibility(View.GONE);
		} else {
			ImageView ivAvatar = (ImageView)getActivity().findViewById(R.id.settings_user_avatar);
			TextView tvUsername = (TextView)getActivity().findViewById(R.id.settings_user_name);
			ImageView ivLogout = (ImageView)getActivity().findViewById(R.id.settings_user_logout);
			
			tvUsername.setText(application.getSession().getServer().serverUserName);
			
			if(application.getSession().getServer().serverAvatar.contains("http")) {
				ImageLoader.getInstance().displayImage(application.getSession().getServer().serverAvatar, ivAvatar);
			} else {
				ivAvatar.setImageResource(R.drawable.no_avatar);
			}
			
			ivLogout.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					logOut();
				}
			});
			
			
			
			userLayout.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					loadMyWall();
				}
			});
		}
	}
	
	private void logOut() {
		
		application.getSession().logOutSession();
		
		Intent intent = new Intent(getActivity(),IntroScreen.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); 
		
		Bundle bundle = new Bundle();
		bundle.putBoolean("reboot", true);
		intent.putExtras(bundle);
		
		startActivity(intent);
	}
	
	//Profile selected interface
  	public interface onProfileSelectedListener {
  		public abstract void onProfileSelected(String username,String userid);
  	}
  	
  	private onProfileSelectedListener profileSelected = null;
  	
  	public void setOnProfileSelectedListener(onProfileSelectedListener l) {
  		profileSelected = l;
  	}
  	
  	//IndexRequest Interface
  	public interface onIndexRequestedListener {
  		public abstract void onIndexRequested();
  	}
  	
  	private onIndexRequestedListener indexRequested = null;
  	
  	public void setOnIndexRequestedListener(onIndexRequestedListener l) {
  		indexRequested = l;
  	}
  	
  	
  	
  	
	//SettingsRequest Interface
  	public interface onSettingsRequestedListener {
  		public abstract void onSettingsRequested();
  	}
  	
  	private onSettingsRequestedListener settingsRequested = null;
  	
  	public void setOnSettingsRequestedListener(onSettingsRequestedListener l) {
  		settingsRequested = l;
  	}
  	
  	//Category Selected Interface
  	public interface onCategorySelectedListener {
  		public abstract void onCategorySelected(Category ca);
  	}
  	
  	private onCategorySelectedListener categorySelected = null;
  	
  	public void setOnCategorySelectedListener(onCategorySelectedListener l) {
  		categorySelected = l;
  	}
  	
  	private void juiceUpMenu() {
  		setupUserCard();
  		
  		if(getActivity() == null) {
  			return;
  		}
		
		lvMain = (ListView)getActivity().findViewById(R.id.settings_list);
		lvMain.setDivider(null);
		
        Bundle bundle = getArguments();

        if(bundle.containsKey("background")) {
        	background = bundle.getString("background");
        }
        
        server_address = application.getSession().getServer().serverAddress;
        
        if(getString(R.string.server_location).contentEquals("0")) {
        	storagePrefix = server_address + "_";
        }
        
        String userid = application.getSession().getServer().serverUserId;

        settingsOptions = new ArrayList<Setting>();

        if(userid.contentEquals("0")) {
        	//To be implemented in future!
        	//settingsOptions.add("Login");
        } else {
        	
        	Setting sInbox = new Setting();
        	sInbox.settingName = "Inbox";
        	sInbox.settingIcon = R.drawable.drawer_inbox;
        	sInbox.counterItem = unreadMail;

        	settingsOptions.add(sInbox);
        	
        }
        
        Setting sIndex = new Setting();
        sIndex.settingName = "Forum Index";
        sIndex.settingIcon = R.drawable.drawer_index;
        
        
        settingsOptions.add(sIndex);
        
        if(userid.contentEquals("0")) {
        	//To be implemented in future!
        	//settingsOptions.add("Login");
        } else {
        	
        	Setting sTimeline = new Setting();
        	sTimeline.settingName = "Timeline";
        	sTimeline.settingIcon = R.drawable.drawer_timeline;
        	
        	Setting sFavs = new Setting();
        	sFavs.settingName = "Favorites";
        	sFavs.settingIcon = R.drawable.drawer_favorites;
            
            Setting sUnread = new Setting();
            sUnread.settingName = "Unread Topics";
            sUnread.settingIcon = R.drawable.drawer_unread;
            
            Setting sParticipated = new Setting();
            sParticipated.settingName = "Participated Topics";
            sParticipated.settingIcon = R.drawable.drawer_participated;
            
            Setting sSubscribed = new Setting();
            sSubscribed.settingName = "Subscribed Topics";
            sSubscribed.settingIcon = R.drawable.drawer_subscribed;
            
            settingsOptions.add(sTimeline);
        	settingsOptions.add(sFavs);
        	settingsOptions.add(sUnread);
        	settingsOptions.add(sParticipated);
        	settingsOptions.add(sSubscribed);
        	
        	//Setting sProfile = new Setting();
        	//sProfile.settingName = "My Profile";
        	//sProfile.settingIcon = R.drawable.drawer_favorites;
        	
        	//settingsOptions.add(sProfile);
        	

        }
        
        Setting sSettings = new Setting();
        sSettings.settingName = "Settings";
        sSettings.settingIcon = R.drawable.drawer_settings;
        
        settingsOptions.add(sSettings); 


        if(getString(R.string.server_location).contentEquals("0")) {
        	
        	Setting sClose = new Setting();
        	sClose.settingName = "Close Forum";
        	sClose.settingIcon = R.drawable.drawer_close_forum;
        	
        	settingsOptions.add(sClose);
        }
        
        
        
        
        lvMain.setAdapter(new SettingsAdapter(settingsOptions,getActivity()));


        lvMain.setTextFilterEnabled(true);

        lvMain.setOnItemClickListener(new OnItemClickListener() 
		  {
		    public void onItemClick(AdapterView<?> parent, View view,int position, long id) 
		    {
		    	if(settingsOptions == null) {
		    		return;
		    	}
		    	
		    	String the_result = settingsOptions.get(position).settingName;
		    	
		    	if(the_result.contentEquals("Unread Topics")) {
		    		Category ca = new Category();
					ca.category_name = "Unread Topics";
					ca.subforum_id = "0";
					ca.category_id = "unread";
					ca.category_description = "Review all of the topics with posts you haven't seen yet.";
					ca.categoryType = "S";
					ca.categoryColor = background;
					
					if(categorySelected != null) {
						categorySelected.onCategorySelected(ca);
					}
		    	}
		    	
		    	if(the_result.contentEquals("Participated Topics")) {
		    		Category ca = new Category();
		    		ca.category_name = "Participated Topics";
					ca.subforum_id = "0";
					ca.category_id = "participated";
					ca.category_description = "Check out all of the topics that you have participated in.";
					ca.categoryType = "S";
					ca.categoryColor = background;
					
					if(categorySelected != null) {
						categorySelected.onCategorySelected(ca);
					}
		    	}
		    	
		    	if(the_result.contentEquals("Subscribed Topics")) {
		    		Category ca = new Category();
		    		ca.category_name = "Subscribed Topics";
					ca.subforum_id = "0";
					ca.category_id = "favs";
					ca.category_description = "Check out all of your subcriptions!";
					ca.categoryType = "S";
					ca.categoryColor = background;
					
					if(categorySelected != null) {
						categorySelected.onCategorySelected(ca);
					}
		    	}
		    	
		    	if(the_result.contentEquals("Favorites")) {
		    		Category ca = new Category();
		    		ca.category_name = "Favorites";
					ca.subforum_id = "0";
					ca.category_id = "forum_favs";
					ca.category_description = "Your favorite forums.";
					ca.categoryType = "S";
					ca.categoryColor = background;
					
					if(categorySelected != null) {
						categorySelected.onCategorySelected(ca);
					}
		    	}
		    	
		    	if(the_result.contentEquals("Timeline")) {
		    		Category ca = new Category();
		    		ca.category_name = "Timeline";
					ca.subforum_id = "0";
					ca.category_id = "timeline";
					ca.category_description = "The latest posts forum-wide.";
					ca.categoryType = "S";
					ca.categoryColor = background;
					
					if(categorySelected != null) {
						categorySelected.onCategorySelected(ca);
					}
		    	}

		    	if(the_result.contentEquals("Make a Donation")) {
		    		String url = "http://www.ape-apps.com/donation/";
		    		Intent i = new Intent(Intent.ACTION_VIEW);
		    		i.setData(Uri.parse(url));
		    		startActivity(i);
		    	}
		    	
		    	if(the_result.contentEquals("Inbox")) {
		    		Intent myIntent = new Intent(getActivity(), Mail.class);
		    		startActivity(myIntent);
		    	}
		    	
		    	if(the_result.contentEquals("My Profile")) {
		    		loadMyWall();
		    	}
		    	
		    	if(the_result.contentEquals("Edit Signature"))
		    	{
		    		taglineEditor();
		    	}

		    	if(the_result.contentEquals("Active Users"))
		    	{
		    		launchUsersList();
		    	}
		    	
		    	
		    	if(the_result.contentEquals("Toggle Theme"))
		    	{
		    		theme_changer();
		    	}
		    	
		    	if(the_result.contentEquals("Change Theme Color")) {
		    		setAccentColor();
		    	}
		    	
		    	if(the_result.contentEquals("License Agreement"))
		    	{
		    		Eula.showDisclaimer(getActivity());
		    	}
		    	
		    	if(the_result.contentEquals("Clear Cache"))
		    	{
		    		clearCache();
		    	}
		    	
		    	if(the_result.contentEquals("Logout"))
		    	{
		    		doLogout();
		    	}
		    	
		    	if(the_result.contentEquals("Text Options")) {
		    		changeTextSettings();
		    	}
		    	
		    	if(the_result.contentEquals("Close Forum")) {
		    		getActivity().finish();
		    	}
		    	
		    	if(the_result.contentEquals("About")) {
		    		Intent aboutIntent = new Intent(getActivity(), About.class);
					startActivity(aboutIntent);
		    	}
		    	
		    	if(the_result.contentEquals("Forum Index")) {
		    		if(indexRequested != null) {
		    			indexRequested.onIndexRequested();
		    		}
		    	}
		    	
		    	if(the_result.contentEquals("Settings")) {
		    		if(settingsRequested != null) {
		    			settingsRequested.onSettingsRequested();
		    		}
		    	}
		    	
		    	
		    	
		    	
		    	if(the_result.contentEquals("The Wiki"))
		    	{
		    		String url = "http://www.discussions-online.com";
		    		Intent i = new Intent(Intent.ACTION_VIEW);
		    		i.setData(Uri.parse(url));
		    		startActivity(i);
		    	}
		    	
		    	if(the_result.contentEquals("Ape Apps Blog"))
		    	{
		    		String url = "http://www.ape-apps.com";
		    		Intent i = new Intent(Intent.ACTION_VIEW);
		    		i.setData(Uri.parse(url));
		    		startActivity(i);
		    	}
		    	
		    	if(the_result.contentEquals("Upgrade (Free)"))
		    	{
		    		String url = "http://market.ape-apps.com/app.php?app=1";
		    		Intent i = new Intent(Intent.ACTION_VIEW);
		    		i.setData(Uri.parse(url));
		    		startActivity(i);
		    	}
		    }
		  });
  	}
  	
  	private class checkUnreadMail extends AsyncTask<String, Void, Object[]> {
		@SuppressWarnings({ "rawtypes" })
		@Override
		protected Object[] doInBackground(String... params) {

			Object[] result = new Object[50];

			try {
			    Vector paramz = new Vector();

			    result[0] = application.getSession().performSynchronousCall("get_inbox_stat", paramz);
			}
			catch(Exception e)
			{
				Log.w("Forum Fiend",e.getMessage());
				return null;
			}
			return result;
		}
		
		@SuppressWarnings("rawtypes")
		protected void onPostExecute(final Object[] result) {
			if(result == null) {
				Toast toast = Toast.makeText(getActivity(), "No response from the server!", Toast.LENGTH_LONG);
				toast.show();
				return;
			}

			for(Object o: result) {
				
				if(o != null) {
		            HashMap map = (HashMap) o;
		            
		            if( map.get("inbox_unread_count") != null) {
		            	unreadMail = (Integer) map.get("inbox_unread_count");
		            }

			    }
		    }

			juiceUpMenu();

	    }
	}
}
