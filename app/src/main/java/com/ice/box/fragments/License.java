package com.ice.box.fragments;

import android.accounts.AccountManager;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.common.AccountPicker;
import com.ice.box.MainActivity;
import com.ice.box.R;
import com.ice.box.helpers.TweaksHelper;
import com.ice.box.iab.MyBilling;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import static android.app.Activity.RESULT_OK;
import static com.ice.box.helpers.Constants.DEBUGTAG;
import static com.ice.box.helpers.Constants.googleAccountKey;
import static com.ice.box.helpers.Constants.isExceptionKey;
import static com.ice.box.helpers.Constants.isFreeVersionKey;
import static com.ice.box.helpers.Constants.isLegacyLicenseKey;
import static com.ice.box.helpers.Constants.riceManagementFolder;
import static com.ice.box.helpers.Constants.riceWebsiteLink;

public class License extends PreferenceFragment implements Preference.OnPreferenceClickListener {

    StringBuilder licenseType = new StringBuilder();
    int counter;
    private int mThemeId = R.style.ThemeLight;
    private MyBilling myBilling;
    private static final int REQUEST_CODE = 1;
    private static final int REQUEST_CODE_OFF_THE_BOOKS = 2;
    private SharedPreferences sharedPref;
    private TweaksHelper tweaksHelper;
    private String googleAccount;
    boolean isInstalledPro;
    boolean isInstalledDonation;
    boolean isMonthly;
    boolean isYearly;
    boolean isPremium2;
    boolean isPremium5;
    boolean isPremium10;
    boolean isFreeVersion;
    boolean isException;
    private int clickCounter;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());

        MainActivity mainActivity = (MainActivity) getActivity();
        tweaksHelper = new TweaksHelper(this.getContext());

        myBilling = new MyBilling(mainActivity);
        myBilling.onCreate();

        isInstalledPro = (sharedPref.getBoolean("isInstalledPro", false));
        isInstalledDonation = (sharedPref.getBoolean("isInstalledDonation", false));
        isMonthly = (sharedPref.getBoolean("isMonthly", false));
        isYearly = (sharedPref.getBoolean("isYearly", false));
        isPremium2 = sharedPref.getBoolean("isPremium2", false);
        isPremium5 = (sharedPref.getBoolean("isPremium5", false));
        isPremium10 = (sharedPref.getBoolean("isPremium10", false));
        isFreeVersion = (sharedPref.getBoolean(isFreeVersionKey, false));
        isException = sharedPref.getBoolean(isExceptionKey, false);
        boolean isLegacyLicense = sharedPref.getBoolean(isLegacyLicenseKey, false);
        googleAccount = sharedPref.getString(googleAccountKey, null);
        Preference checkPref;
        addPreferencesFromResource(R.xml.license_preference);

        mThemeId = sharedPref.getInt("THEMEID", mThemeId);
        Preference filterPref;

        filterPref = findPreference("license_status");
        // filterPref.setSelectable(false);
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
                licenseType.append("\n").append(getResources().getString(R.string
                        .thankyoumultiple));

            } else if (counter > 0) {
                licenseType.append("\n").append(getResources().getString(R.string.thankyou));
            }
        }
        filterPref.setSummary(licenseType);
        if (!isException) {
            filterPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    clickCounter++;
                    switch (clickCounter) {
                        case 1:
                            break;
                        case 2:
                            break;
                        case 3:
                            tweaksHelper.MakeToastShort("Touch me three more times...");
                            break;
                        case 4:
                            tweaksHelper.MakeToastShort("Touch me two more times...");
                            break;
                        case 5:
                            tweaksHelper.MakeToastShort("Touch me one more time...");
                            break;
                        case 6:
                            if (googleAccount == null) {
                                tweaksHelper.MakeToastShort("Enough touching, select your damn account and enjoy the damn freebie");
                                try {
                                    Intent intent = AccountPicker.newChooseAccountIntent(null, null,
                                            new String[]{GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE}, false, null, null, null, null);
                                    startActivityForResult(intent, REQUEST_CODE_OFF_THE_BOOKS);
                                } catch (ActivityNotFoundException e) {
                                    // Should handle pre API 8 Exception. Since App's min SDK is 24 we shouldn't bother with this
                                }
                            } else {
                                tweaksHelper.MakeToastShort("checking if you should have a freebie");
                                new getLegacyLicense().execute(googleAccount, "1");
                            }
                    }
                    return true;
                }
            });
        } else {
            filterPref.setSelectable(false);
        }

        filterPref = findPreference("icebox.monthly");
        filterPref.setOnPreferenceClickListener(this);
        checkPref = findPreference("icebox.monthly");
        if (isMonthly) {
            checkPref.setSelectable(false);
            filterPref.setSummary(getResources().getString(R.string.item_already_purchased));
            filterPref.setEnabled(false);
        }

        filterPref = findPreference("icebox.donation2");
        filterPref.setOnPreferenceClickListener(this);
        checkPref = findPreference("icebox.donation2");
        if (isPremium2) {
            checkPref.setSelectable(false);
            filterPref.setSummary(getResources().getString(R.string.item_already_purchased));
            filterPref.setEnabled(false);
        }

        filterPref = findPreference("icebox.donation5");
        filterPref.setOnPreferenceClickListener(this);
        checkPref = findPreference("icebox.donation5");
        if (isPremium5) {
            checkPref.setSelectable(false);
            filterPref.setSummary(getResources().getString(R.string.item_already_purchased));
            filterPref.setEnabled(false);
        }

        filterPref = findPreference("icebox.donation10");
        filterPref.setOnPreferenceClickListener(this);
        checkPref = findPreference("icebox.donation10");
        if (isPremium10) {
            checkPref.setSelectable(false);
            filterPref.setSummary(getResources().getString(R.string.item_already_purchased));
            filterPref.setEnabled(false);
        }

        filterPref = findPreference("icebox.oldice");
        filterPref.setOnPreferenceClickListener(this);
        if (!TweaksHelper.isEmptyString(googleAccount)) {
            //filterPref.setEnabled(false);
            filterPref.setSummary(getResources().getString(R.string.icebox_oldice_summary_binded) + " "
                    + googleAccount + "\n\n" + getResources().getString(R.string.icebox_oldice_summary_binded2));
        }

        //If we have any InAppBilling License Purchased there is not need for legacy licensing => we remove the preference
        if (isMonthly || isYearly || isPremium2 || isPremium5 || isPremium10) {
            getPreferenceScreen().removePreference(filterPref);
            getPreferenceScreen().removePreference(findPreference("icebox.oldice.category"));
        }


    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        String googleAccount = sharedPref.getString(googleAccountKey, null);

        switch (preference.getKey()) {
            case "icebox.monthly":
                try {
                    myBilling.purchaseSubscriptionMonthly();
                } catch (Exception ignored) {
                    AlertDialogIabNotSupported();
                }
                break;
            case "icebox.donation2":
                try {
                    myBilling.Premium2();
                } catch (Exception ignored) {
                    AlertDialogIabNotSupported();
                }
                break;
            case "icebox.donation5":
                try {
                    myBilling.Premium5();
                } catch (Exception ignored) {
                    AlertDialogIabNotSupported();
                }
                break;
            case "icebox.donation10":
                try {
                    myBilling.Premium10();
                } catch (Exception ignored) {
                    AlertDialogIabNotSupported();
                }
                break;
            case "icebox.oldice":
                if (TweaksHelper.isEmptyString(googleAccount)) {
                    try {
                        Intent intent = AccountPicker.newChooseAccountIntent(null, null,
                                new String[]{GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE}, false, null, null, null, null);
                        startActivityForResult(intent, REQUEST_CODE);
                    } catch (ActivityNotFoundException e) {
                        // Should handle pre API 8 Exception. Since App's min SDK is 24 we shouldn't bother with this
                    }
                } else {
                    tweaksHelper.MakeToast(getResources().getString(R.string.icebox_oldice_toast));
                }
                break;
        }
        return true;
    }

    private void AlertDialogIabNotSupported() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext());
        alertDialog.setTitle(getResources().getString(R.string.app_name));
        alertDialog.setIcon(R.mipmap.ic_key);
        alertDialog.setMessage(getResources().getString(R.string.iab_error));
        alertDialog.setNeutralButton(getResources().getString(R.string.ok),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode,
                                 final Intent data) {
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
            new getLegacyLicense().execute(accountName, "0");
        }
        if (requestCode == REQUEST_CODE_OFF_THE_BOOKS && resultCode == RESULT_OK) {
            String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
            new getLegacyLicense().execute(accountName, "1");
        }
    }

    private class getLegacyLicense extends AsyncTask<String, String, String> {
        String result = "";
        boolean covertOps = false;

        @Override
        protected String doInBackground(String... strings) {
            URL url;
            HttpURLConnection urlConnection = null;
            try {
                url = new URL(
                        riceWebsiteLink + riceManagementFolder + "/search.php?mail=" + strings[0] + "&exception=" + strings[1]);
                urlConnection = (HttpURLConnection) url
                        .openConnection();
                urlConnection.setConnectTimeout(5000);
                BufferedReader bufferedReader = new BufferedReader(
                        new InputStreamReader(urlConnection.getInputStream()));
                String line;

                while ((line = bufferedReader.readLine()) != null) {
                    result += line + "\n";
                }
                bufferedReader.close();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
            if (strings[1].equalsIgnoreCase("1")) {
                covertOps = true;
            }
            sharedPref.edit().putString(googleAccountKey, strings[0]).apply();
            return result.toString();
        }

        @Override
        protected void onPostExecute(String result) {
            if (!covertOps) {
                if (result.contains("true")) {
                    //License was true so lets save the value for pro
                    sharedPref.edit().putBoolean(isLegacyLicenseKey, true)
                            .apply();
                    //Notify the user that license is activated and he/she needs to restart app
                    tweaksHelper.restartSelfOnLicenseOK();
                } else {
                    //Activation failed for this google account
                    tweaksHelper.MakeToast(getResources().getString(R.string.icebox_oldice_toast_nolicense));
                    sharedPref.edit().putBoolean(isLegacyLicenseKey, false)
                            .apply();
                }
            } else {
                if (result.contains("true")) {
                    sharedPref.edit().putBoolean(isLegacyLicenseKey, true)
                            .apply();
                    sharedPref.edit().putBoolean(isExceptionKey,true).apply();
                    tweaksHelper.restartSelfOnLicenseOK();
                } else {
                    sharedPref.edit().putBoolean(isExceptionKey,false).apply();
                    tweaksHelper.MakeToast("Sorry mate, no cheating");
                }
            }
        }
    }
}