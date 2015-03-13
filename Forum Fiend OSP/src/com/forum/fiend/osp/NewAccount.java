package com.forum.fiend.osp;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Vector;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;

@SuppressLint("NewApi")
public class NewAccount extends FragmentActivity {
	private EditText tvUsername;
	private EditText tvEmail;
	private EditText etPassword1;
	private String server_address;
	
	private ForumFiendApp application;
	
	//private CheckBox cbAgeCheck;
	private Button btnCreate;
	
	private AnalyticsHelper ah;
	
	public void onCreate(Bundle savedInstanceState) {
		
		application = (ForumFiendApp)getApplication();
		
		String accent = application.getSession().getServer().serverColor;

		ThemeSetter.setTheme(this,accent);

        super.onCreate(savedInstanceState);
        
        ThemeSetter.setActionBar(this,accent);
		
		
		
		//Track app analytics
		ah = application.getAnalyticsHelper();
		ah.trackScreen(getClass().getName(), false);
 
		server_address = application.getSession().getServer().serverAddress;


        setTitle("New Account");

        setContentView(R.layout.new_account);
        
        link_layouts();
	}
	
	@Override
    public void onStart() {
      super.onStart();
    }

    @Override
    public void onStop() {
      super.onStop();
    }
	
	private void link_layouts() {
		tvUsername = (EditText) findViewById(R.id.newact_username);
		tvEmail = (EditText) findViewById(R.id.newact_email);
		etPassword1 = (EditText) findViewById(R.id.newact_password_1);
		
		TextView disclaimer = (TextView) findViewById(R.id.tv_new_account_disclaimer);
		
		if(server_address.contains("alien-forums.com") || server_address.contains("rp-forums.net")) {
			disclaimer.setVisibility(View.GONE);
		} else {
			disclaimer.setText(disclaimer.getText().toString().replace("SERVERNAME", server_address.replace("http://", "")));
		}
		
		
		
		CheckBox cbAge = (CheckBox) findViewById(R.id.cb_new_account_age);
		cbAge.setOnCheckedChangeListener(AgreementChangedListener);

		btnCreate = (Button) findViewById(R.id.newact_create);
		
		btnCreate.setOnClickListener(create_account);
	}
	
	private OnCheckedChangeListener AgreementChangedListener = new OnCheckedChangeListener()
	{
		public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) 
		{
			if(isChecked) {
				btnCreate.setEnabled(true);
			} else {
				btnCreate.setEnabled(false);
			}
		}
		
	};
	
	private OnClickListener create_account = new OnClickListener() {
		public void onClick(View v) {
			
			if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
				new create_account_thread().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
			} else {
				new create_account_thread().execute();
			}
			
		}	
	};
	
	private class create_account_thread extends AsyncTask<String, Void, Object[]> 
    {
		protected void onPreExecute()
		{
			btnCreate.setEnabled(false);
		}
		
		@SuppressWarnings({ "unchecked", "rawtypes" })
		@Override
		protected Object[] doInBackground(String... params)  {
			
			String username = tvUsername.getText().toString().trim();
			String emailaddress = tvEmail.getText().toString().trim();
			String password = etPassword1.getText().toString().trim();

			Object[] result = new Object[50];
			
			try {
			    Vector paramz = new Vector();
			    paramz.addElement(username.getBytes());
			    paramz.addElement(password.getBytes());
			    paramz.addElement(emailaddress.getBytes());
			    
			    result[0] = application.getSession().performNewSynchronousCall("register", paramz);

			} catch(Exception ex) {
				
				//what evs
				
			}
			
			return result;

		}
		
		@SuppressWarnings("rawtypes")
		protected void onPostExecute(final Object[] result) {
			if(result == null) {
				Toast toast = Toast.makeText(NewAccount.this, "There was an error connecting to the server.  Please try again later.", Toast.LENGTH_LONG);
				toast.show();
				btnCreate.setEnabled(true);
				return;
			}
			
			

			if(result[0] != null) {
	            HashMap map = (HashMap) result[0];
	            
	            if(map.containsKey("result")) {
	            	
	            	Boolean loginSuccess = (Boolean) map.get("result");
	            	
	            	if(loginSuccess) {
	            		Toast toast = Toast.makeText(NewAccount.this, "Welcome to the forums, " + tvUsername.getText().toString().trim()  + ".  Please log in to get started!", Toast.LENGTH_LONG);
	        			toast.show();
	        			
	        	        if(getString(R.string.server_location).contentEquals("0")) {
	        		        ah.trackEvent("account creation", "created", server_address, false);
	        	        }
	        			
	        			finish();
	            	} else {
	            		
	            		String regError = new String((byte[]) map.get("result_text"));
	            		
	            		Toast toast = Toast.makeText(NewAccount.this, regError, Toast.LENGTH_LONG);
	        			toast.show();
	        			btnCreate.setEnabled(true);
	            	}
	            	
	            } else {
	            	Toast toast = Toast.makeText(NewAccount.this, "Server communication error!  Please try again later.", Toast.LENGTH_LONG);
					toast.show();
					btnCreate.setEnabled(true);
	            }
	            
			} else {
				Toast toast = Toast.makeText(NewAccount.this, "Connection to the server could not be established :-(  Please try again later.", Toast.LENGTH_LONG);
				toast.show();
				btnCreate.setEnabled(true);
			}
			
			
			
			
	    }
    }
	

	
	public String MD5(String string) {
		
		String md5 = "";
	    try
	    {
	        MessageDigest crypt = MessageDigest.getInstance("MD5");
	        crypt.reset();
	        crypt.update(string.getBytes("UTF-8"));
	        md5 = byteToHex(crypt.digest());
	    }
	    catch(NoSuchAlgorithmException e)
	    {
	        e.printStackTrace();
	    }
	    catch(UnsupportedEncodingException e)
	    {
	        e.printStackTrace();
	    }
	    return md5;
	}
	
	public String SHA1(String string) {
		
		String sha1 = "";
	    try
	    {
	        MessageDigest crypt = MessageDigest.getInstance("SHA-1");
	        crypt.reset();
	        crypt.update(string.getBytes("UTF-8"));
	        sha1 = byteToHex(crypt.digest());
	    }
	    catch(NoSuchAlgorithmException e)
	    {
	        e.printStackTrace();
	    }
	    catch(UnsupportedEncodingException e)
	    {
	        e.printStackTrace();
	    }
	    return sha1;
	}
	
	private static String byteToHex(final byte[] hash)
	{
		String returnValue = "";
	    Formatter formatter = new Formatter();
	    for (byte b : hash)
	    {
	        formatter.format("%02x", b);
	    }
	    returnValue = formatter.toString();
	    formatter.close();
	    
	    return returnValue;
	}
	
}
