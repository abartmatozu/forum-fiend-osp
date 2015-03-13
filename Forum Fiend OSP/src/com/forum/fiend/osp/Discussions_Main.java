package com.forum.fiend.osp;

import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Formatter;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.GestureOverlayView;
import android.gesture.GestureOverlayView.OnGesturePerformedListener;
import android.gesture.Prediction;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import android.widget.SearchView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;

@SuppressWarnings("deprecation")
@SuppressLint("NewApi")
public class Discussions_Main extends FragmentActivity {
	
	public static Bitmap forum_background;
	private ActionBarDrawerToggle mDrawerToggle;
	private ActionBar actionBar;
	private FrameLayout flSecondary;

	private String userid;
	private String storagePrefix = "";
	private String background;
	private View seperator;
	private String incomingText = "";
	private String server_address;
	private String screenTitle;
	private String screenSubtitle;
	private String baseSubtitle;
	
	public boolean initialLoad = true;
	
	private GestureLibrary gLib;
	private static final String TAG = "com.hascode.android.gesture";
	private GestureOverlayView gestures;
	
	private int backStackId;
	
	private DrawerLayout mDrawerLayout;
	
	private ForumFiendApp application;
	private AnalyticsHelper ah;
	
	private boolean sidebarOption;
	
    /** Called when the activity is first created. */
    @SuppressLint("NewApi")
	@Override
    public void onCreate(Bundle savedInstanceState)  {
    	
    	application = (ForumFiendApp)getApplication();
    	application.appActive = true;
    	backStackId = application.getBackStackId();
    	
    	ah = application.getAnalyticsHelper();

    	if(application.getSession().getServer().serverIcon.contentEquals("0")) {
    		if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
    			new checkForumIcon().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    		} else {
    			new checkForumIcon().execute();
    		}
    	}

        SharedPreferences app_preferences = getSharedPreferences("prefs", 0);
        server_address = application.getSession().getServer().serverAddress;
        
        sidebarOption = app_preferences.getBoolean("show_sidebar", true);
        
		screenTitle = getString(R.string.app_name);

        if(getString(R.string.server_location).contentEquals("0")) {
        	storagePrefix = server_address + "_";
        	screenSubtitle = server_address.replace("http://", "").replace("https://", "");
        } else {
        	screenSubtitle = screenTitle;
        }

        userid = application.getSession().getServer().serverUserId;
        String tagline = application.getSession().getServer().serverTagline;
        
        SharedPreferences.Editor editor = app_preferences.edit();

        if(tagline.contentEquals("null") || tagline.contentEquals("0")) {
	        String deviceName = android.os.Build.MODEL;
	        String appName = getString(R.string.app_name);
	        String appVersion = getString(R.string.app_version);
	        
	        String appColor = getString(R.string.default_color);
	        
	        if(application.getSession().getServer().serverColor.contains("#")) {
	        	appColor = application.getSession().getServer().serverColor;
	        }
	        
	        String standardTagline = "[color=" + appColor + "][b]Sent from my " + deviceName + " using " + appName + " v" + appVersion + ".[/b][/color]";
	
	        application.getSession().getServer().serverTagline = standardTagline;
	        application.getSession().updateServer();
		    
        }

        editor.putInt(storagePrefix + "just_logged_in", 0);
        
        editor.commit();

        if(userid.contentEquals("0")) {
        	Toast toast = Toast.makeText(Discussions_Main.this, "TIP: Tap on the key icon to log in to your forum account.", Toast.LENGTH_LONG);
			toast.show();
        }

        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();
        
        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                handleSendText(intent); // Handle text being sent
            } else if (type.startsWith("image/")) {
                handleSendImage(intent); // Handle single image being sent
            }
        } else if (Intent.ACTION_SEND_MULTIPLE.equals(action) && type != null) {
            if (type.startsWith("image/")) {
                handleSendMultipleImages(intent); // Handle multiple images being sent
            }
        } else {
            // Handle other intents, such as being started from the home screen
        }
        
        background = application.getSession().getServer().serverColor;
        
        ThemeSetter.setTheme(this,background);

        super.onCreate(savedInstanceState);
        
        ThemeSetter.setActionBar(this,background);
        
        actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        
        actionBar.setTitle(screenTitle);
        actionBar.setSubtitle(screenSubtitle);
        
        //Send app analytics data
        ah.trackScreen(getClass().getSimpleName(), false);
	    ah.trackEvent("server connection", "connected", "connected", false);
        
        //Send tracking data for parsed analytics from forumfiend.json
        String manifestAnalytics = server_address = application.getSession().getServer().analyticsId;
        if(manifestAnalytics != null && !manifestAnalytics.contentEquals("0")) {
        	ah.trackCustomScreen(manifestAnalytics,"Forum Fiend OSP v" + getString(R.string.app_version) + " for Android");
        }
        
        setContentView(R.layout.main_swipe);
        
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
    	flSecondary = (FrameLayout) findViewById(R.id.main_page_frame_right);
    	seperator = findViewById(R.id.main_page_seperator);
    	
    	//Setup forum background
    	String forumWallpaper = application.getSession().getServer().serverWallpaper;
    	String forumBackground = application.getSession().getServer().serverBackground;
    	
    	if(forumBackground != null && forumBackground.contains("#") && forumBackground.length() == 7) {
    		mDrawerLayout.setBackgroundColor(Color.parseColor(forumBackground));
    	} else {
    		mDrawerLayout.setBackgroundColor(Color.parseColor(getString(R.string.default_background)));
    	}
    	
    	if(forumWallpaper != null && forumWallpaper.contains("http")) {

        	ImageView main_swipe_image_background = (ImageView)findViewById(R.id.main_swipe_image_background);
        	
        	String imageUrl = forumWallpaper;
    		ImageLoader.getInstance().displayImage(imageUrl, main_swipe_image_background);
    	} else {
    		findViewById(R.id.main_swipe_image_background).setVisibility(View.GONE);
    	}

        setupSlidingDrawer();
        
        Bundle bundle;
        
        String baseName = application.getSession().getServer().serverName;
        
        if(baseName.contentEquals("0")) {
        	baseName = "Forums";
        }

        if(application.stackManager.getBackStackSize(backStackId) == 0) {
        	
        	Log.d("Forum Fiend","Back stack is blank, new session");
        	
        	bundle = new Bundle();
            bundle.putString("background", background);
            
            String currentServerId = application.getSession().getServer().serverId;
    		String keyName = currentServerId + "_home_page";
    		String valueName = app_preferences.getString(keyName, getString(R.string.subforum_id));
    		
    		if(valueName.contentEquals(getString(R.string.subforum_id))) {
    			baseName = "Forums";
    		}
    		
    		if(valueName.contentEquals("forum_favs")) {
    			baseName = "Favorites";
    		}
    		
    		if(valueName.contentEquals("participated")) {
    			baseName = "Participated Topics";
    		}
    		
    		if(valueName.contentEquals("favs")) {
    			baseName = "Subscribed Topics";
    		}
    		
    		if(valueName.contentEquals("unread")) {
    			baseName = "Unread Topics";
    		}
            
            bundle.putString("subforum_id",  valueName);
            bundle.putString("subforum_name", baseName);
            bundle.putString("inTab", "N");
            
            loadForum(bundle,"NEW_SESSION",false);
            //application.stackManager.addToBackstack(backStackId, BackStackManager.BackStackItem.BACKSTACK_TYPE_FORUM,bundle);
        } else {
        	
        	Log.d("Forum Fiend","Recovering old backstack session");
        	
        	BackStackManager.BackStackItem item = application.stackManager.getActiveItemAndRemove(backStackId);
	    	
	    	switch(item.getType()) {
	    	case BackStackManager.BackStackItem.BACKSTACK_TYPE_FORUM:
	    		loadForum(item.getBundle(),"BACKSTACK_RECOVERY",true);
	    		break;
	    	case BackStackManager.BackStackItem.BACKSTACK_TYPE_TOPIC:
	    		loadTopic(item.getBundle());
	    		break;
	    	case BackStackManager.BackStackItem.BACKSTACK_TYPE_PROFILE:
	    		loadProfile(item.getBundle());
	    		break;
	    	case BackStackManager.BackStackItem.BACKSTACK_TYPE_SETTINGS:
	    		loadSettings();
	    		break;
	    	}
        }
    	

    	Bundle parms = getIntent().getExtras();
    	if(parms != null) {
    		if(parms.containsKey("stealing")) {
        		Boolean stealing = parms.getBoolean("stealing");
        		
        		if(stealing) {
        			
        			String stealingLocation = "0";
        			String stealingType = "0";
        			
        			if(parms.containsKey("stealing_location")) {
    	    			String location = parms.getString("stealing_location");
        				if(location != null) {
        					if(location != "0") {
        						stealingLocation = location;
        					}
        				}
        			}
        			
        			if(parms.containsKey("stealing_type")) {
    	    			String tealtype = parms.getString("stealing_type");
        				if(tealtype != null) {
        					if(tealtype != "0") {
        						stealingType = tealtype;
        					}
        				}
        			}
        			
        			boolean locationNumeric = isNumeric(stealingLocation);
        			
        			if(stealingType.contentEquals("forum") && locationNumeric && !stealingLocation.contentEquals("0")) {
        				Category ca = new Category();
        				ca.category_id = stealingLocation;
        				ca.category_name = "External Link";
        				ca.categoryType = "S";
        				loadCategories(ca);
        			}
        			
        			if(stealingType.contentEquals("topic") && locationNumeric && !stealingLocation.contentEquals("0")) {
        				Category ca = new Category();
        				ca.category_id = stealingLocation;
        				ca.category_name = "External Link";
        				ca.categoryType = "C";
        				loadCategories(ca);
        			}
        		}
        	}
    	}
    	
    	//Juice up gesture listener
    	gLib = GestureLibraries.fromRawResource(this, R.raw.gestures);
		if (!gLib.load()) {
			Log.w(TAG, "could not load gesture library");
			finish();
		}
		
		gestures = (GestureOverlayView) findViewById(R.id.gestures);
		gestures.addOnGesturePerformedListener(handleGestureListener);
		
		if(application.getSession().getServer().serverColor.contains("#")) {
			gestures.setUncertainGestureColor(Color.TRANSPARENT);
			gestures.setGestureColor(Color.parseColor(application.getSession().getServer().serverColor.replace("#", "#33")));
		}
    	
		gestures.setEnabled(false);
    }
    
    @SuppressWarnings("unused")
	public static boolean isNumeric(String str) {  
      try  {  
        double d = Double.parseDouble(str);  
      } catch(NumberFormatException nfe) {  
        return false;  
      }  
      return true;  
    }
    
    @Override
    public void onStart() {
      super.onStart();

    }

    @Override
    public void onStop() {
      super.onStop();

    }
    
    public void onDestroy() {
    	
    	if(!getString(R.string.server_location).contentEquals("0")) {
	    	SharedPreferences app_preferences = getSharedPreferences("prefs", 0);
	    	Editor editor = app_preferences.edit();
	        editor.putBoolean("ff_clean_close", true);
	        editor.commit();
    	}
    	
    	application.appActive = false;

    	try{
    	super.onDestroy();
    	} catch(Exception e) {
    		//who cares
    	}
    }
    
    public void onResume() {
    	
    	if(!initialLoad) {
	    	if(application.appActive) {
	    		application.getSession().setSessionListener(null);
	    		application.getSession().refreshLogin();
	    	}
    	}
    	
    	super.onResume();
    	
    	setupSidebar();

    	initialLoad = false;
    }
    
	@Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }
    
	private void setupSlidingDrawer() {
    	
    	FrameLayout flDrawer = (FrameLayout)findViewById(R.id.left_drawer);
    	
    	flDrawer.setBackgroundColor(Color.parseColor("#ffffff"));
    	
    	if(seperator == null) {
    		flSecondary.setBackgroundColor(Color.parseColor("#dddddd"));
    	}
    	
    	
    	SettingsFragment setf = new SettingsFragment();
    	setf.setOnIndexRequestedListener(goToIndex);
    	setf.setOnProfileSelectedListener(myProfileSelected);
    	setf.setOnSettingsRequestedListener(settingsRequested);
    	setf.setOnCategorySelectedListener(settingsCategorySelected);
    	
    	Bundle bundle = new Bundle();
        bundle.putString("background", background);
        setf.setArguments(bundle);
        
        FragmentManager fragmentManager=getSupportFragmentManager();
        FragmentTransaction ftZ=null;
    	ftZ = fragmentManager.beginTransaction();
		ftZ.replace(R.id.left_drawer, setf);
		ftZ.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
    	ftZ.commit();
    	
    	
    	
    	mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
    	mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow_right, GravityCompat.END);

    	mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,    
                R.drawable.ic_drawer,/* DrawerLayout object */
                R.string.drawer_open,  /* "open drawer" description */
                R.string.drawer_close  /* "close drawer" description */
                ) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                //getActionBar().setTitle(screenTitle);
                //getActionBar().setSubtitle(screenSubtitle);
                
                if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
                	invalidateOptionsMenu();
                }
            }

            /** Called when a drawer has settled in a completely open state. */

			public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                

                if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
                	invalidateOptionsMenu();
                }

            }
        };
        
        
        
        mDrawerToggle.setDrawerIndicatorEnabled(true);

        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        
        
    }


    
    
    
    
    public void onPause() {

    	DrawerLayout dl = (DrawerLayout)findViewById(R.id.drawer_layout);
    	FrameLayout flDrawer = (FrameLayout)findViewById(R.id.left_drawer);
    	
    	if(seperator == null) {
	    	if(dl.isDrawerOpen(flSecondary)) {
        		dl.closeDrawer(flSecondary);
        	}
    	}

    	if(dl.isDrawerOpen(flDrawer)) {
    		dl.closeDrawer(flDrawer);
    	}

    	super.onPause();
    	
    }
    
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        
        
        final MenuItem searchMenuItem = menu.findItem(R.id.search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchMenuItem);
        
        if(userid.contentEquals("0") || !getString(R.string.subforum_id).contentEquals("0")) {
        	searchView.setVisibility(View.GONE);
        } else {
        	if(ForegroundColorSetter.getForegroundDark(background)) {
        		searchMenuItem.setIcon(R.drawable.ic_action_search_dark);
        	}
        }

        searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View view, boolean queryTextFocused) {
                if(!queryTextFocused) {
                    searchMenuItem.collapseActionView();
                    searchView.setQuery("", false);
                }
            }
        });
        
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

			public boolean onQueryTextChange(String newText) {
				// TODO Auto-generated method stub
				return false;
			}

			public boolean onQueryTextSubmit(String query) {
				
				if(getActionBar() != null) {
					getActionBar().setSubtitle(baseSubtitle);
				}
				
				searchMenuItem.collapseActionView();
                searchView.setQuery("", false);
				
				Bundle bundle = new Bundle();
				bundle.putString("subforum_name",(String) "Search - " + query);
				bundle.putString("subforum_id",(String) "search");
				bundle.putString("query",(String) query);
				bundle.putString("background",(String) background);
				bundle.putString("icon",(String) "n/a");
				bundle.putString("inTab",(String) "N");
				
				loadForum(bundle,"SEARCH_QUERY",false);

				return false;
			}
        	
        });

        return true;
    }
	
	public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        
        String customChatForum = application.getSession().getServer().chatForum;
    	String customChatThread = application.getSession().getServer().chatThread;
        
        MenuItem newMailItem = menu.findItem(R.id.main_menu_new_mail);
        newMailItem.setVisible(false);
        
        MenuItem searchMenuItem = menu.findItem(R.id.search);
        if(userid.contentEquals("0") || !getString(R.string.subforum_id).contentEquals("0")) {
        	searchMenuItem.setVisible(false);
        }
        
        MenuItem itemLogin = menu.findItem(R.id.main_menu_open_login);
        MenuItem itemChat = menu.findItem(R.id.main_menu_open_chat);
        
        if(userid.contentEquals("0")) {
        	itemLogin.setVisible(true);
        	itemChat.setVisible(false);
        } else {
        	itemLogin.setVisible(false);
        	
        	if((!getString(R.string.chat_thread).contentEquals("0")) || (!customChatForum.contentEquals("0") && !customChatThread.contentEquals("0"))) {
        		itemChat.setVisible(true);
        	}
        }
        
        if(seperator != null) {
        	itemLogin.setVisible(false);
        	itemChat.setVisible(false);
        }
        
        if(ForegroundColorSetter.getForegroundDark(background)) {
        	itemLogin.setIcon(R.drawable.ic_action_accounts_dark);
        	itemChat.setIcon(R.drawable.ic_action_group_dark);
        	newMailItem.setIcon(R.drawable.ic_action_email_dark);

        }
        
        return true;
	}
    
    public boolean onOptionsItemSelected(MenuItem item) 
    {
    	DrawerLayout dl = (DrawerLayout)findViewById(R.id.drawer_layout);
    	FrameLayout flDrawer = (FrameLayout)findViewById(R.id.left_drawer);

        switch (item.getItemId()) 
        {
        case R.id.main_menu_new_mail:
        	Intent myIntent = new Intent(Discussions_Main.this, Mail.class);
    		startActivity(myIntent);
        	return true;
        case android.R.id.home:

        	if(dl.isDrawerOpen(flDrawer)) {
        		dl.closeDrawer(flDrawer);
        	} else {
        		dl.openDrawer(flDrawer);
        	}

        	return true;
        case R.id.main_menu_open_chat:
        	if(dl.isDrawerOpen(flSecondary)) {
        		dl.closeDrawer(flSecondary);
        	} else {
        		dl.openDrawer(flSecondary);
        	}
        	return true;
        case R.id.main_menu_open_login:
        	if(dl.isDrawerOpen(flSecondary)) {
        		dl.closeDrawer(flSecondary);
        	} else {
        		dl.openDrawer(flSecondary);
        	}
        	return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
    
    void handleSendText(Intent intent) {
        String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
        if (sharedText != null) {
        	incomingText = sharedText;
        }
    }

    void handleSendImage(Intent intent) {
        Uri imageUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
        if (imageUri != null) {
            // Update UI to reflect image being shared
        }
    }

    void handleSendMultipleImages(Intent intent) {
        ArrayList<Uri> imageUris = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
        if (imageUris != null) {
            // Update UI to reflect multiple images being shared
        }
    }
    
	public String MD5(String string) {
		
		String md5 = "";
	    try
	    {
	        MessageDigest crypt = MessageDigest.getInstance("MD5");
	        crypt.reset();
	        crypt.update(string.getBytes("UTF-8"));
	        md5 = byteToHex(crypt.digest());
	    }
	    catch(NoSuchAlgorithmException e)
	    {
	        e.printStackTrace();
	    }
	    catch(UnsupportedEncodingException e)
	    {
	        e.printStackTrace();
	    }
	    return md5;
	}
	
	public String SHA1(String string) {
		
		String sha1 = "";
	    try
	    {
	        MessageDigest crypt = MessageDigest.getInstance("SHA-1");
	        crypt.reset();
	        crypt.update(string.getBytes("UTF-8"));
	        sha1 = byteToHex(crypt.digest());
	    }
	    catch(NoSuchAlgorithmException e)
	    {
	        e.printStackTrace();
	    }
	    catch(UnsupportedEncodingException e)
	    {
	        e.printStackTrace();
	    }
	    return sha1;
	}
	
	private static String byteToHex(final byte[] hash)
	{
		String returnValue = "";
	    Formatter formatter = new Formatter();
	    for (byte b : hash)
	    {
	        formatter.format("%02x", b);
	    }
	    returnValue = formatter.toString();
	    formatter.close();
	    
	    return returnValue;
	}
	
	private CategoriesFragment.onCategorySelectedListener categorySelected = new CategoriesFragment.onCategorySelectedListener() {
		
		public void onCategorySelected(Category ca) {
			
			loadCategories(ca);
			
		}
	};
	
	private void loadCategories(Category ca) {
		
		if(ca.category_URL.contains("http")) {
			Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(ca.category_URL));
			startActivity(browserIntent);
			return;
		}
		
		if(ca.categoryType.contentEquals("C")) {
			
			String lockString = "0";
			
			if(ca.isLocked) {
				lockString = "1";
			}
			
			Bundle bundle = new Bundle();
			bundle.putString("subject",(String) ca.category_name);
			bundle.putString("category_id",(String) ca.subforum_id);
			bundle.putString("subforum_id",(String) ca.subforum_id);
			bundle.putString("thread_id",(String) ca.category_id);
			bundle.putString("lock",(String) lockString);
			bundle.putString("background",(String) background);
			bundle.putString("posts",(String) ca.thread_count);
			bundle.putString("moderator",(String) ca.categoryModerator);
			
			Log.d("Forum Fiend","Loading topic " + ca.category_id);
			
			loadTopic(bundle);
		} else {
			Bundle bundle = new Bundle();
			bundle.putString("subforum_name",(String) ca.category_name);
			bundle.putString("subforum_id",(String) ca.category_id);
			bundle.putString("background",(String) ca.categoryColor);
			bundle.putString("icon",(String) ca.categoryIcon);
			bundle.putString("inTab",(String) "N");
			
			loadForum(bundle,"LOAD_CATEGORIES",false);
		}

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    if ((keyCode == KeyEvent.KEYCODE_BACK)) {
	    	
	    	DrawerLayout dl = (DrawerLayout)findViewById(R.id.drawer_layout);
	    	FrameLayout flDrawer = (FrameLayout)findViewById(R.id.left_drawer);
	    	
	    	if(seperator == null) {
		    	if(dl.isDrawerOpen(flSecondary)) {
	        		dl.closeDrawer(flSecondary);
	        		return true;
	        	}
	    	}

	    	if(dl.isDrawerOpen(flDrawer)) {
        		dl.closeDrawer(flDrawer);
        		return true;
        	}
	    	
	    	Log.i("Forum Fiend","Back pressed, backstack size = " + application.stackManager.getBackStackSize(backStackId));
		    
		    if(application.stackManager.getBackStackSize(backStackId) > 1) {
		    	BackStackManager.BackStackItem item = application.stackManager.navigateBack(backStackId);
		    	
		    	actionBar.setSubtitle(screenSubtitle);
		    	
		    	switch(item.getType()) {
		    	case BackStackManager.BackStackItem.BACKSTACK_TYPE_FORUM:
		    		loadForum(item.getBundle(),"KEYDOWN_BACK",true);
		    		break;
		    	case BackStackManager.BackStackItem.BACKSTACK_TYPE_TOPIC:
		    		loadTopic(item.getBundle());
		    		break;
		    	case BackStackManager.BackStackItem.BACKSTACK_TYPE_PROFILE:
		    		loadProfile(item.getBundle());
		    		break;
		    	}
		    	
		    	return true;
		    }
	    }

	    return super.onKeyDown(keyCode, event);
	}
	
	
	
	
	private boolean checkURL(String theURL) {
    	boolean URLValid = false;
    	
    	int code = -1;
    	
    	URL myFileUrl = null;
    	
    	try {
			myFileUrl = new URL(theURL);
		} catch (MalformedURLException e) {
			Log.d("Forum Fiend","Bad URl");
			return URLValid;
		}
    	
    	try {
	    	HttpURLConnection huc =  ( HttpURLConnection ) myFileUrl.openConnection (); 
	    	huc.setRequestMethod ("GET");
	    	huc.setInstanceFollowRedirects(false);
	    	huc.connect(); 
	    	code = huc.getResponseCode();
    	} catch(Exception ex) {
    		Log.d("Forum Fiend","Header connection error");
    		return URLValid;
    	}
    	
    	Log.d("Forum Fiend","Return Code: " + code);
    	
    	if(code == 200) {
    		URLValid = true;
    	}
    	
    	return URLValid;
    }
	
	private class checkForumIcon extends AsyncTask<String, Void, String> {

		protected String doInBackground(String... params) {

			if(application.getSession().getServer().serverIcon.contains("http")) {
				return null;
			}
			
			String forumIconUrl = application.getSession().getServer().serverAddress + "/favicon.ico";
			
			if(checkURL(forumIconUrl)) {
				return forumIconUrl;
			}
			
			return null;
		}
		
		protected void onPostExecute(final String result) 
	    {
			if(result == null) {
				return;
			}

			application.getSession().getServer().serverIcon = result;
			application.getSession().updateServer();
	    }
	}
	
	private void setupSidebar() {
		String customChatForum = application.getSession().getServer().chatForum;
    	String customChatThread = application.getSession().getServer().chatThread;
    	
    	if(!userid.contentEquals("0") && ((!getString(R.string.chat_thread).contentEquals("0")) || (!customChatForum.contentEquals("0") && !customChatThread.contentEquals("0")))) {
    		SocialFragment sf = new SocialFragment();
    		sf.setOnProfileSelectedListener(profileSocialSelected);
    		Bundle bundle = new Bundle();
            bundle.putString("shared_text", incomingText);
            bundle.putString("background", background);
    		sf.setArguments(bundle);

    		FragmentManager fragmentManager=getSupportFragmentManager();
            FragmentTransaction ftZ = null;
        	ftZ = fragmentManager.beginTransaction();
    		ftZ.replace(R.id.main_page_frame_right, sf);
    		ftZ.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        	ftZ.commit();


    	} else if(userid.contentEquals("0")) {
    		Login login = new Login();
    		
    		FragmentManager fragmentManager=getSupportFragmentManager();
            FragmentTransaction ftZ = null;
        	ftZ = fragmentManager.beginTransaction();
    		ftZ.replace(R.id.main_page_frame_right, login);
    		ftZ.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        	ftZ.commit();
    	} else {
    		if(sidebarOption) {
	    		ActiveList active = new ActiveList();
	    		active.setOnProfileSelectedListener(profileActiveSelected);
	    		
	    		FragmentManager fragmentManager=getSupportFragmentManager();
	            FragmentTransaction ftZ = null;
	        	ftZ = fragmentManager.beginTransaction();
	    		ftZ.replace(R.id.main_page_frame_right, active);
	    		ftZ.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
	    		
	    		try {
	    			ftZ.commit();
	    		} catch(Exception ex) {
	    			if(ex.getMessage() != null) {
	    				Log.w("Forum Fiend",ex.getMessage());
	    			}
	    		}
    		} else {
    			flSecondary.setVisibility(View.GONE);
    		}
    	}

	}

	private PostsFragment.onProfileSelectedListener profileSelected = new PostsFragment.onProfileSelectedListener() {
		
		@Override
		public void onProfileSelected(String username, String userid) {
			
			getActionBar().setSubtitle(baseSubtitle);
			
			Log.i("Forum Fiend","Preparing fragment for profile " + userid);
			
			Bundle bundle = new Bundle();
			bundle.putString("username", username);
			bundle.putString("userid", userid);
			loadProfile(bundle);
		}
	};
	
	private ActiveList.onProfileSelectedListener profileActiveSelected = new ActiveList.onProfileSelectedListener() {
		
		@Override
		public void onProfileSelected(String username, String userid) {
			
			Bundle bundle = new Bundle();
			bundle.putString("username", username);
			bundle.putString("userid", userid);
			
			loadProfile(bundle);
	    	
	    	if(seperator == null) {
		    	DrawerLayout dl = (DrawerLayout)findViewById(R.id.drawer_layout);
		    	if(dl.isDrawerOpen(flSecondary)) {
	        		dl.closeDrawer(flSecondary);
	        	}
	    	}

		}
	};
	
	private SettingsFragment.onProfileSelectedListener myProfileSelected = new SettingsFragment.onProfileSelectedListener() {
		
		@Override
		public void onProfileSelected(String username, String userid) {
			
			Bundle bundle = new Bundle();
			bundle.putString("username", username);
			bundle.putString("userid", userid);
			
			loadProfile(bundle);

	    	DrawerLayout dl = (DrawerLayout)findViewById(R.id.drawer_layout);
	    	FrameLayout flDrawer = (FrameLayout)findViewById(R.id.left_drawer);
	    	
	    	if(dl.isDrawerOpen(flDrawer)) {
        		dl.closeDrawer(flDrawer);
        	}


		}
	};
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	private SocialFragment.onProfileSelectedListener profileSocialSelected = new SocialFragment.onProfileSelectedListener() {
		
		@Override
		public void onProfileSelected(String username, String userid) {
			
			Bundle bundle = new Bundle();
			bundle.putString("username", username);
			bundle.putString("userid", userid);
			
			loadProfile(bundle);

	    	if(seperator == null) {
		    	DrawerLayout dl = (DrawerLayout)findViewById(R.id.drawer_layout);
		    	if(dl.isDrawerOpen(flSecondary)) {
	        		dl.closeDrawer(flSecondary);
	        	}
	    	}

		}
	};
	
	private void loadForum(Bundle bundle,String sender,Boolean isBackNav) {
		
		if(!isBackNav) {
			SharedPreferences app_preferences = getSharedPreferences("prefs", 0);
			SharedPreferences.Editor editor = app_preferences.edit();
	        editor.putString(storagePrefix + "forumScrollPosition" + bundle.getString("subforum_id"), "0");
	        editor.commit();
		}
		
		CategoriesFragment cf = new CategoriesFragment();
        cf.setOnCategorySelectedListener(categorySelected);
        cf.setArguments(bundle);
        
        FragmentManager fragmentManager=getSupportFragmentManager();
        FragmentTransaction ftZ=null;
    	ftZ = fragmentManager.beginTransaction();
		ftZ.replace(R.id.main_page_frame_primary, cf);
		ftZ.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
    	ftZ.commit();

    	application.stackManager.addToBackstack(backStackId, BackStackManager.BackStackItem.BACKSTACK_TYPE_FORUM,bundle);
    	
    	Log.i("Forum Fiend","Loading Forum from " + sender);
	}
	
	private void loadTopic(Bundle bundle) {
		PostsFragment pf = new PostsFragment();
        pf.setOnProfileSelectedListener(profileSelected);
        pf.setArguments(bundle);
        
        FragmentManager fragmentManager=getSupportFragmentManager();
        FragmentTransaction ftZ=null;
    	ftZ = fragmentManager.beginTransaction();
		ftZ.replace(R.id.main_page_frame_primary, pf);
		ftZ.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
    	ftZ.commit();
    	
    	application.stackManager.addToBackstack(backStackId, BackStackManager.BackStackItem.BACKSTACK_TYPE_TOPIC,bundle);
	}
	
	private void loadProfile(Bundle bundle) {
		ProfileFragment pf = new ProfileFragment();
        pf.setArguments(bundle);
		
		FragmentManager fragmentManager=getSupportFragmentManager();
        FragmentTransaction ftZ=null;
    	ftZ = fragmentManager.beginTransaction();
		ftZ.replace(R.id.main_page_frame_primary, pf);
		ftZ.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
    	ftZ.commit();
    	
    	application.stackManager.addToBackstack(backStackId, BackStackManager.BackStackItem.BACKSTACK_TYPE_PROFILE,bundle);
	}
	
	private SettingsFragment.onIndexRequestedListener goToIndex = new SettingsFragment.onIndexRequestedListener() {

		@Override
		public void onIndexRequested() {
			
			SharedPreferences app_preferences = getSharedPreferences("prefs", 0);

			String currentServerId = application.getSession().getServer().serverId;
    		String keyName = currentServerId + "_home_page";
    		String valueName = app_preferences.getString(keyName, getString(R.string.subforum_id));

			
            if(valueName.contentEquals(getString(R.string.subforum_id))) {

				BackStackManager.BackStackItem item = application.stackManager.navigateToBase(backStackId);
		    	
		    	switch(item.getType()) {
			    	case BackStackManager.BackStackItem.BACKSTACK_TYPE_FORUM:
			    		loadForum(item.getBundle(),"SETTINGS_INDEX_REQUESTED",false);
			    		break;
			    	case BackStackManager.BackStackItem.BACKSTACK_TYPE_TOPIC:
			    		loadTopic(item.getBundle());
			    		break;
			    	case BackStackManager.BackStackItem.BACKSTACK_TYPE_PROFILE:
			    		loadProfile(item.getBundle());
			    		break;
			    	case BackStackManager.BackStackItem.BACKSTACK_TYPE_SETTINGS:
			    		loadSettings();
			    		break;
			    	}
            } else {
            	Category ca = new Category();
				ca.category_id = getString(R.string.subforum_id);
				ca.category_name = "Forums";
				ca.categoryType = "S";
				loadCategories(ca);
            }
	    	
	    	DrawerLayout dl = (DrawerLayout)findViewById(R.id.drawer_layout);
	    	FrameLayout flDrawer = (FrameLayout)findViewById(R.id.left_drawer);

	    	if(dl.isDrawerOpen(flDrawer)) {
        		dl.closeDrawer(flDrawer);
        	}
		}
		
	};
	
	private SettingsFragment.onSettingsRequestedListener settingsRequested = new SettingsFragment.onSettingsRequestedListener() {

		@Override
		public void onSettingsRequested() {
			loadSettings();
		}
	};
	
	private void loadSettings() {
		ForumSettingsFragment pf = new ForumSettingsFragment();

		FragmentManager fragmentManager=getSupportFragmentManager();
        FragmentTransaction ftZ=null;
    	ftZ = fragmentManager.beginTransaction();
		ftZ.replace(R.id.main_page_frame_primary, pf);
		ftZ.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
    	ftZ.commit();
    	
    	DrawerLayout dl = (DrawerLayout)findViewById(R.id.drawer_layout);
    	FrameLayout flDrawer = (FrameLayout)findViewById(R.id.left_drawer);

    	if(dl.isDrawerOpen(flDrawer)) {
    		dl.closeDrawer(flDrawer);
    	}
    	
    	application.stackManager.addToBackstack(backStackId, BackStackManager.BackStackItem.BACKSTACK_TYPE_SETTINGS,null);
	}
	
	private SettingsFragment.onCategorySelectedListener settingsCategorySelected = new SettingsFragment.onCategorySelectedListener() {
		
		public void onCategorySelected(Category ca) {
			
			loadCategories(ca);
			
			
			DrawerLayout dl = (DrawerLayout)findViewById(R.id.drawer_layout);
	    	FrameLayout flDrawer = (FrameLayout)findViewById(R.id.left_drawer);

	    	if(dl.isDrawerOpen(flDrawer)) {
	    		dl.closeDrawer(flDrawer);
	    	}
		}
	};

	private OnGesturePerformedListener handleGestureListener = new OnGesturePerformedListener() {
		public void onGesturePerformed(GestureOverlayView gestureView,Gesture gesture) {
			ArrayList<Prediction> predictions = gLib.recognize(gesture);
 
			// one prediction needed
			if (predictions.size() > 0) {
				Prediction prediction = predictions.get(0);
				// checking prediction
				if (prediction.score > 1.0) {

					if(prediction.name.contains("reload")) {
						// perform reload action
					}
					
					if(prediction.name.contains("logout")) {
						// perform logout action
					}
					
					if(prediction.name.contains("prev")) {
						// perform next action
					}
					
					if(prediction.name.contains("new")) {
						// perform new action
					}
					
					if(prediction.name.contains("next")) {
						// perform next action
					}
				}
			}
 
		}
	};

}