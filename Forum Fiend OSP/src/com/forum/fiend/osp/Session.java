package com.forum.fiend.osp;

import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;
import de.timroes.axmlrpc.XMLRPCClient;
import de.timroes.axmlrpc.XMLRPCException;
import de.timroes.axmlrpc.XMLRPCServerException;

@SuppressLint({ "NewApi", "TrulyRandom" })
public class Session {
	
	private Context context;
	private Server currentServer;
	private SQLiteDatabase notetasticDB;
	private String sql;
	
	private String avatarSubmissionName = "uploadfile";
	private boolean allowRegistration = false;

	private XMLRPCClient newClient;
	
	private ForumFiendApp application;

	/*
	 *  Forum System Reference
	 *  ----------------------
	 *  0 - Unknown
	 *  1 - phpBB
	 *  2 - MyBB
	 *  3 - vBulletin
	 */
	public int forumSystem = 0;
	public long sessionId;
	
	public Session(Context c,ForumFiendApp app) {
		context = c;
		application = app;
		sessionId = new Date().getTime();
		
		Log.i("Forum Fiend","*** NEW SESSION (" + sessionId + ") ***");
	}
	
	public void setServer(Server server) {
		currentServer = server;
		
		if(server.serverTagline.contentEquals("[*WEBVIEW*]")) {
			return;
		}
		
		if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
			new fetchForumConfiguration().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		} else {
			new fetchForumConfiguration().execute();
		}
	}
	
	public String getAvatarName() {
		return avatarSubmissionName;
	}
	
	public Server getServer() {
		
		if(currentServer == null) {
			currentServer = new Server();
			currentServer.serverColor = context.getString(R.string.default_color);
			currentServer.serverTheme = context.getString(R.string.default_theme);
		}
		
		return currentServer;
	}
	
	public Map<String,String> getCookies() {
		
		if(newClient == null) {
			return null;
		}
		
		Map<String,String> cookies = newClient.getCookies();
		
		return cookies;
	}
	
	public void loginSession(String username, String password) {
		currentServer.serverUserName = username;
		currentServer.serverPassword = password;
		
		if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
			new connectSession().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		} else {
			new connectSession().execute();
		}
	}
	
	public void logOutSession() {
		if(currentServer == null) {
			return;
		}
		
		currentServer.serverUserId = "0";
		currentServer.serverUserName = "0";
		currentServer.serverPassword = "0";
		currentServer.serverPostcount = "0";
		currentServer.serverTab = "0";
		currentServer.serverAvatar = "0";
		
		updateServer();
	}
	
	public void refreshLogin() {
		if(currentServer != null) {
			if(!currentServer.serverUserId.contentEquals("0")) {
				if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
					new connectSession().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
				} else {
					new connectSession().execute();
				}
			}
		}
	}
	
	private class connectSession extends AsyncTask<String, Void, Object[]>  {

		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		protected Object[] doInBackground(String... params) {

			Object[] result = new Object[50];
			
			Log.i("Forum Fiend","Attempting u:" + currentServer.serverUserName + "    " + "p:" + currentServer.serverPassword);

			try {
			    Vector paramz = new Vector();
			    paramz.addElement(currentServer.serverUserName.getBytes());
			    paramz.addElement(currentServer.serverPassword.getBytes());

			    result[0] = performSynchronousCall("login",paramz);

			} catch(Exception ex) {
				//what evs
			}
			
			return result;
		}
		
		@SuppressWarnings("rawtypes")
		protected void onPostExecute(final Object[] result) {
			
			if(result == null) {
				sessionListener.onSessionConnectionFailed("Login Failed.");
				return;
			}
			
			if(result[0] != null) {
	            HashMap map = (HashMap) result[0];
	            
	            if(map.containsKey("result")) {
	            	
	            	Boolean loginSuccess = (Boolean) map.get("result");
	            	
	            	if(loginSuccess) {
	            		
	            		// Submit server login stat to forum owners' analytics account
	            		if(currentServer.analyticsId != null && !currentServer.analyticsId.contentEquals("0")) {
	            			application.getAnalyticsHelper().trackCustomEvent(currentServer.analyticsId,"ff_user","connected",currentServer.serverUserName);
	            		}
	            		
	            		if(map.containsKey("login_name")) {
	            			String loginName = new String((byte[]) map.get("login_name"));
	            			Log.i(context.getString(R.string.app_name),"User login_name is " + loginName);
	            		} else {
	            			Log.e(context.getString(R.string.app_name),"Server provides no login_name information!");
	            		}
	            		
	            		if(map.get("user_id") instanceof Integer) {
	            			currentServer.serverUserId = Integer.toString((Integer) map.get("user_id"));
	            		} else {
	            			currentServer.serverUserId = (String) map.get("user_id");
	            		}
	            		
	            		if(map.containsKey("icon_url")) {
	            			currentServer.serverAvatar = (String) map.get("icon_url");
	            		}
	            		
	            		if(map.get("post_count") != null) {
	            			currentServer.serverPostcount = Integer.toString((Integer) map.get("post_count"));
	    		    	} else {
	    		    		currentServer.serverPostcount = "0";
	    		    	}
	            		
	            		if(map.containsKey("can_profile")) {
	            			boolean canProfile = (Boolean) map.get("can_profile");
	            			
	            			if(canProfile) {
	            				Log.i(context.getString(R.string.app_name),"Use can view and edit profiles!");
	            			} else {
	            				Log.e(context.getString(R.string.app_name),"Use can NOT view or edit profiles!");
	            			}
	            		} else {
	            			Log.e(context.getString(R.string.app_name),"Server provides no profile permission information!");
	            		}
	            		
	            		if(sessionListener != null) {
	            			sessionListener.onSessionConnected();
	            		}

	            	} else {
	            		if(map.containsKey("result_text")) {
	            			String failReason = new String((byte[]) map.get("result_text"));
	            			sessionListener.onSessionConnectionFailed(failReason);
	            			return;
	            		} else {
	            			if(sessionListener != null) {
	            				sessionListener.onSessionConnectionFailed("Wrong username or password.");
	            			}
	            			return;
	            		}
	            		
	            		
	            	}
	            } else {
            		sessionListener.onSessionConnectionFailed("No result key.");
            		return;
	            }
			} else {
				if(sessionListener != null) {
					sessionListener.onSessionConnectionFailed("Login attempt failed.");
				}
        		return;
			}
			
			if(currentServer.serverUserId == null) {
				currentServer.serverUserId = "0";
			}

			updateServer();
		}
	}

	public void updateSpecificServer(Server server) {

		notetasticDB = context.openOrCreateDatabase("forumfiend", Context.MODE_PRIVATE, null);
		
		String cleanId = DatabaseUtils.sqlEscapeString(server.serverId);
        String cleanUserid = DatabaseUtils.sqlEscapeString(server.serverUserId);
        String cleanUsername = DatabaseUtils.sqlEscapeString(server.serverUserName);
        String cleanPassword = DatabaseUtils.sqlEscapeString(server.serverPassword);
        String cleanTagline = DatabaseUtils.sqlEscapeString(server.serverTagline);
        String cleanAvatar = DatabaseUtils.sqlEscapeString(server.serverAvatar);
        String cleanPostcount = DatabaseUtils.sqlEscapeString(server.serverPostcount);
        String cleanColor = DatabaseUtils.sqlEscapeString(server.serverColor);
        String cleanCookies = DatabaseUtils.sqlEscapeString(server.serverCookies);
        String cleanTheme = DatabaseUtils.sqlEscapeString(server.serverTheme);
        String cleanTab = DatabaseUtils.sqlEscapeString(server.serverTab);
        String cleanChatThread = DatabaseUtils.sqlEscapeString(server.chatThread);
        String cleanChatForum = DatabaseUtils.sqlEscapeString(server.chatForum);
        String cleanChatName = DatabaseUtils.sqlEscapeString(server.chatName);
        String cleanIcon = DatabaseUtils.sqlEscapeString(server.serverIcon);
        String cleanname = DatabaseUtils.sqlEscapeString(server.serverName);
        String cleanBackground = DatabaseUtils.sqlEscapeString(server.serverBackground);
        
        String cleanBoxColor = DatabaseUtils.sqlEscapeString(server.serverBoxColor);
        String cleanBoxBorder = DatabaseUtils.sqlEscapeString(server.serverBoxBorder);
        String cleanTextColor = DatabaseUtils.sqlEscapeString(server.serverTextColor);
        String cleanDividerColor = DatabaseUtils.sqlEscapeString(server.serverDividerColor);
        String cleanWallpaper = DatabaseUtils.sqlEscapeString(server.serverWallpaper);
        
        String cleanFFChat = DatabaseUtils.sqlEscapeString(server.ffChatId);
        
        String cleanAnalytics = DatabaseUtils.sqlEscapeString(server.analyticsId);
        String cleanMobfox = DatabaseUtils.sqlEscapeString(server.mobfoxId);
		
		sql = "update accountlist set color = " + cleanColor + ", username = " + cleanUsername + ", password = " + cleanPassword + ", userid = " + cleanUserid + ", avatar = " + cleanAvatar + ", postcount = " + cleanPostcount + ", themeInt = " + cleanTheme + ", cookieCount = " + cleanCookies + ", lastTab = " + cleanTab + ", tagline = " + cleanTagline + ", chatThread = " + cleanChatThread + ", chatForum = " + cleanChatForum + ", chatName = " + cleanChatName + ", icon = " + cleanIcon + ", servername = " + cleanname + ", background = " + cleanBackground + ", boxcolor = " + cleanBoxColor + ", boxborder = " + cleanBoxBorder + ", textcolor = " + cleanTextColor + ", dividercolor = " + cleanDividerColor + ", wallpaper = " + cleanWallpaper + ", ffchat = " + cleanFFChat + ", analytics = " + cleanAnalytics + ", mobfox = " + cleanMobfox + " where _id = " + cleanId + ";";
    	
		try {
			notetasticDB.execSQL(sql);
		} catch(Exception ex) {
			//fuck it for now
		}
		notetasticDB.close();
	}
	
	public void updateServer() {
		
		if(currentServer == null) {
			return;
		}
		
		updateSpecificServer(currentServer);
	}
	
	public interface SessionListener {
		public abstract void onSessionConnected();
		public abstract void onSessionConnectionFailed(String reason);
	}
	
	private SessionListener sessionListener = null;
	
	public void setSessionListener(SessionListener l) {
		sessionListener = l;
	}
	
	private class fetchForumConfiguration extends AsyncTask<String, Void, Object[]>  {

		@SuppressWarnings({ "rawtypes" })
		@Override
		protected Object[] doInBackground(String... params) {

			Object[] result = new Object[50];

			try {
			    Vector paramz = new Vector();
			    result[0] = performSynchronousCall("get_config",paramz);

			} catch(Exception ex) {
				//what evs
			}

			
			return result;
		}
		
		@SuppressWarnings("rawtypes")
		protected void onPostExecute(final Object[] result) {
			/*
			 * 
			 *  THIS MAY HAVE TO BE MOVED IN FUTURE, TO INSURE
			 *  THAT WE HAVE CONFIGURATION SUCCESSFULLY BEFORE
			 *  ATTEMPTING TO LOG IN!
			 * 
			 */
			if(currentServer.serverUserName.contentEquals("0")) {
				sessionListener.onSessionConnected();
			} else {
				if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
					new connectSession().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
				} else {
					new connectSession().execute();
				}
			}
			
			if(result == null) {
				Log.e("Forum Fiend","Fetching Configuration Failed!");
				return;
			}
			
			//Parse tapatalk api data
			if(result[0] != null) {
	            HashMap map = (HashMap) result[0];
	            
	            /*
	            if(map.containsKey("api_level")) {
	            	String api = (String) map.get("api_level");
	            }
	            */

	            if(map.containsKey("version")) {
	            	String system = (String) map.get("version");
	            	
	            	Log.i(context.getString(R.string.app_name),"Forum system code is: " + system);
	            	
	            	if(system.contains("pb")) {
						forumSystem = 1;
						avatarSubmissionName = "uploadfile";
						Log.i(context.getString(R.string.app_name),"Forum is phpBB");
					}
					if(system.contains("mb")) {
						forumSystem = 2;
						avatarSubmissionName = "avatarupload";
						Log.i(context.getString(R.string.app_name),"Forum is MyBB");
					}
					if(system.contains("vb")) {
						forumSystem = 3;
						avatarSubmissionName = "upload";
						Log.i(context.getString(R.string.app_name),"Forum is vBulletin");
					}

	            } else {
	            	Log.e(context.getString(R.string.app_name),"Server returned no system information!");
	            	if(result[0] != null) {
	            		Log.e(context.getString(R.string.app_name),result[0].toString());
	            	}
	            }
	            
	            // see if in-app registration is allowed
				if(map.containsKey("inappreg")) {
					String regKey = (String) map.get("inappreg");
					Log.i(context.getString(R.string.app_name),"Forum inappreg code is: " + regKey);
					
					if(regKey.contentEquals("1")) {
						allowRegistration = true;
					}
				}
			} else {
				Log.e(context.getString(R.string.app_name),"Unable to fetch configuration data!");
        		return;
			}
			
			

		}
	}
	
	@SuppressWarnings("rawtypes")
	public Object performSynchronousCall(String method,Vector parms) {
		
		return performNewSynchronousCall(method,parms);

	}
	
	// Install the all-trusting trust manager
	SSLContext sc;
	// Create empty HostnameVerifier
	HostnameVerifier hv = new HostnameVerifier() {
	public boolean verify(String arg0, SSLSession arg1) {
	return true;
	}
	};
	
	TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
		public X509Certificate[] getAcceptedIssuers() {
		return null;
		}
		 
		public void checkClientTrusted(X509Certificate[] certs,
		String authType) {
		// Trust always
		}
		 
		public void checkServerTrusted(X509Certificate[] certs,
		String authType) {
		// Trust always
		}
		} };
	
	@SuppressLint("TrulyRandom")
	@SuppressWarnings("rawtypes")
	public Object performNewSynchronousCall(String method,Vector parms) {

		Log.d("Forum Fiend", "Performing New Server Call: Method = " + method);
		
		
			try {
				
				Object[] parmsobject = new Object[parms.size()];
				for(int i = 0;i < parms.size();i++) {
					parmsobject[i] = parms.get(i);
				}
				
				if(newClient == null) {
					
					if(currentServer.serverAddress.contains("https")) {
						sc = SSLContext.getInstance("SSL");
						sc.init(null, trustAllCerts, new java.security.SecureRandom());
						HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
						HttpsURLConnection.setDefaultHostnameVerifier(hv);
					}
					
					
					newClient = new XMLRPCClient(new URL(currentServer.serverAddress + "/mobiquo/mobiquo.php"),XMLRPCClient.FLAGS_ENABLE_COOKIES);
				}
				
				

				return newClient.call(method, parmsobject);

			} catch(XMLRPCServerException ex) {
				Log.e(context.getString(R.string.app_name),"Error with tapatalk call er1: " + method);
				if(ex.getMessage() != null) {
					Log.e(context.getString(R.string.app_name),ex.getMessage());
				} else {
					Log.e(context.getString(R.string.app_name),"(no message available)");
				}
			} catch(XMLRPCException ex) {
				Log.e(context.getString(R.string.app_name),"Error with tapatalk call er2: " + method);
				if(ex.getMessage() != null) {
					Log.e(context.getString(R.string.app_name),ex.getMessage());
				} else {
					Log.e(context.getString(R.string.app_name),"(no message available)");
				}
			} catch(Exception ex) {
				Log.e(context.getString(R.string.app_name),"Error with tapatalk call er3: " + method);
				if(ex.getMessage() != null) {
					Log.e(context.getString(R.string.app_name),ex.getMessage());
				} else {
					Log.e(context.getString(R.string.app_name),"(no message available)");
				}
			}
		
		
		
		return null;
		
		
		
		
		/*
		
		Object returnObject = null;
		
		try {
			
			CookieManager cookiemanager = new CookieManager(); 
		    cookiemanager.setCookiePolicy(CookiePolicy.ACCEPT_ALL); 
		    CookieHandler.setDefault(cookiemanager); 
		    cookiemanager.getCookieStore().removeAll();
		    
		    if(authenticatedSession) {

			    for(HttpCookie c:getCookies()) {
			    	try {
			    		URI cookieUri = new URI(c.getDomain());
			    		cookiemanager.getCookieStore().add(cookieUri, c);
			    	} catch(Exception ex) {
			    		//nobody cares
			    	}
			    }
		    }
		    
		    if(client == null) {
		    	XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
		    	
				config.setUserAgent("ForumFiend");
				config.setConnectionTimeout(24000);
				config.setReplyTimeout(24000);
			    config.setServerURL(new URL(currentServer.serverAddress + "/mobiquo/mobiquo.php"));
			    
		    	client = new XmlRpcClient();
		    	client.setConfig(config);

		    }
		    
		    cookiemanager.getCookieStore();

		    XmlRpcTransportFactory tFactory = new XmlRpcSun15HttpTransportFactory(client);

		    client.setTransportFactory(tFactory);

		    returnObject = client.execute(method, parms);
		    
		    if(method.contentEquals("login")) {
			    CookieStore theStore = cookiemanager.getCookieStore();
			    sessionCookies = theStore.getCookies();
			    authenticatedSession = true;
		    }
		    
		    if(authenticatedSession) {
		    	Log.i("Forum Fiend", "Method Success = " + method + " (Authenticated)");
		    } else {
		    	Log.i("Forum Fiend", "Method Success = " + method + " (NOT Authenticated)");
		    }
		    
		} catch(Exception ex) {
			Log.e("Forum Fiend", "Method Fail = " + method);
			
			if(ex.getMessage() != null) {
				Log.e("Forum Fiend", ex.getMessage());
			}
		}
		
		return returnObject;
		*/
	}
	
	public boolean getAllowRegistration() {
		return allowRegistration;
	}
	
}
