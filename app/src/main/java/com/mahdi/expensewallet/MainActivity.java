package com.mahdi.expensewallet;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;

import com.mahdi.expensewallet.data.BackupManager;
import com.mahdi.expensewallet.data.DatabaseStorage;
import com.mahdi.expensewallet.data.ExchangeRateManager;
import com.mahdi.expensewallet.data.SettingsManager;
import com.mahdi.expensewallet.notification.NotificationUtils;
import com.mahdi.expensewallet.ui.Navigator;
import com.mahdi.expensewallet.ui.Theme;
import com.mahdi.expensewallet.ui.UiHelper;
import com.mahdi.expensewallet.ui.pages.AccountsPage;
import com.mahdi.expensewallet.ui.pages.CategoriesPage;
import com.mahdi.expensewallet.ui.pages.HomePage;
import com.mahdi.expensewallet.ui.pages.OnboardPage;
import com.mahdi.expensewallet.ui.pages.Page;
import com.mahdi.expensewallet.ui.pages.SettingsPage;
import com.mahdi.expensewallet.utils.Translator;

import static com.mahdi.expensewallet.data.DatabaseStorage.loadDb;
import static com.mahdi.expensewallet.data.ExchangeRateManager.fetchRates;
import static com.mahdi.expensewallet.data.ExchangeRateManager.loadRates;
import static com.mahdi.expensewallet.data.SettingsManager.loadSettings;
import static com.mahdi.expensewallet.data.SettingsManager.onboarded;
import static com.mahdi.expensewallet.data.SettingsManager.remH;
import static com.mahdi.expensewallet.data.SettingsManager.remM;
import static com.mahdi.expensewallet.data.SettingsManager.remOn;
import static com.mahdi.expensewallet.data.SettingsManager.saveSettings;
import static com.mahdi.expensewallet.data.SettingsManager.sharedPreferences;
import static com.mahdi.expensewallet.notification.NotificationUtils.notifyNow;
import static com.mahdi.expensewallet.notification.NotificationUtils.pendingTest;
import static com.mahdi.expensewallet.notification.NotificationUtils.schedule;
import static com.mahdi.expensewallet.ui.Navigator.buildShell;
import static com.mahdi.expensewallet.ui.Navigator.render;
import static com.mahdi.expensewallet.ui.Navigator.stack;
import static com.mahdi.expensewallet.ui.Navigator.tabs;
import static com.mahdi.expensewallet.ui.Navigator.top;
import static com.mahdi.expensewallet.ui.Theme.density;
import static com.mahdi.expensewallet.ui.UiHelper.toast;
import static com.mahdi.expensewallet.utils.Constants.PREFS;
import static com.mahdi.expensewallet.utils.Constants.REQ_NOTIF;
import static com.mahdi.expensewallet.utils.Translator.applyLang;
import static com.mahdi.expensewallet.utils.Translator.t;

/**
 * Entry point of the app: sets the state up, builds the screen shell and forwards system callbacks.
 * Everything else lives in the model, data, utils, notification and ui packages.
 */
public class MainActivity extends Activity {

    /** The running activity; used by helpers that need a Context (views, dialogs, resources). */
    @SuppressLint("StaticFieldLeak")
    public static MainActivity activity;

    /** Every state holder back to its initial values (a new activity instance used to start clean). */
    private static void resetState() {
        Translator.reset();
        SettingsManager.reset();
        DatabaseStorage.reset();
        ExchangeRateManager.reset();
        NotificationUtils.reset();
        Theme.reset();
        Navigator.reset();
        UiHelper.reset();
    }

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        activity = this;
        resetState();
        sharedPreferences = getSharedPreferences(PREFS, MODE_PRIVATE);
        density = getResources().getDisplayMetrics().density;
        loadSettings();
        loadDb();
        loadRates();
        applyLang();
        buildShell();
        tabs = new Page[]{new HomePage(), new AccountsPage(), new SettingsPage(), new CategoriesPage()};
        if (onboarded) stack.add(tabs[0]); else stack.add(new OnboardPage());
        render();
        if (onboarded) fetchRates(false);
        if (onboarded && remOn) schedule(this, true, remH, remM);
    }

    @Override
    public void onBackPressed() { top().back(); }

    @Override
    public void onConfigurationChanged(Configuration c) {
        super.onConfigurationChanged(c);
        if (!stack.isEmpty()) render();
    }

    @Override
    public void onRequestPermissionsResult(int req, String[] perms, int[] res) {
        super.onRequestPermissionsResult(req, perms, res);
        if (req != REQ_NOTIF) return;
        boolean ok = res.length > 0 && res[0] == android.content.pm.PackageManager.PERMISSION_GRANTED;
        if (ok) {
            if (pendingTest) { pendingTest = false; notifyNow(this); toast(t("test_sent")); }
            return;
        }
        pendingTest = false;
        if (remOn) { remOn = false; saveSettings(); schedule(this, false, remH, remM); }
        toast(t("notif_denied"));
        render();
    }

    @Override
    protected void onActivityResult(int req, int res, Intent data) {
        super.onActivityResult(req, res, data);
        BackupManager.handleResult(req, res, data);
    }
}
