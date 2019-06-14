package com.neostra.imin.printerupdate;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;


public class NeostraPrinterUpdateReceiver extends BroadcastReceiver {
    private static final String TAG = "NeoPrinterUpdate";
    static final String ACTION = "android.intent.action.BOOT_COMPLETED";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(ACTION)) {
            Log.d(TAG, "onReceive BOOT_COMPLETED intent.getAction() = " + intent.getAction());
            Intent serviceIntent = new Intent(context, NeostraPrinterUpdateService.class);
            context.startService(serviceIntent);
        }
    }
}