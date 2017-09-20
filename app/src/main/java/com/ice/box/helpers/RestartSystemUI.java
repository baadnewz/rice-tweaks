package com.ice.box.helpers;

import android.app.Activity;
import android.os.Bundle;

public class RestartSystemUI extends Activity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new Thread() {
            public void run() {
                RootUtils.runCommand("busybox killall com.android.systemui");
                RootUtils.runCommand("pkill com.android.systemui");
                finish();
            }
        }.start();
    }
}