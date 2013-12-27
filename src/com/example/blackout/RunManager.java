package com.example.blackout;

import java.util.ArrayList;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

import com.example.blackout.RunDatabase.RunCursor;
import com.example.blackout.RunDatabase.RunLocationCursor;

public class RunManager {
	
	private static final String TAG = "RunManager";
	public static final String ACTION_LOCATION = "com.example.black.action_location";
	public static final int LOCATION_REQUEST = 1;
	private static final String PREF_CURRENT_RUN_ID = "RunManager.currentRunId";
	private static final String PREFS_FILE = "runs";
	
	private Context mContext;
	private static RunManager sRunManager;
	private LocationManager mLocationManager;
	private ArrayList<Run> mRuns;
	private Run mCurrentRun;
	private RunDatabase mDatabase;
	private long mCurrentRunId;
	private SharedPreferences mPrefs;
	
	public RunManager(Context c){
		mContext = c;
		mLocationManager = (LocationManager) c.getSystemService(Context.LOCATION_SERVICE);
		mDatabase = new RunDatabase(mContext);
		mPrefs = mContext.getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE);
        mCurrentRunId = mPrefs.getLong(PREF_CURRENT_RUN_ID, -1);
	}
	
	public void setCurrentRun(Run run){
		Log.i(TAG, "Run Id from set current run: " + run.getId());
		mCurrentRunId = run.getId();
		mPrefs.edit().putLong(PREF_CURRENT_RUN_ID, run.getId()).commit();
	}
	
	public void insertLocation(Location loc){
		if(mCurrentRunId != -1){
			Log.i(TAG, "Location has been inserted: " + mCurrentRunId + " | " + loc.getLatitude() + " | " + loc.getLongitude());			
			mDatabase.insertLocation(mCurrentRunId, loc);
		}		
	}
	
	public Run insertRun(Run run){
		run.setId(mDatabase.insertRun(run));
		Log.i(TAG, "Run Id from the database: " + run.getId());
		return run;
	}

	public Run getRun(long id){
		Run run = null;
		RunCursor cursor = mDatabase.getRun(id);
		cursor.moveToFirst();
		if(!cursor.isAfterLast())
			run = cursor.getRun();
		cursor.close();
		return run;
	}
	
	public ArrayList<Run> getRuns(){
		ArrayList<Run> runs = new ArrayList<Run>();
		RunCursor cursor = mDatabase.getRuns();
		if(cursor.moveToFirst()){
			do{
				runs.add(cursor.getRun());
			}while(cursor.moveToNext());
		}
		cursor.close();
		return runs;
	}

	public ArrayList<RunLocation> getRunLocations(long runId){
		Log.i(TAG, "Location retrieval has been called");
		ArrayList<RunLocation> locations = new ArrayList<RunLocation>();
		RunLocationCursor cursor = mDatabase.getRunLocations(runId);
		if(cursor.moveToFirst()){
			do{
				locations.add(cursor.getRunLocation());
			}while(cursor.moveToNext());
		}
		cursor.close();
		return locations;
	}
	
	public RunLocation getLastKnownRunLocation(long runId){
		RunLocation location = null;
		RunLocationCursor cursor = mDatabase.getLastKnownRunLocation(runId);
		cursor.moveToFirst();
		if(!cursor.isAfterLast())
			location = cursor.getRunLocation();
		cursor.close();
		return location;
	}
	
	//get locations
	 public Run startNewRun(Run run) {
        // insert a run into the db
        run = insertRun(run);
        // start tracking the run
        startTrackingRun(run);
        return run;
    }
    
    public void startTrackingRun(Run run) {
        // keep the ID
    	setCurrentRun(run);        
        // start location updates
        startLocationUpdates(run.getFrequency());
        //got to change the parameter for this
    }
	
	public PendingIntent getPendingIntent(boolean shouldCreate){
		Intent i = new Intent(ACTION_LOCATION);
		int flag = (shouldCreate) ? 0 : PendingIntent.FLAG_NO_CREATE;
		return PendingIntent.getBroadcast(mContext, LOCATION_REQUEST, i, flag);
	}
	
	public void startLocationUpdates(long seconds){
		String provider = LocationManager.NETWORK_PROVIDER;
		
		Location lastLocation = mLocationManager.getLastKnownLocation(provider);
		if(lastLocation != null){
			lastLocation.setTime(System.currentTimeMillis());
			sendBroadcast(lastLocation);
		}
		
		PendingIntent pi = getPendingIntent(true);
		mLocationManager.requestLocationUpdates(provider, 0, 0, pi);	
	}
	
	public void stopLocationUpdates(Run run){
		PendingIntent pi = getPendingIntent(false);
		if(pi != null){
			mLocationManager.removeUpdates(pi);
			pi.cancel();
		}
	}
	
	public boolean isLocationUpdateOn(){
		return getPendingIntent(false) != null;
	}
	
	public void sendBroadcast(Location loc){
		Intent i = new Intent(ACTION_LOCATION);
		i.putExtra(LocationManager.KEY_LOCATION_CHANGED, loc);
		mContext.sendBroadcast(i);
	}
	
	public static RunManager get(Context c){
		if(sRunManager == null) sRunManager = new RunManager(c.getApplicationContext());
		return sRunManager;
	}
	
}
