package com.forum.fiend.osp;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import android.content.Context;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;

public class FileUploader {
	
	public String uploadBitmap(Context context, String url, Bitmap bitmap,ForumFiendApp application) {

		String result = "fail";
		
		try {
		
			HttpClient httpClient = new DefaultHttpClient();  
	        HttpContext localContext = new BasicHttpContext(); 
	        
	        HttpPost httpPost = new HttpPost(url);  

	        MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
	        
	        ByteArrayOutputStream bos = new ByteArrayOutputStream();
	        
	        bitmap.compress(CompressFormat.JPEG, 100, bos);  
	        byte[] data = bos.toByteArray();  

	        entity.addPart("uploadedfile", new ByteArrayBody(data, "temp.jpg"));
	        entity.addPart("server_address", new StringBody(application.getSession().getServer().serverAddress));
	        entity.addPart("id", new StringBody(application.getSession().getServer().serverUserName));
	        
	        httpPost.setEntity(entity);  
	        
	        HttpResponse response = httpClient.execute(httpPost, localContext);  
	        BufferedReader reader = new BufferedReader(new InputStreamReader( response.getEntity().getContent(), "UTF-8"));  
	        result = reader.readLine();
        
		} catch(Exception ex) {
			//fuck it
		}
		
		return result;
	}
	
}
