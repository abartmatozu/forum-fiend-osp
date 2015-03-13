package com.forum.fiend.osp;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class ColorPickerDialogFragment extends DialogFragment {
	
	private boolean showOpacity = false;
	private int opacity = 255;
	
	private LinearLayout color_picker_colors_layout;
	
	static ColorPickerDialogFragment newInstance() {
		return new ColorPickerDialogFragment();
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		this.setStyle(STYLE_NO_TITLE, getTheme());
	}
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.color_picker_dialog, container, false);
		
		setupColorPicker(v);

        return v;
    }

	private OnClickListener colorSetter = new OnClickListener() {

		@Override
		public void onClick(View v) {
			String color = (String)v.getTag();
			setColor(color);
		}
		
	};
	
	private void setupColorPicker(View v) {
		
		Bundle bundle = getArguments();
		if(bundle != null) {
			if(bundle.containsKey("show_opacity")) {
				showOpacity = bundle.getBoolean("show_opacity");
			}
		}
		
		color_picker_colors_layout = (LinearLayout) v.findViewById(R.id.color_picker_colors_layout);
		
		LinearLayout opacityLayout = (LinearLayout) v.findViewById(R.id.color_picker_opacity_layout);
		
		if(showOpacity) {
			opacityLayout.setVisibility(View.VISIBLE);
			
			SeekBar color_opacity_seeker = (SeekBar)v.findViewById(R.id.color_opacity_seeker);
			color_opacity_seeker.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

				@Override
				public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
					// TODO Auto-generated method stub
					opacity = progress;
					float opacityPercent = progress / 255.0f;
					
					//Log.d("Color Picker",progress + "/255 = " + opacityPercent);
					
					color_picker_colors_layout.setAlpha(opacityPercent);
				}

				@Override
				public void onStartTrackingTouch(SeekBar seekBar) {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void onStopTrackingTouch(SeekBar seekBar) {
					// TODO Auto-generated method stub
					
				}
				
			});
		} else {
			opacityLayout.setVisibility(View.GONE);
		}
		
		LinearLayout llPicker = (LinearLayout) v.findViewById(R.id.profileColorPicker);
    	llPicker.setVisibility(View.VISIBLE);
    	
    	v.findViewById(R.id.pickColor1).setOnClickListener(colorSetter);
    	v.findViewById(R.id.pickColor2).setOnClickListener(colorSetter);
    	v.findViewById(R.id.pickColor3).setOnClickListener(colorSetter);
    	v.findViewById(R.id.pickColor4).setOnClickListener(colorSetter);
    	v.findViewById(R.id.pickColor5).setOnClickListener(colorSetter);
    	v.findViewById(R.id.pickColor6).setOnClickListener(colorSetter);
    	
    	v.findViewById(R.id.pickColor7).setOnClickListener(colorSetter);
    	v.findViewById(R.id.pickColor8).setOnClickListener(colorSetter);
    	v.findViewById(R.id.pickColor9).setOnClickListener(colorSetter);
    	v.findViewById(R.id.pickColor10).setOnClickListener(colorSetter);
    	v.findViewById(R.id.pickColor11).setOnClickListener(colorSetter);
    	v.findViewById(R.id.pickColor12).setOnClickListener(colorSetter);
    	
    	v.findViewById(R.id.pickColor13).setOnClickListener(colorSetter);
    	v.findViewById(R.id.pickColor14).setOnClickListener(colorSetter);
    	v.findViewById(R.id.pickColor15).setOnClickListener(colorSetter);
    	v.findViewById(R.id.pickColor16).setOnClickListener(colorSetter);
    	v.findViewById(R.id.pickColor17).setOnClickListener(colorSetter);
    	v.findViewById(R.id.pickColor18).setOnClickListener(colorSetter);

    	v.findViewById(R.id.pickColor19).setOnClickListener(colorSetter);
    	v.findViewById(R.id.pickColor20).setOnClickListener(colorSetter);
    	v.findViewById(R.id.pickColor21).setOnClickListener(colorSetter);
    	v.findViewById(R.id.pickColor22).setOnClickListener(colorSetter);
    	v.findViewById(R.id.pickColor23).setOnClickListener(colorSetter);
    	v.findViewById(R.id.pickColor24).setOnClickListener(colorSetter);
    	
    	v.findViewById(R.id.pickColor25).setOnClickListener(colorSetter);
    	v.findViewById(R.id.pickColor26).setOnClickListener(colorSetter);
    	v.findViewById(R.id.pickColor27).setOnClickListener(colorSetter);
    	v.findViewById(R.id.pickColor28).setOnClickListener(colorSetter);
    	v.findViewById(R.id.pickColor29).setOnClickListener(colorSetter);
    	v.findViewById(R.id.pickColor30).setOnClickListener(colorSetter);
    	
    	v.findViewById(R.id.pickColor31).setOnClickListener(colorSetter);
    	v.findViewById(R.id.pickColor32).setOnClickListener(colorSetter);
    	v.findViewById(R.id.pickColor33).setOnClickListener(colorSetter);
    	v.findViewById(R.id.pickColor34).setOnClickListener(colorSetter);
    	v.findViewById(R.id.pickColor35).setOnClickListener(colorSetter);
    	v.findViewById(R.id.pickColor36).setOnClickListener(colorSetter);

    	v.findViewById(R.id.pickColor37).setOnClickListener(colorSetter);
    	v.findViewById(R.id.pickColor38).setOnClickListener(colorSetter);
    	v.findViewById(R.id.pickColor39).setOnClickListener(colorSetter);
    	v.findViewById(R.id.pickColor40).setOnClickListener(colorSetter);
    	v.findViewById(R.id.pickColor41).setOnClickListener(colorSetter);
    	v.findViewById(R.id.pickColor42).setOnClickListener(colorSetter);
    	

	}
	
	private void setColor(String color) {
		if(colorSelected == null) {
			this.dismiss();
			return;
		}
		
		if(opacity < 255) {
			String alphaValue = Integer.toHexString(opacity);
			
			if(alphaValue.length() == 1) {
				alphaValue = "0" + alphaValue;
			}
			
			color = color.replace("#", "#" + alphaValue);
			
			Log.d("Color Picker","Aplha'd color is: " + color);
		}
		
		colorSelected.onColorSelected(color);
		
		this.dismiss();
	}
	
	//Color Selected Interface
	public interface onColorSelectedListener {
		public abstract void onColorSelected(String color);
	}
	
	private onColorSelectedListener colorSelected = null;
	
	public void setOnColorSelectedListener(onColorSelectedListener l) {
		colorSelected = l;
	}
}