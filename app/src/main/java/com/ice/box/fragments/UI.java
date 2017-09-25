package com.ice.box.fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.MultiSelectListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.ice.box.R;
import com.ice.box.helpers.RootUtils;
import com.ice.box.helpers.SeekDialog;
import com.ice.box.helpers.SystemProperties;
import com.ice.box.helpers.TweaksHelper;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static com.ice.box.helpers.Constants.DEBUGTAG;
import static com.ice.box.helpers.Constants.ShowNavbarKey;
import static com.ice.box.helpers.Constants.allowCustomNavBarHeightKey;
import static com.ice.box.helpers.Constants.backToKillKey;
import static com.ice.box.helpers.Constants.isFreeVersionKey;
import static com.ice.box.helpers.Constants.isNote8PortKey;
import static com.ice.box.helpers.Constants.navBarHeightKey;
import static com.ice.box.helpers.Constants.renovateFingerprintProp;


@SuppressWarnings("unused")
public class UI extends PreferenceFragment implements Preference.OnPreferenceClickListener,
        Preference.OnPreferenceChangeListener {

    private SeekDialog seekDialog;
    private TweaksHelper tweaksHelper;
    private boolean isFreeVersion;
    private ListPreference mRecentKey;
    private ListPreference mWindowAnimation;
    private ListPreference mTransititionAnimation;
    private ListPreference mAnimatorDuration;
    private ListPreference mClockPos;
    private ListPreference mQsButtons;
    private ListPreference mQsRow;
    private ListPreference mQsColumn;
    private ListPreference mBatthickness;
    private ListPreference mImmersiveList;
    private MultiSelectListPreference immersivePerAppList;

    //Nav bar height SeekDialog
    private final int navBarHeightSeekMax = 200;
    private SeekBar navBarHeightSeek;
    private TextView navBarHeightValue;
    private AlertDialog navBarHeightDialog;
    private int navBarHeightProgress;

    //private int mThemeId = R.style.ThemeLight;

    //Plus button
    private final Button.OnClickListener navBarHeightPlusListener = new Button.OnClickListener() {
        @SuppressLint("SetTextI18n")
        @Override
        public void onClick(View v) {
            // So the value can't go too high
            if (navBarHeightProgress != navBarHeightSeekMax) {
                navBarHeightProgress++;
                navBarHeightValue.setText(getResources().getString(R.string.tweaks_navbar_height_title) + ": " + String.valueOf(navBarHeightProgress));
                navBarHeightSeek.setProgress(navBarHeightProgress);
            }
        }
    };
    //Minus button
    private final Button.OnClickListener navBarHeightMinusListener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            // So the value can't go too low
            int navBarHeightSeekMin = 50;
            if (navBarHeightProgress != navBarHeightSeekMin) {
                navBarHeightProgress--;
                navBarHeightValue.setText(getResources().getString(R.string.tweaks_navbar_height_title) + ": " + String.valueOf(navBarHeightProgress));
                navBarHeightSeek.setProgress(navBarHeightProgress);
            }
        }
    };
    private final Button.OnClickListener navBarHeightApplyListener = new Button.OnClickListener() {
        public void onClick(View v) {
            int j = navBarHeightProgress;
            try {
                Settings.System.putInt(getContext().getContentResolver(),
                        navBarHeightKey, j);
                Log.d(this.getClass().getName(), "Successful!");
            } catch (Exception e) {
                tweaksHelper.MakeToast(getResources().getString(R.string.error_write_settings));
            }
            tweaksHelper.MakeToast(getResources().getString(R.string.tweaks_navbar_height_title) + ": " + String.valueOf(navBarHeightProgress));
            navBarHeightDialog.dismiss();
            Log.d(this.getClass().getName(), "Successful!" + j);

        }
    };
    //Listener
    private final SeekBar.OnSeekBarChangeListener navBarHeightSeekListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            // This what you do when the slider is changing
            navBarHeightValue.setText(getResources().getString(R.string.tweaks_navbar_height_title) + ": " + String.valueOf(progress));
            navBarHeightProgress = progress;
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
        }
    };

    //END NAV BAR HEIGHT SEEKBAR

    public void onCreate(Bundle savedInstanceState) {
        SwitchPreference checkPref;
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.ui_preference);
        Preference filterPref;
/*        mThemeId = sharedPref.getInt("THEMEID", mThemeId);
        if (mThemeId == R.style.ThemeDark) {
            filterPref = findPreference("ui");
            filterPref.setLayoutResource(R.layout.ui_dark);
        }*/
        isFreeVersion = sharedPref.getBoolean(isFreeVersionKey, true);
        boolean isGalaxyS8 = sharedPref.getBoolean("isGalaxyS8", false);
        boolean isGalaxyS7 = sharedPref.getBoolean("isGalaxyS7", false);
        boolean isNotePort = sharedPref.getBoolean(isNote8PortKey, false);

        tweaksHelper = new TweaksHelper(this.getContext());
        seekDialog = new SeekDialog(this.getContext());

        filterPref = findPreference("tweaks_immersive_list");
        filterPref.setOnPreferenceChangeListener(this);
        mImmersiveList = (ListPreference) findPreference("tweaks_immersive_list");
        String ImmersiveListValue = Settings.Global.getString(this.getContext().getContentResolver(),
                "policy_control");
        if (isFreeVersion) {
            CharSequence[] entries = { getString(R.string.immersive_global_off), getString(R.string.immersive_global_on) };
            CharSequence[] entryValues = {"immersive.full=" , "immersive.full=*"};
            mImmersiveList.setEntries(entries);
            mImmersiveList.setEntryValues(entryValues);
            try {
                if (ImmersiveListValue == null || ImmersiveListValue.equals("immersive.full=")) {
                    mImmersiveList.setValueIndex(0);
                } else if (ImmersiveListValue.equals("immersive.full=*")) {
                    mImmersiveList.setValueIndex(1);
                }
            } catch (ArrayIndexOutOfBoundsException exception) {
                Log.d(DEBUGTAG, "OutOfBoundsException: " + mImmersiveList);
            }

        } else {
            try {
                if (ImmersiveListValue == null || ImmersiveListValue.equals("immersive.full=")) {
                    mImmersiveList.setValueIndex(0);
                } else if (ImmersiveListValue.equals("immersive.full=*")) {
                    mImmersiveList.setValueIndex(1);
                } else {
                    mImmersiveList.setValueIndex(2);
                }
            } catch (ArrayIndexOutOfBoundsException exception) {
                Log.d(DEBUGTAG, "OutOfBoundsException: " + mImmersiveList);
            }
        }


        filterPref = findPreference("immersive_perapp");
        filterPref.setOnPreferenceChangeListener(this);
        immersivePerAppList = (MultiSelectListPreference) findPreference("immersive_perapp");
        if (isFreeVersion) {
            immersivePerAppList.setEnabled(false);
            immersivePerAppList.setSelectable(false);
            immersivePerAppList.setSummary(getResources().getString(R.string.free_notification2));
        }
        if (ImmersiveListValue == null || ImmersiveListValue.isEmpty() || ImmersiveListValue.equals("immersive.full=") || ImmersiveListValue.equals("immersive.full=*")) {
            immersivePerAppList.setEnabled(false);
            immersivePerAppList.setSelectable(false);
            if (isFreeVersion) {
                immersivePerAppList.setSummary(R.string.free_notification2);
            } else {
                immersivePerAppList.setSummary(R.string.immersive_perapp_hint);
            }
        }
        new Thread() {
            public void run() {
                PackageManager pm = getContext().getPackageManager();
                List<ApplicationInfo> appListInfo = pm.getInstalledApplications(0);
                Collections.sort(appListInfo, new ApplicationInfo.DisplayNameComparator(pm));
                CharSequence[] entries = new CharSequence[appListInfo.size()];
                CharSequence[] entryValues = new CharSequence[appListInfo.size()];
                try {
                    int i = 0;
                    for (ApplicationInfo p : appListInfo) {
                        entries[i] = p.loadLabel(pm);
                        entryValues[i] = p.packageName;
                        i++;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                immersivePerAppList.setEntries(entries);
                immersivePerAppList.setEntryValues(entryValues);
                immersivePerAppList.setNegativeButtonText(R.string.cancel);
                immersivePerAppList.setPositiveButtonText(R.string.choose);
                //immersivePerAppList.setDialogIcon(R.mipmap.ic_launcher_white);
                immersivePerAppList.setDialogTitle(R.string.immersive_perapp_summary);
            }
        }.start();

        filterPref = findPreference("tweaks_fingerprint_unlock");
        filterPref.setOnPreferenceClickListener(this);
        checkPref = (SwitchPreference) findPreference("tweaks_fingerprint_unlock");
        String Finger = SystemProperties.get(renovateFingerprintProp);

        if (Finger == null || Finger.contains("0"))
            checkPref.setChecked(false);
        else
            checkPref.setChecked(true);

        assert Finger != null;
        if (Finger.equals("1"))
            checkPref.setChecked(true);
        else
            checkPref.setChecked(false);


        filterPref = findPreference("tweaks_all_rotations");
        filterPref.setOnPreferenceClickListener(this);
        checkPref = (SwitchPreference) findPreference("tweaks_all_rotations");
        boolean checked = (Settings.System.getInt(this.getContext().getContentResolver(),
                "tweaks_all_rotations", 0) == 1);
        checkPref.setChecked(checked);

        filterPref = findPreference(backToKillKey);
        filterPref.setOnPreferenceClickListener(this);
        checkPref = (SwitchPreference) findPreference(backToKillKey);
        checked = (Settings.System.getInt(this.getContext().getContentResolver(),
                backToKillKey, 0) == 1);
        checkPref.setChecked(checked);
        if (isNotePort) {
            getPreferenceScreen().removePreference(findPreference(backToKillKey));
        }


        filterPref = findPreference("tweaks_disable_volume_warning");
        filterPref.setOnPreferenceClickListener(this);
        checkPref = (SwitchPreference) findPreference("tweaks_disable_volume_warning");
        checked = (Settings.System.getInt(this.getContext().getContentResolver(),
                "tweaks_disable_volume_warning", 0) == 0);
        checkPref.setChecked(checked);

        filterPref = findPreference("tweaks_hide_brightness_warning");
        filterPref.setOnPreferenceClickListener(this);
        checkPref = (SwitchPreference) findPreference("tweaks_hide_brightness_warning");
        checked = (Settings.System.getInt(this.getContext().getContentResolver(),
                "tweaks_hide_brightness_warning", 0) == 0);
        checkPref.setChecked(checked);

        filterPref = findPreference("tweaks_clock_onclick");
        filterPref.setOnPreferenceClickListener(this);
        checkPref = (SwitchPreference) findPreference("tweaks_clock_onclick");
        checked = (Settings.System.getInt(this.getContext().getContentResolver(),
                "tweaks_clock_onclick", 0) == 1);
        checkPref.setChecked(checked);

        filterPref = findPreference("tweaks_hide_battery");
        filterPref.setOnPreferenceClickListener(this);
        checkPref = (SwitchPreference) findPreference("tweaks_hide_battery");
        checked = (Settings.System.getInt(this.getContext().getContentResolver(),
                "tweaks_hide_battery", 0) == 1);
        checkPref.setChecked(checked);

        filterPref = findPreference("tweaks_double_tap_sleep");
        filterPref.setOnPreferenceClickListener(this);
        checkPref = (SwitchPreference) findPreference("tweaks_double_tap_sleep");
        checked = (Settings.System.getInt(this.getContext().getContentResolver(),
                "tweaks_double_tap_sleep", 0) == 1);
        checkPref.setChecked(checked);

        filterPref = findPreference("tweaks_hide_brightness_slider");
        filterPref.setOnPreferenceClickListener(this);
        checkPref = (SwitchPreference) findPreference("tweaks_hide_brightness_slider");
        checked = (Settings.System.getInt(this.getContext().getContentResolver(),
                "tweaks_hide_brightness_slider", 0) == 1);
        checkPref.setChecked(checked);

        filterPref = findPreference("tweaks_qs_lock");
        filterPref.setOnPreferenceClickListener(this);
        checkPref = (SwitchPreference) findPreference("tweaks_qs_lock");
        checked = (Settings.System.getInt(this.getContext().getContentResolver(),
                "tweaks_qs_lock", 0) == 1);
        checkPref.setChecked(checked);

        filterPref = findPreference("tweaks_qs_override_lockscreen");
        filterPref.setOnPreferenceClickListener(this);
        checkPref = (SwitchPreference) findPreference("tweaks_qs_override_lockscreen");
        checked = (Settings.System.getInt(this.getContext().getContentResolver(),
                "tweaks_qs_override_lockscreen", 0) == 1);
        checkPref.setChecked(checked);

        filterPref = findPreference("tweaks_qs_pulldown");
        filterPref.setOnPreferenceClickListener(this);
        checkPref = (SwitchPreference) findPreference("tweaks_qs_pulldown");
        checked = (Settings.System.getInt(this.getContext().getContentResolver(),
                "tweaks_qs_pulldown", 1) == 1);
        checkPref.setChecked(checked);

        filterPref = findPreference("tweaks_pulldown_blur");
        filterPref.setOnPreferenceClickListener(this);
        checkPref = (SwitchPreference) findPreference("tweaks_pulldown_blur");
        checked = (Settings.System.getInt(this.getContext().getContentResolver(),
                "tweaks_pulldown_blur", 0) == 1);
        checkPref.setChecked(checked);

        filterPref = findPreference("tweaks_disable_persistent_notifications");
        filterPref.setOnPreferenceClickListener(this);
        checkPref = (SwitchPreference) findPreference("tweaks_disable_persistent_notifications");
        checked = (Settings.System.getInt(this.getContext().getContentResolver(),
                "tweaks_disable_persistent_notifications", 0) == 1);
        checkPref.setChecked(checked);

        filterPref = findPreference("tweaks_hide_keyboard_switcher");
        filterPref.setOnPreferenceClickListener(this);
        checkPref = (SwitchPreference) findPreference("tweaks_hide_keyboard_switcher");
        checked = (Settings.System.getInt(this.getContext().getContentResolver(),
                "tweaks_hide_keyboard_switcher", 0) == 1);
        checkPref.setChecked(checked);

        filterPref = findPreference("battery_bar");
        filterPref.setOnPreferenceClickListener(this);
        checkPref = (SwitchPreference) findPreference("battery_bar");
        checked = (Settings.System.getInt(this.getContext().getContentResolver(), "battery_bar",
                0) == 1);
        checkPref.setChecked(checked);

        filterPref = findPreference("battery_bar_animate");
        filterPref.setOnPreferenceClickListener(this);
        checkPref = (SwitchPreference) findPreference("battery_bar_animate");
        checked = (Settings.System.getInt(this.getContext().getContentResolver(),
                "battery_bar_animate", 0) == 1);
        checkPref.setChecked(checked);

        filterPref = findPreference("battery_bar_style");
        filterPref.setOnPreferenceClickListener(this);
        checkPref = (SwitchPreference) findPreference("battery_bar_style");
        checked = (Settings.System.getInt(this.getContext().getContentResolver(),
                "battery_bar_style", 0) == 1);
        checkPref.setChecked(checked);

        filterPref = findPreference("tweaks_show_multi_user");
        filterPref.setOnPreferenceClickListener(this);
        checkPref = (SwitchPreference) findPreference("tweaks_show_multi_user");
        checked = (Settings.System.getInt(this.getContext().getContentResolver(),
                "tweaks_show_multi_user", 0) == 1);
        checkPref.setChecked(checked);

        filterPref = findPreference("tweaks_recent_key_config");
        filterPref.setOnPreferenceChangeListener(this);
        if (isGalaxyS8)
            getPreferenceScreen().removePreference(findPreference("tweaks_recent_key_config"));

        filterPref = findPreference("window_animation_scale");
        filterPref.setOnPreferenceChangeListener(this);

        filterPref = findPreference("transition_animation_scale");
        filterPref.setOnPreferenceChangeListener(this);

        filterPref = findPreference("animator_duration_scale");
        filterPref.setOnPreferenceChangeListener(this);

        filterPref = findPreference("tweaks_clock_position");
        filterPref.setOnPreferenceChangeListener(this);

        filterPref = findPreference("tweaks_quick_qs_buttons");
        filterPref.setOnPreferenceChangeListener(this);

        filterPref = findPreference("qs_tile_row");
        filterPref.setOnPreferenceChangeListener(this);

        filterPref = findPreference("qs_tile_column");
        filterPref.setOnPreferenceChangeListener(this);

        filterPref = findPreference("battery_bar_thickness");
        filterPref.setOnPreferenceChangeListener(this);

        mRecentKey = (ListPreference) findPreference("tweaks_recent_key_config");

        mWindowAnimation = (ListPreference) findPreference("window_animation_scale");
        String globalWindowValue = Settings.Global.getString(this.getContext().getContentResolver(),
                "window_animation_scale");
        if (globalWindowValue == null || globalWindowValue.equals("0.0")) {
            mWindowAnimation.setValueIndex(4);
        } else if (globalWindowValue.equals("0.25")) {
            mWindowAnimation.setValueIndex(1);
        } else if (globalWindowValue.equals("0.5")) {
            mWindowAnimation.setValueIndex(2);
        } else if (globalWindowValue.equals("0.75")) {
            mWindowAnimation.setValueIndex(3);
        } else if (globalWindowValue.equals("1.0")) {
            mWindowAnimation.setValueIndex(4);
        } else if (globalWindowValue.equals("1.25")) {
            mWindowAnimation.setValueIndex(5);
        } else if (globalWindowValue.equals("1.5")) {
            mWindowAnimation.setValueIndex(6);
        } else if (globalWindowValue.equals("1.75")) {
            mWindowAnimation.setValueIndex(7);
        } else if (globalWindowValue.equals("2.0")) {
            mWindowAnimation.setValueIndex(8);
        } else if (globalWindowValue.equals("5.0")) {
            mWindowAnimation.setValueIndex(9);
        } else if (globalWindowValue.equals("10.0")) {
            mWindowAnimation.setValueIndex(10);
        } else {
            Log.d(DEBUGTAG, "missed: ");
        }

        mTransititionAnimation = (ListPreference) findPreference("transition_animation_scale");
        String globalTransitionValue = Settings.Global.getString(this.getContext().getContentResolver(),
                "transition_animation_scale");
        if (globalTransitionValue == null || globalTransitionValue.equals("0.0")) {
            mTransititionAnimation.setValueIndex(4);
        } else if (globalTransitionValue.equals("0.25")) {
            mTransititionAnimation.setValueIndex(1);
        } else if (globalTransitionValue.equals("0.5")) {
            mTransititionAnimation.setValueIndex(2);
        } else if (globalTransitionValue.equals("0.75")) {
            mTransititionAnimation.setValueIndex(3);
        } else if (globalTransitionValue.equals("1.0")) {
            mTransititionAnimation.setValueIndex(4);
        } else if (globalTransitionValue.equals("1.25")) {
            mTransititionAnimation.setValueIndex(5);
        } else if (globalTransitionValue.equals("1.5")) {
            mTransititionAnimation.setValueIndex(6);
        } else if (globalTransitionValue.equals("1.75")) {
            mTransititionAnimation.setValueIndex(7);
        } else if (globalTransitionValue.equals("2.0")) {
            mTransititionAnimation.setValueIndex(8);
        } else if (globalTransitionValue.equals("5.0")) {
            mTransititionAnimation.setValueIndex(9);
        } else if (globalTransitionValue.equals("10.0")) {
            mTransititionAnimation.setValueIndex(10);
        } else {
            Log.d(DEBUGTAG, "missed: ");
        }

        mAnimatorDuration = (ListPreference) findPreference("animator_duration_scale");
        String globalAnimationValue = Settings.Global.getString(this.getContext().getContentResolver(),
                "animator_duration_scale");
        if (globalAnimationValue == null || globalAnimationValue.equals("0.0")) {
            mAnimatorDuration.setValueIndex(4);
        } else if (globalAnimationValue.equals("0.25")) {
            mAnimatorDuration.setValueIndex(1);
        } else if (globalAnimationValue.equals("0.5")) {
            mAnimatorDuration.setValueIndex(2);
        } else if (globalAnimationValue.equals("0.75")) {
            mAnimatorDuration.setValueIndex(3);
        } else if (globalAnimationValue.equals("1.0")) {
            mAnimatorDuration.setValueIndex(4);
        } else if (globalAnimationValue.equals("1.25")) {
            mAnimatorDuration.setValueIndex(5);
        } else if (globalAnimationValue.equals("1.5")) {
            mAnimatorDuration.setValueIndex(6);
        } else if (globalAnimationValue.equals("1.75")) {
            mAnimatorDuration.setValueIndex(7);
        } else if (globalAnimationValue.equals("2.0")) {
            mAnimatorDuration.setValueIndex(8);
        } else if (globalAnimationValue.equals("5.0")) {
            mAnimatorDuration.setValueIndex(9);
        } else if (globalAnimationValue.equals("10.0")) {
            mAnimatorDuration.setValueIndex(10);
        } else {
            Log.d(DEBUGTAG, "missed: ");
        }

        mClockPos = (ListPreference) findPreference("tweaks_clock_position");
        String ClockPosValue = Settings.System.getString(this.getContext().getContentResolver(),
                "tweaks_clock_position");
        try {
            if (ClockPosValue == null || ClockPosValue.equals("0")) {
                mClockPos.setValueIndex(0);
            } else if (ClockPosValue.equals("1")) {
                mClockPos.setValueIndex(1);
            } else if (ClockPosValue.equals("2")) {
                mClockPos.setValueIndex(2);
            } else if (ClockPosValue.equals("3")) {
                mClockPos.setValueIndex(3);
            } else {
                Log.d(DEBUGTAG, "missed: ");
            }
        } catch (ArrayIndexOutOfBoundsException exception) {
            Log.d(DEBUGTAG, "OutOfBoundsException: " + mClockPos);
        }

        mQsButtons = (ListPreference) findPreference("tweaks_quick_qs_buttons");
        String QsButtonsValue = Settings.System.getString(this.getContext().getContentResolver(),
                "tweaks_quick_qs_buttons");
        if (QsButtonsValue == null || QsButtonsValue.equals("3")) {
            mQsButtons.setValueIndex(0);
        } else if (QsButtonsValue.equals("4")) {
            mQsButtons.setValueIndex(1);
        } else if (QsButtonsValue.equals("5")) {
            mQsButtons.setValueIndex(2);
        } else if (QsButtonsValue.equals("6")) {
            mQsButtons.setValueIndex(3);
        } else if (QsButtonsValue.equals("7")) {
            mQsButtons.setValueIndex(4);
        } else if (QsButtonsValue.equals("8")) {
            mQsButtons.setValueIndex(5);
        } else {
            Log.d(DEBUGTAG, "missed: ");
        }

        mQsRow = (ListPreference) findPreference("qs_tile_row");
        String QsRowValue = Settings.Secure.getString(this.getContext().getContentResolver(),
                "qs_tile_row");
        if (QsRowValue == null || QsRowValue.equals("2")) {
            mQsRow.setValueIndex(0);
        } else if (QsRowValue.equals("3")) {
            mQsRow.setValueIndex(1);
        } else if (QsRowValue.equals("4")) {
            mQsRow.setValueIndex(2);
        } else if (QsRowValue.equals("5")) {
            mQsRow.setValueIndex(3);
        } else if (QsRowValue.equals("6")) {
            mQsRow.setValueIndex(4);
        } else {
            Log.d(DEBUGTAG, "missed: ");
        }

        mQsColumn = (ListPreference) findPreference("qs_tile_column");
        String QsColumnValue = Settings.Secure.getString(this.getContext().getContentResolver(),
                "qs_tile_column");
        if (QsColumnValue == null || QsColumnValue.equals("2")) {
            mQsColumn.setValueIndex(0);
        } else if (QsColumnValue.equals("3")) {
            mQsColumn.setValueIndex(1);
        } else if (QsColumnValue.equals("4")) {
            mQsColumn.setValueIndex(2);
        } else if (QsColumnValue.equals("5")) {
            mQsColumn.setValueIndex(3);
        } else {
            Log.d(DEBUGTAG, "missed: ");
        }

        mBatthickness = (ListPreference) findPreference("battery_bar_thickness");
        String BatThicknessValue = Settings.System.getString(this.getContext().getContentResolver(),
                "battery_bar_thickness");
        if (BatThicknessValue == null || BatThicknessValue.equals("1")) {
            mBatthickness.setValueIndex(0);
        } else if (BatThicknessValue.equals("2")) {
            mBatthickness.setValueIndex(1);
        } else if (BatThicknessValue.equals("3")) {
            mBatthickness.setValueIndex(2);
        } else if (BatThicknessValue.equals("4")) {
            mBatthickness.setValueIndex(3);
        } else {
            Log.d(DEBUGTAG, "missed: ");
        }

        filterPref = findPreference(navBarHeightKey);
        filterPref.setOnPreferenceClickListener(this);

        filterPref = findPreference(allowCustomNavBarHeightKey);
        filterPref.setOnPreferenceClickListener(this);
        checkPref = (SwitchPreference) findPreference(allowCustomNavBarHeightKey);
        checked = (Settings.System.getInt(this.getContext().getContentResolver(),
                allowCustomNavBarHeightKey, 0) == 1);
        checkPref.setChecked(checked);

        filterPref = findPreference(ShowNavbarKey);
        filterPref.setOnPreferenceClickListener(this);
        checkPref = (SwitchPreference) findPreference(ShowNavbarKey);
        checked = (Settings.System.getInt(this.getContext().getContentResolver(),
                ShowNavbarKey, 0) == 1);
        checkPref.setChecked(checked);
        if (isGalaxyS8)
            getPreferenceScreen().removePreference(findPreference(ShowNavbarKey));
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if (preference.getKey().equals("tweaks_fingerprint_unlock")) {
            String newstring = TweaksHelper.returnSwitch(SystemProperties.get(renovateFingerprintProp));
            try {
                RootUtils.setProp(getContext(), renovateFingerprintProp, newstring);
                Log.d(this.getClass().getName(), "Successful!");
                tweaksHelper.createRebootNotification();
            } catch (Exception e) {
                tweaksHelper.MakeToast("Sorry..that didn't work..is Renovate ROM?");
            }
        }
        if (preference.getKey().equals("tweaks_all_rotations")) {
            boolean checked = ((SwitchPreference) preference).isChecked();
            int value = (checked ? 1 : 0);
            try {
                Settings.System.putInt(
                        this.getContext().getContentResolver(),
                        "tweaks_all_rotations",
                        value);
                Log.d(this.getClass().getName(), "Successful!");
                tweaksHelper.createRebootNotification();
            } catch (Exception e) {
                tweaksHelper.MakeToast(getResources().getString(R.string.error_write_settings));
            }
        }
        if (preference.getKey().equals(backToKillKey)) {
            boolean checked = ((SwitchPreference) preference).isChecked();
            int value = (checked ? 1 : 0);
            try {
                Settings.System.putInt(this.getContext().getContentResolver(),
                        backToKillKey, value);
                Log.d(this.getClass().getName(), "Successful!");
            } catch (Exception e) {
                tweaksHelper.MakeToast(getResources().getString(R.string.error_write_settings));
            }
        }
        if (preference.getKey().equals("tweaks_disable_volume_warning")) {
            boolean checked = ((SwitchPreference) preference).isChecked();
            int value = (checked ? 0 : 1);
            try {
                Settings.System.putInt(
                        this.getContext().getContentResolver(),
                        "tweaks_disable_volume_warning",
                        value);
                Log.d(this.getClass().getName(), "Successful!");
            } catch (Exception e) {
                tweaksHelper.MakeToast(getResources().getString(R.string.error_write_settings));
            }
        }
        if (preference.getKey().equals("tweaks_hide_brightness_warning")) {
            boolean checked = ((SwitchPreference) preference).isChecked();
            int value = (checked ? 0 : 1);
            try {
                Settings.System.putInt(
                        this.getContext().getContentResolver(),
                        "tweaks_hide_brightness_warning",
                        value);
                Log.d(this.getClass().getName(), "Successful!");
                tweaksHelper.createSystemUINotification();
            } catch (Exception e) {
                tweaksHelper.MakeToast(getResources().getString(R.string.error_write_settings));
            }
        }

        if (preference.getKey().equals("tweaks_clock_onclick")) {
            boolean checked = ((SwitchPreference) preference).isChecked();
            int value = (checked ? 1 : 0);
            try {
                Settings.System.putInt(
                        this.getContext().getContentResolver(),
                        "tweaks_clock_onclick",
                        value);
                Log.d(this.getClass().getName(), "Successful!");
                tweaksHelper.createRebootNotification();
            } catch (Exception e) {
                tweaksHelper.MakeToast(getResources().getString(R.string.error_write_settings));
            }
        }
        if (preference.getKey().equals("tweaks_hide_battery")) {
            boolean checked = ((SwitchPreference) preference).isChecked();
            int value = (checked ? 1 : 0);
            try {
                Settings.System.putInt(
                        this.getContext().getContentResolver(),
                        "tweaks_hide_battery",
                        value);
                Log.d(this.getClass().getName(), "Successful!");
            } catch (Exception e) {
                tweaksHelper.MakeToast(getResources().getString(R.string.error_write_settings));
            }
        }
        if (preference.getKey().equals("tweaks_double_tap_sleep")) {
            boolean checked = ((SwitchPreference) preference).isChecked();
            int value = (checked ? 1 : 0);
            try {
                Settings.System.putInt(
                        this.getContext().getContentResolver(),
                        "tweaks_double_tap_sleep",
                        value);
                Log.d(this.getClass().getName(), "Successful!");
            } catch (Exception e) {
                tweaksHelper.MakeToast(getResources().getString(R.string.error_write_settings));
            }
        }
        if (preference.getKey().equals("tweaks_hide_brightness_slider")) {
            boolean checked = ((SwitchPreference) preference).isChecked();
            int value = (checked ? 1 : 0);
            try {
                Settings.System.putInt(
                        this.getContext().getContentResolver(),
                        "tweaks_hide_brightness_slider",
                        value);
                Log.d(this.getClass().getName(), "Successful!");
                tweaksHelper.createRebootNotification();
            } catch (Exception e) {
                tweaksHelper.MakeToast(getResources().getString(R.string.error_write_settings));
            }
        }
        if (preference.getKey().equals("tweaks_qs_lock")) {
            boolean checked = ((SwitchPreference) preference).isChecked();
            int value = (checked ? 1 : 0);
            try {
                Settings.System.putInt(
                        this.getContext().getContentResolver(),
                        "tweaks_qs_lock",
                        value);
                Log.d(this.getClass().getName(), "Successful!");
            } catch (Exception e) {
                tweaksHelper.MakeToast(getResources().getString(R.string.error_write_settings));
            }
        }
        if (preference.getKey().equals("tweaks_qs_override_lockscreen")) {
            boolean checked = ((SwitchPreference) preference).isChecked();
            int value = (checked ? 1 : 0);
            try {
                Settings.System.putInt(
                        this.getContext().getContentResolver(),
                        "tweaks_qs_override_lockscreen",
                        value);
                Log.d(this.getClass().getName(), "Successful!");
                tweaksHelper.createSafetyNotification();
            } catch (Exception e) {
                tweaksHelper.MakeToast(getResources().getString(R.string.error_write_settings));
            }
        }
        if (preference.getKey().equals("tweaks_qs_pulldown")) {
            boolean checked = ((SwitchPreference) preference).isChecked();
            int value = (checked ? 1 : 0);
            try {
                Settings.System.putInt(
                        this.getContext().getContentResolver(),
                        "tweaks_qs_pulldown",
                        value);
                Log.d(this.getClass().getName(), "Successful!");
            } catch (Exception e) {
                tweaksHelper.MakeToast(getResources().getString(R.string.error_write_settings));
            }
        }
        if (preference.getKey().equals("tweaks_pulldown_blur")) {
            boolean checked = ((SwitchPreference) preference).isChecked();
            int value = (checked ? 1 : 0);
            try {
                Settings.System.putInt(
                        this.getContext().getContentResolver(),
                        "tweaks_pulldown_blur",
                        value);
                Log.d(this.getClass().getName(), "Successful!");
            } catch (Exception e) {
                tweaksHelper.MakeToast(getResources().getString(R.string.error_write_settings));
            }
        }
        if (preference.getKey().equals("tweaks_disable_persistent_notifications")) {
            boolean checked = ((SwitchPreference) preference).isChecked();
            int value = (checked ? 1 : 0);
            try {
                Settings.System.putInt(
                        this.getContext().getContentResolver(),
                        "tweaks_disable_persistent_notifications",
                        value);
                Log.d(this.getClass().getName(), "Successful!");
                tweaksHelper.createRebootNotification();
            } catch (Exception e) {
                tweaksHelper.MakeToast(getResources().getString(R.string.error_write_settings));
            }
        }
        if (preference.getKey().equals("tweaks_hide_keyboard_switcher")) {
            boolean checked = ((SwitchPreference) preference).isChecked();
            int value = (checked ? 1 : 0);
            try {
                Settings.System.putInt(
                        this.getContext().getContentResolver(),
                        "tweaks_hide_keyboard_switcher",
                        value);
                Log.d(this.getClass().getName(), "Successful!");
                tweaksHelper.createRebootNotification();
            } catch (Exception e) {
                tweaksHelper.MakeToast(getResources().getString(R.string.error_write_settings));
            }
        }
        if (preference.getKey().equals("battery_bar")) {
            boolean checked = ((SwitchPreference) preference).isChecked();
            int value = (checked ? 1 : 0);

            try {
                Settings.System.putInt(
                        this.getContext().getContentResolver(),
                        "battery_bar",
                        value);
                Log.d(this.getClass().getName(), "Successful!");
            } catch (Exception e) {
                tweaksHelper.MakeToast(getResources().getString(R.string.error_write_settings));
            }
        }
        if (preference.getKey().equals("battery_bar_animate")) {
            boolean checked = ((SwitchPreference) preference).isChecked();
            int value = (checked ? 1 : 0);

            try {
                Settings.System.putInt(
                        this.getContext().getContentResolver(),
                        "battery_bar_animate",
                        value);
                Log.d(this.getClass().getName(), "Successful!");
            } catch (Exception e) {
                tweaksHelper.MakeToast(getResources().getString(R.string.error_write_settings));
            }
        }
        if (preference.getKey().equals("battery_bar_style")) {
            boolean checked = ((SwitchPreference) preference).isChecked();
            int value = (checked ? 1 : 0);

            try {
                Settings.System.putInt(
                        this.getContext().getContentResolver(),
                        "battery_bar_style",
                        value);
                Log.d(this.getClass().getName(), "Successful!");
            } catch (Exception e) {
                tweaksHelper.MakeToast(getResources().getString(R.string.error_write_settings));
            }
        }
        if (preference.getKey().equals("tweaks_show_multi_user")) {
            boolean checked = ((SwitchPreference) preference).isChecked();
            int value = (checked ? 1 : 0);
            try {
                Settings.System.putInt(
                        this.getContext().getContentResolver(),
                        "tweaks_show_multi_user",
                        value);
                Log.d(this.getClass().getName(), "Successful!");
                tweaksHelper.createRebootNotification();
            } catch (Exception e) {
                tweaksHelper.MakeToast(getResources().getString(R.string.error_write_settings));
            }
        }
        if (preference.getKey().equals(navBarHeightKey)) {
            try {
                navBarHeightDialog = seekDialog.getSeekDialog(SeekDialog.SeekEnum.eNavBarHeight);
                navBarHeightDialog.show();
                // Find all the IDs on the dialog (this has to be done after the dislog has been shown
                // other wise you will crash the app
                navBarHeightSeek = (SeekBar) navBarHeightDialog.findViewById(R.id.seekbar1);
                navBarHeightValue = (TextView) navBarHeightDialog.findViewById(R.id.seek1Value);
                Button navBarHeightMinus = (Button) navBarHeightDialog.findViewById(R.id.minus1);
                Button navBarHeightPlus = (Button) navBarHeightDialog.findViewById(R.id.add1);
                Button navBarHeightApply = (Button) navBarHeightDialog.findViewById(R.id.apply);
                // Set the maximum the slider can go up to
                navBarHeightSeek.setMax(navBarHeightSeekMax);
                // Get the current value
                navBarHeightProgress = (Settings.System.getInt(this.getContext().getContentResolver(),
                        navBarHeightKey, 128));
                // Set the value to the seek bar
                navBarHeightSeek.setProgress(navBarHeightProgress);
                // Set the text above the seek bar
                navBarHeightValue.setText(getResources().getString(R.string.tweaks_navbar_height_title) + ": " + String.valueOf(navBarHeightProgress));
                //Set the Seek bar listener
                navBarHeightSeek.setOnSeekBarChangeListener(navBarHeightSeekListener);
                //Set the button listeners
                navBarHeightPlus.setOnClickListener(navBarHeightPlusListener);
                navBarHeightMinus.setOnClickListener(navBarHeightMinusListener);
                navBarHeightApply.setOnClickListener(navBarHeightApplyListener);
                return true;
            } catch (Exception e) {
                tweaksHelper.MakeToast(getResources().getString(R.string.error_write_settings));
            }
        }
        if (preference.getKey().equals(allowCustomNavBarHeightKey)) {
            boolean checked = ((SwitchPreference) preference).isChecked();
            int value = (checked ? 1 : 0);
            try {
                Settings.System.putInt(
                        this.getContext().getContentResolver(),
                        allowCustomNavBarHeightKey,
                        value);
                Log.d(this.getClass().getName(), "Successful!");
            } catch (Exception e) {
                tweaksHelper.MakeToast(getResources().getString(R.string.error_write_settings));
            }
        }
        if (preference.getKey().equals(ShowNavbarKey)) {
            boolean checked = ((SwitchPreference) preference).isChecked();
            int value = (checked ? 1 : 0);
            try {
                Settings.System.putInt(
                        this.getContext().getContentResolver(),
                        ShowNavbarKey,
                        value);
                Log.d(this.getClass().getName(), "Successful!");
                tweaksHelper.createRebootNotification();
            } catch (Exception e) {
                tweaksHelper.MakeToast(getResources().getString(R.string.error_write_settings));
            }
        }
        return false;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
        if (preference == mImmersiveList) {
            String textValue = newValue.toString();
            int index = mImmersiveList.findIndexOfValue(textValue);
            CharSequence[] entries = mImmersiveList.getEntries();
            for (int i = 0; i < entries.length; i++) {
                if (entries[i].equals(newValue)) {
                    index = i;
                    break;
                }
            }
            String immersive = (entries[index] + "");
            if (immersive.equals(entries[0])) {
                immersivePerAppList.setEnabled(false);
                immersivePerAppList.setSelectable(false);
                if (isFreeVersion) {
                    immersivePerAppList.setSummary(R.string.free_notification2);
                } else {
                    immersivePerAppList.setSummary(R.string.immersive_perapp_hint);
                }
                try {
                    Settings.Global.putString(
                            this.getContext().getContentResolver(),
                            "policy_control",
                            "immersive.full=");
                    Log.d(this.getClass().getName(), "Successful!");
                } catch (Exception e) {
                    tweaksHelper.MakeToast(getResources().getString(R.string.error_write_settings));
                }
            } else if (immersive.equals(entries[1])) {
                immersivePerAppList.setEnabled(false);
                immersivePerAppList.setSelectable(false);
                if (isFreeVersion) {
                    immersivePerAppList.setSummary(R.string.free_notification2);
                } else {
                    immersivePerAppList.setSummary(R.string.immersive_perapp_hint);
                }
                try {
                    Settings.Global.putString(
                            this.getContext().getContentResolver(),
                            "policy_control",
                            "immersive.full=*");
                    Log.d(this.getClass().getName(), "Successful!");
                } catch (Exception e) {
                    tweaksHelper.MakeToast(getResources().getString(R.string.error_write_settings));
                }
            } else if (immersive.equals(entries[2])) {
                if (!isFreeVersion) {
                    immersivePerAppList.setEnabled(true);
                    immersivePerAppList.setSelectable(true);
                    immersivePerAppList.setSummary("");
                    Set<String> immersiveSelections = sharedPref.getStringSet("immersive_perapp", null);
                    if (immersiveSelections == null)
                        tweaksHelper.MakeToast(getResources().getString(R.string.immersive_perapp_empty));
                    else if (immersiveSelections.isEmpty())
                        tweaksHelper.MakeToast(getResources().getString(R.string.immersive_perapp_empty));
                    else {
                        String immersiveList = TextUtils.join(", ", immersiveSelections);
                        try {
                            Settings.Global.putString(
                                    getContext().getContentResolver(),
                                    "policy_control",
                                    "immersive.full=" + immersiveList);

                            Log.d(this.getClass().getName(), "Successful!");
                        } catch (Exception e) {
                            tweaksHelper.MakeToast(getResources().getString(R.string.error_write_settings));
                        }
                    }
                } else {
                    tweaksHelper.MakeToast(getResources().getString(R.string.free_notification2));
                }
            }
            return true;
        }
        if (preference == mRecentKey) {
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
                        "tweaks_recent_key_config",
                        newint);
                Log.d(this.getClass().getName(), "Successful!");
            } catch (Exception e) {
                tweaksHelper.MakeToast(getResources().getString(R.string.error_write_settings));
            }

            return true;
        }
        if (preference == mWindowAnimation) {
            int index = 0;
            CharSequence[] entries = ((ListPreference) preference).getEntryValues();
            for (int i = 0; i < entries.length; i++) {
                if (entries[i].equals(newValue)) {
                    index = i;
                    break;
                }
            }
            String newWindowAnimation = (entries[index] + "");
            try {
                Settings.Global.putString(
                        this.getContext().getContentResolver(),
                        "window_animation_scale",
                        newWindowAnimation);
                Log.d(this.getClass().getName(), "Successful!");
            } catch (Exception e) {
                tweaksHelper.MakeToast(getResources().getString(R.string.error_write_settings));
            }
            return true;
        }

        if (preference == mTransititionAnimation) {
            int index = 0;
            CharSequence[] entries = ((ListPreference) preference).getEntryValues();
            for (int i = 0; i < entries.length; i++) {
                if (entries[i].equals(newValue)) {
                    index = i;
                    break;
                }
            }
            String newTransitionAnimation = (entries[index] + "");
            try {
                Settings.Global.putString(
                        this.getContext().getContentResolver(),
                        "transition_animation_scale",
                        newTransitionAnimation);
                Log.d(this.getClass().getName(), "Successful!");
            } catch (Exception e) {
                tweaksHelper.MakeToast(getResources().getString(R.string.error_write_settings));
            }
            return true;
        }

        if (preference == mAnimatorDuration) {
            int index = 0;
            CharSequence[] entries = ((ListPreference) preference).getEntryValues();
            for (int i = 0; i < entries.length; i++) {
                if (entries[i].equals(newValue)) {
                    index = i;
                    break;
                }
            }
            String newAnimatorScale = (entries[index] + "");
            try {
                Settings.Global.putString(
                        this.getContext().getContentResolver(),
                        "animator_duration_scale",
                        newAnimatorScale);
                Log.d(this.getClass().getName(), "Successful!");
            } catch (Exception e) {
                tweaksHelper.MakeToast(getResources().getString(R.string.error_write_settings));
            }
            return true;
        }
        if (preference == mClockPos) {
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
                        "tweaks_clock_position",
                        newint);
                Log.d(this.getClass().getName(), "Successful!");
            } catch (Exception e) {
                tweaksHelper.MakeToast(getResources().getString(R.string.error_write_settings));
            }
            return true;
        }
        if (preference == mQsButtons) {
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
                        "tweaks_quick_qs_buttons",
                        newint);
                Log.d(this.getClass().getName(), "Successful!");
                tweaksHelper.createRebootNotification();
            } catch (Exception e) {
                tweaksHelper.MakeToast(getResources().getString(R.string.error_write_settings));
            }
            return true;
        }
        if (preference == mQsRow) {
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
                Settings.Secure.putInt(
                        this.getContext().getContentResolver(),
                        "qs_tile_row",
                        newint);
                Log.d(this.getClass().getName(), "Successful!");
            } catch (Exception e) {
                tweaksHelper.MakeToast(getResources().getString(R.string.error_write_settings));
            }

            return true;
        }
        if (preference == mQsColumn) {
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
                Settings.Secure.putInt(
                        this.getContext().getContentResolver(),
                        "qs_tile_column",
                        newint);
                Log.d(this.getClass().getName(), "Successful!");
            } catch (Exception e) {
                tweaksHelper.MakeToast(getResources().getString(R.string.error_write_settings));
            }
            return true;
        }
        if (preference == mBatthickness) {
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
                        "battery_bar_thickness",
                        newint);
                Log.d(this.getClass().getName(), "Successful!");
            } catch (Exception e) {
                tweaksHelper.MakeToast(getResources().getString(R.string.error_write_settings));
            }

            return true;
        }
        if (preference == immersivePerAppList) {
            try {
                Settings.Global.putString(
                        getContext().getContentResolver(),
                        "policy_control",
                        "immersive.full=" + newValue.toString().replace("[", "")
                                .replace("]", ""));
                Log.d(this.getClass().getName(), "Successful!");
                //Log.d(DEBUGTAG, "newImmersiveListSlections: " + "immersive.full=" + newValue.toString().replace("[", "").replace("]", ""));
            } catch (Exception e) {
                tweaksHelper.MakeToast(getResources().getString(R.string.error_write_settings));
            }
            /*String newPerAppList[] = newValue.toString().replace("[", "").replace("]", "").split(", ");
            Set<String> hashSet = new HashSet<String>(Arrays.asList(newPerAppList));
            Log.d(DEBUGTAG, "newImmersiveListSlections: " + newPerAppList);
            Log.d(DEBUGTAG, "newImmersiveListSlections2: " + hashSet);
            return true;*/

        }
        return false;
    }
}