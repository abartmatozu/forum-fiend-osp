package com.forum.fiend.osp;

import java.util.ArrayList;

import java.util.List;
import java.util.UUID;

import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;


public class AnalyticsHelper {
	
	private String analyticsId;
	private String appName;
	
	private Context context;
	
	private String uniqueID;

	public AnalyticsHelper(Context c,String analytics,String name) {
		context = c;
		analyticsId = analytics;
		appName = name;
		
		SharedPreferences app_preferences = context.getSharedPreferences("prefs", 0);
		uniqueID = app_preferences.getString("analytics_uuid", "0");
		
		if(uniqueID.contentEquals("0")) {
			uniqueID = UUID.randomUUID().toString(); 
			SharedPreferences.Editor editor = app_preferences.edit();
			editor.putString("analytics_uuid", uniqueID);
			editor.commit();
		}

		
	}

	public void trackScreen(String name,boolean global) {
        new LogAnalyticsView().execute(name);
        
        if(global) {
	        // not used currently
        }
	}
	
	public void trackCustomScreen(String analytics,String name) {
        new LogAnalyticsView().execute(name);

	}
	
	public void trackCustomEvent(String analytics,String cat,String act,String lab) {
		new LogAnalyticsEvent().execute(cat,act,lab,analytics);
	}
	
	public void trackEvent(String cat,String act,String lab,boolean global) {
		
		new LogAnalyticsEvent().execute(cat,act,lab);
		
		if(global) {
			// not used currently
		}
	}
	
	private class LogAnalyticsView extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... params) {
			HttpClient httpclient = new DefaultHttpClient(); 
            HttpPost httppost;
            
            String viewName = params[0];
            
            String viewId = null;
            
            if(params.length > 1) {
            	viewId = params[1];
            }
            
            if(viewId == null) {
            	viewId = analyticsId;
            }

            httppost = new HttpPost("http://www.google-analytics.com/collect"); 

            try {  
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);  
                nameValuePairs.add(new BasicNameValuePair("v", "1"));
                nameValuePairs.add(new BasicNameValuePair("tid", viewId)); 
                nameValuePairs.add(new BasicNameValuePair("cid", uniqueID)); 
                nameValuePairs.add(new BasicNameValuePair("t", "appview")); 
                nameValuePairs.add(new BasicNameValuePair("an", appName)); 
                nameValuePairs.add(new BasicNameValuePair("av", context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName)); 
                nameValuePairs.add(new BasicNameValuePair("cd", viewName)); 
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));  

                ResponseHandler<String> responseHandler = new BasicResponseHandler();
	            String response = httpclient.execute(httppost,responseHandler);
	            return response;
                  
            } catch (Exception e) {
				return "fail";
            }

		}
		
		protected void onPostExecute(final String result) {
			
			// yay
	    }
		
	}
	
	private class LogAnalyticsEvent extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... params) {
			HttpClient httpclient = new DefaultHttpClient(); 
            HttpPost httppost;
            
            String eventCat = params[0];
            String eventAct = params[1];
            String eventLab = params[2];
            String eventId = null;
            
            if(params.length > 3) {
            	eventId = params[3];
            }
            
            if(eventId == null) {
            	eventId = analyticsId;
            }

            httppost = new HttpPost("http://www.google-analytics.com/collect"); 

            try {  
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);  
                nameValuePairs.add(new BasicNameValuePair("v", "1"));
                nameValuePairs.add(new BasicNameValuePair("tid", eventId)); 
                nameValuePairs.add(new BasicNameValuePair("cid", uniqueID)); 
                nameValuePairs.add(new BasicNameValuePair("t", "event")); 
                nameValuePairs.add(new BasicNameValuePair("ec", eventCat)); 
                nameValuePairs.add(new BasicNameValuePair("ea", eventAct)); 
                nameValuePairs.add(new BasicNameValuePair("el", eventLab)); 
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));  

                ResponseHandler<String> responseHandler = new BasicResponseHandler();
	            String response = httpclient.execute(httppost,responseHandler);
	            return response;
                  
            } catch (Exception e) {
				return "fail";
            }

		}
		
		protected void onPostExecute(final String result) {
			
			// yay
	    }
		
	}
}
