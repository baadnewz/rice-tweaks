package com.ice.box.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.ice.box.services.MyService;

/**
 * Created by Adrian on 03.05.2017.
 */

public class MyAlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent i = new Intent(context, MyService.class);
        context.startService(i);
        //Log.d(DEBUGTAG, "Alarm fired");
    }
}