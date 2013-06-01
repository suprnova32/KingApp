package com.insomniware.kingapp;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.google.android.gms.location.Geofence;
import com.insomniware.kingapp.extras.MyConstants;
import com.insomniware.kingapp.fragments.InfoFragment;
import com.insomniware.kingapp.fragments.LocationFragment;
import com.insomniware.kingapp.helpers.SimpleGeofence;
import com.insomniware.kingapp.receivers.PassiveLocationChangedReceiver;
import com.insomniware.kingapp.helpers.GeofenceRemover;
import com.insomniware.kingapp.helpers.GeofenceRequester;
import com.insomniware.kingapp.helpers.GeofenceUtils;
import com.insomniware.kingapp.helpers.GeofenceUtils.*;
import com.insomniware.kingapp.helpers.SimpleGeofenceStore;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class MainPageActivity extends FragmentActivity  {

	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a
	 * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
	 * will keep every loaded fragment in memory. If this becomes too memory
	 * intensive, it may be best to switch to a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */
	SectionsPagerAdapter mSectionsPagerAdapter;
	public static SharedPreferences settings;
	public static SharedPreferences.Editor editor;
	private static boolean KILL = false;
	public static String auth_token;
	public static String email;
	private MenuItem menuItem;
	protected PendingIntent locationListenerPassivePendingIntent;
	Fragment[] fragments;

    // Store the current request
    public static REQUEST_TYPE mRequestType;

    // Store the current type of removal
    public static REMOVE_TYPE mRemoveType;

    // Persistent storage for geofences
    public static SimpleGeofenceStore mPrefs;

    // Store a list of geofences to add
    public static List<Geofence> mCurrentGeofences;

    // Add geofences handler
    public static GeofenceRequester mGeofenceRequester;
    // Remove geofences handler
    public static GeofenceRemover mGeofenceRemover;

    /*
     * An instance of an inner class that receives broadcasts from listeners and from the
     * IntentService that receives geofence transition events
     */
    private GeofenceSampleReceiver mBroadcastReceiver;


    // An intent filter for the broadcast receiver
    private IntentFilter mIntentFilter;

    // Store the list of geofences to remove
    public static List<String> mGeofenceIdsToRemove;



	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager mViewPager;
	
	public static LocationManager locationManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        // Create a new broadcast receiver to receive updates from the listeners and service
        mBroadcastReceiver = new GeofenceSampleReceiver();
        // Create an intent filter for the broadcast receiver
        mIntentFilter = new IntentFilter();
        // Action for broadcast Intents that report successful addition of geofences
        mIntentFilter.addAction(GeofenceUtils.ACTION_GEOFENCES_ADDED);
        // Action for broadcast Intents that report successful removal of geofences
        mIntentFilter.addAction(GeofenceUtils.ACTION_GEOFENCES_REMOVED);
        // Action for broadcast Intents containing various types of geofencing errors
        mIntentFilter.addAction(GeofenceUtils.ACTION_GEOFENCE_ERROR);
        // All Location Services sample apps use this category
        mIntentFilter.addCategory(GeofenceUtils.CATEGORY_LOCATION_SERVICES);
        // Instantiate a new geofence storage area
        mPrefs = new SimpleGeofenceStore(this);
        // Instantiate the current List of geofences
        mCurrentGeofences = new ArrayList<Geofence>();
        // Instantiate a Geofence requester
        mGeofenceRequester = new GeofenceRequester(this);
        // Instantiate a Geofence remover
        mGeofenceRemover = new GeofenceRemover(this);

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		settings = getSharedPreferences(MyConstants.PREFS_NAME, 0);
		editor = settings.edit();
        fragments = new Fragment[]{new InfoFragment(), new LocationFragment()};
		auth_token = settings.getString("token", null);
		email = settings.getString("email", null); 
		loginDataCheck(auth_token);
		setContentView(R.layout.activity_main_page);
		
//		Intent passiveIntent = new Intent(this, PassiveLocationChangedReceiver.class);
//	    locationListenerPassivePendingIntent = PendingIntent.getBroadcast(this, 0, passiveIntent, PendingIntent.FLAG_UPDATE_CURRENT);
//	    locationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, MyConstants.MIN_TIME, MyConstants.MIN_DISTANCE, locationListenerPassivePendingIntent);


		// Create the adapter that will return a fragment for each of the three
		// primary sections of the app.
		mSectionsPagerAdapter = new SectionsPagerAdapter(
				getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);
		//Toast.makeText(this, auth_token, Toast.LENGTH_LONG).show();
		if(LocationFragment.mMap == null){
			FragmentManager manager = getSupportFragmentManager();
            ((LocationFragment)fragments[1]).forceMapReload(manager);
		}
	}

    /*
     * Handle results returned to this Activity by other Activities started with
     * startActivityForResult(). In particular, the method onConnectionFailed() in
     * GeofenceRemover and GeofenceRequester may call startResolutionForResult() to
     * start an Activity that handles Google Play services problems. The result of this
     * call returns here, to onActivityResult.
     * calls
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        // Choose what to do based on the request code
        switch (requestCode) {

            // If the request code matches the code sent in onConnectionFailed
            case GeofenceUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST :

                switch (resultCode) {
                    // If Google Play services resolved the problem
                    case Activity.RESULT_OK:

                        // If the request was to add geofences
                        if (GeofenceUtils.REQUEST_TYPE.ADD == mRequestType) {

                            // Toggle the request flag and send a new request
                            mGeofenceRequester.setInProgressFlag(false);

                            // Restart the process of adding the current geofences
                            mGeofenceRequester.addGeofences(mCurrentGeofences);

                            // If the request was to remove geofences
                        } else if (GeofenceUtils.REQUEST_TYPE.REMOVE == mRequestType ){

                            // Toggle the removal flag and send a new removal request
                            mGeofenceRemover.setInProgressFlag(false);

                            // If the removal was by Intent
                            if (GeofenceUtils.REMOVE_TYPE.INTENT == mRemoveType) {

                                // Restart the removal of all geofences for the PendingIntent
                                mGeofenceRemover.removeGeofencesByIntent(
                                        mGeofenceRequester.getRequestPendingIntent());

                                // If the removal was by a List of geofence IDs
                            } else {

                                // Restart the removal of the geofence list
                                mGeofenceRemover.removeGeofencesById(mGeofenceIdsToRemove);
                            }
                        }
                        break;

                    // If any other result was returned by Google Play services
                    default:

                        // Report that Google Play services was unable to resolve the problem.
                        Log.d(GeofenceUtils.APPTAG, getString(R.string.no_resolution));
                }

                // If any other request code was received
            default:
                // Report that this Activity received an unknown requestCode
                Log.d(GeofenceUtils.APPTAG,
                        getString(R.string.unknown_activity_request_code, requestCode));

                break;
        }
    }
	
	@Override
	protected void onPause() {
		super.onPause();
	}
	
	@Override
	protected void onSaveInstanceState (Bundle outState) {
				
	}
	
	@Override
	protected void onResume() {
		super.onResume();
        // Register the broadcast receiver to receive status updates
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver, mIntentFilter);
        //startPeriodicUpdates();
	}
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main_page, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch(item.getItemId()) {
	    case R.id.action_settings:
	        Intent intent = new Intent(this, SettingsActivity.class);
	        startActivity(intent);
	        break;
	    case R.id.log_out:
	        editor.remove("token");
	        editor.commit();
	        KILL = true;
	        locationManager.removeUpdates(locationListenerPassivePendingIntent);
	    	finish();
	        break;
	    case R.id.action_refresh:
	    	menuItem = item;
	        menuItem.setActionView(R.layout.reload_progress);
	        menuItem.expandActionView();
	        try{
	        	((InfoFragment) fragments[0]).fetchUserInformation();
                ((LocationFragment)fragments[1]).forceMapReload(getSupportFragmentManager());
	        } catch (Exception e){
	        	showError();	        	
	        }
	        ReloadTask task = new ReloadTask();
	        task.execute((Void) null);
	        break;
	    default:
	        return super.onOptionsItemSelected(item);
	    }

	    return true;
	}
	
	@Override
    protected void onStop() {
       super.onStop();
        //stopPeriodicUpdates();
       
       if (KILL == false) {
    	  // We need an Editor object to make preference changes.
	      // All objects are from android.context.Context
	      editor.putString("token", auth_token);

	      // Commit the edits!
	      editor.commit();    	   
       }
       
    }
	
	private void showError(){
		Toast.makeText(this, "There was an error, please try again", Toast.LENGTH_LONG).show();
	}
	
	private boolean loginDataCheck(String token){
		if((token == null)) {
        	Intent i = new Intent(this, LoginActivity.class);
            startActivity(i);
        }
        return false;		
	}

	/**
	 * Represents an asynchronous reload task used refresh
	 * the user's info.
	 */
	public class ReloadTask extends AsyncTask<Void, Void, Boolean> {
		
		@Override
		protected Boolean doInBackground(Void... params) {
			
			try {
	    		
	    	} catch (Exception e) {
	    		e.printStackTrace();
	    		return false;	    			    		
	    	}			
			return true;
		}

		@Override
		protected void onPostExecute(final Boolean status) {
			if(!status)
				showError();
			menuItem.collapseActionView();
			menuItem.setActionView(null);			
		}

		@Override
		protected void onCancelled() {
			menuItem.collapseActionView();
			menuItem.setActionView(null);
			
		}
	}

	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {

			return fragments[position];
		}

		@Override
		public int getCount() {
			return fragments.length;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			Locale l = Locale.getDefault();
			switch (position) {
			case 0:
				return getString(R.string.user_info).toUpperCase(l);
			case 1:
				return getString(R.string.hidden_stuff).toUpperCase(l);
			}
			return null;
		}
	}

    /**
     * Define a Broadcast receiver that receives updates from connection listeners and
     * the geofence transition service.
     */
    public class GeofenceSampleReceiver extends BroadcastReceiver {
        /*
         * Define the required method for broadcast receivers
         * This method is invoked when a broadcast Intent triggers the receiver
         */
        @Override
        public void onReceive(Context context, Intent intent) {

            // Check the action code and determine what to do
            String action = intent.getAction();

            // Intent contains information about errors in adding or removing geofences
            if (TextUtils.equals(action, GeofenceUtils.ACTION_GEOFENCE_ERROR)) {

                handleGeofenceError(context, intent);

                // Intent contains information about successful addition or removal of geofences
            } else if (
                    TextUtils.equals(action, GeofenceUtils.ACTION_GEOFENCES_ADDED)
                            ||
                            TextUtils.equals(action, GeofenceUtils.ACTION_GEOFENCES_REMOVED)) {

                handleGeofenceStatus(context, intent);

                // Intent contains information about a geofence transition
            } else if (TextUtils.equals(action, GeofenceUtils.ACTION_GEOFENCE_TRANSITION)) {

                handleGeofenceTransition(context, intent);

                // The Intent contained an invalid action
            } else {
                Log.e(GeofenceUtils.APPTAG, getString(R.string.invalid_action_detail, action));
                Toast.makeText(context, R.string.invalid_action, Toast.LENGTH_LONG).show();
            }
        }

        /**
         * If you want to display a UI message about adding or removing geofences, put it here.
         *
         * @param context A Context for this component
         * @param intent The received broadcast Intent
         */
        private void handleGeofenceStatus(Context context, Intent intent) {

        }

        /**
         * Report geofence transitions to the UI
         *
         * @param context A Context for this component
         * @param intent The Intent containing the transition
         */
        private void handleGeofenceTransition(Context context, Intent intent) {
            /*
             * If you want to change the UI when a transition occurs, put the code
             * here. The current design of the app uses a notification to inform the
             * user that a transition has occurred.
             */
        }

        /**
         * Report addition or removal errors to the UI, using a Toast
         *
         * @param intent A broadcast Intent sent by ReceiveTransitionsIntentService
         */
        private void handleGeofenceError(Context context, Intent intent) {
            String msg = intent.getStringExtra(GeofenceUtils.EXTRA_GEOFENCE_STATUS);
            Log.e(GeofenceUtils.APPTAG, msg);
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
        }
    }

}
