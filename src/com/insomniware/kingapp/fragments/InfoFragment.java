package com.insomniware.kingapp.fragments;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.insomniware.kingapp.CheckInActivity;
import com.insomniware.kingapp.MainPageActivity;
import com.insomniware.kingapp.R;
import com.insomniware.kingapp.helpers.ConnectionHelper;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class InfoFragment extends Fragment {
	
	private ArrayList<HashMap<String, String>> mList;
	private ListView mListView;
	
	private static UserInfoTask mInfoTask = null;
	private static View mInfoView;
	private static View mInfoStatusView;
	private static TextView mUserName;
	private static TextView mEmail;
	
	public InfoFragment(){
		
	}
	
	private void setupLocationList(JSONArray rooms, JSONArray locations) throws JSONException {
		mList = new ArrayList<HashMap<String, String>>();
		for(int i=0;i<rooms.length();i++){						
			HashMap<String, String> map = new HashMap<String, String>();	
			JSONObject e = rooms.getJSONObject(i);
			boolean claimed = e.getBoolean("claimed");
			String claimed_info;
			if (claimed) {
				claimed_info = "Room claimed by: " + e.getString("claimed_by");
			} else {
				claimed_info = "Unclaimed! Tell your friends to check in now!";				
			}			
			
			map.put("id",  e.getString("id_hash"));
			map.put("type", "2");
        	map.put("name", "Room: " + e.getString("building_id") + "/" + e.getString("number"));
        	map.put("info", claimed_info);
        	mList.add(map);			
		}
		
		for(int i=0; i<locations.length(); i++) {
			HashMap<String, String> map = new HashMap<String, String>();	
			JSONObject e = locations.getJSONObject(i);
			
			map.put("id", e.getString("id"));
			map.put("type", "3");
			map.put("name", "Location name: " + e.getString("name"));
			map.put("info", "Points scored: "); // + e.getString("score"));
			mList.add(map);
			
		}
		
		SimpleAdapter sa = new SimpleAdapter(
    	        getActivity(),
    	        mList,
    	        R.layout.location_details,
    	        new String[] { "name", "info"},
    	        new int[] { R.id.locationName, R.id.locationDetails});
		mListView.setAdapter(sa);
		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Intent room_info = new Intent(view.getContext(), CheckInActivity.class);
				Bundle b = new Bundle();
				b.putInt("selected", Integer.parseInt(mList.get(position).get("type")));
				b.putString("id", mList.get(position).get("id"));
				b.putString("room_name", mList.get(position).get("name"));
				room_info.putExtras(b);
				startActivity(room_info);				
			}
			
		});
	}
	
	@Override  
    public void onCreate(Bundle savedInstanceState) {  
        super.onCreate(savedInstanceState);
        
    }
	
	@Override
	public void onResume(){
		super.onResume();
		if (MainPageActivity.auth_token != null)
			fetchUserInformation();
		
	}
	
	public void showAlert(){
		CharSequence[] options = {"QR Code (for rooms)", "GPS (for hidden locations)"};
		final Bundle b = new Bundle();
		
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(R.string.check_in_title)
	       .setSingleChoiceItems(options, 0, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				b.putInt("selected", which);				
			}
		})
	       .setPositiveButton("Continue", new DialogInterface.OnClickListener() {
	           @Override
			public void onClick(DialogInterface dialog, int id) {
	        	   Intent check_in = new Intent(getActivity(), CheckInActivity.class);
	        	   check_in.putExtras(b);
	        	   startActivity(check_in);
	           }
	       })
	       .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
	           @Override
			public void onClick(DialogInterface dialog, int id) {
	                dialog.cancel();
	           }
	       });
		AlertDialog alert = builder.create();
		alert.show();
    }
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		LinearLayout wrapper = new LinearLayout(getActivity());
		//View rootView = inflater.inflate(R.layout.fragment_info_layout, container, false);
		inflater.inflate(R.layout.fragment_info_layout, wrapper, true);
		
		mInfoView = wrapper.findViewById(R.id.info_view);
		mInfoStatusView = wrapper.findViewById(R.id.info_status);
		mUserName = (TextView) wrapper.findViewById(R.id.user_name);
		mEmail = (TextView) wrapper.findViewById(R.id.your_email);
		mListView = (ListView) wrapper.findViewById(R.id.locations_list);
		
		if (MainPageActivity.auth_token != null)
			fetchUserInformation();
		
		Button check_in = (Button) wrapper.findViewById(R.id.check_in_button);
		check_in.setOnClickListener(new OnClickListener() {
        	@Override
			public void onClick(View view) {
        		showAlert();
            }
        });
		
		return wrapper; //rootView;
	}
	
	public void fetchUserInformation() {
		if (mInfoTask != null) {
			return;
		}
		showProgress(true);
		mInfoTask = new UserInfoTask();
		mInfoTask.execute((Void) null);
	}
	
	/**
	 * Shows the progress UI and hides the login form.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	private void showProgress(final boolean show) {
		// On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
		// for very easy animations. If available, use these APIs to fade-in
		// the progress spinner.
//		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
//			int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
//
//			mInfoStatusView.setVisibility(View.VISIBLE);
//			mInfoStatusView.animate().setDuration(shortAnimTime)
//					.alpha(show ? 1 : 0)
//					.setListener(new AnimatorListenerAdapter() {
//						@Override
//						public void onAnimationEnd(Animator animation) {
//							mInfoStatusView.setVisibility(show ? View.VISIBLE
//									: View.GONE);
//						}
//					});
//
//			mInfoView.setVisibility(View.VISIBLE);
//			mInfoView.animate().setDuration(shortAnimTime)
//					.alpha(show ? 0 : 1)
//					.setListener(new AnimatorListenerAdapter() {
//						@Override
//						public void onAnimationEnd(Animator animation) {
//							mInfoView.setVisibility(show ? View.GONE
//									: View.VISIBLE);
//						}
//					});
//		} else {
			// The ViewPropertyAnimator APIs are not available, so simply show
			// and hide the relevant UI components.
			mInfoStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
			mInfoView.setVisibility(show ? View.GONE : View.VISIBLE);
//		}
	}
	
	private void showError(String message){
		Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
	}
	
	/**
	 * Represents an asynchronous login/registration task used to authenticate
	 * the user.
	 */
	public class UserInfoTask extends AsyncTask<Void, Void, Boolean> {
		
		JSONObject user = null;
		JSONArray rooms, locations = null;
		
		@Override
		protected Boolean doInBackground(Void... params) {
			
			JSONObject jsonobj = new JSONObject();
			
			try {
				jsonobj.put("email", MainPageActivity.email);
				ConnectionHelper conn = new ConnectionHelper("user_info", jsonobj);
				JSONObject recvdjson = conn.performRequest(getActivity());
				if (recvdjson.has("user")){
					user = recvdjson.getJSONObject("user");
					locations = recvdjson.getJSONArray("locations");
					rooms = recvdjson.getJSONArray("rooms"); 
					return true;					
				}else if (recvdjson.has("error")) {
					return false;					
				}
				
			} catch (JSONException ex) {
				ex.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
			return false;
		}

		@Override
		protected void onPostExecute(final Boolean status) {
			
			mInfoTask = null;
			if(status){
				try {
					mUserName.setText(user.getString("name"));
					mEmail.setText(user.getString("email"));
					setupLocationList(rooms, locations);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}								
			} else {
				showError("Authentication problem.");
				showError("Please log in again.");
			}
			showProgress(false);
			
			
			
		}

		@Override
		protected void onCancelled() {
			mInfoTask = null;
			showProgress(false);
		}
	}

}
