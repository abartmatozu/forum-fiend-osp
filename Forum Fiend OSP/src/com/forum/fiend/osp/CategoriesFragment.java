package com.forum.fiend.osp;

import java.util.ArrayList;
import java.util.Vector;

import com.google.gson.internal.LinkedTreeMap;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.ListFragment;

import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.Toast;

@SuppressLint("NewApi")
public class CategoriesFragment extends ListFragment {
	private String server_address;
	private String subforum_id = "0";
	private String background;
	private String userid;
	private Category clicked_category;
	private String username;
	
	//private String hashId = "0";

	private String storagePrefix = "";
	
	private downloadCategories categoriesDownloader;
	private ForumFiendApp application;
	
	
	private String searchQuery = "";
	
	private String passedSubforum = "";

	private String screenTitle;
	private String screenSubtitle;
	
	private int startingPos = 0;
	private int endingPos = 20;
	
	private boolean canScrollMoreThreads = true;
	private boolean isExtraScrolling = false;
	private boolean isLoading = false;
	private boolean initialLoadComplete = false;
	
	private String subforumParts[];
	
	private String shareURL = "0";
	
	private FragmentActivity activity;
	private String totalHash;
	
	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		
		
		
		activity = (FragmentActivity)getActivity();
		
		application = (ForumFiendApp)activity.getApplication();

		if(activity != null) {
			if(activity.getActionBar() != null) {
				if(activity.getActionBar().getSubtitle() != null) {
					screenSubtitle = activity.getActionBar().getSubtitle().toString();
				}
			}
		}

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

		Bundle bundle = getArguments();
		subforum_id = bundle.getString("subforum_id");
        background = bundle.getString("background");
        screenTitle = bundle.getString("subforum_name");
        
        
        passedSubforum = subforum_id;
        
        if(bundle.containsKey("query")) {
        	searchQuery = bundle.getString("query");
        }
        
        //Log.i("Forum Fiend", "**** New CategoriesFragment Instance ****");
        
        //Log.d("Forum Fiend","Passed subforum " + subforum_id);
        
        totalHash = subforum_id;
        
        if(subforum_id.contains("###")) {
        	subforumParts = subforum_id.split("###");
        	Log.d("Forum Fiend","Subforum has " + subforumParts.length + " parts.");
        	subforum_id = subforumParts[0];
        	//hashId = subforumParts[1];
        } else {
        	subforumParts = new String[1];
        	subforumParts[0] = subforum_id;
        }
        
        Log.d("Forum Fiend","Entering subforum " + subforum_id);
        
        server_address = application.getSession().getServer().serverAddress;
        
        if(getString(R.string.server_location).contentEquals("0")) {
        	storagePrefix = server_address + "_";
        }
        
        userid = application.getSession().getServer().serverUserId;
        username = application.getSession().getServer().serverPassword;
        
        String shareId = subforum_id;
        //if(hashId != "0") {
        //	shareId = hashId;
        //}
        
        if(shareId.contentEquals("0")) {
        	shareURL = application.getSession().getServer().serverAddress;
        } else {
        	if(application.getSession().forumSystem == 1) {
        		shareURL = application.getSession().getServer().serverAddress + "/viewforum.php?f=" + shareId;
        	}
        }
        

        getListView().setOnScrollListener(listScrolled);
        
        //Log.d("Forum Fiend","CF OnStart Completed");
	}
	
	@Override
	public void onPause() {
		if(!subforum_id.contentEquals("unread") && !subforum_id.contentEquals("participated") && !subforum_id.contentEquals("userrecent") && !subforum_id.contentEquals("favs") && !subforum_id.contentEquals("search") && !subforum_id.contentEquals("forum_favs")) {
			String scrollY = Integer.toString(getListView().getFirstVisiblePosition());
			
			SharedPreferences app_preferences = activity.getSharedPreferences("prefs", 0);
			SharedPreferences.Editor editor = app_preferences.edit();
	        editor.putString(storagePrefix + "forumScrollPosition" + passedSubforum, scrollY);
	        editor.commit();
		}
		
		endCurrentlyRunning();
		
		super.onPause();
	}
	
	@Override
	public void onResume() {
		
		//Log.d("Forum Fiend","CF OnResume Began");
		
		activity.getActionBar().setTitle(screenTitle);
		activity.getActionBar().setSubtitle(screenSubtitle);
		
		//activity.getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		
		
		SharedPreferences app_preferences = activity.getSharedPreferences("prefs", 0);
		String cachedForum = app_preferences.getString(storagePrefix + "forum" + subforum_id, "n/a");
        
		if(!(cachedForum.contentEquals("n/a"))) {
        	try {
        		Object[][] forumObject = GsonHelper.customGson.fromJson(cachedForum, Object[][].class);
        		parseCachedForums(forumObject);
        		Log.d("Forum Fiend","Forum cache available, using it");
        	} catch(Exception ex) {
        		if(ex.getMessage() != null) {
        			Log.e("Forum Fiend",ex.getMessage());
        		}
        	}
        }

        load_categories();
        
        if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
        	activity.invalidateOptionsMenu();
        }
        
        //Log.d("Forum Fiend","CF OnResume Completed");
		
		super.onResume();
	}
	
	private void endCurrentlyRunning() {
		//Stop any running tasks
		if(categoriesDownloader != null) {
			if(categoriesDownloader.getStatus() == Status.RUNNING) {
				categoriesDownloader.cancel(true);
				Log.i("Forum Fiend","Killed Currently Running");
			}
		}
	}

	@Override
	public void onStop() {
		super.onStop();
		
		endCurrentlyRunning();
	}
	
	private void load_categories() {
		Log.d("Forum Fiend","CF Starting load_categories");
		endCurrentlyRunning();
		categoriesDownloader = new downloadCategories();
		
		if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
			categoriesDownloader.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		} else {
			categoriesDownloader.execute();
		}
	}
	

	
	private class downloadCategories extends AsyncTask<String, Void, Object[][]> {
		
		@Override
	    protected void onPreExecute() {
			Log.i("Forum Fiend","downloadCategories onPreExecute");
	        super.onPreExecute();
	    }
		
		@SuppressLint("UseValueOf")
		@SuppressWarnings({ "unchecked", "rawtypes" })
		@Override
		protected Object[][] doInBackground(String... params) {
			
			Log.i("Forum Fiend","downloadCategories doInBackground");
			
			if(activity == null) {
				Log.e("Forum Fiend","Category activity is null!");
				return null;
			}
			
			isLoading = true;
			


			Object[][] result = new Object[5][50];

			    
			    Vector paramz;
			    
			    //Do not get a forum listing if we are inside one of the special sections
			    if(!subforum_id.contentEquals("timeline") && !subforum_id.contentEquals("unread") && !subforum_id.contentEquals("participated") && !subforum_id.contentEquals("userrecent") && !subforum_id.contentEquals("favs") && !subforum_id.contentEquals("search") && !subforum_id.contentEquals("forum_favs")) {
			    	
			    	if(!isExtraScrolling) {
			    	
				    	try {
						    paramz = new Vector();
						    
						    if(!subforum_id.contentEquals("0")) {
						    	paramz.addElement(new Boolean(true));
						    	paramz.addElement(subforum_id);
						    }
						    
						    
						    
						    //result[0] = (Object[]) application.getSession().performSynchronousCall("get_forum", paramz);
						    result[0] = (Object[]) application.getSession().performNewSynchronousCall("get_forum", paramz);
						    
						    if(result[0] == null) {
						    	Log.e("Forum Fiend","shits null on " + subforum_id);
						    }
						    
				    	} catch (Exception ex) {
				    		if(ex.getMessage() != null) {
				    			Log.w("Forum Fiend", ex.getMessage());
				    		}
						}
					    
				    	try {
						    //First grab any announcement topics
						    paramz = new Vector();
						    paramz.addElement(subforum_id);
						    paramz.addElement(0);
						    paramz.addElement(20);
						    paramz.addElement("ANN");
						    //result[1][0] = application.getSession().performSynchronousCall("get_topic", paramz);
						    result[1][0] = application.getSession().performNewSynchronousCall("get_topic", paramz);
				    	} catch (Exception ex) {
				    		if(ex.getMessage() != null) {
				    			
				    			Log.w("Forum Fiend", ex.getMessage());
				    		}
						}
					    
				    	try {
						    //Then grab any sticky topics
						    paramz = new Vector();
						    paramz.addElement(subforum_id);
						    paramz.addElement(0);
						    paramz.addElement(20);
						    paramz.addElement("TOP");
						    //result[2][0] = application.getSession().performSynchronousCall("get_topic", paramz);
						    result[2][0] = application.getSession().performNewSynchronousCall("get_topic", paramz);
				    	} catch (Exception ex) {
				    		if(ex.getMessage() != null) {
				    			Log.w("Forum Fiend", ex.getMessage());
				    		}
						}
			    	
			    	}
				    
			    	try {
					    //Grab the non-sticky topics
			    		
			    		Log.d("Forum Fiend","Getting topics " + startingPos + " through " + endingPos);
			    		
					    paramz = new Vector();
					    paramz.addElement(subforum_id);
					    paramz.addElement(startingPos);
					    paramz.addElement(endingPos);
					    //result[3][0] = application.getSession().performSynchronousCall("get_topic", paramz);
					    result[3][0] = application.getSession().performNewSynchronousCall("get_topic", paramz);
					    
			    	} catch (Exception ex) {
			    		if(ex.getMessage() != null) {
			    			Log.w("Forum Fiend", ex.getMessage());
			    		}
					}
			    }
			    
			    //Handle topic listing for the Search function
			    if(subforum_id.contentEquals("search")) {
			    	try {
				    	paramz = new Vector();
				    	paramz.addElement(searchQuery.getBytes());
				    	paramz.addElement(startingPos);
					    paramz.addElement(endingPos);
					    //result[3][0] = application.getSession().performSynchronousCall("search_topic", paramz);
					    result[3][0] = application.getSession().performNewSynchronousCall("search_topic", paramz);
			    	} catch (Exception ex) {
			    		if(ex.getMessage() != null) {
			    			Log.w("Forum Fiend", ex.getMessage());
			    		}
					}
			    }
			    
			    if(!isExtraScrolling) {
			    
				    //Handle topic listing for the Unread category
				    if(subforum_id.contentEquals("unread")) {
				    	try {
					    	paramz = new Vector();
						    //result[3][0] = application.getSession().performSynchronousCall("get_unread_topic", paramz);
						    result[3][0] = application.getSession().performNewSynchronousCall("get_unread_topic", paramz);
				    	} catch (Exception ex) {
				    		if(ex.getMessage() != null) {
				    			Log.w("Forum Fiend", ex.getMessage());
				    		}
						}
				    }
			    
			    }
			    
			    //Handle timeline get_latest_topic
			    if(subforum_id.contentEquals("timeline")) {
			    	try {
				    	paramz = new Vector();
				    	//paramz.addElement(username.getBytes());
					    paramz.addElement(startingPos);
					    paramz.addElement(endingPos);
					    //paramz.addElement("");
					    //paramz.addElement(userid);
					    //result[3][0] = application.getSession().performSynchronousCall("get_participated_topic", paramz);
					    result[3][0] = application.getSession().performNewSynchronousCall("get_latest_topic", paramz);
			    	} catch (Exception ex) {
			    		if(ex.getMessage() != null) {
			    			Log.w("Forum Fiend", ex.getMessage());
			    		}
					}
			    }
			    
			    //Handle participated topics category
			    if(subforum_id.contentEquals("participated")) {
			    	try {
				    	paramz = new Vector();
				    	paramz.addElement(username.getBytes());
					    paramz.addElement(startingPos);
					    paramz.addElement(endingPos);
					    paramz.addElement("");
					    paramz.addElement(userid);
					    //result[3][0] = application.getSession().performSynchronousCall("get_participated_topic", paramz);
					    result[3][0] = application.getSession().performNewSynchronousCall("get_participated_topic", paramz);
			    	} catch (Exception ex) {
			    		if(ex.getMessage() != null) {
			    			Log.w("Forum Fiend", ex.getMessage());
			    		}
					}
			    }
			    
			    //Handle subscription category
			    if(subforum_id.contentEquals("favs")) {
			    	try {
				    	paramz = new Vector();
					    paramz.addElement(startingPos);
					    paramz.addElement(endingPos);
					    //result[3][0] = application.getSession().performSynchronousCall("get_subscribed_topic", paramz);
					    result[3][0] = application.getSession().performNewSynchronousCall("get_subscribed_topic", paramz);
			    	} catch (Exception ex) {
			    		if(ex.getMessage() != null) {
			    			Log.w("Forum Fiend", ex.getMessage());
			    		}
					}
			    }
			    
			    //Handle favorites category
			    if(subforum_id.contentEquals("forum_favs")) {
			    	try {
				    	paramz = new Vector();
				    	//result[0] = (Object[]) client.execute("get_subscribed_forum", paramz);
					    //result[4][0] = application.getSession().performSynchronousCall("get_subscribed_forum", paramz);
					    result[4][0] = application.getSession().performNewSynchronousCall("get_subscribed_forum", paramz);
			    	} catch (Exception ex) {
			    		if(ex.getMessage() != null) {
			    			Log.w("Forum Fiend", "Favorites Error: " + ex.getMessage());
			    		}
					}
			    }

			return result;
		}
		
		protected void onPostExecute(final Object[][] result) {
			
			Log.i("Forum Fiend","downloadCategories onPostExecute");
			
			if(activity == null) {
				return;
			}
			
			if(result == null) {
				Toast toast = Toast.makeText(activity, "Error pulling data from the server, ecCFDL", Toast.LENGTH_SHORT);
				toast.show();
				return;
			}
			
			Log.i("Forum Fiend","Recieved category data!");
			
			initialLoadComplete = true;
			isLoading = false;

			String objectString = GsonHelper.customGson.toJson(result);
			
			//Log.i("Forum Fiend",objectString);
			
			SharedPreferences app_preferences = activity.getSharedPreferences("prefs", 0);
			String cachedForum = app_preferences.getString(storagePrefix + "forum" + subforum_id, "n/a");
			
			if(objectString.contentEquals(cachedForum)) {
				return;
			} else {
				if(!isExtraScrolling) {
					SharedPreferences.Editor editor = app_preferences.edit();
		            editor.putString(storagePrefix + "forum" + subforum_id, objectString);
		            editor.commit();
				}
			}
			
			if(activity != null) {
				Object[][] forumObject = GsonHelper.customGson.fromJson(objectString, Object[][].class);
				parseCachedForums(forumObject);
			}
			
			
		}
	}
	
	private ArrayList<Category> categoryList;
	private boolean initialParseDone = false;

	@SuppressWarnings("rawtypes")
	private void parseCachedForums(Object[][] result) {

		if(categoryList == null || !isExtraScrolling) {
			categoryList = new ArrayList<Category>();
		}
		
		int retainedPosition = getListView().getFirstVisiblePosition();
		
		if(!initialParseDone) {
			SharedPreferences app_preferences = activity.getSharedPreferences("prefs", 0);
			String savedForumPosition = app_preferences.getString(storagePrefix + "forumScrollPosition" + passedSubforum, "0");
			retainedPosition = Integer.parseInt(savedForumPosition);
		}
		
		//Announcement Topics
		for(Object o: result[1]) {
			
			if(o != null) {
				LinkedTreeMap map = (LinkedTreeMap) o;
	            
	            if(map.containsKey("topics")) {
	            	ArrayList topics = (ArrayList) map.get("topics");
	            	for(Object t:topics) {
	            		
	            		LinkedTreeMap topicMap = (LinkedTreeMap) t;

						
						Category ca = new Category();
						ca.category_name = (String) topicMap.get("topic_title");
						ca.subforum_id = subforum_id;
						
						//if(!hashId.contentEquals("0")) {
						//	ca.subforum_id = hashId;
						//}
						
						ca.category_id = (String) topicMap.get("topic_id");
						ca.category_lastupdate = (String) topicMap.get("last_reply_time");
						ca.category_lastthread = (String) topicMap.get("topic_author_name");
						ca.topicSticky = "Y";
						ca.categoryType = "C";
						ca.categoryColor = background;
						
						if(topicMap.get("reply_number") != null) {
							ca.thread_count = topicMap.get("reply_number").toString().replace(".0", "");
						}
						
						if(topicMap.get("view_number") != null) {
							ca.view_count = topicMap.get("view_number").toString().replace(".0", "");
						}
						
						if(topicMap.get("new_post") != null) {
							ca.hasNewTopic = (Boolean) topicMap.get("new_post");
						}
						
						if(topicMap.get("is_closed") != null) {
							ca.isLocked = (Boolean) topicMap.get("is_closed");
						}
						
						if(topicMap.containsKey("icon_url")) {
							if(topicMap.get("icon_url") != null) {
								ca.categoryIcon = (String) topicMap.get("icon_url");
							}
						}
						
						if(topicMap.get("can_stick") != null) {
							ca.canSticky = (Boolean) topicMap.get("can_stick");
						}
						
						if(topicMap.get("can_delete") != null) {
							ca.canDelete = (Boolean) topicMap.get("can_delete");
						}
						
						if(topicMap.get("can_close") != null) {
							ca.canLock = (Boolean) topicMap.get("can_close");
						}
						
						categoryList.add(ca);
	            	}
	            }

			}
        }
				
				
		//Sticky Topics
		for(Object o: result[2]) {
			
			if(o != null) {
				LinkedTreeMap map = (LinkedTreeMap) o;
	            
	            if(map.containsKey("topics")) {
	            	ArrayList topics = (ArrayList) map.get("topics");
	            	for(Object t:topics) {
	            		
	            		LinkedTreeMap topicMap = (LinkedTreeMap) t;
	     
						Category ca = new Category();
						ca.category_name = (String) topicMap.get("topic_title");
						ca.subforum_id = subforum_id;
						
						//if(!hashId.contentEquals("0")) {
						//	ca.subforum_id = hashId;
						//}
						
						ca.category_id = (String) topicMap.get("topic_id");
						ca.category_lastupdate = (String) topicMap.get("last_reply_time");
						ca.category_lastthread = (String) topicMap.get("topic_author_name");
						ca.topicSticky = "Y";
						ca.categoryType = "C";
						ca.categoryColor = background;
						
						if(topicMap.get("reply_number") != null) {
							ca.thread_count = topicMap.get("reply_number").toString().replace(".0", "");
						}
						
						if(topicMap.get("view_number") != null) {
							ca.view_count = topicMap.get("view_number").toString().replace(".0", "");
						}
						
						if(topicMap.get("new_post") != null) {
							ca.hasNewTopic = (Boolean) topicMap.get("new_post");
						}
						
						if(topicMap.containsKey("icon_url")) {
							if(topicMap.get("icon_url") != null) {
								ca.categoryIcon = (String) topicMap.get("icon_url");
							}
						}
						
						if(topicMap.get("is_closed") != null) {
							ca.isLocked = (Boolean) topicMap.get("is_closed");
						}
						
						if(topicMap.get("can_stick") != null) {
							ca.canSticky = (Boolean) topicMap.get("can_stick");
						}
						
						if(topicMap.get("can_delete") != null) {
							ca.canDelete = (Boolean) topicMap.get("can_delete");
						}
						
						if(topicMap.get("can_close") != null) {
							ca.canLock = (Boolean) topicMap.get("can_close");
						}
						
						categoryList.add(ca);
	            	}
	            }

			}
        }

		Log.d("Forum Fiend","Starting category parse!");
		
		//Forums
		if(result[0] != null) {
			
			ArrayList<Category> forumz = CategoryParser.parseCategories(result[0], subforum_id, background);
			
			Log.d("Forum Fiend","Forums parsed!");
			
			String currentHash = subforumParts[0];
			
			Log.d("Forum Fiend","Hash Size: " + subforumParts.length);
			
			if(subforumParts.length == 1) {
				
				for(Category c:forumz) {
					categoryList.add(c);
				}

			} else {
				for(int i = 1;i<subforumParts.length;i++) {
					currentHash = currentHash + "###" + subforumParts[i];
					
					Log.d("Forum Fiend","Checking hash: " + currentHash + " (total hash is " + totalHash + ")");

					ArrayList<Category> tempForums = null;
					
					for(Category c:forumz) {
						if(c.children != null && c.category_id.contentEquals(currentHash)) {
							tempForums = c.children;
						}
					}
					
					if(tempForums != null) {
						forumz = tempForums;
						
						if(currentHash.contentEquals(totalHash)) {
							for(Category c:forumz) {
								categoryList.add(c);
							}
						}
					}
				}

			}
		}
			
			
		Log.d("Forum Fiend","Finished category parse!");
			
			
			
			
			//sdf
			
			/*
			
			for(Object o: result[0]) {
				if(o != null) {
	
					
		            LinkedTreeMap map = (LinkedTreeMap) o;
	
					Category ca = new Category();
					ca.category_name = (String) map.get("forum_name");
					ca.subforum_id = subforum_id;
					ca.category_id = (String) map.get("forum_id");
					ca.categoryType = "S";
					ca.categoryColor = background;
					
					if(map.containsKey("logo_url")) {
						if(map.get("logo_url") != null) {
							ca.categoryIcon = (String) map.get("logo_url");
						}
					}
					
					if(map.containsKey("url")) {
						if(map.get("url") != null) {
							ca.category_URL = (String) map.get("url");
						}
					}
					
					if(map.get("is_subscribed") != null) {
						ca.isSubscribed = (Boolean) map.get("is_subscribed");
					}
					
					if(map.get("can_subscribe") != null) {
						ca.canSubscribe = (Boolean) map.get("can_subscribe");
					}
					
					if(map.get("new_post") != null) {
						ca.hasNewTopic = (Boolean) map.get("new_post");
					}
					
					Boolean subOnly = false;
					
					if(map.containsKey("sub_only")) {
						if(map.get("sub_only") != null) {
							subOnly = (Boolean) map.get("sub_only");
							ca.hasChildren = true;
							Log.e("Forum Fiend","aaa sub only on " + ca.category_id);
						}
					}
					
					if(subOnly) {
						
						if(map.containsKey("child")) {
							if(map.get("child") != null) {
								
								ca.category_id = subforum_id + "###" + (String) map.get("forum_id");
								
								//Fix for forums with custom URL layouts 
								ca.category_URL = "n/a";
								
								if(!hashId.contentEquals("0")) {
									//Log.d("Forum Fiend","Building Child Array from " + map.get("child").toString());
									ArrayList childArray = (ArrayList) map.get("child");
	
									for(Object childForum : childArray) {
										LinkedTreeMap childMap = (LinkedTreeMap) childForum;
										
										String parentForum = (String) childMap.get("parent_id");
	
										if(parentForum.contentEquals(hashId)) {
											ca = new Category();
											ca.category_name = (String) childMap.get("forum_name");
											ca.subforum_id = hashId;
											//ca.subforum_id = hashId;
											ca.category_id = (String) childMap.get("forum_id");
											ca.categoryType = "S";
											ca.categoryColor = background;
											
											if(childMap.containsKey("logo_url")) {
												if(childMap.get("logo_url") != null) {
													ca.categoryIcon = (String) childMap.get("logo_url");
												}
											}
											
											if(childMap.get("new_post") != null) {
												ca.hasNewTopic = (Boolean) childMap.get("new_post");
											}
											
											if(childMap.containsKey("sub_only")) {
												if(childMap.get("sub_only") != null) {
													ca.hasChildren = (Boolean) childMap.get("sub_only");
													if(ca.hasChildren) {
														Log.e("Forum Fiend","bbb sub only on " + ca.category_id);
														if(childMap.containsKey("child")) {
															if(childMap.get("child") != null) {
																Log.e("Forum Fiend","jesus fuck even more children here for " + ca.category_id);
															}
														}
													}
												}
											}
											
											if(childMap.containsKey("url")) {
												if(childMap.get("url") != null) {
													ca.category_URL = (String) childMap.get("url");
												}
											}
											
											if(childMap.get("is_subscribed") != null) {
												ca.isSubscribed = (Boolean) childMap.get("is_subscribed");
											}
											
											if(childMap.get("can_subscribe") != null) {
												ca.canSubscribe = (Boolean) childMap.get("can_subscribe");
											}
											
											if(childMap.containsKey("child")) {
												if(childMap.get("child") != null) {
													
													Log.e("Forum Fiend","children for " + ca.category_id);
	
													ca.hasChildren = true;
													
													ArrayList childerArray = (ArrayList) childMap.get("child");
	
													for(Object childForumr : childerArray) {
														LinkedTreeMap childMapr = (LinkedTreeMap) childForumr;
														if(childMapr.get("new_post") != null) {
															boolean hasNew = (Boolean) childMapr.get("new_post");
															
															if(hasNew) {
																ca.hasNewTopic = hasNew;
															}
														}
													}
													
												}
											}
											
											
											
											categoryList.add(ca);
										}
									}
								}
								
							}
						}
					} else {
	
						//Check for unread posts in children
						if(map.containsKey("child")) {
							if(map.get("child") != null) {
								ca.hasChildren = true;
								
								ArrayList childArray = (ArrayList) map.get("child");
	
								for(Object childForum : childArray) {
									LinkedTreeMap childMap = (LinkedTreeMap) childForum;
									if(childMap.get("new_post") != null) {
										boolean hasNew = (Boolean) childMap.get("new_post");
										
										if(hasNew) {
											ca.hasNewTopic = hasNew;
										}
									}
								}
								
							}
						}
						
					}
					
					if(hashId.contentEquals("0")) {
						categoryList.add(ca);
					}
					
	
				}
	        }
		}
		*/

		//Non-Sticky Topics
		
		if(result[3] == null || result[3].length == 0) {
			canScrollMoreThreads = false;
		}
		
		for(Object o: result[3]) {
			
			if(o != null) {
				LinkedTreeMap map = (LinkedTreeMap) o;
	            
	            if(map.containsKey("topics")) {
	            	ArrayList topics = (ArrayList) map.get("topics");
	            	for(Object t:topics) {
	            		
	            		LinkedTreeMap topicMap = (LinkedTreeMap) t;
	            		

						Category ca = new Category();
						ca.category_name = (String) topicMap.get("topic_title");
						
						if(topicMap.get("forum_id") != null) {
							ca.subforum_id = (String) topicMap.get("forum_id");
						} else {
							ca.subforum_id = subforum_id;
							
							//if(!hashId.contentEquals("0")) {
							//	ca.subforum_id = hashId;
							//}
						}
						
						ca.category_id = (String) topicMap.get("topic_id");
						ca.category_lastupdate = (String) topicMap.get("last_reply_time");
						
						if(topicMap.get("topic_author_name") != null) {
							ca.category_lastthread = (String) topicMap.get("topic_author_name");
						} else {
							ca.category_lastthread = (String) topicMap.get("forum_name");
						}
						
						
						ca.categoryType = "C";
						ca.categoryColor = background;
						
						
						if(topicMap.get("reply_number") != null) {
							ca.thread_count = topicMap.get("reply_number").toString().replace(".0", "");
						}
						
						if(topicMap.get("view_number") != null) {
							ca.view_count = topicMap.get("view_number").toString().replace(".0", "");
						}
						
						//Log.d("Forum Fiend",(String) topicMap.get("reply_number"));
						
						if(topicMap.get("new_post") != null) {
							ca.hasNewTopic = (Boolean) topicMap.get("new_post");
						}
						
						if(topicMap.containsKey("icon_url")) {
							if(topicMap.get("icon_url") != null) {
								ca.categoryIcon = (String) topicMap.get("icon_url");
							}
						}
						
						if(topicMap.get("can_stick") != null) {
							ca.canSticky = (Boolean) topicMap.get("can_stick");
						}
						
						if(topicMap.get("can_delete") != null) {
							ca.canDelete = (Boolean) topicMap.get("can_delete");
						}
						
						if(topicMap.get("can_close") != null) {
							ca.canLock = (Boolean) topicMap.get("can_close");
						}
						
						if(topicMap.get("is_closed") != null) {
							ca.isLocked = (Boolean) topicMap.get("is_closed");
						}
						
						categoryList.add(ca);
	            	}
	            }
			}
        }
		

		for(Object o: result[4]) {
			
			
			
			if(o != null) {
				
				Log.i("Forum Fiend","We have some favs!");
				
				LinkedTreeMap map = (LinkedTreeMap) o;
	            
	            if(map.containsKey("forums")) {
	            	ArrayList forums = (ArrayList) map.get("forums");
	            	for(Object f:forums) {
	            		
	            		LinkedTreeMap forumMap = (LinkedTreeMap) f;
	            		
	            		Category ca = new Category();
	    				ca.category_name = (String) forumMap.get("forum_name");
	    				ca.subforum_id = subforum_id;
	    				ca.category_id = (String) forumMap.get("forum_id");
	    				ca.categoryType = "S";
	    				ca.categoryColor = background;
	    				
	    				if(forumMap.containsKey("icon_url")) {
	    					if(forumMap.get("icon_url") != null) {
	    						ca.categoryIcon = (String) forumMap.get("icon_url");
	    					}
	    				}

	    				ca.isSubscribed = true;

	    				if(forumMap.get("new_post") != null) {
	    					ca.hasNewTopic = (Boolean) forumMap.get("new_post");
	    				}
	    				
	    				categoryList.add(ca);
	            	}
	            } else {
	            	Log.e("Forum Fiend","Favs has no forums!");
	            }
			} else {
				
			}
		}
		
		
		
		
		
		
		setListAdapter(new CategoryAdapter(categoryList,activity,application));
		registerForContextMenu(getListView());
		getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {

			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,long arg3) {
				Category sender = (Category) arg0.getItemAtPosition(arg2);
				
				if(sender == null) {
					return;
				}
				
				if(categorySelected == null) {
					return;
				}
				
				categorySelected.onCategorySelected(sender);
			}
		});

		
		getListView().setSelection(retainedPosition);
		
		initialParseDone = true;
    }
	
	/*
	private void setChatThread() {
		
        application.getSession().getServer().chatThread = clicked_category.category_id;
        application.getSession().getServer().chatForum = clicked_category.subforum_id;
        application.getSession().getServer().chatName = clicked_category.category_name;
        application.getSession().updateServer();
        
        chatChanged.onChatChanged(application.getSession().getServer().chatThread);
	}
	*/
		
	public void onCreateContextMenu(ContextMenu menu, View v,ContextMenuInfo menuInfo) {

    	String the_userid = application.getSession().getServer().serverUserId;

    	AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
    	
    	
    	clicked_category = (Category) CategoriesFragment.this.getListView().getItemAtPosition(info.position);
    	
    	if(the_userid.contentEquals("0"))
    		return;
    	
    	super.onCreateContextMenu(menu, v, menuInfo);
		menu.setHeaderTitle(clicked_category.category_name);
		MenuInflater inflater = activity.getMenuInflater();
		
		inflater.inflate(R.menu.categories_context, menu);
		
		MenuItem ubsubItem = menu.findItem(R.id.categories_unsubscribe);
		MenuItem subItem = menu.findItem(R.id.categories_subscribe);
		MenuItem stickyItem = menu.findItem(R.id.categories_context_sticky);
		MenuItem lockItem = menu.findItem(R.id.categories_context_lock);
		MenuItem deleteItem = menu.findItem(R.id.categories_context_delete);

		MenuItem subscribeItem = menu.findItem(R.id.categories_add_favorite);
		MenuItem unsubscribeItem = menu.findItem(R.id.categories_remove_favorite);
    	
    	if(clicked_category.categoryType.contentEquals("S")) {
    		ubsubItem.setVisible(false);
    		subItem.setVisible(false);
    		stickyItem.setVisible(false);
    		lockItem.setVisible(false);
    		deleteItem.setVisible(false);

    		if(clicked_category.canSubscribe) {
    			subscribeItem.setVisible(true);
    		} else {
    			subscribeItem.setVisible(false);
    		}
    		
    		if(clicked_category.isSubscribed) {
    			unsubscribeItem.setVisible(true);
    			subscribeItem.setVisible(false);
    		} else {
    			unsubscribeItem.setVisible(false);
    		}
    	} else {
    		
    		unsubscribeItem.setVisible(false);
			subscribeItem.setVisible(false);
    		
    		
    		if(clicked_category.canSticky) {
    			stickyItem.setVisible(true);
    			
    			if(clicked_category.topicSticky.contentEquals("N")) {
    				stickyItem.setTitle("Stick Topic");
    			} else {
    				stickyItem.setTitle("Unstick Topic");
    			}
    		} else {
    			stickyItem.setVisible(false);
    		}
    		
    		if(clicked_category.canDelete) {
    			deleteItem.setVisible(true);
    		} else {
    			deleteItem.setVisible(false);
    		}
    		
    		if(clicked_category.canLock) {
    			lockItem.setVisible(true);
    			
    			if(clicked_category.isLocked) {
    				lockItem.setTitle("Unlock Topic");
    			} else {
    				lockItem.setTitle("Lock Topic");
    			}
    		} else {
    			lockItem.setVisible(false);
    		}
    		
    		if(subforum_id.contentEquals("favs")) {
    			ubsubItem.setVisible(true);
    			subItem.setVisible(false);
    		} else {
    			ubsubItem.setVisible(false);
    			subItem.setVisible(true);
    		}

    	}

		
		
		
    }
	
	public boolean onContextItemSelected(MenuItem item) {
		  switch (item.getItemId()) 
		  {
		  case R.id.categories_unsubscribe:
			  new unsubscribeTopic().execute(clicked_category.category_id);
			  return true;
		  case R.id.categories_subscribe:
			  new subscribeTopic().execute(clicked_category.category_id);
			  return true;
		  case R.id.categories_context_sticky:
			  if(clicked_category.topicSticky.contentEquals("N")) {
				  new stickyTopic().execute(clicked_category.category_id,"1");
			  } else {
				  new stickyTopic().execute(clicked_category.category_id,"2");
			  }
			  return true;
		  case R.id.categories_context_lock:
			  if(clicked_category.isLocked) {
				  new lockTopic().execute(clicked_category.category_id,"1"); 
			  } else {
				  new lockTopic().execute(clicked_category.category_id,"2"); 
			  }
			  return true;
		  case R.id.categories_context_delete_yes:
			  new deleteTopic().execute(clicked_category.category_id);
			  return true;
		  case R.id.categories_add_favorite:
			  new addToFavorites().execute(clicked_category.category_id);
			  return true;
		  case R.id.categories_remove_favorite:
			  new removeFromFavorites().execute(clicked_category.category_id);
			  return true;
		  default:
		    return super.onContextItemSelected(item);
		  }
	}

	
	@SuppressLint("NewApi")
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		
		if(userid != null) {
			if(!userid.contentEquals("0")) {
				inflater.inflate(R.menu.categories_menu, menu);
			}
		}

	    super.onCreateOptionsMenu(menu, inflater);

	}
	
	@Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        
        if(userid != null) {
	        if(!userid.contentEquals("0") && menu != null) {
	        
		        if(subforum_id.contentEquals("0") || subforum_id.contentEquals("participated") || subforum_id.contentEquals("favs") || subforum_id.contentEquals("search")) {
					MenuItem item = menu.findItem(R.id.cat_mark_read);
					if(item != null) {
						item.setVisible(false);
					}
				} else {
					MenuItem item = menu.findItem(R.id.cat_mark_read);
					if(item != null) {
						if(ForegroundColorSetter.getForegroundDark(background)) {
							item.setIcon(R.drawable.ic_action_read_dark);
			        	}
					}
				}
		        
		        if(subforum_id.contentEquals("0") || subforum_id.contentEquals("participated") || subforum_id.contentEquals("favs") || subforum_id.contentEquals("userrecent") || subforum_id.contentEquals("search")) {
					MenuItem item2 = menu.findItem(R.id.cat_new_thread);
					if(item2 != null) {
						item2.setVisible(false);
					}
				} else {
					MenuItem item2 = menu.findItem(R.id.cat_new_thread);
					if(item2 != null) {
						if(ForegroundColorSetter.getForegroundDark(background)) {
							item2.setIcon(R.drawable.ic_action_new_dark);
			        	}
					}
				}
		        
		        MenuItem browserItem = menu.findItem(R.id.cat_open_browser);
		        
		        if(shareURL.contentEquals("0")) {
		        	browserItem.setVisible(false);
		        } else {
		        	browserItem.setVisible(true);
		        }
	        
	        }
        }
        
        

	}
	
	@Override
	public boolean onOptionsItemSelected (MenuItem item) {
		switch (item.getItemId()) {
        case R.id.cat_new_thread:
        	start_post();
            return true;
        case R.id.cat_mark_read:
        	markAsRead();
        	return true;
        case R.id.cat_open_browser:
        	Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(shareURL));
        	startActivity(browserIntent);
        	return true;
        default:
            return super.onOptionsItemSelected(item);
        }
	}
	
	private void start_post() {
		
		if(subforum_id.contentEquals("0") || userid.contentEquals("0")) {
			Toast toast = Toast.makeText(activity, "You are not allowed to post here!", Toast.LENGTH_LONG);
			toast.show();
			return;
		}
    	
    	Intent myIntent = new Intent(activity, New_Post.class);
		
		Bundle bundle = new Bundle();
		bundle.putString("postid",(String) "0");
		bundle.putString("parent",(String) "0");
		bundle.putString("category",(String) subforum_id);
		bundle.putString("subforum_id",(String) subforum_id);
		bundle.putString("original_text",(String) "");
		bundle.putString("boxTitle",(String) "New Thread");
		bundle.putString("picture",(String) "0");
		bundle.putString("subject",(String) "");
		bundle.putInt("post_type",(Integer) 1);
		bundle.putString("color",(String) background);
		myIntent.putExtras(bundle);
		
		startActivity(myIntent);

    }

	//Category Selected Interface
	public interface onCategorySelectedListener {
		public abstract void onCategorySelected(Category ca);
	}
	
	private onCategorySelectedListener categorySelected = null;
	
	public void setOnCategorySelectedListener(onCategorySelectedListener l) {
		categorySelected = l;
	}

	private void markAsRead() {
		new readMarker().execute(subforum_id);
	}
	
	private class readMarker extends AsyncTask<String, Void, String> {
		
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
			    if(!params[0].contentEquals("0") && !params[0].contentEquals("unread")) {
			    	paramz.addElement(params[0]);
			    }
			    //application.getSession().performSynchronousCall("mark_all_as_read", paramz);
			    application.getSession().performNewSynchronousCall("mark_all_as_read", paramz);

			} catch (Exception ex) {
				Log.w("Discussions", ex.getMessage());
			}

			return result;
		}
		
		protected void onPostExecute(final String result) {
			
			if(activity == null) {
				return;
			}
	
			if(subforum_id.contentEquals("unread")) {
				activity.finish();
			}else {
				load_categories();
				Toast toast = Toast.makeText(activity, "Posts marked read!", Toast.LENGTH_LONG);
				toast.show();
			}
		}
	}
	
	private class subscribeTopic extends AsyncTask<String, Void, String> {
		
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
			    //application.getSession().performSynchronousCall("subscribe_topic", paramz);
			    application.getSession().performNewSynchronousCall("subscribe_topic", paramz);

			} catch (Exception ex) {
				Log.w("Discussions", ex.getMessage());
			}

			return result;
		}
		
		protected void onPostExecute(final String result) {
			
			if(activity == null) {
				return;
			}
			
			Toast toast = Toast.makeText(activity, "Subscribed!", Toast.LENGTH_SHORT);
			toast.show();
		}
	}
	
	private class unsubscribeTopic extends AsyncTask<String, Void, String> {
		
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
			    //application.getSession().performSynchronousCall("unsubscribe_topic", paramz);
			    application.getSession().performNewSynchronousCall("unsubscribe_topic", paramz);

			} catch (Exception ex) {
				Log.w("Discussions", ex.getMessage());
			}

			return result;
		}
		
		protected void onPostExecute(final String result) {
			
			if(activity == null) {
				return;
			}
			
			load_categories();
		}
	}
	
	
	private class stickyTopic extends AsyncTask<String, Void, String> {
		
		// parm[0] - (string)topic_id
		// parm[1] - (int)mode (1 - stick; 2 - unstick)
		
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
			    paramz.addElement(Integer.parseInt(params[1]));
			    //application.getSession().performSynchronousCall("m_stick_topic", paramz);
			    application.getSession().performNewSynchronousCall("m_stick_topic", paramz);

			} catch (Exception ex) {
				Log.w("Forum Fiend", ex.getMessage());
			}

			return result;
		}
		
		protected void onPostExecute(final String result) {
			
			if(activity == null) {
				return;
			}
			
			load_categories();
		}
	}
	
	private class lockTopic extends AsyncTask<String, Void, String> {
		
		// parm[0] - (string)topic_id
		// parm[1] - (int)mode (1 - unlock; 2 - lock)
		
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
			    paramz.addElement(Integer.parseInt(params[1]));
			    //application.getSession().performSynchronousCall("m_close_topic", paramz);
			    application.getSession().performNewSynchronousCall("m_close_topic", paramz);

			} catch (Exception ex) {
				Log.w("Forum Fiend", ex.getMessage());
			}

			return result;
		}
		
		protected void onPostExecute(final String result) {
			
			if(activity == null) {
				return;
			}
			
			load_categories();
		}
	}

	private class deleteTopic extends AsyncTask<String, Void, String> {
		
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
			    //application.getSession().performSynchronousCall("m_delete_topic", paramz);
			    application.getSession().performNewSynchronousCall("m_delete_topic", paramz);

			} catch (Exception ex) {
				Log.w("Forum Fiend", ex.getMessage());
			}

			return result;
		}
		
		protected void onPostExecute(final String result) {
			
			if(activity == null) {
				return;
			}
			
			load_categories();
		}
	}
	
	private OnScrollListener listScrolled = new OnScrollListener() {

		@Override
		public void onScroll(AbsListView arg0, int arg1, int arg2, int arg3) {
			//do nothing
		}

		@Override
		public void onScrollStateChanged(AbsListView arg0, int arg1) {
			
			if(!canScrollMoreThreads || isLoading) {
				return;
			}
			
			if(categoryList == null) {
				return;
			}
			
			if(categoryList.size() < 20) {
				return; 
			}
			
			if(!initialLoadComplete) {
				return;
			}
			
			if(arg1 == SCROLL_STATE_IDLE) {
				if(arg0.getLastVisiblePosition() >= categoryList.size() - 5) {
					isExtraScrolling = true;

					startingPos = endingPos + 1;
					endingPos = startingPos + 20;
					
					categoriesDownloader = new downloadCategories();
					categoriesDownloader.execute();
				}
			}
		}
		
	};

	private class addToFavorites extends AsyncTask<String, Void, String> {
		
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
			    //application.getSession().performSynchronousCall("subscribe_forum", paramz);
			    application.getSession().performNewSynchronousCall("subscribe_forum", paramz);

			} catch (Exception ex) {
				Log.w("Discussions", ex.getMessage());
			}

			return result;
		}
		
		protected void onPostExecute(final String result) {
			
			if(activity == null) {
				return;
			}
			
			Toast toast = Toast.makeText(activity, "Forum added to favorites!", Toast.LENGTH_SHORT);
			toast.show();
			
			load_categories();
		}
	}
	
	
	
	
	private class removeFromFavorites extends AsyncTask<String, Void, String> {
		
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
			    //application.getSession().performSynchronousCall("unsubscribe_forum", paramz);
			    application.getSession().performNewSynchronousCall("unsubscribe_forum", paramz);

			} catch (Exception ex) {
				Log.w("Discussions", ex.getMessage());
			}

			return result;
		}
		
		protected void onPostExecute(final String result) {
			
			if(activity == null) {
				return;
			}
			
			Toast toast = Toast.makeText(activity, "Forum removed from favorites!", Toast.LENGTH_SHORT);
			toast.show();
			
			load_categories();
		}
	}
	
	
	
}
