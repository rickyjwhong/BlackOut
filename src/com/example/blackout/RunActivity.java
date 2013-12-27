package com.example.blackout;

import java.util.ArrayList;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;

public class RunActivity extends SingleFragmentActivity {
	
	private static final String TAG = "RunPagerActivity";
	
	@Override
	protected Fragment createFragment() {
		long runId = (Long) getIntent().getSerializableExtra(RunFragment.RUN_EXTRA_ID);
		// TODO Auto-generated method stub
		return RunFragment.newInstance(runId);
	}
	
	
	
}
