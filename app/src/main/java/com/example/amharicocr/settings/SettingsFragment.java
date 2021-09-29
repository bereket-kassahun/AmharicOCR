package com.example.amharicocr.settings;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.amharicocr.MainActivity;
import com.example.amharicocr.R;
import com.example.amharicocr.sharedpreference.SharedPreference;

import java.util.Locale;

public class SettingsFragment extends Fragment {
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.settings_fragment, container, false);

        return root;
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getActivity().getFragmentManager().beginTransaction()
                .replace(R.id.settings_fragment, new MyPreferenceFragment())
                .commit();


    }
    public static void setLocale(Activity activity, String languageCode) {
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);
        Resources resources = activity.getResources();
        Configuration config = resources.getConfiguration();
        config.setLocale(locale);
        resources.updateConfiguration(config, resources.getDisplayMetrics());

    }

    public static class MyPreferenceFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

        SharedPreference preference;
        public MyPreferenceFragment() {

        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            preference = new SharedPreference(getActivity());
            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.prefrences);
            ListPreference listPreference = (ListPreference) findPreference("Font Size");
            CharSequence currText = listPreference.getEntry();
            String currValue = listPreference.getValue();

            preference.setFontSize(Integer.parseInt(currValue));

        }

        @Override
        public void onPause() {
            super.onPause();
            getPreferenceScreen().getSharedPreferences()
                    .unregisterOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onResume() {
            super.onResume();
            getPreferenceScreen().getSharedPreferences()
                    .registerOnSharedPreferenceChangeListener(this);
        }


        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
//            Toast.makeText(getActivity(), "damn", Toast.LENGTH_SHORT).show();

            if (s.equals("language"))
            {
                // Set summary to be the user-description for the selected value
//                Preference exercisesPref = findPreference(s);
//                exercisesPref.setSummary(sharedPreferences.getString(s, ""));
//                MainActivity activity = (MainActivity) getActivity();
//                activity.setLocale(sharedPreferences.getString(s, ""));
                Log.e("shit ===++++", sharedPreferences.getString(s, ""));
                MainActivity mainActivity =(MainActivity) getActivity();
                preference.setLanguage(sharedPreferences.getString(s, ""));
//                setLocale(getActivity(), sharedPreferences.getString(s, ""));
                mainActivity.setLocale(sharedPreferences.getString(s, ""));
            }else if(s.equals("Font Size")){
                ListPreference listPreference = (ListPreference) findPreference("Font Size");
                CharSequence currText = listPreference.getEntry();
                String currValue = listPreference.getValue();
                preference.setFontSize(Integer.parseInt(currValue));
            }
        }
    }
}