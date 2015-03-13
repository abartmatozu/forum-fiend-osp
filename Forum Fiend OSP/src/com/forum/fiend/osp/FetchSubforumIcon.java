package com.forum.fiend.osp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.ImageView;

public class FetchSubforumIcon extends AsyncTask<Object, Void, String> {

	private InputStream is;
	Bitmap bmImg;
	private ImageView secureHolder;
	private String cacheName;
	private String ImageLocation;
	private ForumFiendApp app;
	
	@Override
	protected String doInBackground(Object... params) {
		secureHolder = (ImageView)params[1];
		cacheName = (String)params[0];
		ImageLocation = (String)params[2];
		app = (ForumFiendApp)params[3];
		
		try{
			
			BasicCookieStore cStore = new BasicCookieStore();

			CookieManager cookiemanager = new CookieManager(); 
		    cookiemanager.setCookiePolicy(CookiePolicy.ACCEPT_ALL); 
		    CookieHandler.setDefault(cookiemanager); 
		    
		    URL myFileUrl = new URL(ImageLocation);
			
			
	    	
		    String cookieString = "";
		    
		    
		    for(String s:app.getSession().getCookies().keySet()) {
		    	try {
		    		BasicClientCookie aCookie = new BasicClientCookie(s,app.getSession().getCookies().get(s));
					cStore.addCookie(aCookie);
					
					cookieString = cookieString + s + "=" + app.getSession().getCookies().get(s) + ";";
		    	} catch(Exception ex) {
		    		//nobody cares
		    	}
		    }
		    
		    HttpContext localContext = new BasicHttpContext(); 
	        localContext.setAttribute(ClientContext.COOKIE_STORE, cStore);
			
			HttpURLConnection conn = (HttpURLConnection)myFileUrl.openConnection();
	    	conn.setDoInput(true);

		    conn.setRequestProperty("Cookie", cookieString);
		    conn.setRequestProperty("Content-Type", "image/*");

	    	conn.connect();

	    	is = conn.getInputStream();

			bmImg = BitmapFactory.decodeStream(is);
			return "web";
		} catch(Exception e) {
			if(e.getMessage() != null) {
    			Log.e("Forum Fiend", "ApeImageCacher: Connection Exception: " + e.getMessage());
    		} else {
    			Log.e("Forum Fiend", "ApeImageCacher: exNull Error Downloading Image!");
    		}
		}
		
		return "fail";
	}
	
	protected void onPostExecute(final String result) {
		if(result.contentEquals("fail")) {
			//we will just use the default icon, get out of here.
			return;
		}
		
		//If it it web or cache, we have what we need, set the bitmap

		//Save the image to cache.
		try {
			File saveDirectory = new File(Environment.getExternalStorageDirectory(), ApeImageCacher.cacheDirectory);
			File file = new File(saveDirectory.getPath() + File.separator + cacheName);
			OutputStream os = new FileOutputStream(file);
			bmImg.compress(Bitmap.CompressFormat.JPEG,80,os);
			os.close();
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		
		if(secureHolder != null && bmImg != null) {
			secureHolder.setImageBitmap(bmImg);
		}
		
		return;
    }

}
