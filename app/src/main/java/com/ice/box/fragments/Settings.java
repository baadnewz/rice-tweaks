package com.ice.box.fragments;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.util.Log;

import com.ice.box.R;
import com.ice.box.helpers.Constants;
import com.ice.box.helpers.RootUtils;
import com.ice.box.helpers.TweaksHelper;
import com.ice.box.receivers.MyAlarmReceiver;
import com.ice.box.receivers.MyBootReceiver;

import static com.ice.box.helpers.Constants.DEBUGTAG;
import static com.ice.box.helpers.Constants.isFreeVersionKey;


public class Settings extends PreferenceFragment implements
        Preference.OnPreferenceClickListener {
    private boolean checked;
    private TweaksHelper tweaksHelper;
    private int mThemeId = R.style.ThemeLight;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
        mThemeId = sharedPref.getInt("THEMEID", mThemeId);

        boolean isICE = (sharedPref.getBoolean("isICE", false));
        boolean isDeviceRooted = (sharedPref.getBoolean("isDeviceRooted", false));
        Boolean isSystemApp = sharedPref.getBoolean("isSystemApp", false);
        boolean isNightly = sharedPref.getBoolean("isNightly", false);
        boolean isFreeVersion = sharedPref.getBoolean(isFreeVersionKey, false);

        addPreferencesFromResource(R.xml.settings_preference);
        tweaksHelper = new TweaksHelper(getActivity());
        SwitchPreference checkPref;

        Preference filterPref = findPreference("sysapp");
        filterPref.setOnPreferenceClickListener(this);
        if (isICE) {
            getPreferenceScreen().removePreference(filterPref);
        } else if (!isDeviceRooted) {
            filterPref.setEnabled(false);
            filterPref.setSummary(R.string.not_rooted);
        } else if (isSystemApp) {
            getPreferenceScreen().removePreference(filterPref);
        }

        filterPref = findPreference("force_english");
        filterPref.setOnPreferenceClickListener(this);
        checkPref = (SwitchPreference) findPreference("force_english");
        checked = (sharedPref.getBoolean("forceEnglish", false));
        checkPref.setChecked(checked);

        filterPref = findPreference("switch_theme");
        filterPref.setOnPreferenceClickListener(this);
        checkPref = (SwitchPreference) findPreference("switch_theme");
        if (isFreeVersion) {
            filterPref.setSummary(getResources().getString(R.string.free_notification));
            filterPref.setEnabled(false);
            checkPref.setChecked(false);
        } else if (mThemeId == R.style.ThemeDark) {
            checkPref.setChecked(true);
        }

        filterPref = findPreference("romupdate_start");
        filterPref.setOnPreferenceClickListener(this);
        checkPref = (SwitchPreference) findPreference("romupdate_start");
        checked = (sharedPref.getBoolean("romupdate_start", false));
        checkPref.setChecked(checked);
        if (!isICE) {
            getPreferenceScreen().removePreference(filterPref);
        } else if (isNightly) {
            getPreferenceScreen().removePreference(filterPref);
        }

        filterPref = findPreference("romupdate");
        filterPref.setOnPreferenceClickListener(this);
        checkPref = (SwitchPreference) findPreference("romupdate");
        checked = (sharedPref.getBoolean("romupdate", false));
        checkPref.setChecked(checked);
        if (!isICE) {
            getPreferenceScreen().removePreference(filterPref);
        } else if (isNightly) {
            getPreferenceScreen().removePreference(filterPref);
        } else if (isFreeVersion) {
            checkPref.setChecked(false);
            filterPref.setEnabled(false);
            filterPref.setSummary(getResources().getString(R.string.free_notification));
        }
        filterPref = findPreference("romupdateinterval");
        if (!isICE) {
            getPreferenceScreen().removePreference(filterPref);
            getPreferenceScreen().removePreference(findPreference("romupdatescat"));
        } else if (isNightly) {
            getPreferenceScreen().removePreference(filterPref);
            getPreferenceScreen().removePreference(findPreference("romupdatescat"));
        }


        filterPref = findPreference("nightlyupdate_start");
        filterPref.setOnPreferenceClickListener(this);
        checkPref = (SwitchPreference) findPreference("nightlyupdate_start");
        checked = (sharedPref.getBoolean("nightlyupdate_start", false));
        checkPref.setChecked(checked);
        if (!isICE) {
            getPreferenceScreen().removePreference(filterPref);
        } else if (!isNightly) {
            getPreferenceScreen().removePreference(filterPref);
        }

        filterPref = findPreference("nightlyupdate");
        filterPref.setOnPreferenceClickListener(this);
        checkPref = (SwitchPreference) findPreference("nightlyupdate");
        checked = (sharedPref.getBoolean("nightlyupdate", false));
        //Log.d(DEBUGTAG, "checked nightlyupdate: " + checked);
        checkPref.setChecked(checked);
        if (!isICE) {
            getPreferenceScreen().removePreference(filterPref);
        } else if (!isNightly) {
            getPreferenceScreen().removePreference(filterPref);
        } else if (isFreeVersion) {
            checkPref.setChecked(false);
            filterPref.setEnabled(false);
            filterPref.setSummary(getResources().getString(R.string.free_notification));
        }

        filterPref = findPreference("nightlyupdateinterval");
        if (!isICE) {
            getPreferenceScreen().removePreference(filterPref);
            getPreferenceScreen().removePreference(findPreference("romupdatesnightlycat"));
        } else if (!isNightly) {
            getPreferenceScreen().removePreference(filterPref);
            getPreferenceScreen().removePreference(findPreference("romupdatesnightlycat"));
        }

    }

    public boolean onPreferenceClick(Preference preference) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
        boolean isFreeVersion = sharedPref.getBoolean(isFreeVersionKey, false);
        boolean checked;
        MyBootReceiver myBootReceiver;
        myBootReceiver = new MyBootReceiver();

        if (preference.getKey().equals("sysapp")) {
            String appPath = getContext().getPackageResourcePath();
            String appName = getResources().getString(R.string.app_name).replace(" ", "");
            RootUtils.mountSystemRW(true);
            RootUtils.runCommand(Constants.BUSYBOX_PATH + " test -d /system/priv-app/" + appName
                    + " || " + Constants.BUSYBOX_PATH + " mkdir -p /system/priv-app/" + appName +
                    " " +
                    "&& " + Constants.BUSYBOX_PATH + " cp " + appPath + " /system/priv-app/" +
                    appName + "/" + appName + ".apk " +
                    "&& " + Constants.BUSYBOX_PATH + " chmod -R 755 /system/priv-app/" + appName);
            RootUtils.mountSystemRW(false);
            tweaksHelper.createRebootNotification();
        }

        if (preference.getKey().equals("force_english")) {
            try {
                sharedPref.edit().putBoolean("forceEnglish", (((SwitchPreference) preference)
                        .isChecked())).apply();
                Log.d(this.getClass().getName(), "Successful!");
            } catch (Exception e) {
                tweaksHelper.MakeToast(getResources().getString(R.string.toast_error_apply));
            }
            Intent i = getContext().getPackageManager()
                    .getLaunchIntentForPackage(getContext().getPackageName());
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
        }

        if (preference.getKey().equals("switch_theme")) {
            checked = ((SwitchPreference) preference).isChecked();
            if (!isFreeVersion) {
                try {
                    if (checked) {
                        sharedPref.edit().putInt("THEMEID", R.style.ThemeDark).apply();
                        getActivity().finish();
                    } else {
                        sharedPref.edit().putInt("THEMEID", R.style.ThemeLight).apply();
                        getActivity().finish();
                    }
                    Intent i = getContext().getPackageManager()
                            .getLaunchIntentForPackage(getContext().getPackageName());
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);
                } catch (Exception e) {
                    tweaksHelper.MakeToast(getResources().getString(R.string.toast_error_apply));
                }
            }
        }

        if (preference.getKey().equals("romupdate_start")) {
            try {
                sharedPref.edit().putBoolean("romupdate_start", (((SwitchPreference) preference)
                        .isChecked())).apply();
                Log.d(this.getClass().getName(), "Successful!");
            } catch (Exception e) {
                tweaksHelper.MakeToast(getResources().getString(R.string.toast_error_apply));
            }
        }
        if (preference.getKey().equals("romupdate")) {
            try {
                sharedPref.edit().putBoolean("romupdate", (((SwitchPreference) preference)
                        .isChecked())).apply();
                sharedPref.edit().putBoolean("nightlyupdate", false).apply();
                Log.d(this.getClass().getName(), "Successful!");
                //boolean romupdate = sharedPref.getBoolean("romupdate", false);
                //if (romupdate)
                myBootReceiver.scheduleAlarm(getContext());
            } catch (Exception e) {
                tweaksHelper.MakeToast(getResources().getString(R.string.toast_error_apply));
            }
        }
        if (preference.getKey().equals("nightlyupdate_start")) {
            try {
                sharedPref.edit().putBoolean("nightlyupdate_start", (((SwitchPreference) preference)
                        .isChecked())).apply();
                Log.d(this.getClass().getName(), "Successful!");
            } catch (Exception e) {
                tweaksHelper.MakeToast(getResources().getString(R.string.toast_error_apply));
            }
        }
        if (preference.getKey().equals("nightlyupdate")) {
            try {
                sharedPref.edit().putBoolean("nightlyupdate", (((SwitchPreference) preference)
                        .isChecked())).apply();
                sharedPref.edit().putBoolean("romupdate", false).apply();
                Log.d(this.getClass().getName(), "SuccessfulXX!");

               // Log.d(DEBUGTAG, "nightlyupdate: " + sharedPref.getBoolean("nightlyupdate", false));
                //boolean nightlyupdate = sharedPref.getBoolean("nightlyupdate", false);
                //if (nightlyupdate)
                myBootReceiver.scheduleNightlyAlarm(getContext());
            } catch (Exception e) {
                tweaksHelper.MakeToast(getResources().getString(R.string.toast_error_apply));
            }
        }
        return false;
    }

   /* private void scheduleAlarm() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
        int checkinFreq = Integer.parseInt(sharedPref.getString("romupdateinterval", "24"));
        boolean romupdate = sharedPref.getBoolean("romupdate", false);
        Intent intent = new Intent(getContext(), MyAlarmReceiver.class);
        PendingIntent pIntent = PendingIntent.getBroadcast(getContext(),
                0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        long firstMillis = System.currentTimeMillis(); // alarm is set right away
        AlarmManager alarm = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);
        alarm.cancel(pIntent);
        if (romupdate) {
            alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, firstMillis,
                    1000 * 60 * 60 * checkinFreq, pIntent);
            //scheduleNightlyAlarm();
        } else {
            alarm.cancel(pIntent);
        }
        //Fires up every 10 seconds for testing purposes
        //alarm.setRepeating(AlarmManager.RTC_WAKEUP, firstMillis, 10000, pIntent);
    }

    private void scheduleNightlyAlarm() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
        int checkinFreq = Integer.parseInt(sharedPref.getString("nightlyupdateinterval", "12"));
        boolean nightlyupdate = sharedPref.getBoolean("nightlyupdate", false);
        Intent intent = new Intent(getContext(), MyAlarmReceiver.class);
        PendingIntent pIntent = PendingIntent.getBroadcast(getContext(),
                0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        long firstMillis = System.currentTimeMillis(); // alarm is set right away
        AlarmManager alarm = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);
        alarm.cancel(pIntent);
        // First parameter is the type: ELAPSED_REALTIME, , RTC_WAKEUP
        // Interval can be INTERVAL_FIFTEEN_MINUTES, INTERVAL_HALF_HOUR, INTERVAL_HOUR, INTERVAL_DAY
*//*        alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, firstMillis,
                AlarmManager.INTERVAL_DAY, pIntent);*//*
        if (nightlyupdate) {
            alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, firstMillis,
                    1000 * 60 * 60 * checkinFreq, pIntent);
            //scheduleAlarm();
            *//*        alarm.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                0,
                1000 * 60 * 60 * checkinFreq,
                pIntent);*//*
        } else {
            alarm.cancel(pIntent);
        }

        //alarm.setRepeating(AlarmManager.RTC_WAKEUP, firstMillis, 10000, pIntent);

    }*/
}