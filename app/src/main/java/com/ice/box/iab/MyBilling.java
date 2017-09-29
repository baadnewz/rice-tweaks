package com.ice.box.iab;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.ice.box.R;
import com.ice.box.SplashActivity;

import java.util.ArrayList;
import java.util.List;

import static com.ice.box.helpers.Constants.base64EncodedPublicKey;
import static com.ice.box.helpers.Constants.payload;


public class MyBilling {
    private static final String SKU_ICE_PREMIUM_MONTHLY = "icebox.monthly";
    private static final String SKU_ICE_PREMIUM_YEARLY = "com.ice.tweaks.yearly";
    private static final String SKU_ICE_PREMIUM_2 = "icebox.donation2";
    private static final String SKU_ICE_PREMIUM_5 = "icebox.donation5";
    private static final String SKU_ICE_PREMIUM_10 = "icebox.donation10";

    // (arbitrary) request code for the purchase flow
    private static final int RC_REQUEST = 10111;
    private Activity activity;
    // The helper object
    private IabHelper mHelper;
    private String subscriptionType;
    // Listener that's called when we finish querying the items and
    // subscriptions we own
    private IabHelper.QueryInventoryFinishedListener mGotInventoryListener =
            new IabHelper.QueryInventoryFinishedListener() {
                Context context = SplashActivity.splashActivity.getApplicationContext();
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);

                public void onQueryInventoryFinished(IabResult result,
                                                     Inventory inventory) {
                    // Have we been disposed of in the meantime? If so, quit.
                    if (mHelper == null)
                        return;

                    // Is it a failure?
                    if (result.isFailure()) {
                        // complain("Failed to query inventory: " + result);
                        return;
                    }

                    /*
                     * Check for items we own. Notice that for each purchase, we check
                     * the developer payload to see if it's correct! See
                     * verifyDeveloperPayload().
                     */

                    // Do we have the premium upgrade?
                    Purchase isMonthlySubscription = inventory.getPurchase(SKU_ICE_PREMIUM_MONTHLY);
                    Purchase isYearlySubscription = inventory.getPurchase(SKU_ICE_PREMIUM_YEARLY);
                    Purchase isPremium2 = inventory.getPurchase(SKU_ICE_PREMIUM_2);
                    Purchase isPremium5 = inventory.getPurchase(SKU_ICE_PREMIUM_5);
                    Purchase isPremium10 = inventory.getPurchase(SKU_ICE_PREMIUM_10);

                    if (isPremium2 != null) {
                        sharedPref.edit().putBoolean("isPremium2", true).apply();
                    } else {
                        sharedPref.edit().putBoolean("isPremium2", false).apply();
                    }

                    if (isPremium5 != null) {
                        sharedPref.edit().putBoolean("isPremium5", true).apply();
                    } else {
                        sharedPref.edit().putBoolean("isPremium5", false).apply();
                    }

                    if (isPremium10 != null) {
                        sharedPref.edit().putBoolean("isPremium10", true).apply();
                    } else {
                        sharedPref.edit().putBoolean("isPremium10", false).apply();
                    }

                    if (isMonthlySubscription != null) {
                        sharedPref.edit().putBoolean("isMonthly", true).apply();
                        sharedPref.edit().putBoolean("isYearly", false).apply();
                        subscriptionType = SKU_ICE_PREMIUM_MONTHLY;
                    } else if (isYearlySubscription != null) {
                        sharedPref.edit().putBoolean("isYearly", true).apply();
                        sharedPref.edit().putBoolean("isMonthly", false).apply();
                        subscriptionType = SKU_ICE_PREMIUM_YEARLY;
                    } else {
                        sharedPref.edit().putBoolean("isMonthly", false).apply();
                        sharedPref.edit().putBoolean("isYearly", false).apply();
                        subscriptionType = "";
                    }
                }
            };
    // Callback for when a purchase is finished
    private IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener =
            new IabHelper.OnIabPurchaseFinishedListener() {
                Context context = SplashActivity.splashActivity.getApplicationContext();
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);

                public void onIabPurchaseFinished(IabResult result, Purchase purchase) {

                    // if we were disposed of in the meantime, quit.
                    if (mHelper == null)
                        return;

                    if (result.isFailure()) {
                        complain("Error purchasing: " + result);
                        return;
                    }
                    if (!verifyDeveloperPayload()) {
                        complain("Error purchasing. Authenticity verification failed.");
                        return;
                    }

                    switch (purchase.getSku()) {
                        case SKU_ICE_PREMIUM_10:
                            sharedPref.edit().putBoolean("isPremium10", true).apply();
                            restartSelfOnLicenseOK();
                            break;
                        case SKU_ICE_PREMIUM_5:
                            sharedPref.edit().putBoolean("isPremium5", true).apply();
                            restartSelfOnLicenseOK();
                            break;
                        case SKU_ICE_PREMIUM_2:
                            sharedPref.edit().putBoolean("isPremium2", true).apply();
                            restartSelfOnLicenseOK();
                            break;
                        case SKU_ICE_PREMIUM_MONTHLY:
                            sharedPref.edit().putBoolean("isMonthly", true).apply();
                            restartSelfOnLicenseOK();
                            break;
                        case SKU_ICE_PREMIUM_YEARLY:
                            sharedPref.edit().putBoolean("isYearly", true).apply();
                            restartSelfOnLicenseOK();
                            break;
                    }

                }
            };

    public MyBilling(Activity launcher) {
        this.activity = launcher;
    }

    public void onCreate() {

        // Create the helper, passing it our context and the public key to
        // verify signatures with
        //Log.d(TAG, "Creating IAB helper.");
        mHelper = new IabHelper(activity, base64EncodedPublicKey);

        // enable debug logging (for a production application, you should set
        // this to false).
        mHelper.enableDebugLogging(false);

        // Start setup. This is asynchronous and the specified listener
        // will be called once setup completes.
        //Log.d(TAG, "Starting setup.");
        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
                //Log.d(TAG, "Setup finished.");

                if (!result.isSuccess()) {
                    // Oh noes, there was a problem.
                    // complain("Problem setting up in-app billing: " + result);
                    return;
                }

                // Have we been disposed off in the meantime? If so, quit.
                if (mHelper == null)
                    return;

                // IAB is fully set up. Now, let's get an inventory of stuff we
                // own.
                //Log.d(TAG, "Setup successful. Querying inventory.");
                try {
                    mHelper.queryInventoryAsync(mGotInventoryListener);
                } catch (IabHelper.IabAsyncInProgressException ignored) {
                }
            }
        });
    }

    public void purchaseSubscriptionMonthly() {
        final Context context = SplashActivity.splashActivity.getApplicationContext();
        if (!mHelper.subscriptionsSupported()) {
            complain(context.getResources().getString(R.string.subscription_error));
        } else {
            activity.runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    List<String> oldSkus = null;
                    if (!TextUtils.isEmpty(subscriptionType)) {
                        // The user currently has a valid subscription, any purchase action is
                        // going to
                        // replace that subscription
                        oldSkus = new ArrayList<>();
                        oldSkus.add(SKU_ICE_PREMIUM_YEARLY);
                    }
                    mHelper.flagEndAsync();
                    try {
                        mHelper.launchPurchaseFlow(
                                activity,
                                SKU_ICE_PREMIUM_MONTHLY,
                                IabHelper.ITEM_TYPE_SUBS,
                                oldSkus,
                                RC_REQUEST,
                                mPurchaseFinishedListener,
                                payload);
                    } catch (IabHelper.IabAsyncInProgressException e) {
                        complain(context.getString(R.string.error_license_flow));
                    }
                }
            });
        }
    }

    public void Premium2() {
        activity.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                mHelper.flagEndAsync();
                try {

                    mHelper.launchPurchaseFlow(activity, SKU_ICE_PREMIUM_2,
                            RC_REQUEST, mPurchaseFinishedListener, payload);
                } catch (IabHelper.IabAsyncInProgressException e) {
                    Context context = SplashActivity.splashActivity.getApplicationContext();
                    complain(context.getString(R.string.error_license_flow));
                }
            }
        });
    }

    public void Premium5() {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mHelper.flagEndAsync();
                try {
                    mHelper.launchPurchaseFlow(activity, SKU_ICE_PREMIUM_5,
                            RC_REQUEST, mPurchaseFinishedListener, payload);
                } catch (IabHelper.IabAsyncInProgressException e) {
                    Context context = SplashActivity.splashActivity.getApplicationContext();
                    complain(context.getString(R.string.error_license_flow));
                }
            }
        });
    }

    public void Premium10() {

        activity.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                mHelper.flagEndAsync();
                try {
                    mHelper.launchPurchaseFlow(activity, SKU_ICE_PREMIUM_10,
                            RC_REQUEST, mPurchaseFinishedListener, payload);
                } catch (IabHelper.IabAsyncInProgressException e) {
                    Context context = SplashActivity.splashActivity.getApplicationContext();
                    complain(context.getString(R.string.error_license_flow));
                }
            }
        });
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (mHelper == null)
            return;

        // Pass on the activity result to the helper for handling
        mHelper.handleActivityResult(requestCode, resultCode, data);
    }

    /**
     * Verifies the developer payload of a purchase.
     */
    private boolean verifyDeveloperPayload() {
        /*
         * DONE: verify that the developer payload of the purchase is correct.
         * It will be the same one that you sent when initiating the purchase.
         *
         * WARNING: Locally generating a random string when starting a purchase
         * and verifying it here might seem like a good approach, but this will
         * fail in the case where the user purchases an item on one device and
         * then uses your app on a different device, because on the other device
         * you will not have access to the random string you originally
         * generated.
         *
         * So a good developer payload has these characteristics:
         *
         * 1. If two different users purchase an item, the payload is different
         * between them, so that one user's purchase can't be replayed to
         * another user.
         *
         * 2. The payload must be such that you can verify it even when the app
         * wasn't the one who initiated the purchase flow (so that items
         * purchased by the user on one device work on other devices owned by
         * the user).
         *
         * Using your own server to store and verify developer payloads across
         * app installations is recommended.
         */
        return true;
    }

    // We're being destroyed. It's important to dispose of the helper here!
    public void onDestroy() {

        // very important:
        //Log.d(TAG, "Destroying helper.");
        if (mHelper != null) {
            try {
                mHelper.dispose();
            } catch (IabHelper.IabAsyncInProgressException e) {
                // Should not be thrown, because we reset mAsyncInProgress immediately before
                // calling dispose().
            }
            mHelper = null;
        }
    }

    private void complain(String message) {
        alert(message);
    }

    private void alert(final String message) {
        activity.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                AlertDialog.Builder bld = new AlertDialog.Builder(activity);
                bld.setMessage(message);
                bld.setNeutralButton(android.R.string.ok, null);
                bld.create().show();
            }
        });
    }


    public void restartSelfOnLicenseOK() {
        final Context context = SplashActivity.splashActivity.getApplicationContext();
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
        alertDialog.setTitle(context.getResources().getString(R.string.app_name));
        alertDialog.setMessage(context.getString(R.string.tweakshelper_restartSelfOnLicenseOK_dialog));
        alertDialog.setPositiveButton(context.getResources().getString(R.string.ok), new
                DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
/*        am.set(AlarmManager.RTC_WAKEUP, Calendar.getInstance().getTimeInMillis() + 500, // one second
                PendingIntent.getActivity(getActivity(), 0, getActivity().getIntent(), PendingIntent.FLAG_ONE_SHOT
                        | PendingIntent.FLAG_CANCEL_CURRENT));*/
                        Intent i = context.getPackageManager()
                                .getLaunchIntentForPackage(context.getPackageName());
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        context.startActivity(i);
                    }
                });
        alertDialog.show();
    }
}