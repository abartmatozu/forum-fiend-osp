package com.forum.fiend.osp;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

public class ThemeEditor extends FragmentActivity {
	
	private ForumFiendApp application;
	
	private AnalyticsHelper ah;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		application = (ForumFiendApp)getApplication();
		application.setForceRefresh(true);
		
		//Track app analytics
		ah = ((ForumFiendApp)getApplication()).getAnalyticsHelper();
		ah.trackScreen(getClass().getName(), false);
		
		setContentView(R.layout.theme_editor);
		
		updatePreview();
		juiceUpOptions();
	}
	
	private void updatePreview() {
		SharedPreferences app_preferences = getSharedPreferences("prefs", 0);

        boolean useShading = app_preferences.getBoolean("use_shading", false);
        boolean useOpenSans = app_preferences.getBoolean("use_opensans", false);
		int fontSize = app_preferences.getInt("font_size", 16);
		boolean currentAvatarSetting = app_preferences.getBoolean("show_images",true);
		
		TextView theme_editor_preview_appbar = (TextView)findViewById(R.id.theme_editor_preview_appbar);
		theme_editor_preview_appbar.setBackgroundColor(Color.parseColor(application.getSession().getServer().serverColor));
		
		if(ThemeSetter.getForegroundDark(application.getSession().getServer().serverColor)) {
			theme_editor_preview_appbar.setTextColor(Color.BLACK);
		} else {
			theme_editor_preview_appbar.setTextColor(Color.WHITE);
		}
		
		FrameLayout theme_editor_preview = (FrameLayout) findViewById(R.id.theme_editor_preview);
		ImageView theme_editor_preview_wallpaper = (ImageView)findViewById(R.id.theme_editor_preview_wallpaper);
		
		String forumWallpaper = application.getSession().getServer().serverWallpaper;
    	String forumBackground = application.getSession().getServer().serverBackground;

    	if(forumWallpaper != null && forumWallpaper.contains("http")) {
        	String imageUrl = forumWallpaper;
        	theme_editor_preview_wallpaper.setVisibility(View.VISIBLE);
    		ImageLoader.getInstance().displayImage(imageUrl, theme_editor_preview_wallpaper);
    	} else {
    		theme_editor_preview_wallpaper.setVisibility(View.GONE);
    	}
    	
    	if(forumBackground != null && forumBackground.contains("#") && forumBackground.length() == 7) {
    		theme_editor_preview.setBackgroundColor(Color.parseColor(forumBackground));
    	} else {
    		theme_editor_preview.setBackgroundColor(Color.parseColor(getString(R.string.default_background)));
    	}
    	
    	boolean useDivider = true;
    	
    	if(!(application.getSession().getServer().serverBackground.contentEquals(application.getSession().getServer().serverBoxColor) && application.getSession().getServer().serverBoxBorder.contentEquals("0"))) {
    		//useDivider = false;
		}
				
		LinearLayout previewHolder = (LinearLayout) findViewById(R.id.theme_editor_preview_holder);
		previewHolder.removeAllViews();
		
		Category cat = new Category();
		cat.category_name = "Fun Category";
		cat.categoryType = "S";
		
		Category top = new Category();
		top.category_name = "Important Thread";
		top.topicSticky = "Y";
		top.thread_count = "2";
		top.view_count = "7";
		top.category_lastthread = "NR89";
		top.categoryType = "C";
		top.hasNewTopic = true;
		top.categoryIcon = "http://www.ape-apps.com/nr90.jpg";
		
		Post po = new Post();
		po.post_author = "nezkeeeze";
		po.post_body = "Hey guys I'm new.  How do I get colored text?  Can I be an admin?  How do I start a new topic?<br /><br />" + application.getSession().getServer().serverTagline;
		po.post_avatar = "http://www.ape-apps.com/nezkeys.png";
		
		LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		View vC = vi.inflate(R.layout.category, null);
		View vT = vi.inflate(R.layout.thread, null);
		View vP = vi.inflate(R.layout.post, null);
		
		vC = ElementRenderer.renderCategory(vC,application,this,useOpenSans,useShading,cat,currentAvatarSetting);
		vT = ElementRenderer.renderCategory(vT,application,this,useOpenSans,useShading,top,currentAvatarSetting);
		vP = ElementRenderer.renderPost(vP,application,1,this,0,useOpenSans,useShading,po,fontSize,currentAvatarSetting);
		
		previewHolder.addView(vC);
		
		if(useDivider) {
			View d1 = vi.inflate(R.layout.preview_seperator, null);
			previewHolder.addView(d1);
		}
		
		previewHolder.addView(vT);
		
		if(useDivider) {
			View d2 = vi.inflate(R.layout.preview_seperator, null);
			previewHolder.addView(d2);
		}
		
		previewHolder.addView(vP);
		
		if(useDivider) {
			View d3 = vi.inflate(R.layout.preview_seperator, null);
			previewHolder.addView(d3);
		}
	}
	
	private void setColor(String color) {
		application.getSession().getServer().serverColor = color;
		application.getSession().updateServer();
		
		updatePreview();
	}
	
	private void setBackground(String color) {
		application.getSession().getServer().serverBackground = color;
		application.getSession().updateServer();
		
		updatePreview();
	}
	
	private void setTextColor(String color) {
		application.getSession().getServer().serverTextColor = color;
		application.getSession().updateServer();
		
		updatePreview();
	}
	
	private void setElementColor(String color) {
		application.getSession().getServer().serverBoxColor = color;
		application.getSession().updateServer();
		
		updatePreview();
	}
	
	private void juiceUpOptions() {
		Button btnAccent = (Button)findViewById(R.id.theme_editor_accent_color);
		btnAccent.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ColorPickerDialogFragment newFragment = ColorPickerDialogFragment.newInstance();
				newFragment.setOnColorSelectedListener(new ColorPickerDialogFragment.onColorSelectedListener() {
					
					public void onColorSelected(String color) {
						setColor(color);
					}
				});
			    newFragment.show(getSupportFragmentManager(), "dialog");
			}
			
		});
		
		Button btnBackground = (Button)findViewById(R.id.theme_editor_background_color);
		btnBackground.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ColorPickerDialogFragment newFragment = ColorPickerDialogFragment.newInstance();
				newFragment.setOnColorSelectedListener(new ColorPickerDialogFragment.onColorSelectedListener() {
					
					public void onColorSelected(String color) {
						setBackground(color);
					}
				});
			    newFragment.show(getSupportFragmentManager(), "dialog");
			}
			
		});
		
		Button btnTextColor = (Button)findViewById(R.id.theme_editor_text_color);
		btnTextColor.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ColorPickerDialogFragment newFragment = ColorPickerDialogFragment.newInstance();
				newFragment.setOnColorSelectedListener(new ColorPickerDialogFragment.onColorSelectedListener() {
					
					public void onColorSelected(String color) {
						setTextColor(color);
					}
				});
			    newFragment.show(getSupportFragmentManager(), "dialog");
			}
			
		});
		
		Button btnElementBorder = (Button)findViewById(R.id.theme_editor_borders);
		btnElementBorder.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String currentSetting = getString(R.string.default_element_border);
				
				if(application.getSession().getServer().serverBoxBorder != null) {
					currentSetting = application.getSession().getServer().serverBoxBorder;
				}

				if(currentSetting.contentEquals("1")) {
					application.getSession().getServer().serverBoxBorder = "0";
				} else {
					application.getSession().getServer().serverBoxBorder = "1";
				}
				
				application.getSession().updateServer();
				
				updatePreview();
			}
			
		});
		
		Button btnElementColor = (Button)findViewById(R.id.theme_editor_element_color);
		btnElementColor.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ColorPickerDialogFragment newFragment = ColorPickerDialogFragment.newInstance();
				
				Bundle bundle = new Bundle();
				bundle.putBoolean("show_opacity", true);
				newFragment.setArguments(bundle);
				
				newFragment.setOnColorSelectedListener(new ColorPickerDialogFragment.onColorSelectedListener() {
					
					public void onColorSelected(String color) {
						setElementColor(color);
					}
				});
			    newFragment.show(getSupportFragmentManager(), "dialog");
			}
			
		});
		
		Button btnWallpaper = (Button)findViewById(R.id.theme_editor_wallpaper);
		btnWallpaper.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				BackgroundUrlDialogFragment newFragment = BackgroundUrlDialogFragment.newInstance();
			    newFragment.show(getSupportFragmentManager(), "dialog");
			}
			
		});
	}

}
