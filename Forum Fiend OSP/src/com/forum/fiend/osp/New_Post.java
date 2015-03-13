package com.forum.fiend.osp;

import java.util.List;
import java.util.Vector;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;

import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import android.widget.LinearLayout;

import android.widget.Toast;

@SuppressLint("NewApi")
public class New_Post extends FragmentActivity {
	//1: New Thread, 2: Reply, 3: Edit Post, 4: Message, 6: Tagline, 7: Instapost
	public int post_type = 1;
	//private String server_address;
	public String parent = "0";
	public String category = "0";
	public String original_text = "";
	public String subforum = "0";
	public String picture = "0";

	private String postId = "0";


	private String theSubject = "0";

	public EditText subject_inputter;
	public EditText body_inputter;
	private Button submitter;
	private Button picture_attacher;

	private String tagline;
	
	private ForumFiendApp application;
	
	private boolean colorPickerOpen = false;
	
	private boolean postSubmitted = false;
	
	private SQLiteDatabase notetasticDB;
	private String sql;
	private Session mailSession;
	
	private AnalyticsHelper ah;
	
	public void onCreate(Bundle savedInstanceState) {
		
		application = (ForumFiendApp)getApplication();

        Bundle bundle = getIntent().getExtras();
        subforum = bundle.getString("subforum_id");
        post_type = bundle.getInt("post_type");
        parent = bundle.getString("parent");
        category = bundle.getString("category");
        original_text = bundle.getString("original_text");
        picture = bundle.getString("picture");
        postId = bundle.getString("postid");
        String boxTitle = bundle.getString("boxTitle");
        theSubject = bundle.getString("subject");
        
        if(post_type == 4 && theSubject.length() > 0) {
        	theSubject = "Re: " + theSubject;
        }

        if(bundle.containsKey("server")) {
 
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
    				return;
    			}

    			@Override
    			public void onSessionConnectionFailed(String reason) {
    				return;
    			}
    			
    		});
        	mailSession.setServer(server);
        	
        	
        } else {
        	mailSession = application.getSession();;
        }
        
        tagline = mailSession.getServer().serverTagline;
        
        String accent = mailSession.getServer().serverColor;
        
        
        ThemeSetter.setTheme(this,accent);

        super.onCreate(savedInstanceState);
        
        ThemeSetter.setActionBar(this,accent);
        
        //Track app analytics
        ah = ((ForumFiendApp)getApplication()).getAnalyticsHelper();
        ah.trackScreen(getClass().getName(), false);
		
		this.setResult(0);
		
        setContentView(R.layout.new_post);
        
        setTitle(boxTitle);
        
        subject_inputter = (EditText) findViewById(R.id.new_post_subject);
        body_inputter = (EditText) findViewById(R.id.new_post_body);
        submitter = (Button) findViewById(R.id.new_post_submit);
        picture_attacher = (Button) findViewById(R.id.new_post_picture);
        
        body_inputter.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if(hasFocus) {
					postSubmitted = false;
				}
			}
		});
        
        if(post_type == 5)
        	picture_attacher.setVisibility(View.GONE);
        

        Button bold = (Button) findViewById(R.id.new_post_bold);
        Button itialic = (Button) findViewById(R.id.new_post_italic);
        Button underline = (Button) findViewById(R.id.new_post_underline);
        Button picker = (Button) findViewById(R.id.new_post_color);
        
        picker.setTextColor(Color.parseColor(accent));
        
        submitter.setOnClickListener(launch_submit);
        picture_attacher.setOnClickListener(submission_otions);
        bold.setOnClickListener(set_bold);
        itialic.setOnClickListener(set_italic);
        underline.setOnClickListener(set_underline);
        picker.setOnClickListener(set_color);
        
        if(post_type != 1 && post_type != 4)
        {
        	subject_inputter.setVisibility(View.GONE);
        	subject_inputter.setText(theSubject);
        }
        
        if(post_type == 4) {
        	subject_inputter.setText(theSubject);
        	body_inputter.setSelection(0);
        }
        
		original_text = original_text.replace("</blockquote>", "[/quote]").replace("<blockquote>", "[quote]").replace("<u>", "[u]").replace("</u>", "[/u]").replace("<i>", "[i]").replace("</i>", "[/i]").replace("<b>", "[b]").replace("</b>", "[/b]").replace("&lt;", "<").replace("&gt;", ">").replace("&quot;", "\"").replace("&amp;", "&").replace("<br />", "\n");
		original_text = original_text.replaceAll("\\<font color=\"([^<]*)\"\\>([^<]*)\\</font\\>", "[color=$1]$2[/color]");
		
		body_inputter.setText(original_text);
		
		if(post_type == 6) {
			body_inputter.setText(tagline);
		}
		
		if(post_type == 7) {
			post_type = 2;
			if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
				new data_poster().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
			} else {
				new data_poster().execute();
			}
			return;
		}
		
		if(post_type == 2 && original_text.length() > 0) {
			body_inputter.setSelection(original_text.length() - 1);
		}
        
	}
	
	@Override
    public void onStart() {
      super.onStart();

    }

    @Override
    public void onStop() {
      super.onStop();

    } 
    
    @Override
    public void onPause() {
    	
    	SharedPreferences app_preferences = getSharedPreferences("prefs", 0);
		Editor editor = app_preferences.edit();
		
		String postContent = "0";
		String postSubject = "0";
    	
    	if(!postSubmitted && post_type != 6) {
    		postContent = body_inputter.getText().toString().trim();
    		postSubject = subject_inputter.getText().toString().trim();
    		
    		if(postContent.length() > 0) {
    			Toast toast = Toast.makeText(New_Post.this, "Draft Saved", Toast.LENGTH_SHORT);
    			toast.show();
    		} else {
    			postContent = "0";
    			postSubject = "0";
    		}
    	}
    	
    	editor.putString(mailSession.getServer().serverAddress + "_" + subforum + "_" + post_type + "_" + parent + "_" + category + "_" + postId + "_draft_subject", postSubject);
    	editor.putString(mailSession.getServer().serverAddress + "_" + subforum + "_" + post_type + "_" + parent + "_" + category + "_" + postId + "_draft", postContent);
    	editor.commit();
    	
    	super.onPause();
    }
    
    @Override
    public void onResume() {
    	super.onResume();
    	
    	SharedPreferences app_preferences = getSharedPreferences("prefs", 0);
    	String savedDraft = app_preferences.getString(mailSession.getServer().serverAddress + "_" + subforum + "_" + post_type + "_" + parent + "_" + category + "_" + postId + "_draft", "0");
    	String savedSubject = app_preferences.getString(mailSession.getServer().serverAddress + "_" + subforum + "_" + post_type + "_" + parent + "_" + category + "_" + postId + "_draft_subject", "0");
    	
    	//Restore draft
    	if(!savedDraft.contentEquals("0")) {
    		body_inputter.setText(savedDraft);
    		subject_inputter.setText(savedSubject);
    	}
    }
    
    private View.OnClickListener set_color = new View.OnClickListener() {
		public void onClick(View v) {
			
			colorSelectionStart = body_inputter.getSelectionStart();
			colorSelectionEnd = body_inputter.getSelectionEnd();
			
			colorPickerOpen = true;
			
			InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(body_inputter.getWindowToken(), 0);

			ColorPickerDialogFragment newFragment = ColorPickerDialogFragment.newInstance();
			newFragment.setOnColorSelectedListener(new ColorPickerDialogFragment.onColorSelectedListener() {
				
				public void onColorSelected(String color) {
					setColor(color);
				}
			});
		    newFragment.show(getSupportFragmentManager(), "dialog");
		}
	};
	
	private View.OnClickListener set_bold = new View.OnClickListener() {
		public void onClick(View v) {
			
			int startSelection = body_inputter.getSelectionStart();
			int endSelection = body_inputter.getSelectionEnd();

			String selectedText = body_inputter.getText().toString().substring(startSelection, endSelection).trim();
			
			String firstPart = body_inputter.getText().toString().substring(0, startSelection);
			String secondPart = body_inputter.getText().toString().substring(endSelection, body_inputter.getText().toString().length());
			body_inputter.setText(firstPart + "[b]" + selectedText + "[/b]" + secondPart);
			body_inputter.setSelection(endSelection + 3);	
		}
	};
	
	private View.OnClickListener set_italic = new View.OnClickListener() {
		public void onClick(View v) {
			int startSelection = body_inputter.getSelectionStart();
			int endSelection = body_inputter.getSelectionEnd();

			String selectedText = body_inputter.getText().toString().substring(startSelection, endSelection);
			
			String firstPart = body_inputter.getText().toString().substring(0, startSelection);
			String secondPart = body_inputter.getText().toString().substring(endSelection, body_inputter.getText().toString().length());
			body_inputter.setText(firstPart + "[i]" + selectedText + "[/i]" + secondPart);
			body_inputter.setSelection(endSelection + 3);	
		}
	};
	
	private View.OnClickListener set_underline = new View.OnClickListener() {
		public void onClick(View v) {
			int startSelection = body_inputter.getSelectionStart();
			int endSelection = body_inputter.getSelectionEnd();

			String selectedText = body_inputter.getText().toString().substring(startSelection, endSelection).trim();
			
			String firstPart = body_inputter.getText().toString().substring(0, startSelection);
			String secondPart = body_inputter.getText().toString().substring(endSelection, body_inputter.getText().toString().length());
			body_inputter.setText(firstPart + "[u]" + selectedText + "[/u]" + secondPart);
			body_inputter.setSelection(endSelection + 3);	
		}
	};
	
	private View.OnClickListener launch_submit = new View.OnClickListener()
	{
		public void onClick(View v) 
		{
			submitter.setEnabled(false);
			
			if(post_type == 6) {
				postSubmitted = true;
				String comment = body_inputter.getText().toString();
				mailSession.getServer().serverTagline = comment;
				mailSession.updateServer();
			    finish();
			    return;
			}
			
			Toast toast = Toast.makeText(New_Post.this, "Submitting, please wait!", Toast.LENGTH_SHORT);
			toast.show();
			
			if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
				new data_poster().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
			} else {
				new data_poster().execute();
			}

			
			
		}
	};
	
	private class data_poster extends AsyncTask<String, Void, Object[]> {
		@SuppressWarnings({ "unchecked", "rawtypes" })
		protected Object[] doInBackground(final String... args) {
			String comment;
        	String subject = theSubject;
        	
        	comment = body_inputter.getText().toString();

        	/*
			CookieManager cookiemanager = new CookieManager(); 
		    cookiemanager.setCookiePolicy(CookiePolicy.ACCEPT_ALL); 
		    CookieHandler.setDefault(cookiemanager); 

		    for(HttpCookie c:mailSession.getCookies()) {
		    	try {
		    	URI cookieUri = new URI(c.getDomain());
		    		cookiemanager.getCookieStore().add(cookieUri, c);
		    	} catch(Exception ex) {
		    		//nobody cares
		    	}
		    }
		    */

			Object[] result = new Object[50];
        	
        	if (post_type == 1 || post_type == 4) {
        		subject = subject_inputter.getText().toString();
        	}
        	
        	comment = comment.trim();
        	
        	subject = subject.trim();
        	
        	if (subject.length() > 45)
        		subject = subject.substring(0,44);
        	
        	if (subject.length() < 1)
        		subject = "no subject";
        	
        	if (comment.length() < 1) {
        		
    			return null;
        	}
        	
        	if((post_type == 1 || post_type == 2 | post_type == 4) && tagline.length() > 0) {
        		comment = comment + "\n\n" + tagline;
        	}
        	
        	try {
        		/*
				XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
				config.setUserAgent("ForumFiend");
			    config.setServerURL(new URL(mailSession.getServer().serverAddress + "/mobiquo/mobiquo.php"));
			    XmlRpcClient client = new XmlRpcClient();
			    client.setConfig(config);
			    
			    cookiemanager.getCookieStore();

			    XmlRpcTransportFactory tFactory = new XmlRpcSun15HttpTransportFactory(client); 
			    client.setTransportFactory(tFactory);
			    
			    */
			    
			    if(post_type == 1) {			    
				    Vector paramz = new Vector();
				    paramz.addElement(category);
				    paramz.addElement(subject.getBytes());
				    paramz.addElement(comment.getBytes());
				    result[0] = mailSession.performSynchronousCall("new_topic", paramz);
			    }
			    
			    
			    if(post_type == 2) {			    
				    Vector paramz = new Vector();
				    paramz.addElement(category);
				    paramz.addElement(parent);
				    paramz.addElement(subject.getBytes());
				    paramz.addElement(comment.getBytes());
				    result[0] = mailSession.performSynchronousCall("reply_post", paramz);
			    }
			    
			    if(post_type == 3) {			    
				    Vector paramz = new Vector();
				    paramz.addElement(postId);
				    paramz.addElement(subject.getBytes());
				    paramz.addElement(comment.getBytes());
				    result[0] = mailSession.performSynchronousCall("save_raw_post", paramz);
			    }
			    
			    if(post_type == 4) {	
			    	
			    	byte[][] toname = new byte[1][50];
			    	toname[0] = category.getBytes();
			    	
			    	Log.d("Discussions","Sending message to " + parent);
			    	
				    Vector paramz = new Vector();
				    paramz.addElement(toname);
				    paramz.addElement(subject.getBytes());
				    paramz.addElement(comment.getBytes());
				    result[0] = mailSession.performSynchronousCall("create_message", paramz);
			    }

			    //cookiemanager.getCookieStore();

			}
			catch(Exception e)
			{
				Log.w("Discussions",e.getMessage());
				return null;
			}
        	

        	return result;
        }
		
		//This method is executed after the thread has completed.
        protected void onPostExecute(final Object[] result)  {
        	
        	if(result == null) {
        		submitter.setEnabled(true);
    			
    			Toast toast = Toast.makeText(New_Post.this, "Submission error, please retry :-(", Toast.LENGTH_LONG);
    			toast.show();
    			postSubmitted = false;
    			return;
        	}
        	
        	postSubmitted = true;
        	
        	New_Post.this.setResult(1);
        	finish();
        }

     }
	
	private View.OnClickListener submission_otions = new View.OnClickListener()
    {
		public void onClick(View v) {

			final EditText input = new EditText(New_Post.this);
        	
        	new AlertDialog.Builder(New_Post.this)
        	.setTitle("Insert Image")
            .setMessage("Enter the URL of the image you would like to post.")
            .setView(input)
            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    runOnUiThread(new Runnable() {
		        	    public void run() {
		        	    	body_inputter.setText(body_inputter.getText() + "[img]" + input.getText().toString().trim() + "[/img]");
		        	    }
		        	});
					
					dialog.dismiss();
                }
            }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    // Do nothing.
                }
            }).show();
			
			
		}
    };
    

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if(resultCode != Activity.RESULT_OK) {
			return;
		}
		

    }


	int colorSelectionStart = 0;
	int colorSelectionEnd = 0;
    
	private void setColor(String color) {

    	LinearLayout llPicker = (LinearLayout) findViewById(R.id.profileColorPicker);
    	llPicker.setVisibility(View.GONE);
    	
    	

		String selectedText = body_inputter.getText().toString().substring(colorSelectionStart, colorSelectionEnd).trim();
		
		String firstPart = body_inputter.getText().toString().substring(0, colorSelectionStart);
		String secondPart = body_inputter.getText().toString().substring(colorSelectionEnd, body_inputter.getText().toString().length());
		body_inputter.setText(firstPart + "[color=" + color + "]" + selectedText + "[/color]" + secondPart);
		body_inputter.setSelection(colorSelectionEnd + 15);
		
		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.showSoftInput(body_inputter, 0);
		
		colorPickerOpen = false;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    if ((keyCode == KeyEvent.KEYCODE_BACK)) {
	    	if(colorPickerOpen) {
	    		
	    		LinearLayout llPicker = (LinearLayout) findViewById(R.id.profileColorPicker);
	        	llPicker.setVisibility(View.GONE);
	    		
	    		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
	    		imm.showSoftInput(body_inputter, 0);
	    		
	    		colorPickerOpen = false;
	    		
	    		return true;
	    	}
	    }
	    
	    return super.onKeyDown(keyCode, event);
	}
	
	protected boolean canHandleCameraIntent() {
	  final Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
	  final List<ResolveInfo> results = getPackageManager().queryIntentActivities(intent, 0);
	  return (results.size() > 0);
	}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.post_editor_menu, menu);

        
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menu_editor_preview:
            	showPreview();
                return true;
            case R.id.menu_editor_select_all:
            	body_inputter.selectAll();
                return true;
            case R.id.menu_editor_clear_all:
            	body_inputter.setText("");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
    private void showPreview() {
    	String previewText = body_inputter.getText().toString().trim().replace("\n", "<br />");
    	
    	Bundle bundle = new Bundle();
    	bundle.putString("text", previewText);
    	
    	PreviewDialogFragment newFragment = PreviewDialogFragment.newInstance();
    	newFragment.setArguments(bundle);
	    newFragment.show(getSupportFragmentManager(), "preview");
	    
    }
}
