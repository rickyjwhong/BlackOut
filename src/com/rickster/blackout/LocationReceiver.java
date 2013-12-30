package com.rickster.blackout;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

public class LocationReceiver extends BroadcastReceiver {

	private static final String TAG = "LocationReceiver";
	
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		Location loc = intent.getParcelableExtra(LocationManager.KEY_LOCATION_CHANGED);
		
		if(loc != null){
			onLocationChanged(context, loc);
			return;
		}
		
		if(intent.hasExtra(LocationManager.KEY_PROVIDER_ENABLED)){
			onProviderEnabled(intent.getBooleanExtra(LocationManager.KEY_PROVIDER_ENABLED, false));
		}
	}
	
	protected void onLocationChanged(Context c, Location loc){
		Log.i(TAG, "Location Received: " + loc.getLatitude() + " and " + loc.getAltitude());
	}
	
	protected void onProviderEnabled(boolean enabled){
		Log.i(TAG, "Provider status has been changed: " + enabled);
	}

}
