package com.ramis.keepchat;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceManager;

public class MainActivity extends Activity {
	
	public static Context context;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = this;
		getFragmentManager().beginTransaction()
				.replace(android.R.id.content, new Settings()).commit();
		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
	}
}
