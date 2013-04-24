package com.insomniware.kingapp;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class InfoFragment extends Fragment {
	
	private static UserInfoTask mInfoTask = null;
	private static View mInfoView;
	private static View mInfoStatusView;
	private static TextView mUserName;
	private static TextView mEmail;
	
	public InfoFragment(){
		
	}
	
	@Override  
    public void onCreate(Bundle savedInstanceState) {  
        super.onCreate(savedInstanceState);
        
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
		
		Button qr_scan = (Button) wrapper.findViewById(R.id.check_in_button);
		qr_scan.setOnClickListener(new OnClickListener() {
        	@Override
			public void onClick(View view) {
        		Intent i = new Intent(getActivity(), CheckInActivity.class);
                startActivity(i);
//        		IntentIntegrator integrator = new IntentIntegrator(getActivity());
//        		integrator.initiateScan();
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
		
		@Override
		protected Boolean doInBackground(Void... params) {
			
			JSONObject jsonobj = new JSONObject();
			
			try {
				jsonobj.put("email", MainPageActivity.email);
				jsonobj.put("auth_token", MainPageActivity.auth_token);
				ConnectionHelper conn = new ConnectionHelper("user_info", jsonobj);
				JSONObject recvdjson = conn.performRequest();
				if (recvdjson.has("user")){
					user = recvdjson.getJSONObject("user");
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
