package com.forum.fiend.osp;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.util.Log;

public class AvatarUploader {

	public String uploadBitmap(Context context, String url, Bitmap bitmap,ForumFiendApp application) {
		
		String result = "fail";
		
		
		
		BasicCookieStore cStore = new BasicCookieStore();

		CookieManager cookiemanager = new CookieManager(); 
	    cookiemanager.setCookiePolicy(CookiePolicy.ACCEPT_ALL); 
	    CookieHandler.setDefault(cookiemanager); 
	    
	    String cookieString = "";
	    
	    
	    for(String s:application.getSession().getCookies().keySet()) {
	    	try {
	    		BasicClientCookie aCookie = new BasicClientCookie(s,application.getSession().getCookies().get(s));
				cStore.addCookie(aCookie);
				
				cookieString = cookieString + s + "=" + application.getSession().getCookies().get(s) + ";";
	    	} catch(Exception ex) {
	    		//nobody cares
	    	}
	    }
		
		try {
		
			HttpClient httpClient = new DefaultHttpClient();
	        HttpContext localContext = new BasicHttpContext(); 
	        
	        localContext.setAttribute(ClientContext.COOKIE_STORE, cStore);
	        
	        HttpPost httpPost = new HttpPost(url);  
	        
	        //httpPost.setHeader("User-Agent", "ForumFiend");
	        httpPost.setHeader("Cookie", cookieString);
	        
	        Log.d(context.getString(R.string.app_name), "Cookie String: " + cookieString);

	        MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
	        
	        
	        
	        ByteArrayOutputStream bos = new ByteArrayOutputStream();
	        
	        bitmap.compress(CompressFormat.JPEG, 60, bos);  
	        
	        
	        Log.d(context.getString(R.string.app_name), "Outgoing Avatar Size: " + bitmap.getWidth() + "x" + bitmap.getHeight());
	        
	        
	        byte[] data = bos.toByteArray();  
	        
	        //entity.addPart("myParam", new StringBody("my value"));
	        
	        File f = new File(context.getCacheDir(), "temp.jpg");
	        f.createNewFile();
	        FileOutputStream fos = new FileOutputStream(f);
	        fos.write(data);
	        fos.close();
	        
	        FileBody bin = new FileBody(f, "image/jpeg");
	        
	        
	        String methodName = "upload_avatar";
	        String uploadFileFieldName;
	        
	        //String uploadFileFieldName = "uploadfile";
	        
	        uploadFileFieldName = application.getSession().getAvatarName();
	        
	        Log.d(context.getString(R.string.app_name), "Avatar Upload Field Name: " + uploadFileFieldName);
	        
	        entity.addPart(uploadFileFieldName, bin);
	        entity.addPart("method_name", new StringBody(methodName));
	        
	        httpPost.setEntity(entity);  
	        

	        HttpResponse response = httpClient.execute(httpPost, localContext);  
	        BufferedReader reader = new BufferedReader(new InputStreamReader( response.getEntity().getContent(), "UTF-8"));  
	        
	        
	        String line;
	        String xml = "";
			
			while((line = reader.readLine()) != null)
			{
				xml = xml + line;
				Log.d("Discussions",line);
			}


	        result = reader.readLine();
        
		} catch(Exception ex) {
			//fuck it
			Log.d("Discussions",ex.getMessage());
		}
		
		
		return result;
	}
	
}
