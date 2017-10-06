package com.ice.box.receivers;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;

import com.ice.box.helpers.Constants;
import com.ice.box.helpers.RestartOS;
import com.ice.box.helpers.RestartSystemUI;

import static com.ice.box.helpers.Constants.DEBUGTAG;
import static com.ice.box.helpers.Constants.riceWebsiteLink;


/**
 * Created by Adrian on 03.05.2017.
 */

public class NotificationReceiver extends BroadcastReceiver {
    private NotificationManager notificationManager;

    public void onReceive(Context context, Intent intent) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        boolean isNote8Port = sharedPref.getBoolean(Constants.isNote8PortKey, false);
        switch (intent.getAction()) {
            case "romupdate":
                intent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse(riceWebsiteLink));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
                notificationManager.cancel(4);
                break;
            case "romupdate_later":
                notificationManager.cancel(4);
                break;
            case "appupdate":
                intent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/details?id=com.ice.box"));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
                notificationManager.cancel(5);
                break;
            case "appupdate_later":
                notificationManager.cancel(5);
                break;
            case "reboot":
                intent = new Intent(context, RestartOS.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
                notificationManager.cancel(3);
                break;
            case "reboot_later":
                notificationManager.cancel(3);
                break;
            case "systemui":
                intent = new Intent(context, RestartSystemUI.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
                notificationManager.cancel(2);
                break;
            case "systemui_later":
                notificationManager.cancel(2);
                break;
            case "nightlyupdate":
                if (isNote8Port) {
                    intent = new Intent(Intent.ACTION_VIEW,
                            Uri.parse(riceWebsiteLink + "nightlies_great-port.html"));
                } else {
                    intent = new Intent(Intent.ACTION_VIEW,
                            Uri.parse(riceWebsiteLink + "nightlies_dream.html"));
                }
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
                notificationManager.cancel(6);
                break;
            case "nightlyupdate_later":
                notificationManager.cancel(6);
                break;

        }
    }
}