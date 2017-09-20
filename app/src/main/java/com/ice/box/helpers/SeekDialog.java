package com.ice.box.helpers;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import com.ice.box.R;

public class SeekDialog {
    Context mContext;

    public SeekDialog(Context pContext) {
        mContext = pContext;
    }

    public AlertDialog getSeekDialog(SeekEnum pEnum) {
        //  AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext).create();
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        String title;
        int viewInt;
        switch (pEnum) {
            case eBatterySize:
                viewInt = R.layout.seek_dialog;
                title = mContext.getResources().getString(R.string.battery_percentage_flash_title);
                break;
            case eNavBarHeight:
                viewInt = R.layout.seek_dialog;
                title = mContext.getResources().getString(R.string.tweaks_navbar_height_title);
                break;
            default:
                viewInt = R.layout.seek_dialog;
                title = "DEFAULT";
                break;
        }
        View view = inflater.inflate(viewInt, null);
        alertDialog.setView(view);
        alertDialog.setTitle(title);
       /* alertDialog.setNegativeButton("close", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
              
            }

        });*/
        return alertDialog.show();
    }

    public enum SeekEnum {
        eBatterySize,
        eNavBarHeight,
    }
}
