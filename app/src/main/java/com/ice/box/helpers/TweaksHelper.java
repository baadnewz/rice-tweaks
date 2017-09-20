package com.ice.box.helpers;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.ice.box.R;
import com.ice.box.receivers.NotificationReceiver;

import static com.ice.box.helpers.Constants.onlineNightlyVersionKey;
import static com.ice.box.helpers.Constants.onlineStableVersionTextKey;
import static com.ice.box.helpers.Constants.riceWebsiteLink;
import static com.ice.box.helpers.RootUtils.rootAccess;


/**
 * Created by Adrian on 02.05.2017.
 */

@SuppressWarnings("DefaultFileTemplate")
public class TweaksHelper {
    private final Context mContext;

    public TweaksHelper(Context pContext) {
        mContext = pContext;
    }

    public static String returnSwitch(String current) {
        try {
            switch (current) {
                case "":
                    return String.valueOf("1");
                case "0":
                    return String.valueOf("1");
                case "1":
                    return String.valueOf("0");
                case "true":
                    return String.valueOf("false");
                case "false":
                    return String.valueOf("true");
                case "adb":
                    return String.valueOf("mtp,adb");
                case "mtp,adb":
                    return String.valueOf("adb");
                case "Y":
                    return String.valueOf("N");
                case "N":
                    return String.valueOf("Y");
                case "immersive.full=":
                    return String.valueOf("immersive.full=*");
                case "immersive.full=*":
                    return String.valueOf("immersive.full=");
            }
        } catch (Exception e) {
            System.out.println("Error " + e.getMessage());
        }
        return returnSwitch(current);
    }

    public void MakeToast(String string) {
        Toast.makeText(mContext, string, Toast.LENGTH_LONG).show();
    }

    public void createRomNotification() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
        String onlineRomVersionText = sharedPref.getString(onlineStableVersionTextKey, null);
        Intent intent;
        intent = new Intent(Intent.ACTION_VIEW,
                Uri.parse(riceWebsiteLink));

        intent.setClass(mContext, NotificationReceiver.class);
        intent.setAction("romupdate");
        Intent dismissIntent = new Intent();
        dismissIntent.setClass(mContext, NotificationReceiver.class);
        dismissIntent.setAction("romupdate_later");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, 4,
                intent, PendingIntent.FLAG_CANCEL_CURRENT);
        PendingIntent pendingDismissIntent = PendingIntent.getBroadcast(mContext, 4,
                dismissIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        NotificationManager notificationManager = (NotificationManager) mContext.getSystemService
                (Context.NOTIFICATION_SERVICE);
        Notification.Action action = new Notification.Action.Builder(
                Icon.createWithResource(mContext, android.R.drawable.ic_popup_sync),
                mContext.getResources().getString(R.string.update_notification),
                pendingIntent).build();
        Notification.Action action1 = new Notification.Action.Builder(
                Icon.createWithResource(mContext, android.R.drawable.ic_menu_close_clear_cancel),
                mContext.getResources().getString(R.string.later_notification),
                pendingDismissIntent).build();
        Notification notification = new Notification.Builder(mContext)
                .setPriority(Notification.PRIORITY_MAX)
                .setDefaults(Notification.DEFAULT_ALL)
                .setOnlyAlertOnce(true)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(mContext.getResources().getString(R.string.app_name))
                .setStyle(new Notification.BigTextStyle()
                        .bigText((mContext.getResources().getString(R.string.rom)) + " " +
                                onlineRomVersionText + " " + mContext.getResources().getString(R
                                .string.update) + "\n"
                                + mContext.getResources().getString(R.string.rom_update)))
                .setContentIntent(pendingIntent)
                .addAction(action1)
                .addAction(action) //here launches the link but doesn't dismiss the notification
                .setAutoCancel(true)
                .build();
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        notificationManager.notify(4, notification);
    }

    public void createNightlyNotification() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
        int nightliesOnlineCurrentRevision = sharedPref.getInt(onlineNightlyVersionKey, 1);
        Intent intent;
        intent = new Intent(Intent.ACTION_VIEW,
                Uri.parse(riceWebsiteLink));
        intent.setClass(mContext, NotificationReceiver.class);
        intent.setAction("nightlyupdate");
        Intent dismissIntent = new Intent();
        dismissIntent.setClass(mContext, NotificationReceiver.class);
        dismissIntent.setAction("nightlyupdate_later");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, 6,
                intent, PendingIntent.FLAG_CANCEL_CURRENT);
        PendingIntent pendingDismissIntent = PendingIntent.getBroadcast(mContext, 6,
                dismissIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        NotificationManager notificationManager = (NotificationManager) mContext.getSystemService
                (Context.NOTIFICATION_SERVICE);
        Notification.Action action = new Notification.Action.Builder(
                Icon.createWithResource(mContext, android.R.drawable.ic_popup_sync),
                mContext.getResources().getString(R.string.update_notification),
                pendingIntent).build();
        Notification.Action action1 = new Notification.Action.Builder(
                Icon.createWithResource(mContext, android.R.drawable.ic_menu_close_clear_cancel),
                mContext.getResources().getString(R.string.later_notification),
                pendingDismissIntent).build();
        Notification notification = new Notification.Builder(mContext)
                .setPriority(Notification.PRIORITY_MAX)
                .setDefaults(Notification.DEFAULT_ALL)
                .setOnlyAlertOnce(true)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(mContext.getResources().getString(R.string.app_name))
                .setStyle(new Notification.BigTextStyle()
                        .bigText((mContext.getResources().getString(R.string.nightly)) + " R" +
                                nightliesOnlineCurrentRevision + " " + mContext.getResources().getString(R
                                .string.update) + "\n"
                                + mContext.getResources().getString(R.string.rom_update)))
                .setContentIntent(pendingIntent)
                .addAction(action1)
                .addAction(action) //here launches the link but doesn't dismiss the notification
                .setAutoCancel(true)
                .build();
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        notificationManager.notify(6, notification);
    }

    public void createAppNotification() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
        String onlineAppVersionText = sharedPref.getString("onlineAppVersionText", null);
        Intent intent;
        intent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/details?id=com.ice.box"));
        intent.setClass(mContext, NotificationReceiver.class);
        intent.setAction("appupdate");
        Intent dismissIntent = new Intent();
        dismissIntent.setClass(mContext, NotificationReceiver.class);
        dismissIntent.setAction("appupdate_later");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, 5,
                intent, PendingIntent.FLAG_CANCEL_CURRENT);
        PendingIntent pendingDismissIntent = PendingIntent.getBroadcast(mContext, 5,
                dismissIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        NotificationManager notificationManager = (NotificationManager) mContext.getSystemService
                (Context.NOTIFICATION_SERVICE);
        Notification.Action action = new Notification.Action.Builder(
                Icon.createWithResource(mContext, android.R.drawable.ic_popup_sync),
                mContext.getResources().getString(R.string.update_notification),
                pendingIntent).build();
        Notification.Action action1 = new Notification.Action.Builder(
                Icon.createWithResource(mContext, android.R.drawable.ic_menu_close_clear_cancel),
                mContext.getResources().getString(R.string.later_notification),
                pendingDismissIntent).build();
        Notification notification = new Notification.Builder(mContext)
                .setPriority(Notification.PRIORITY_MAX)
                .setDefaults(Notification.DEFAULT_ALL)
                .setOnlyAlertOnce(true)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(mContext.getResources().getString(R.string.app_name))
                .setStyle(new Notification.BigTextStyle()
                        .bigText((mContext.getResources().getString(R.string.app_name)) + " " +
                                onlineAppVersionText + " " + mContext.getResources().getString(R
                                .string.update) + "\n"
                                + mContext.getResources().getString(R.string.app_update)))
                .setContentIntent(pendingIntent)
                .addAction(action1)
                .addAction(action) //here launches the link but doesn't dismiss the notification
                .setAutoCancel(true)
                .build();
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        notificationManager.notify(5, notification);
    }

    public void createSystemUINotification() {
        Intent intent = new Intent(mContext, RestartSystemUI.class);
        intent.setClass(mContext, NotificationReceiver.class);
        intent.setAction("systemui");
        Intent dismissIntent = new Intent();
        dismissIntent.setClass(mContext, NotificationReceiver.class);
        dismissIntent.setAction("systemui_later");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, 2,
                intent, PendingIntent.FLAG_CANCEL_CURRENT);
        PendingIntent pendingDismissIntent = PendingIntent.getBroadcast(mContext, 2,
                dismissIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        Notification.Action action = new Notification.Action.Builder(
                Icon.createWithResource(mContext, android.R.drawable.ic_popup_sync),
                mContext.getResources().getString(R.string.apply_notification),
                pendingIntent).build();
        Notification.Action action1 = new Notification.Action.Builder(
                Icon.createWithResource(mContext, android.R.drawable.ic_menu_close_clear_cancel),
                mContext.getResources().getString(R.string.later_notification),
                pendingDismissIntent).build();
        NotificationManager notificationManager = (NotificationManager) mContext.getSystemService
                (Context.NOTIFICATION_SERVICE);
        Notification notification = new Notification.Builder(mContext)
                .setPriority(Notification.PRIORITY_MAX)
                .setDefaults(Notification.DEFAULT_ALL)
                .setOnlyAlertOnce(true)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(mContext.getResources().getString(R.string.app_name))
                .setContentText(mContext.getString(R.string.tweakshelper_reboot_sysui1))
                .setContentIntent(pendingIntent)
                .addAction(action1)
                .addAction(action)
                .setAutoCancel(true)
                .build();
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        notificationManager.notify(2, notification);
    }

    public void createRebootNotification() {
        Intent intent = new Intent(mContext, RestartOS.class);
        intent.setClass(mContext, NotificationReceiver.class);
        intent.setAction("reboot");
        Intent dismissIntent = new Intent();
        dismissIntent.setClass(mContext, NotificationReceiver.class);
        dismissIntent.setAction("reboot_later");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, 3,
                intent, PendingIntent.FLAG_CANCEL_CURRENT);
        PendingIntent pendingDismissIntent = PendingIntent.getBroadcast(mContext, 3,
                dismissIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        Notification.Action action = new Notification.Action.Builder(
                Icon.createWithResource(mContext, android.R.drawable.ic_popup_sync),
                mContext.getResources().getString(R.string.apply_notification),
                pendingIntent).build();
        Notification.Action action1 = new Notification.Action.Builder(
                Icon.createWithResource(mContext, android.R.drawable.ic_menu_close_clear_cancel),
                mContext.getResources().getString(R.string.later_notification),
                pendingDismissIntent).build();
        NotificationManager notificationManager = (NotificationManager)
                mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = new Notification.Builder(mContext)
                .setPriority(Notification.PRIORITY_MAX)
                .setDefaults(Notification.DEFAULT_ALL)
                .setOnlyAlertOnce(true)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(mContext.getResources().getString(R.string.app_name))
                .setContentText(
                        mContext.getResources().getString(R.string.tweakshelper_reboot_system1))
                .setContentIntent(pendingIntent)
                .addAction(action1)
                .addAction(action)
                .setAutoCancel(true)
                .build();
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        notificationManager.notify(3, notification);
    }


    ///OLD CODE - will need improvements at a later date!!
    public void createCSCNotification() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
        alertDialog.setTitle(mContext.getResources().getString(R.string.app_name));
        alertDialog.setMessage(mContext.getString(R.string.tweakshelper_csc));
        alertDialog.setPositiveButton(mContext.getResources().getString(R.string.ok), new
                DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        RootUtils.runCommand("am start com.sec.android.Preconfig/.Preconfig");
                    }
                });
        alertDialog.show();
    }

    public void createSafetyNotification() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
        alertDialog.setTitle(mContext.getResources().getString(R.string.app_name));
        alertDialog.setMessage(mContext.getString(R.string.tweakshelper_not_recommended));
        alertDialog.setPositiveButton(mContext.getResources().getString(R.string.ok), new
                DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
        alertDialog.show();
    }

    public void createSafetyNetNotification() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
        alertDialog.setTitle(mContext.getResources().getString(R.string.app_name));
        alertDialog.setMessage(mContext.getString(R.string.tweakshelper_safetynet));
        alertDialog.setPositiveButton(mContext.getResources().getString(R.string.ok), new
                DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
        alertDialog.show();
    }

    public void createBackupSuccessNotification() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
        alertDialog.setTitle(mContext.getString(R.string.tweakshelper_success));
        alertDialog.setMessage(mContext.getString(R.string.tweakshelper_color_backup));
        alertDialog.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            }
        });
        alertDialog.show();
    }

    public void createBackupFailedNotification() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
        alertDialog.setTitle(mContext.getString(R.string.tweakshelper_error));
        alertDialog.setMessage(mContext.getString(R.string.tweakshelper_color_backup_failed));
        alertDialog.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            }
        });
        alertDialog.show();
    }

    public void createRestoreSuccessNotification() {
        {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
            alertDialog.setTitle(mContext.getString(R.string.tweakshelper_success));
            alertDialog.setMessage(mContext.getString(R.string.tweakshelper_color_restore));
            alertDialog.setPositiveButton(android.R.string.ok,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Intent intent = new Intent(mContext, RestartSystemUI.class);
                            mContext.startActivity(intent);
                        }
                    });

            alertDialog.setNegativeButton(mContext.getString(R.string.tweakshelper_later),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
            alertDialog.show();
        }
    }

    public void createRestoreFailedNotification() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
        alertDialog.setTitle(mContext.getString(R.string.tweakshelper_error));
        alertDialog.setMessage(mContext.getString(R.string.tweakshelper_color_restore_failed));
        alertDialog.setPositiveButton(android.R.string.ok,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
        alertDialog.show();
    }

    public void killPackage (String packageName) {
        String appName = "";
        PackageManager packageManager= mContext.getPackageManager();
        if (RootUtils.rootAccess()) {
            RootUtils.runCommand("kill -9 " + packageName);
            try {
                appName = (String) packageManager.getApplicationLabel(packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA));
                MakeToast(appName + " " + mContext.getResources().getString(R.string.kill_package));
            } catch (PackageManager.NameNotFoundException e) {
                MakeToast(packageName + " " + mContext.getResources().getString(R.string.kill_package));

            }
        } else {
            try {
                appName = (String) packageManager.getApplicationLabel(packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA));
                MakeToast(appName + " " + mContext.getResources().getString(R.string.kill_package_error));
            } catch (PackageManager.NameNotFoundException e) {
                MakeToast(packageName + " " + mContext.getResources().getString(R.string.kill_package_error));

            }
        }
    }

    public static boolean isEmptyString(String text) {
        return (text == null || text.trim().equals("null") || text.trim()
                .length() <= 0);
    }
}