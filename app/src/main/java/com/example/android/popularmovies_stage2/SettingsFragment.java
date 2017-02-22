package com.example.android.popularmovies_stage2;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceScreen;

public class SettingsFragment extends PreferenceFragmentCompat
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    /*
    We override this method so that we can register our OnSharedPreferenceChangeListener.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    /*
    We override this method so that we can unregister our OnSharedPreferenceChangeListener.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();

        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    /*
    This method is called within onCreate() so that preferences are supplied to this fragment.
    Within this method, we obtain a reference to all of our preferences and set their summary if
    necessary.
     */
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.movie_preferences);

        //References to the SharedPreferences DB and the preference screen are obtained.
        SharedPreferences sharedPreferences = getPreferenceScreen().getSharedPreferences();
        PreferenceScreen screen = getPreferenceScreen();

        //Iterate through all of the preferences and set the summary if they are not a CheckPreference.
        for (int i = 0; i < screen.getPreferenceCount(); i++){
            Preference preference = screen.getPreference(i);
            if (preference != null){
                if (!(preference instanceof CheckBoxPreference)){
                    String value = sharedPreferences.getString(preference.getKey(),"");
                    setPreferenceSummary(preference, value);
                }
            }
        }
    }

    /*
    This is a helper method which sets the summary of a Preference if it is a ListPreference. Checking
    if it is a ListPreference will suffice for now because that's the only preference type that we
    are using, but it may be necessary to expand this method if more preferences are added.
     */

    //TODO: I don't think there's much of a purpose to include the value String in this method since
    //getEntry() appears to work instead of getEntries(). Therefore, consider removing the value
    //parameter.
    private void setPreferenceSummary(Preference preference, String value){
        if (preference instanceof ListPreference){
            ListPreference listPreference = (ListPreference) preference;
            listPreference.setSummary(listPreference.getEntry());
//            int preferenceIndex = listPreference.findIndexOfValue(value);
//            if (preferenceIndex >= 0){
//                listPreference.setSummary(listPreference.getEntries()[preferenceIndex]);
//            }
        }
    }

    /*
    Necessary method in order to use the OnSharedPreferenceChangeListener interface. Within this
    method, we use setPreferenceSummary() to update the summary of our ListPreference accordingly.
     */
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Preference preference = findPreference(key);
        if (preference != null){
            if (!(preference instanceof CheckBoxPreference)){
                String value = sharedPreferences.getString(preference.getKey(),"");
                setPreferenceSummary(preference, value);
            }
        }
    }
}
