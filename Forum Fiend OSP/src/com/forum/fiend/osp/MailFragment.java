package com.forum.fiend.osp;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Vector;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.AsyncTask.Status;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;

@SuppressLint("NewApi")
public class MailFragment extends ListFragment {
	//private LinearLayout main_layout;

	private String rogueTitle;
	
	private String ourInboxId = "0";
	private String accent;
	private download_mail mailDownloader;
	private ForumFiendApp application;

	private InboxItem selected_item;
	
	
	
	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		
		application = (ForumFiendApp)getActivity().getApplication();

		setHasOptionsMenu(true);
	}
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {

        return super.onCreateView(inflater, container, savedInstanceState);
    }
	
	@Override
	public void onStart() {
		super.onStart(); 
		
		if(!(application.getSession().getServer().serverBackground.contentEquals(application.getSession().getServer().serverBoxColor) && application.getSession().getServer().serverBoxBorder.contentEquals("0"))) {
			getListView().setDivider(null);
		}

        accent = application.getSession().getServer().serverColor;


	}
	
	@Override
	public void onResume() {
		load_mail();
        super.onResume();   
        
    }
	
	private void load_mail() {
		mailDownloader = new download_mail();
		
		if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
			mailDownloader.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		} else {
			mailDownloader.execute();
		}
	}
	
	@Override
	public void onStop() {
		super.onStop();
		
		//Stop any running tasks
		if(mailDownloader != null) {
			if(mailDownloader.getStatus() == Status.RUNNING) {
				mailDownloader.cancel(true);
			}
		}
	}
	
	private class download_mail extends AsyncTask<String, Void, Object[]> {
		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		protected Object[] doInBackground(String... params) {


			Object[] result = new Object[50];

			try
			{

			    Vector paramz = new Vector();

			    HashMap map = (HashMap) application.getSession().performSynchronousCall("get_box_info", paramz);
			    
			    Object[] boxes = (Object[]) map.get("list");
			    
			    ourInboxId = "0";
			    
			    for(Object o:boxes) {
			    	HashMap boxMap = (HashMap) o;
			    	
			    	String boxType = (String) boxMap.get("box_type");
			    	
			    	Log.d("Forum Fiend","Found Mailbox: " + boxType);
			    	
			    	if(boxType.contentEquals("INBOX")) {
			    		ourInboxId = (String) boxMap.get("box_id");
			    	}
			    }

			    paramz = new Vector();
			    paramz.addElement(ourInboxId);
			    result[0] = application.getSession().performSynchronousCall("get_box", paramz);

			}
			catch(Exception e)
			{
				//null response
				return null;
			}
			return result;
		}
		
		@SuppressWarnings("rawtypes")
		protected void onPostExecute(final Object[] result) 
	    {
			if(result == null) {
				//Toast toast = Toast.makeText(getActivity(), "Server connection timeout :-(", Toast.LENGTH_SHORT);
				//toast.show();
				return;
			}

			try
			{

				try
				{
					ArrayList<InboxItem> inboxList = new ArrayList<InboxItem>();
					
					
					for(Object o: result) {
						
						if(o != null) {
				            HashMap map = (HashMap) o;
				            
				            if(map.containsKey("list")) {
				            	Object[] topics = (Object[]) map.get("list");
				            	for(Object t:topics) {
				            		
				            		HashMap topicMap = (HashMap) t;
				            		
				            		Date timestamp = (Date) topicMap.get("sent_date");
				            		
				            		InboxItem ii = new InboxItem();
				            		
				            		if(topicMap.containsKey("msg_state")) {
				            			int state = (Integer) topicMap.get("msg_state");
				            			
				            			if(state == 1){
				            				ii.isUnread = true;
				            			}
				            		}
				            		
					        		ii.inbox_unread = timestamp.toString();
					        		ii.inbox_sender = new String((byte[]) topicMap.get("msg_subject"));
					        		ii.sender_id = (String) topicMap.get("msg_id");
					        		ii.inbox_moderator = new String((byte[]) topicMap.get("msg_from"));
					        		ii.moderatorId = (String) topicMap.get("msg_from_id");
					        		ii.inboxId = ourInboxId;
					        		
					        		if(topicMap.containsKey("icon_url")) {
					        			ii.senderAvatar = (String) topicMap.get("icon_url");
					        		}
					        		
					        		ii.inbox_sender_color = accent;
					        		inboxList.add(ii);

				            	}
				            }
						}
			        }
					
					setListAdapter(new InboxAdapter(inboxList,getActivity(),application));
					registerForContextMenu(getListView());
					getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {

						public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,long arg3) {
							InboxItem sender = (InboxItem) arg0.getItemAtPosition(arg2);
							
							load_conversation(sender);
						}
					});
					
				}
				catch(Exception ex)
				{
					//error
				}
			}
			catch (Exception e)
			{
				//error
			}
	    }
    }
	
	private void load_conversation(InboxItem sender) {

		Intent myIntent = new Intent(getActivity(), Conversation.class);
		
		Bundle bundle = new Bundle();
		bundle.putString("id",(String) sender.sender_id);
		bundle.putString("boxid",(String) ourInboxId);
		bundle.putString("name",(String) sender.inbox_sender);
		bundle.putString("moderator",(String) sender.moderatorId);
		bundle.putString("background",(String) accent);
		myIntent.putExtras(bundle);
		
		MailFragment.this.startActivity(myIntent);
		
		
		
	};
	
	public void onCreateContextMenu(ContextMenu menu, View v,ContextMenuInfo menuInfo) {
		
		if(rogueTitle != null) {
			return;
		}
		
    	AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
    	selected_item = (InboxItem) getListView().getItemAtPosition(info.position);
    	
    	super.onCreateContextMenu(menu, v, menuInfo);
    	
    	MenuInflater inflater = getActivity().getMenuInflater();
	    inflater.inflate(R.menu.delete_mail, menu);
    }
    
    public boolean onContextItemSelected(MenuItem item) 
	{
		  switch (item.getItemId()) 
		  {
		  case R.id.mail_delete:
			  if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
				  new messageDeleter().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
			  } else {
				  new messageDeleter().execute();
			  }
		    return true;
		  default:
		    return super.onContextItemSelected(item);
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
			    paramz.addElement(selected_item.sender_id);
			    paramz.addElement(ourInboxId);

			    result[0] = application.getSession().performSynchronousCall("delete_message", paramz);

			}
			catch(Exception e)
			{
				Log.w("Forum Fiend",e.getMessage());
				return null;
			}
			return result;

		}
		
		protected void onPostExecute(final Object[] result) {
			
			load_mail();
		}
    }
    
    
}
