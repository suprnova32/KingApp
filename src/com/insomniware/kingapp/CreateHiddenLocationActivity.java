package com.insomniware.kingapp;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v4.app.NavUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.insomniware.kingapp.fragments.LocationFragment;
import com.insomniware.kingapp.helpers.ConnectionHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class CreateHiddenLocationActivity extends Activity {

    private View mHiddenFormView;
    private View mHiddenStatusView;
    private TextView mHiddenStatusMessageView;
    private CreateLocationTask mCreateTask = null;
    private double mLatitude;
    private double mLongitude;
    private EditText mPointsView;
    private EditText mHintView;
    private EditText mNameView;
    private String mPoints;
    private String mName;
    private String mHint;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_hidden_location);

        mHiddenFormView = findViewById(R.id.hidden_form);
        mHiddenStatusView = findViewById(R.id.hidden_status);
        mHiddenStatusMessageView = (TextView) findViewById(R.id.hidden_status_message);
        mPointsView = (EditText) findViewById(R.id.h_points);
        mHintView = (EditText) findViewById(R.id.h_hint);
        mNameView = (EditText) findViewById(R.id.h_name);


		// Show the Up button in the action bar.
		setupActionBar();
        TextView coordinates = (TextView) findViewById(R.id.coordinate_show);
        Location latest = LocationFragment.mLocationClient.getLastLocation();
        float accuracy = latest.getAccuracy();
        if (accuracy == 0.0 || accuracy > 13.0) {
            Log.e("Accuracy", String.valueOf(accuracy));
            Toast.makeText(getApplication(), "GPS Coordinates not accurate enough!", Toast.LENGTH_LONG).show();
            Toast.makeText(getApplication(), "Try again later.", Toast.LENGTH_LONG).show();
            finish();
        }
        mLatitude = latest.getLatitude();
        mLongitude = latest.getLongitude();
        coordinates.setText(String.format("%f, %f", mLatitude, mLongitude));
        Button create_loc = (Button) findViewById(R.id.h_create_button);
        create_loc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createLocation();
            }
        });
	}

	/**
	 * Set up the {@link android.app.ActionBar}.
	 */
	private void setupActionBar() {

		getActionBar().setDisplayHomeAsUpEnabled(true);

	}

    private void createLocation() {
        mHiddenStatusMessageView.setText(R.string.hidden_progress_signing_in);
        mPoints = mPointsView.getText().toString();
        mHint = mHintView.getText().toString();
        mName = mNameView.getText().toString();
        showProgress(true);
        mCreateTask = new CreateLocationTask();
        mCreateTask.execute((Void) null);
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.create_hidden_location, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
            case android.R.id.home:
                // This ID represents the Home or Up button. In the case of this
                // activity, the Up button is shown. Use NavUtils to allow users
                // to navigate up one level in the application structure. For
                // more details, see the Navigation pattern on Android Design:
                //
                // http://developer.android.com/design/patterns/navigation.html#up-vs-back
                //
                NavUtils.navigateUpFromSameTask(this);
                return true;
            case R.id.action_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                break;
		}
		return super.onOptionsItemSelected(item);
	}

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(
                    android.R.integer.config_shortAnimTime);

            mHiddenStatusView.setVisibility(View.VISIBLE);
            mHiddenStatusView.animate().setDuration(shortAnimTime)
                    .alpha(show ? 1 : 0)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mHiddenStatusView.setVisibility(show ? View.VISIBLE
                                    : View.GONE);
                        }
                    });

            mHiddenFormView.setVisibility(View.VISIBLE);
            mHiddenFormView.animate().setDuration(shortAnimTime)
                    .alpha(show ? 0 : 1)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mHiddenFormView.setVisibility(show ? View.GONE
                                    : View.VISIBLE);
                        }
                    });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mHiddenStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
            mHiddenFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }


    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class CreateLocationTask extends AsyncTask<Void, Void, Integer> {
        @Override
        protected Integer doInBackground(Void... params) {

            JSONObject jsonobj = new JSONObject();
            JSONObject location = new JSONObject();

            try {
                location.put("latitude", mLatitude);
                location.put("longitude", mLongitude);
                location.put("points", mPoints);
                location.put("name", mName);
                location.put("hint", mHint);
                jsonobj.put("hidden_location", location);
                ConnectionHelper conn = new ConnectionHelper("locations.json", jsonobj);

                JSONObject recvdjson = conn.performRequest(getApplicationContext());
                if (recvdjson.has("message")){
                    String msn = recvdjson.getString("message");
                    if (msn.contentEquals("Location added successfully")) {
                        return 0;
                    }
                }
            } catch (JSONException ex) {
                ex.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
                return 3;
            }
            return 3;
        }

        @Override
        protected void onPostExecute(final Integer status) {
            mCreateTask = null;
            showProgress(false);

            switch(status) {
                case 0:
                    Toast.makeText(getApplication(), "Location created successfully!", Toast.LENGTH_LONG).show();
                    finish();
                    break;
                default:
                    Toast.makeText(getApplication(), "An error has occurred, Please try again.", Toast.LENGTH_LONG).show();
                    finish();

            }
        }

        @Override
        protected void onCancelled() {
            mCreateTask = null;
            showProgress(false);
        }
    }

}
