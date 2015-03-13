package com.forum.fiend.osp;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import android.annotation.SuppressLint;


import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.text.Html;
import android.text.util.Linkify;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;

@SuppressLint("NewApi")
@SuppressWarnings("deprecation")
public class ProfileFragment extends Fragment {
	

	private String category_id;        
	
	private TextView tvCreated;
	private TextView tvPostCount;
	private TextView tvActivity;
	private TextView tvTagline;
	private TextView tvAbout;
	private ImageView ivProfilePic;

	private static final int CAMERA_PIC_REQUEST = 1337;
	private static final int GALLERY_PIC_REQUEST = 1338;
	
	private String userName;
	
	private FragmentActivity activity;
	private ForumFiendApp application;
	
	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		
		activity = (FragmentActivity)getActivity();
		application = (ForumFiendApp)activity.getApplication();
		
		setHasOptionsMenu(true);
	}
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
        View v = inflater.inflate(R.layout.view_edit_profile, container, false);
        return v;
    }

	@Override
	public void onStart() {
		
		super.onStart();
		
		Bundle bundle = getArguments();
        category_id = bundle.getString("userid");
        userName = bundle.getString("username");

		setupElements();
		

		if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
			new DownloadProfile().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		} else {
			new DownloadProfile().execute();
		}
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		activity.getActionBar().setTitle(userName);

	}
	
	private void setupElements() {
		
		tvCreated = (TextView) activity.findViewById(R.id.profileCreated);
		tvPostCount = (TextView) activity.findViewById(R.id.profilePostCount);
		tvActivity = (TextView) activity.findViewById(R.id.profileLastActivity);
		tvTagline = (TextView) activity.findViewById(R.id.profileTagline);
		tvAbout = (TextView) activity.findViewById(R.id.profileAbout);
		ivProfilePic = (ImageView) activity.findViewById(R.id.profilePicture);

        String userid = application.getSession().getServer().serverUserId;
        
        LinearLayout avatarButtons = (LinearLayout) activity.findViewById(R.id.profile_avatar_editor_buttons);
        
        if(category_id == null) {
        	avatarButtons.setVisibility(View.GONE);
        } else {
        	if(!userid.contentEquals(category_id)) {
    			
    			avatarButtons.setVisibility(View.GONE);
            }
        }
		
		Button btnPicFromCamera = (Button) activity.findViewById(R.id.profile_upload_avatar_camera);
		Button btnPicFromGallery = (Button) activity.findViewById(R.id.profile_upload_avatar_gallery);
		
		if(!canHandleCameraIntent()) {
			btnPicFromCamera.setVisibility(View.GONE);
		}
		
		btnPicFromGallery.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent();  
				intent.setType("image/*");  
				intent.setAction(Intent.ACTION_GET_CONTENT);  
				startActivityForResult(Intent.createChooser(intent, "Select Picture"),GALLERY_PIC_REQUEST);
			}
		});
		
		btnPicFromCamera.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent imageIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			    File imagesFolder = new File(Environment.getExternalStorageDirectory(), "temp");
			    imagesFolder.mkdirs(); 
			    File image = new File(imagesFolder, "temp.jpg");
			    Uri uriSavedImage = Uri.fromFile(image);
			    imageIntent.putExtra(MediaStore.EXTRA_OUTPUT, uriSavedImage);
			    startActivityForResult(imageIntent,CAMERA_PIC_REQUEST);
			}
		});
		
	}
	
	private class DownloadProfile extends AsyncTask<String, Void, Object[]> {
		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		protected Object[] doInBackground(String... params) {
			
			if(activity == null) {
				return null;
			}

			Object[] result = new Object[50];

			try
			{

		    	Log.d("Discussions","Viewing profile of " + userName);
		    	
			    Vector paramz = new Vector();
			    paramz.addElement(userName.getBytes());
			    if(category_id != null) {
			    	paramz.addElement(category_id);
			    	Log.i(getString(R.string.app_name),"Loading profile for " + userName + " (" + category_id + ")");
			    } else {
			    	Log.i(getString(R.string.app_name),"Loading profile for " + userName + " (null)");
			    }

			    result[0] = application.getSession().performSynchronousCall("get_user_info", paramz);

			}
			catch(Exception e)
			{
				Log.w(getString(R.string.app_name),e.getMessage());
				return null;
			}
			return result;
		}
		
		@SuppressWarnings("rawtypes")
		protected void onPostExecute(final Object[] result) {
			if(result == null || result[0] == null) {
				Log.e(getString(R.string.app_name),"No response for profile!");
				if(result != null) {
					Log.e(getString(R.string.app_name),Integer.toString(result.length));
				}
				return;
			}
			
			
			HashMap topicMap = (HashMap) result[0];
			
			if(topicMap== null) {
				Log.e(getString(R.string.app_name),"No topicmap!");
				Log.e(getString(R.string.app_name),result[0].toString());
				return;
			} else {
				Log.i(getString(R.string.app_name),result[0].toString());
			}
			
			Date timestamp = null;
			
			if(topicMap.containsKey("reg_time")) {
				timestamp = (Date) topicMap.get("reg_time");
			}
			
			Date lastactive = (Date) topicMap.get("last_activity_time");
			
			if(timestamp != null) {
				tvCreated.setText("Member Since: " + timestamp.toString());
			}
			
			if(topicMap.containsKey("post_count")) {
				tvPostCount.setText("Post Count: " + Integer.toString((Integer) topicMap.get("post_count")));
			} else {
				tvPostCount.setVisibility(View.GONE);
			}
			
			if(lastactive == null) {
				tvActivity.setVisibility(View.GONE);
			} else {
				tvActivity.setText("Last Activity: " + lastactive.toString());
			}
			
			if(topicMap.get("current_activity") != null) {
				tvTagline.setText(new String((byte[]) topicMap.get("current_activity")));
			}

			if(topicMap.containsKey("icon_url")) {
		        if(((String) topicMap.get("icon_url")).contains("http://")){
		        	ImageLoader.getInstance().displayImage(((String) topicMap.get("icon_url")), ivProfilePic);
	
		        }
			}
	        
	        Object[] fieldsMap = (Object[]) topicMap.get("custom_fields_list");
	        
	        String aboutSection = "";
	        
	        if(fieldsMap != null) {	        
		        for(Object t:fieldsMap) {
		        	
		        	HashMap m = (HashMap) t;
	
		        	String tName = new String((byte[]) m.get("name"));
		        	String tValue = new String((byte[]) m.get("value"));
		        	
		        	aboutSection = aboutSection + "<b>" + tName + ":</b> " + tValue + "<br /><br />";
		        }
	
		        tvAbout.setText(Html.fromHtml(aboutSection));
				Linkify.addLinks(tvAbout, Linkify.ALL);
		        }
	    }
    }
	
	@SuppressLint("NewApi")
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		
		inflater.inflate(R.menu.profile_menu, menu);
		
		if(ForegroundColorSetter.getForegroundDark(application.getSession().getServer().serverColor)) {
			MenuItem item = menu.findItem(R.id.profile_menu_message);
			item.setIcon(R.drawable.ic_action_new_email_dark);
		}
		
		
		if(userName == null || userName.contentEquals(application.getSession().getServer().serverUserName)) {
			MenuItem msgitem = menu.findItem(R.id.profile_menu_message);
			msgitem.setVisible(false);
		}
		
	    super.onCreateOptionsMenu(menu, inflater);

	}
	
	@Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        //nothing at this time
	}
	
	@Override
	public boolean onOptionsItemSelected (MenuItem item) {
		switch (item.getItemId()) {
        case R.id.profile_menu_message:
        	sendMessage();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
	}
	
	private void sendMessage() {
		Intent myIntent = new Intent(activity, New_Post.class);
		
		Bundle bundle = new Bundle();
		bundle.putString("postid",(String) "0");
		bundle.putString("parent",(String) "0");
		bundle.putString("category",userName);
		bundle.putString("subforum_id",(String) "0");
		bundle.putString("original_text",(String) "");
		bundle.putString("boxTitle",(String) "Message " + userName);
		bundle.putString("picture",(String) "0");
		bundle.putString("color",(String) getString(R.string.default_color));
		bundle.putString("subject",(String) "");
		bundle.putInt("post_type",(Integer) 4);
		myIntent.putExtras(bundle);

		startActivity(myIntent);
	}
	
	public void finishUpSubmission() {
		application.getSession().setSessionListener(new Session.SessionListener() {
			
			@Override
			public void onSessionConnectionFailed(String reason) {
				if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
					new DownloadProfile().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
				} else {
					new DownloadProfile().execute();
				}
			}
			
			@Override
			public void onSessionConnected() {
				if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
					new DownloadProfile().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
				} else {
					new DownloadProfile().execute();
				}
			}
		});
		application.getSession().refreshLogin();
		
		
	}
	
	private String upload_pic;
	private Bitmap uploadPic;

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		if (resultCode != Activity.RESULT_OK)  {
			return;
	    }
		
		if (requestCode == CAMERA_PIC_REQUEST) 
        {
        	try 
        	{
        		
        		
        		File imagesFolder = new File(Environment.getExternalStorageDirectory(), "temp");
    		    imagesFolder.mkdirs(); 
    		    File image = new File(imagesFolder, "temp.jpg");
        		
    		    upload_pic = image.getPath();
    		    
        	    BitmapFactory.Options options = new BitmapFactory.Options();
        	    options.inJustDecodeBounds = true;
        	    int imageWidth = options.outWidth;

        	    double desiredPicSize = 800;
        	    
        	    options = new BitmapFactory.Options();
        	    
        	    options.inSampleSize = 1;
        	    
        	    if(imageWidth > (desiredPicSize * 2)) {
        	    	options.inSampleSize = 2;
        	    }
        	    
        	    if(imageWidth > (desiredPicSize * 4)) {
        	    	options.inSampleSize = 4;
        	    }
        	    
        	    if(imageWidth > (desiredPicSize * 8)) {
        	    	options.inSampleSize = 8;
        	    }
        	    
        	    Bitmap thumbnail2 = BitmapFactory.decodeFile(upload_pic,options);

        	    thumbnail2 = Bitmap.createScaledBitmap(thumbnail2, 100, 100, false);
        	    //thumbnail2 = Bitmap.createScaledBitmap(thumbnail2, 70, 70, false);
        	    uploadPic = thumbnail2;
        	    
        	    Log.d("Discussions", "Avatar Size: " + uploadPic.getWidth() + "x" + uploadPic.getHeight());
        	    
        	    submitpic();
        	    
        	}
        	catch (Exception e) 
        	{
        		Toast toast = Toast.makeText(activity, "Error loading image!" + e.getMessage(), Toast.LENGTH_LONG);
    			toast.show();
        		return;
        	}
        }
        else
        {
        	if (requestCode == GALLERY_PIC_REQUEST) 
            {
            	try 
            	{
            		Uri currImageURI;
            		currImageURI = data.getData();
            		
            		
            		String [] proj={MediaStore.Images.Media.DATA};  
            	    Cursor cursor = activity.managedQuery( currImageURI,  
            	            proj, // Which columns to return  
            	            null,       // WHERE clause; which rows to return (all rows)  
            	            null,       // WHERE clause selection arguments (none)  
            	            null); // Order-by clause (ascending by name)  
            	    int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);  
            	    cursor.moveToFirst();  
            	  
            	    upload_pic = cursor.getString(column_index); 
            	    
            	    
            	    
            	    BitmapFactory.Options options = new BitmapFactory.Options();
            	    options.inJustDecodeBounds = true;
            	    int imageWidth = options.outWidth;

            	    
            	    double desiredPicSize = 800;

            	    options = new BitmapFactory.Options();
            	    
            	    options.inSampleSize = 1;
            	    
            	    if(imageWidth > (desiredPicSize * 2)) {
            	    	options.inSampleSize = 2;
            	    }
            	    
            	    if(imageWidth > (desiredPicSize * 4)) {
            	    	options.inSampleSize = 4;
            	    }
            	    
            	    if(imageWidth > (desiredPicSize * 8)) {
            	    	options.inSampleSize = 8;
            	    }
            	    
            	    Bitmap thumbnail2 = BitmapFactory.decodeFile(upload_pic,options);

            	    thumbnail2 = Bitmap.createScaledBitmap(thumbnail2, 100, 100, false);
            	    //thumbnail2 = Bitmap.createScaledBitmap(thumbnail2, 70, 70, false);
            	    uploadPic = thumbnail2;
            	    
            	    Log.d("Discussions", "Avatar Size: " + uploadPic.getWidth() + "x" + uploadPic.getHeight());
            	    
            	    submitpic();
            	    
            	}
            	catch (Exception e) 
            	{
            		Toast toast = Toast.makeText(activity, "Can only upload locally stored content!", Toast.LENGTH_LONG);
        			toast.show();
            		return;
            	}
            }
        }

        
        
        super.onActivityResult(requestCode, resultCode, data);
    }
	
	
	private void submitpic() {
		if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
			new upload_image().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		} else {
			new upload_image().execute();
		}
    }
    
  //This background thread class uploads an image to the server.
    private class upload_image extends AsyncTask<String, Void, String> 
    {
    	private Dialog error_box;
    	
        //This method is performed before the thread is executed.
        protected void onPreExecute() 
        {
        	error_box = new Dialog(activity);
    		error_box.setTitle("Uploading...");
    		error_box.show();
    		
    		TextView error_message = new TextView(activity);
    		error_message.setPadding(12, 12, 12, 12);
    		error_message.setText("Uploading photo, please wait...");
    		error_box.setContentView(error_message);
        }

        //This method that is done in the background thread.
        protected String doInBackground(final String... args) {

        	String server_address = application.getSession().getServer().serverAddress;
        	
        	String uploadURL = server_address + "/mobiquo/upload.php";
        	
        	String result = new AvatarUploader().uploadBitmap(activity, uploadURL, uploadPic,application);
        	
        	return result;
        }

        //This method is executed after the thread has completed.
        protected void onPostExecute(final String result) 
        {
        	try
        	{
        	error_box.dismiss();
        	}
        	catch (Exception e)
        	{
        	}
        	
        	if(result == null) {
        		return;
        	}
        	
        	if (result.contentEquals("fail")) {
        		
        		//error

        	} else {

        			finishUpSubmission();

        	}
        	
        	return;
        }
     }
	
    protected boolean canHandleCameraIntent() {
  	  final Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
  	  final List<ResolveInfo> results = activity.getPackageManager().queryIntentActivities(intent, 0);
  	  return (results.size() > 0);
  	}
}
