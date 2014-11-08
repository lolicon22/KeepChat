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

    private static final int REQUEST_CHOOSE_DIR = 1;
	public static final String PREF_KEY_SNAP_IMAGES = "pref_key_snaps_images";
	public static final String PREF_KEY_SNAP_VIDEOS = "pref_key_snaps_videos";
	public static final String PREF_KEY_STORIES_IMAGES = "pref_key_stories_images";
	public static final String PREF_KEY_STORIES_VIDEOS = "pref_key_stories_videos";
	public static final String PREF_KEY_TOASTS_DURATION = "pref_key_toasts_duration";
	public static final String PREF_KEY_SAVE_LOCATION = "pref_key_save_location";

    private SharedPreferences sharedPreferences;

	@SuppressWarnings("deprecation")
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getPreferenceManager().setSharedPreferencesMode(Context.MODE_WORLD_READABLE);
		// Load the preferences from an XML resource
		addPreferencesFromResource(R.xml.preferences);

		// check if preference exists in SharedPreferences
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
		if (!sharedPreferences.contains(PREF_KEY_SAVE_LOCATION)) {
			// set default value
			String root = Environment.getExternalStorageDirectory().toString();
			SharedPreferences.Editor editor = sharedPreferences.edit();
			editor.putString(PREF_KEY_SAVE_LOCATION, root + "/keepchat");
			editor.apply();
		}

        // set on click listener
        Preference locationChooser = findPreference(PREF_KEY_SAVE_LOCATION);
        locationChooser.setSummary(sharedPreferences.getString(PREF_KEY_SAVE_LOCATION, ""));
        locationChooser.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                // opens new activity which asks the user to choose path
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
