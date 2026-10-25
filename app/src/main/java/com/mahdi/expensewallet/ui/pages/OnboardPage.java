package com.mahdi.expensewallet.ui.pages;

import android.graphics.Color;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mahdi.expensewallet.model.Account;
import com.mahdi.expensewallet.ui.components.M3Switch;

import java.util.Arrays;
import java.util.Locale;

import static com.mahdi.expensewallet.MainActivity.activity;
import static com.mahdi.expensewallet.data.BackupManager.startImport;
import static com.mahdi.expensewallet.data.DatabaseStorage.database;
import static com.mahdi.expensewallet.data.DatabaseStorage.saveDb;
import static com.mahdi.expensewallet.data.ExchangeRateManager.fetchRates;
import static com.mahdi.expensewallet.data.SettingsManager.base;
import static com.mahdi.expensewallet.data.SettingsManager.calType;
import static com.mahdi.expensewallet.data.SettingsManager.lang;
import static com.mahdi.expensewallet.data.SettingsManager.onboarded;
import static com.mahdi.expensewallet.data.SettingsManager.saveSettings;
import static com.mahdi.expensewallet.data.SettingsManager.uname;
import static com.mahdi.expensewallet.ui.Navigator.render;
import static com.mahdi.expensewallet.ui.Navigator.stack;
import static com.mahdi.expensewallet.ui.Navigator.tabIdx;
import static com.mahdi.expensewallet.ui.Navigator.tabs;
import static com.mahdi.expensewallet.ui.Theme.accentColor;
import static com.mahdi.expensewallet.ui.Theme.secondarySurfaceColor;
import static com.mahdi.expensewallet.ui.Theme.secondaryTextColor;
import static com.mahdi.expensewallet.ui.Theme.surfaceColor;
import static com.mahdi.expensewallet.ui.Theme.textColor;
import static com.mahdi.expensewallet.ui.UiHelper.NUMS;
import static com.mahdi.expensewallet.ui.UiHelper.TXT;
import static com.mahdi.expensewallet.ui.UiHelper.box;
import static com.mahdi.expensewallet.ui.UiHelper.btn;
import static com.mahdi.expensewallet.ui.UiHelper.card;
import static com.mahdi.expensewallet.ui.UiHelper.chips;
import static com.mahdi.expensewallet.ui.UiHelper.circ;
import static com.mahdi.expensewallet.ui.UiHelper.click;
import static com.mahdi.expensewallet.ui.UiHelper.dp;
import static com.mahdi.expensewallet.ui.UiHelper.edit;
import static com.mahdi.expensewallet.ui.UiHelper.put;
import static com.mahdi.expensewallet.ui.UiHelper.putW;
import static com.mahdi.expensewallet.ui.UiHelper.section;
import static com.mahdi.expensewallet.ui.UiHelper.shape;
import static com.mahdi.expensewallet.ui.UiHelper.toast;
import static com.mahdi.expensewallet.ui.UiHelper.tv;
import static com.mahdi.expensewallet.utils.Constants.ACC_COLORS;
import static com.mahdi.expensewallet.utils.Constants.ACC_ICONS;
import static com.mahdi.expensewallet.utils.Constants.ACC_KEYS;
import static com.mahdi.expensewallet.utils.Constants.CURS;
import static com.mahdi.expensewallet.utils.CurrencyUtils.parseAmt;
import static com.mahdi.expensewallet.utils.CurrencyUtils.sym;
import static com.mahdi.expensewallet.utils.Translator.setLang;
import static com.mahdi.expensewallet.utils.Translator.t;

public class OnboardPage extends Page {
    int step = 0;
    String nm = uname;
    boolean[] on = {false, true, true};
    String[] names = new String[3];
    String[] bals = {"", "", ""};

    @Override public void back() { if (step > 0) { step--; render(); } else activity.finish(); }

    @Override public void build(LinearLayout c) {
        LinearLayout logo = box(false);
        logo.setGravity(Gravity.CENTER_HORIZONTAL);
        logo.addView(circ("ic_account_balance_wallet", 84, accentColor));
        put(c, logo, 24, 12);
        TextView title = tv(t(step == 0 ? "onb_welcome" : step == 1 ? "onb_currency_title" : "onb_accounts_title"), 26, textColor, 3);
        title.setGravity(Gravity.CENTER);
        put(c, title, 4, 6);
        TextView sub = tv(t(step == 0 ? "onb_welcome_sub" : step == 1 ? "onb_currency_sub" : "onb_accounts_sub"), 14, secondaryTextColor, 0);
        sub.setGravity(Gravity.CENTER);
        put(c, sub, 0, 20);
        if (step == 0) {
            c.addView(section(t("language")));
            String[] ln = {"English", "Deutsch", "فارسی"};
            String[] lc = {"en", "de", "fa"};
            int sel = Arrays.asList(lc).indexOf(lang);
            final String[] lcf = lc;
            put(c, chips(ln, sel, i -> { calType = i == 2 ? 1 : 0; setLang(lcf[i]); }), 6, 16);
            c.addView(section(t("your_name")));
            put(c, edit(t("name_hint"), nm, TXT, s -> nm = s), 6, 16);
            put(c, btn(t("restore_backup"), secondarySurfaceColor, textColor, () -> startImport()), 4, 8);
        } else if (step == 1) {
            for (final String cu : CURS) {
                LinearLayout r = box(true);
                r.setGravity(Gravity.CENTER_VERTICAL);
                r.setBackground(shape(base.equals(cu) ? accentColor : surfaceColor, 16));
                r.setPadding(dp(16), dp(16), dp(16), dp(16));
                int fg = base.equals(cu) ? Color.WHITE : textColor;
                putW(r, tv(t("cur_" + cu.toLowerCase(Locale.ROOT)), 16, fg, 2), 1f, 0, 0);
                r.addView(tv(sym(cu), 16, fg, 3));
                click(r, () -> { base = cu; render(); });
                put(c, r, 0, 8);
            }
            c.addView(section(t("calendar")));
            put(c, chips(new String[]{t("cal_gregorian"), t("cal_jalali")}, calType, i -> { calType = i; render(); }), 6, 8);
        } else {
            for (int i = 0; i < 3; i++) {
                final int k = i;
                if (names[k] == null) names[k] = t(ACC_KEYS[k]);
                LinearLayout cd = card();
                LinearLayout r = box(true);
                r.setGravity(Gravity.CENTER_VERTICAL);
                put(r, circ(ACC_ICONS[k], 44, ACC_COLORS[k]), dp(44), dp(44), 0, 0, 12, 0);
                putW(r, tv(t(ACC_KEYS[k]), 16, textColor, 2), 1f, 0, 0);
                M3Switch sw = new M3Switch(activity);
                sw.setChecked(on[k]);
                sw.setOnChange(v -> { on[k] = v; render(); });
                r.addView(sw);
                cd.addView(r);
                if (on[k]) {
                    put(cd, edit(t("account_name"), names[k], TXT, s -> names[k] = s), 12, 0);
                    put(cd, edit(t("initial_balance") + " (" + sym(base) + ")", bals[k], NUMS, s -> bals[k] = s), 8, 0);
                }
                put(c, cd, 0, 10);
            }
        }
        LinearLayout nav = box(true);
        if (step > 0) putW(nav, btn(t("back"), secondarySurfaceColor, textColor, () -> back()), 1f, 0, 8);
        putW(nav, btn(t(step == 2 ? "finish" : "next"), accentColor, Color.WHITE, () -> next()), 2f, 0, 0);
        put(c, nav, 16, 0);
    }

    void next() {
        if (step == 0) { uname = nm.trim(); step = 1; render(); return; }
        if (step == 1) { step = 2; render(); return; }
        boolean any = false;
        for (boolean b : on) any |= b;
        if (!any) { toast(t("onb_need_one")); return; }
        database.accounts.clear();
        for (int k = 0; k < 3; k++) {
            if (!on[k]) continue;
            Account a = new Account();
            a.type = k;
            a.name = names[k] == null || names[k].trim().isEmpty() ? t(ACC_KEYS[k]) : names[k].trim();
            a.initial = parseAmt(bals[k]);
            database.accounts.add(a);
        }
        onboarded = true;
        saveSettings();
        saveDb();
        fetchRates(true);
        tabIdx = 0;
        stack.clear();
        stack.add(tabs[0]);
        render();
    }
}
