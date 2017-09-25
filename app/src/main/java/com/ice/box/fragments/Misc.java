package com.ice.box.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.util.Log;

import com.ice.box.R;
import com.ice.box.helpers.RootUtils;
import com.ice.box.helpers.SeekDialog;
import com.ice.box.helpers.TweaksHelper;




@SuppressWarnings("unused")
public class Misc extends PreferenceFragment implements
        Preference.OnPreferenceClickListener, Preference.OnPreferenceChangeListener {

    public static SharedPreferences user_prefs;
    SeekDialog seekDialog;
    TweaksHelper tweaksHelper;
    Preference filterPref;
    ListPreference iLocation;
    //private int mThemeId = R.style.ThemeLight;

    public void onCreate(Bundle savedInstanceState) {
        SwitchPreference checkPref;
        super.onCreate(savedInstanceState);
        user_prefs = getContext().getSharedPreferences("user_prefs", 0);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());;
        addPreferencesFromResource(R.xml.misc_preference);
        tweaksHelper = new TweaksHelper(this.getContext());
        seekDialog = new SeekDialog(this.getContext());
        boolean checked;

/*
        mThemeId = sharedPref.getInt("THEMEID", mThemeId);
        if (mThemeId == R.style.ThemeDark) {
            filterPref = findPreference("misc");
            filterPref.setLayoutResource(R.layout.misc_dark);
        }
*/

        filterPref = findPreference("tweaks_install_location");
        filterPref.setOnPreferenceChangeListener(this);
        iLocation = (ListPreference) findPreference("tweaks_install_location");

        filterPref = findPreference("tweaks_usb_wake");
        filterPref.setOnPreferenceClickListener(this);
        checkPref = (SwitchPreference) findPreference("tweaks_usb_wake");
        checked = (Settings.System.getInt(this.getContext().getContentResolver(),
                "tweaks_usb_wake", 1) == 1);
        checkPref.setChecked(checked);

        filterPref = findPreference("csc_selection");
        filterPref.setOnPreferenceClickListener(this);
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if (preference.getKey().equals("tweaks_usb_wake")) {
            boolean checked = ((SwitchPreference) preference).isChecked();
            int value = (checked ? 1 : 0);
            try {
                Settings.System.putInt(this.getContext().getContentResolver(),
                        "tweaks_usb_wake", value);
                Log.d(this.getClass().getName(), "Successful!");
            } catch (Exception e) {
                tweaksHelper.MakeToast(getResources().getString(R.string.error_write_settings));
            }
        }
        if (preference.getKey().equals("csc_selection")) {
            tweaksHelper.createCSCNotification();
        }
        return false;
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == iLocation) {
            int index = 0;
            CharSequence[] entries = ((ListPreference) preference).getEntryValues();
            for (int i = 0; i < entries.length; i++) {
                if (entries[i].equals(newValue)) {
                    index = i;
                    break;
                }
            }
            int newint = Integer.parseInt(entries[index] + "");
            try {
                Settings.System.putInt(
                        this.getContext().getContentResolver(),
                        "tweaks_install_location",
                        newint);
                Log.d(this.getClass().getName(), "Successful!");
            } catch (Exception e) {
                tweaksHelper.MakeToast(getResources().getString(R.string.error_write_settings));
            }
            RootUtils.runCommand("pm set-install-location '" + newint + "'");
            return true;
        }

        return false;
    }
}