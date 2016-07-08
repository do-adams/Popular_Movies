/*
 * Copyright (C) 2016 Damián Adams
 */

package com.damian.popularmovies.activities.settings;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import com.damian.popularmovies.R;

/**
 * Fragment used to extend and implement a PreferenceFragment (settings).
 */

public class SettingsFragment extends PreferenceFragment {
    public static final String LIST_SUMMARY_KEY = "Sort";
    private ListPreference mListPreference;
    private String mListPreferenceSummary;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_general);

        mListPreference = (ListPreference)
                findPreference(getString(R.string.sorting_preference_key));

        mListPreference.setSummary(PreferenceManager.getDefaultSharedPreferences(getActivity())
                .getString(LIST_SUMMARY_KEY, getString(R.string.sorting_value_popular)));

        mListPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                String sortBy = (String) newValue;
                mListPreferenceSummary = sortBy;
                mListPreference.setSummary(sortBy);
                return true;
            }
        });
    }

    @Override
    public void onPause() {
        PreferenceManager.getDefaultSharedPreferences(getActivity())
                .edit()
                .putString(LIST_SUMMARY_KEY, mListPreferenceSummary)
                .apply();
        super.onPause();
    }
}
