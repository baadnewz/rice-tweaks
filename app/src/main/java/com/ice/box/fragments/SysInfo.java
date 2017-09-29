package com.ice.box.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import com.ice.box.R;
import com.ice.box.helpers.RootUtils;
import com.ice.box.helpers.SystemProperties;
import com.ice.box.helpers.Tools;


@SuppressWarnings("unused")
public class SysInfo extends PreferenceFragment implements Preference.OnPreferenceClickListener {
    private final static String MAGISK_VER = "magisk.version";
    //private int mThemeId = R.style.ThemeLight;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.sysinfo_preference);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());;

        boolean isDeviceRooted = (sharedPref.getBoolean("isDeviceRooted", false));
        boolean isDeviceRootPresent = (sharedPref.getBoolean("isDeviceRootPresent", false));
        String magiskver = SystemProperties.get(MAGISK_VER);
        Boolean magisk = (magiskver != null && !magiskver.isEmpty());
        Preference filterPref;

/*        mThemeId = sharedPref.getInt("THEMEID", mThemeId);
        if (mThemeId == R.style.ThemeDark) {
            filterPref = findPreference("sysinfo");
            filterPref.setLayoutResource(R.layout.sysinfo_dark);
        }*/

/*        filterPref = findPreference("sysinfo");
        filterPref.setSelectable(false);*/

        filterPref = findPreference("root_Version");
        filterPref.setOnPreferenceClickListener(this);
        String rootstatus;
        if (!magisk) {
            if (isDeviceRootPresent) {
                if (isDeviceRooted) {
                    rootstatus = getResources().getString(R.string.rooted);
                    filterPref.setSelectable(false);
                } else {
                    rootstatus =
                            getResources().getString(R.string.root_denied) + ". " +
                                    getResources().getString(R.string.request_root);
                }
            } else {
                rootstatus =
                        getResources().getString(R.string.rooted_not) + ". " +
                                getResources().getString(R.string.root_not_limited) + ".\n" +
                                getResources().getString(R.string.tap_to_download);
            }
        } else {
            if (isDeviceRootPresent) {
                if (isDeviceRooted) {
                    rootstatus = (
                            getResources().getString(R.string.magisc_v) + magiskver + " " +
                                    getResources().getString(R.string.mounted) + "\n" +
                                    getResources().getString(R.string.launchmanager));
                } else {
                    rootstatus = (
                            getResources().getString(R.string.magisc_v) + magiskver + " " +
                                    getResources().getString(R.string.mounted) + "\n" +
                                    getResources().getString(R.string.root_denied));
                }
            } else {
                rootstatus = (
                        getResources().getString(R.string.magisc_v) + magiskver + " " +
                                getResources().getString(R.string.unmounted) + "\n" +
                                getResources().getString(R.string.launchmanager));
            }
        }
        filterPref.setSummary(rootstatus);

        filterPref = findPreference("rom_busybox");
        filterPref.setOnPreferenceClickListener(this);
        String busybox;
        String checkBusyBoxFirst = Tools.readCommandOutput("busybox");
        if (RootUtils.busyboxInstalled() &&
                checkBusyBoxFirst != null &&
                checkBusyBoxFirst.length() > 0) {
            busybox = Tools.readCommandOutput("busybox");
            filterPref.setSelectable(false);
        } else {
            busybox = getResources().getString(R.string.busybox_not) + " " +
                    getResources().getString(R.string.tap_to_download);
        }
        filterPref.setSummary(busybox);

        filterPref = findPreference("device_model");
        filterPref.setSelectable(false);
        filterPref.setSummary(SystemProperties.get("ro.product.model"));

        filterPref = findPreference("android_Version");
        filterPref.setSelectable(false);
        filterPref.setSummary(SystemProperties.get("ro.build.version.release"));

        filterPref = findPreference("android_Security");
        filterPref.setSelectable(false);
        filterPref.setSummary(SystemProperties.get("ro.build.version.security_patch"));

        filterPref = findPreference("kernel_version");
        filterPref.setSelectable(false);
        filterPref.setSummary(System.getProperty("os.version") + "\n\n" + Tools.readCommandOutput
                ("uname -a"));
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());;
        boolean isDeviceRooted = (sharedPref.getBoolean("isDeviceRooted", false));
        boolean isDeviceRootPresent = (sharedPref.getBoolean("isDeviceRootPresent", false));
        String magiskver = SystemProperties.get(MAGISK_VER);
        Boolean magisk = (magiskver != null && !magiskver.isEmpty());

        if (preference.getKey().equals("root_Version")) {
            if (!magisk) {
                if (isDeviceRootPresent) {
                    if (!isDeviceRooted) {
                        sharedPref.edit().putBoolean("isDeviceRooted", RootUtils.isRootPresent())
                                .apply();
                        Intent i = getContext().getPackageManager().getLaunchIntentForPackage
                                (getContext().getPackageName());
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(i);
                    }
                } else {
                    Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google" +
                            ".com/store/apps/details?id=eu.chainfire.supersu"));
                    startActivity(i);
                }
            } else {
                PackageManager manager = getContext().getPackageManager();
                Intent i = manager.getLaunchIntentForPackage("com.topjohnwu.magisk");
                if (i != null) {
                    i.addCategory(Intent.CATEGORY_LAUNCHER);
                    getContext().startActivity(i);
                }
            }
        }
        if (preference.getKey().equals("rom_busybox")) {
            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google" +
                    ".com/store/apps/details?id=stericson.busybox"));
            startActivity(i);
        }
        return true;
    }
}