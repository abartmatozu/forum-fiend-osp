package com.forum.fiend.osp;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

public class ActiveUsersActivity extends FragmentActivity {
	
	private AnalyticsHelper ah;

	@SuppressLint("NewApi")
	public void onCreate(Bundle savedInstanceState) {

		String backgroundColor = ((ForumFiendApp) getApplication()).getSession().getServer().serverColor;

        ThemeSetter.setTheme(this, backgroundColor);
		
		super.onCreate(savedInstanceState);
		
		ThemeSetter.setActionBar(this, backgroundColor);
        
        //Track app analytics
        ah = ((ForumFiendApp)getApplication()).getAnalyticsHelper();
        ah.trackScreen(getClass().getSimpleName(), false);
        
        setContentView(R.layout.single_frame_activity);
		
		FragmentManager fragmentManager=getSupportFragmentManager();
    	FragmentTransaction ft=null;

    	ft = fragmentManager.beginTransaction();
    	
    	final ActiveList subforums = new ActiveList();

    	ft.replace(R.id.single_frame_layout_frame, subforums,"Active Users");

    	ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
    	ft.commit();
    	
    	setTitle("Active Users");
    	
    	if(getString(R.string.server_location).contentEquals("0")) {
	        if(ForegroundColorSetter.getForegroundDark(backgroundColor)) {
	        	getActionBar().setIcon(R.drawable.ic_ab_main_black);
	        } else {
	        	getActionBar().setIcon(R.drawable.ic_ab_main_white);
	        }
        }
    	
	}
	
	@Override
    public void onStart() {
      super.onStart();

    }

    @Override
    public void onStop() {
      super.onStop();

    }
	

}
