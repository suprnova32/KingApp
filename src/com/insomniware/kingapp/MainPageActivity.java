package com.insomniware.kingapp;

import java.util.Locale;

import com.insomniware.kingapp.extras.MyConstants;
import com.insomniware.kingapp.fragments.InfoFragment;
import com.insomniware.kingapp.fragments.LocationFragment;
import com.insomniware.kingapp.receivers.PassiveLocationChangedReceiver;
import com.insomniware.kingapp.receivers.ProximityIntentReceiver;

import android.app.PendingIntent;
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
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class MainPageActivity extends FragmentActivity {

	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a
	 * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
	 * will keep every loaded fragment in memory. If this becomes too memory
	 * intensive, it may be best to switch to a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */
	SectionsPagerAdapter mSectionsPagerAdapter;
	SharedPreferences settings;
	SharedPreferences.Editor editor;
	private static boolean KILL = false;
	public static String auth_token;
	public static String email;
	private MenuItem menuItem;
	protected PendingIntent locationListenerPassivePendingIntent;
	Fragment[] fragments;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager mViewPager;
	
	public static LocationManager locationManager;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		fragments = new Fragment[]{new InfoFragment(), new LocationFragment()};
		settings = getSharedPreferences(MyConstants.PREFS_NAME, 0);
		editor = settings.edit();
		
		auth_token = settings.getString("token", null);
		email = settings.getString("email", null); 
		loginDataCheck(auth_token);
		setContentView(R.layout.activity_main_page);
		
		Intent passiveIntent = new Intent(this, PassiveLocationChangedReceiver.class);
	    locationListenerPassivePendingIntent = PendingIntent.getBroadcast(this, 0, passiveIntent, PendingIntent.FLAG_UPDATE_CURRENT);
	    locationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, MyConstants.MAX_TIME, MyConstants.MAX_DISTANCE, locationListenerPassivePendingIntent); 


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
			new LocationFragment().forceMapReload(manager);
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
	        	new LocationFragment().forceMapReload(getSupportFragmentManager());
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
			//Bundle args = new Bundle();
			//args.putInt(DummySectionFragment.ARG_SECTION_NUMBER, position + 1);
			//fragment.setArguments(args);
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

}
