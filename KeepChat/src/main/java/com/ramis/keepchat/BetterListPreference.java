package com.ramis.keepchat;

import android.content.Context;
import android.preference.ListPreference;
import android.util.AttributeSet;

/**
 * ListPreference which automatically updates its summary when the value is changed.
 */
public class BetterListPreference extends ListPreference{
    public BetterListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BetterListPreference(Context context) {
        super(context);
    }

    @Override
    public void setValue(final String value) {
        super.setValue(value);
        notifyChanged();
    }

    @Override
    public CharSequence getSummary() {
        int index = findIndexOfValue(getValue());
        return getEntries()[index];
    }

    @Override
    protected boolean persistString(String value) {
        if(value == null) {
            return false;
        } else {
            return persistInt(Integer.valueOf(value));
        }
    }

    @Override
    protected String getPersistedString(String defaultReturnValue) {
        if(getSharedPreferences().contains(getKey())) {
            int intValue = getPersistedInt(0);
            return String.valueOf(intValue);
        } else {
            return defaultReturnValue;
        }
    }
}
