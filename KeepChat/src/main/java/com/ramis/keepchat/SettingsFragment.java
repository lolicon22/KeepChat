package com.ramis.keepchat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import net.rdrei.android.dirchooser.DirectoryChooserActivity;

import java.io.File;

public class SettingsFragment extends PreferenceFragment {

    private static final int REQUEST_CHOOSE_DIR = 0x0B00B135;
    public static final String PREF_KEY_SAVE_LOCATION = "pref_key_save_location";

    private SharedPreferences sharedPreferences;

	@SuppressWarnings("deprecation")
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getPreferenceManager().setSharedPreferencesMode(Context.MODE_WORLD_READABLE);
		// Load the preferences from an XML resource
		addPreferencesFromResource(R.xml.preferences);

		// If the Save Location doesn't exist in SharedPreferences add it
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
		if (!sharedPreferences.contains(PREF_KEY_SAVE_LOCATION)) {
            String defaultLocation = Environment.getExternalStorageDirectory().toString() + "/keepchat";
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(PREF_KEY_SAVE_LOCATION, defaultLocation);
			editor.apply();
		}

        // Set onClickListener for choosing the Save Location
        Preference locationChooser = findPreference(PREF_KEY_SAVE_LOCATION);
        locationChooser.setSummary(sharedPreferences.getString(PREF_KEY_SAVE_LOCATION, ""));
        locationChooser.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                // Open a new activity asking the user to select a folder
                final Intent chooserIntent = new Intent(getActivity(), DirectoryChooserActivity.class);
                chooserIntent.putExtra(DirectoryChooserActivity.EXTRA_NEW_DIR_NAME, "Keepchat");
                startActivityForResult(chooserIntent, REQUEST_CHOOSE_DIR);
                return true;
            }
        });
	}

    // Receives the result of the DirectoryChooserActivity
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == REQUEST_CHOOSE_DIR) {
			if (resultCode == DirectoryChooserActivity.RESULT_CODE_DIR_SELECTED) {
                String newLocation = data.getStringExtra(DirectoryChooserActivity.RESULT_SELECTED_DIR);

                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(PREF_KEY_SAVE_LOCATION, newLocation);
                editor.apply();

                Preference pref = findPreference(PREF_KEY_SAVE_LOCATION);
                pref.setSummary(newLocation);
			}
		}
	}

    @Override
    public void onPause() {
        super.onPause();

        // Set preferences file permissions to be world readable
        File sharedPrefsDir = new File(getActivity().getApplicationInfo().dataDir, "shared_prefs");
        File sharedPrefsFile = new File(sharedPrefsDir, getPreferenceManager().getSharedPreferencesName() + ".xml");
        if (sharedPrefsFile.exists()) {
            sharedPrefsFile.setReadable(true, false);
        }
    }
}
