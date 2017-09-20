package com.ice.box.qstiles;

import android.provider.Settings;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.util.Log;

import com.ice.box.R;

/**
 * Created by Adrian on 08.05.2017.
 */

public class ImmersiveMode extends TileService {

    private boolean mListening = false;

    private static String toggle(String current) {
        switch (current) {

            case "immersive.full=":
                return String.valueOf("immersive.full=*");
            case "immersive.full=*":
                return String.valueOf("immersive.full=");
        }

        return toggle(current);
    }

    private String returnlabel(String current) {
        switch (current) {
            case "immersive.full=":
                return getString(R.string.disabled);
            case "immersive.full=*":
                return getString(R.string.enabled);
        }

        return returnlabel(current);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onTileAdded() {

        super.onTileAdded();
    }

    @Override
    public void onTileRemoved() {
        super.onTileRemoved();
    }

    @Override
    public void onStartListening() {
        mListening = true;
        updateTile();
        super.onStartListening();
    }

    @Override
    public void onStopListening() {
        mListening = false;
        updateTile();
        super.onStopListening();
    }

    @Override
    public void onClick() {

        String immersive = Settings.Global.getString(getApplicationContext().getContentResolver(), "policy_control");
        if (immersive == null) {
            immersive = "immersive.full=";
        }


        try {
            Settings.Global.putString(getApplicationContext().getContentResolver(),
                    "policy_control", toggle(immersive));
        } catch (Exception e) {
            Log.e("ICEDEBUG", "Fail to set policy_control from QS Tile");
        }


        updateTile();
        super.onClick();
    }

    private void updateTile() {

        Tile tile = this.getQsTile();
        String currentState = Settings.Global.getString(getApplicationContext().getContentResolver(), "policy_control");
        if (currentState == null) {
            currentState = "immersive.full=";
        }

        //int currentState = Settings.Global.getInt(getApplicationContext().getContentResolver(), "adb_enabled", 0);

        //Icon newIcon;
        String newLabel;
        int newState;

        newLabel = returnlabel(currentState);

        tile.setLabel(newLabel);

        if (currentState.equals("immersive.full=*")) {
            newState = Tile.STATE_ACTIVE;
        } else {
            newState = Tile.STATE_INACTIVE;
        }

        tile.setState(newState);
        tile.updateTile();

    }
}
