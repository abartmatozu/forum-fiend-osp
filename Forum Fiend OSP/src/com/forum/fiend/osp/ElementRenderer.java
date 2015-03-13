package com.forum.fiend.osp;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.nostra13.universalimageloader.core.ImageLoader;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.Html;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ElementRenderer {
	
	public static final View renderPost(View v,ForumFiendApp application,int page,Context c,int arg0,boolean useOpenSans,boolean useShading,Post po,int fontSize,boolean currentAvatarSetting) {
		TextView poAuthor = (TextView)v.findViewById(R.id.post_author);
		TextView poTimestamp = (TextView)v.findViewById(R.id.post_timestamp);
		TextView poPage = (TextView)v.findViewById(R.id.post_number);
		
		TextView tvThanks = (TextView)v.findViewById(R.id.post_thanks_count);
		TextView tvLikes = (TextView)v.findViewById(R.id.post_likes_count);
		
		TextView tvOnline = (TextView)v.findViewById(R.id.post_online_status);

		Typeface opensans = Typeface.createFromAsset(c.getAssets(), "fonts/opensans.ttf");
		
		if(page == -1) {
			poPage.setVisibility(View.GONE);
		} else {
			int postNumber = ((page - 1) * 20) + (arg0 + 1);
			poPage.setText("#" + postNumber);
		}
		

		LinearLayout ll_border_background = (LinearLayout) v.findViewById(R.id.ll_border_background);
		LinearLayout ll_color_background = (LinearLayout) v.findViewById(R.id.ll_color_background);

		String textColor = c.getString(R.string.default_text_color);
		
		if(application.getSession().getServer().serverTextColor.contains("#")) {
			textColor = application.getSession().getServer().serverTextColor;
		}
		
		String boxColor = c.getString(R.string.default_element_background);
		
		if(application.getSession().getServer().serverBoxColor != null) {
			boxColor = application.getSession().getServer().serverBoxColor;
		}
		
		if(boxColor.contains("#")) {
			ll_color_background.setBackgroundColor(Color.parseColor(boxColor));
		} else {
			ll_color_background.setBackgroundColor(Color.TRANSPARENT);
		}
		
		
		String boxBorder = c.getString(R.string.default_element_border);
		
		if(application.getSession().getServer().serverBoxBorder != null) {
			boxBorder = application.getSession().getServer().serverBoxBorder;
		}
		
		if(boxBorder.contentEquals("1")) {
			ll_border_background.setBackgroundResource(R.drawable.element_border);
		} else {
			ll_border_background.setBackgroundColor(Color.TRANSPARENT);
		}
		

		if(useOpenSans) {
			poAuthor.setTypeface(opensans);
			poTimestamp.setTypeface(opensans);
			tvThanks.setTypeface(opensans);
			tvLikes.setTypeface(opensans);
			tvOnline.setTypeface(opensans);
			poPage.setTypeface(opensans);
		}
		
		if(useShading) {
			poAuthor.setShadowLayer(2, 0, 0, Color.parseColor("#66000000"));
			tvThanks.setShadowLayer(2, 0, 0, Color.parseColor("#66000000"));
			tvLikes.setShadowLayer(2, 0, 0, Color.parseColor("#66000000"));
			tvOnline.setShadowLayer(2, 0, 0, Color.parseColor("#66000000"));
		}

		LinearLayout llPostBodyHolder = (LinearLayout) v.findViewById(R.id.post_body_holder);

		llPostBodyHolder.removeAllViews();
		
		//llPostBodyHolder.setMovementMethod(null);
		
		
		ImageView poAvatar = (ImageView)v.findViewById(R.id.post_avatar);
		
		if(boxColor != null && boxColor.contains("#") && boxColor.length() == 7) {
			ImageView post_avatar_frame = (ImageView)v.findViewById(R.id.post_avatar_frame);
			post_avatar_frame.setColorFilter(Color.parseColor(boxColor));
		} else {
			ImageView post_avatar_frame = (ImageView)v.findViewById(R.id.post_avatar_frame);
			post_avatar_frame.setVisibility(View.GONE);
		}

		if(po.userOnline) {
			tvOnline.setText("ONLINE");
			tvOnline.setVisibility(View.VISIBLE);
		} else {
			tvOnline.setVisibility(View.GONE);
		}
		
		poAuthor.setText(po.post_author);
		
		String timeString = po.post_timestamp;
		
		try {
			Date date = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy",Locale.ENGLISH).parse(po.post_timestamp);
			
			Date now = new Date();
			
			long difference = now.getTime() - date.getTime();
			
			long seconds = difference / 1000;
			
			timeString = seconds + "s";
			
			if(seconds > 59) {
				long minutes = seconds / 60;
				timeString = minutes + "m";
				
				if(minutes > 59) {
					long hours = minutes / 60;
					timeString = hours + "h";
					
					if(hours > 23) {
						long days = hours / 24;
						timeString = days + "d";
					}
				}
			}
			
			poTimestamp.setText(timeString);
		} catch(Exception ex) {
			poTimestamp.setVisibility(View.GONE);
			tvOnline.setText(po.post_timestamp);
			tvOnline.setVisibility(View.VISIBLE);
			tvOnline.setTextColor(Color.parseColor(textColor));
		}
		
		
		
		tvThanks.setText("+" + Integer.toString(po.thanksCount) + " Thanks");
		tvLikes.setText("+" + Integer.toString(po.likeCount) + " Likes");
		
		if(po.thanksCount == 0) {
			tvThanks.setVisibility(View.GONE);
		} else {
			tvThanks.setVisibility(View.VISIBLE);
		}
		
		if(po.likeCount == 0) {
			tvLikes.setVisibility(View.GONE);
		} else {
			tvLikes.setVisibility(View.VISIBLE);
		}
		
		if(po.userBanned) {
			poAuthor.setPaintFlags(poAuthor.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
			poAuthor.setTextColor(Color.LTGRAY);
		}
		
		String postContent = po.post_body;
		
		BBCodeParser.parseCode(c, llPostBodyHolder, postContent, opensans, useOpenSans, useShading, po.attachmentList, fontSize,true,textColor,application);

		poAuthor.setTextColor(Color.parseColor(textColor));
		poTimestamp.setTextColor(Color.parseColor(textColor));
		poPage.setTextColor(Color.parseColor(textColor));

		if(po.categoryModerator != null) {
			if(po.post_author_id != null) {
				if(po.post_author_id.contentEquals(po.categoryModerator) && !po.categoryModerator.contentEquals("0")) {
					poAuthor.setTextColor(Color.BLUE);
				}
			}
		}
		
		if(po.post_author_level.contentEquals("D")) {
			poAuthor.setTextColor(Color.parseColor("#ffcc00"));
		}


        if(currentAvatarSetting) {
			if(po.post_avatar != null && po.post_avatar.contains("http://")) {
				String imageUrl = po.post_avatar;
				ImageLoader.getInstance().displayImage(imageUrl, poAvatar);
			} else {
				poAvatar.setImageResource(R.drawable.no_avatar);
			}
        } else {
        	poAvatar.setVisibility(View.GONE);
        }
        
        return v;
	}
	
	public static final View renderCategory(View v,ForumFiendApp application,Context c,boolean useOpenSans,boolean useShading,Category ca,boolean currentAvatarSetting) {
		TextView tvCategoryName = (TextView) v.findViewById(R.id.category_name);
		TextView tvCategoryLastThread = (TextView) v.findViewById(R.id.category_last_thread);
		TextView tvCategoryUpdate = (TextView) v.findViewById(R.id.category_last_update);
		ImageView ivSubforumIndicator = (ImageView) v.findViewById(R.id.category_subforum_indicator);
		TextView tvThreadReplies = (TextView) v.findViewById(R.id.thread_replies);
		TextView tvThreadViews = (TextView) v.findViewById(R.id.thread_views);

		LinearLayout ll_border_background = (LinearLayout) v.findViewById(R.id.ll_border_background);
		LinearLayout ll_color_background = (LinearLayout) v.findViewById(R.id.ll_color_background);

		String textColor = c.getString(R.string.default_text_color);
		
		if(application.getSession().getServer().serverTextColor.contains("#")) {
			textColor = application.getSession().getServer().serverTextColor;
		}
		
		String boxColor = c.getString(R.string.default_element_background);
		
		if(application.getSession().getServer().serverBoxColor != null) {
			boxColor = application.getSession().getServer().serverBoxColor;
		}
		
		if(boxColor.contains("#")) {
			ll_color_background.setBackgroundColor(Color.parseColor(boxColor));
		} else {
			ll_color_background.setBackgroundColor(Color.TRANSPARENT);
		}
		
		
		String boxBorder = c.getString(R.string.default_element_border);
		
		if(application.getSession().getServer().serverBoxBorder != null) {
			boxBorder = application.getSession().getServer().serverBoxBorder;
		}
		
		if(boxBorder.contentEquals("1")) {
			ll_border_background.setBackgroundResource(R.drawable.element_border);
		} else {
			try {
				ll_border_background.setBackgroundResource(0);
			} catch(Exception ex) {
				// Android might be old version that cannot set background
				// didn't want to research which
			}
		}
		
		Typeface opensans = Typeface.createFromAsset(c.getAssets(), "fonts/opensans.ttf");
		
		if(useOpenSans) {
			tvCategoryName.setTypeface(opensans);
			tvCategoryLastThread.setTypeface(opensans);
			tvCategoryUpdate.setTypeface(opensans);
		}
		
		if(ca.categoryType.contentEquals("S")) {
			tvCategoryLastThread.setVisibility(View.GONE);
			tvCategoryUpdate.setVisibility(View.GONE);
		} else {
			tvCategoryLastThread.setVisibility(View.VISIBLE);
			tvCategoryUpdate.setVisibility(View.VISIBLE);
		}
		
		
		
		tvCategoryName.setTextColor(Color.parseColor(textColor));
		tvCategoryLastThread.setTextColor(Color.parseColor(textColor));
		tvCategoryUpdate.setTextColor(Color.parseColor(textColor));
		
		if(tvThreadReplies != null) {
			tvThreadReplies.setTextColor(Color.parseColor(textColor));
		}
		
		if(tvThreadViews != null) {
			tvThreadViews.setTextColor(Color.parseColor(textColor));
		}
		
		
		if(useShading) {
			tvCategoryName.setShadowLayer(2, 0, 0, Color.parseColor("#66000000"));
		}
		
		String timeAgo = ca.category_lastupdate;
		
		if(ca.categoryType.contentEquals("C")) {
			try {
				String string = ca.category_lastupdate;
				Date date = new SimpleDateFormat("MMM dd, yyyy hh:mm:ss a",Locale.ENGLISH).parse(string);
				
				Date now = new Date();
				
				long difference = now.getTime() - date.getTime();
				
				long seconds = difference / 1000;
				
				timeAgo = seconds + "s";
				
				if(seconds > 59) {
					long minutes = seconds / 60;
					timeAgo = minutes + "m";
					
					if(minutes > 59) {
						long hours = minutes / 60;
						timeAgo = hours + "h";
						
						if(hours > 23) {
							long days = hours / 24;
							timeAgo = days + "d";
						}
					}
				}
			} catch(Exception ex) {
				timeAgo = ca.category_lastupdate;
			}
		}
		
		tvCategoryName.setText(ca.category_name);
		tvCategoryLastThread.setText(Html.fromHtml(ca.category_lastthread));
		tvCategoryUpdate.setText(timeAgo);
		
		if(ca.isLocked) {
			tvCategoryName.setTextColor(Color.LTGRAY);
			tvCategoryName.setText("LOCKED: " + ca.category_name);
		}
		
		if(useOpenSans) {
			if(ca.hasNewTopic) {
				tvCategoryName.setTypeface(opensans, Typeface.BOLD);
			} else {
				tvCategoryName.setTypeface(opensans, Typeface.NORMAL);
			}
		} else {
			if(ca.hasNewTopic) {
				tvCategoryName.setTypeface(null, Typeface.BOLD);
			} else {
				tvCategoryName.setTypeface(null, Typeface.NORMAL);
			}
		}

		if(currentAvatarSetting) {
			if(ca.categoryType.contentEquals("S")) {
				if(ivSubforumIndicator != null) {
					ivSubforumIndicator.setVisibility(View.VISIBLE);
					
					if(ca.categoryIcon.contains("http")) {
						String imageUrl = ca.categoryIcon;
						ImageLoader.getInstance().displayImage(imageUrl, ivSubforumIndicator);
					} else {
						if(ca.category_URL.contains("http")) {
							ivSubforumIndicator.setImageResource(R.drawable.social_global_on);
						} else {
							
							ivSubforumIndicator.setImageResource(R.drawable.default_unread);
							
							if(ca.hasNewTopic) {
								if(application.getSession().getServer().serverColor.contains("#")) {
									String appColor = application.getSession().getServer().serverColor;
									ivSubforumIndicator.setColorFilter(Color.parseColor(appColor));
								} else {
									ivSubforumIndicator.setColorFilter(Color.BLACK);
								}
							} else {
								ivSubforumIndicator.setColorFilter(Color.BLACK);
							}
							
							/*
							if(ca.hasNewTopic) {
								if(ca.hasChildren) {
									ivSubforumIndicator.setImageResource(R.drawable.category_unread);
								} else {
									ivSubforumIndicator.setImageResource(R.drawable.default_unread);
								}
								
							} else {
								if(ca.hasChildren) {
									ivSubforumIndicator.setImageResource(R.drawable.category_read);
								} else {
									ivSubforumIndicator.setImageResource(R.drawable.default_read);
								}
							}
							*/
						}
					}
				}
			} else {
				if(ivSubforumIndicator != null) {
					if(ca.categoryIcon.contains("http")) {
						String imageUrl = ca.categoryIcon;
						ImageLoader.getInstance().displayImage(imageUrl, ivSubforumIndicator);
					} else {
						ivSubforumIndicator.setImageResource(R.drawable.no_avatar);
					}
				}
				
				if(boxColor != null && boxColor.contains("#") && boxColor.length() == 7) {
					ImageView category_subforum_indicator_frame = (ImageView)v.findViewById(R.id.category_subforum_indicator_frame);
					category_subforum_indicator_frame.setColorFilter(Color.parseColor(boxColor));
				} else {
					ImageView category_subforum_indicator_frame = (ImageView)v.findViewById(R.id.category_subforum_indicator_frame);
					category_subforum_indicator_frame.setVisibility(View.GONE);
				}
				
			}
		} else {
			ivSubforumIndicator.setVisibility(View.GONE);
			View indicator = v.findViewById(R.id.category_subforum_indicator_frame);
			if(indicator != null) {
				indicator.setVisibility(View.GONE);
			}
		}
		
		if(ca.categoryType.contentEquals("C")) {
			if(tvThreadReplies != null) {
				if(ca.thread_count != null) {
					tvThreadReplies.setText(ca.thread_count);
				} else {
					tvThreadReplies.setVisibility(View.GONE);
				}
			}
			
			if(tvThreadViews != null) {
				if(ca.view_count != null) {
					tvThreadViews.setText(ca.view_count);
				} else {
					tvThreadViews.setVisibility(View.GONE);
				}
			}
		}
		
		if(ca.topicSticky.contentEquals("Y")) {
			
			tvCategoryName.setTextColor(Color.RED);
			
			if(useShading) {
				tvCategoryName.setShadowLayer(2, 0, 0, Color.parseColor("#66ff0000"));
			}
		}

		if(ca.category_URL.contains("http")) {
			tvCategoryUpdate.setVisibility(View.VISIBLE);
			tvCategoryUpdate.setText(ca.category_URL);
		}
		
		return v;
	}
}
