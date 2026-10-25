package com.mahdi.expensewallet.ui.pages;

import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mahdi.expensewallet.model.Account;

import static com.mahdi.expensewallet.data.DatabaseStorage.database;
import static com.mahdi.expensewallet.data.ExchangeRateManager.ratesMissing;
import static com.mahdi.expensewallet.data.FinanceManager.accBalance;
import static com.mahdi.expensewallet.data.FinanceManager.totalBalance;
import static com.mahdi.expensewallet.data.SettingsManager.base;
import static com.mahdi.expensewallet.data.SettingsManager.hide;
import static com.mahdi.expensewallet.data.SettingsManager.saveSettings;
import static com.mahdi.expensewallet.ui.Navigator.push;
import static com.mahdi.expensewallet.ui.Navigator.render;
import static com.mahdi.expensewallet.ui.Theme.accentColor;
import static com.mahdi.expensewallet.ui.Theme.expenseColor;
import static com.mahdi.expensewallet.ui.Theme.secondaryTextColor;
import static com.mahdi.expensewallet.ui.Theme.surfaceColor;
import static com.mahdi.expensewallet.ui.Theme.textColor;
import static com.mahdi.expensewallet.ui.Theme.warningColor;
import static com.mahdi.expensewallet.ui.UiHelper.actIcon;
import static com.mahdi.expensewallet.ui.UiHelper.amountText;
import static com.mahdi.expensewallet.ui.UiHelper.box;
import static com.mahdi.expensewallet.ui.UiHelper.btn;
import static com.mahdi.expensewallet.ui.UiHelper.card;
import static com.mahdi.expensewallet.ui.UiHelper.circ;
import static com.mahdi.expensewallet.ui.UiHelper.click;
import static com.mahdi.expensewallet.ui.UiHelper.dp;
import static com.mahdi.expensewallet.ui.UiHelper.header;
import static com.mahdi.expensewallet.ui.UiHelper.put;
import static com.mahdi.expensewallet.ui.UiHelper.putW;
import static com.mahdi.expensewallet.ui.UiHelper.shape;
import static com.mahdi.expensewallet.ui.UiHelper.tv;
import static com.mahdi.expensewallet.utils.Constants.ACC_COLORS;
import static com.mahdi.expensewallet.utils.Constants.ACC_ICONS;
import static com.mahdi.expensewallet.utils.Constants.ACC_KEYS;
import static com.mahdi.expensewallet.utils.CurrencyUtils.exch;
import static com.mahdi.expensewallet.utils.CurrencyUtils.money;
import static com.mahdi.expensewallet.utils.FormatUtils.hid;
import static com.mahdi.expensewallet.utils.Translator.t;

public class AccountsPage extends TabPage {
    @Override public void build(LinearLayout c) {
        header(c, t("accounts"), false, actIcon(hide ? "ic_visibility_off" : "ic_visibility", () -> { hide = !hide; saveSettings(); render(); }));
        LinearLayout tc = card();
        tc.addView(tv(t("total_balance"), 13, secondaryTextColor, 1));
        TextView big = tv("", 30, textColor, 3);
        amountText(big, hid(money(totalBalance(), base)), 30);
        put(tc, big, 4, 0);
        String tx = exch(totalBalance(), base);
        if (!tx.isEmpty()) {
            TextView ev = tv(hid(tx), 13, secondaryTextColor, 1);
            ev.setTextDirection(View.TEXT_DIRECTION_LTR);
            tc.addView(ev);
        }
        put(c, tc, 0, 14);
        if (ratesMissing) put(c, tv(t("rates_missing"), 12, warningColor, 1), 0, 10);
        for (final Account a : database.accounts) {
            LinearLayout r = box(true);
            r.setGravity(Gravity.CENTER_VERTICAL);
            r.setBackground(shape(surfaceColor, 18));
            r.setPadding(dp(14), dp(14), dp(16), dp(14));
            put(r, circ(ACC_ICONS[a.type], 46, ACC_COLORS[a.type]), dp(46), dp(46), 0, 0, 12, 0);
            LinearLayout col = box(false);
            col.addView(tv(a.name, 16, textColor, 2));
            col.addView(tv(t(ACC_KEYS[a.type]), 12, secondaryTextColor, 0));
            putW(r, col, 1f, 0, 8);
            LinearLayout am = box(false);
            am.setGravity(Gravity.END);
            double bal = accBalance(a);
            TextView bv = tv("", 16, bal < 0 ? expenseColor : textColor, 2);
            amountText(bv, hid(money(bal, base)), 16);
            am.addView(bv);
            String ax = exch(bal, base);
            if (!ax.isEmpty()) {
                TextView cv = tv(hid(ax), 11, secondaryTextColor, 0);
                cv.setTextDirection(View.TEXT_DIRECTION_LTR);
                am.addView(cv);
            }
            r.addView(am);
            click(r, () -> push(new AccountPage(a)));
            put(c, r, 0, 10);
        }
        put(c, btn(t("add_account"), accentColor, Color.WHITE, () -> push(new AccountFormPage(null))), 6, 0);
    }
}
