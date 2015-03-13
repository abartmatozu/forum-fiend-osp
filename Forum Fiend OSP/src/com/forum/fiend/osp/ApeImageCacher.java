package com.forum.fiend.osp;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.ImageView;

@SuppressLint("NewApi")
public class ApeImageCacher {

	public static final String cacheDirectory = ".ff_cache";

	public static final void DownloadImage(String ImageURL, ImageView ivHolder, ForumFiendApp application, Context context) {
		
		Log.d("Forum Fiend", "Downloading " + ImageURL + " with the ApeImageCacher");
		
		String cacheName = application.getSession().getServer().serverAddress.replace("http", "").replace("/", "").replace(".", "").replace("https", "").replace(":", "");
		
		cacheName = cacheName + "_" +  ImageURL.substring((ImageURL.lastIndexOf("?")) + 1,ImageURL.length());
		cacheName = cacheName + ".jpg";

		if(ivHolder == null) {
			return;
		}
		
		File saveDirectory = new File(Environment.getExternalStorageDirectory(), cacheDirectory);
		
		if (! saveDirectory.exists()) {
            if (! saveDirectory.mkdirs()) {
                Log.d("Forum Fiend", "failed to create directory");
                return;
            }
        }

		try {
			BitmapFactory.Options options = new BitmapFactory.Options();

			File file = new File(saveDirectory.getPath() + File.separator + cacheName);
			
			FileInputStream fis = new FileInputStream(file);
			
			BufferedInputStream buf = new BufferedInputStream(fis);
			Bitmap bmImg = BitmapFactory.decodeStream(buf, null, options);
			
			if(bmImg.getHeight() < 2) {
				if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
					new FetchSubforumIcon().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,cacheName,ivHolder,ImageURL,application);
				} else {
					new FetchSubforumIcon().execute(cacheName,ivHolder,ImageURL,application);
				}
				
				Log.d("Forum Fiend", "Downloading new copy for " + ImageURL);
				return;
			}
			
			ivHolder.setImageBitmap(bmImg);
			
			Log.d("Forum Fiend", "Using cached image for " + ImageURL);
		} catch(Exception e) {
			
			if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
				new FetchSubforumIcon().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,cacheName,ivHolder,ImageURL,application);
			} else {
				new FetchSubforumIcon().execute(cacheName,ivHolder,ImageURL,application);
			}
			
			Log.d("Forum Fiend", "Downloading new copy for " + ImageURL);
			return;
		}
		
		
	}
	

}
