package com.mahdi.expensewallet.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import static com.mahdi.expensewallet.notification.NotificationUtils.notifyNow;
import static com.mahdi.expensewallet.notification.NotificationUtils.schedule;
import static com.mahdi.expensewallet.utils.Constants.PREFS;

public class ReminderReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context c, Intent i) {
        SharedPreferences sharedPreferences = c.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        if (!sharedPreferences.getBoolean("reminder", false)) return;
        boolean boot = i != null && Intent.ACTION_BOOT_COMPLETED.equals(i.getAction());
        if (!boot) notifyNow(c);
        schedule(c, true, sharedPreferences.getInt("remH", 20), sharedPreferences.getInt("remM", 0));
    }
}
