package com.rickster.blackout;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.rickster.blackout.R;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class RunDatabase extends SQLiteOpenHelper {
	
	private static final String TAG = "RunDatabase";
	private static final String DB_NAME = "runs.sqlite";
	private static final int DB_VERSION = 7;
	private Context mContext;
	
	private static final String TABLE_RUN = "run";
    private static final String COLUMN_RUN_ID = "_id";
    private static final String COLUMN_RUN_START_DATE = "start_date";
    private static final String COLUMN_RUN_TEXT = "column_text";
    private static final String COLUMN_RUN_CLOSED = "column_closed";

    private static final String TABLE_LOCATION = "location";
    private static final String COLUMN_LOCATION_ID = "_id";
    private static final String COLUMN_LOCATION_LATITUDE = "latitude";
    private static final String COLUMN_LOCATION_LONGITUDE = "longitude";
    private static final String COLUMN_LOCATION_ALTITUDE = "altitude";
    private static final String COLUMN_LOCATION_TIMESTAMP = "timestamp";
    private static final String COLUMN_LOCATION_PROVIDER = "provider";
    private static final String COLUMN_LOCATION_RUN_ID = "run_id";
    private static final String COLUMN_LOCATION_NEARBY = "near_by";
    private static final String COLUMN_LOCATION_UNKNOWN = "unknown";
	
	public RunDatabase(Context c){
		super(c, DB_NAME, null, DB_VERSION);
		mContext = c;
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		String query = "CREATE TABLE IF NOT EXISTS " + TABLE_RUN 
					+ " ( " 
					+ COLUMN_RUN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " 
					+ COLUMN_RUN_START_DATE + " INTEGER , "
					+ COLUMN_RUN_TEXT + " VARCHAR(100), "
					+ COLUMN_RUN_CLOSED + " INTEGER DEFAULT 1"
					+ " ) ";
		db.execSQL(query);
		
		query = " CREATE TABLE IF NOT EXISTS " + TABLE_LOCATION 
				+ " (  "
				+ COLUMN_LOCATION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ COLUMN_LOCATION_LATITUDE + " REAL, " 
				+ COLUMN_LOCATION_LONGITUDE + " REAL, "
				+ COLUMN_LOCATION_ALTITUDE + " REAL, "
				+ COLUMN_LOCATION_TIMESTAMP + " INTEGER, "
				+ COLUMN_LOCATION_PROVIDER + " VARCHAR(100), "
				+ COLUMN_LOCATION_NEARBY + " VARCHAR(250), "
				+ COLUMN_LOCATION_UNKNOWN + " INTEGER DEFAULT 0, "
				+ COLUMN_LOCATION_RUN_ID + " INTEGER REFERENCES run(_id) "
				
				+ " ) ";
		db.execSQL(query);		
		
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		String query = "ALTER TABLE " + TABLE_LOCATION + " ADD " + COLUMN_LOCATION_UNKNOWN + " INTEGER DEFAULT 0";	
		
		db.execSQL(query);
		query = "ALTER TABLE " + TABLE_LOCATION + " ADD " + COLUMN_LOCATION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT";
		db.execSQL(query);
	}
	
	//insert run
	public long insertRun(Run run){
		ContentValues cv = new ContentValues();
		cv.put(COLUMN_RUN_START_DATE, run.getDate().getTime());
		cv.put(COLUMN_RUN_TEXT, run.getText());
		cv.put(COLUMN_RUN_CLOSED, (run.isClosed())? 1 : 0);
		Log.i(TAG, "Run has been inserted: " + run.getText() + " that started " + run.getDate().toString() + " Status: " + run.isClosed());
		
		return getWritableDatabase().insert(TABLE_RUN, null, cv);
	}
	
	//insert location
	public long insertLocation(long runId, Location loc){
		ContentValues cv = new ContentValues();
		cv.put(COLUMN_LOCATION_LATITUDE, loc.getLatitude());
		cv.put(COLUMN_LOCATION_LONGITUDE, loc.getLongitude());
		cv.put(COLUMN_LOCATION_ALTITUDE, loc.getAltitude());
		cv.put(COLUMN_LOCATION_TIMESTAMP, loc.getTime());
		cv.put(COLUMN_LOCATION_PROVIDER, loc.getProvider());
		cv.put(COLUMN_LOCATION_RUN_ID, runId);
		
		String nearby = getNearByLocation(loc);
		if(nearby.contentEquals(mContext.getString(R.string.location_unknown))) cv.put(COLUMN_LOCATION_UNKNOWN, 1);
		else cv.put(COLUMN_LOCATION_UNKNOWN, 0);
			
		cv.put(COLUMN_LOCATION_NEARBY, nearby);
		
		Log.i(TAG, "Location has been inserted: RunId " + runId + " at " + new Date(loc.getTime()).toLocaleString() + " | " + getNearByLocation(loc));
		return getWritableDatabase().insert(TABLE_LOCATION, null, cv);
	}
	
	//get nearest location
	public String getNearByLocation(Location loc){
		
		if(!hasConnection()) return mContext.getString(R.string.location_unknown);
		
		Geocoder geocoder = new Geocoder(mContext, Locale.getDefault());
		StringBuilder sb = new StringBuilder();
		try {
			List<Address> addresses = geocoder.getFromLocation(loc.getLatitude(), loc.getLongitude(), 1);	
			
			String address = addresses.get(0).getAddressLine(0);
			String locality = addresses.get(0).getLocality();
			String state = addresses.get(0).getAdminArea();
			
			sb.append(address).append(" , ").append(locality).append(" , ").append(state);			
				//Log.i(TAG, "Got some addresses: " + a.getLocality() + " - " + a.getFeatureName() + " - " + a.getMaxAddressLineIndex() + " - " + a.getSubThoroughfare() + " - " + a.getSubThoroughfare());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Log.i(TAG, "Address attained: " + sb.toString());
		return sb.toString();
	}
	
	@SuppressWarnings("resource")
	public void refreshUnknownLocations(long runId){		
		if(!hasConnection()) return;
		//has connection get all location marked with 0 for unkown
		String query = "SELECT * FROM " + TABLE_LOCATION + " WHERE " + COLUMN_LOCATION_UNKNOWN + " = ? AND " + COLUMN_LOCATION_RUN_ID + " = ? ";
		RunLocationCursor cursor = new RunLocationCursor(getReadableDatabase().rawQuery(query, new String[]{ String.valueOf(0) , String.valueOf(runId)}));
		if(cursor.moveToFirst()){
			do{
				//get nearby				
				RunLocation rl = (RunLocation) cursor.getRunLocation();
				Log.i(TAG, "Refreshing Location: " + rl.getId() + " | " + rl.getLatitude() + " | " + rl.getLongitude());
				Location loc = (Location) rl;			
				rl.setNearBy(getNearByLocation(loc));
				updateLocation(rl);
			}while(cursor.moveToNext());
		}		
	}
	
	public boolean updateLocation(RunLocation rl){
		ContentValues cv = new ContentValues();
		cv.put(COLUMN_LOCATION_ID, rl.getId());
		cv.put(COLUMN_LOCATION_LATITUDE, rl.getLatitude());
		cv.put(COLUMN_LOCATION_LONGITUDE, rl.getLongitude());
		cv.put(COLUMN_LOCATION_ALTITUDE, rl.getAltitude());
		cv.put(COLUMN_LOCATION_NEARBY, rl.getNearBy());
		cv.put(COLUMN_LOCATION_RUN_ID, rl.getRunId());
		cv.put(COLUMN_LOCATION_TIMESTAMP, rl.getTime());
		cv.put(COLUMN_LOCATION_PROVIDER, rl.getProvider());
		cv.put(COLUMN_LOCATION_UNKNOWN, (rl.getNearBy().equalsIgnoreCase(mContext.getString(R.string.location_unknown))) ? 0 : 1);		
		
		Log.i(TAG, "Location has been changed: " + rl.getId() + " | " + rl.getNearBy());
		return getWritableDatabase().update(TABLE_LOCATION, cv, COLUMN_LOCATION_ID + " =? " , new String[] { String.valueOf(rl.getId()) }) > 0;		
	}
	
	public boolean hasConnection(){
		ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = cm.getActiveNetworkInfo();
		return info != null && info.isAvailable() && info.isConnected();
	}
	
	//get run
	public RunCursor getRun(long id){
		String query = "SELECT * FROM " + TABLE_RUN + " WHERE " + COLUMN_RUN_ID + " =? LIMIT 1";
		Cursor wrapped = getReadableDatabase().rawQuery(query, new String[] { String.valueOf(id)});
		return new RunCursor(wrapped);
	}
	
	//get runs
	public RunCursor getRuns(){
		String query = "SELECT * FROM " + TABLE_RUN + " ORDER BY " + COLUMN_RUN_START_DATE + " DESC ";
		Cursor wrapped = getReadableDatabase().rawQuery(query, null);
		return new RunCursor(wrapped);
	}
	
	public RunLocationCursor getRunLocations(long runId){
		String query = "SELECT * FROM " + TABLE_LOCATION + " WHERE " + COLUMN_LOCATION_RUN_ID + " =? ORDER BY " + COLUMN_LOCATION_TIMESTAMP + " DESC ";
		Cursor wrapped = getReadableDatabase().rawQuery(query, new String[] { String.valueOf(runId) });
		return new RunLocationCursor(wrapped);
	}
	
	public RunLocationCursor getLastKnownRunLocation(long runId){
		String query = "SELECT * FROM " + TABLE_LOCATION + " WHERE " + COLUMN_LOCATION_RUN_ID + " =? ORDER BY " + COLUMN_LOCATION_TIMESTAMP + " DESC LIMIT 1";
		Cursor wrapped = getReadableDatabase().rawQuery(query, new String[] { String.valueOf(runId) });
		return new RunLocationCursor(wrapped);
	}	
	
	public boolean updateRun(Run run){
		ContentValues cv = new ContentValues();
		cv.put(COLUMN_RUN_ID, run.getId());
		cv.put(COLUMN_RUN_START_DATE, run.getDate().getTime());
		cv.put(COLUMN_RUN_TEXT, run.getText());
		cv.put(COLUMN_RUN_CLOSED, (run.isClosed())? 1 : 0);
		Log.i(TAG, "Run has been changed: " + run.getText() + " that started " + run.getDate().toString() + " Status: " + run.isClosed());
		return getWritableDatabase().update(TABLE_RUN, cv, COLUMN_RUN_ID + " =? " , new String[] { String.valueOf(run.getId()) }) > 0;			
	}	
	
	public class RunCursor extends CursorWrapper{		
		public RunCursor(Cursor c){
			super(c);
		}		
		public Run getRun(){
			if(isBeforeFirst() || isAfterLast()) return null;			
			Run run = new Run();
			run.setId(getLong(getColumnIndex(COLUMN_RUN_ID)));
			run.setDate(new Date(getLong(getColumnIndex(COLUMN_RUN_START_DATE))));
			run.setText(getString(getColumnIndex(COLUMN_RUN_TEXT)));
			run.setClosed((getInt(getColumnIndex(COLUMN_RUN_CLOSED)) == 1) ? true: false);
			return run;
		}
	}
	
	public class RunLocationCursor extends CursorWrapper{		
		public RunLocationCursor(Cursor c){
			super(c);
		}
		public RunLocation getRunLocation(){
			if(isBeforeFirst() || isAfterLast()) return null;
			
			String provider = getString(getColumnIndex(COLUMN_LOCATION_PROVIDER));
			Location location = new Location(provider);
			
			location.setLatitude(getDouble(getColumnIndex(COLUMN_LOCATION_LATITUDE)));
			location.setLongitude(getDouble(getColumnIndex(COLUMN_LOCATION_LONGITUDE)));
			location.setAltitude(getDouble(getColumnIndex(COLUMN_LOCATION_ALTITUDE)));
			location.setTime(getLong(getColumnIndex(COLUMN_LOCATION_TIMESTAMP)));
			
			RunLocation runLocation = new RunLocation(location);
			runLocation.setNearBy(getString(getColumnIndex(COLUMN_LOCATION_NEARBY)));
			runLocation.setId(getLong(getColumnIndex(COLUMN_LOCATION_ID)));
			runLocation.setRunId(getLong(getColumnIndex(COLUMN_LOCATION_RUN_ID)));
			
			Log.i(TAG, "Returning: " + runLocation.getNearBy());
			return runLocation;
		}		
	}
}
