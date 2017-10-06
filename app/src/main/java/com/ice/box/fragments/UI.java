package com.ice.box.fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
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
import android.widget.TabWidget;
import android.widget.TextView;

import com.ice.box.R;
import com.ice.box.helpers.RootUtils;
import com.ice.box.helpers.SeekDialog;
import com.ice.box.helpers.SystemProperties;
import com.ice.box.helpers.TweaksHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.ice.box.helpers.Constants.DEBUGTAG;
import static com.ice.box.helpers.Constants.ShowNavbarKey;
import static com.ice.box.helpers.Constants.allowCustomNavBarHeightKey;
import static com.ice.box.helpers.Constants.backToKillKey;
import static com.ice.box.helpers.Constants.disableHardHomeWakeKey;
import static com.ice.box.helpers.Constants.disableMtpNotifKey;
import static com.ice.box.helpers.Constants.isExceptionKey;
import static com.ice.box.helpers.Constants.isFreeVersionKey;
import static com.ice.box.helpers.Constants.isNightlyKey;
import static com.ice.box.helpers.Constants.isNote8PortKey;
import static com.ice.box.helpers.Constants.licenseRatingKey;
import static com.ice.box.helpers.Constants.localNightlyVersionKey;
import static com.ice.box.helpers.Constants.localStableVersionKey;
import static com.ice.box.helpers.Constants.navBarHeightKey;
import static com.ice.box.helpers.Constants.renovateFingerprintProp;


@SuppressWarnings("unused")
public class UI extends PreferenceFragment {

    private SeekDialog seekDialog;
    private TweaksHelper tweaksHelper;
    private boolean isFreeVersion;
    private ListPreference mBixbyRemap;
    private ListPreference mBixbyCustomApp;
    private ListPreference mWindowAnimation;
    private ListPreference mTransititionAnimation;
    private ListPreference mAnimatorDuration;
    private ListPreference mImmersiveList;
    private MultiSelectListPreference mImmersivePerAppList;
    String immersiveListValue;
    String bixbySavedValue;
    int bixbyRemapValue;
    SharedPreferences sharedPref;


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
        @SuppressLint("SetTextI18n")
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
        @SuppressLint("SetTextI18n")
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
        sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.ui_preference);
        Preference filterPref;

        isFreeVersion = sharedPref.getBoolean(isFreeVersionKey, true);
        boolean isMonthly = (sharedPref.getBoolean("isMonthly", false));
        boolean isYearly = (sharedPref.getBoolean("isYearly", false));
        boolean isException = sharedPref.getBoolean(isExceptionKey, false);
        int currentRomVersion = sharedPref.getInt(localStableVersionKey, 1);
        int nightliesOfflineCurrentRevision = sharedPref.getInt(localNightlyVersionKey, 1);
        boolean isGalaxyS8 = sharedPref.getBoolean("isGalaxyS8", false);
        boolean isGalaxyS7 = sharedPref.getBoolean("isGalaxyS7", false);
        boolean isNotePort = sharedPref.getBoolean(isNote8PortKey, false);
        boolean isNightly = sharedPref.getBoolean(isNightlyKey, false);
        boolean isICE = sharedPref.getBoolean("isICE", false);
        int licenseRating = sharedPref.getInt(licenseRatingKey, 0);
        immersiveListValue = Settings.Global.getString(this.getContext().getContentResolver(),
                "policy_control");
        bixbyRemapValue = Settings.System.getInt(getContext().getContentResolver(),
                "tweaks_custom_bixby", 0);
        bixbySavedValue = Settings.System.getString(getContext().getContentResolver(),
                "tweaks_custom_bixby_app");


        tweaksHelper = new TweaksHelper(this.getContext());
        seekDialog = new SeekDialog(this.getContext());

        //Immersive mode
        mImmersiveList = (ListPreference) findPreference("tweaks_immersive_list");
        mImmersivePerAppList = (MultiSelectListPreference) findPreference("immersive_perapp");
        if (isFreeVersion) {
            CharSequence[] entries = {getString(R.string.immersive_global_off), getString(R.string.immersive_global_on)};
            CharSequence[] entryValues = {"immersive.full=", "immersive.full=*"};
            mImmersiveList.setEntries(entries);
            mImmersiveList.setEntryValues(entryValues);
            if (TweaksHelper.isEmptyString(immersiveListValue)) {
                mImmersiveList.setValueIndex(0);
            } else {
                switch (immersiveListValue) {
                    case "immersive.full=":
                        try {
                            mImmersiveList.setValueIndex(0);
                        } catch (ArrayIndexOutOfBoundsException exception) {
                            Log.e(this.getClass().getName(), "OutOfBoundsException: " + mImmersiveList);
                        }
                        break;
                    case "immersive.full=*":
                        try {
                            mImmersiveList.setValueIndex(1);
                        } catch (ArrayIndexOutOfBoundsException exception) {
                            Log.e(this.getClass().getName(), "OutOfBoundsException: " + mImmersiveList);
                        }
                        break;
                    default:
                        try {
                            mImmersiveList.setValueIndex(1);
                        } catch (ArrayIndexOutOfBoundsException exception) {
                            Log.e(this.getClass().getName(), "OutOfBoundsException: " + mImmersiveList);
                        }
                        break;
                }
            }
        } else {
            if (TweaksHelper.isEmptyString(immersiveListValue)) {
                mImmersiveList.setValueIndex(0);
            } else {
                switch (immersiveListValue) {
                    case "immersive.full=":
                        try {
                            mImmersiveList.setValueIndex(0);

                        } catch (ArrayIndexOutOfBoundsException exception) {
                            Log.e(this.getClass().getName(), "OutOfBoundsException: " + mImmersiveList);
                        }
                        break;
                    case "immersive.full=*":
                        try {
                            mImmersiveList.setValueIndex(1);
                        } catch (ArrayIndexOutOfBoundsException exception) {
                            Log.e(this.getClass().getName(), "OutOfBoundsException: " + mImmersiveList);
                        }
                        break;
                    default:
                        try {
                            mImmersiveList.setValueIndex(2);
                        } catch (ArrayIndexOutOfBoundsException exception) {
                            Log.e(this.getClass().getName(), "OutOfBoundsException: " + mImmersiveList);
                        }
                        break;

                }
            }

        }
        //Listener
        //
        mImmersiveList.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
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
                    mImmersivePerAppList.setEnabled(false);
                    mImmersivePerAppList.setSelectable(false);
                    if (isFreeVersion) {
                        mImmersivePerAppList.setSummary(R.string.free_notification2);
                    } else {
                        mImmersivePerAppList.setSummary(R.string.immersive_perapp_hint);
                    }
                    try {
                        Settings.Global.putString(
                                getContext().getContentResolver(),
                                "policy_control",
                                "immersive.full=");
                        Log.d(this.getClass().getName(), "Successful!");
                        return true;
                    } catch (Exception e) {
                        tweaksHelper.MakeToast(getResources().getString(R.string.error_write_settings));
                        return false;
                    }
                } else if (immersive.equals(entries[1])) {
                    mImmersivePerAppList.setEnabled(false);
                    mImmersivePerAppList.setSelectable(false);
                    if (isFreeVersion) {
                        mImmersivePerAppList.setSummary(R.string.free_notification2);
                    } else {
                        mImmersivePerAppList.setSummary(R.string.immersive_perapp_hint);
                    }
                    try {
                        Settings.Global.putString(
                                getContext().getContentResolver(),
                                "policy_control",
                                "immersive.full=*");
                        Log.d(this.getClass().getName(), "Successful!");
                        return true;
                    } catch (Exception e) {
                        tweaksHelper.MakeToast(getResources().getString(R.string.error_write_settings));
                        return false;
                    }
                } else if (immersive.equals(entries[2])) {
                    if (!isFreeVersion) {
                        mImmersivePerAppList.setEnabled(true);
                        mImmersivePerAppList.setSelectable(true);
                        mImmersivePerAppList.setSummary("");
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
                                return true;
                            } catch (Exception e) {
                                tweaksHelper.MakeToast(getResources().getString(R.string.error_write_settings));
                                return false;
                            }
                        }
                    } else {
                        tweaksHelper.MakeToast(getResources().getString(R.string.free_notification2));
                        return false;
                    }
                }
                return false;
            }
        });


        //Immersive perapp list
        mImmersivePerAppList = (MultiSelectListPreference) findPreference("immersive_perapp");
        if (isFreeVersion) {
            mImmersivePerAppList.setEnabled(false);
            mImmersivePerAppList.setSelectable(false);
            mImmersivePerAppList.setSummary(getResources().getString(R.string.free_notification2));
        } else {
            if (TweaksHelper.isEmptyString(immersiveListValue)) {
                mImmersivePerAppList.setEnabled(false);
                mImmersivePerAppList.setSelectable(false);
                mImmersivePerAppList.setSummary(R.string.immersive_perapp_hint);
            } else {
                switch (immersiveListValue) {
                    case "immersive.full=":
                        mImmersivePerAppList.setEnabled(false);
                        mImmersivePerAppList.setSelectable(false);
                        mImmersivePerAppList.setSummary(R.string.immersive_perapp_hint);
                        break;
                    case "immersive.full=*":
                        mImmersivePerAppList.setEnabled(false);
                        mImmersivePerAppList.setSelectable(false);
                        mImmersivePerAppList.setSummary(R.string.immersive_perapp_hint);
                        break;
                    default:
                        mImmersivePerAppList.setEnabled(true);
                        mImmersivePerAppList.setSelectable(true);
                        break;
                }
            }
            //Populate array with entries
            new Thread(new Runnable() {
                @Override
                public void run() {
                    PackageManager pm = getContext().getPackageManager();
                    Intent intent = new Intent()
                            .setAction(Intent.ACTION_MAIN)
                            .addCategory(Intent.CATEGORY_LAUNCHER);
                    List<ResolveInfo> list = pm.queryIntentActivities(intent, 0);
                    HashSet<String> packageNames = new HashSet<>(0);
                    List<ApplicationInfo> appListInfo = new ArrayList<>();

                    //adding unique packagenames to hashset
                    for (ResolveInfo resolveInfo : list) {
                        packageNames.add(resolveInfo.activityInfo.packageName);
                    }

                    //get application infos and add them to arraylist
                    for (String packageName : packageNames) {
                        try {
                            appListInfo.add(pm.getApplicationInfo(packageName, PackageManager.GET_META_DATA));
                        } catch (PackageManager.NameNotFoundException e) {
                            //Do Nothing
                        }
                    }
                    Collections.sort(appListInfo, new ApplicationInfo.DisplayNameComparator(pm));
                    CharSequence[] entries = new CharSequence[appListInfo.size()];
                    CharSequence[] entryValues = new CharSequence[appListInfo.size()];
                    try {
                        int i = 0;
                        for (ApplicationInfo applicationInfo : appListInfo) {
                            entries[i] = applicationInfo.loadLabel(pm);
                            entryValues[i] = applicationInfo.packageName;
                            i++;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    mImmersivePerAppList.setEntries(entries);
                    mImmersivePerAppList.setEntryValues(entryValues);
                    //Read saved values in Global Settings, covert them and set them as default
                    // so that app wont be out of sync if it's data is wiped
                    if (!TweaksHelper.isEmptyString(immersiveListValue) && !immersiveListValue.equals("immersive.full=")
                            || !TweaksHelper.isEmptyString(immersiveListValue) && !immersiveListValue.equals("immersive.full=*")) {
                        String[] valuesGlobal = immersiveListValue.replace("immersive.full=", "").split(", ");
                        Set<String> savedValues = new HashSet<>(Arrays.asList(valuesGlobal));
                        mImmersivePerAppList.setValues(savedValues);
                    }
                    mImmersivePerAppList.setNegativeButtonText(R.string.cancel);
                    mImmersivePerAppList.setPositiveButtonText(R.string.choose);
                    mImmersivePerAppList.setDialogTitle(R.string.immersive_perapp_summary);

                }
            }).start();
        }

        //Listener
        mImmersivePerAppList.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                //String textValue = newValue.toString();
                try {
                    Settings.Global.putString(
                            getContext().getContentResolver(),
                            "policy_control",
                            "immersive.full=" + newValue.toString().replace("[", "")
                                    .replace("]", ""));
                    Log.d(this.getClass().getName(), "Successful!");
                    if (TweaksHelper.isEmptyString(newValue.toString().replace("[", "")
                            .replace("]", ""))) {
                        mImmersiveList.setValueIndex(0);
                        mImmersivePerAppList.setEnabled(false);
                        mImmersivePerAppList.setSelectable(false);
                        mImmersivePerAppList.setSummary(R.string.immersive_perapp_hint);
                        return false;
                    } else {
                        return true;
                    }
                } catch (Exception e) {
                    tweaksHelper.MakeToast(getResources().getString(R.string.error_write_settings));
                    return false;
                }

            }
        });


        filterPref = findPreference("tweaks_fingerprint_unlock");
        checkPref = (SwitchPreference) findPreference("tweaks_fingerprint_unlock");
        String fingerPropValue = SystemProperties.get(renovateFingerprintProp);
        if (TweaksHelper.isEmptyString(fingerPropValue)) {
            checkPref.setChecked(false);
        } else {
            switch (fingerPropValue) {
                case "0":
                    checkPref.setChecked(false);
                    break;
                case "1":
                    checkPref.setChecked(true);
                    break;
            }
        }
        filterPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                String newstring = TweaksHelper.returnSwitch(SystemProperties.get(renovateFingerprintProp));
                try {
                    RootUtils.setProp(getContext(), renovateFingerprintProp, newstring);
                    Log.d(this.getClass().getName(), "Successful!");
                    tweaksHelper.createRebootNotification();
                    return true;
                } catch (Exception e) {
                    tweaksHelper.MakeToast("Sorry..that didn't work..is Renovate ROM?");
                    return false;
                }

            }
        });


        filterPref = findPreference("tweaks_all_rotations");
        checkPref = (SwitchPreference) findPreference("tweaks_all_rotations");
        boolean checked = (Settings.System.getInt(this.getContext().getContentResolver(),
                "tweaks_all_rotations", 0) == 1);
        checkPref.setChecked(checked);
        filterPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                boolean checked = ((SwitchPreference) preference).isChecked();
                int value = (checked ? 1 : 0);
                try {
                    Settings.System.putInt(
                            getContext().getContentResolver(),
                            "tweaks_all_rotations",
                            value);
                    Log.d(this.getClass().getName(), "Successful!");
                    tweaksHelper.createRebootNotification();
                    return true;
                } catch (Exception e) {
                    tweaksHelper.MakeToast(getResources().getString(R.string.error_write_settings));
                    return false;
                }
            }
        });

        filterPref = findPreference(backToKillKey);
        checkPref = (SwitchPreference) findPreference(backToKillKey);
        checked = (Settings.System.getInt(this.getContext().getContentResolver(),
                backToKillKey, 0) == 1);
        checkPref.setChecked(checked);
        if (isNotePort) {
            getPreferenceScreen().removePreference(findPreference(backToKillKey));
        } else {
            filterPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    boolean checked = ((SwitchPreference) preference).isChecked();
                    int value = (checked ? 1 : 0);
                    try {
                        Settings.System.putInt(getContext().getContentResolver(),
                                backToKillKey, value);
                        Log.d(this.getClass().getName(), "Successful!");
                        return true;
                    } catch (Exception e) {
                        tweaksHelper.MakeToast(getResources().getString(R.string.error_write_settings));
                        return false;
                    }
                }
            });
        }


        filterPref = findPreference("tweaks_disable_volume_warning");
        checkPref = (SwitchPreference) findPreference("tweaks_disable_volume_warning");
        checked = (Settings.System.getInt(this.getContext().getContentResolver(),
                "tweaks_disable_volume_warning", 0) == 0);
        checkPref.setChecked(checked);
        filterPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                boolean checked = ((SwitchPreference) preference).isChecked();
                int value = (checked ? 0 : 1);
                try {
                    Settings.System.putInt(
                            getContext().getContentResolver(),
                            "tweaks_disable_volume_warning",
                            value);
                    Log.d(this.getClass().getName(), "Successful!");
                    return true;
                } catch (Exception e) {
                    tweaksHelper.MakeToast(getResources().getString(R.string.error_write_settings));
                    return false;
                }
            }
        });

        filterPref = findPreference("tweaks_hide_brightness_warning");
        checkPref = (SwitchPreference) findPreference("tweaks_hide_brightness_warning");
        checked = (Settings.System.getInt(this.getContext().getContentResolver(),
                "tweaks_hide_brightness_warning", 0) == 0);
        checkPref.setChecked(checked);
        filterPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                boolean checked = ((SwitchPreference) preference).isChecked();
                int value = (checked ? 0 : 1);
                try {
                    Settings.System.putInt(
                            getContext().getContentResolver(),
                            "tweaks_hide_brightness_warning",
                            value);
                    Log.d(this.getClass().getName(), "Successful!");
                    tweaksHelper.createSystemUINotification();
                    return true;
                } catch (Exception e) {
                    tweaksHelper.MakeToast(getResources().getString(R.string.error_write_settings));
                    return false;
                }

            }
        });

        filterPref = findPreference("tweaks_clock_onclick");
        checkPref = (SwitchPreference) findPreference("tweaks_clock_onclick");
        checked = (Settings.System.getInt(this.getContext().getContentResolver(),
                "tweaks_clock_onclick", 0) == 1);
        checkPref.setChecked(checked);
        filterPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                boolean checked = ((SwitchPreference) preference).isChecked();
                int value = (checked ? 1 : 0);
                try {
                    Settings.System.putInt(
                            getContext().getContentResolver(),
                            "tweaks_clock_onclick",
                            value);
                    Log.d(this.getClass().getName(), "Successful!");
                    tweaksHelper.createRebootNotification();
                    return true;
                } catch (Exception e) {
                    tweaksHelper.MakeToast(getResources().getString(R.string.error_write_settings));
                    return false;
                }
            }
        });

        filterPref = findPreference("tweaks_hide_battery");
        checkPref = (SwitchPreference) findPreference("tweaks_hide_battery");
        checked = (Settings.System.getInt(this.getContext().getContentResolver(),
                "tweaks_hide_battery", 0) == 1);
        checkPref.setChecked(checked);
        filterPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                boolean checked = ((SwitchPreference) preference).isChecked();
                int value = (checked ? 1 : 0);
                try {
                    Settings.System.putInt(
                            getContext().getContentResolver(),
                            "tweaks_hide_battery",
                            value);
                    Log.d(this.getClass().getName(), "Successful!");
                    return true;
                } catch (Exception e) {
                    tweaksHelper.MakeToast(getResources().getString(R.string.error_write_settings));
                    return false;
                }
            }
        });

        filterPref = findPreference("tweaks_double_tap_sleep");
        checkPref = (SwitchPreference) findPreference("tweaks_double_tap_sleep");
        checked = (Settings.System.getInt(this.getContext().getContentResolver(),
                "tweaks_double_tap_sleep", 0) == 1);
        checkPref.setChecked(checked);
        filterPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                boolean checked = ((SwitchPreference) preference).isChecked();
                int value = (checked ? 1 : 0);
                try {
                    Settings.System.putInt(
                            getContext().getContentResolver(),
                            "tweaks_double_tap_sleep",
                            value);
                    Log.d(this.getClass().getName(), "Successful!");
                    return true;
                } catch (Exception e) {
                    tweaksHelper.MakeToast(getResources().getString(R.string.error_write_settings));
                    return false;
                }
            }
        });

        filterPref = findPreference("tweaks_hide_brightness_slider");
        checkPref = (SwitchPreference) findPreference("tweaks_hide_brightness_slider");
        checked = (Settings.System.getInt(this.getContext().getContentResolver(),
                "tweaks_hide_brightness_slider", 0) == 1);
        checkPref.setChecked(checked);
        filterPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                boolean checked = ((SwitchPreference) preference).isChecked();
                int value = (checked ? 1 : 0);
                try {
                    Settings.System.putInt(
                            getContext().getContentResolver(),
                            "tweaks_hide_brightness_slider",
                            value);
                    Log.d(this.getClass().getName(), "Successful!");
                    tweaksHelper.createRebootNotification();
                    return true;
                } catch (Exception e) {
                    tweaksHelper.MakeToast(getResources().getString(R.string.error_write_settings));
                    return false;
                }

            }
        });

        filterPref = findPreference("tweaks_qs_lock");
        checkPref = (SwitchPreference) findPreference("tweaks_qs_lock");
        checked = (Settings.System.getInt(this.getContext().getContentResolver(),
                "tweaks_qs_lock", 0) == 1);
        checkPref.setChecked(checked);
        filterPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                boolean checked = ((SwitchPreference) preference).isChecked();
                int value = (checked ? 1 : 0);
                try {
                    Settings.System.putInt(
                            getContext().getContentResolver(),
                            "tweaks_qs_lock",
                            value);
                    Log.d(this.getClass().getName(), "Successful!");
                    return true;
                } catch (Exception e) {
                    tweaksHelper.MakeToast(getResources().getString(R.string.error_write_settings));
                    return false;
                }
            }
        });


        filterPref = findPreference("tweaks_qs_override_lockscreen");
        checkPref = (SwitchPreference) findPreference("tweaks_qs_override_lockscreen");
        checked = (Settings.System.getInt(this.getContext().getContentResolver(),
                "tweaks_qs_override_lockscreen", 0) == 1);
        checkPref.setChecked(checked);
        filterPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                boolean checked = ((SwitchPreference) preference).isChecked();
                int value = (checked ? 1 : 0);
                try {
                    Settings.System.putInt(
                            getContext().getContentResolver(),
                            "tweaks_qs_override_lockscreen",
                            value);
                    Log.d(this.getClass().getName(), "Successful!");
                    tweaksHelper.createSafetyNotification();
                    return true;
                } catch (Exception e) {
                    tweaksHelper.MakeToast(getResources().getString(R.string.error_write_settings));
                    return false;
                }
            }
        });

        filterPref = findPreference("tweaks_qs_pulldown");
        checkPref = (SwitchPreference) findPreference("tweaks_qs_pulldown");
        checked = (Settings.System.getInt(this.getContext().getContentResolver(),
                "tweaks_qs_pulldown", 1) == 1);
        checkPref.setChecked(checked);
        filterPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                boolean checked = ((SwitchPreference) preference).isChecked();
                int value = (checked ? 1 : 0);
                try {
                    Settings.System.putInt(
                            getContext().getContentResolver(),
                            "tweaks_qs_pulldown",
                            value);
                    Log.d(this.getClass().getName(), "Successful!");
                    return true;
                } catch (Exception e) {
                    tweaksHelper.MakeToast(getResources().getString(R.string.error_write_settings));
                    return false;
                }

            }
        });

        filterPref = findPreference("tweaks_pulldown_blur");
        checkPref = (SwitchPreference) findPreference("tweaks_pulldown_blur");
        checked = (Settings.System.getInt(this.getContext().getContentResolver(),
                "tweaks_pulldown_blur", 0) == 1);
        checkPref.setChecked(checked);
        filterPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                boolean checked = ((SwitchPreference) preference).isChecked();
                int value = (checked ? 1 : 0);
                try {
                    Settings.System.putInt(
                            getContext().getContentResolver(),
                            "tweaks_pulldown_blur",
                            value);
                    Log.d(this.getClass().getName(), "Successful!");
                    return true;
                } catch (Exception e) {
                    tweaksHelper.MakeToast(getResources().getString(R.string.error_write_settings));
                    return false;
                }
            }
        });

        filterPref = findPreference("tweaks_disable_persistent_notifications");
        checkPref = (SwitchPreference) findPreference("tweaks_disable_persistent_notifications");
        checked = (Settings.System.getInt(this.getContext().getContentResolver(),
                "tweaks_disable_persistent_notifications", 0) == 1);
        checkPref.setChecked(checked);
        filterPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                boolean checked = ((SwitchPreference) preference).isChecked();
                int value = (checked ? 1 : 0);
                try {
                    Settings.System.putInt(
                            getContext().getContentResolver(),
                            "tweaks_disable_persistent_notifications",
                            value);
                    Log.d(this.getClass().getName(), "Successful!");
                    tweaksHelper.createRebootNotification();
                    return true;
                } catch (Exception e) {
                    tweaksHelper.MakeToast(getResources().getString(R.string.error_write_settings));
                    return false;
                }
            }
        });


        filterPref = findPreference("tweaks_hide_keyboard_switcher");
        checkPref = (SwitchPreference) findPreference("tweaks_hide_keyboard_switcher");
        checked = (Settings.System.getInt(this.getContext().getContentResolver(),
                "tweaks_hide_keyboard_switcher", 0) == 1);
        checkPref.setChecked(checked);
        filterPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                boolean checked = ((SwitchPreference) preference).isChecked();
                int value = (checked ? 1 : 0);
                try {
                    Settings.System.putInt(
                            getContext().getContentResolver(),
                            "tweaks_hide_keyboard_switcher",
                            value);
                    Log.d(this.getClass().getName(), "Successful!");
                    tweaksHelper.createRebootNotification();
                    return true;
                } catch (Exception e) {
                    tweaksHelper.MakeToast(getResources().getString(R.string.error_write_settings));
                    return false;
                }
            }
        });

        filterPref = findPreference(disableMtpNotifKey);
        checkPref = (SwitchPreference) findPreference(disableMtpNotifKey);
        checked = (Settings.System.getInt(this.getContext().getContentResolver(),
                disableMtpNotifKey, 0) == 1);
        checkPref.setChecked(checked);
        filterPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                boolean checked = ((SwitchPreference) preference).isChecked();
                int value = (checked ? 1 : 0);
                try {
                    Settings.System.putInt(
                            getContext().getContentResolver(),
                            disableMtpNotifKey,
                            value);
                    Log.d(this.getClass().getName(), "Successful!");
                    return true;
                } catch (Exception e) {
                    tweaksHelper.MakeToast(getResources().getString(R.string.error_write_settings));
                    return false;
                }
            }
        });


        filterPref = findPreference("battery_bar");
        checkPref = (SwitchPreference) findPreference("battery_bar");
        checked = (Settings.System.getInt(this.getContext().getContentResolver(), "battery_bar",
                0) == 1);
        checkPref.setChecked(checked);
        filterPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                boolean checked = ((SwitchPreference) preference).isChecked();
                int value = (checked ? 1 : 0);

                try {
                    Settings.System.putInt(
                            getContext().getContentResolver(),
                            "battery_bar",
                            value);
                    Log.d(this.getClass().getName(), "Successful!");
                    return true;
                } catch (Exception e) {
                    tweaksHelper.MakeToast(getResources().getString(R.string.error_write_settings));
                    return false;
                }
            }
        });


        filterPref = findPreference("battery_bar_animate");
        checkPref = (SwitchPreference) findPreference("battery_bar_animate");
        checked = (Settings.System.getInt(this.getContext().getContentResolver(),
                "battery_bar_animate", 0) == 1);
        checkPref.setChecked(checked);
        filterPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                boolean checked = ((SwitchPreference) preference).isChecked();
                int value = (checked ? 1 : 0);

                try {
                    Settings.System.putInt(
                            getContext().getContentResolver(),
                            "battery_bar_animate",
                            value);
                    Log.d(this.getClass().getName(), "Successful!");
                    return true;
                } catch (Exception e) {
                    tweaksHelper.MakeToast(getResources().getString(R.string.error_write_settings));
                    return false;
                }
            }
        });


        filterPref = findPreference("battery_bar_style");
        checkPref = (SwitchPreference) findPreference("battery_bar_style");
        checked = (Settings.System.getInt(this.getContext().getContentResolver(),
                "battery_bar_style", 0) == 1);
        checkPref.setChecked(checked);
        filterPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                boolean checked = ((SwitchPreference) preference).isChecked();
                int value = (checked ? 1 : 0);

                try {
                    Settings.System.putInt(
                            getContext().getContentResolver(),
                            "battery_bar_style",
                            value);
                    Log.d(this.getClass().getName(), "Successful!");
                    return true;
                } catch (Exception e) {
                    tweaksHelper.MakeToast(getResources().getString(R.string.error_write_settings));
                    return false;
                }
            }
        });

        filterPref = findPreference("tweaks_show_multi_user");
        checkPref = (SwitchPreference) findPreference("tweaks_show_multi_user");
        checked = (Settings.System.getInt(this.getContext().getContentResolver(),
                "tweaks_show_multi_user", 0) == 1);
        checkPref.setChecked(checked);
        filterPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                boolean checked = ((SwitchPreference) preference).isChecked();
                int value = (checked ? 1 : 0);
                try {
                    Settings.System.putInt(
                            getContext().getContentResolver(),
                            "tweaks_show_multi_user",
                            value);
                    Log.d(this.getClass().getName(), "Successful!");
                    tweaksHelper.createRebootNotification();
                    return true;
                } catch (Exception e) {
                    tweaksHelper.MakeToast(getResources().getString(R.string.error_write_settings));
                    return false;
                }
            }
        });

        filterPref = findPreference(disableHardHomeWakeKey);
        checkPref = (SwitchPreference) findPreference(disableHardHomeWakeKey);
        checked = (Settings.System.getInt(this.getContext().getContentResolver(),
                disableHardHomeWakeKey, 0) == 1);
        checkPref.setChecked(checked);
        filterPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                boolean checked = ((SwitchPreference) preference).isChecked();
                int value = (checked ? 1 : 0);
                try {
                    Settings.System.putInt(
                            getContext().getContentResolver(),
                            disableHardHomeWakeKey,
                            value);
                    Log.d(this.getClass().getName(), "Successful!");
                    return true;
                } catch (Exception e) {
                    tweaksHelper.MakeToast(getResources().getString(R.string.error_write_settings));
                    return false;
                }
            }
        });


        //Recent apps button remap
        ListPreference mRecentKey = (ListPreference) findPreference("tweaks_recent_key_config");
        if (isGalaxyS8) {
            getPreferenceScreen().removePreference(mRecentKey);
        } else {
            //Button present, create listener
            mRecentKey.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    String textValue = newValue.toString();
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
                                getContext().getContentResolver(),
                                "tweaks_recent_key_config",
                                newint);
                        Log.d(this.getClass().getName(), "Successful!");
                        return true;
                    } catch (Exception e) {
                        tweaksHelper.MakeToast(getResources().getString(R.string.error_write_settings));
                        return false;
                    }
                }

            });
        }


        //Bixby button remap + custom app
        mBixbyRemap = (ListPreference) findPreference("tweaks_custom_bixby");
        mBixbyCustomApp = (ListPreference) findPreference("tweaks_custom_bixby_app");

        //workaround to have custom app last in array but not change its value
        if (bixbyRemapValue < 3) {
            try {
                mBixbyRemap.setValueIndex(bixbyRemapValue);
            } catch (ArrayIndexOutOfBoundsException e) {
                Log.e(DEBUGTAG, "outofbounds: " + this.getClass().getName());
            }
        } else if (bixbyRemapValue == 3) {
            try {
                mBixbyRemap.setValueIndex(mBixbyRemap.findIndexOfValue("3"));
            } catch (ArrayIndexOutOfBoundsException e) {
                Log.e(DEBUGTAG, "outofbounds: " + this.getClass().getName());
            }
        } else {
            try {
                mBixbyRemap.setValueIndex(bixbyRemapValue - 1);
            } catch (ArrayIndexOutOfBoundsException e) {
                Log.e(DEBUGTAG, "outofbounds: " + this.getClass().getName());
            }
        }

        //Remove it if ROM version is too old to have bixby remap support code
        if (isICE) {
            if (isNotePort) {
                if (isNightly) {
                    if (nightliesOfflineCurrentRevision < 82) {
                        getPreferenceScreen().removePreference(mBixbyRemap);
                        getPreferenceScreen().removePreference(mBixbyCustomApp);
                    }
                } else {
                    if (currentRomVersion < 12) {
                        getPreferenceScreen().removePreference(mBixbyRemap);
                        getPreferenceScreen().removePreference(mBixbyCustomApp);
                    }
                }
            } else {
                if (isNightly) {
                    if (nightliesOfflineCurrentRevision < 207) {
                        getPreferenceScreen().removePreference(mBixbyRemap);
                        getPreferenceScreen().removePreference(mBixbyCustomApp);
                    }
                } else {
                    if (currentRomVersion < 52) {
                        getPreferenceScreen().removePreference(mBixbyRemap);
                        getPreferenceScreen().removePreference(mBixbyCustomApp);
                    }
                }
            }
        }
        //Listener
        if (licenseRating < 16 /*if users has montly subscription (16points) or all 3 one time fee combined (17points) this option will be enabled */) {
            mBixbyRemap.setEnabled(false);
            mBixbyRemap.setSelectable(false);
            mBixbyRemap.setSummary(getResources().getString(R.string.nosubscription));
            mBixbyCustomApp.setEnabled(false);
            mBixbyCustomApp.setSelectable(false);
            mBixbyCustomApp.setSummary(getResources().getString(R.string.nosubscription));
        } else {
            populateAvailableAppsList(bixbySavedValue, mBixbyCustomApp);
            mBixbyRemap.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    String textValue = newValue.toString();
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
                                getContext().getContentResolver(),
                                "tweaks_custom_bixby",
                                newint);
                        Log.d(this.getClass().getName(), "Successful!");
                        if (newint != 3) {
                            mBixbyCustomApp.setEnabled(false);
                        } else {
                            mBixbyCustomApp.setEnabled(true);
                            mBixbyCustomApp.setSelectable(true);
                        }
                        return true;
                    } catch (Exception e) {
                        tweaksHelper.MakeToast(getResources().getString(R.string.error_write_settings));
                        return false;
                    }
                }
            });
        }
        if (bixbyRemapValue != 3) {
            mBixbyCustomApp.setEnabled(false);
            mBixbyCustomApp.setSelectable(false);
/*            if (TweaksHelper.isEmptyString(bixbySavedValue)) {
                mBixbyCustomApp.setSummary(getResources().getString(R.string.bixby_perapp_hint));
            }*/
        } else {
            //User wants custom app for bixby - create listener
            mBixbyCustomApp.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    String textValue = newValue.toString();
                    try {
                        Settings.System.putString(
                                getContext().getContentResolver(),
                                "tweaks_custom_bixby_app",
                                newValue.toString());
                        return true;
                    } catch (Exception e) {
                        tweaksHelper.MakeToast(getResources().getString(R.string.error_write_settings));
                        return false;
                    }
                }

            });
        }

        //Window Animation Scale Preference
        mWindowAnimation = (ListPreference) findPreference("window_animation_scale");
        //SET DEFAULT VALUE
        String globalWindowValue = Settings.Global.getString(this.getContext().getContentResolver(),
                "window_animation_scale");
        if (globalWindowValue == null) {
            mWindowAnimation.setValueIndex(4);
        } else {
            switch (globalWindowValue) {
                case "0":
                    mWindowAnimation.setValueIndex(0);
                    break;
                case ".25":
                    mWindowAnimation.setValueIndex(1);
                    break;
                case ".5":
                    mWindowAnimation.setValueIndex(2);
                    break;
                case ".75":
                    mWindowAnimation.setValueIndex(3);
                    break;
                case "1":
                    mWindowAnimation.setValueIndex(4);
                    break;
                case "1.25":
                    mWindowAnimation.setValueIndex(5);
                    break;
                case "1.5":
                    mWindowAnimation.setValueIndex(6);
                    break;
                case "1.75":
                    mWindowAnimation.setValueIndex(7);
                    break;
                case "2":
                    mWindowAnimation.setValueIndex(8);
                    break;
                case "5":
                    mWindowAnimation.setValueIndex(9);
                    break;
                case "10":
                    mWindowAnimation.setValueIndex(10);
                    break;
                default:
                    mWindowAnimation.setValueIndex(4);
                    break;
            }
        }
        //Listener
        mWindowAnimation.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                int index = 0;
                CharSequence[] entries = mWindowAnimation.getEntryValues();
                for (int i = 0; i < entries.length; i++) {
                    if (entries[i].equals(newValue)) {
                        index = i;
                        break;
                    }
                }
                String newWindowAnimation = (entries[index] + "");
                try {
                    Settings.Global.putString(
                            getContext().getContentResolver(),
                            "window_animation_scale",
                            newWindowAnimation);
                    Log.d(this.getClass().getName(), "Successful!");
                    return true;
                } catch (Exception e) {
                    tweaksHelper.MakeToast(getResources().getString(R.string.error_write_settings));
                    return false;
                }
            }
        });


        //Transition Animation Scale Preference
        mTransititionAnimation = (ListPreference) findPreference("transition_animation_scale");
        //Set default value
        String globalTransitionValue = Settings.Global.getString(this.getContext().getContentResolver(),
                "transition_animation_scale");

        if (TweaksHelper.isEmptyString(globalTransitionValue)) {
            mTransititionAnimation.setValueIndex(4);
        } else {
            switch (globalTransitionValue) {
                case "0":
                    mTransititionAnimation.setValueIndex(0);
                    break;
                case ".25":
                    mTransititionAnimation.setValueIndex(1);
                    break;
                case ".5":
                    mTransititionAnimation.setValueIndex(2);
                    break;
                case ".75":
                    mTransititionAnimation.setValueIndex(3);
                    break;
                case "1":
                    mTransititionAnimation.setValueIndex(4);
                    break;
                case "1.25":
                    mTransititionAnimation.setValueIndex(5);
                    break;
                case "1.5":
                    mTransititionAnimation.setValueIndex(6);
                    break;
                case "1.75":
                    mTransititionAnimation.setValueIndex(7);
                    break;
                case "2":
                    mTransititionAnimation.setValueIndex(8);
                    break;
                case "5":
                    mTransititionAnimation.setValueIndex(9);
                    break;
                case "10":
                    mTransititionAnimation.setValueIndex(10);
                    break;
                default:
                    mTransititionAnimation.setValueIndex(4);
                    break;
            }
        }
        //Listener
        mTransititionAnimation.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                int index = 0;
                CharSequence[] entries = mTransititionAnimation.getEntryValues();
                for (int i = 0; i < entries.length; i++) {
                    if (entries[i].equals(newValue)) {
                        index = i;
                        break;
                    }
                }
                String newWindowAnimation = (entries[index] + "");
                try {
                    Settings.Global.putString(
                            getContext().getContentResolver(),
                            "transition_animation_scale",
                            newWindowAnimation);
                    Log.d(this.getClass().getName(), "Successful!");
                    return true;
                } catch (Exception e) {
                    tweaksHelper.MakeToast(getResources().getString(R.string.error_write_settings));
                    return false;
                }
            }
        });

        //Animator Duration Scale
        mAnimatorDuration = (ListPreference) findPreference("animator_duration_scale");
        //Setting default value
        String globalAnimationValue = Settings.Global.getString(this.getContext().getContentResolver(),
                "animator_duration_scale");
        if (TweaksHelper.isEmptyString(globalAnimationValue)) {
            mAnimatorDuration.setValueIndex(4);
        } else {
            switch (globalAnimationValue) {
                case "0":
                    mAnimatorDuration.setValueIndex(0);
                    break;
                case ".25":
                    mAnimatorDuration.setValueIndex(1);
                    break;
                case ".5":
                    mAnimatorDuration.setValueIndex(2);
                    break;
                case ".75":
                    mAnimatorDuration.setValueIndex(3);
                    break;
                case "1":
                    mAnimatorDuration.setValueIndex(4);
                    break;
                case "1.25":
                    mAnimatorDuration.setValueIndex(5);
                    break;
                case "1.5":
                    mAnimatorDuration.setValueIndex(6);
                    break;
                case "1.75":
                    mAnimatorDuration.setValueIndex(7);
                    break;
                case "2":
                    mAnimatorDuration.setValueIndex(8);
                    break;
                case "5":
                    mAnimatorDuration.setValueIndex(9);
                    break;
                case "10":
                    mAnimatorDuration.setValueIndex(10);
                    break;
                default:
                    mAnimatorDuration.setValueIndex(4);
                    break;
            }
        }
        //Listener
        mAnimatorDuration.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                int index = 0;
                CharSequence[] entries = mAnimatorDuration.getEntryValues();
                for (int i = 0; i < entries.length; i++) {
                    if (entries[i].equals(newValue)) {
                        index = i;
                        break;
                    }
                }
                String newWindowAnimation = (entries[index] + "");
                try {
                    Settings.Global.putString(
                            getContext().getContentResolver(),
                            "animator_duration_scale",
                            newWindowAnimation);
                    Log.d(this.getClass().getName(), "Successful!");
                    return true;
                } catch (Exception e) {
                    tweaksHelper.MakeToast(getResources().getString(R.string.error_write_settings));
                    return false;
                }
            }
        });


        ListPreference mClockPos = (ListPreference) findPreference("tweaks_clock_position");
        String clockPosValue = Settings.System.getString(this.getContext().getContentResolver(),
                "tweaks_clock_position");
        if (TweaksHelper.isEmptyString(clockPosValue)) {
            mClockPos.setValueIndex(0);
        } else {
            switch (clockPosValue) {
                case "0":
                    mClockPos.setValueIndex(0);
                    break;
                case "1":
                    mClockPos.setValueIndex(1);
                    break;
                case "2":
                    mClockPos.setValueIndex(2);
                    break;
                case "3":
                    mClockPos.setValueIndex(3);
                    break;
            }
        }
        mClockPos.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
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
                            getContext().getContentResolver(),
                            "tweaks_clock_position",
                            newint);
                    Log.d(this.getClass().getName(), "Successful!");
                    return true;
                } catch (Exception e) {
                    tweaksHelper.MakeToast(getResources().getString(R.string.error_write_settings));
                    return false;
                }
            }
        });


        ListPreference mQsButtons = (ListPreference) findPreference("tweaks_quick_qs_buttons");
        String qsButtonsValue = Settings.System.getString(this.getContext().getContentResolver(),
                "tweaks_quick_qs_buttons");
        if (qsButtonsValue == null) {
            mQsButtons.setValueIndex(0);
        } else {
            switch (qsButtonsValue) {
                case "0":
                    mQsButtons.setValueIndex(0);
                    break;
                case "4":
                    mQsButtons.setValueIndex(1);
                    break;
                case "5":
                    mQsButtons.setValueIndex(2);
                    break;
                case "6":
                    mQsButtons.setValueIndex(3);
                    break;
                case "7":
                    mQsButtons.setValueIndex(4);
                    break;
                case "8":
                    mQsButtons.setValueIndex(5);
                    break;
            }
        }
        mQsButtons.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
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
                            getContext().getContentResolver(),
                            "tweaks_quick_qs_buttons",
                            newint);
                    Log.d(this.getClass().getName(), "Successful!");
                    //tweaksHelper.createRebootNotification();
                    return true;
                } catch (Exception e) {
                    tweaksHelper.MakeToast(getResources().getString(R.string.error_write_settings));
                    return false;
                }
            }
        });

        ListPreference mQsRow = (ListPreference) findPreference("qs_tile_row");
        String qsRowValue = Settings.Secure.getString(this.getContext().getContentResolver(),
                "qs_tile_row");
        if (qsRowValue == null) {
            mQsRow.setValueIndex(0);
        } else {
            switch (qsRowValue) {
                case "2":
                    mQsRow.setValueIndex(0);
                    break;
                case "3":
                    mQsRow.setValueIndex(1);
                    break;
                case "4":
                    mQsRow.setValueIndex(2);
                    break;
                case "5":
                    mQsRow.setValueIndex(3);
                    break;
                case "6":
                    mQsRow.setValueIndex(4);
                    break;
            }
        }
        mQsRow.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
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
                            getContext().getContentResolver(),
                            "qs_tile_row",
                            newint);
                    Log.d(this.getClass().getName(), "Successful!");
                    return true;
                } catch (Exception e) {
                    tweaksHelper.MakeToast(getResources().getString(R.string.error_write_settings));
                    return false;
                }
            }
        });

        ListPreference mQsColumn = (ListPreference) findPreference("qs_tile_column");
        String qsColumnValue = Settings.Secure.getString(this.getContext().getContentResolver(),
                "qs_tile_column");
        if (qsColumnValue == null) {
            mQsColumn.setValueIndex(0);
        } else {
            switch (qsColumnValue) {
                case "2":
                    mQsColumn.setValueIndex(0);
                    break;
                case "3":
                    mQsColumn.setValueIndex(1);
                    break;
                case "4":
                    mQsColumn.setValueIndex(2);
                    break;
                case "5":
                    mQsColumn.setValueIndex(3);
                    break;
            }
        }
        mQsColumn.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
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
                            getContext().getContentResolver(),
                            "qs_tile_column",
                            newint);
                    Log.d(this.getClass().getName(), "Successful!");
                    return true;
                } catch (Exception e) {
                    tweaksHelper.MakeToast(getResources().getString(R.string.error_write_settings));
                    return false;
                }
            }
        });


        ListPreference mBatthickness = (ListPreference) findPreference("battery_bar_thickness");
        String batThicknessValue = Settings.System.getString(this.getContext().getContentResolver(),
                "battery_bar_thickness");
        if (batThicknessValue == null) {
            mBatthickness.setValueIndex(0);
        } else {
            switch (batThicknessValue) {
                case "1":
                    mBatthickness.setValueIndex(0);
                    break;
                case "2":
                    mBatthickness.setValueIndex(1);
                    break;
                case "3":
                    mBatthickness.setValueIndex(2);
                    break;
                case "4":
                    mBatthickness.setValueIndex(3);
                    break;
            }
        }
        mBatthickness.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
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
                            getContext().getContentResolver(),
                            "battery_bar_thickness",
                            newint);
                    Log.d(this.getClass().getName(), "Successful!");
                    return true;
                } catch (Exception e) {
                    tweaksHelper.MakeToast(getResources().getString(R.string.error_write_settings));
                    return false;
                }
            }
        });


        filterPref = findPreference(navBarHeightKey);
        filterPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public boolean onPreferenceClick(Preference preference) {
                try {
                    navBarHeightDialog = seekDialog.getSeekDialog(SeekDialog.SeekEnum.eNavBarHeight);
                    navBarHeightDialog.show();
                    // Find all the IDs on the dialog (this has to be done after the dislog has been shown
                    // other wise you will crash the app
                    navBarHeightSeek = navBarHeightDialog.findViewById(R.id.seekbar1);
                    navBarHeightValue = navBarHeightDialog.findViewById(R.id.seek1Value);
                    Button navBarHeightMinus = navBarHeightDialog.findViewById(R.id.minus1);
                    Button navBarHeightPlus = navBarHeightDialog.findViewById(R.id.add1);
                    Button navBarHeightApply = navBarHeightDialog.findViewById(R.id.apply);
                    // Set the maximum the slider can go up to
                    navBarHeightSeek.setMax(navBarHeightSeekMax);
                    // Get the current value
                    navBarHeightProgress = (Settings.System.getInt(getContext().getContentResolver(),
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
                    return false;
                }

            }
        });

        filterPref = findPreference(allowCustomNavBarHeightKey);
        checkPref = (SwitchPreference) findPreference(allowCustomNavBarHeightKey);
        checked = (Settings.System.getInt(this.getContext().getContentResolver(),
                allowCustomNavBarHeightKey, 0) == 1);
        checkPref.setChecked(checked);
        filterPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                boolean checked = ((SwitchPreference) preference).isChecked();
                int value = (checked ? 1 : 0);
                try {
                    Settings.System.putInt(
                            getContext().getContentResolver(),
                            allowCustomNavBarHeightKey,
                            value);
                    Log.d(this.getClass().getName(), "Successful!");
                    return true;
                } catch (Exception e) {
                    tweaksHelper.MakeToast(getResources().getString(R.string.error_write_settings));
                    return false;
                }

            }
        });

        filterPref = findPreference(ShowNavbarKey);
        checkPref = (SwitchPreference) findPreference(ShowNavbarKey);
        checked = (Settings.System.getInt(this.getContext().getContentResolver(),
                ShowNavbarKey, 0) == 1);
        checkPref.setChecked(checked);
        if (isGalaxyS8) {
            getPreferenceScreen().removePreference(findPreference(ShowNavbarKey));
        } else {
            filterPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    boolean checked = ((SwitchPreference) preference).isChecked();
                    int value = (checked ? 1 : 0);
                    try {
                        Settings.System.putInt(
                                getContext().getContentResolver(),
                                ShowNavbarKey,
                                value);
                        Log.d(this.getClass().getName(), "Successful!");
                        tweaksHelper.createRebootNotification();
                        return true;
                    } catch (Exception e) {
                        tweaksHelper.MakeToast(getResources().getString(R.string.error_write_settings));
                        return false;
                    }
                }
            });
        }
    }

    private void populateAvailableAppsList(final String string, final ListPreference listPreference) {
        new Thread(new Runnable() {
            @Override
            public void run() {

                PackageManager pm = getContext().getPackageManager();
                Intent intent = new Intent()
                        .setAction(Intent.ACTION_MAIN)
                        .addCategory(Intent.CATEGORY_LAUNCHER);
                List<ResolveInfo> list = pm.queryIntentActivities(intent, 0);
                HashSet<String> packageNames = new HashSet<>(0);
                List<ApplicationInfo> appListInfo = new ArrayList<>();

                //adding unique packagenames to hashset
                for (ResolveInfo resolveInfo : list) {
                    packageNames.add(resolveInfo.activityInfo.packageName);
                }

                //get application infos and add them to arraylist
                for (String packageName : packageNames) {
                    try {
                        appListInfo.add(pm.getApplicationInfo(packageName, PackageManager.GET_META_DATA));
                    } catch (PackageManager.NameNotFoundException e) {
                        //Do Nothing
                    }
                }

                Collections.sort(appListInfo, new ApplicationInfo.DisplayNameComparator(pm));
                CharSequence[] entries = new CharSequence[appListInfo.size()];
                CharSequence[] entryValues = new CharSequence[appListInfo.size()];

                int j = 0;
                try {
                    int i = 0;
                    for (ApplicationInfo applicationInfo : appListInfo) {
                        entries[i] = applicationInfo.loadLabel(pm);
                        entryValues[i] = applicationInfo.packageName;
                        if (!TweaksHelper.isEmptyString(string)) {
                            if (entryValues[i].equals(string)) {
                                j = i;
                            }
                        }
                        i++;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                listPreference.setEntries(entries);
                listPreference.setEntryValues(entryValues);
                if (!TweaksHelper.isEmptyString(string)) {
                    try {
                        listPreference.setValueIndex(j);
                        listPreference.setDefaultValue(entries[j]);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                    //listPreference.setValueIndex(0);
                    listPreference.setSummary(R.string.bixby_perapp_hint);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }
}