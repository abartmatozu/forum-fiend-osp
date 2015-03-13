package com.forum.fiend.osp;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Vector;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.ListView;

public class Conversation extends FragmentActivity {

	private String partner;
	private String partner_name;
	private ListView conversationList;

	private String boxId = "0";
	
	private String senderName = "";

	private String conversationModerator;
	
	private SQLiteDatabase notetasticDB;
	private String sql;
	
	private String accent = "";
	private ForumFiendApp application;

	private String externalServer = "0";
	
	private Session mailSession;
	
	private AnalyticsHelper ah;
	
	/** Called when the activity is first created. */
	

    @SuppressLint("NewApi")
	@Override
    public void onCreate(Bundle savedInstanceState) {
    	
    	application = (ForumFiendApp)getApplication();
    	
        Bundle bundle = getIntent().getExtras();
    	partner = bundle.getString("id");
    	partner_name = bundle.getString("name");
    	conversationModerator = bundle.getString("moderator");

        if(bundle.getString("background") != null) {
    		String bgc = bundle.getString("background");
    		
    		if(bgc.contains("#")) {
    			accent = bgc;
    			
    		} else {
    			accent = application.getSession().getServer().serverColor;
    		}
    	} else {
    		accent = application.getSession().getServer().serverColor;
    	}

        ThemeSetter.setTheme(this,accent);

        super.onCreate(savedInstanceState);
        
        ThemeSetter.setActionBar(this,accent);
        
        //Track app analytics
        ah = ((ForumFiendApp)getApplication()).getAnalyticsHelper();
        ah.trackScreen(getClass().getName(), false);
        
        setContentView(R.layout.conversation);
    	
    	if(bundle.getString("boxid") != null) {
    		boxId = bundle.getString("boxid");
    	}
    	
    	setTitle(partner_name);
    	

    	FrameLayout container = (FrameLayout) findViewById(R.id.conversation_list_container);
        
    	conversationList = new ListView(this);
    	conversationList.setDivider(null);
    	conversationList.setLayoutParams(new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        container.addView(conversationList);

        if(bundle.containsKey("server")) {
        	
        	externalServer = bundle.getString("server");

        	Log.i("Forum Fiend","Mail bundle contains server!");
        	
        	notetasticDB = this.openOrCreateDatabase("forumfiend", 0, null);
        	
        	String cleanServer = DatabaseUtils.sqlEscapeString(bundle.getString("server"));
        	
        	sql = "select * from accountlist where _id = " + cleanServer + ";";
              
          	Cursor c = notetasticDB.rawQuery(sql,null);
          	
          	
          	
          	if(c == null) {
          		notetasticDB.close();
          		return;
          	}
          	
          	Server server = null;

        	while(c.moveToNext()) {
        		server = IntroScreen.parseServerData(c);
        	}
        	
        	notetasticDB.close();

        	if(server == null) {
        		Log.i("Forum Fiend","Conversaion Server is null!");
        		return;
        	}
        	
        	mailSession = new Session(this,((ForumFiendApp)getApplication()));
        	
        	
        	mailSession.setSessionListener(new Session.SessionListener() {

    			@Override
    			public void onSessionConnected() {
    				new load_inbox().execute();
    			}

    			@Override
    			public void onSessionConnectionFailed(String reason) {
    				return;
    			}
    			
    		});
        	mailSession.setServer(server);
        	
        	
        } else {
        	mailSession = application.getSession();
        	
        	new load_inbox().execute();
        }
        
        if(getString(R.string.server_location).contentEquals("0")) {
	        if(ForegroundColorSetter.getForegroundDark(mailSession.getServer().serverColor)) {
	        	getActionBar().setIcon(R.drawable.ic_ab_main_black);
	        } else {
	        	getActionBar().setIcon(R.drawable.ic_ab_main_white);
	        }
        }
        
        
    }
    
    @Override
    public void onResume() {
    	//new fetchParticipants().execute();
    	
    	
    	super.onResume();
    }
    
    @Override
    public void onStart() {
      super.onStart();

    }

    @Override
    public void onStop() {
      super.onStop();

    }

    private class load_inbox extends AsyncTask<String, Void, Object[]> {
    
        // can use UI thread here
        protected void onPreExecute() {

        }

        // automatically done on worker thread (separate from UI thread)
        @SuppressWarnings({ "unchecked", "rawtypes" })
		protected Object[] doInBackground(final String... args) {

			Object[] result = new Object[50];

			try
			{


			    Vector paramz = new Vector();
			    paramz.addElement(partner);
			    paramz.addElement(boxId);
			    paramz.addElement(true);

			    result[0] = application.getSession().performSynchronousCall("get_message", paramz);

			}
			catch(Exception e)
			{
				Log.w("Discussions",e.getMessage());
				return null;
			}
			return result;
        }

        // can use UI thread here
        @SuppressWarnings("rawtypes")
		protected void onPostExecute(final Object[] result) {
        	if(result == null) {
				return;
			}
        	

			
			ArrayList<Post> postList = new ArrayList<Post>();

			try {
				
				
				for(Object o: result) {
					
					if(o != null) {
			            HashMap map = (HashMap) o;
			            
			            Date timestamp = (Date) map.get("sent_date");
	            		
	            		Post po = new Post();
	        			//po.category_id = category_id;
	        			//po.subforum_id = subforum_id;
	        			//po.thread_id = thread_id;
	        			//po.categoryModerator = moderator;
	        			
	            		senderName = new String((byte[]) map.get("msg_from"));
	        			po.post_author = new String((byte[]) map.get("msg_from"));
	        			po.post_author_id = conversationModerator;
	        			po.post_body = new String((byte[]) map.get("text_body"));
	        			po.post_avatar = (String) map.get("icon_url");
	        			
	        			po.post_id = partner;
	        			po.post_tagline = "tagline";
	        			po.post_timestamp = timestamp.toString();

	        			
	        			postList.add(po);
					}
		        }
				
				conversationList.setAdapter(new PostAdapter(postList,Conversation.this,application,-1));
				
			} catch (Exception e) {
				return;
			}
        }
     }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.conversation_menu, menu);
        
        if(ForegroundColorSetter.getForegroundDark(accent)) {
        	MenuItem itemReply = menu.findItem(R.id.convo_menu_reply);
        	MenuItem itemDelete = menu.findItem(R.id.convo_menu_delete);
        	
        	itemReply.setIcon(R.drawable.ic_action_reply_dark);
        	itemDelete.setIcon(R.drawable.ic_action_discard_dark);
        }
        
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.convo_menu_reply:
            	launchComposer();
                return true;
            case R.id.convo_menu_delete:
            	deleteMessage();
            	return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
    private void launchComposer() {
    	Intent myIntent = new Intent(Conversation.this, New_Post.class);
		
		Bundle bundle = new Bundle();
		bundle.putString("postid",(String) partner);
		bundle.putString("parent",(String) conversationModerator);
		bundle.putString("category",senderName);
		bundle.putString("subforum_id",(String) "0");
		bundle.putString("original_text",(String) "");
		bundle.putString("boxTitle",(String) partner_name);
		bundle.putString("picture",(String) "0");
		bundle.putString("color",(String) accent);
		bundle.putString("subject",(String) partner_name);
		bundle.putInt("post_type",(Integer) 4);
		
		if(!externalServer.contentEquals("0")) {
			bundle.putString("server",externalServer);
		}
		
		myIntent.putExtras(bundle);

		startActivity(myIntent);
    }
    
    @SuppressLint("NewApi")
	private void deleteMessage() {
    	if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
    		new messageDeleter().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    	} else {
    		new messageDeleter().execute();
    	}
    }
    

    private class messageDeleter extends AsyncTask<String, Void, Object[]> {
    	
		@SuppressWarnings({ "unchecked", "rawtypes" })
		@Override
		protected Object[] doInBackground(String... params) {

			Object[] result = new Object[50];

			try
			{

			    Vector paramz = new Vector();
			    paramz.addElement(partner);
			    paramz.addElement(boxId);

			    result[0] = application.getSession().performSynchronousCall("delete_message", paramz);
			}
			catch(Exception e)
			{
				Log.w("Discussions",e.getMessage());
				return null;
			}
			return result;

		}
		
		protected void onPostExecute(final Object[] result) {
			
			finish();
		}
    }
}
