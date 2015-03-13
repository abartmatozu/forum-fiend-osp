package com.forum.fiend.osp;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Vector;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;

import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;


public class MailService extends Service {
	
	private SQLiteDatabase notetasticDB;
	private String sql;

	private Session mailSession;
	
	private int serviceTimer = 20000000;
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		initDatabase();
		startservice();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		stopservice();
		
	}
	
	private void startservice() {
		MyCount counter = new MyCount(serviceTimer,1000);
		counter.start();
		Log.d("Forum Fiend","Starting MailService");
	}
	
	public class MyCount extends CountDownTimer{

		public MyCount(long millisInFuture, long countDownInterval) {
			super(millisInFuture, countDownInterval);
		}

		@Override
		public void onFinish() {
			
			Log.d("Forum Fiend","MailService Tick - Checking Mail");
			routineMailCheck();

			MyCount counter = new MyCount(serviceTimer,1000);
			counter.start();
		}

		@Override
		public void onTick(long millisUntilFinished) {
			//whatever
		}

	}

	private void stopservice() {

	}
	
	private ArrayList<Server> serverList;
	int currentServer = 0;
	
	private void routineMailCheck() {
		
		mailSession = new Session(this,((ForumFiendApp)getApplication()));
		
		serverList = new ArrayList<Server>();
    	
    	notetasticDB = this.openOrCreateDatabase("forumfiend", 0, null);
    	
    	sql = "select * from accountlist;";
          
      	Cursor c = notetasticDB.rawQuery(sql,null);
      	
      	if(c == null) {
      		notetasticDB.close();
      		return;
      	}

    	while(c.moveToNext()) {
    		Server server = IntroScreen.parseServerData(c);
    		
    		Log.i("Forum Fiend","Checking login data for server " + server.serverAddress);
    		
    		if(!server.serverUserId.contentEquals("0")) {
    			serverList.add(server);
    		}
    		
    		
    	}
    	
    	notetasticDB.close();
    	
    	if(serverList.size() == 0) {
    		Log.d("Forum Fiend","No servers found, ending check.");
    	}
		
		currentServer = 0;
		nextServer();
	}
	
	private void nextServer() {
		if((currentServer + 1) > serverList.size()) {
        	return;
        }
        
		Log.d("Forum Fiend","MailService Tick - Checking " + serverList.get(currentServer).serverAddress);
		
		mailSession.setSessionListener(new Session.SessionListener() {

			@Override
			public void onSessionConnected() {
				new mailChecker().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,mailSession.getServer());
			}

			@Override
			public void onSessionConnectionFailed(String reason) {
				nextServer();
			}
			
		});
		mailSession.setServer(serverList.get(currentServer));
        currentServer++;
	}

	private class mailChecker extends AsyncTask<Server, Void, Object[]> {
		
		private String currentServerAddress;

		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		protected Object[] doInBackground(Server... params) {
			
			currentServerAddress = params[0].serverAddress;

			Object[] result = new Object[50];

			try {

			    Vector paramz = new Vector();

			    HashMap map = (HashMap) mailSession.performSynchronousCall("get_box_info", paramz);
			    
			    Object[] boxes = (Object[]) map.get("list");
			    
			    String ourInboxId = "0";
			    
			    for(Object o:boxes) {
			    	HashMap boxMap = (HashMap) o;
			    	
			    	String boxType = (String) boxMap.get("box_type");
			    	
			    	if(boxType.contentEquals("INBOX")) {
			    		ourInboxId = (String) boxMap.get("box_id");
			    	}
			    }

			    paramz = new Vector();
			    paramz.addElement(ourInboxId);

			    result[0] = mailSession.performSynchronousCall("get_box", paramz);

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
					        		inboxList.add(ii);
					        		
					        		if(ii.isUnread) {
					        			processUnreadMessage(ii,currentServerAddress);
					        		}

				            	}
				            }
						}
			        }
					
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
			
			nextServer();
	    }
    }

	
	//See if notification previously sent.  If not, make a new notification
	private void processUnreadMessage(InboxItem ii,String server) {

		boolean alreadyNotified = checkIfAlreadyNotified(server,Integer.parseInt(ii.sender_id));
		
		if(alreadyNotified) {
			return;
		}
		
		String notificationColor = getString(R.string.default_color);

		String customColor = mailSession.getServer().serverColor;
		
		if(customColor.contains("#")) {
			notificationColor = customColor;
		}
		
		Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		long[] pattern = {500,500,500,500,500,500,500,500,500};
		
		NotificationCompat.Builder mBuilder =
		        new NotificationCompat.Builder(MailService.this)
		        .setSmallIcon(R.drawable.ic_launcher)
		        .setContentTitle("New Message From " + ii.inbox_moderator)
		        .setContentText(ii.inbox_sender)
		        .setSound(alarmSound)
		        .setLights(Color.parseColor(notificationColor), 500, 500)
		        .setVibrate(pattern)
		        .setAutoCancel(true);

		Intent resultIntent = new Intent(MailService.this, Conversation.class);
		Bundle bundle = new Bundle();
		bundle.putString("id",(String) ii.sender_id);
		bundle.putString("boxid",(String) "0");
		bundle.putString("name",(String) ii.inbox_sender);
		bundle.putString("moderator",(String) ii.moderatorId);
		bundle.putString("background",(String) notificationColor);
		bundle.putString("server",mailSession.getServer().serverId);
		resultIntent.putExtras(bundle);
		
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(MailService.this);
		stackBuilder.addParentStack(Conversation.class);
		stackBuilder.addNextIntent(resultIntent);
		
		String flag = ii.sender_id;
		if(flag.length() > 5) {
			flag = flag.substring(flag.length() - 5, flag.length());
		}
		
		PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(Integer.parseInt(flag),PendingIntent.FLAG_UPDATE_CURRENT);
		mBuilder.setContentIntent(resultPendingIntent);
		
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.notify(Integer.parseInt(ii.sender_id), mBuilder.build());
		
		insertNotificationIntoDatabase(server,Integer.parseInt(ii.sender_id));
	}
	
	private void initDatabase() {
    	notetasticDB = this.openOrCreateDatabase("forumfiend", MODE_PRIVATE, null);
    	sql = "create table if not exists notifications(_id integer primary key,server varchar,message integer);";
		notetasticDB.setVersion(4);
		notetasticDB.execSQL(sql);
		notetasticDB.close();
	}
	
	private void insertNotificationIntoDatabase(String server,int notification) {
		String cleanServer = DatabaseUtils.sqlEscapeString(server);
		
		notetasticDB = this.openOrCreateDatabase("forumfiend", MODE_PRIVATE, null);
		
		sql = "insert into notifications(server,message) values(" + cleanServer + "," + notification + ");";
    	notetasticDB.execSQL(sql);
		
		notetasticDB.close();
	}
	
	private boolean checkIfAlreadyNotified(String server,int notification) {
		String cleanServer = DatabaseUtils.sqlEscapeString(server);
		
		notetasticDB = this.openOrCreateDatabase("forumfiend", 0, null);
		
        sql = "select _id " +
        	  "from notifications " +
        	  "where server = " + cleanServer + " " + 
        	  "and message = " + notification + ";";
        
    	Cursor c = notetasticDB.rawQuery(sql,null);
    	
    	
    	
    	if(c == null) {
    		notetasticDB.close();
    		return false;
    	}
    	
    	if(c.getCount() == 0) {
    		notetasticDB.close();
    		return false;
    	}
    	
    	notetasticDB.close();
    	
    	return true;
	}

}
