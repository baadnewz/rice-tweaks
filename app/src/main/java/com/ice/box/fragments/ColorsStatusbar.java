package com.ice.box.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.util.Log;

import com.ice.box.R;
import com.ice.box.helpers.SeekDialog;
import com.ice.box.helpers.TweaksHelper;

import static com.ice.box.helpers.Constants.StatusbarAirplaneModeColorKey;
import static com.ice.box.helpers.Constants.StatusbarBatteryColorKey;
import static com.ice.box.helpers.Constants.StatusbarBatteryPercentColorKey;
import static com.ice.box.helpers.Constants.StatusbarClockColorKey;
import static com.ice.box.helpers.Constants.StatusbarColorableBatteryIconKey;
import static com.ice.box.helpers.Constants.StatusbarIconColorKey;
import static com.ice.box.helpers.Constants.StatusbarMobileRoamingColorKey;
import static com.ice.box.helpers.Constants.StatusbarMobileSignalColorKey;
import static com.ice.box.helpers.Constants.StatusbarMobileTypeColorKey;
import static com.ice.box.helpers.Constants.StatusbarNotificationIconColorKey;
import static com.ice.box.helpers.Constants.StatusbarWifiSignalColorKey;
import static com.ice.box.helpers.Constants.linkStatusbarColorsKey;

/**
 * Created by Adrian on 25.07.2017.
 */

public class ColorsStatusbar extends PreferenceFragment implements Preference.OnPreferenceClickListener {

    SeekDialog seekDialog;
    TweaksHelper tweaksHelper;
    Preference filterPref;

    private int mThemeId = R.style.ThemeLight;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.colors_statusbar_preference);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());

        SwitchPreference checkPref;
        boolean checked;
        tweaksHelper = new TweaksHelper(this.getContext());
        seekDialog = new SeekDialog(this.getContext());
        mThemeId = sharedPref.getInt("THEMEID", mThemeId);

        findPreference(linkStatusbarColorsKey).setOnPreferenceClickListener(this);

        filterPref = findPreference(StatusbarColorableBatteryIconKey);
        filterPref.setOnPreferenceClickListener(this);
        checkPref = (SwitchPreference) findPreference(StatusbarColorableBatteryIconKey);
        checked = (Settings.System.getInt(this.getContext().getContentResolver(),
                StatusbarColorableBatteryIconKey, 0) == 1);
        checkPref.setChecked(checked);


    }
    public boolean onPreferenceClick(Preference preference) {
        if (preference.getKey().equals(linkStatusbarColorsKey)) {
            Integer linked_color = Settings.System.getInt(this.getContext()
                            .getContentResolver(), StatusbarClockColorKey, -1);
                try {
                Settings.System.putInt(this.getContext().getContentResolver(),
                        StatusbarIconColorKey, linked_color);
                Settings.System.putInt(this.getContext().getContentResolver(),
                        StatusbarNotificationIconColorKey, linked_color);
                Settings.System.putInt(this.getContext().getContentResolver(),
                        StatusbarBatteryPercentColorKey, linked_color);
                Settings.System.putInt(this.getContext().getContentResolver(),
                        StatusbarWifiSignalColorKey, linked_color);
                Settings.System.putInt(this.getContext().getContentResolver(),
                        StatusbarMobileSignalColorKey, linked_color);
                Settings.System.putInt(this.getContext().getContentResolver(),
                        StatusbarMobileTypeColorKey, linked_color);
                Settings.System.putInt(this.getContext().getContentResolver(),
                        StatusbarMobileRoamingColorKey, linked_color);
                Settings.System.putInt(this.getContext().getContentResolver(),
                        StatusbarAirplaneModeColorKey, linked_color);
                Settings.System.putInt(this.getContext().getContentResolver(),
                        StatusbarBatteryColorKey, linked_color);
                Log.d(this.getClass().getName(), "Successful!");
            } catch (Exception e) {
                tweaksHelper.MakeToast(getResources().getString(R.string.error_write_settings));
            }
        }
        if (preference.getKey().equals(StatusbarColorableBatteryIconKey)) {
            boolean checked = ((SwitchPreference) preference).isChecked();
            int value = (checked ? 1 : 0);
            try {
                Settings.System.putInt(
                        this.getContext().getContentResolver(),
                        StatusbarColorableBatteryIconKey,
                        value);
                Log.d(this.getClass().getName(), "Successful!");
                tweaksHelper.createRebootNotification();
            } catch (Exception e) {
                tweaksHelper.MakeToast(getResources().getString(R.string.error_write_settings));
            }
        }
        return false;
    }
}