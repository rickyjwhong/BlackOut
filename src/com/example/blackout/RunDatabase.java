package com.example.blackout;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.util.Log;

public class RunDatabase extends SQLiteOpenHelper {
	
	private static final String TAG = "RunDatabase";
	private static final String DB_NAME = "runs.sqlite";
	private static final int DB_VERSION = 5;
	private Context mContext;
	
	private static final String TABLE_RUN = "run";
    private static final String COLUMN_RUN_ID = "_id";
    private static final String COLUMN_RUN_START_DATE = "start_date";
    private static final String COLUMN_RUN_TEXT = "column_text";
    private static final String COLUMN_RUN_RUNNING = "column_running";

    private static final String TABLE_LOCATION = "location";
    private static final String COLUMN_LOCATION_LATITUDE = "latitude";
    private static final String COLUMN_LOCATION_LONGITUDE = "longitude";
    private static final String COLUMN_LOCATION_ALTITUDE = "altitude";
    private static final String COLUMN_LOCATION_TIMESTAMP = "timestamp";
    private static final String COLUMN_LOCATION_PROVIDER = "provider";
    private static final String COLUMN_LOCATION_RUN_ID = "run_id";
    private static final String COLUMN_LOCATION_NEARBY = "near_by";
	
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
					+ COLUMN_RUN_RUNNING + " INTEGER "
					+ " ) ";
		db.execSQL(query);
		
		query = " CREATE TABLE IF NOT EXISTS " + TABLE_LOCATION 
				+ " (  "
				+ COLUMN_LOCATION_LATITUDE + " REAL, " 
				+ COLUMN_LOCATION_LONGITUDE + " REAL, "
				+ COLUMN_LOCATION_ALTITUDE + " REAL, "
				+ COLUMN_LOCATION_TIMESTAMP + " INTEGER, "
				+ COLUMN_LOCATION_PROVIDER + " VARCHAR(100), "
				+ COLUMN_LOCATION_NEARBY + " VARCHAR(250), "
				+ COLUMN_LOCATION_RUN_ID + " INTEGER REFERENCES run(_id) "
				+ " ) ";
		db.execSQL(query);		
		
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		
		String query = "ALTER TABLE " + TABLE_LOCATION + " ADD " + COLUMN_LOCATION_NEARBY + " VARCHAR(250) ";
		db.execSQL(query);
	}
	
	//insert run
	public long insertRun(Run run){
		ContentValues cv = new ContentValues();
		cv.put(COLUMN_RUN_START_DATE, run.getDate().getTime());
		cv.put(COLUMN_RUN_TEXT, run.getText());
		cv.put(COLUMN_RUN_RUNNING, (run.isClosed())? 0 : 1);
		Log.i(TAG, "Run has been inserted: " + run.getText() + " that started " + run.getDate().toString());
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
		cv.put(COLUMN_LOCATION_NEARBY, getNearByLocation(loc));
		Log.i(TAG, "Location has been inserted: RunId " + runId + " at " + new Date(loc.getTime()).toLocaleString() + " | " + getNearByLocation(loc));
		return getWritableDatabase().insert(TABLE_LOCATION, null, cv);
	}
	
	//get nearest location
	public String getNearByLocation(Location loc){
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
		String query = 
				" UPDATE " + TABLE_RUN + " SET " 
				+ COLUMN_RUN_START_DATE + " = ? " 
				+ COLUMN_RUN_TEXT + " =? "
				+ COLUMN_RUN_RUNNING + " =? "
				+ " WHERE " + COLUMN_RUN_ID + " =? "
				;
		try	{
			getWritableDatabase().rawQuery(query, 
					new String[] { 
						String.valueOf(run.getDate().getTime()), 
						run.getText(), 
						Integer.toString((run.isClosed()) ? 1 : 0), 
						String.valueOf(run.getId())
						});
		}catch(Exception e){
			return false;
		}
		return true;		
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
			run.setClosed((getInt(getColumnIndex(COLUMN_RUN_RUNNING)) == 0) ? false : true);
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
			Log.i(TAG, "Returning: " + runLocation.getNearBy());
			return runLocation;
		}
		
		
	}

}
