package com.forum.fiend.osp;

import java.io.InputStream;

import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class About extends Activity {
	
	private AnalyticsHelper ah;

	protected void onCreate(Bundle savedInstanceState) {
		
		ThemeSetter.setThemeNoTitlebar(this, getString(R.string.default_color));
		
		super.onCreate(savedInstanceState);
		
		ThemeSetter.setNavAndStatusBar(this, getString(R.string.default_color));
		
		ah = ((ForumFiendApp)getApplication()).getAnalyticsHelper();
        ah.trackScreen(getClass().getSimpleName(), false);

		setContentView(R.layout.about_layout);
		
		TextView tvChangelog = (TextView)findViewById(R.id.about_tv_changelog);
		
		try {
	        Resources res = getResources();
	        InputStream in_s = res.openRawResource(R.raw.changelog);

	        byte[] b = new byte[in_s.available()];
	        in_s.read(b);
	        tvChangelog.setText(new String(b));
	    } catch (Exception e) {
	        // e.printStackTrace();
	    	tvChangelog.setText("Error: can't show help.");
	    }
		
		TextView tvEdition = (TextView)findViewById(R.id.about_tv_edition);
		tvEdition.setText("Community Edition (OSP)");

		ImageView ivPowered = (ImageView)findViewById(R.id.about_powered_by);

		if(getString(R.string.server_location).contentEquals("0")) {
			ivPowered.setVisibility(View.GONE);
		}
	}
}
