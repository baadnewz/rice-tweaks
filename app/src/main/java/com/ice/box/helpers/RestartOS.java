package com.ice.box.helpers;

import android.app.Activity;
import android.os.Bundle;
import android.os.PowerManager;

public class RestartOS extends Activity {

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new Thread() {
            public void run() {
                PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
                powerManager.reboot(null);
                finish();
            }
        }.start();
    }
}