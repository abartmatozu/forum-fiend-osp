package com.forum.fiend.osp;

import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class TextDialogFragment extends DialogFragment {
	
	private CheckBox cbShading;
	private CheckBox cbOpenSans;
	private SeekBar sbFontSize;
	private TextView tbSample;
	
	private boolean useShading = false;
	private boolean useOpenSans = false;
	private int fontSize = 20;
	
	private Typeface opensans;
	
	static TextDialogFragment newInstance() {
		return new TextDialogFragment();
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		opensans = Typeface.createFromAsset(getActivity().getAssets(), "fonts/opensans.ttf");
		
		this.setStyle(STYLE_NO_TITLE, getTheme());
	}
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.text_options, container, false);
		
		setupDialog(v);

        return v;
    }
	
	private void setupDialog(View v) {
		
		cbShading = (CheckBox)v.findViewById(R.id.text_settings_cb_smoothing);
		cbOpenSans = (CheckBox)v.findViewById(R.id.text_settings_cb_opensans);
		sbFontSize = (SeekBar)v.findViewById(R.id.text_settings_sb_font_size);
		tbSample = (TextView)v.findViewById(R.id.text_settings_tb_sample);
		
		cbShading.setOnCheckedChangeListener(shadingChecked);
		cbOpenSans.setOnCheckedChangeListener(sansChecked);
		sbFontSize.setOnSeekBarChangeListener(fontSizeChanged);
		
		getCurrentValues();
		updateSample();
	}
	
	private void getCurrentValues() {
		SharedPreferences app_preferences = getActivity().getSharedPreferences("prefs", 0);
		
		useShading = app_preferences.getBoolean("use_shading", false);
		useOpenSans = app_preferences.getBoolean("use_opensans", false);
		fontSize = app_preferences.getInt("font_size", 16);
		
		sbFontSize.setProgress(fontSize - 10);
		cbShading.setChecked(useShading);
		cbOpenSans.setChecked(useOpenSans);
	}
	
	private void storeNewValues() {
		SharedPreferences app_preferences = getActivity().getSharedPreferences("prefs", 0);
		SharedPreferences.Editor editor = app_preferences.edit();
		editor.putBoolean("use_shading", useShading);
		editor.putBoolean("use_opensans", useOpenSans);
		editor.putInt("font_size", fontSize);
		editor.commit();
	}
	
	private void updateSample() {
		tbSample.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize);
		
		if(useShading) {
			tbSample.setShadowLayer(2, 0, 0, tbSample.getCurrentTextColor());
		} else {
			tbSample.setShadowLayer(0, 0, 0, 0);
		}
		
		if(useOpenSans) {
			tbSample.setTypeface(opensans);
		} else {
			tbSample.setTypeface(null);
		}
		
		storeNewValues();
	}
	
	private OnCheckedChangeListener shadingChecked = new OnCheckedChangeListener() {

		@Override
		public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
			useShading = isChecked;
			updateSample();
		}
		
	};
	
	private OnCheckedChangeListener sansChecked = new OnCheckedChangeListener() {

		@Override
		public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
			useOpenSans = isChecked;
			updateSample();
		}
		
	};
	
	private OnSeekBarChangeListener fontSizeChanged = new OnSeekBarChangeListener() {

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) {
			
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			fontSize = seekBar.getProgress() + 10;
			updateSample();
		}
		
	};

}
