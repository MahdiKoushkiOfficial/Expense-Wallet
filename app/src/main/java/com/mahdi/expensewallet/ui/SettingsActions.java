package com.mahdi.expensewallet.ui;

import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.mahdi.expensewallet.model.Account;
import com.mahdi.expensewallet.model.AppDatabase;
import com.mahdi.expensewallet.model.PlannedPayment;
import com.mahdi.expensewallet.model.Saving;
import com.mahdi.expensewallet.model.Transaction;
import com.mahdi.expensewallet.ui.pages.OnboardPage;

import java.util.ArrayList;

import static com.mahdi.expensewallet.MainActivity.activity;
import static com.mahdi.expensewallet.data.DatabaseStorage.database;
import static com.mahdi.expensewallet.data.DatabaseStorage.saveDb;
import static com.mahdi.expensewallet.data.ExchangeRateManager.conv;
import static com.mahdi.expensewallet.data.ExchangeRateManager.fetchRates;
import static com.mahdi.expensewallet.data.ExchangeRateManager.rates;
import static com.mahdi.expensewallet.data.ExchangeRateManager.ratesTime;
import static com.mahdi.expensewallet.data.SettingsManager.base;
import static com.mahdi.expensewallet.data.SettingsManager.buffer;
import static com.mahdi.expensewallet.data.SettingsManager.exchCur;
import static com.mahdi.expensewallet.data.SettingsManager.fiscal;
import static com.mahdi.expensewallet.data.SettingsManager.loadSettings;
import static com.mahdi.expensewallet.data.SettingsManager.saveSettings;
import static com.mahdi.expensewallet.data.SettingsManager.sharedPreferences;
import static com.mahdi.expensewallet.notification.NotificationUtils.hasNotifPermission;
import static com.mahdi.expensewallet.notification.NotificationUtils.notifyNow;
import static com.mahdi.expensewallet.notification.NotificationUtils.pendingTest;
import static com.mahdi.expensewallet.notification.NotificationUtils.requestNotifPermission;
import static com.mahdi.expensewallet.notification.NotificationUtils.schedule;
import static com.mahdi.expensewallet.ui.Dialogs.confirm;
import static com.mahdi.expensewallet.ui.Dialogs.dlgB;
import static com.mahdi.expensewallet.ui.Dialogs.listDlg;
import static com.mahdi.expensewallet.ui.Dialogs.np;
import static com.mahdi.expensewallet.ui.Dialogs.showDlg;
import static com.mahdi.expensewallet.ui.Navigator.render;
import static com.mahdi.expensewallet.ui.Navigator.stack;
import static com.mahdi.expensewallet.ui.Navigator.tabIdx;
import static com.mahdi.expensewallet.ui.Theme.secondaryTextColor;
import static com.mahdi.expensewallet.ui.UiHelper.box;
import static com.mahdi.expensewallet.ui.UiHelper.dp;
import static com.mahdi.expensewallet.ui.UiHelper.put;
import static com.mahdi.expensewallet.ui.UiHelper.toast;
import static com.mahdi.expensewallet.ui.UiHelper.tv;
import static com.mahdi.expensewallet.utils.Constants.CURS;
import static com.mahdi.expensewallet.utils.Constants.WRAP;
import static com.mahdi.expensewallet.utils.CurrencyUtils.curLabel;
import static com.mahdi.expensewallet.utils.CurrencyUtils.round2;
import static com.mahdi.expensewallet.utils.Translator.applyLang;
import static com.mahdi.expensewallet.utils.Translator.t;

/** Actions behind the buttons of the settings page. */
public final class SettingsActions {
    private SettingsActions() { }

    public static void testNotification() {
        if (!hasNotifPermission()) { pendingTest = true; requestNotifPermission(); return; }
        notifyNow(activity);
        toast(t("test_sent"));
    }

    public static void chooseBase() {
        String[] n = new String[CURS.length];
        for (int i = 0; i < n.length; i++) n[i] = curLabel(CURS[i]);
        listDlg(t("base_currency"), n, i -> askBase(CURS[i]));
    }

    public static void chooseExchange() {
        final ArrayList<String> opts = new ArrayList<>();
        opts.add("");
        for (String cu : CURS) if (!cu.equals(base)) opts.add(cu);
        String[] n = new String[opts.size()];
        for (int i = 0; i < n.length; i++) n[i] = i == 0 ? t("none") : curLabel(opts.get(i));
        listDlg(t("exchange_currency"), n, i -> { exchCur = opts.get(i); saveSettings(); render(); });
    }

    /**
     * Re-denominates ALL stored amounts into a new base currency (no UI, no saving).
     * Returns false and changes nothing when an exchange rate is missing.
     */
    public static boolean rebase(String nb) {
        double rb = conv(1, base, nb);
        if (Double.isNaN(rb)) return false;
        for (Account a : database.accounts) a.initial = round2(a.initial * rb, nb);
        for (Transaction x : database.transactions) x.amount = round2(x.amount * rb, nb);
        for (PlannedPayment x : database.plannedPayments) x.amount = round2(x.amount * rb, nb);
        for (Saving x : database.savings) x.amount = round2(x.amount * rb, nb);
        for (double[] b : database.budgets.values()) b[1] = round2(b[1] * rb, nb);
        buffer = round2(buffer * rb, nb);
        base = nb;
        if (exchCur.equals(nb)) exchCur = "";
        return true;
    }

    public static void askBase(final String nb) {
        if (nb.equals(base)) return;
        if (rates.size() < CURS.length) { fetchRates(true); toast(t("rates_needed")); return; }
        confirm(t("base_currency"), t("base_change_q", curLabel(nb)), t("convert"), () -> {
            if (rebase(nb)) { saveDb(); saveSettings(); render(); toast(t("base_changed")); }
            else toast(t("rates_needed"));
        });
    }

    public static void fiscalDlg() {
        final NumberPicker pk = np(1, 28, fiscal);
        LinearLayout w = box(false);
        w.setGravity(Gravity.CENTER_HORIZONTAL);
        w.setPadding(dp(20), dp(16), dp(20), 0);
        TextView h = tv(t("fiscal_start_hint"), 13, secondaryTextColor, 0);
        w.addView(h);
        put(w, pk, WRAP, WRAP, 0, 8, 0, 0);
        showDlg(dlgB().setTitle(t("fiscal_start")).setView(w).setNegativeButton(t("cancel"), null)
                .setPositiveButton(t("ok"), (d, x) -> { fiscal = pk.getValue(); saveSettings(); render(); }));
    }

    public static void wipeAll() {
        sharedPreferences.edit().clear().apply();
        schedule(activity, false, 0, 0);
        database = new AppDatabase();
        rates.clear();
        ratesTime = 0;
        dismissedReset();
        loadSettings();
        applyLang();
        tabIdx = 0;
        stack.clear();
        stack.add(new OnboardPage());
        render();
    }

    public static void dismissedReset() { sharedPreferences.edit().remove("dismissed").apply(); }
}
