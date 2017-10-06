package com.ice.box.fragments;


import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;

import com.ice.box.MainActivity;
import com.ice.box.R;
import com.ice.box.helpers.Constants;
import com.ice.box.helpers.SystemProperties;

import static com.ice.box.helpers.Constants.DEBUGTAG;
import static com.ice.box.helpers.Constants.isFreeVersionKey;
import static com.ice.box.helpers.Constants.isLegacyLicenseKey;
import static com.ice.box.helpers.Constants.isNightlyKey;
import static com.ice.box.helpers.Constants.localNightlyVersionKey;
import static com.ice.box.helpers.Constants.localStableVersionKey;
import static com.ice.box.helpers.Constants.localStableVersionTextKey;
import static com.ice.box.helpers.Constants.nightliesChangelogKey;
import static com.ice.box.helpers.Constants.onlineNightlyVersionKey;
import static com.ice.box.helpers.Constants.onlineStableVersionKey;
import static com.ice.box.helpers.Constants.onlineStableVersionTextKey;
import static com.ice.box.helpers.Constants.riceWebsiteLink;
import static com.ice.box.helpers.Constants.xdaNote8Port;
import static com.ice.box.helpers.Constants.xdaS8;

@SuppressWarnings("unused")
public class Home extends PreferenceFragment implements Preference.OnPreferenceClickListener, SharedPreferences.OnSharedPreferenceChangeListener {
    StringBuilder licenseType = new StringBuilder();
    SharedPreferences sharedPref;
    int counter;
    private int mThemeId = R.style.ThemeLight;


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
        addPreferencesFromResource(R.xml.home_preference);


        String romstatus;
        String currentRomVersionText = sharedPref.getString(localStableVersionTextKey, null);
        String onlineRomVersionText = sharedPref.getString(onlineStableVersionTextKey, null);
        String nightliesChangelog = (sharedPref.getString(nightliesChangelogKey, ""));
        boolean isICE = (sharedPref.getBoolean("isICE", false));
        boolean isInstalledPro = (sharedPref.getBoolean("isInstalledPro", false));
        boolean isInstalledDonation = (sharedPref.getBoolean("isInstalledDonation", false));
        boolean isMonthly = (sharedPref.getBoolean("isMonthly", false));
        boolean isYearly = (sharedPref.getBoolean("isYearly", false));
        boolean isPremium2 = (sharedPref.getBoolean("isPremium2", false));
        boolean isPremium5 = (sharedPref.getBoolean("isPremium5", false));
        boolean isPremium10 = (sharedPref.getBoolean("isPremium10", false));
        boolean isFreeVersion = (sharedPref.getBoolean(isFreeVersionKey, false));
        boolean isLegacyLicense = sharedPref.getBoolean(isLegacyLicenseKey, false);
        boolean isNightly = sharedPref.getBoolean(isNightlyKey, false);
        boolean isNote8Port = sharedPref.getBoolean(Constants.isNote8PortKey, false);
        int currentRomVersion = sharedPref.getInt(localStableVersionKey, 1);
        int nightliesOnlineCurrentRevision = sharedPref.getInt(onlineNightlyVersionKey, 1);
        int nightliesOfflineCurrentRevision = sharedPref.getInt(localNightlyVersionKey, 1);
        int onlineRomVersion = sharedPref.getInt(onlineStableVersionKey, 1);
        Preference filterPref;

        mThemeId = sharedPref.getInt("THEMEID", mThemeId);
        if (mThemeId == R.style.ThemeDark) {
            filterPref = findPreference("header_image");
            filterPref.setLayoutResource(R.layout.header_image_dark);
        }

        filterPref = findPreference("buy_premium");
        filterPref.setOnPreferenceClickListener(this);
        if (isFreeVersion) {
            licenseType.append(getResources().getString(R.string.free_license));
        } else {
            if (isPremium2) {
                licenseType.append(getResources().getString(R.string.donation2)).append(" ")
                        .append(getResources().getString(R.string.detected)).append("\n");
                counter++;
            }
            if (isPremium5) {
                licenseType.append(getResources().getString(R.string.donation5)).append(" ")
                        .append(getResources().getString(R.string.detected)).append("\n");
                counter++;
            }
            if (isPremium10) {
                licenseType.append(getResources().getString(R.string.donation10)).append(" ")
                        .append(getResources().getString(R.string.detected)).append("\n");
                counter++;
            }
            if (isMonthly || isYearly) {
                licenseType.append(getResources().getString(R.string.subscription)).append(" ")
                        .append(getResources().getString(R.string.detected)).append("\n");
                counter++;
            }
            if (isLegacyLicense) {
                licenseType.append(getResources().getString(R.string.legacy_license)).append(" ")
                        .append(getResources().getString(R.string.detected)).append("\n");
                counter++;
            }
            if (counter > 1) {
                licenseType.append(getResources().getString(R.string
                        .thankyoumultiple)).append("\n");
            } else if (counter > 0) {
                licenseType.append(getResources().getString(R.string.thankyou));
            }
        }
        filterPref.setSummary(licenseType);

        setPreferencesValuesForRomVersion();
        setPreferencesValuesForChangelog();

        filterPref = findPreference("app_Version");
        String versionName;
        try {
            versionName = getContext().getPackageManager()
                    .getPackageInfo(getContext().getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            versionName = getResources().getString(R.string.fail_read_app_version);
        }
        filterPref.setSummary(versionName);

        filterPref = findPreference("xda_thread");
        filterPref.setOnPreferenceClickListener(this);

        filterPref = findPreference("telegram_chat");
        filterPref.setOnPreferenceClickListener(this);
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        String nightliesChangelog = (sharedPref.getString(nightliesChangelogKey, ""));
        boolean isNightly = sharedPref.getBoolean(isNightlyKey, false);
        boolean isNote8Port = sharedPref.getBoolean(Constants.isNote8PortKey, false);
        //Fragment fragment;


        if (preference.getKey().equals("buy_premium")) {
            MainActivity.isFragmentOpen = true;
            switchFragment("License", getString(R.string.settings_license));

        }

        if (preference.getKey().equals("rom_Version")) {

            if (!isNote8Port) {
                if (!isNightly) {
                    Intent i = new Intent(Intent.ACTION_VIEW,
                            Uri.parse(xdaS8));
                    startActivity(i);
                } else {
                    Intent i = new Intent(Intent.ACTION_VIEW,
                            Uri.parse(riceWebsiteLink + "nightlies_dream.html"));
                    startActivity(i);
                }
            } else {
                if (!isNightly) {
                    Intent i = new Intent(Intent.ACTION_VIEW,
                            Uri.parse(xdaNote8Port));
                    startActivity(i);
                } else {
                    Intent i = new Intent(Intent.ACTION_VIEW,
                            Uri.parse(riceWebsiteLink + "nightlies_great-port.html"));
                    startActivity(i);
                }
            }


        }
        if (preference.getKey().equals("app_Version")) {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=com.ice.box")));
        }
        if (preference.getKey().equals("rom_changelog")) {
            AlertDialog.Builder alertDialog2 = new AlertDialog.Builder(getContext());
            alertDialog2.setTitle(getString(R.string.rom_changelog));
            alertDialog2.setMessage(nightliesChangelog);
            alertDialog2.setNeutralButton(getResources().getString(R.string.ok),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            dialog.cancel();
                        }
                    });
            alertDialog2.show();
        }
        if (preference.getKey().equals("xda_thread")) {
            if (!isNote8Port) {
                Intent i = new Intent(Intent.ACTION_VIEW,
                        Uri.parse(xdaS8));
                startActivity(i);
            } else {
                Intent i = new Intent(Intent.ACTION_VIEW,
                        Uri.parse(xdaNote8Port));
                startActivity(i);
            }
        }
        if (preference.getKey().equals("telegram_chat")) {
            Intent i = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://t.me/joinchat/AAAAAEGXRtcs018AhsbrUw"));
            startActivity(i);
        }
        return true;
    }

    private void setPreferencesValuesForRomVersion() {
        String romstatus;
        String currentRomVersionText = sharedPref.getString(localStableVersionTextKey, null);
        String onlineRomVersionText = sharedPref.getString(onlineStableVersionTextKey, null);
        String nightliesChangelog = (sharedPref.getString(nightliesChangelogKey, ""));
        boolean isICE = (sharedPref.getBoolean("isICE", false));
        boolean isNightly = sharedPref.getBoolean(isNightlyKey, false);
        int currentRomVersion = sharedPref.getInt(localStableVersionKey, 1);
        int nightliesOnlineCurrentRevision = sharedPref.getInt(onlineNightlyVersionKey, 1);
        int nightliesOfflineCurrentRevision = sharedPref.getInt(localNightlyVersionKey, 1);
        int onlineRomVersion = sharedPref.getInt(onlineStableVersionKey, 1);
        Preference filterPref;

        filterPref = findPreference("rom_Version");
        filterPref.setOnPreferenceClickListener(this);
        //filterPref.setSummary(String.valueOf(nightliesOnlineCurrentRevision));

        if (!isICE) {
            filterPref.setSelectable(true);
            romstatus = (SystemProperties.get("ro.build.display.id") +
                    "\n\n" + getResources().getString(R.string.not_ice2) + " " +
                    getResources().getString(R.string.not_ice3));
            Spannable romstatusc = new SpannableString(romstatus);
            romstatusc.setSpan(new ForegroundColorSpan(Color.RED), 0, romstatusc.length(), 0);
            filterPref.setSummary(romstatusc);
        } else {
            if (!isNightly) {
                if (onlineRomVersion > currentRomVersion) {
                    romstatus = getResources().getString(R.string.installed) + ": " +
                            currentRomVersionText + "\n"
                            + getResources().getString(R.string.lastest) + ": " +
                            onlineRomVersionText + "\n"
                            + getResources().getString(R.string.tap_to_download);
                    filterPref.setSelectable(true);

                } else {
                    romstatus = getResources().getString(R.string.installed) + ": " +
                            currentRomVersionText + "\n"
                            + getResources().getString(R.string.lastest) + ": " +
                            onlineRomVersionText;

                }
            } else {
                if (nightliesOnlineCurrentRevision > nightliesOfflineCurrentRevision) {
                    romstatus = getResources().getString(R.string.installed) + ": " +
                            "R" + nightliesOfflineCurrentRevision + "\n"
                            + getResources().getString(R.string.lastest) + ": " +
                            "R" + nightliesOnlineCurrentRevision + "\n"
                            + getResources().getString(R.string.tap_to_download);
                    filterPref.setSelectable(true);

                } else {
                    romstatus = getResources().getString(R.string.installed) + ": " +
                            "R" + nightliesOfflineCurrentRevision + "\n"
                            + getResources().getString(R.string.lastest) + ": " +
                            "R" + nightliesOnlineCurrentRevision;

                }
            }
            Spannable romStatus = new SpannableString(romstatus);
            if (!isNightly) {
                if (onlineRomVersion > currentRomVersion) {
                    romStatus.setSpan(new ForegroundColorSpan(Color.RED), 0, romStatus.length(), 0);
                } else {
                    romStatus.setSpan(new ForegroundColorSpan(Color.GREEN), 0, romStatus.length(), 0);
                }
            } else {
                if (nightliesOnlineCurrentRevision > nightliesOfflineCurrentRevision) {
                    romStatus.setSpan(new ForegroundColorSpan(Color.RED), 0, romStatus.length(), 0);
                } else {
                    romStatus.setSpan(new ForegroundColorSpan(Color.GREEN), 0, romStatus.length(), 0);
                }
            }
            filterPref.setSummary(romStatus);
        }

    }

    private void setPreferencesValuesForChangelog() {
        String nightliesChangelog = (sharedPref.getString(nightliesChangelogKey, ""));
        Preference filterPref;

        filterPref = findPreference("rom_changelog");
        filterPref.setOnPreferenceClickListener(this);
        try {
            if (!nightliesChangelog.isEmpty()) {
                filterPref.setSummary(getResources().getString(R.string.nightlies_summary));
            } else {
                filterPref.setSummary(getResources().getString(R.string.nightlies_no_connection));
            }
        } catch (Exception e) {
            Log.e(this.getClass().getName(), "Failed to set changelog summary");
        }

    }

    private void switchFragment(final String fragmentClass, final String fragmentTitle) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
                toolbar.setTitle(fragmentTitle);
                FragmentTransaction fm = getFragmentManager().beginTransaction();
                fm.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out);
                fm.replace(R.id.container,
                        Fragment.instantiate(
                                getContext(),
                                "com.ice.box.fragments." + fragmentClass))
                        .commit();
            }
        }, 0);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                          String key) {
        if (key == onlineStableVersionKey) {
            setPreferencesValuesForRomVersion();
        }
        if (key == nightliesChangelogKey) {
            setPreferencesValuesForChangelog();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }
}