package com.rickster.blackout;


import java.util.ArrayList;
import java.util.Date;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.text.format.Time;
import android.util.Log;
import android.widget.Toast;

import com.rickster.blackout.R;
import com.rickster.blackout.RunDatabase.RunCursor;
import com.rickster.blackout.RunDatabase.RunLocationCursor;

public class RunManager {
	
	private static final String TAG = "RunManager";
	public static final String ACTION_LOCATION = "com.rickster.black.action_location";
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
	
	public long getCurrentRunId(){
		return mCurrentRunId;
	}
	
	public Run getCurrentRun(){
		return getRun(getCurrentRunId());
	}
	
	public void insertLocation(Location loc){
		if(mCurrentRunId != -1){
			Log.i(TAG, "Location has been inserted: " + mCurrentRunId + " | " + loc.getLatitude() + " | " + loc.getLongitude());
			if(mCurrentRunId != -1) mDatabase.insertLocation(mCurrentRunId, loc);
			if(new Date().getTime() - getCurrentRun().getDate().getTime() > RunDialog.DEFAULT_ENDING) stopLocationUpdates(getCurrentRun());
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
	
	public void updateRun(Run run){
		Log.i(TAG, "Updating Run: " + run.isClosed());
		mDatabase.updateRun(run);
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
	 
	public void closeRun(Run run){
		mPrefs.edit().putLong(PREF_CURRENT_RUN_ID, -1).commit();
		mCurrentRunId = -1;
		run.setClosed(true);
		updateRun(run);
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
		mLocationManager.requestLocationUpdates(provider, seconds, RunDialog.DEFAULT_DISTANCE, pi);
		Toast.makeText(mContext, R.string.have_fun_message, Toast.LENGTH_LONG).show();
		Log.i(TAG, "Started Location updates");
	}
	
	public void stopLocationUpdates(Run run){
		PendingIntent pi = getPendingIntent(false);
		if(pi != null){
			mLocationManager.removeUpdates(pi);
			pi.cancel();
			closeRun(run);
			Log.i(TAG, "Stopped Location Updates");
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
