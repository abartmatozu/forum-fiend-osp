package com.forum.fiend.osp;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.WindowManager;
import android.widget.TextView;

@SuppressLint("NewApi")
public class ThemeSetter {
	
	public static final void setNavBarOnly(Activity activity,String color) {

		if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {

			activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);

			activity.getWindow().setNavigationBarColor(Color.parseColor(darkenColor(color)));
		}
	}
	
	public static final void setNavAndStatusBar(Activity activity,String color) {

		if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
			//activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
			activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
			activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
			//activity.getWindow().setStatusBarColor(Color.parseColor(darkenColor(color)));
			
			activity.getWindow().setStatusBarColor(Color.parseColor(darkenColor(color)));
			activity.getWindow().setNavigationBarColor(Color.parseColor(darkenColor(color)));
		}
	}
	
	public static final void setActionBar(Activity activity,String color) {
		if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {

			ActionBar bar = activity.getActionBar();
			bar.setBackgroundDrawable(new ColorDrawable(Color.parseColor(color)));
			
			int actionBarTitleId = Resources.getSystem().getIdentifier("action_bar_title", "id", "android");
    		TextView actionBarTextView = (TextView)activity.findViewById(actionBarTitleId); 
    		
    		if(actionBarTextView != null) {
    			actionBarTextView.setTextColor(Color.parseColor(getForeground(color)));
    		}
    		
    		int actionBarSubTitleId = Resources.getSystem().getIdentifier("action_bar_subtitle", "id", "android");
    		TextView actionBarSubTextView = (TextView)activity.findViewById(actionBarSubTitleId); 
    		
    		if(actionBarSubTextView != null) {
    			actionBarSubTextView.setTextColor(Color.parseColor(getForeground(color)));
    			actionBarSubTextView.setAlpha(0.5f);
    		}

		} else {
			activity.setTheme(android.R.style.Theme_Light);
		}

		if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
			activity.getWindow().setStatusBarColor(Color.parseColor(darkenColor(color)));
			activity.getWindow().setNavigationBarColor(Color.parseColor(darkenColor(color)));
		}
	}
	
	public static final void setActionBarNoElevation(Activity activity,String color) {
		if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {

			ActionBar bar = activity.getActionBar();
			bar.setBackgroundDrawable(new ColorDrawable(Color.parseColor(color)));
			
			int actionBarTitleId = Resources.getSystem().getIdentifier("action_bar_title", "id", "android");
    		TextView actionBarTextView = (TextView)activity.findViewById(actionBarTitleId); 
    		
    		if(actionBarTextView != null) {
    			actionBarTextView.setTextColor(Color.parseColor(getForeground(color)));
    		}
    		
    		int actionBarSubTitleId = Resources.getSystem().getIdentifier("action_bar_subtitle", "id", "android");
    		TextView actionBarSubTextView = (TextView)activity.findViewById(actionBarSubTitleId); 
    		
    		if(actionBarSubTextView != null) {
    			actionBarSubTextView.setTextColor(Color.parseColor(getForeground(color)));
    			actionBarSubTextView.setAlpha(0.5f);
    		}
			
		} else {
			activity.setTheme(android.R.style.Theme_Light);
		}
		
		
		if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
			activity.getWindow().setStatusBarColor(Color.parseColor(darkenColor(color)));
			activity.getWindow().setNavigationBarColor(Color.parseColor(darkenColor(color)));
			activity.getActionBar().setElevation(0f);
		}
	}
	
	public static final void setThemeNoTitlebar(Activity activity,String color) {
		if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
			
			if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
				activity.setTheme(android.R.style.Theme_Holo_Light_NoActionBar);
			} else {
				if(android.os.Build.VERSION.SDK_INT == android.os.Build.VERSION_CODES.LOLLIPOP) {
					activity.setTheme(android.R.style.Theme_Material_Light_NoActionBar_TranslucentDecor);
				} else {
					activity.setTheme(android.R.style.Theme_DeviceDefault_Light_NoActionBar);
				}
				
			}

		} else {
			activity.setTheme(android.R.style.Theme_Light_NoTitleBar);
		}
	}
	
	public static final void setThemeFullscreen(Activity activity,String color) {
		if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
			
			if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
				activity.setTheme(android.R.style.Theme_Holo_Light_NoActionBar_Fullscreen);
			} else {
				activity.setTheme(android.R.style.Theme_DeviceDefault_Light_NoActionBar_Fullscreen);
			}

		} else {
			activity.setTheme(android.R.style.Theme_Light_NoTitleBar_Fullscreen);
		}
	}
	
	public static final void setTheme(Activity activity,String color) {
		if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
			
			if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
				activity.setTheme(android.R.style.Theme_Holo_Light);
			} else {
				if(getForegroundDark(color)) {
					activity.setTheme(android.R.style.Theme_DeviceDefault_Light);
				} else {
					activity.setTheme(android.R.style.Theme_DeviceDefault_Light_DarkActionBar);
				}
			}

		} else {
			activity.setTheme(android.R.style.Theme_Light);
		}

	}
	
	public static String darkenColor(String colorStr) {
		
		int r = Integer.valueOf( colorStr.substring( 1, 3 ), 16 );
		int g = Integer.valueOf( colorStr.substring( 3, 5 ), 16 );
		int b = Integer.valueOf( colorStr.substring( 5, 7 ), 16 );
		
	    if(r > 40) {
	    	r -= 40;
	    } else {
	    	r = 0;
	    }
	    
	    if(g > 40) {
	    	g -= 40;
	    } else {
	    	g = 0;
	    }
	    
	    if(b > 40) {
	    	b -= 40;
	    } else {
	    	b = 0;
	    }
	    
	    String hexR = Integer.toHexString(r);
	    String hexG = Integer.toHexString(g);
	    String hexB = Integer.toHexString(b);
	    
	    if(hexR.length() == 1) {
	    	hexR = "0" + hexR;
	    }
	    
	    if(hexG.length() == 1) {
	    	hexG = "0" + hexG;
	    }
	    
	    if(hexB.length() == 1) {
	    	hexB = "0" + hexB;
	    }
	    
	    Log.d("Forum Fiend","Converted " + r + "," + g + "," + b + " to " + "#" + hexR + hexG + hexB);
	    
	    return "#" + hexR + hexG + hexB;
	}
	
	public static String getForeground(String hexColor) {
		if(getForegroundDark(hexColor)) {
			return "#000000";
		} else {
			return "#ffffff";
		}
	}
	
	public static boolean getForegroundDark(String hexColor) {
		
		if(hexColor == null) {
			return false;
		}
		
		if(!hexColor.contains("#")) {
			return false;
		}
		
		if(hexColor.length() != 7) {
			return false;
		}
		
		int red = Integer.valueOf( hexColor.substring( 1, 3 ), 16 );
		int green = Integer.valueOf( hexColor.substring( 3, 5 ), 16 );
		int blue = Integer.valueOf( hexColor.substring( 5, 7 ), 16 );
		
		int totalColor = red + green + blue;
		
		if(totalColor > 382) {
			return true;
		} else {
			return false;
		}
	}
}
