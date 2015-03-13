package com.forum.fiend.osp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

public class BackgroundUrlDialogFragment extends DialogFragment {
	
	private EditText etUrl;
	private Button btnSave;
	private Button btnBackgroundBag;
	private Button btnClearWallpaper;
	private String currentURL = "";
	private ForumFiendApp application;
	
	static BackgroundUrlDialogFragment newInstance() {
		return new BackgroundUrlDialogFragment();
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		application = (ForumFiendApp)getActivity().getApplication();
		currentURL = application.getSession().getServer().serverWallpaper;

		this.setStyle(STYLE_NO_TITLE, getTheme());
	}
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.wallpaper_url_window, container, false);
		
		setupDialog(v);

        return v;
    }
	
	private void setupDialog(View v) {
		
		etUrl = (EditText)v.findViewById(R.id.etWallpaperURL);
		btnSave = (Button)v.findViewById(R.id.btnSetWallpaper);
		btnBackgroundBag = (Button)v.findViewById(R.id.btnFindWallpapers);
		btnClearWallpaper = (Button)v.findViewById(R.id.btnClearWallpaper);
		
		etUrl.setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if(hasFocus) {
					if(etUrl.getText().length() > 0) {
						etUrl.selectAll();
					}
				}
			}
			
		});
		
		if(currentURL.contains("http")) {
			etUrl.setText(currentURL);
		}
		
		btnBackgroundBag.setOnClickListener(goToBackgroundBag);
		btnClearWallpaper.setOnClickListener(clearWallpaper);
		btnSave.setOnClickListener(saveURL);
	}
	
	private OnClickListener goToBackgroundBag = new OnClickListener() {

		@Override
		public void onClick(View v) {
			Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.backgroundbag.com"));
			startActivity(browserIntent);
		}
		
	};
	
	private OnClickListener saveURL = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if(etUrl.getText().toString().trim().contains("http")) {
				currentURL = application.getSession().getServer().serverWallpaper = etUrl.getText().toString().trim();
				application.getSession().updateServer();
				BackgroundUrlDialogFragment.this.dismiss();
				
				getActivity().finish();
				getActivity().startActivity(getActivity().getIntent());
			
			}
		}
		
	};
	
	private OnClickListener clearWallpaper = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if(etUrl.getText().toString().trim().contains("http")) {
				currentURL = application.getSession().getServer().serverWallpaper = "0";
				application.getSession().updateServer();
				BackgroundUrlDialogFragment.this.dismiss();
				
				getActivity().finish();
				getActivity().startActivity(getActivity().getIntent());
			
			}
		}
		
	};
}
