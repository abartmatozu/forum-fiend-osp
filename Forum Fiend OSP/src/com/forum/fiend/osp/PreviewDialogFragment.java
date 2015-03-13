package com.forum.fiend.osp;

import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;


public class PreviewDialogFragment extends DialogFragment {

	private LinearLayout preview_dialog_linear_layout;
	
	private boolean useShading = false;
	private boolean useOpenSans = false;
	private int fontSize = 20;
	
	private String previewText;
	
	private Typeface opensans;
	
	static PreviewDialogFragment newInstance() {
		return new PreviewDialogFragment();
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		opensans = Typeface.createFromAsset(getActivity().getAssets(), "fonts/opensans.ttf");
		
		SharedPreferences app_preferences = getActivity().getSharedPreferences("prefs", 0);
		
		useShading = app_preferences.getBoolean("use_shading", false);
		useOpenSans = app_preferences.getBoolean("use_opensans", true);
		fontSize = app_preferences.getInt("font_size", 16);
		
		this.setStyle(DialogFragment.STYLE_NORMAL, getTheme());
		
	}
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.preview_dialog_layout, container, false);
		
		setupDialog(v);

        return v;
    }
	
	private void setupDialog(View v) {
		preview_dialog_linear_layout = (LinearLayout)v.findViewById(R.id.preview_dialog_linear_layout);
		
		Bundle bundle = getArguments();
		previewText = bundle.getString("text");
		
		showPreview();
		
		this.getDialog().setTitle("Preview");
	}
	
	private void showPreview() {
		BBCodeParser.parseCode(getActivity(), preview_dialog_linear_layout, previewText, opensans, useOpenSans, useShading, null, fontSize,false,"#333333",(ForumFiendApp)getActivity().getApplication());
	}

}
