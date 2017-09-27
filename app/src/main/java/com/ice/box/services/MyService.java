package com.ice.box.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import com.ice.box.helpers.Constants;
import com.ice.box.helpers.SystemProperties;
import com.ice.box.helpers.TweaksHelper;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.URL;

import static com.ice.box.helpers.Constants.isNightlyKey;
import static com.ice.box.helpers.Constants.isNote8PortKey;
import static com.ice.box.helpers.Constants.localNightlyVersionKey;
import static com.ice.box.helpers.Constants.localStableVersionKey;
import static com.ice.box.helpers.Constants.onlineNightlyVersionKey;
import static com.ice.box.helpers.Constants.onlineStableVersionKey;
import static com.ice.box.helpers.Constants.onlineStableVersionTextKey;
import static com.ice.box.helpers.Constants.riceSvnLink;
import static com.ice.box.helpers.Constants.svnPassword;
import static com.ice.box.helpers.Constants.svnUsername;


/**
 * Created by Adrian on 03.05.2017.
 */


@SuppressWarnings("DefaultFileTemplate")
public class MyService extends IntentService {
    private TweaksHelper tweaksHelper;
    SharedPreferences sharedPref;

    public MyService() {
        super("MyService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // Release the wake lock provided by the WakefulBroadcastReceiver.
        WakefulBroadcastReceiver.completeWakefulIntent(intent);
        // Do only awesome stuff
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isNightly = sharedPref.getBoolean(isNightlyKey, false);
        boolean isICE = (sharedPref.getBoolean("isICE", false));
        tweaksHelper = new TweaksHelper(getApplicationContext());

        //Check if ROM is ICE
        //If ICE is true then run background service to fetch lastest online version
        if (isICE) {
            if (!isNightly) {
                //ROM is stable branch - run stable branch version fetch
                //Log.d(DEBUGTAG, "Service running not nightly");
                new romUpdate().execute();
            } else {
                //ROM is nightly branch - run stable branch nightly fetch
                //Log.d(DEBUGTAG, "Service running nightly");
                new nightlyUpdate().execute();
            }
        }
    }

    private class nightlyUpdate extends AsyncTask<String, String, String> {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        boolean isNote8Port = sharedPref.getBoolean(Constants.isNote8PortKey, false);
        String nightlyRevision;

        @Override
        protected String doInBackground(String... strings) {
            URL url;
            Authenticator.setDefault(new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(svnUsername, svnPassword.toCharArray());
                }
            });
            HttpURLConnection urlConnection = null;
            try {
                if (!isNote8Port) {
                    url = new URL(
                            riceSvnLink + "renovate-dream/trunk/META-INF/com/google/android/aroma/changelog.txt");
                } else {
                    url = new URL(
                            riceSvnLink + "renovate-great-port/trunk/META-INF/com/google/android/aroma/changelog.txt");
                }
                urlConnection = (HttpURLConnection) url
                        .openConnection();
                BufferedReader bufferedReader =
                        new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                nightlyRevision = bufferedReader.readLine();
                bufferedReader.close();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
            return nightlyRevision;
        }

        @Override
        protected void onPostExecute(String result) {
            int nightliesOnlineCurrentRevision = Integer.parseInt
                    (result.replaceAll("[^\\d.]", ""));
            sharedPref.edit().putInt(onlineNightlyVersionKey, nightliesOnlineCurrentRevision)
                    .apply();
            int nightliesOfflineCurrentRevision = sharedPref.getInt(localNightlyVersionKey, 1);
            boolean isICE = (sharedPref.getBoolean("isICE", false));
            if (isICE && nightliesOnlineCurrentRevision > nightliesOfflineCurrentRevision)
                tweaksHelper.createNightlyNotification();
        }
    }

    private class romUpdate extends AsyncTask<String, String, String> {
        boolean isNotePort = sharedPref.getBoolean(isNote8PortKey, false);
        String stringdevice = SystemProperties.get("ro.product.board");
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
                if (isNotePort == true) {
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
            return latestROMVersion;
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                sharedPref.edit().putString(onlineStableVersionTextKey, result).apply();
                sharedPref.edit().putInt(onlineStableVersionKey, Integer.parseInt(result
                        .replaceAll("[\\D]", ""))).apply();
            } catch (Exception e) {
                e.printStackTrace();
            }
            int localROMVersion = sharedPref.getInt(localStableVersionKey, 12);
            int onlineROMVersion = Integer.parseInt(result
                    .replaceAll("[\\D]", ""));
            if (localROMVersion < onlineROMVersion) {
                tweaksHelper.createRomNotification();
            }
        }
    }
}