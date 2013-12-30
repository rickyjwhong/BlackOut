package com.rickster.blackout;

import com.rickster.blackout.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

public class RunDialog extends DialogFragment {
	
	private static final String TAG = "RunDialog";
	public static final String RUN_ID_EXTRA = "run_id_extra";
	public static final String TITLE_EXTRA = "title_extra";
	public static final String DURATION_EXTRA = "duration_extra";
	public static final int MINUTES_SECONDS = 60; // 60 seconds
	public static final int DEFAULT_DISTANCE = 40; //40 meters
	public static final long DEFAULT_DURATION = 1000 * 20 * MINUTES_SECONDS; //20 seconds or 20 min depdnding on debug
	public static final long DEFAULT_ENDING = 8 * 60 * 60 * 1000; //8 hours default time and then it shuts off
	private String mTitle;
	private EditText mTitleView;
	private RadioGroup mDurationGroup;
	private long mDuration;
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState){
		
		View v = getActivity().getLayoutInflater().inflate(R.layout.run_dialog, null);
		mTitleView = (EditText) v.findViewById(R.id.run_dialog_name);
		mTitleView.addTextChangedListener(new TextWatcher() {

			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// TODO Auto-generated method stub
				if(s != null)
					mTitle = s.toString();
			}
			
		});
		
		mDurationGroup = (RadioGroup) v.findViewById(R.id.durationPreference);
		mDurationGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				// TODO Auto-generated method stub
				switch(checkedId){
					case R.id.interval_5:
						mDuration = 5000 * MINUTES_SECONDS;
						break;
					case R.id.interval_10:
						mDuration = 10000 * MINUTES_SECONDS;
						break;
					case R.id.interval_20:
						mDuration = 20000 * MINUTES_SECONDS;
						break;
					case R.id.interval_30:
						mDuration = 30000 * MINUTES_SECONDS;
						break;
					default:
						mDuration = DEFAULT_DURATION;
				}
			}
		});
		
		return new AlertDialog.Builder(getActivity())
		.setTitle(R.string.run_dialog_title)
		.setView(v)
		.setPositiveButton(android.R.string.ok, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if(mDuration != 0 && mTitle != null)
					sendResult(Activity.RESULT_OK);
				else{
					Toast.makeText(getActivity(), "Please fill out everything", Toast.LENGTH_LONG).show();
				}
			}
		}).setNegativeButton(android.R.string.cancel,new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
			}
		}).create();		
	}
	
	public void sendResult(int resultCode){
		if(resultCode != Activity.RESULT_OK){
			//delete the run that was created
			Toast.makeText(getActivity(), "Sorry something went wrong" , Toast.LENGTH_LONG).show();
		}else{
			//modify run and start the update
			if(getTargetFragment() == null) return;
			Intent i = new Intent();
			i.putExtra(DURATION_EXTRA, mDuration);
			i.putExtra(TITLE_EXTRA, mTitle);
			Log.i(TAG, "Sending Result back - Name : " + mTitle + " - Duration: " + mDuration);
			getTargetFragment().onActivityResult(getTargetRequestCode(), resultCode, i);
		}
	}
	
}
