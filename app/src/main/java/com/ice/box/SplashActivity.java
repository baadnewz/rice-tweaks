package com.ice.box;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.ice.box.helpers.RootUtils;
import com.ice.box.helpers.SystemProperties;
import com.ice.box.helpers.Tools;
import com.ice.box.helpers.TweaksHelper;
import com.ice.box.iab.MyBilling;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import static com.ice.box.helpers.Constants.*;

public class SplashActivity extends AppCompatActivity {

    public static SplashActivity splashActivity;
    private MyBilling myBilling;
    private int mThemeId = R.style.ThemeLight;
    SharedPreferences sharedPref;

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        mThemeId = sharedPref.getInt("THEMEID", mThemeId);
        setTheme(mThemeId);
        setContentView(R.layout.content_splash);
        splashActivity = this;

        //billing
        myBilling = new MyBilling(this);
        try {
            myBilling.onCreate();
        } catch (Exception ignored) {

        }

        final boolean isICE = sharedPref.getBoolean("isICE", false);
        final int isAppUpdate = sharedPref.getInt("isAppUpdate", 1);
        isFreeVersion();
        isForceEnglish();
        isAppUpdate();
        getLocalRenovateVersion();
        new Thread() {
            public void run() {
                gotRoot();
                isSystemApp();
                isICE();
                resetDaily();
                androidVersion();
                isNotePort();
                whatGalaxy();
                //RUN the fetchers only if ROM is ICE
                if (isICE) {
                    new getStableOnlineVersion().execute();
                    if (SystemProperties.getBoolean(isNightlyProp, false)) {
                        new getNightlyOnlineVersionAndChangelog().execute();
                    }
                }
                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish();
            }
        }.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            myBilling.onDestroy();
        } catch (Exception e) {
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            super.onActivityResult(requestCode, resultCode, data);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        try {
            myBilling.onActivityResult(requestCode, resultCode, data);
        } catch (Exception e) {
        }
    }

    private void copyScriptFiles() {
        File backup_colors = new File(getFilesDir().getPath() + "/backup_colors.sh");
        File restore_colors = new File(getFilesDir().getPath() + "/restore_colors.sh");
        File set_build_prop = new File(getFilesDir().getPath() + "/set_build_prop.sh");
        File set_others = new File(getFilesDir().getPath() + "/set_others.sh");
        Tools.copyAssetsFile("backup_colors.sh");
        Tools.copyAssetsFile("restore_colors.sh");
        Tools.copyAssetsFile("set_build_prop.sh");
        Tools.copyAssetsFile("set_others.sh");
        try {
            Runtime.getRuntime().exec("chmod 755 " + set_build_prop);
            Runtime.getRuntime().exec("chmod 755 " + backup_colors);
            Runtime.getRuntime().exec("chmod 755 " + restore_colors);
            Runtime.getRuntime().exec("chmod 755 " + set_others);
        } catch (Exception e) {
            Log.d(DEBUGTAG, "Failed to set scripts permissions");
        }

    }


    private boolean isPackageInstalledAndEnabled(String packagename) {
        ApplicationInfo ai;
        try {
            ai = getApplicationContext().getPackageManager().getApplicationInfo(packagename, 0);
            return ai.enabled;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    private void gotRoot() {

        sharedPref.edit().putBoolean("isDeviceRootPresent", RootUtils.isRootPresent()).apply();
        sharedPref.edit().putBoolean("isDeviceRooted", RootUtils.isRootGranted()).apply();
    }

    private void isFreeVersion() {
        //Reading and writing old app keys values
        //boolean isInstalledPro = isPackageInstalledAndEnabled("com.renovate.premium") || isPackageInstalledAndEnabled("com.ice.premium");
        //boolean isInstalledDonation = isPackageInstalledAndEnabled("com.renovate.premium2") || isPackageInstalledAndEnabled("com.ice.premium2");
        //sharedPref.edit().putBoolean("isInstalledPro", isInstalledPro).apply();
        //sharedPref.edit().putBoolean("isInstalledDonation", isInstalledDonation).apply();
        //Reading In app billing values
        boolean isMonthly = (sharedPref.getBoolean("isMonthly", false));
        boolean isYearly = (sharedPref.getBoolean("isYearly", false));
        boolean isPremium2 = (sharedPref.getBoolean("isPremium2", false));
        boolean isPremium5 = (sharedPref.getBoolean("isPremium5", false));
        boolean isPremium10 = (sharedPref.getBoolean("isPremium10", false));
        //Reading and writing legacy license values
        boolean isLegacyLicense = sharedPref.getBoolean(isLegacyLicenseKey, false);

        //Setting global boolean for premium
        if (!isMonthly && !isYearly && !isPremium2 && !isPremium5 && !isPremium10 && !isLegacyLicense) {
            sharedPref.edit().putBoolean(isFreeVersionKey, true).apply();
            //RESTING PROPS ONLY FOR PREMIUM
            sharedPref.edit().putInt("THEMEID", R.style.ThemeLight).apply();
            sharedPref.edit().putBoolean("romupdate", false).apply();
            sharedPref.edit().putBoolean("nightlyupdate", false).apply();
        } else {
            sharedPref.edit().putBoolean(isFreeVersionKey, false).apply();
        }
    }

    private void isForceEnglish() {
        boolean forceEnglish = (sharedPref.getBoolean("forceEnglish", false));
        if (forceEnglish) {
            sharedPref.edit().putBoolean("forceEnglish", true).apply();
            String languageToLoad = "en"; // your language
            Locale locale = new Locale(languageToLoad);
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.locale = locale;
            getBaseContext().getResources().updateConfiguration(config,
                    getBaseContext().getResources().getDisplayMetrics());
        } else {
            sharedPref.edit().putBoolean("forceEnglish", false).apply();
            Locale locale = Resources.getSystem().getConfiguration().locale;
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.locale = locale;
            getBaseContext().getResources().updateConfiguration(config,
                    getBaseContext().getResources().getDisplayMetrics());
        }

    }

    private void isSystemApp() {
        boolean isSystemApp = (getApplicationInfo().flags & (ApplicationInfo.FLAG_SYSTEM |
                ApplicationInfo.FLAG_UPDATED_SYSTEM_APP)) != 0;
        if (isSystemApp) {
            sharedPref.edit().putBoolean("isSystemApp", true).apply();
        } else {
            sharedPref.edit().putBoolean("isSystemApp", false).apply();
        }
    }

    private void isICE() {
        String isICE = SystemProperties.get("ro.build.display.id");
        if (!TweaksHelper.isEmptyString(isICE)) {
            if (isICE.contains("RENOVATE")) {
                sharedPref.edit().putBoolean("isICE", true).apply();
            } else {
                sharedPref.edit().putBoolean("isICE", false).apply();
            }
        } else {
            sharedPref.edit().putBoolean("isICE", false).apply();
        }
    }

    private void whatGalaxy() {
        try {
            if (SystemProperties.get("ro.chipname").equals("exynos8895")) {
                sharedPref.edit().putBoolean("isGalaxyS8", true).apply();
                sharedPref.edit().putBoolean("isGalaxyS7", false).apply();
            } else if (SystemProperties.get("ro.chipname").equals("exynos8890")) {
                sharedPref.edit().putBoolean("isGalaxyS8", false).apply();
                sharedPref.edit().putBoolean("isGalaxyS7", true).apply();
            } else {
                sharedPref.edit().putBoolean("isGalaxyS8", false).apply();
                sharedPref.edit().putBoolean("isGalaxyS7", false).apply();
            }
        } catch (NullPointerException e) {
            Log.i(this.getClass().getName() + DEBUGTAG, "Error reading what glaxy");

        }

    }

    private void getBoard() {
        int isAppUpdate = sharedPref.getInt("isAppUpdate", 0);
        String getBoard = sharedPref.getString("getBoard", "");
        if (getBoard.equals("") || isAppUpdate == 1) {
            try {
                sharedPref.edit().putString("getBoard", SystemProperties.get("ro.product.board"))
                        .apply();
            } catch (Exception ignored) {
            }
        }
    }

    private void getLocalRenovateVersion() {
        //LOCAL STABLE
        String localStableVersion = SystemProperties.get(stableVersionProp);
        assert localStableVersion != null;
        if (localStableVersion.isEmpty()) {
            localStableVersion = "1.0";
        }
        sharedPref.edit().putString(localStableVersionTextKey, localStableVersion).apply();
        sharedPref.edit().putInt(localStableVersionKey, Integer.parseInt(localStableVersion.replaceAll
                ("[\\D]", ""))).apply();

        //LOCAL NIGHTLY
        sharedPref.edit().putBoolean(isNightlyKey, SystemProperties.getBoolean(isNightlyProp, false)).apply();
        sharedPref.edit().putInt(localNightlyVersionKey, SystemProperties.getInt("renovate.nightly.version", 1)).apply();


    }

    private void androidVersion() {
        try {
            sharedPref.edit().putInt("androidVersion", Build.VERSION.SDK_INT).apply();
        } catch (Exception e) {
            sharedPref.edit().putInt("androidVersion", 0).apply();
        }
    }

    private void isAppUpdate() {
        int initialVersionCode = sharedPref.getInt("VERSION_CODE", 0);
        int currentVersionCode = sharedPref.getInt("VERSION_CODE", BuildConfig.VERSION_CODE);
        if (initialVersionCode == 0) {
            sharedPref.edit().putInt("isAppUpdate", 1).apply();
            //initial installation so lets copy scripts
            copyScriptFiles();
        } else if (currentVersionCode != BuildConfig.VERSION_CODE) {
            sharedPref.edit().putInt("isAppUpdate", 2).apply();
            //app is updated so lets copy scripts in case newer versions are packed with the new app
            copyScriptFiles();
        } else {
            sharedPref.edit().putInt("isAppUpdate", 3).apply();
            //no 1st install, no updated
        }
        sharedPref.edit().putInt("VERSION_CODE", BuildConfig.VERSION_CODE).apply();
    }

    private void resetDaily() {
        @SuppressLint("SimpleDateFormat") DateFormat df = new SimpleDateFormat("yyyy.MM.dd");
        int todayDate = Integer.parseInt(df.format(Calendar.getInstance().getTime()).replaceAll
                ("[\\D]", ""));
        int initialDate = sharedPref.getInt("lastRunDate", 0);
        if (todayDate > initialDate) {
            sharedPref.edit().putBoolean("resetDaily", true).apply();
        } else {
            sharedPref.edit().putBoolean("resetDaily", false).apply();
        }
        sharedPref.edit().putInt("lastRunDate", todayDate).apply();
    }

    private void isNotePort() {
        if (SystemProperties.getBoolean("n950port", false)) {
            sharedPref.edit().putBoolean(isNote8PortKey, true).apply();
        } else {
            sharedPref.edit().putBoolean(isNote8PortKey, false).apply();
        }
    }

    //ASYNC TASKS
    public class getStableOnlineVersion extends AsyncTask<String, String, String> {
        final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        final boolean isNotePort = sharedPref.getBoolean(isNote8PortKey, false);
        final String stringdevice = SystemProperties.get("ro.product.board");
        String latestROMVersion;

        @Override
        protected String doInBackground(String... params) {
            URL url;
            Authenticator.setDefault(new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(svnUsername, svnPassword.toCharArray());
                }
            });
            HttpURLConnection urlConnection = null;
            try {
                if (isNotePort) {
                    url = new URL(
                            riceSvnLink + "renovate-great-port/trunk/versions.txt");
                } else {
                    url = new URL(
                            riceSvnLink + "renovate-dream/trunk/versions.txt");
                }
                urlConnection = (HttpURLConnection) url
                        .openConnection();
                urlConnection.setConnectTimeout(5000);
                BufferedReader bufferedReader = new BufferedReader(
                        new InputStreamReader(urlConnection.getInputStream()));
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    if (stringdevice != null && line.contains(stringdevice)) {
                        latestROMVersion = line;
                    }
                }
                bufferedReader.close();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
            if (!TweaksHelper.isEmptyString(latestROMVersion)) {
                latestROMVersion = latestROMVersion.replace(stringdevice, "").trim();
            }
            //Log.d(DEBUGTAG, "getStableOnlineVersion: " + latestROMVersion);
            return latestROMVersion;

        }

        @Override
        protected void onPostExecute(String result) {
            try {
                sharedPref.edit().putString(onlineStableVersionTextKey, result).apply();
                sharedPref.edit().putInt(onlineStableVersionKey, Integer.parseInt(result
                        .replaceAll("[\\D]", ""))).apply();
                //Log.d(DEBUGTAG, "getStableOnlineVersion STORED");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private class getNightlyOnlineVersionAndChangelog extends AsyncTask<String, String, String> {
        final boolean isNotePort = sharedPref.getBoolean(isNote8PortKey, false);
        final StringBuilder content = new StringBuilder();

        @Override
        protected String doInBackground(String... params) {
            URL url;
            Authenticator.setDefault(new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(svnUsername, svnPassword.toCharArray());
                }
            });
            HttpURLConnection urlConnection = null;
            try {
                if (isNotePort) {
                    url = new URL(
                            riceSvnLink + "renovate-great-port/trunk/META-INF/" +
                                    "com/google/android/aroma/changelog.txt");
                } else {
                    url = new URL(
                            riceSvnLink + "renovate-dream/trunk/META-INF/" +
                                    "com/google/android/aroma/changelog.txt");
                }
                urlConnection = (HttpURLConnection) url
                        .openConnection();
                urlConnection.setConnectTimeout(5000);
                BufferedReader bufferedReader =
                        new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    content.append(line).append("\n");
                }
                bufferedReader.close();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
            return content.toString();
        }

        @Override
        protected void onPostExecute(String result) {
            if (!TweaksHelper.isEmptyString(result)) {
                sharedPref.edit().putString(nightliesChangelogKey, result)
                        .apply();
                InputStream is = new ByteArrayInputStream(result.getBytes());
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                try {
                    String nightlyRevisionString = br.readLine();
                    int nightlyRevisionInt = Integer.parseInt
                            (nightlyRevisionString.replaceAll("[^\\d.]", ""));
                    sharedPref.edit().putInt(onlineNightlyVersionKey, nightlyRevisionInt)
                            .apply();
                    br.close();
                } catch (IOException | NumberFormatException e) {
                    sharedPref.edit().putString(nightliesChangelogKey,
                            getResources().getString(R.string.nightlies_error)).apply();
                }
            } else {
                sharedPref.edit().putString(nightliesChangelogKey,
                        getResources().getString(R.string.nightlies_no_connection)).apply();
            }
            Log.d(DEBUGTAG, "getNightlyOnlineVersionAndChangelog STORED");
        }
    }
}