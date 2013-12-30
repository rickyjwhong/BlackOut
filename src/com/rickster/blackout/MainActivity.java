package com.rickster.blackout;

import android.os.Bundle;
import android.app.Activity;
import android.support.v4.app.Fragment;
import android.view.Menu;

public class MainActivity extends SingleFragmentActivity {

	@Override
	protected Fragment createFragment() {
		// TODO Auto-generated method stub
		long runId = RunManager.get(this).getCurrentRunId();
		if(runId != -1){
			return RunFragment.newInstance(runId);
		}else{
			return RunFragment.newInstance(-1);
		}
		
	}

}
