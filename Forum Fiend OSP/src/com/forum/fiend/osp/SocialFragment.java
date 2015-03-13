package com.forum.fiend.osp;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import java.util.Vector;


import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.AsyncTask.Status;
import android.os.CountDownTimer;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;

import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

@SuppressLint("NewApi")
public class SocialFragment extends Fragment {

	private ListView socialList;
	private EditText new_status;

	private Post selected_post;

	private String chatForum = "0";
	private String chatThread = "0";

	private int socialRate = 30000;
	
	
	private Button update_status_button;
	
	private download_statuses socialLoader;
	
	private ForumFiendApp application;
	
	private SocialTimer socialTimer;
	
	private String newChatId = "0";
	
	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		
		application = (ForumFiendApp)getActivity().getApplication();
		
		newChatId = application.getSession().getServer().ffChatId;

		Log.d("Forum Fiend","newChatId is " + newChatId);

		setHasOptionsMenu(true);
	}
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
        View v = inflater.inflate(R.layout.social, container, false);
        return v;
    }

	@Override
	public void onStart() {
		
		super.onStart();

        chatForum = application.getSession().getServer().chatForum;
        chatThread = application.getSession().getServer().chatThread;

        socialList = (ListView) getActivity().findViewById(R.id.social_list_view);
        socialList.setDivider(null);
        
        update_status_button = (Button) getActivity().findViewById(R.id.social_submit_status);
        new_status = (EditText) getActivity().findViewById(R.id.social_status);
        
        if(application.getSession().getServer().serverColor.contains("#")) {
        	update_status_button.setTextColor(Color.parseColor(application.getSession().getServer().serverColor));
        }
        
        if(application.getSession().getServer().serverTextColor.contains("#")) {
        	new_status.setTextColor(Color.parseColor(application.getSession().getServer().serverTextColor));
        	
        	if(application.getSession().getServer().serverColor.contentEquals(application.getSession().getServer().serverBoxColor)) {
            	update_status_button.setTextColor(Color.parseColor(application.getSession().getServer().serverTextColor));
            }
        	
        }

        Bundle bundle = getArguments();
        String shared_text = bundle.getString("shared_text");
        if(shared_text.length() > 1) {
        	new_status.setText(shared_text);
        }
        
        new_status.setOnEditorActionListener(new OnEditorActionListener() {

			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_DONE) {
					submitPost();
					return true;
				}
				return false;
			}
        	
        });

        update_status_button.setOnClickListener(update_status);
        
        String boxColor = getString(R.string.default_element_background);
		
		if(application.getSession().getServer().serverBoxColor != null) {
			boxColor = application.getSession().getServer().serverBoxColor;
		}
        
		if(boxColor.contains("#")) {
			LinearLayout chat_input_area = (LinearLayout)getActivity().findViewById(R.id.chat_input_area);
			chat_input_area.setBackgroundColor(Color.parseColor(boxColor));
		}
        
	}
	
	@Override
	public void onDestroy() {
		
		if(socialTimer != null) {
			socialTimer.cancel();
			socialTimer = null;
		}
		
		super.onDestroy();
	}
	
	@Override
	public void onPause() { 
		if(socialTimer != null) {
			socialTimer.cancel();
		}
		
		super.onPause();
	}
	
	
	@Override
	public void onResume() {

		/*
		SharedPreferences app_preferences = getActivity().getSharedPreferences("prefs", 0);
		String cached_social = app_preferences.getString("social_list", "n/a");

        if(!(cached_social.contentEquals("n/a"))) {
        	try {
	    		Object[] forumObject = GsonHelper.customGson.fromJson(cached_social, Object[].class);
        		parseCachedSocial(forumObject);
        	} catch(Exception ex) {
        		//don't do anything
        	}
        }
		*/
		
		
		load_statuses();
		
		if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
			getActivity().invalidateOptionsMenu();
		}
		
        super.onResume();   
        
        socialTimer = new SocialTimer(socialRate,1000);
        socialTimer.start();
    }
	
	
	@Override
	public void onStop() {
		super.onStop();
		
		//Stop any running tasks
		if(socialLoader != null) {
			if(socialLoader.getStatus() == Status.RUNNING) {
				socialLoader.cancel(true);
			}
		}
	}
	
	private void load_statuses() {
		socialLoader = new download_statuses();
		
		if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
			socialLoader.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		} else {
			socialLoader.execute();
		}
		
	}
	
	private void submitPost() {
		new_status.setEnabled(false);
		update_status_button.setEnabled(false);
		
		if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
			new socialPoster().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		} else {
			new socialPoster().execute();
		}
	}

	private OnClickListener update_status = new OnClickListener() {

		public void onClick(View v) {
			submitPost();
		}
		
	};
	
	private class socialPoster extends AsyncTask<String, Void, Object[]> {
		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		
		protected Object[] doInBackground(String... params) {

			//String tagline = application.getSession().getServer().serverTagline;

			
			String comment = new_status.getText().toString().trim();
			String subject = "RE: Social";
			
			/*
			if(tagline.length() > 0) {
        		comment = comment + "\n\n" + tagline;
        	}
        	*/

			Object[] result = new Object[50];

			try
			{

			    Vector paramz = new Vector();
			    paramz.addElement(chatForum);
			    paramz.addElement(chatThread);
			    paramz.addElement(subject.getBytes());
			    paramz.addElement(comment.getBytes());


			    result[0] = application.getSession().performSynchronousCall("reply_post", paramz);

			}
			catch(Exception e)
			{
				Log.w("Discussions",e.getMessage());
				return null;
			}
			
			
			return result;
		}
		
		protected void onPostExecute(final Object[] result) {
			if(result == null) {
				
				
				Toast toast = Toast.makeText(getActivity(), "Error connecting to the server!  erSPE", Toast.LENGTH_SHORT);
				toast.show();
				
				return;
			}

			load_statuses();
			new_status.setText("");
			update_status_button.setEnabled(true);
			new_status.setEnabled(true);
		}
    }
	
	
	
	private class download_statuses extends AsyncTask<String, Void, Object[]> {
		@SuppressWarnings({ "unchecked", "rawtypes" })
		@Override
		protected Object[] doInBackground(String... params) {

			Object[] result = new Object[50];
			
			int minPost = 0;
			int maxPost = 19;
			
			try
			{

			    Vector paramz = new Vector();
			    paramz.addElement(chatThread);
			    paramz.addElement(minPost);
			    paramz.addElement(minPost);
			    paramz.addElement(true);

			    HashMap map = (HashMap) application.getSession().performSynchronousCall("get_thread", paramz);
			    
			    maxPost = (Integer) map.get("total_post_num");
			    minPost = maxPost - 20;
			    
			    paramz = new Vector();
			    paramz.addElement(chatThread);
			    paramz.addElement(minPost);
			    paramz.addElement(maxPost);
			    paramz.addElement(true);

			    result[0] = application.getSession().performSynchronousCall("get_thread", paramz);

			}
			catch(Exception e)
			{
				if(e.getMessage() != null) {
					Log.w(getString(R.string.app_name),e.getMessage());
				} else {
					Log.w(getString(R.string.app_name),"Chat connection error!");
				}
				return null;
			}

			return result;
			
		}
		
		@SuppressWarnings("rawtypes")
		protected void onPostExecute(final Object[] result) {
			
			if(result == null) {
				Toast toast = Toast.makeText(getActivity(), "Cannot connect to chat!", Toast.LENGTH_SHORT);
				toast.show();
				return;
			}
			
			String objectString = GsonHelper.customGson.toJson(result);
			
			SharedPreferences app_preferences = getActivity().getSharedPreferences("prefs", 0);
			String cachedForum = app_preferences.getString("social_list", "n/a");
			
			if(objectString.contentEquals(cachedForum)) {
				return;
			} else {
				SharedPreferences.Editor editor = app_preferences.edit();
	            editor.putString("social_list", objectString);
	            editor.commit();
			}
			
			if(getActivity() == null) {
				return;
			}
			
			ArrayList<Post> postList = new ArrayList<Post>();
			
			for(Object o: result) {
				
				if(o != null) {
		            HashMap map = (HashMap) o;
		            
		            if(map.containsKey("posts")) {
		            	Object[] topics = (Object[]) map.get("posts");
		            	for(Object t:topics) {
		            		
		            		HashMap topicMap = (HashMap) t;

		            		Date timestamp = (Date) topicMap.get("post_time");
		            		
		            		Post po = new Post();
		        			po.category_id = "108";
		        			po.subforum_id = "108";
		        			po.thread_id = "21";
		        			
		        			if(topicMap.containsKey("is_online")) {
		        				po.userOnline = (Boolean) topicMap.get("is_online");
		        			}
		        			
		        			po.post_author = new String((byte[]) topicMap.get("post_author_name"));
		        			po.post_author_id = (String) topicMap.get("post_author_id");
		        			po.post_body = new String((byte[]) topicMap.get("post_content"));
		        			po.post_avatar = (String) topicMap.get("icon_url");
		        			po.post_id = (String) topicMap.get("post_id");
		        			po.post_tagline = "tagline";
		        			po.post_timestamp = timestamp.toString();
		        			postList.add(0, po);

		            	}
		            }
				}
	        }

			int position = socialList.getFirstVisiblePosition();
			socialList.setOnItemClickListener(socailItemTapped);
			socialList.setItemsCanFocus(true);
			socialList.setAdapter(new PostAdapter(postList,getActivity(),application,-1));
			socialList.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
			socialList.setSelectionFromTop(position, 0);
		}
			
			
    }

	private AdapterView.OnItemClickListener socailItemTapped = new AdapterView.OnItemClickListener() {

		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
			
			Post sender = (Post) arg0.getItemAtPosition(arg2);
			
			if(profileSelected != null) {
				profileSelected.onProfileSelected(sender.post_author, sender.post_author_id);
			}

			
		}
		
	};
	
    @SuppressLint("NewApi")
	@SuppressWarnings("deprecation")
	private void storePostInClipboard() {
    	
    	//Copy text support for all Android versions
    	if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
    		ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
    		ClipData cd = ClipData.newHtmlText(selected_post.post_author + "'s Social Post", selected_post.post_body, selected_post.post_body);
    		clipboard.setPrimaryClip(cd);
    	} else {
    		android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
    		clipboard.setText(selected_post.post_body);
    	}

		Toast toast = Toast.makeText(getActivity(), "Text copied!", Toast.LENGTH_SHORT);
		toast.show();
    }
    
    /*
    @SuppressLint("NewApi")
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    	
    	if(getString(R.string.chat_forum).contentEquals("0")) {
    		inflater.inflate(R.menu.chat_menu, menu);
    		
    		if(ForegroundColorSetter.getForegroundDark(background)) {
    			MenuItem removeItem = menu.findItem(R.id.menu_chat_remove);
    			removeItem.setIcon(R.drawable.ic_action_remove_dark);
    		}
    	}

	    super.onCreateOptionsMenu(menu, inflater);

	}
    
    @Override
	public boolean onOptionsItemSelected (MenuItem item) {
		switch (item.getItemId()) {
        case R.id.menu_chat_remove:
        	removeChat();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
	}
    
    private void removeChat() {
    	application.getSession().getServer().chatForum = "0";
    	application.getSession().getServer().chatThread = "0";
    	application.getSession().getServer().chatName = "0";
    	application.getSession().updateServer();
        
        getActivity().finish();
    	getActivity().startActivity(getActivity().getIntent());
    }
    */
    
    public class SocialTimer extends CountDownTimer {
    	
    	public SocialTimer(long millisInFuture, long countDownInterval) {
			super(millisInFuture, countDownInterval);
		}

		@Override
		public void onTick(long millisUntilFinished) {
			//whatever
		}

		@Override
		public void onFinish() {
			load_statuses();
			socialTimer = new SocialTimer(socialRate,1000);
	        socialTimer.start();
		}
    }
    
    //Profile selected interface
  	public interface onProfileSelectedListener {
  		public abstract void onProfileSelected(String username,String userid);
  	}
  	
  	private onProfileSelectedListener profileSelected = null;
  	
  	public void setOnProfileSelectedListener(onProfileSelectedListener l) {
  		profileSelected = l;
  	}
    
}
