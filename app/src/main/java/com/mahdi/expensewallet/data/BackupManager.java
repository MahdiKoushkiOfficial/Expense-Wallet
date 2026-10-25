package com.mahdi.expensewallet.data;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

import com.mahdi.expensewallet.model.AppDatabase;
import com.mahdi.expensewallet.utils.CalendarUtils;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;

import org.json.JSONObject;

import static com.mahdi.expensewallet.MainActivity.activity;
import static com.mahdi.expensewallet.data.DatabaseStorage.database;
import static com.mahdi.expensewallet.data.DatabaseStorage.saveDb;
import static com.mahdi.expensewallet.data.ExchangeRateManager.fetchRates;
import static com.mahdi.expensewallet.data.SettingsManager.base;
import static com.mahdi.expensewallet.data.SettingsManager.buffer;
import static com.mahdi.expensewallet.data.SettingsManager.calType;
import static com.mahdi.expensewallet.data.SettingsManager.exchCur;
import static com.mahdi.expensewallet.data.SettingsManager.fiscal;
import static com.mahdi.expensewallet.data.SettingsManager.hide;
import static com.mahdi.expensewallet.data.SettingsManager.lang;
import static com.mahdi.expensewallet.data.SettingsManager.onboarded;
import static com.mahdi.expensewallet.data.SettingsManager.remH;
import static com.mahdi.expensewallet.data.SettingsManager.remM;
import static com.mahdi.expensewallet.data.SettingsManager.remOn;
import static com.mahdi.expensewallet.data.SettingsManager.saveSettings;
import static com.mahdi.expensewallet.data.SettingsManager.settingsJs;
import static com.mahdi.expensewallet.data.SettingsManager.theme;
import static com.mahdi.expensewallet.data.SettingsManager.uname;
import static com.mahdi.expensewallet.notification.NotificationUtils.schedule;
import static com.mahdi.expensewallet.ui.Dialogs.confirm;
import static com.mahdi.expensewallet.ui.Navigator.render;
import static com.mahdi.expensewallet.ui.Navigator.stack;
import static com.mahdi.expensewallet.ui.Navigator.tabIdx;
import static com.mahdi.expensewallet.ui.Navigator.tabs;
import static com.mahdi.expensewallet.ui.UiHelper.toast;
import static com.mahdi.expensewallet.utils.Constants.REQ_EXPORT;
import static com.mahdi.expensewallet.utils.Constants.REQ_IMPORT;
import static com.mahdi.expensewallet.utils.FormatUtils.now;
import static com.mahdi.expensewallet.utils.Translator.applyLang;
import static com.mahdi.expensewallet.utils.Translator.t;

/** Backup export / import (JSON file chosen through the system file picker). */
public final class BackupManager {
    private BackupManager() { }

    public static void startExport() {
        Intent i = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("application/json");
        i.putExtra(Intent.EXTRA_TITLE, "wallet-backup-" + new java.text.SimpleDateFormat("yyyyMMdd", Locale.US).format(new java.util.Date()) + ".json");
        try { activity.startActivityForResult(i, REQ_EXPORT); } catch (Exception e) { toast(t("backup_failed")); }
    }

    public static void startImport() {
        Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("*/*");
        try { activity.startActivityForResult(i, REQ_IMPORT); } catch (Exception e) { toast(t("backup_failed")); }
    }

    /** Called by MainActivity.onActivityResult after the file picker returned. */
    public static void handleResult(int req, int res, Intent data) {
        if (res != Activity.RESULT_OK || data == null || data.getData() == null) return;
        Uri u = data.getData();
        if (req == REQ_EXPORT) writeBackup(u);
        else if (req == REQ_IMPORT) readBackup(u);
    }

    public static void writeBackup(Uri u) {
        try {
            JSONObject o = new JSONObject();
            o.put("format", "wallet-backup");
            o.put("version", 1);
            o.put("exported", CalendarUtils.stamp(now()));
            o.put("settings", settingsJs());
            o.put("db", database.js());
            OutputStream out = activity.getContentResolver().openOutputStream(u);
            out.write(o.toString(2).getBytes("UTF-8"));
            out.close();
            toast(t("backup_saved"));
        } catch (Exception e) {
            toast(t("backup_failed"));
        }
    }

    public static void readBackup(Uri u) {
        try {
            InputStream in = activity.getContentResolver().openInputStream(u);
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            byte[] buf = new byte[8192];
            int n;
            while ((n = in.read(buf)) > 0) bo.write(buf, 0, n);
            in.close();
            final JSONObject o = new JSONObject(bo.toString("UTF-8"));
            if (!"wallet-backup".equals(o.optString("format")) || o.optJSONObject("db") == null) {
                toast(t("backup_invalid"));
                return;
            }
            final AppDatabase parsed = AppDatabase.of(o.getJSONObject("db")); // validates everything before anything is replaced
            final Runnable apply = () -> applyBackup(o, parsed);
            if (onboarded && (!database.accounts.isEmpty() || !database.transactions.isEmpty()))
                confirm(t("restore_backup"), t("restore_confirm"), t("restore"), apply);
            else apply.run();
        } catch (Exception e) {
            toast(t("backup_invalid"));
        }
    }

    public static void applyBackup(JSONObject o, AppDatabase parsed) {
        try {
            database = parsed;
            JSONObject s = o.optJSONObject("settings");
            if (s != null) {
                uname = s.optString("uname", uname);
                base = s.optString("base", base);
                lang = s.optString("lang", lang);
                theme = s.optInt("theme", theme);
                calType = s.optInt("cal", calType);
                fiscal = Math.max(1, Math.min(28, s.optInt("fiscal", fiscal)));
                hide = s.optBoolean("hide", hide);
                remOn = s.optBoolean("reminder", remOn);
                remH = s.optInt("remH", remH);
                remM = s.optInt("remM", remM);
                buffer = s.optDouble("buffer", buffer);
                exchCur = s.optString("exch", "");
                if (exchCur.equals(base)) exchCur = "";
            }
            onboarded = true;
            saveSettings();
            saveDb();
            schedule(activity, remOn, remH, remM);
            applyLang();
            fetchRates(false);
            tabIdx = 0;
            stack.clear();
            stack.add(tabs[0]);
            render();
            toast(t("restore_done"));
        } catch (Exception e) {
            toast(t("backup_invalid"));
        }
    }
}
