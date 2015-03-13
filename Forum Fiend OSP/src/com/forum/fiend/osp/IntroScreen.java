package com.forum.fiend.osp;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.AsyncTask.Status;
import android.support.v4.app.FragmentActivity;

import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


@SuppressLint("NewApi")
public class IntroScreen extends FragmentActivity {
	
	private AnalyticsHelper ah;
	
	private EditText serverInputter;
	private ListView lvServers;
	private GridView gvServers;
	
	private boolean isStealingLink = false;

	private Server selectedServer;

	private ProgressDialog progress;

	
	private String preinstalledServers = "http://forum.forumfiend.net";
	private SQLiteDatabase notetasticDB;
	private String sql;
	private ArrayList<Server> serverList;
	
	private boolean incomingShortcut = false;
	private String shortcutServerId = "0";
	
	@SuppressLint("NewApi")
	public void onCreate(Bundle savedInstanceState) {
		
		Bundle bundle = getIntent().getExtras();
		if(bundle != null) {

			if(bundle.containsKey("server_id")) {
				
				if(bundle.getString("server_id") != null) {

					incomingShortcut = true;
					shortcutServerId = bundle.getString("server_id");
				}
			}
		}
		
		ForumFiendApp app = (ForumFiendApp)getApplication();
		app.initSession();

		startService(new Intent(this,MailService.class));
		initDatabase();
		
		SharedPreferences app_preferences = getSharedPreferences("prefs", 0);
		
		
		
		this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		
		SharedPreferences.Editor editor = app_preferences.edit();
		editor.putString("server_address", getString(R.string.server_location));
		editor.commit();
		
		String backgroundColor = app.getSession().getServer().serverColor;
		
		ThemeSetter.setTheme(this,backgroundColor);
		
		super.onCreate(savedInstanceState);
		
        ThemeSetter.setActionBar(this,backgroundColor);

        //Track app analytics
      	ah = ((ForumFiendApp)getApplication()).getAnalyticsHelper();
      	ah.trackScreen(getString(R.string.app_name) + " v" + getString(R.string.app_version) + " for Android", true);
        
        setContentView(R.layout.intro_screen);
        
        serverInputter = (EditText) findViewById(R.id.intro_screen_add_server_box);
        
        Button serverAdder = (Button) findViewById(R.id.intro_screen_submit_new_server);
        
        serverAdder.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				
				if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
					new validateServer().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,serverInputter.getText().toString().trim());
				} else {
					new validateServer().execute(serverInputter.getText().toString().trim());
				}

			}
		});
        
        lvServers = (ListView) findViewById(R.id.intro_screen_server_list);
        gvServers = (GridView) findViewById(R.id.intro_screen_server_grid);

        if(lvServers == null) {
            registerForContextMenu(gvServers);
            gvServers.setOnItemClickListener(new OnItemClickListener() {
    		    public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
    		    	Server server = serverList.get(position);
    		    	connectToServer(server);
    		    }
            });
        } else {
        	lvServers.setDivider(null);
            registerForContextMenu(lvServers);
            lvServers.setOnItemClickListener(new OnItemClickListener() {
    		    public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
    		    	Server server = serverList.get(position);
    		    	connectToServer(server);
    		    }
            });
        }
        
        
        TextView tvTapaShoutout = (TextView) findViewById(R.id.intro_screen_tapatalk_notice);
        tvTapaShoutout.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/bastecklein/forum-fiend-osp"));
				startActivity(browserIntent);
			}
		});
        
        //Check for incoming link from tapatalk :-)
        String host = "";
        
        Uri data = getIntent().getData();
        
        if(data != null) {
            host = data.getHost();
            stealingType = data.getQueryParameter("location");
            
            if(stealingType == null) {
            	stealingType = "0";
            } else {
            	if(stealingType.contentEquals("forum")) {
            		String forumId = data.getQueryParameter("fid");
            		
            		if(forumId == null) {
            			stealingLocation = "0";
            		} else {
            			stealingLocation = forumId;
            		}
            	}
            	
            	if(stealingType.contentEquals("topic")) {
            		String topicId = data.getQueryParameter("tid");
            		
            		if(topicId == null) {
            			stealingLocation = "0";
            		} else {
            			stealingLocation = topicId;
            		}
            	}
            }

        }
        
        if(host.length() > 0) {
        	linkToSteal = host;
        	stealingLink = true;
        	return;
        }

	}
	
	private boolean stealingLink = false;
	private String linkToSteal = "0";
	private String stealingType = "0";
	private String stealingLocation = "0";
	
	@Override
	public void onResume() {
		super.onResume();
		
		ForumFiendApp app = (ForumFiendApp)getApplication();
		app.initSession();
		app.appActive = false;
		
		RelativeLayout connectingLayout = (RelativeLayout)findViewById(R.id.intro_connecting_layout);
		connectingLayout.setVisibility(View.GONE);
		
		getActionBar().show();
		
		if(stealingLink) {
			stealTapatalkLink(linkToSteal);
			return;
		}
		
		if(getString(R.string.server_location).contentEquals("0") && !incomingShortcut) {
        	refreshList();
        } else {
        	connectToServer(selectedServer);
        }
		
		TextView tvUpgrade = (TextView)findViewById(R.id.intro_screen_remove_ads);
		tvUpgrade.setVisibility(View.GONE);
	}
	
	@Override
    public void onStart() {
      super.onStart();
    }

    @Override
    public void onStop() {
      super.onStop();
    }
    
    @Override
    public void onDestroy() {
    	
    	if(getString(R.string.server_location).contentEquals("0")) {
	    	SharedPreferences app_preferences = getSharedPreferences("prefs", 0);
	    	Editor editor = app_preferences.edit();
	        editor.putBoolean("ff_clean_close", true);
	        editor.commit();
    	}

		super.onDestroy();
    }
    
    private ArrayList<CheckForumManifest> runningManifestChecks;
	
    private void refreshList() {
    	
    	runningManifestChecks = new ArrayList<CheckForumManifest>();
    	
    	serverList = new ArrayList<Server>();
    	
    	notetasticDB = this.openOrCreateDatabase("forumfiend", 0, null);
    	
    	sql = "select * from accountlist;";
          
      	Cursor c = notetasticDB.rawQuery(sql,null);
      	
      	if(c == null) {
      		return;
      	}

    	while(c.moveToNext()) {
    		Server server = parseServerData(c);
    		CheckForumManifest manifestCheck = new CheckForumManifest();
    		runningManifestChecks.add(manifestCheck);
    		manifestCheck.execute(server);
    		serverList.add(server);
    	}
    	
    	notetasticDB.close();
    	
    	if(lvServers == null) {
    		gvServers.setAdapter(new ServerAdapter(serverList,this));
    	} else {
    		lvServers.setAdapter(new ServerAdapter(serverList,this));
    	}
    	

    }
    
    public void onCreateContextMenu(ContextMenu menu, View v,ContextMenuInfo menuInfo) {
    	
    	AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
    	
    	if(lvServers == null) {
    		selectedServer = (Server) gvServers.getAdapter().getItem(info.position);
    	} else {
    		selectedServer = (Server) lvServers.getAdapter().getItem(info.position);
    	}
    	
    	
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.setHeaderTitle(selectedServer.serverAddress);
		MenuInflater inflater = getMenuInflater();
		
		inflater.inflate(R.menu.intro_context, menu);
    }
    
    public boolean onContextItemSelected(MenuItem item) {
		  switch (item.getItemId()) 
		  {
		  case R.id.intro_context_remove:
			  removeServer(selectedServer);
			  return true;
		  case R.id.intro_shortcut:
			  createHomescreenShortcut(selectedServer);
			  return true;
		  case R.id.intro_context_rename:
			  renameServer(selectedServer);
			  return true;
		  default:
			  return super.onContextItemSelected(item);
		  }
	}

    private String checkForTapatalk(String enteredURL) {
    	String tapatalkUrl = "none";
    	
    	String baseURL = enteredURL;
    	
    	if(!baseURL.contains("http")) {
    		baseURL = "http://" + enteredURL;
    	}
    	
    	String attemptURL;
    	
    	attemptURL = baseURL + "/mobiquo/mobiquo.php";
    	if(checkURL(attemptURL)) {
    		return attemptURL.replace("/mobiquo/mobiquo.php", "");
    	}
    	
    	attemptURL = baseURL + "/forums/mobiquo/mobiquo.php";
    	if(checkURL(attemptURL)) {
    		return attemptURL.replace("/mobiquo/mobiquo.php", "");
    	}
    	
    	attemptURL = baseURL + "/forum/mobiquo/mobiquo.php";
    	if(checkURL(attemptURL)) {
    		return attemptURL.replace("/mobiquo/mobiquo.php", "");
    	}
    	
    	attemptURL = baseURL + "/board/mobiquo/mobiquo.php";
    	if(checkURL(attemptURL)) {
    		return attemptURL.replace("/mobiquo/mobiquo.php", "");
    	}
    	
    	attemptURL = baseURL + "/community/mobiquo/mobiquo.php";
    	if(checkURL(attemptURL)) {
    		return attemptURL.replace("/mobiquo/mobiquo.php", "");
    	}
    	
    	if(!baseURL.contains("http://www.")) {
	    	baseURL = baseURL.replace("http://", "http://www.");
	    	
	    	attemptURL = baseURL + "/mobiquo/mobiquo.php";

	    	if(checkURL(attemptURL)) {
	    		return attemptURL.replace("/mobiquo/mobiquo.php", "");
	    	}
	    	
	    	attemptURL = baseURL + "/forums/mobiquo/mobiquo.php";

	    	if(checkURL(attemptURL)) {
	    		return attemptURL.replace("/mobiquo/mobiquo.php", "");
	    	}
	    	
	    	attemptURL = baseURL + "/forum/mobiquo/mobiquo.php";

	    	if(checkURL(attemptURL)) {
	    		return attemptURL.replace("/mobiquo/mobiquo.php", "");
	    	}
	    	
	    	attemptURL = baseURL + "/board/mobiquo/mobiquo.php";

	    	if(checkURL(attemptURL)) {
	    		return attemptURL.replace("/mobiquo/mobiquo.php", "");
	    	}
	    	
	    	attemptURL = baseURL + "/community/mobiquo/mobiquo.php";

	    	if(checkURL(attemptURL)) {
	    		return attemptURL.replace("/mobiquo/mobiquo.php", "");
	    	}
    	}
    	
    	
    	
    	baseURL = baseURL.replace("http://www.", "http://forum.");
    	
    	attemptURL = baseURL + "/mobiquo/mobiquo.php";

    	if(checkURL(attemptURL)) {
    		return attemptURL.replace("/mobiquo/mobiquo.php", "");
    	}
    	
    	attemptURL = baseURL + "/forums/mobiquo/mobiquo.php";

    	if(checkURL(attemptURL)) {
    		return attemptURL.replace("/mobiquo/mobiquo.php", "");
    	}
    	
    	attemptURL = baseURL + "/forum/mobiquo/mobiquo.php";

    	if(checkURL(attemptURL)) {
    		return attemptURL.replace("/mobiquo/mobiquo.php", "");
    	}
    	
    	attemptURL = baseURL + "/board/mobiquo/mobiquo.php";

    	if(checkURL(attemptURL)) {
    		return attemptURL.replace("/mobiquo/mobiquo.php", "");
    	}
    	
    	attemptURL = baseURL + "/community/mobiquo/mobiquo.php";

    	if(checkURL(attemptURL)) {
    		return attemptURL.replace("/mobiquo/mobiquo.php", "");
    	}
    	
    	baseURL = baseURL.replace("http://forum.", "http://forums.");
    	
    	attemptURL = baseURL + "/mobiquo/mobiquo.php";

    	if(checkURL(attemptURL)) {
    		return attemptURL.replace("/mobiquo/mobiquo.php", "");
    	}
    	
    	attemptURL = baseURL + "/forums/mobiquo/mobiquo.php";

    	if(checkURL(attemptURL)) {
    		return attemptURL.replace("/mobiquo/mobiquo.php", "");
    	}
    	
    	attemptURL = baseURL + "/forum/mobiquo/mobiquo.php";

    	if(checkURL(attemptURL)) {
    		return attemptURL.replace("/mobiquo/mobiquo.php", "");
    	}
    	
    	attemptURL = baseURL + "/board/mobiquo/mobiquo.php";

    	if(checkURL(attemptURL)) {
    		return attemptURL.replace("/mobiquo/mobiquo.php", "");
    	}
    	
    	attemptURL = baseURL + "/community/mobiquo/mobiquo.php";

    	if(checkURL(attemptURL)) {
    		return attemptURL.replace("/mobiquo/mobiquo.php", "");
    	}
    	
    	baseURL = baseURL.replace("http://forums.", "http://board.");
    	
    	attemptURL = baseURL + "/mobiquo/mobiquo.php";

    	if(checkURL(attemptURL)) {
    		return attemptURL.replace("/mobiquo/mobiquo.php", "");
    	}
    	
    	attemptURL = baseURL + "/forums/mobiquo/mobiquo.php";

    	if(checkURL(attemptURL)) {
    		return attemptURL.replace("/mobiquo/mobiquo.php", "");
    	}
    	
    	attemptURL = baseURL + "/forum/mobiquo/mobiquo.php";

    	if(checkURL(attemptURL)) {
    		return attemptURL.replace("/mobiquo/mobiquo.php", "");
    	}
    	
    	attemptURL = baseURL + "/board/mobiquo/mobiquo.php";

    	if(checkURL(attemptURL)) {
    		return attemptURL.replace("/mobiquo/mobiquo.php", "");
    	}
    	
    	attemptURL = baseURL + "/community/mobiquo/mobiquo.php";

    	if(checkURL(attemptURL)) {
    		return attemptURL.replace("/mobiquo/mobiquo.php", "");
    	}
    	
    	baseURL = baseURL.replace("http://board.", "http://discussions.");
    	
    	attemptURL = baseURL + "/mobiquo/mobiquo.php";

    	if(checkURL(attemptURL)) {
    		return attemptURL.replace("/mobiquo/mobiquo.php", "");
    	}
    	
    	attemptURL = baseURL + "/forums/mobiquo/mobiquo.php";

    	if(checkURL(attemptURL)) {
    		return attemptURL.replace("/mobiquo/mobiquo.php", "");
    	}
    	
    	attemptURL = baseURL + "/forum/mobiquo/mobiquo.php";

    	if(checkURL(attemptURL)) {
    		return attemptURL.replace("/mobiquo/mobiquo.php", "");
    	}
    	
    	attemptURL = baseURL + "/board/mobiquo/mobiquo.php";

    	if(checkURL(attemptURL)) {
    		return attemptURL.replace("/mobiquo/mobiquo.php", "");
    	}
    	
    	attemptURL = baseURL + "/community/mobiquo/mobiquo.php";

    	if(checkURL(attemptURL)) {
    		return attemptURL.replace("/mobiquo/mobiquo.php", "");
    	}
    	
    	return tapatalkUrl;
    }
    
    private boolean checkURL(String theURL) {
    	
    	Log.d("Forum Fiend","Checking: " + theURL);
    	
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
    		if(ex.getMessage() != null) {
    			Log.e("Forum Fiend",ex.getMessage());
    		} else {
    			Log.e("Forum Fiend","Header connection error");
    		}
    		
    		return URLValid;
    	}

    	if(code == 200) {
    		URLValid = true;
    	}
    	
    	return URLValid;
    }

    private class validateServer extends AsyncTask<String, Void, String> {

    	protected void onPreExecute() {
    		progress = ProgressDialog.show(IntroScreen.this, "Please Wait","Validating server information, please wait.", true);
    	}
    	
		@Override
		protected String doInBackground(String... params) {
			String validServer = checkForTapatalk(params[0]);
			
			return validServer;
		}
		
		protected void onPostExecute(String result) {
			
			try {
				progress.dismiss();
			} catch(Exception ex) {
				//what evs
			}
			
			if(result.contentEquals("none")) {

				if(isStealingLink) {
					finish();
					return;
				}
				
				askAboutWebview();
				
				return;
			}
			
			addNewServer(result);
			refreshList();
			
			serverInputter.setText("");
			
			InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(serverInputter.getWindowToken(), 0);

			if(isStealingLink) {
				
				SharedPreferences app_preferences = getSharedPreferences("prefs", 0);
				SharedPreferences.Editor editor = app_preferences.edit();
				
				String the_result = result;
		    	
		    	if(!the_result.contains("http")) {
		    		the_result = "http://" + the_result;
		    	}

				editor.putString("server_address", the_result);
				editor.putString(the_result + "_forumScrollPosition0", "0");
				editor.commit();
		    	
				Intent myIntent = new Intent(IntroScreen.this, Discussions_Main.class);
				startActivity(myIntent);
				
				return;
			}
		}
    	
    }
    
    private void stealTapatalkLink(String link) {

    	if(!getString(R.string.server_location).contentEquals("0")) {
    		
    		String queryLink = link;
    		if(!queryLink.contains("http")) {
    			queryLink = "http://" + link;
    		}
    		
    		if(queryLink.contentEquals(getString(R.string.server_location))) {
    			connectToServer(selectedServer);
    			return;
    		} else {
    			final AlertDialog.Builder builder = new AlertDialog.Builder(IntroScreen.this);
                builder.setTitle("Download Forum Fiend");
                builder.setCancelable(true);
                builder.setPositiveButton("Yep!", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    	
                    	String fiendURL = "http://apps.ape-apps.com/forum-fiend-osp/";

                    	
                    	Intent intent = new Intent(Intent.ACTION_VIEW); 
                		intent.setData(Uri.parse(fiendURL)); 
                		IntroScreen.this.startActivity(intent);
                		
                		finish();
                    }
                });
                builder.setNegativeButton("Nah.", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    	finish();
                    }
                });
                builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    public void onCancel(DialogInterface dialog) {
                    	finish();
                    }
                });

                builder.setMessage("Do you want to download and view " + link + " using Forum Fiend, the free mobile forum reader app that " + getString(R.string.app_name) + " is based off of?");
                builder.create().show();
        		
            	return;
    		}
    		
    		
        }
    	
		String queryLink = link;
		if(!queryLink.contains("http")) {
			queryLink = "http://" + link;
		}

		String cleanServer = DatabaseUtils.sqlEscapeString(queryLink);

		notetasticDB = this.openOrCreateDatabase("forumfiend", 0, null);
    	
    	sql = "select * from accountlist where server = " + cleanServer + " limit 1;";
          
      	Cursor c = notetasticDB.rawQuery(sql,null);
      	
      	if(c != null) {
	    	while(c.moveToNext()) {
	    		selectedServer = parseServerData(c);
	    	}
      	}
    	
    	notetasticDB.close();
		
		if(selectedServer != null) {
			if(!selectedServer.serverAddress.contentEquals("0")) {
				connectToServer(selectedServer);
				return;
			}
		}
		
		if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
			new validateServer().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,link.trim());
		} else {
			new validateServer().execute(link.trim());
		}

    }
    
 
	private void initDatabase() {
		
		
		
		String uvalue = DatabaseUtils.sqlEscapeString(getString(R.string.default_background));
		String uBoxColor = DatabaseUtils.sqlEscapeString(getString(R.string.default_element_background));
		String uBoxBorder = DatabaseUtils.sqlEscapeString(getString(R.string.default_element_border));
		String uTextColor = DatabaseUtils.sqlEscapeString(getString(R.string.default_text_color));
		String uDividerColor = DatabaseUtils.sqlEscapeString(getString(R.string.default_divider_color));
		String uWallpaper = DatabaseUtils.sqlEscapeString(getString(R.string.default_wallpaper_url));
		
		String uFFChat = DatabaseUtils.sqlEscapeString(getString(R.string.new_chat_id));
		String uAnalytics = DatabaseUtils.sqlEscapeString("0");
		String uMobfox = DatabaseUtils.sqlEscapeString("0");
		
	 	notetasticDB = this.openOrCreateDatabase("forumfiend", MODE_PRIVATE, null);
	 	
	 	Log.d("Forum Fiend","Opening database version " + notetasticDB.getVersion());
	 	
	 	sql = "create table if not exists accountlist(_id integer primary key,server varchar,icon varchar,color varchar,servername varchar,username varchar,password varchar,userid varchar,avatar varchar,postcount varchar,themeInt varchar,cookieCount varchar,lastTab varchar,tagline varchar,chatThread varchar,chatForum varchar,chatName varchar,background varchar default " + uvalue + ",boxcolor varchar default " + uBoxColor + ",boxborder varchar default " + uBoxBorder + ",textcolor varchar default " + uTextColor + ",dividercolor varchar default " + uDividerColor + ",wallpaper varchar default " + uWallpaper + ",ffchat varchar default " + uFFChat + ",analytics varchar default " + uAnalytics + ",mobfox varchar default " + uMobfox + ");";
		
	 	//conduct upgrade from initial database
	 	if(notetasticDB.getVersion() == 1) {
	 		sql = "alter table accountlist add column background varchar default " + uvalue + ";";
	 		notetasticDB.execSQL(sql);
	 		
	 		sql = "alter table accountlist add column boxcolor varchar default " + uBoxColor + ";";
	 		notetasticDB.execSQL(sql);
	 		
	 		sql = "alter table accountlist add column boxborder varchar default " + uBoxBorder + ";";
	 		notetasticDB.execSQL(sql);
	 		
	 		sql = "alter table accountlist add column textcolor varchar default " + uTextColor + ";";
	 		notetasticDB.execSQL(sql);
	 		
	 		sql = "alter table accountlist add column dividercolor varchar default " + uDividerColor + ";";
	 		notetasticDB.execSQL(sql);
	 		
	 		sql = "alter table accountlist add column wallpaper varchar default " + uWallpaper + ";";
	 		notetasticDB.execSQL(sql);
	 		
	 		sql = "alter table accountlist add column ffchat varchar default " + uFFChat + ";";
	 		notetasticDB.execSQL(sql);
	 		
	 		sql = "alter table accountlist add column analytics varchar default " + uAnalytics + ";";
	 		notetasticDB.execSQL(sql);
	 		
	 		sql = "alter table accountlist add column mobfox varchar default " + uMobfox + ";";
	 		
    		Log.d("Forum Fiend","Database upgraded to v4...");
	 	}
	 	
	 	if(notetasticDB.getVersion() == 2) {
	 		try {
	 			sql = "alter table accountlist add column ffchat varchar default " + uFFChat + ";";
	 			notetasticDB.execSQL(sql);
	 		} catch(Exception ex) {
	 			//oh well
	 		}
	 		sql = "alter table accountlist add column analytics varchar default " + uAnalytics + ";";
	 		notetasticDB.execSQL(sql);
	 		
	 		sql = "alter table accountlist add column mobfox varchar default " + uMobfox + ";";
	 		
	 		Log.d("Forum Fiend","Database upgraded to v4...");
	 	}
	 	
	 	if(notetasticDB.getVersion() == 3) {
	 		sql = "alter table accountlist add column analytics varchar default " + uAnalytics + ";";
	 		notetasticDB.execSQL(sql);
	 		
	 		sql = "alter table accountlist add column mobfox varchar default " + uMobfox + ";";
	 		
	 		Log.d("Forum Fiend","Database upgraded to v4...");
	 	}
	 	
	 	notetasticDB.setVersion(4);
	 	
	 	try {
	 		notetasticDB.execSQL(sql);
	 	} catch(Exception ex) {
	 		
	 	}
		notetasticDB.close();
		
		upgradeToDatabase();

		//Juice up the server for stand alone app
		if(!getString(R.string.server_location).contentEquals("0") || incomingShortcut) {
			notetasticDB = this.openOrCreateDatabase("forumfiend", 0, null);
			
			if(incomingShortcut) {
				String cleanServer = DatabaseUtils.sqlEscapeString(shortcutServerId);
				sql = "select * from accountlist where _id = " + cleanServer + "limit 1;";
			} else {
				sql = "select * from accountlist limit 1;";
			}
			
	    	
	          
	      	Cursor c = notetasticDB.rawQuery(sql,null);
	      	
	      	if(c == null) {
	      		return;
	      	}

	    	while(c.moveToNext()) {
	    		selectedServer = parseServerData(c);
	    	}
	    	
	    	notetasticDB.close();
		}
	}
	
	private void upgradeToDatabase() {
		
		String[] serverListing;
		
		SharedPreferences app_preferences = getSharedPreferences("prefs", 0);
		boolean dbCreated = app_preferences.getBoolean("dbInit", false);
		
		if(!dbCreated) {
			notetasticDB = this.openOrCreateDatabase("forumfiend", MODE_PRIVATE, null);
			
			String storagePrefix = "";
			
			if(getString(R.string.server_location).contentEquals("0")) {
				String rawServers = app_preferences.getString("serverListing", preinstalledServers);
				serverListing = rawServers.split(",");
	        } else {
	        	serverListing = new String[1];
	        	serverListing[0] = getString(R.string.server_location);
	        }
			
			for(String server:serverListing) {
				
				String raw = server;

				if(!raw.contains("http")) {
					raw = "http://" + raw;
		    	}
				
				storagePrefix = raw + "_";
				
				if(getString(R.string.server_location).contentEquals("0")) {
					storagePrefix = "";
				}
				
				String userid = app_preferences.getString(storagePrefix + "logged_userid", "0");
				String username = app_preferences.getString(storagePrefix + "logged_username", "0");
				String password = app_preferences.getString(storagePrefix + "logged_password", "0");
		        String tagline = app_preferences.getString(storagePrefix + "logged_tagline", "null");
		        
		        String avatar = app_preferences.getString(storagePrefix + "logged_avatar", "0");
		        String postcount = app_preferences.getString(storagePrefix + "logged_postcount", "0");
		        String color = app_preferences.getString(storagePrefix + "logged_bgColor", getString(R.string.default_color));
		        
		        int cookies = app_preferences.getInt(storagePrefix + "cookie_count", 0);
		        int theme = app_preferences.getInt(storagePrefix + "loggedThemeInt", Integer.parseInt(getString(R.string.default_theme)));
		        int tab = app_preferences.getInt(storagePrefix + "last_main_tab", 0);
		        
		        String chatThread = app_preferences.getString(storagePrefix + "_custom_chat_thread", getString(R.string.chat_thread));
		        String chatForum = app_preferences.getString(storagePrefix + "_custom_chat_forum", getString(R.string.chat_forum));
		        String chatName = app_preferences.getString(storagePrefix + "_custom_chat_name", getString(R.string.chat_name));

		        String cleanServer = DatabaseUtils.sqlEscapeString(raw);
		        String cleanUserid = DatabaseUtils.sqlEscapeString(userid);
		        String cleanUsername = DatabaseUtils.sqlEscapeString(username);
		        String cleanPassword = DatabaseUtils.sqlEscapeString(password);
		        String cleanTagline = DatabaseUtils.sqlEscapeString(tagline);
		        String cleanAvatar = DatabaseUtils.sqlEscapeString(avatar);
		        String cleanPostcount = DatabaseUtils.sqlEscapeString(postcount);
		        String cleanColor = DatabaseUtils.sqlEscapeString(color);
		        String cleanCookies = DatabaseUtils.sqlEscapeString(Integer.toString(cookies));
		        String cleanTheme = DatabaseUtils.sqlEscapeString(Integer.toString(theme));
		        String cleanTab = DatabaseUtils.sqlEscapeString(Integer.toString(tab));
		        String cleanChatThread = DatabaseUtils.sqlEscapeString(chatThread);
		        String cleanChatForum = DatabaseUtils.sqlEscapeString(chatForum);
		        String cleanChatName = DatabaseUtils.sqlEscapeString(chatName);
		        
		        String cleanBackground = DatabaseUtils.sqlEscapeString(getString(R.string.default_background));
		        String cleanBoxColor = DatabaseUtils.sqlEscapeString(getString(R.string.default_element_background));
		        String cleanBoxBorder = DatabaseUtils.sqlEscapeString(getString(R.string.default_element_border));
		        String cleanTextColor = DatabaseUtils.sqlEscapeString(getString(R.string.default_text_color));
		        String cleanDividerColor = DatabaseUtils.sqlEscapeString(getString(R.string.default_divider_color));
		        String cleanWallpaper = DatabaseUtils.sqlEscapeString(getString(R.string.default_wallpaper_url));
		        
		        sql = "insert into accountlist(server,icon,color,servername,username,password,userid,avatar,postcount,themeInt,cookieCount,lastTab,tagline,chatThread,chatForum,chatName,background,boxcolor,boxborder,textcolor,dividercolor,wallpaper) values(" + cleanServer + ",'0'," + cleanColor + ",'0'," + cleanUsername + "," + cleanPassword + "," + cleanUserid + "," + cleanAvatar + "," + cleanPostcount + "," + cleanTheme + "," + cleanCookies + "," + cleanTab + "," + cleanTagline + "," + cleanChatThread + "," + cleanChatForum + "," + cleanChatName + "," + cleanBackground + "," + cleanBoxColor + "," + cleanBoxBorder + "," + cleanTextColor + "," + cleanDividerColor + "," + cleanWallpaper + ");";
		    	notetasticDB.execSQL(sql);
			}
			
			notetasticDB.close();
		}
		
		SharedPreferences.Editor editor = app_preferences.edit();
		editor.putBoolean("dbInit", true);
		editor.commit();
	}
	
	private void addNewServer(String server) {
		
		String raw = server;

		if(!raw.contains("http")) {
			raw = "http://" + raw;
    	}
		
		notetasticDB = this.openOrCreateDatabase("forumfiend", MODE_PRIVATE, null);
		
		String cleanServer = DatabaseUtils.sqlEscapeString(raw);
        String cleanUserid = DatabaseUtils.sqlEscapeString("0");
        String cleanUsername = DatabaseUtils.sqlEscapeString("0");
        String cleanPassword = DatabaseUtils.sqlEscapeString("0");
        String cleanTagline = DatabaseUtils.sqlEscapeString("null");
        String cleanAvatar = DatabaseUtils.sqlEscapeString("0");
        String cleanPostcount = DatabaseUtils.sqlEscapeString("0");
        String cleanColor = DatabaseUtils.sqlEscapeString(getString(R.string.default_color));
        String cleanBackground = DatabaseUtils.sqlEscapeString(getString(R.string.default_background));
        String cleanCookies = DatabaseUtils.sqlEscapeString(Integer.toString(0));
        String cleanTheme = DatabaseUtils.sqlEscapeString(getString(R.string.default_theme));
        String cleanTab = DatabaseUtils.sqlEscapeString(Integer.toString(0));
        String cleanChatThread = DatabaseUtils.sqlEscapeString(getString(R.string.chat_thread));
        String cleanChatForum = DatabaseUtils.sqlEscapeString(getString(R.string.chat_forum));
        String cleanChatName = DatabaseUtils.sqlEscapeString(getString(R.string.chat_name));
        
        String cleanBoxColor = DatabaseUtils.sqlEscapeString(getString(R.string.default_element_background));
        String cleanBoxBorder = DatabaseUtils.sqlEscapeString(getString(R.string.default_element_border));
        String cleanTextColor = DatabaseUtils.sqlEscapeString(getString(R.string.default_text_color));
        String cleanDividerColor = DatabaseUtils.sqlEscapeString(getString(R.string.default_divider_color));
        String cleanWallpaper = DatabaseUtils.sqlEscapeString(getString(R.string.default_wallpaper_url));
        
        sql = "insert into accountlist(server,icon,color,servername,username,password,userid,avatar,postcount,themeInt,cookieCount,lastTab,tagline,chatThread,chatForum,chatName,background,boxcolor,boxborder,textcolor,dividercolor,wallpaper) values(" + cleanServer + ",'0'," + cleanColor + ",'0'," + cleanUsername + "," + cleanPassword + "," + cleanUserid + "," + cleanAvatar + "," + cleanPostcount + "," + cleanTheme + "," + cleanCookies + "," + cleanTab + "," + cleanTagline + "," + cleanChatThread + "," + cleanChatForum + "," + cleanChatName + "," + cleanBackground + "," + cleanBoxColor + "," + cleanBoxBorder + "," + cleanTextColor + "," + cleanDividerColor + "," + cleanWallpaper + ");";
    	notetasticDB.execSQL(sql);
		
		notetasticDB.close();
	}
	
	private void connectToServer(Server server) {
		
		if(runningManifestChecks != null) {
			int killedManifests = 0;
			
			for(CheckForumManifest cfm:runningManifestChecks) {
				if(cfm.getStatus() == Status.RUNNING) {
					cfm.cancel(true);
					killedManifests++;
					
				}
			}
			
			if(killedManifests > 0) {
				Log.i("Forum Fiend","Killed " + killedManifests + " manifest checks!");
			}
		}
		
		ForumFiendApp app = (ForumFiendApp)getApplication();
		
		app.initSession();
		
		if(server.serverTagline.contentEquals("[*WEBVIEW*]")) {
			app.getSession().setServer(server);
			Intent myIntent = new Intent(IntroScreen.this, WebViewer.class);
			startActivity(myIntent);
			return;
		}
		
		RelativeLayout connectingLayout = (RelativeLayout)findViewById(R.id.intro_connecting_layout);
		connectingLayout.setVisibility(View.VISIBLE);
		
		TextView tvServerConnectionText = (TextView)findViewById(R.id.intro_connecting_text);
		tvServerConnectionText.setText("Logging in to\n" + server.serverAddress);
		
		if(server.serverColor.contains("#")) {
			connectingLayout.setBackgroundColor(Color.parseColor(server.serverColor));
			tvServerConnectionText.setTextColor(Color.parseColor(ForegroundColorSetter.getForeground(server.serverColor)));
		}
		
		//getActionBar().hide();
		
		
		app.getSession().setSessionListener(new Session.SessionListener() {

			@Override
			public void onSessionConnected() {
				loadForum();
			}

			@Override
			public void onSessionConnectionFailed(String reason) {
				Toast toast = Toast.makeText(IntroScreen.this, "Unable to log in: " + reason, Toast.LENGTH_LONG);
				toast.show();

				loadForum();
			}
			
		});
		app.getSession().setServer(server);
	}
	
	private void loadForum() {
		
		ForumFiendApp app = (ForumFiendApp)getApplication();
		app.freshBackstack();
		
		Bundle bundle = new Bundle();
		if(stealingLink) {
			bundle.putBoolean("stealing", true);
			bundle.putString("stealing_type", stealingType);
			bundle.putString("stealing_location", stealingLocation);
		} else {
			bundle.putBoolean("stealing", false);
		}
		
		stealingLink = false;
		
		Intent myIntent = new Intent(IntroScreen.this, Discussions_Main.class);
		myIntent.putExtras(bundle);
		startActivity(myIntent);
		
		//Close the intro screen on stand alone apps
		if(!getString(R.string.server_location).contentEquals("0") || incomingShortcut) {
			finish();
		}
	}

	private void removeServer(Server server) {
		notetasticDB = this.openOrCreateDatabase("forumfiend", MODE_PRIVATE, null);
		
		String cleanServerId = DatabaseUtils.sqlEscapeString(server.serverId);
        
        sql = "delete from accountlist where _id = " + cleanServerId + ";";
    	notetasticDB.execSQL(sql);
		
		notetasticDB.close();
		
		refreshList();
	}
	
	public static Server parseServerData(Cursor c) {
		Server parsedServer = new Server();
		
		parsedServer.serverUserId = c.getString(c.getColumnIndex("userid"));
		parsedServer.serverName = c.getString(c.getColumnIndex("servername"));
		parsedServer.serverAddress = c.getString(c.getColumnIndex("server"));
		parsedServer.serverAvatar = c.getString(c.getColumnIndex("avatar"));
		parsedServer.serverUserName = c.getString(c.getColumnIndex("username"));
		parsedServer.serverColor = c.getString(c.getColumnIndex("color"));
		parsedServer.serverId = Integer.toString(c.getInt((c.getColumnIndex("_id"))));
		parsedServer.serverPassword = c.getString(c.getColumnIndex("password"));
		parsedServer.serverTheme = c.getString(c.getColumnIndex("themeInt"));
		parsedServer.serverIcon = c.getString(c.getColumnIndex("icon"));
		parsedServer.serverTagline = c.getString(c.getColumnIndex("tagline"));
		parsedServer.chatForum = c.getString(c.getColumnIndex("chatForum"));
		parsedServer.chatName = c.getString(c.getColumnIndex("chatName"));
		parsedServer.chatThread = c.getString(c.getColumnIndex("chatThread"));
		parsedServer.ffChatId = c.getString(c.getColumnIndex("ffchat"));
		
		if(c.getColumnIndex("analytics") > -1) {
			parsedServer.analyticsId = c.getString(c.getColumnIndex("analytics"));
		}
		
		if(c.getColumnIndex("mobfox") > -1) {
			parsedServer.mobfoxId = c.getString(c.getColumnIndex("mobfox"));
		}
		
		if(c.getColumnIndex("background") > -1) {
			parsedServer.serverBackground = c.getString(c.getColumnIndex("background"));
		}
		
		if(c.getColumnIndex("boxcolor") > -1) {
			parsedServer.serverBoxColor = c.getString(c.getColumnIndex("boxcolor"));
		}
		
		if(c.getColumnIndex("boxborder") > -1) {
			parsedServer.serverBoxBorder = c.getString(c.getColumnIndex("boxborder"));
		}
		
		if(c.getColumnIndex("textcolor") > -1) {
			parsedServer.serverTextColor = c.getString(c.getColumnIndex("textcolor"));
		}
		
		if(c.getColumnIndex("dividercolor") > -1) {
			parsedServer.serverDividerColor = c.getString(c.getColumnIndex("dividercolor"));
		}
		
		if(c.getColumnIndex("wallpaper") > -1) {
			parsedServer.serverWallpaper = c.getString(c.getColumnIndex("wallpaper"));
		}
		
		return parsedServer;
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.intro_menu, menu);

        return true;
    }
	
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) 
        {
        case R.id.intro_menu_owners:
        	Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://blog.forumfiend.net/p/forum-owners.html"));
			startActivity(browserIntent);
        	return true;
        case R.id.intro_menu_about:
        	Intent aboutIntent = new Intent(IntroScreen.this, About.class);
			startActivity(aboutIntent);
        	return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
    
    private class CheckForumManifest extends AsyncTask<Server, Void, String> {
    	
    	private Server passedServer;

		protected String doInBackground(Server... params) {

			
			passedServer = params[0];
			
			String manifestUrl = passedServer.serverAddress + "/forumfiend.json";
			
			if(checkURL(manifestUrl)) {
				
				try {
					HttpClient httpclient = new DefaultHttpClient();
					HttpGet httpget = new HttpGet(manifestUrl);
					httpget.setHeader("User-Agent", "ForumFiend");
					ResponseHandler<String> responseHandler = new BasicResponseHandler();
		            String responseBody = httpclient.execute(httpget, responseHandler);
		            return responseBody;
				} catch(Exception ex) {
					
				}
			}
			
			return null;
		}
		
		protected void onPostExecute(final String result) 
	    {
			if(result == null) {
				return;
			}
		
			JSONObject jo;
			
			try {
				jo = new JSONObject(result);
			} catch(Exception ex) {
				Log.e("Forum Fiend",passedServer.serverAddress + " ex1 - Invalid JSON Object!");
				return;
			}
			
			String manifestName = "";
			String manifestIcon = "";
			String manifestColor = "";
			String manifestChatName = "";
			String manifestChatTopic = "";
			String manifestChatForum = "";
			String manifestFFChatId = "";
			
			String manifestAnalytics = "";
			String manifestMobfox = "";

			if(jo.has("an_mobfox_id")) {
				try {
					manifestMobfox = jo.getString("an_mobfox_id");
				} catch(Exception ex) {
					manifestMobfox = "";
				}
			}
			
			if(jo.has("g_analytics")) {
				try {
					manifestAnalytics = jo.getString("g_analytics");
				} catch(Exception ex) {
					manifestAnalytics = "";
				}
			}
			
			if(jo.has("ff_chat_id")) {
				try {
					manifestFFChatId = jo.getString("ff_chat_id");
				} catch(Exception ex) {
					manifestFFChatId = "";
				}
			}
			
			if(jo.has("name")) {
				try {
					manifestName = jo.getString("name");
				} catch(Exception ex) {
					manifestName = "";
				}
			}
			
			if(jo.has("icon")) {
				try {
					manifestIcon = jo.getString("icon");
				} catch(Exception ex) {
					manifestIcon = "";
				}
			}
			
			if(jo.has("color")) {
				try {
					manifestColor = jo.getString("color");
				} catch(Exception ex) {
					manifestColor = "";
				}
			}
			
			if(jo.has("chat_name")) {
				try {
					manifestChatName = jo.getString("chat_name");
				} catch(Exception ex) {
					manifestChatName = "";
				}
			}
			
			if(jo.has("chat_forum")) {
				try {
					manifestChatForum = jo.getString("chat_forum");
				} catch(Exception ex) {
					manifestChatForum = "";
				}
			}
			
			if(jo.has("chat_topic")) {
				try {
					manifestChatTopic = jo.getString("chat_topic");
				} catch(Exception ex) {
					manifestChatTopic = "";
				}
			}
			
			int validFields = 0;
			
			if(manifestAnalytics.length() > 0) {
				passedServer.analyticsId = manifestAnalytics;
				validFields++;
			}
			
			if(manifestMobfox.length() > 0) {
				passedServer.mobfoxId = manifestMobfox;
				validFields++;
			}
			
			if(manifestFFChatId.length() > 0) {
				passedServer.ffChatId = manifestFFChatId;
				validFields++;
			}
			
			if(manifestName.length() > 0) {
				passedServer.serverName = manifestName;
				validFields++;
			}
			
			if(manifestColor.length() > 0 && (passedServer.serverColor.contentEquals("0") || passedServer.serverColor.contentEquals(getString(R.string.default_color)))) {
				if(manifestColor.length() == 7) {
					if(manifestColor.substring(0, 1).contentEquals("#")) {
						passedServer.serverColor = manifestColor;
					}
				}
				validFields++;
			}
			
			if(manifestIcon.length() > 0) {
				passedServer.serverIcon = manifestIcon;
				validFields++;
			}
			
			if(manifestChatName.length() > 0 && !manifestChatName.contentEquals("0")) {
				passedServer.chatName = manifestChatName;
				validFields++;
			}
			
			if(manifestChatForum.length() > 0 && !manifestChatForum.contentEquals("0")) {
				passedServer.chatForum = manifestChatForum;
				validFields++;
			}
			
			if(manifestChatTopic.length() > 0 && !manifestChatTopic.contentEquals("0")) {
				passedServer.chatThread = manifestChatTopic;
				validFields++;
			}
			
			ForumFiendApp app = (ForumFiendApp)getApplication();
			app.getSession().updateSpecificServer(passedServer);

			if(lvServers == null) {
				sAdapterTemp = ((ServerAdapter)gvServers.getAdapter());
			} else {
				sAdapterTemp = ((ServerAdapter)lvServers.getAdapter());
			}
			 
			runOnUiThread(new Runnable() {

			    public void run() {
			    	sAdapterTemp.notifyDataSetChanged();
			    }
			});
			
			if(validFields > 0) {
				ah.trackEvent("forum fiend manifest", "parsed", passedServer.serverAddress, false);
			}
	    }
	}
    
    private ServerAdapter sAdapterTemp;
    
    private void createHomescreenShortcut(Server server) {
        new iconMaker().execute(server);
    }
    
    public static Bitmap getBitmapFromURL(String src) {
        try {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    private class iconMaker extends AsyncTask<Server, Void, Bitmap> {

    	private Server server;
    	
		@Override
		protected Bitmap doInBackground(Server... params) {
			server = params[0];
			
			Bitmap bitmap = null;
			
			if(server.serverIcon.contains("png") || server.serverIcon.contains("ico")) {
				bitmap = getBitmapFromURL(server.serverIcon);
				int size = (int) getResources().getDimension(android.R.dimen.app_icon_size);
				bitmap = Bitmap.createScaledBitmap(bitmap, size, size, true);
			}
			
			return bitmap;
		}
		
		protected void onPostExecute(Bitmap result) {

			final Intent shortcutIntent = new Intent(IntroScreen.this, IntroScreen.class);
			shortcutIntent.putExtra("server_id", server.serverId);
			
	        final Intent intent = new Intent();
	        intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
	        
	        shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	        shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	        
	        if(server.serverName.contentEquals("0")) {
	        	intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, server.serverAddress.replace("http://", ""));
	        } else {
	        	intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, server.serverName);
	        }

	        if(result == null) {
	        	intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, Intent.ShortcutIconResource.fromContext(IntroScreen.this, R.drawable.ic_launcher));
	        } else {
	        	intent.putExtra(Intent.EXTRA_SHORTCUT_ICON, result);
	        }

	        intent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
	        sendBroadcast(intent);
	        
	        Toast toast = Toast.makeText(IntroScreen.this, "Homescren Icon Created!", Toast.LENGTH_LONG);
			toast.show();
		}
    	
    }
    
    private void askAboutWebview() {
    	final AlertDialog.Builder builder = new AlertDialog.Builder(IntroScreen.this);
        builder.setTitle("Tapatalk API Not Found");
        builder.setCancelable(true);
        builder.setPositiveButton("Try WebView", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            	addWebViewServer(serverInputter.getText().toString().trim());
            	
            	refreshList();
    			
    			serverInputter.setText("");
    			
    			InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
    			imm.hideSoftInputFromWindow(serverInputter.getWindowToken(), 0);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            	//do nothing
            }
        });
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            public void onCancel(DialogInterface dialog) {
            	//do nothing
            }
        });

        builder.setMessage("The Tapatalk API cannot be found at the URL you provided.  Do you want to view this forum in a WebView instead?  If not, you can go back and try to re-enter your server information.");
        builder.create().show();
		
    	return;
	}
    
    private void addWebViewServer(String url) {
    	
    	String raw = url;
    	
    	if(!raw.contains("http")) {
			raw = "http://" + raw;
    	}
    	
    	notetasticDB = this.openOrCreateDatabase("forumfiend", MODE_PRIVATE, null);
		
		String cleanServer = DatabaseUtils.sqlEscapeString(raw);
        String cleanUserid = DatabaseUtils.sqlEscapeString("0");
        String cleanUsername = DatabaseUtils.sqlEscapeString("WebView Forum");
        String cleanPassword = DatabaseUtils.sqlEscapeString("0");
        String cleanTagline = DatabaseUtils.sqlEscapeString("[*WEBVIEW*]");
        String cleanAvatar = DatabaseUtils.sqlEscapeString("0");
        String cleanPostcount = DatabaseUtils.sqlEscapeString("0");
        String cleanColor = DatabaseUtils.sqlEscapeString(getString(R.string.default_color));
        String cleanBackground = DatabaseUtils.sqlEscapeString(getString(R.string.default_background));
        String cleanCookies = DatabaseUtils.sqlEscapeString(Integer.toString(0));
        String cleanTheme = DatabaseUtils.sqlEscapeString(getString(R.string.default_theme));
        String cleanTab = DatabaseUtils.sqlEscapeString(Integer.toString(0));
        String cleanChatThread = DatabaseUtils.sqlEscapeString(getString(R.string.chat_thread));
        String cleanChatForum = DatabaseUtils.sqlEscapeString(getString(R.string.chat_forum));
        String cleanChatName = DatabaseUtils.sqlEscapeString(getString(R.string.chat_name));
        
        String cleanBoxColor = DatabaseUtils.sqlEscapeString(getString(R.string.default_element_background));
        String cleanBoxBorder = DatabaseUtils.sqlEscapeString(getString(R.string.default_element_border));
        String cleanTextColor = DatabaseUtils.sqlEscapeString(getString(R.string.default_text_color));
        String cleanDividerColor = DatabaseUtils.sqlEscapeString(getString(R.string.default_divider_color));
        String cleanWallpaper = DatabaseUtils.sqlEscapeString(getString(R.string.default_wallpaper_url));
        
        sql = "insert into accountlist(server,icon,color,servername,username,password,userid,avatar,postcount,themeInt,cookieCount,lastTab,tagline,chatThread,chatForum,chatName,background,boxcolor,boxborder,textcolor,dividercolor,wallpaper) values(" + cleanServer + ",'0'," + cleanColor + ",'0'," + cleanUsername + "," + cleanPassword + "," + cleanUserid + "," + cleanAvatar + "," + cleanPostcount + "," + cleanTheme + "," + cleanCookies + "," + cleanTab + "," + cleanTagline + "," + cleanChatThread + "," + cleanChatForum + "," + cleanChatName + "," + cleanBackground + "," + cleanBoxColor + "," + cleanBoxBorder + "," + cleanTextColor + "," + cleanDividerColor + "," + cleanWallpaper + ");";
    	notetasticDB.execSQL(sql);
		
		notetasticDB.close();
    }
    
    @Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) { 

		super.onActivityResult(requestCode, resultCode, data);
	}
    
    private void renameServer(final Server server) {
    	
    	String oldName = server.serverName;
    	
    	if(oldName.contentEquals("0")) {
    		oldName = server.serverAddress;
    	}
    	
    	final EditText input = new EditText(this);
    	input.setText(oldName);
    	
    	new AlertDialog.Builder(this)
    	.setTitle("Rename Server")
        .setMessage("Choose a new display name for this server.")
        .setView(input)
        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                runOnUiThread(new Runnable() {
	        	    public void run() {
	        	    	notetasticDB = openOrCreateDatabase("forumfiend", MODE_PRIVATE, null);
	        			
	        			String cleanServerId = DatabaseUtils.sqlEscapeString(server.serverId);
	        			String cleanName = DatabaseUtils.sqlEscapeString(input.getText().toString().trim());
	        			
	        			if(cleanName.length() == 0) {
	        				return;
	        			}
	        	        
	        	        sql = "update accountlist set servername = " + cleanName + " where _id = " + cleanServerId + ";";
	        	    	notetasticDB.execSQL(sql);
	        			
	        			notetasticDB.close();
	        			
	        			refreshList();
	        	    }
	        	});
				
				dialog.dismiss();
            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Do nothing.
            }
        }).show();
    }
}
