package com.ice.box.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
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

/**
 * Created by Adrian on 25.07.2017.
 */

public class ColorsBackupRestore  extends PreferenceFragment implements Preference.OnPreferenceClickListener {

    SeekDialog seekDialog;
    TweaksHelper tweaksHelper;

    private int mThemeId = R.style.ThemeLight;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.colors_backuprestore_preference);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
        ;

        tweaksHelper = new TweaksHelper(this.getContext());
        seekDialog = new SeekDialog(this.getContext());

        mThemeId = sharedPref.getInt("THEMEID", mThemeId);
/*        if (mThemeId == R.style.ThemeDark) {
            Preference colors = findPreference("colors");
            colors.setLayoutResource(R.layout.colors_dark);
        }*/

        findPreference("tweaks_backup_colors").setOnPreferenceClickListener(this);

        findPreference("tweaks_restore_colors").setOnPreferenceClickListener(this);

        findPreference("tweaks_stock_look").setOnPreferenceClickListener(this);

    }
    public boolean onPreferenceClick(Preference preference) {
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

        if (preference.getKey().equals("tweaks_stock_look")) {
            try {
                android.provider.Settings.System.putInt(
                        this.getContext().getContentResolver(),
                        "tweaks_navbar_icon_color",
                        tweaks_navbar_icon_color);
                android.provider.Settings.System.putInt(
                        this.getContext().getContentResolver(),
                        "tweaks_notification_background_color",
                        tweaks_notification_background_color);
                android.provider.Settings.System.putInt(
                        this.getContext().getContentResolver(),
                        "tweaks_header_text_color",
                        tweaks_header_text_color);
                android.provider.Settings.System.putInt(
                        this.getContext().getContentResolver(),
                        "tweaks_header_icon_color",
                        tweaks_header_icon_color);
                android.provider.Settings.System.putInt(
                        this.getContext().getContentResolver(),
                        "tweaks_qs_background_color",
                        tweaks_qs_background_color);
                android.provider.Settings.System.putInt(
                        this.getContext().getContentResolver(),
                        "tweaks_notification_title_text_color",
                        tweaks_notification_title_text_color);
                android.provider.Settings.System.putInt(
                        this.getContext().getContentResolver(),
                        "tweaks_notification_summary_text_color",
                        tweaks_notification_summary_text_color);
                android.provider.Settings.System.putInt(
                        this.getContext().getContentResolver(),
                        "tweaks_qs_text_color",
                        tweaks_qs_text_color);
                android.provider.Settings.System.putInt(
                        this.getContext().getContentResolver(),
                        "tweaks_qs_divider_color",
                        tweaks_qs_divider_color);
                android.provider.Settings.System.putInt(
                        this.getContext().getContentResolver(),
                        "tweaks_qs_icon_on_color",
                        tweaks_qs_icon_on_color);
                android.provider.Settings.System.putInt(
                        this.getContext().getContentResolver(),
                        "tweaks_qs_icon_off_color",
                        tweaks_qs_icon_off_color);
                android.provider.Settings.System.putInt(
                        this.getContext().getContentResolver(),
                        "tweaks_qs_drag_handle_background_color",
                        tweaks_qs_drag_handle_background_color);
                android.provider.Settings.System.putInt(
                        this.getContext().getContentResolver(),
                        "tweaks_qs_drag_handle_icon_color",
                        tweaks_qs_drag_handle_icon_color);
                android.provider.Settings.System.putInt(
                        this.getContext().getContentResolver(),
                        "tweaks_qs_slider_color",
                        tweaks_qs_slider_color);
                android.provider.Settings.System.putInt(
                        this.getContext().getContentResolver(),
                        "tweaks_notif_footer_text_color",
                        tweaks_notif_footer_text_color);
                android.provider.Settings.System.putInt(
                        this.getContext().getContentResolver(),
                        "tweaks_notif_footer_background_color",
                        tweaks_notif_footer_background_color);
                Log.d(this.getClass().getName(), "Successful!");
                tweaksHelper.createRebootNotification();
            } catch (Exception e) {
                tweaksHelper.MakeToast(getResources().getString(R.string.error_write_settings));
            }
        }

        return false;
    }
}
