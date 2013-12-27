package com.example.blackout;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.widget.FrameLayout;

public abstract class SingleFragmentActivity extends FragmentActivity {
	
	private static final String TAG = "SingleFragmentActivity";
	
	protected abstract Fragment createFragment();
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
		FrameLayout fl = new FrameLayout(this);
		fl.setId(R.id.fragmentContainer);
		setContentView(fl);		
		
		FragmentManager fm = getSupportFragmentManager();
		Fragment fragment = fm.findFragmentById(R.id.fragmentContainer);
		
		if(fragment == null){
			fragment = createFragment();
			fm.beginTransaction().add(R.id.fragmentContainer, fragment).commit();
		}
	}
	
}
