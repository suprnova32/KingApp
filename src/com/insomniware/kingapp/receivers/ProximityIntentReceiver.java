package com.insomniware.kingapp.receivers;

import com.insomniware.kingapp.MainPageActivity;
import com.insomniware.kingapp.R;
import com.insomniware.kingapp.R.drawable;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.LocationManager;
import android.util.Log;

public class ProximityIntentReceiver extends BroadcastReceiver {
    
    private static final int NOTIFICATION_ID = 1000;

	@Override
    public void onReceive(Context context, Intent intent) {
        
        Boolean entering = intent.getBooleanExtra(LocationManager.KEY_PROXIMITY_ENTERING, false);
        
        if (entering) {
            Log.d(getClass().getSimpleName(), "entering");
        }
        else {
            Log.d(getClass().getSimpleName(), "exiting");
        }
        launchNotify("", context);
        
        
    }
    
    @SuppressWarnings("deprecation")
	public void launchNotify(String name, Context context) {
    	NotificationManager notificationManager = 
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            
        Intent myIntent = new Intent(context, MainPageActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(context, 0, myIntent, 0);        
        
        Notification notification = createNotification();
        notification.setLatestEventInfo(context, 
            "Proximity Alert!", "You are near a hidden location called " + name, pIntent);
        
        notificationManager.notify(NOTIFICATION_ID, notification);
    }
    
    private Notification createNotification() {
        Notification notification = new Notification();
        
        notification.icon = R.drawable.ic_launcher;
        notification.when = System.currentTimeMillis();
        
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        notification.flags |= Notification.FLAG_SHOW_LIGHTS;
        
        notification.defaults |= Notification.DEFAULT_VIBRATE;
        notification.defaults |= Notification.DEFAULT_LIGHTS;
        
        notification.ledARGB = Color.WHITE;
        notification.ledOnMS = 1500;
        notification.ledOffMS = 1500;
        
        return notification;
    }
    
}