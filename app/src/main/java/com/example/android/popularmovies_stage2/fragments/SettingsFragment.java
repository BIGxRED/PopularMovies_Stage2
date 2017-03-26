/*
The following code is the property and sole work of Mike Palarz, a student at Udacity
 */

package com.example.android.popularmovies_stage2.fragments;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.preference.PreferenceScreen;

import com.example.android.popularmovies_stage2.R;

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
                    setPreferenceSummary(preference);
                }
            }
        }
    }

    /*
    This is a helper method which sets the summary of a Preference if it is a ListPreference. Checking
    if it is a ListPreference will suffice for now because that's the only preference type that we
    are using, but it may be necessary to expand this method if more preferences are added.
     */
    private void setPreferenceSummary(Preference preference){
        if (preference instanceof ListPreference){
            ListPreference listPreference = (ListPreference) preference;
            listPreference.setSummary(listPreference.getEntry());
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
                setPreferenceSummary(preference);
            }
        }
    }

    /*
    This is a helper method which obtains the method flag for us. It is especially useful within
    several classes (MovieSelection, MovieFetcher, etc.).  Because only one preference is available
    within the app at this time, this method only applies to that single preference. However, it
    has been structured such that it could be easily adjusted in case additional preferences would
    be added in the future.
     */
    public static int getPreferenceValue(Context context, String key){
        //Obtain a reference to the default SharedPreferences DB
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);

        //We then check if the key that was passed is the same as the key for the list preference
        if (key == context.getString(R.string.list_preference_sorting_options_key)){
            String sortingOptionsValueString = preferences.getString(key,
                    context.getString(R.string.list_preference_sorting_options_default_value));

            int sortingOptionsValue = Integer.parseInt(sortingOptionsValueString);

            //If the correct key was passed, then the appropriate method flag is returned
            return sortingOptionsValue;
        }
        else {
            return 0;
        }
    }
}
