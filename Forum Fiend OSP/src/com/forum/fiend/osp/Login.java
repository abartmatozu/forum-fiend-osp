package com.forum.fiend.osp;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class Login extends Fragment
{
	private TextView tvUsername;
	private TextView tvPassword;
	private CheckBox cbAgreement;
	private CheckBox cbAge;
	private Button btnLogin;
	private Button btnNewAccount;

	private ForumFiendApp application;
	private String server_address;
	
	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		
		application = (ForumFiendApp)getActivity().getApplication();

	}
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.login, container, false);
        return v;
    }

	@Override
	public void onStart() {
		super.onStart();

		server_address = application.getSession().getServer().serverAddress;
		
        link_layouts();
	}
	
	@Override
	public void onResume() {

        String userid = application.getSession().getServer().serverUserId;

        if(!userid.contentEquals("0")) {
			getActivity().finish();
	    	getActivity().startActivity(getActivity().getIntent());
        }
		
		super.onResume();
	}
	
	private void link_layouts() {
		tvUsername = (TextView) getActivity().findViewById(R.id.login_username);
		tvPassword = (TextView) getActivity().findViewById(R.id.login_password);
		cbAgreement = (CheckBox) getActivity().findViewById(R.id.login_agreement);
		cbAge = (CheckBox) getActivity().findViewById(R.id.login_age);
		btnLogin = (Button) getActivity().findViewById(R.id.login_login);
		btnNewAccount = (Button) getActivity().findViewById(R.id.login_new_account);
		
		TextView disclaimer = (TextView) getActivity().findViewById(R.id.tv_login_disclaimer);
		
		if(server_address.contains("alien-forums.com") || server_address.contains("rp-forums.net")) {
			disclaimer.setVisibility(View.GONE);
		}
		
		cbAgreement.setText(cbAgreement.getText().toString().replace("SERVERNAME", server_address.replace("http://", "")));
		
		btnLogin.setEnabled(false);
		
		cbAgreement.setOnCheckedChangeListener(AgreementChangedListener);
		cbAge.setOnCheckedChangeListener(AgreementChangedListener);
		
		btnNewAccount.setOnClickListener(CreateAccount);
		btnLogin.setOnClickListener(LoginListener);
	}
	
	
	private OnCheckedChangeListener AgreementChangedListener = new OnCheckedChangeListener()
	{
		public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) 
		{
			if(cbAgreement.isChecked() && cbAge.isChecked()) {
				btnLogin.setEnabled(true);
			} else {
				btnLogin.setEnabled(false);
			}
		}
		
	};
	
	private OnClickListener CreateAccount = new OnClickListener() {
		public void onClick(View v) {
			if(getString(R.string.registration_url).contentEquals("0")) {
				if(application.getSession().getAllowRegistration()) {
					Intent myIntent = new Intent(getActivity(), NewAccount.class);
					Login.this.startActivity(myIntent);
				} else {
					AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
					builder.setTitle("Account Registration");
		            builder.setMessage("Account registeration for this forum must be done on the forum website.  Hit Ok to go to the website now.");
		            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(application.getSession().getServer().serverAddress));
							startActivity(browserIntent);
							
							dialog.dismiss();
						}
					});
		            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// whatever
							
							dialog.dismiss();
						}
					});
		            builder.create().show();
				}
			} else {
				Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.registration_url)));
				startActivity(browserIntent);
			}
		}
	};
	
	private OnClickListener LoginListener = new OnClickListener() {
		public void onClick(View v) {
			btnLogin.setEnabled(false);
			
			String username = tvUsername.getText().toString().trim();
			String password = tvPassword.getText().toString().trim();
			
			application.getSession().setSessionListener(new Session.SessionListener() {

				@Override
				public void onSessionConnected() {
					
					if(getString(R.string.server_location).contentEquals("0")) {
						application.sendLoginStat(application.getSession().getServer().serverAddress);
					}
					
					getActivity().finish();
					application.getSession().getServer().serverTab = "0";
					
	    	    	getActivity().startActivity(getActivity().getIntent());
				}

				@Override
				public void onSessionConnectionFailed(String reason) {
					if(reason != null) {
						Toast toast = Toast.makeText(getActivity(), reason, Toast.LENGTH_LONG);
						toast.show();
						btnLogin.setEnabled(true);
					}
				}
				
			});
			
			application.getSession().loginSession(username, password);
		}
	};
	
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
