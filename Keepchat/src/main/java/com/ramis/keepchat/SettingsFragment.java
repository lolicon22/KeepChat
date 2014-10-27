package com.ramis.keepchat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import net.rdrei.android.dirchooser.DirectoryChooserActivity;

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
        locationChooser.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                // opens new activity which asks the user to choose path
                final Intent chooserIntent = new Intent(getActivity(), DirectoryChooserActivity.class);
                startActivityForResult(chooserIntent, REQUEST_CHOOSE_DIR);
                return true;
            }
        });

		updateSummary(PREF_KEY_SNAP_IMAGES);
		updateSummary(PREF_KEY_SNAP_VIDEOS);
		updateSummary(PREF_KEY_STORIES_IMAGES);
		updateSummary(PREF_KEY_STORIES_VIDEOS);
		updateSummary(PREF_KEY_TOASTS_DURATION);
		updateSummary(PREF_KEY_SAVE_LOCATION);
	}

    // Receives the result of the DirectoryChooserActivity
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == REQUEST_CHOOSE_DIR) {
			if (resultCode == DirectoryChooserActivity.RESULT_CODE_DIR_SELECTED) {
				SharedPreferences.Editor editor = sharedPreferences.edit();

                String result = data.getStringExtra(DirectoryChooserActivity.RESULT_SELECTED_DIR);
				editor.putString(PREF_KEY_SAVE_LOCATION, result);
				editor.apply();

				updateSummary(PREF_KEY_SAVE_LOCATION);
			}
		}
	}

	private void updateSummary(String key) {
        Preference pref = findPreference(key);

        if(pref instanceof ListPreference) {
            ListPreference lp = (ListPreference) pref;
            lp.setSummary(lp.getEntry());
        } else if (key.equals(PREF_KEY_SAVE_LOCATION)) {
			pref.setSummary(sharedPreferences.getString(PREF_KEY_SAVE_LOCATION, ""));
		}
	}
}
