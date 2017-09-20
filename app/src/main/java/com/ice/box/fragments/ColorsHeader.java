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

import static com.ice.box.helpers.Constants.tweaks_header_icon_color;
import static com.ice.box.helpers.Constants.tweaks_header_text_color;

/**
 * Created by Adrian on 25.07.2017.
 */

public class ColorsHeader extends PreferenceFragment implements Preference.OnPreferenceClickListener {

    SeekDialog seekDialog;
    TweaksHelper tweaksHelper;

    private int mThemeId = R.style.ThemeLight;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.colors_header_preference);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());

        tweaksHelper = new TweaksHelper(this.getContext());
        seekDialog = new SeekDialog(this.getContext());
        mThemeId = sharedPref.getInt("THEMEID", mThemeId);

        Preference tweaks_unlock_header_colors = findPreference(
                "tweaks_unlock_header_colors");
        tweaks_unlock_header_colors.setOnPreferenceClickListener(this);
        ((SwitchPreference) tweaks_unlock_header_colors)
                .setChecked(Settings.System.getInt(this.getContext().getContentResolver(),
                        "tweaks_unlock_header_colors", 0) == 1);

        findPreference("tweaks_header_stock_look").setOnPreferenceClickListener(this);


    }
    public boolean onPreferenceClick(Preference preference) {
        if (preference.getKey().equals("tweaks_unlock_header_colors")) {
            boolean checked = ((SwitchPreference) preference).isChecked();
            int value = (checked ? 1 : 0);
            try {
                Settings.System.putInt(
                        this.getContext().getContentResolver(),
                        "tweaks_unlock_header_colors",
                        value);
                Log.d(this.getClass().getName(), "Successful!");
                tweaksHelper.createRebootNotification();
            } catch (Exception e) {
                tweaksHelper.MakeToast(getResources().getString(R.string.error_write_settings));
            }
        }

        if (preference.getKey().equals("tweaks_header_stock_look")) {
            try {
                Settings.System.putInt(this.getContext().getContentResolver(),
                        "tweaks_header_text_color", tweaks_header_text_color);
                Settings.System.putInt(this.getContext().getContentResolver(),
                        "tweaks_header_icon_color", tweaks_header_icon_color);
                Log.d(this.getClass().getName(), "Successful!");
                tweaksHelper.createRebootNotification();
            } catch (Exception e) {
                tweaksHelper.MakeToast(getResources().getString(R.string.error_write_settings));
            }
        }
        return false;
    }
}