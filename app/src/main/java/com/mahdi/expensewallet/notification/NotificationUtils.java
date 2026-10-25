package com.mahdi.expensewallet.notification;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;

import com.mahdi.expensewallet.MainActivity;

import java.util.Calendar;

import static com.mahdi.expensewallet.MainActivity.activity;
import static com.mahdi.expensewallet.utils.Constants.PREFS;
import static com.mahdi.expensewallet.utils.Constants.REQ_NOTIF;
import static com.mahdi.expensewallet.utils.Translator.strFor;

/** Daily reminder: permission, alarm scheduling and the notification itself. */
public final class NotificationUtils {
    private NotificationUtils() { }

    public static boolean pendingTest = false;

    public static boolean hasNotifPermission() {
        return Build.VERSION.SDK_INT < 33
                || activity.checkSelfPermission("android.permission.POST_NOTIFICATIONS") == android.content.pm.PackageManager.PERMISSION_GRANTED;
    }

    public static void requestNotifPermission() {
        if (Build.VERSION.SDK_INT >= 33) activity.requestPermissions(new String[]{"android.permission.POST_NOTIFICATIONS"}, REQ_NOTIF);
    }

    public static final String CHANNEL = "daily_reminder";

    public static PendingIntent alarmPi(Context c, int code) {
        return PendingIntent.getBroadcast(c, code, new Intent(c, ReminderReceiver.class),
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
    }

    /**
     * Arms the NEXT occurrence only (the receiver re-arms the following day). Exact when Android allows it,
     * otherwise a short 10-minute window plus an idle-safe fallback, so it never drifts by hours.
     */
    public static void schedule(Context c, boolean on, int h, int m) {
        AlarmManager am = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
        if (am == null) return;
        PendingIntent p1 = alarmPi(c, 7), p2 = alarmPi(c, 8);
        am.cancel(p1);
        am.cancel(p2);
        if (!on) return;
        Calendar t = Calendar.getInstance();
        t.set(Calendar.HOUR_OF_DAY, h);
        t.set(Calendar.MINUTE, m);
        t.set(Calendar.SECOND, 0);
        t.set(Calendar.MILLISECOND, 0);
        if (t.getTimeInMillis() <= System.currentTimeMillis() + 1000) t.add(Calendar.DAY_OF_MONTH, 1);
        long at = t.getTimeInMillis();
        try {
            if (Build.VERSION.SDK_INT < 31 || am.canScheduleExactAlarms()) {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, p1);
            } else {
                am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, p1);
                am.setWindow(AlarmManager.RTC_WAKEUP, at, 10 * 60 * 1000L, p2);
            }
        } catch (SecurityException e) {
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, p1);
        }
    }

    /** Posts the reminder notification right now (used by the daily alarm and the "test" button). */
    public static void notifyNow(Context c) {
        SharedPreferences sp = c.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        String lang = sp.getString("lang", "en");
        NotificationManager nm = (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm == null) return;
        if (Build.VERSION.SDK_INT >= 26)
            nm.createNotificationChannel(new NotificationChannel(CHANNEL, strFor(c, lang, "reminder_channel"), NotificationManager.IMPORTANCE_DEFAULT));
        PendingIntent pi = PendingIntent.getActivity(c, 0, new Intent(c, MainActivity.class), PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        Notification.Builder b = Build.VERSION.SDK_INT >= 26 ? new Notification.Builder(c, CHANNEL) : new Notification.Builder(c);
        int icon = c.getResources().getIdentifier("ic_notifications_light", "drawable", c.getPackageName());
        if (icon == 0) icon = android.R.drawable.ic_dialog_info;
        b.setSmallIcon(icon)
                .setContentTitle(strFor(c, lang, "reminder_title"))
                .setContentText(strFor(c, lang, "reminder_text"))
                .setContentIntent(pi)
                .setOnlyAlertOnce(true)
                .setAutoCancel(true);
        try { nm.notify(1, b.build()); } catch (Exception ignored) { }
    }

    /** Back to the initial values (a new activity instance used to start from these). */
    public static void reset() {
        pendingTest = false;
    }
}
