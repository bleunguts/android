package org.syncloud.android.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;

import com.google.common.collect.Sets;

import org.syncloud.android.Preferences;
import org.syncloud.android.R;
import org.syncloud.android.SyncloudApplication;

import java.util.Set;

import static org.syncloud.android.Preferences.KEY_PREF_API_URL;
import static org.syncloud.android.Preferences.KEY_PREF_DISCOVERY_LIBRARY;
import static org.syncloud.android.Preferences.KEY_PREF_EMAIL;
import static org.syncloud.android.Preferences.KEY_PREF_SSH_MODE;

public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    private Preference removeAccountPref;
    private Preference feedbackPref;
    private SyncloudApplication application;
    private Set<String> summaryUpdatable = Sets.newHashSet(
        KEY_PREF_API_URL,
//        KEY_PREF_EMAIL,
        KEY_PREF_DISCOVERY_LIBRARY,
        KEY_PREF_SSH_MODE);
    private PreferenceCategory systemCategory;
    private Preference preferenceSshMode;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        application = (SyncloudApplication) getActivity().getApplication();

        addPreferencesFromResource(R.xml.preferences);

        systemCategory = (PreferenceCategory) findPreference(Preferences.KEY_CATEGORY_SYSTEM);
        preferenceSshMode = findPreference(KEY_PREF_SSH_MODE);

        Preference debugPreference = findPreference(Preferences.KEY_PREF_DEBUG_MODE);
        debugPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                debugMode((Boolean)o);
                return true;
            }
        });

        removeAccountPref = findPreference(Preferences.KEY_PREF_ACCOUNT_REMOVE);
        removeAccountPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                SharedPreferences preferences = getPreferenceScreen().getSharedPreferences();
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString(KEY_PREF_EMAIL, null);
                editor.putString(Preferences.KEY_PREF_PASSWORD, null);
                editor.apply();
                updateSummary(preferences, KEY_PREF_EMAIL);

                Intent intent = new Intent(SettingsFragment.this.getActivity(), AuthActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);

                return true;
            }
        });

        feedbackPref = findPreference(Preferences.KEY_PREF_FEEDBACK_SEND);
        feedbackPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                application.reportError();
                return true;
            }
        });

        findPreference(Preferences.KEY_PREF_LOGS).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                startActivity(new Intent(SettingsFragment.this.getActivity(), LogsActivity.class));
                return true;
            }
        });

        SharedPreferences preferences = getPreferenceScreen().getSharedPreferences();
        preferences.registerOnSharedPreferenceChangeListener(this);
        for (String pref : summaryUpdatable) {
            updateSummary(preferences, pref);
        }
        updateSummary(preferences, Preferences.KEY_PREF_EMAIL);
        updateRemoveAccountPref(preferences);

        debugMode(application.getPreferences().isDebug());
    }

    private void debugMode(boolean isDebug) {
        if (isDebug) {
            systemCategory.addPreference(preferenceSshMode);
        } else {
            systemCategory.removePreference(preferenceSshMode);
        }
    }

    private void updateRemoveAccountPref(SharedPreferences sharedPreferences) {
        String email = sharedPreferences.getString(KEY_PREF_EMAIL, null);
        removeAccountPref.setEnabled(email != null);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(KEY_PREF_EMAIL)) {
            updateRemoveAccountPref(sharedPreferences);
        } else if (summaryUpdatable.contains(key)) {
            updateSummary(sharedPreferences, key);
        }
    }

    private void updateSummary(SharedPreferences sharedPreferences, String key) {
        String summary = getSummary(sharedPreferences, key);
        findPreference(key).setSummary(summary);
    }

    private String getSummary(SharedPreferences sharedPreferences, String key) {
        String summary = sharedPreferences.getString(key, null);
        if (summary != null)
            return summary;
        if (key.equals(KEY_PREF_EMAIL))
            return "Not specified yet";
        return "None";
    }
}
