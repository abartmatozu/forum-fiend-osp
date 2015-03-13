package com.forum.fiend.osp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;
import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

@SuppressLint("NewApi")
public class ActiveList extends ListFragment {

	private ForumFiendApp application;
	
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
		
		getListView().setDivider(null);

        load_mail();
	}
	
	private void load_mail() {
		if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
			new download_mail().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		} else {
			new download_mail().execute();
		}
	}
	
	private class download_mail extends AsyncTask<String, Void, Object[]> {
		@SuppressWarnings("rawtypes")
		@Override
		protected Object[] doInBackground(String... params) {

			Object[] result = new Object[50];

			try
			{

			    Vector paramz = new Vector();
			    
			    result[0] = (HashMap)application.getSession().performSynchronousCall("get_online_users", paramz);
			}
			catch(Exception e)
			{
				Log.w("Discussions",e.getMessage());
				return null;
			}
			return result;
		}
		
		@SuppressWarnings("rawtypes")
		protected void onPostExecute(final Object[] result) 
	    {
			if(result == null) {
				Log.d("Discussions","Null active list");
				return;
			}


			try
			{
				try
				{
					ArrayList<IgnoreItem> inboxList = new ArrayList<IgnoreItem>();
					
					
					
					
					for(Object o: result) {
						
						if(o != null) {
				            HashMap map = (HashMap) o;
				            
				            if(map.containsKey("list")) {
				            	Object[] topics = (Object[]) map.get("list");
				            	for(Object t:topics) {
				            		
				            		HashMap topicMap = (HashMap) t;

				            		IgnoreItem ii = new IgnoreItem();
				            		
				            		if(topicMap.containsKey("username")) {
				            			ii.ignoreItemUsername = new String((byte[]) topicMap.get("username"));
				            		} else {
				            			if(topicMap.containsKey("user_name")) {
					            			ii.ignoreItemUsername = new String((byte[]) topicMap.get("user_name"));
					            		}
				            		}
				            		
				            		if(topicMap.containsKey("icon_url")) {
				            			ii.ignoreItemAvatar = (String) topicMap.get("icon_url");
				            		}
				            		
				            		if(topicMap.containsKey("display_text")) {
				            			ii.ignoreItemDate = new String((byte[]) topicMap.get("display_text"));
				            		}
					        		
				            		if(topicMap.containsKey("user_id")) {
				            			ii.ignoreUserId = (String) topicMap.get("user_id");
				            		}
					        		
					        		
					        		ii.ignoreProfileColor = "#000000";
				            		
					        		inboxList.add(ii);

				            	}
				            }
						}
			        }
					
					

					
					setListAdapter(new UserCardAdapter(inboxList,getActivity()));
					//registerForContextMenu(getListView());
					
					getListView().setOnItemClickListener(new OnItemClickListener() {

						public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
							IgnoreItem sender = (IgnoreItem)arg0.getItemAtPosition(arg2);

							if(profileSelected != null) {
								profileSelected.onProfileSelected(sender.ignoreItemUsername, sender.ignoreUserId);
							}
						}
					});
					
				}
				catch(Exception ex)
				{
					Log.d("Discussions","ex1 - " + ex.getMessage());
				}
			}
			catch (Exception e)
			{
				Log.d("Discussions","ex2 - " + e.getMessage());
			}
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
