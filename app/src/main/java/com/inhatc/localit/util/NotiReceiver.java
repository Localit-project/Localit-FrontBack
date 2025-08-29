package com.inhatc.localit.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class NotiReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String msg = intent.getStringExtra("msg");
        if (msg != null) {
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
        }
    }
}