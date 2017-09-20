package com.ice.box.helpers;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by Adrian on 09.03.2017.
 */

public class NonRootHelper {

    Context mContext;

    public NonRootHelper(Context pContext) {
        mContext = pContext;
    }


    public void MakeToast(String string) {
        Toast.makeText(mContext, string, Toast.LENGTH_LONG).show();
    }

    public void MakeToastLong(String string) {
        Toast.makeText(mContext, string, Toast.LENGTH_LONG).show();
    }

}


