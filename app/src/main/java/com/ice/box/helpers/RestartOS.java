package com.ice.box.helpers;

import android.app.Activity;
import android.os.Bundle;
import android.os.PowerManager;

import com.ice.box.R;

public class RestartOS extends Activity {
    TweaksHelper tweaksHelper;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tweaksHelper = new TweaksHelper(getApplicationContext());
        try {
            PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
            powerManager.reboot(null);
        } catch (SecurityException e) {
            tweaksHelper.MakeToast(getResources().getString(R.string.fail_os_reboot));

        }
    }
}