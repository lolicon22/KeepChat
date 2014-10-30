package com.ramis.keepchat;

import android.content.Context;
import android.preference.ListPreference;
import android.util.AttributeSet;

/**
 * ListPreference which automatically updates its summary when the value is changed.
 */
public class SummaryListPreference extends ListPreference{
    public SummaryListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SummaryListPreference(Context context) {
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
}
