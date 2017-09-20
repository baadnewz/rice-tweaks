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
import com.ice.box.helpers.RootUtils;
import com.ice.box.helpers.SeekDialog;
import com.ice.box.helpers.Tools;
import com.ice.box.helpers.TweaksHelper;

import static com.ice.box.helpers.Constants.riceColorsFile;
import static com.ice.box.helpers.Constants.riceInternalStorageFolder;
import static com.ice.box.helpers.Constants.systemSettingsPath;
import static com.ice.box.helpers.Constants.tweaks_header_icon_color;
import static com.ice.box.helpers.Constants.tweaks_header_text_color;
import static com.ice.box.helpers.Constants.tweaks_navbar_icon_color;
import static com.ice.box.helpers.Constants.tweaks_notif_footer_background_color;
import static com.ice.box.helpers.Constants.tweaks_notif_footer_text_color;
import static com.ice.box.helpers.Constants.tweaks_notification_background_color;
import static com.ice.box.helpers.Constants.tweaks_notification_summary_text_color;
import static com.ice.box.helpers.Constants.tweaks_notification_title_text_color;
import static com.ice.box.helpers.Constants.tweaks_qs_background_color;
import static com.ice.box.helpers.Constants.tweaks_qs_divider_color;
import static com.ice.box.helpers.Constants.tweaks_qs_drag_handle_background_color;
import static com.ice.box.helpers.Constants.tweaks_qs_drag_handle_icon_color;
import static com.ice.box.helpers.Constants.tweaks_qs_icon_off_color;
import static com.ice.box.helpers.Constants.tweaks_qs_icon_on_color;
import static com.ice.box.helpers.Constants.tweaks_qs_slider_color;
import static com.ice.box.helpers.Constants.tweaks_qs_text_color;

@SuppressWarnings("unused")
public class Colors extends PreferenceFragment implements Preference.OnPreferenceClickListener {

    SeekDialog seekDialog;
    TweaksHelper tweaksHelper;

    private int mThemeId = R.style.ThemeLight;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.colors_preference);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());;

        tweaksHelper = new TweaksHelper(this.getContext());
        seekDialog = new SeekDialog(this.getContext());
        mThemeId = sharedPref.getInt("THEMEID", mThemeId);
        if (mThemeId == R.style.ThemeDark) {
            Preference colors = findPreference("colors");
            colors.setLayoutResource(R.layout.colors_dark);
        }

        Preference tweaks_unlock_navbar_colors = findPreference(
                "tweaks_unlock_navbar_colors");
        tweaks_unlock_navbar_colors.setOnPreferenceClickListener(this);
        ((SwitchPreference) tweaks_unlock_navbar_colors)
                .setChecked(Settings.System.getInt(this.getContext().getContentResolver(),
                        "tweaks_unlock_navbar_colors", 0) == 1);

        Preference tweaks_dynamic_navbar = findPreference(
                "tweaks_dynamic_navbar");
        tweaks_dynamic_navbar.setOnPreferenceClickListener(this);
        ((SwitchPreference) tweaks_dynamic_navbar)
                .setChecked(Settings.System.getInt(this.getContext().getContentResolver(),
                        "tweaks_dynamic_navbar", 0) == 1);

        Preference tweaks_unlock_header_colors = findPreference(
                "tweaks_unlock_header_colors");
        tweaks_unlock_header_colors.setOnPreferenceClickListener(this);
        ((SwitchPreference) tweaks_unlock_header_colors)
                .setChecked(Settings.System.getInt(this.getContext().getContentResolver(),
                        "tweaks_unlock_header_colors", 0) == 1);

        Preference tweaks_unlock_qs_colors = findPreference("tweaks_unlock_qs_colors");
        tweaks_unlock_qs_colors.setOnPreferenceClickListener(this);
        ((SwitchPreference) tweaks_unlock_qs_colors)
                .setChecked(Settings.System.getInt(this.getContext().getContentResolver(),
                        "tweaks_unlock_qs_colors", 0) == 1);

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

        findPreference("tweaks_stock_look").setOnPreferenceClickListener(this);

        findPreference("tweaks_header_stock_look").setOnPreferenceClickListener(this);

        findPreference("tweaks_qs_stock_look").setOnPreferenceClickListener(this);

        findPreference("tweaks_notif_stock_look").setOnPreferenceClickListener(this);

        findPreference("tweaks_backup_colors").setOnPreferenceClickListener(this);

        findPreference("tweaks_restore_colors").setOnPreferenceClickListener(this);

    }

    @Override
    public boolean onPreferenceClick(Preference preference) {

        if (preference.getKey().equals("tweaks_stock_look")) {
            try {
                Settings.System.putInt(
                        this.getContext().getContentResolver(),
                        "tweaks_navbar_icon_color",
                        tweaks_navbar_icon_color);
                Settings.System.putInt(
                        this.getContext().getContentResolver(),
                        "tweaks_notification_background_color",
                        tweaks_notification_background_color);
                Settings.System.putInt(
                        this.getContext().getContentResolver(),
                        "tweaks_header_text_color",
                        tweaks_header_text_color);
                Settings.System.putInt(
                        this.getContext().getContentResolver(),
                        "tweaks_header_icon_color",
                        tweaks_header_icon_color);
                Settings.System.putInt(
                        this.getContext().getContentResolver(),
                        "tweaks_qs_background_color",
                        tweaks_qs_background_color);
                Settings.System.putInt(
                        this.getContext().getContentResolver(),
                        "tweaks_notification_title_text_color",
                        tweaks_notification_title_text_color);
                Settings.System.putInt(
                        this.getContext().getContentResolver(),
                        "tweaks_notification_summary_text_color",
                        tweaks_notification_summary_text_color);
                Settings.System.putInt(
                        this.getContext().getContentResolver(),
                        "tweaks_qs_text_color",
                        tweaks_qs_text_color);
                Settings.System.putInt(
                        this.getContext().getContentResolver(),
                        "tweaks_qs_divider_color",
                        tweaks_qs_divider_color);
                Settings.System.putInt(
                        this.getContext().getContentResolver(),
                        "tweaks_qs_icon_on_color",
                        tweaks_qs_icon_on_color);
                Settings.System.putInt(
                        this.getContext().getContentResolver(),
                        "tweaks_qs_icon_off_color",
                        tweaks_qs_icon_off_color);
                Settings.System.putInt(
                        this.getContext().getContentResolver(),
                        "tweaks_qs_drag_handle_background_color",
                        tweaks_qs_drag_handle_background_color);
                Settings.System.putInt(
                        this.getContext().getContentResolver(),
                        "tweaks_qs_drag_handle_icon_color",
                        tweaks_qs_drag_handle_icon_color);
                Settings.System.putInt(
                        this.getContext().getContentResolver(),
                        "tweaks_qs_slider_color",
                        tweaks_qs_slider_color);
                Settings.System.putInt(
                        this.getContext().getContentResolver(),
                        "tweaks_notif_footer_text_color",
                        tweaks_notif_footer_text_color);
                Settings.System.putInt(
                        this.getContext().getContentResolver(),
                        "tweaks_notif_footer_background_color",
                        tweaks_notif_footer_background_color);
                Log.d(this.getClass().getName(), "Successful!");
                tweaksHelper.createRebootNotification();
            } catch (Exception e) {
                tweaksHelper.MakeToast(getResources().getString(R.string.error_write_settings));
            }
        }
        if (preference.getKey().equals("tweaks_unlock_navbar_colors")) {
            boolean checked = ((SwitchPreference) preference).isChecked();
            int value = (checked ? 1 : 0);
            try {
                Settings.System.putInt(
                        this.getContext().getContentResolver(),
                        "tweaks_unlock_navbar_colors",
                        value);
                Log.d(this.getClass().getName(), "Successful!");
                tweaksHelper.createRebootNotification();
            } catch (Exception e) {
                tweaksHelper.MakeToast(getResources().getString(R.string.error_write_settings));
            }
        }
        if (preference.getKey().equals("tweaks_dynamic_navbar")) {
            boolean checked = ((SwitchPreference) preference).isChecked();
            int value = (checked ? 1 : 0);
            try {
                Settings.System.putInt(
                        this.getContext().getContentResolver(),
                        "tweaks_dynamic_navbar",
                        value);
                Log.d(this.getClass().getName(), "Successful!");
            } catch (Exception e) {
                tweaksHelper.MakeToast(getResources().getString(R.string.error_write_settings));
            }
        }
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
        if (preference.getKey().equals("tweaks_unlock_notification_colors")) {
            boolean checked = ((SwitchPreference) preference).isChecked();
            int value = (checked ? 1 : 0);
            try {
                Settings.System.putInt(
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
        if (preference.getKey().equals("tweaks_backup_colors")) {
            if (!Tools.existFile(riceInternalStorageFolder, true)) {
                RootUtils.runCommand("mkdir " + riceInternalStorageFolder);
            }
            try {
                RootUtils.backupColors(getContext(), systemSettingsPath,
                        riceInternalStorageFolder + "/" + riceColorsFile);
                tweaksHelper.createBackupSuccessNotification();
            } catch (Exception e) {
                tweaksHelper.createBackupFailedNotification();
            }

        }
        if (preference.getKey().equals("tweaks_restore_colors")) {
            if (Tools.existFile(riceInternalStorageFolder, true)) {
                try {
                    RootUtils.restoreColors(getContext(), riceInternalStorageFolder + "/" +
                            riceColorsFile);
                    tweaksHelper.createRestoreSuccessNotification();
                } catch (Exception e) {
                    tweaksHelper.createRestoreFailedNotification();
                }
            } else {
                tweaksHelper.createRestoreFailedNotification();
            }
        }
        return false;
    }
}