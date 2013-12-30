package com.rickster.blackout;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.util.Log;

public class OffLineReceiver extends LocationReceiver {
	
	private static final String TAG = "OffLineReceiver";
	
	@Override
	public void onLocationChanged(Context c, Location loc){
		Log.i(TAG, "OffLine Location Received: " + loc.getLatitude() + " and " + loc.getLongitude());
		RunManager.get(c).insertLocation(loc);
		
	}
	
}
