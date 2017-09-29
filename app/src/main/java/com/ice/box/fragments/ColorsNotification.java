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

import static com.ice.box.helpers.Constants.tweaks_notif_footer_background_color;
import static com.ice.box.helpers.Constants.tweaks_notif_footer_text_color;
import static com.ice.box.helpers.Constants.tweaks_notification_background_color;
import static com.ice.box.helpers.Constants.tweaks_notification_summary_text_color;
import static com.ice.box.helpers.Constants.tweaks_notification_title_text_color;

/**
 * Created by Adrian on 25.07.2017.
 */

public class ColorsNotification  extends PreferenceFragment implements Preference.OnPreferenceClickListener {

    SeekDialog seekDialog;
    TweaksHelper tweaksHelper;

    private int mThemeId = R.style.ThemeLight;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.colors_notification_preference);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());

        tweaksHelper = new TweaksHelper(this.getContext());
        seekDialog = new SeekDialog(this.getContext());
        mThemeId = sharedPref.getInt("THEMEID", mThemeId);

        Preference tweaks_unlock_notification_colors = findPreference(
                "tweaks_unlock_notification_colors");
        tweaks_unlock_notification_colors.setOnPreferenceClickListener(this);
        ((SwitchPreference) tweaks_unlock_notification_colors)
                .setChecked(Settings.System.getInt(
                        this.getContext().getContentResolver(),
                        "tweaks_unlock_notification_colors", 0) == 1);

        Preference tweaks_allow_transparent_notifications = findPreference(
                "tweaks_allow_transparent_notifications");
        tweaks_allow_transparent_notifications.setOnPreferenceClickListener(this);
        ((SwitchPreference) tweaks_allow_transparent_notifications)
                .setChecked(Settings.System.getInt(this.getContext().getContentResolver(),
                        "tweaks_allow_transparent_notifications", 0) == 1);

        findPreference("tweaks_notif_stock_look").setOnPreferenceClickListener(this);

    }
    public boolean onPreferenceClick(Preference preference) {
        if (preference.getKey().equals("tweaks_unlock_notification_colors")) {
            boolean checked = ((SwitchPreference) preference).isChecked();
            int value = (checked ? 1 : 0);
            try {
                android.provider.Settings.System.putInt(
                        this.getContext().getContentResolver(),
                        "tweaks_unlock_notification_colors",
                        value);
                Log.d(this.getClass().getName(), "Successful!");
                tweaksHelper.createRebootNotification();
            } catch (Exception e) {
                tweaksHelper.MakeToast(getResources().getString(R.string.error_write_settings));
            }
        }
        if (preference.getKey().equals("tweaks_allow_transparent_notifications")) {
            boolean checked = ((SwitchPreference) preference).isChecked();
            int value = (checked ? 1 : 0);
            try {
                Settings.System.putInt(
                        this.getContext().getContentResolver(),
                        "tweaks_allow_transparent_notifications",
                        value);
                Log.d(this.getClass().getName(), "Successful!");
            } catch (Exception e) {
                tweaksHelper.MakeToast(getResources().getString(R.string.error_write_settings));
            }
        }
        if (preference.getKey().equals("tweaks_notif_stock_look")) {
            try {
                Settings.System.putInt(
                        this.getContext().getContentResolver(),
                        "tweaks_notification_background_color",
                        tweaks_notification_background_color);
                Settings.System.putInt(
                        this.getContext().getContentResolver(),
                        "tweaks_notif_footer_text_color",
                        tweaks_notif_footer_text_color);
                Settings.System.putInt(
                        this.getContext().getContentResolver(),
                        "tweaks_notif_footer_background_color",
                        tweaks_notif_footer_background_color);
                Settings.System.putInt(
                        this.getContext().getContentResolver(),
                        "tweaks_notification_summary_text_color",
                        tweaks_notification_summary_text_color);
                Settings.System.putInt(
                        this.getContext().getContentResolver(),
                        "tweaks_notification_title_text_color",
                        tweaks_notification_title_text_color);
                Log.d(this.getClass().getName(), "Successful!");
                tweaksHelper.createRebootNotification();
            } catch (Exception e) {
                tweaksHelper.MakeToast(getResources().getString(R.string.error_write_settings));
            }
        }
        return false;
    }
}
