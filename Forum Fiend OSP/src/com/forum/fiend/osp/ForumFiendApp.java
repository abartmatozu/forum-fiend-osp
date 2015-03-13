package com.forum.fiend.osp;

import java.util.ArrayList;
import java.util.List;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import android.app.Application;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;


public class ForumFiendApp extends Application {
	
	private AnalyticsHelper ah;
	
	private Session session;
	public boolean appActive = false;
	public BackStackManager stackManager;
	private int backStackid;
	
	private boolean forceRefresh = false;

	
	@Override
    public void onCreate() {
        super.onCreate();

        stackManager = new BackStackManager();
        backStackid = stackManager.createBackstack();
        
        SharedPreferences app_preferences = getSharedPreferences("prefs", 0);
		boolean cleanClose = app_preferences.getBoolean("ff_clean_close", true);

        ah = new AnalyticsHelper(this,getString(R.string.analytics_app_tracker),getString(R.string.app_name));
        
        DisplayImageOptions options = new DisplayImageOptions.Builder()
        .cacheInMemory(true)
        .cacheOnDisk(true)
        .bitmapConfig(Bitmap.Config.RGB_565)
        .imageScaleType(ImageScaleType.IN_SAMPLE_INT)
        .build();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext()).defaultDisplayImageOptions(options).build();
        ImageLoader.getInstance().init(config);
        
        if(!cleanClose) {
        	Log.d("Forum Fiend", "Bad shutdown detected, clearing image cache.");
        	ImageLoader.getInstance().clearDiskCache();
        	ImageLoader.getInstance().clearMemoryCache();
        }
        
        Editor editor = app_preferences.edit();
        editor.putBoolean("ff_clean_close", false);
        editor.commit();
    }
	
	public AnalyticsHelper getAnalyticsHelper() {
		return ah;
	}
	
	public void freshBackstack() {
		stackManager.clearAllStacks();
		backStackid = stackManager.createBackstack();
	}
	
	public int getBackStackId() {
		return backStackid;
	}
	
	public void initSession() {
		session = new Session(this,this);
	}
	
	public Session getSession() {
		
		if(session == null) {
			session = new Session(this,this);
		}
		
		return session;
	}
	  
	@Override
	public void onLowMemory() {
		Runtime.getRuntime().gc();
	}
	  
	  public void sendLoginStat(String address) {
		  new sendLoginStat().execute(address);
	  }
	  
	  private class sendLoginStat extends AsyncTask<String, Void, String> {
			
			@Override
			protected String doInBackground(String... params) {
				HttpClient httpclient = new DefaultHttpClient(); 
	            HttpPost httppost = new HttpPost("http://forumfiend.net/api/loginstat.php");
	            
	            try {  
	                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);  
	                nameValuePairs.add(new BasicNameValuePair("server_address", params[0]));  
	                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));  
	           
	                httpclient.execute(httppost);
	            } catch (Exception e) {
	            	//who cares
	            }
	            
	            return "";
			}
			
			protected void onPostExecute(final String result) {
				return;
		    }
	    }
	  
	  public void setForceRefresh(boolean value) {
		  forceRefresh = value;
	  }
	  
	  public boolean getForceRefresh() {
		  return forceRefresh;
	  }
}
