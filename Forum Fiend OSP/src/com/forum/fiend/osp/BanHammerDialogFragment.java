package com.forum.fiend.osp;

import java.util.Vector;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

public class BanHammerDialogFragment extends DialogFragment {

	private TextView tvIntro;
	private EditText etReason;
	private Button submitButton;
	private ProgressBar banWorking;
	
	private ForumFiendApp application;

	private String banId;
	private String banReason;
	
	static BanHammerDialogFragment newInstance() {
		BanHammerDialogFragment f = new BanHammerDialogFragment();
		
		Bundle args = new Bundle();
		args.putString("username", "cylon");
		f.setArguments(args);
		
		return f;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.setStyle(STYLE_NO_TITLE, getTheme());
	}
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.ban_submission, container, false);
		
		tvIntro = (TextView)v.findViewById(R.id.tvBanIntro);
		etReason = (EditText)v.findViewById(R.id.etBanReason);
		submitButton = (Button)v.findViewById(R.id.btnBanSubmit);
		banWorking = (ProgressBar)v.findViewById(R.id.ban_dialog_working);
		
		banWorking.setVisibility(View.GONE);
		
		Bundle args = getArguments();
		tvIntro.setText("Ban " + args.getString("username") + " for the following reason:");
		
		banId = args.getString("username");
		
		submitButton.setOnClickListener(submitBan);
		
		application = (ForumFiendApp)getActivity().getApplication();

        return v;
    }

	private OnClickListener submitBan = new OnClickListener() {

		@Override
		public void onClick(View v) {
			banReason = etReason.getText().toString().trim();
			submitButton.setEnabled(false);
			banWorking.setVisibility(View.VISIBLE);
			new banSubmitter().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,banId,banReason);
		}
		
	};
	
	private class banSubmitter extends AsyncTask<String, Void, String> {
		
		// parm[0] - (string)topic_id

		@SuppressLint("UseValueOf")
		@SuppressWarnings({ "unchecked", "rawtypes" })
		@Override
		protected String doInBackground(String... params) {
			
			if(getActivity() == null) {
				return null;
			}

			String result = "";

			
			try {

			    Vector paramz;
			    
			    paramz = new Vector();
			    paramz.addElement(params[0].getBytes());
			    paramz.addElement(1);
			    paramz.addElement(params[1].getBytes());
			    
			    application.getSession().performSynchronousCall("m_ban_user", paramz);

			} catch (Exception ex) {
				Log.w("Forum Fiend", ex.getMessage());
			}

			return result;
		}
		
		protected void onPostExecute(final String result) {
			
			if(getActivity() == null) {
				return;
			}
			
			BanHammerDialogFragment.this.dismiss();
		}
	}

}
