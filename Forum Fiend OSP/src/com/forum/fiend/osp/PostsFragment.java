package com.forum.fiend.osp;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Vector;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("NewApi")
@SuppressWarnings("deprecation")
public class PostsFragment extends Fragment {
	
	private String server_address;
	private String subforum_id;
	private String category_id;
	private String thread_id;
	private String userid;
	private Integer page_number;
	private Integer total_pages;

	private String storagePrefix = "";
	
	private download_posts postsDownloader;

	private Post selected_post;
	private String lock;
	private String post_count;
	private String background;

	private String currentThreadSubject;

	private String shareURL = "0";
	
	private int scrollLocation = 0;


	private int curMinPost = 0;
	private int curMaxPost = 19;
	private int curTotalPosts = 0;
	
	private boolean forceBottomScroll = false;


	static final int POST_RESPONSE = 1;
	
	private boolean canPost = false;;


	private ForumFiendApp application;
	
	private ListView mainList;
	
	private LinearLayout posts_bottom_holder;
	private LinearLayout posts_input_area;
	private LinearLayout posts_pagination;
	
	private EditText posts_quick_reply;
	private Button posts_quick_reply_submit;
	
	private ImageView imFirst;
	private ImageView imPrevious;
	private ImageView imNext;
	private ImageView imLast;
	
	private TextView posts_page_number;
	
	private FragmentActivity activity;
	
	
	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		
		activity = (FragmentActivity)getActivity();
		
		application = (ForumFiendApp)activity.getApplication();

		setHasOptionsMenu(true);
	}
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
        View v = inflater.inflate(R.layout.posts, container, false);
        return v;
    }
	

	@Override
	public void onStart() {
		
		super.onStart();
		
		mainList = (ListView)activity.findViewById(R.id.posts_list_view);
		
		// Set up reply and pagination colors
		posts_bottom_holder = (LinearLayout)activity.findViewById(R.id.posts_bottom_holder);
		posts_input_area = (LinearLayout)activity.findViewById(R.id.posts_input_area);
		posts_pagination = (LinearLayout)activity.findViewById(R.id.posts_pagination);
		posts_quick_reply = (EditText)activity.findViewById(R.id.posts_quick_reply);
		posts_quick_reply_submit = (Button)activity.findViewById(R.id.posts_quick_reply_submit);
		imFirst = (ImageView)activity.findViewById(R.id.imFirst);
		imPrevious = (ImageView)activity.findViewById(R.id.imPrevious);
		imNext = (ImageView)activity.findViewById(R.id.imNext);
		imLast = (ImageView)activity.findViewById(R.id.imLast);
		posts_page_number = (TextView)activity.findViewById(R.id.posts_page_number);
		
		posts_quick_reply_submit.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				posts_quick_reply.setEnabled(false);
				posts_quick_reply_submit.setEnabled(false);
				new QuickReply().execute();
			}
		});
		
		imFirst.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				first_page();
			}
		});
		
		imPrevious.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				prev_page();
			}
		});
		
		imNext.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				next_page();
			}
		});
		
		imLast.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				last_page();
			}
		});
		
		if(application.getSession().getServer().serverColor.contains("#")) {
			posts_quick_reply_submit.setTextColor(Color.parseColor(application.getSession().getServer().serverColor));
			
			imFirst.setColorFilter(Color.parseColor(application.getSession().getServer().serverColor));
			imPrevious.setColorFilter(Color.parseColor(application.getSession().getServer().serverColor));
			imNext.setColorFilter(Color.parseColor(application.getSession().getServer().serverColor));
			imLast.setColorFilter(Color.parseColor(application.getSession().getServer().serverColor));
        }
        
        if(application.getSession().getServer().serverTextColor.contains("#")) {
        	posts_quick_reply.setTextColor(Color.parseColor(application.getSession().getServer().serverTextColor));
        	posts_page_number.setTextColor(Color.parseColor(application.getSession().getServer().serverTextColor));
        	
        	if(application.getSession().getServer().serverColor.contentEquals(application.getSession().getServer().serverBoxColor)) {
        		posts_quick_reply_submit.setTextColor(Color.parseColor(application.getSession().getServer().serverTextColor));
        		
        		imFirst.setColorFilter(Color.parseColor(application.getSession().getServer().serverTextColor));
    			imPrevious.setColorFilter(Color.parseColor(application.getSession().getServer().serverTextColor));
    			imNext.setColorFilter(Color.parseColor(application.getSession().getServer().serverTextColor));
    			imLast.setColorFilter(Color.parseColor(application.getSession().getServer().serverTextColor));
            }
        	
        }
        
        String boxColor = getString(R.string.default_element_background);
		
		if(application.getSession().getServer().serverBoxColor != null) {
			boxColor = application.getSession().getServer().serverBoxColor;
		}
        
		if(boxColor.contains("#")) {
			posts_bottom_holder.setBackgroundColor(Color.parseColor(boxColor));
		}
		
		if(!(application.getSession().getServer().serverBackground.contentEquals(application.getSession().getServer().serverBoxColor) && application.getSession().getServer().serverBoxBorder.contentEquals("0"))) {
			mainList.setDivider(null);
		}
		
		Bundle bundle = getArguments();
        subforum_id = bundle.getString("subforum_id");
        category_id = bundle.getString("category_id");
        thread_id = bundle.getString("thread_id");
        lock = bundle.getString("lock");
        post_count = bundle.getString("posts");
        currentThreadSubject = bundle.getString("subject");
        
        if(application.getSession().forumSystem == 1) {
    		shareURL = application.getSession().getServer().serverAddress + "/viewtopic.php?f=" + subforum_id + "&t=" + thread_id;
    	}
        
        background = application.getSession().getServer().serverColor;

        SharedPreferences app_preferences = activity.getSharedPreferences("prefs", 0);
        server_address = application.getSession().getServer().serverAddress;


        if(getString(R.string.server_location).contentEquals("0")) {
        	storagePrefix = server_address + "_";
        }
        
        userid = application.getSession().getServer().serverUserId;

        
        
        if(lock.contentEquals("1"))
		{
			Toast toast = Toast.makeText(activity, "Thread is locked!", Toast.LENGTH_LONG);
			toast.show();
		}

        page_number = 1;
        page_number = app_preferences.getInt(storagePrefix + "thread_" + thread_id + "_retained_page", -1);
        total_pages = (((Integer.parseInt(post_count) - 1) / 20) + 1);
        
        if(page_number == -1) {
        	page_number = total_pages;
        	
        	if(page_number > 1) {
        		forceBottomScroll = true;
        	}
        }
        
        curMinPost = (page_number - 1) * 20;
		curMaxPost = curMinPost + 19;

		posts_input_area.setVisibility(View.GONE);
	}


    @Override
    public void onStop() {
    	
    	endCurrentlyRunning();
    	
    	super.onStop();

    }
    
    @Override
    public void onPause() {
    	
    	final SharedPreferences preferences = activity.getSharedPreferences("prefs", 0);
		SharedPreferences.Editor editor = preferences.edit();
    	editor.putInt(storagePrefix + "t_" + thread_id + "_p_" + page_number + "_position", mainList.getFirstVisiblePosition());
    	editor.commit();
    	
    	endCurrentlyRunning();
    	
    	super.onPause();
    }
	
	@Override
	public void onResume() {
		
		activity.getActionBar().setTitle(currentThreadSubject);

		final SharedPreferences preferences = activity.getSharedPreferences("prefs", 0);
		scrollLocation = preferences.getInt(storagePrefix + "t_" + thread_id + "_p_" + page_number + "_position", 0);
		
		load_posts();
		
		super.onResume();
	}
	
	public void onDestroy() {

    	super.onDestroy();

    }
	

	private void setupPagination() {
		
		total_pages = (((curTotalPosts - 1) / 20) + 1);
		
		posts_page_number.setText(page_number + " of " + total_pages);

		
		if(total_pages > 1) {
			posts_pagination.setVisibility(View.VISIBLE);
		} else {
			posts_pagination.setVisibility(View.GONE);
		}
		
	}

	
	private void load_posts() {
		//getActivity().getActionBar().setSubtitle("Page " + page_number + " of " + total_pages);
		
		SharedPreferences app_preferences = activity.getSharedPreferences("prefs", 0);
		
		//Save what page we are on
		SharedPreferences.Editor editor = app_preferences.edit();
        editor.putInt(storagePrefix + "thread_" + thread_id + "_retained_page", page_number);
        editor.commit();
        
        endCurrentlyRunning();
        
        postsDownloader = new download_posts();
        
        if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
        	postsDownloader.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
        	postsDownloader.execute();
        }
	}
	
	private void endCurrentlyRunning() {
		//Stop any running tasks
		if(postsDownloader != null) {
			if(postsDownloader.getStatus() == Status.RUNNING) {
				postsDownloader.cancel(true);
			}
		}
	}
	
	private class download_posts extends AsyncTask<String, Void, Object[]> {
		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		protected Object[] doInBackground(String... params) {
			
			Log.d("Forum Fiend","Posts - download_posts");

			Object[] result = new Object[50];

			try {
			    Vector paramz = new Vector();
			    paramz.addElement(thread_id);
			    paramz.addElement(curMinPost);
			    paramz.addElement(curMaxPost);
			    paramz.addElement(true);

			    result[0] = application.getSession().performSynchronousCall("get_thread", paramz);

			}
			catch(Exception e)
			{
				Log.w("Forum Fiend",e.getMessage());
				return null;
			}
			return result;
		}
		
		@SuppressWarnings("rawtypes")
		protected void onPostExecute(final Object[] result) {
			if(activity == null) {
				return;
			}
			
			if(result == null) {
				Toast toast = Toast.makeText(activity, "No response from the server!", Toast.LENGTH_LONG);
				toast.show();
				return;
			}
			
			ArrayList<Post> postList = new ArrayList<Post>();
			
			for(Object o: result) {
				
				if(o != null) {
		            HashMap map = (HashMap) o;

		            if( map.get("total_post_num") != null) {
		            	curTotalPosts = (Integer) map.get("total_post_num");
		            }
		            
		            if( map.get("can_reply") != null) {
		            	canPost = (Boolean)map.get("can_reply");
		            	
		            	if(canPost) {
		            		
		            		SharedPreferences app_preferences = getActivity().getSharedPreferences("prefs", 0);
		    				boolean quickReplySetting = app_preferences.getBoolean("show_quick_reply",true);
		            		
		    				if(quickReplySetting) {
		    					posts_input_area.setVisibility(View.VISIBLE);
		    				}
		            	}
		            }
		            
		            
		            if(map.containsKey("posts")) {
		            	Object[] topics = (Object[]) map.get("posts");
		            	for(Object t:topics) {
		            		
		            		HashMap topicMap = (HashMap) t;

		            		Date timestamp = (Date) topicMap.get("post_time");
		            		
		            		Post po = new Post();
		        			po.category_id = category_id;
		        			po.subforum_id = subforum_id;
		        			po.thread_id = thread_id;
		        			//po.categoryModerator = moderator;
		        			
		        			if(!topicMap.containsKey("post_author_id")) {
		        				Log.w("Forum Fiend","There is no author id with this post!");
		        			}
		        			
		        			po.post_author = new String((byte[]) topicMap.get("post_author_name"));
		        			po.post_author_id = (String) topicMap.get("post_author_id");
		        			po.post_body = new String((byte[]) topicMap.get("post_content"));
		        			po.post_avatar = (String) topicMap.get("icon_url");

		        			po.post_id = (String) topicMap.get("post_id");
		        			po.post_tagline = "tagline";
		        			
		        			if(timestamp != null) {
		        				po.post_timestamp = timestamp.toString();
		        			}
		        			
		        			if(topicMap.containsKey("attachments")) {
				            	Object[] attachments = (Object[]) topicMap.get("attachments");
				            	
				            	for(Object a:attachments) {
				            		
				            		HashMap attachmentMap = (HashMap) a;
				            		
				            		String attachmentType = (String) attachmentMap.get("content_type");
				            		String attachmentUrl = (String) attachmentMap.get("url");
				            		
				            		String attachmentName = null;
				            		
				            		if(attachmentMap.containsKey("filename")) {
				            			attachmentName = new String((byte[]) attachmentMap.get("filename"));
				            		}
				            		
				            		if(attachmentType != null) {
				            			Log.i("Forum Fiend","Post has attachment of type: " + attachmentType);
				            		}
				            		
				            		if(attachmentUrl != null) {
				            			Log.i("Forum Fiend","Post has attachment of url: " + attachmentUrl);
				            		}
				            		
				            		if(attachmentName != null) {
				            			Log.i("Forum Fiend","Post has attachment of type: " + attachmentName);
				            		}
				            		
				            		if(attachmentType != null && attachmentUrl != null && attachmentName != null) {
				            			PostAttachment pa = new PostAttachment();
				            			pa.content_type = attachmentType;
				            			pa.url = attachmentUrl;
				            			pa.filename = attachmentName;
				            			po.attachmentList.add(pa);
				            		}
				            	}
				            	
				            	
		        			}
		        			
		        			if(topicMap.containsKey("is_online")) {
		        				po.userOnline = (Boolean) topicMap.get("is_online");
		        			}
		        			
		        			if(topicMap.containsKey("is_ban")) {
		        				po.userBanned = (Boolean) topicMap.get("is_ban");
		        			}
		        			
		        			if(topicMap.containsKey("can_delete")) {
		        				po.canDelete = (Boolean) topicMap.get("can_delete");
		        			}
		        			
		        			if(topicMap.containsKey("can_ban")) {
		        				po.canBan = (Boolean) topicMap.get("can_ban");
		        			}
		        			
		        			if(topicMap.containsKey("can_edit")) {
		        				po.canEdit = (Boolean) topicMap.get("can_edit");
		        			}
		        			
		        			if(topicMap.containsKey("can_thank")) {
		        				po.canThank = (Boolean) topicMap.get("can_thank");
		        			}
		        			
		        			if(topicMap.containsKey("can_like")) {
		        				po.canLike = (Boolean) topicMap.get("can_like");
		        			}
		        			
		        			if(topicMap.containsKey("thanks_info")) {
				            	Object[] thankses = (Object[]) topicMap.get("thanks_info");
				            	
				            	int thanksCount = thankses.length;
				            	
				            	po.thanksCount = thanksCount;
		        			}
		        			
		        			if(topicMap.containsKey("likes_info")) {
				            	Object[] likes = (Object[]) topicMap.get("likes_info");
				            	
				            	int likesCount = likes.length;
				            	
				            	po.likeCount = likesCount;
		        			}
		        			
		        			postList.add(po);

		            	}
		            }
				}
	        }
			
			setupPagination();
			
			ListView lvPosts;
			
			try {
				lvPosts = mainList;
			} catch (Exception ex) {
				return;
			}
			
			if(lvPosts == null) {
				return;
			}
			
			registerForContextMenu(lvPosts);
			
			
			lvPosts.setOnItemClickListener(new AdapterView.OnItemClickListener() {

				public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,long arg3) {

			        if(userid.contentEquals("0")) {
			        	return;
			        }

					if(profileSelected != null) {
						Post sender = (Post) arg0.getItemAtPosition(arg2);
						profileSelected.onProfileSelected(sender.post_author, sender.post_author_id);
					}

				}
			});

			mainList.setAdapter(new PostAdapter(postList,activity,application,page_number));

			lvPosts.setItemsCanFocus(true);
			activity.setProgressBarIndeterminateVisibility(false); 
			
			if(forceBottomScroll) {
				Log.d("Forum Fiend","Force Bottom Scroll: " + (postList.size() - 1));
				forceBottomScroll = false;
				lvPosts.setSelection(postList.size() - 1);
			} else {
				lvPosts.setSelection(scrollLocation);
				Log.d("Forum Fiend","Retained Scroll: " + scrollLocation);
			}

	    }
    }
	
	
	
	private void refresh() {

		load_posts();
	}
	
	private void next_page()
	{
		if(page_number < total_pages)
		{
			curMinPost = curMinPost + 20;
			curMaxPost = curMaxPost + 20;
			page_number ++;
			load_posts();

		}
	}
	
	private void prev_page()
	{
		if(page_number > 1) {
			curMinPost = curMinPost - 20;
			curMaxPost = curMaxPost - 20;
			page_number --;
			load_posts();

		}
	}
	
	private void first_page() {
		curMinPost = 0;
		curMaxPost = 19;
		page_number = 1;
		load_posts();

	}
	
	private void last_page() {
		curMinPost = (total_pages - 1) * 20;
		curMaxPost = curMinPost + 19;
		page_number = total_pages;
		load_posts();

	}
	
	private void start_post()
    {
		if(lock.contentEquals("1")) {
			Toast toast = Toast.makeText(activity, "Thread is locked!", Toast.LENGTH_LONG);
			toast.show();
			return;
		}

		if(application.getSession().getServer().serverUserId.contentEquals("0")) {
			Toast toast = Toast.makeText(activity, "You must be logged in to post!", Toast.LENGTH_LONG);
			toast.show();
			return;
		}
		
		Intent myIntent = new Intent(activity, New_Post.class);
		
		Bundle bundle = new Bundle();
		bundle.putString("postid",(String) "0");
		bundle.putString("parent",(String) thread_id);
		bundle.putString("category",(String) category_id);
		bundle.putString("subforum_id",(String) subforum_id);
		bundle.putString("original_text",(String) "");
		bundle.putString("boxTitle",(String) "RE: " + currentThreadSubject);
		bundle.putString("picture",(String) "0");
		bundle.putString("color",(String) background);
		bundle.putString("subject",(String) currentThreadSubject);
		bundle.putInt("post_type",(Integer) 2);
		myIntent.putExtras(bundle);
		
		PostsFragment.this.startActivityForResult(myIntent,POST_RESPONSE);
    }
	
	private void edit_post()
    {
		if(lock.contentEquals("1"))
		{
			Toast toast = Toast.makeText(activity, "Thread is locked!", Toast.LENGTH_LONG);
			toast.show();
			return;
		}

		if(application.getSession().getServer().serverUserId.contentEquals("0"))
		{
			Toast toast = Toast.makeText(activity, "You must be logged in to post!", Toast.LENGTH_LONG);
			toast.show();
			return;
		}
		
		
		Intent myIntent = new Intent(activity, New_Post.class);
		
		Bundle bundle = new Bundle();
		bundle.putString("postid",(String) selected_post.post_id);
		bundle.putString("parent",(String) thread_id);
		bundle.putString("category",(String) category_id);
		bundle.putString("subforum_id",(String) subforum_id);
		bundle.putString("original_text",(String) selected_post.post_body);
		bundle.putString("boxTitle",(String) "RE: " + currentThreadSubject);
		bundle.putString("picture",(String) selected_post.post_picture);
		bundle.putString("color",(String) background);
		bundle.putString("subject",(String) currentThreadSubject);
		bundle.putInt("post_type",(Integer) 3);
		myIntent.putExtras(bundle);
		
		PostsFragment.this.startActivityForResult(myIntent,POST_RESPONSE);

    }
	
	private void quote_post()
    {
		if(lock.contentEquals("1"))
		{
			Toast toast = Toast.makeText(activity, "Thread is locked!", Toast.LENGTH_LONG);
			toast.show();
			return;
		}
		
		if(application.getSession().getServer().serverUserId.contentEquals("0"))
		{
			Toast toast = Toast.makeText(activity, "You must be logged in to post!", Toast.LENGTH_LONG);
			toast.show();
			return;
		}
		
		Intent myIntent = new Intent(activity, New_Post.class);
		
		Bundle bundle = new Bundle();
		bundle.putString("postid",(String) "0");
		bundle.putString("parent",(String) thread_id);
		bundle.putString("category",(String) category_id);
		bundle.putString("subforum_id",(String) subforum_id);
		bundle.putString("boxTitle",(String) "RE: " + currentThreadSubject);
		bundle.putString("original_text",(String) "[quote=\"" + selected_post.post_author + "\"]" +  selected_post.post_body.replace("[img]http://forumfiend.net/dice.png[/img]", "") + "[/quote]<br /><br />");
		bundle.putString("picture",(String) "0");
		bundle.putString("color",(String) background);
		bundle.putInt("post_type",(Integer) 2);
		bundle.putString("subject",(String) currentThreadSubject);
		myIntent.putExtras(bundle);
		
		PostsFragment.this.startActivityForResult(myIntent,POST_RESPONSE);
    }
	
	public void onActivityResult(int requestCode, int resultCode, Intent data) 
	{
		if (requestCode == POST_RESPONSE)
		{
			page_number = total_pages;
			load_posts();
		}
		else
		{
			//do nothing at this time
		}
	}
    
    public void onCreateContextMenu(ContextMenu menu, View v,ContextMenuInfo menuInfo) 
    {
    	//Block profile viewing on restricted accounts
		SharedPreferences app_preferences = activity.getSharedPreferences("prefs", 0);
        boolean accountRestricted = app_preferences.getBoolean(storagePrefix + "logged_banned", false);
        if(accountRestricted) {
        	return;
        }
        
    	AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
    	selected_post = (Post) mainList.getItemAtPosition(info.position);
    	
    	super.onCreateContextMenu(menu, v, menuInfo);
    	MenuInflater inflater = activity.getMenuInflater();
	    inflater.inflate(R.menu.posts_context, menu);

    	
    	if(!userid.contentEquals("0"))
		{
    		MenuItem item3 = menu.findItem(R.id.posts_quote);
            item3.setVisible(true);
            
            MenuItem item5 = menu.findItem(R.id.posts_message);
            item5.setVisible(true);
		}
    	
    	
    	if(selected_post.post_author_id != null) {
	    	if(userid.contentEquals(selected_post.post_author_id)) {
	            MenuItem item5 = menu.findItem(R.id.posts_message);
	            item5.setVisible(false);
	    	}
    	}

    	MenuItem itemBan = menu.findItem(R.id.posts_context_ban);
    	MenuItem itemDelete = menu.findItem(R.id.posts_context_delete);
    	MenuItem itemEdit = menu.findItem(R.id.posts_edit);
    	MenuItem itemThank = menu.findItem(R.id.posts_thank);
    	MenuItem itemLike = menu.findItem(R.id.posts_like);
    	
    	if(selected_post.canThank) {
    		itemThank.setVisible(true);
    	} else {
    		itemThank.setVisible(false);
    	}
    	
    	if(selected_post.canLike) {
    		itemLike.setVisible(true);
    	} else {
    		itemLike.setVisible(false);
    	}
    	
    	if(selected_post.canBan && !selected_post.userBanned) {
    		itemBan.setVisible(true);
    	} else {
    		itemBan.setVisible(false);
    	}

    	if(selected_post.canDelete) {
    		itemDelete.setVisible(true);
    	} else {
    		itemDelete.setVisible(false);
    	}
    	
    	if(selected_post.canEdit) {
    		itemEdit.setVisible(true);
    		
    		if(selected_post.post_body.contains("http://forumfiend.net/dice.png")) {
    			itemEdit.setVisible(false);
    		}
    	} else {
    		itemEdit.setVisible(false);
    	}
    }
    
    public boolean onContextItemSelected(MenuItem item) {
    	
		  switch (item.getItemId()) {
		  
		  case R.id.posts_edit:
			  edit_post();
		    return true;
		  case R.id.posts_quote:
			  quote_post();
		    return true;
		  case R.id.posts_message:
			  startConvo();
		    return true;
		  case R.id.posts_copy:
			  storePostInClipboard();
			  return true;
		  case R.id.posts_context_delete_yes:
			  new deletePost().execute(selected_post.post_id);
			  return true;
		  case R.id.posts_context_ban:
			  dropTheHammer();
			  return true;
		  case R.id.posts_thank:
			  new thankPost().execute(selected_post.post_id);
			  return true;
		  case R.id.posts_like:
			  new likePost().execute(selected_post.post_id);
			  return true;
		  default:
		    return super.onContextItemSelected(item);
		  }
	}
    
    @SuppressLint("NewApi")
	private void storePostInClipboard() {
    	
    	//Copy text support for all Android versions
    	if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
    		ClipboardManager clipboard = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
    		ClipData cd = ClipData.newHtmlText(currentThreadSubject, selected_post.post_body, selected_post.post_body);
    		clipboard.setPrimaryClip(cd);
    	} else {
    		android.text.ClipboardManager clipboard = (android.text.ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
    		clipboard.setText(selected_post.post_body);
    	}

		Toast toast = Toast.makeText(activity, "Text copied!", Toast.LENGTH_SHORT);
		toast.show();
    }
    
    @SuppressLint("NewApi")
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		

        inflater.inflate(R.menu.post_n_page, menu);

        MenuItem itemRefresh = menu.findItem(R.id.menu_refresh);
        MenuItem itemNew = menu.findItem(R.id.menu_newpost);
        
        if(ForegroundColorSetter.getForegroundDark(background)) {
        	itemRefresh.setIcon(R.drawable.ic_action_refresh_dark);
        	itemNew.setIcon(R.drawable.ic_action_new_dark);
        }
		
		if(total_pages == null) {
			total_pages = 0;
		}

		
		MenuItem browserItem = menu.findItem(R.id.posts_menu_browser);
		MenuItem shareItem = menu.findItem(R.id.posts_menu_share);
        
        if(shareURL.contentEquals("0")) {
        	browserItem.setVisible(false);
        	shareItem.setVisible(false);
        } else {
        	browserItem.setVisible(true);
        	shareItem.setVisible(true);
        }

	    super.onCreateOptionsMenu(menu, inflater);

	}
    

    
    public boolean onOptionsItemSelected(MenuItem item) 
    {
        switch (item.getItemId()) 
        {
        case R.id.posts_menu_browser:
        	Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(shareURL));
        	startActivity(browserIntent);
            return true;
        case R.id.posts_menu_share:
        	Intent sendIntent = new Intent(android.content.Intent.ACTION_SEND);
        	sendIntent.putExtra(Intent.EXTRA_SUBJECT, currentThreadSubject);
        	sendIntent.putExtra(Intent.EXTRA_TEXT, shareURL);
        	sendIntent.setType("text/plain");
        	startActivity(sendIntent);
        	return true;
        case R.id.menu_newpost:
        	start_post();
            return true;
        case R.id.menu_refresh:
        	refresh();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
    
    private void startConvo() {

    	Intent myIntent = new Intent(activity, New_Post.class);
		
		Bundle bundle = new Bundle();
		bundle.putString("postid",(String) "0");
		bundle.putString("parent",(String) "0");
		bundle.putString("category",selected_post.post_author);
		bundle.putString("subforum_id",(String) "0");
		bundle.putString("original_text",(String) "");
		bundle.putString("boxTitle",(String) "New Message");
		bundle.putString("picture",(String) "0");
		bundle.putString("color",(String) getString(R.string.default_color));
		bundle.putString("subject",(String) "");
		bundle.putInt("post_type",(Integer) 4);
		myIntent.putExtras(bundle);

		startActivity(myIntent);
    	
    	
    }

    private class deletePost extends AsyncTask<String, Void, String> {
		
		// parm[0] - (string)topic_id

		@SuppressLint("UseValueOf")
		@SuppressWarnings({ "unchecked", "rawtypes" })
		@Override
		protected String doInBackground(String... params) {
			
			if(activity == null) {
				return null;
			}

			String result = "";

			
			try {

			    Vector paramz;
			    
			    paramz = new Vector();
			    paramz.addElement(params[0]);
			    paramz.addElement(2);

			    application.getSession().performSynchronousCall("m_delete_post", paramz);
			    

			} catch (Exception ex) {
				Log.w("Forum Fiend", ex.getMessage());
			}

			return result;
		}
		
		protected void onPostExecute(final String result) {
			
			if(activity == null) {
				return;
			}
			
			load_posts();
		}
	}
    
    private void dropTheHammer() {
    	
    	Bundle bundle = new Bundle();
    	bundle.putString("username", selected_post.post_author);
    	
    	BanHammerDialogFragment newFragment = BanHammerDialogFragment.newInstance();
    	newFragment.setArguments(bundle);
	    newFragment.show(activity.getSupportFragmentManager(), "dialog");
    }
    
    //Profile selected interface
  	public interface onProfileSelectedListener {
  		public abstract void onProfileSelected(String username,String userid);
  	}
  	
  	private onProfileSelectedListener profileSelected = null;
  	
  	public void setOnProfileSelectedListener(onProfileSelectedListener l) {
  		profileSelected = l;
  	}
  	
  	private class thankPost extends AsyncTask<String, Void, String> {

		@SuppressLint("UseValueOf")
		@SuppressWarnings({ "unchecked", "rawtypes" })
		@Override
		protected String doInBackground(String... params) {
			
			if(activity == null) {
				return null;
			}

			String result = "";

			
			try {
			    Vector paramz;
			    
			    paramz = new Vector();
			    paramz.addElement(params[0]);

			    application.getSession().performSynchronousCall("thank_post", paramz);
			    

			} catch (Exception ex) {
				Log.w("Forum Fiend", ex.getMessage());
			}

			return result;
		}
		
		protected void onPostExecute(final String result) {
			
			if(activity == null) {
				return;
			}
			
			load_posts();
		}
	}
  	
  	private class likePost extends AsyncTask<String, Void, String> {

		@SuppressLint("UseValueOf")
		@SuppressWarnings({ "unchecked", "rawtypes" })
		@Override
		protected String doInBackground(String... params) {
			
			if(activity == null) {
				return null;
			}

			String result = "";

			
			try {

			    Vector paramz;
			    
			    paramz = new Vector();
			    paramz.addElement(params[0]);

			    application.getSession().performSynchronousCall("like_post", paramz);
			    

			} catch (Exception ex) {
				Log.w("Forum Fiend", ex.getMessage());
			}

			return result;
		}
		
		protected void onPostExecute(final String result) {
			
			if(activity == null) {
				return;
			}
			
			load_posts();
		}
	}
  	
  	private class QuickReply extends AsyncTask<String, Void, Object[]> {
  		
		@SuppressWarnings({ "unchecked", "rawtypes" })
		protected Object[] doInBackground(final String... args) {
			String comment;
        	String subject = currentThreadSubject;
        	
        	comment = posts_quick_reply.getText().toString();

			Object[] result = new Object[50];

        	comment = comment.trim();
        	subject = subject.trim();
        	
        	if (subject.length() > 45)
        		subject = subject.substring(0,44);
        	
        	if (subject.length() < 1)
        		subject = "no subject";
        	
        	if (comment.length() < 1) {
    			return null;
        	}
        	
        	String tagline = application.getSession().getServer().serverTagline;
        	
        	if(tagline.length() > 0) {
        		comment = comment + "\n\n" + tagline;
        	}
        	
        	try {
        		Vector paramz = new Vector();
			    paramz.addElement(category_id);
			    paramz.addElement(thread_id);
			    paramz.addElement(subject.getBytes());
			    paramz.addElement(comment.getBytes());
			    result[0] = application.getSession().performSynchronousCall("reply_post", paramz);
			} catch(Exception e) {
				Log.w("Forum Fiend",e.getMessage());
				return null;
			}
        	

        	return result;
        }
		
		//This method is executed after the thread has completed.
        protected void onPostExecute(final Object[] result)  {
        	
        	posts_quick_reply_submit.setEnabled(true);
        	posts_quick_reply.setEnabled(true);
        	
        	if(result == null) {
    			Toast toast = Toast.makeText(activity, "Submission error, please retry :-(", Toast.LENGTH_LONG);
    			toast.show();
    			return;
        	}
        	
        	forceBottomScroll = true;
        	posts_quick_reply.setText("");
        	load_posts();
        }

     }
}
