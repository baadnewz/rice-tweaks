package com.ice.box.fragments;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;

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
public class Home extends PreferenceFragment implements Preference.OnPreferenceClickListener {
    StringBuilder licensetype = new StringBuilder();
    int counter;
    private int mThemeId = R.style.ThemeLight;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
        addPreferencesFromResource(R.xml.home_preference);

        //Listener for on shared preference changed (updated)
        PreferenceManager.getDefaultSharedPreferences(getContext()).registerOnSharedPreferenceChangeListener(
                new SharedPreferences.OnSharedPreferenceChangeListener() {
                    @Override
                    public void onSharedPreferenceChanged(
                            SharedPreferences sharedPreferences, String key) {
                        if (key == onlineStableVersionKey) {
                            Log.d(DEBUGTAG, "updated value for key: " + key);
                            setPreferencesValuesForRomVersion();
                        }
                        if (key == nightliesChangelogKey) {
                            Log.d(DEBUGTAG, "updated value for key: " + key);
                            setPreferencesValuesForChangelog();
                        }
                    }
                });

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

        //Log.d(DEBUGTAG, "IsNightly: " + isNightly + " Lastest Nightly: " + nightliesOnlineCurrentRevision);

        filterPref = findPreference("buy_premium");
        filterPref.setOnPreferenceClickListener(this);
        if (isFreeVersion) {
            licensetype.append(getResources().getString(R.string.free_license));
        } else {
            if (isInstalledPro || isPremium2) {
                licensetype.append(getResources().getString(R.string.donation2)).append(" ")
                        .append(getResources().getString(R.string.detected)).append("\n");
                counter++;
            }
            if (isInstalledDonation || isPremium5) {
                licensetype.append(getResources().getString(R.string.donation5)).append(" ")
                        .append(getResources().getString(R.string.detected)).append("\n");
                counter++;
            }
            if (isPremium10) {
                licensetype.append(getResources().getString(R.string.donation10)).append(" ")
                        .append(getResources().getString(R.string.detected)).append("\n");
                counter++;
            }
            if (isMonthly || isYearly) {
                licensetype.append(getResources().getString(R.string.subscription)).append(" ")
                        .append(getResources().getString(R.string.detected)).append("\n");
                counter++;
            }
            if (isLegacyLicense) {
                licensetype.append(getResources().getString(R.string.legacy_license)).append(" ")
                        .append(getResources().getString(R.string.detected)).append("\n");
                counter++;
            }
            if (counter > 1) {
                licensetype.append(getResources().getString(R.string
                        .thankyoumultiple)).append("\n");
            } else if (counter > 0) {
                licensetype.append(getResources().getString(R.string.thankyou));
            }
        }
        filterPref.setSummary(licensetype);

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
        final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
        ;
        String nightliesChangelog = (sharedPref.getString(nightliesChangelogKey, ""));
        boolean isNightly = sharedPref.getBoolean(isNightlyKey, false);
        boolean isNote8Port = sharedPref.getBoolean(Constants.isNote8PortKey, false);
        //Fragment fragment;


/*        if (preference.getKey().equals("buy_premium")) {
            Fragment someFragment = new SomeFragment();
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.replace(R.id., someFragment ); // give your fragment container id in first parameter
            transaction.addToBackStack(null);  // if written, this transaction will be added to backstack
            transaction.commit();
        }*/

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
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
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
        //Log.d(DEBUGTAG, "onlineROMis: " + onlineRomVersion);
        //Log.d(DEBUGTAG, "onlineNightlyis: " + nightliesOnlineCurrentRevision);

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
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
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
            Log.d(DEBUGTAG, "Failed to set changelog summary");
        }

    }

}