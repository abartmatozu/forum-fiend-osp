package com.forum.fiend.osp;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;


public class WebViewer extends FragmentActivity {


	private ActionBar actionBar;
	private String background;
	
	private WebView wvMain;
	
	private ForumFiendApp application;
	private AnalyticsHelper ah;
	
	/** Called when the activity is first created. */
	

    @SuppressLint("NewApi")
	@Override
    public void onCreate(Bundle savedInstanceState) {
    	
    	application = (ForumFiendApp)getApplication();

    	String url = application.getSession().getServer().serverAddress;
    	
    	background = application.getSession().getServer().serverColor;
        ThemeSetter.setTheme(this,background);



        
        super.onCreate(savedInstanceState);
        
        ThemeSetter.setActionBar(this,background);
        
        
        actionBar = getActionBar();
        //actionBar.setDisplayHomeAsUpEnabled(true);
        //actionBar.setHomeButtonEnabled(true);
        
        //actionBar.setTitle(screenTitle);
        actionBar.setSubtitle(url);
        
        //Track app analytics
        ah = application.getAnalyticsHelper();
        ah.trackScreen(getClass().getName(), false);
        
        setContentView(R.layout.web_viewer);
        
        
        
        wvMain = (WebView)findViewById(R.id.web_viewer_webview);
        
        wvMain.setWebViewClient(new HelloWebViewClient());
        
        wvMain.loadUrl(url);
  
        new checkForumIcon().execute();
    }
    
    @Override
    public void onResume() {

    	super.onResume();
    }
    
    @Override
    public void onStart() {
      super.onStart();

    }

    @Override
    public void onStop() {
      super.onStop();

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
    
    private class HelloWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(event.getAction() == KeyEvent.ACTION_DOWN){
            switch(keyCode)
            {
            case KeyEvent.KEYCODE_BACK:
                if(wvMain.canGoBack()){
                	wvMain.goBack();
                }else{
                    finish();
                }
                return true;
            }

        }
        
        return super.onKeyDown(keyCode, event);
    }
    
    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.web_view_menu, menu);

        return true;
    }
	
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) 
        {
        case R.id.web_view_menu_close:
        	finish();
        	return true;
        case R.id.web_view_menu_theme:
        	ColorPickerDialogFragment newFragment = ColorPickerDialogFragment.newInstance();
			newFragment.setOnColorSelectedListener(new ColorPickerDialogFragment.onColorSelectedListener() {
				
				public void onColorSelected(String color) {
					setColor(color);
				}
			});
		    newFragment.show(getSupportFragmentManager(), "dialog");
        	return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
    
    private void setColor(String color) {
		
		application.getSession().getServer().serverColor = color;
		application.getSession().updateServer();
		
		finish();
		startActivity(getIntent());
	}
}
