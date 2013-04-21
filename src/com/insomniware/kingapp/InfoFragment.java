package com.insomniware.kingapp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.zip.GZIPInputStream;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

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
		if (MainPageActivity.auth_token != null)
			fetchUserInformation();
		
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
	
	private static String convertStreamToString(InputStream is) {
	    String line = "";
	    StringBuilder total = new StringBuilder();
	    BufferedReader rd = new BufferedReader(new InputStreamReader(is));
	    try {
	        while ((line = rd.readLine()) != null) {
	            total.append(line);
	        }
	    } catch (Exception e) {
	    	
	    }
	    return total.toString();
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
	
	/**
	 * Represents an asynchronous login/registration task used to authenticate
	 * the user.
	 */
	public class UserInfoTask extends AsyncTask<Void, Void, Boolean> {
		
		JSONObject user = null;
		
		@Override
		protected Boolean doInBackground(Void... params) {
			
			JSONObject jsonobj = new JSONObject();
			
			DefaultHttpClient httpclient = new DefaultHttpClient();
			HttpPost httppostreq = new HttpPost("http://192.168.1.149:3000/api/v1/user_info");
			StringEntity se;
			
			try {
				jsonobj.put("email", MainPageActivity.email);
				jsonobj.put("auth_token", MainPageActivity.auth_token);
				se = new StringEntity(jsonobj.toString());
				se.setContentType("application/json;charset=UTF-8");
				se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json;charset=UTF-8"));
				httppostreq.setEntity(se);
				HttpResponse httpresponse = httpclient.execute(httppostreq);
				HttpEntity resultentity = httpresponse.getEntity();
				InputStream inputstream = resultentity.getContent();
				Header contentencoding = httpresponse.getFirstHeader("Content-Encoding");
				if(contentencoding != null && contentencoding.getValue().equalsIgnoreCase("gzip")) {
					inputstream = new GZIPInputStream(inputstream);
				}
				String resultstring = convertStreamToString(inputstream);
				Log.e("JSON Received", resultstring);
				inputstream.close();
				JSONObject recvdjson = new JSONObject(resultstring);
				if (recvdjson.has("user")){
					user = recvdjson.getJSONObject("user");
					return true;					
				}
				
			} catch (JSONException ex) {
				ex.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
			return false;
		}

		@Override
		protected void onPostExecute(final Boolean status) {
			
			mInfoTask = null;
			try {
				mUserName.setText(user.getString("name"));
				mEmail.setText(user.getString("email"));
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
			showProgress(false);
			
			if(status){
												
			}
		}

		@Override
		protected void onCancelled() {
			mInfoTask = null;
			showProgress(false);
		}
	}

}
