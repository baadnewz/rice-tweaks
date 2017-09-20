package com.ice.box.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.provider.*;
import android.provider.Settings;
import android.util.Log;

import com.ice.box.R;
import com.ice.box.helpers.SeekDialog;
import com.ice.box.helpers.TweaksHelper;

import static com.ice.box.helpers.Constants.tweaks_qs_background_color;
import static com.ice.box.helpers.Constants.tweaks_qs_divider_color;
import static com.ice.box.helpers.Constants.tweaks_qs_drag_handle_background_color;
import static com.ice.box.helpers.Constants.tweaks_qs_drag_handle_icon_color;
import static com.ice.box.helpers.Constants.tweaks_qs_icon_off_color;
import static com.ice.box.helpers.Constants.tweaks_qs_icon_on_color;
import static com.ice.box.helpers.Constants.tweaks_qs_slider_color;
import static com.ice.box.helpers.Constants.tweaks_qs_text_color;

/**
 * Created by Adrian on 25.07.2017.
 */

public class ColorsQuickSettings  extends PreferenceFragment implements Preference.OnPreferenceClickListener {

    SeekDialog seekDialog;
    TweaksHelper tweaksHelper;

    private int mThemeId = R.style.ThemeLight;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.colors_quicksettings_preference);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());

        tweaksHelper = new TweaksHelper(this.getContext());
        seekDialog = new SeekDialog(this.getContext());
        mThemeId = sharedPref.getInt("THEMEID", mThemeId);

        Preference tweaks_unlock_qs_colors = findPreference("tweaks_unlock_qs_colors");
        tweaks_unlock_qs_colors.setOnPreferenceClickListener(this);
        ((SwitchPreference) tweaks_unlock_qs_colors)
                .setChecked(android.provider.Settings.System.getInt(this.getContext().getContentResolver(),
                        "tweaks_unlock_qs_colors", 0) == 1);

        findPreference("tweaks_qs_stock_look").setOnPreferenceClickListener(this);
    }
    public boolean onPreferenceClick(Preference preference) {
        if (preference.getKey().equals("tweaks_unlock_qs_colors")) {
            boolean checked = ((SwitchPreference) preference).isChecked();
            int value = (checked ? 1 : 0);
            try {
                Settings.System.putInt(
                        this.getContext().getContentResolver(),
                        "tweaks_unlock_qs_colors",
                        value);
                Log.d(this.getClass().getName(), "Successful!");
                tweaksHelper.createRebootNotification();
            } catch (Exception e) {
                tweaksHelper.MakeToast(getResources().getString(R.string.error_write_settings));
            }
        }
        if (preference.getKey().equals("tweaks_qs_stock_look")) {
            try {
                Settings.System.putInt(this.getContext().getContentResolver(),
                        "tweaks_qs_background_color", tweaks_qs_background_color);
                Settings.System.putInt(this.getContext().getContentResolver(),
                        "tweaks_qs_icon_off_color", tweaks_qs_icon_off_color);
                Settings.System.putInt(this.getContext().getContentResolver(),
                        "tweaks_qs_icon_on_color", tweaks_qs_icon_on_color);
                Settings.System.putInt(this.getContext().getContentResolver(),
                        "tweaks_qs_icon_off_color", tweaks_qs_icon_off_color);
                Settings.System.putInt(this.getContext().getContentResolver(),
                        "tweaks_qs_text_color", tweaks_qs_text_color);
                Settings.System.putInt(this.getContext().getContentResolver(),
                        "tweaks_qs_divider_color", tweaks_qs_divider_color);
                Settings.System.putInt(this.getContext().getContentResolver(),
                        "tweaks_qs_drag_handle_background_color",
                        tweaks_qs_drag_handle_background_color);
                Settings.System.putInt(this.getContext().getContentResolver(),
                        "tweaks_qs_drag_handle_icon_color", tweaks_qs_drag_handle_icon_color);
                Settings.System.putInt(this.getContext().getContentResolver(),
                        "tweaks_qs_slider_color", tweaks_qs_slider_color);
                Log.d(this.getClass().getName(), "Successful!");
                tweaksHelper.createRebootNotification();
            } catch (Exception e) {
                tweaksHelper.MakeToast(getResources().getString(R.string.error_write_settings));
            }
        }
        return false;
    }
}
