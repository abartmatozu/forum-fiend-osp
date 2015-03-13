package com.forum.fiend.osp;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.ImageLoader;


public class Mail extends FragmentActivity {

	@SuppressLint("NewApi")
	public void onCreate(Bundle savedInstanceState) {
		
		ForumFiendApp application = (ForumFiendApp)getApplication();
		String background = application.getSession().getServer().serverColor;

        
        
        ThemeSetter.setTheme(this,background);

        super.onCreate(savedInstanceState);
        
        ThemeSetter.setActionBar(this,background);
        
        setTitle("Inbox");

        
        setContentView(R.layout.single_frame_activity);
        
        //Setup forum background
    	String forumWallpaper = application.getSession().getServer().serverWallpaper;
    	String forumBackground = application.getSession().getServer().serverBackground;
    	
    	FrameLayout sfa_holder = (FrameLayout)findViewById(R.id.sfa_holder);
    	ImageView sfa_wallpaper = (ImageView)findViewById(R.id.sfa_wallpaper);
    	
    	if(forumBackground != null && forumBackground.contains("#") && forumBackground.length() == 7) {
    		sfa_holder.setBackgroundColor(Color.parseColor(forumBackground));
    	} else {
    		sfa_holder.setBackgroundColor(Color.parseColor(getString(R.string.default_background)));
    	}
    	
    	if(forumWallpaper != null && forumWallpaper.contains("http")) {

        	String imageUrl = forumWallpaper;
    		ImageLoader.getInstance().displayImage(imageUrl, sfa_wallpaper);
    	} else {
    		sfa_wallpaper.setVisibility(View.GONE);
    	}
        
        MailFragment mf = new MailFragment();
        
        FragmentManager fragmentManager=getSupportFragmentManager();
        FragmentTransaction ftZ=null;
    	ftZ = fragmentManager.beginTransaction();
		ftZ.replace(R.id.single_frame_layout_frame, mf);
		ftZ.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
    	ftZ.commit();

	}
	
	
}
