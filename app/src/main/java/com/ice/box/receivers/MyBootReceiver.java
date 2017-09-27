package com.ice.box.receivers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.WakefulBroadcastReceiver;

import static com.ice.box.helpers.Constants.isFreeVersionKey;
import static com.ice.box.helpers.Constants.isNightlyKey;

/**
 * Created by Adrian on 03.05.2017.
 */

// WakefulBroadcastReceiver ensures the device does not go back to sleep
// during the startup of the service
@SuppressWarnings("DefaultFileTemplate")
public class MyBootReceiver extends WakefulBroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // Launch the specified service when this message is received
/*        Intent startServiceIntent = new Intent(context, MyService.class);
        startWakefulService(context, startServiceIntent);*/
        //startWakefulService(context, scheduleAlarm());
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        boolean isNightly = sharedPref.getBoolean(isNightlyKey, false);
        boolean isFreeVersion = sharedPref.getBoolean(isFreeVersionKey, false);

        //Log.d(DEBUGTAG, "nightlyupdateinterval: " + checkinFreq);
        if (!isFreeVersion) {
            if (isNightly) {
                if (sharedPref.getBoolean("nightlyupdate", false)) {
                    scheduleNightlyAlarm(context);
                }
            } else {
                if (sharedPref.getBoolean("romupdate", false)) {
                    scheduleAlarm(context);
                }

            }
        }


    }

    public void scheduleAlarm(Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        int checkinFreq = Integer.parseInt(sharedPref.getString("romupdateinterval", "24"));
        boolean romupdate = sharedPref.getBoolean("romupdate", false);
        Intent intent = new Intent(context, MyAlarmReceiver.class);
        PendingIntent pIntent = PendingIntent.getBroadcast(context,
                0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        long firstMillis = System.currentTimeMillis(); // alarm is set right away
        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarm.cancel(pIntent);
        // First parameter is the type: ELAPSED_REALTIME, , RTC_WAKEUP
        // Interval can be INTERVAL_FIFTEEN_MINUTES, INTERVAL_HALF_HOUR, INTERVAL_HOUR, INTERVAL_DAY
        if (romupdate) {
            alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, firstMillis,
                    1000 * 60 * 60 * checkinFreq, pIntent);
        } else {
            alarm.cancel(pIntent);
        }
    }

    public void scheduleNightlyAlarm(Context context){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        int checkinFreq = Integer.parseInt(sharedPref.getString("nightlyupdateinterval", "12"));
        boolean nightlyupdate = sharedPref.getBoolean("nightlyupdate", false);
        Intent intent = new Intent(context, MyAlarmReceiver.class);
        PendingIntent pIntent = PendingIntent.getBroadcast(context,
                0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        long firstMillis = System.currentTimeMillis(); // alarm is set right away
        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarm.cancel(pIntent);
        // First parameter is the type: ELAPSED_REALTIME, , RTC_WAKEUP
        // Interval can be INTERVAL_FIFTEEN_MINUTES, INTERVAL_HALF_HOUR, INTERVAL_HOUR, INTERVAL_DAY
/*        alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, firstMillis,
                AlarmManager.INTERVAL_DAY, pIntent);*/
        if (nightlyupdate) {
            alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, firstMillis,
                    1000 * 60 * 60 * checkinFreq, pIntent);
        } else {
            alarm.cancel(pIntent);
        }
/*        alarm.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                0,
                1000 * 60 * 60 * checkinFreq,
                pIntent);*/
        //alarm.setRepeating(AlarmManager.RTC_WAKEUP, firstMillis, 10000, pIntent);

    }


}