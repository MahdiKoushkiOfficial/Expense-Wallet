package com.mahdi.expensewallet.ui.pages;

import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mahdi.expensewallet.model.Account;
import com.mahdi.expensewallet.model.DateRange;

import java.util.Collections;

import static com.mahdi.expensewallet.data.DatabaseStorage.database;
import static com.mahdi.expensewallet.data.DatabaseStorage.saveDb;
import static com.mahdi.expensewallet.data.FinanceManager.accBalance;
import static com.mahdi.expensewallet.data.FinanceManager.history;
import static com.mahdi.expensewallet.data.FinanceManager.sums;
import static com.mahdi.expensewallet.data.SettingsManager.base;
import static com.mahdi.expensewallet.ui.Dialogs.confirm;
import static com.mahdi.expensewallet.ui.Dialogs.inputDlg;
import static com.mahdi.expensewallet.ui.Navigator.pop;
import static com.mahdi.expensewallet.ui.Navigator.render;
import static com.mahdi.expensewallet.ui.SharedWidgets.histList;
import static com.mahdi.expensewallet.ui.Theme.expenseColor;
import static com.mahdi.expensewallet.ui.Theme.incomeColor;
import static com.mahdi.expensewallet.ui.Theme.secondarySurfaceColor;
import static com.mahdi.expensewallet.ui.Theme.secondaryTextColor;
import static com.mahdi.expensewallet.ui.Theme.textColor;
import static com.mahdi.expensewallet.ui.UiHelper.TXT;
import static com.mahdi.expensewallet.ui.UiHelper.actIcon;
import static com.mahdi.expensewallet.ui.UiHelper.amountText;
import static com.mahdi.expensewallet.ui.UiHelper.box;
import static com.mahdi.expensewallet.ui.UiHelper.btn;
import static com.mahdi.expensewallet.ui.UiHelper.card;
import static com.mahdi.expensewallet.ui.UiHelper.circ;
import static com.mahdi.expensewallet.ui.UiHelper.dp;
import static com.mahdi.expensewallet.ui.UiHelper.header;
import static com.mahdi.expensewallet.ui.UiHelper.iconLabel;
import static com.mahdi.expensewallet.ui.UiHelper.periodNav;
import static com.mahdi.expensewallet.ui.UiHelper.put;
import static com.mahdi.expensewallet.ui.UiHelper.putW;
import static com.mahdi.expensewallet.ui.UiHelper.tv;
import static com.mahdi.expensewallet.utils.Constants.ACC_COLORS;
import static com.mahdi.expensewallet.utils.Constants.ACC_ICONS;
import static com.mahdi.expensewallet.utils.Constants.ACC_KEYS;
import static com.mahdi.expensewallet.utils.Constants.ICON_EXPENSE;
import static com.mahdi.expensewallet.utils.Constants.ICON_INCOME;
import static com.mahdi.expensewallet.utils.CurrencyUtils.exch;
import static com.mahdi.expensewallet.utils.CurrencyUtils.money;
import static com.mahdi.expensewallet.utils.FormatUtils.hid;
import static com.mahdi.expensewallet.utils.FormatUtils.monthPeriod;
import static com.mahdi.expensewallet.utils.Translator.t;

public class AccountPage extends Page {
    final Account a;
    int off = 0;

    public AccountPage(Account a) { this.a = a; }

    @Override public void build(LinearLayout c) {
        header(c, a.name, true,
                actIcon("ic_edit", () -> inputDlg(t("rename"), t("account_name"), a.name, TXT, s -> {
                    if (!s.trim().isEmpty()) { a.name = s.trim(); saveDb(); render(); }
                })),
                actIcon("ic_delete", () -> confirm(t("delete_account"), t("delete_account_q", a.name), t("delete"), () -> {
                    for (int i = database.transactions.size() - 1; i >= 0; i--) if (database.transactions.get(i).accountId.equals(a.id)) database.transactions.remove(i);
                    for (int i = database.savings.size() - 1; i >= 0; i--) if (database.savings.get(i).accountId.equals(a.id)) database.savings.remove(i);
                    for (int i = database.plannedPayments.size() - 1; i >= 0; i--) if (database.plannedPayments.get(i).accountId.equals(a.id)) database.plannedPayments.remove(i);
                    database.accounts.remove(a);
                    saveDb();
                    pop();
                })));
        LinearLayout bc = card();
        LinearLayout r = box(true);
        r.setGravity(Gravity.CENTER_VERTICAL);
        put(r, circ(ACC_ICONS[a.type], 46, ACC_COLORS[a.type]), dp(46), dp(46), 0, 0, 12, 0);
        LinearLayout col = box(false);
        col.addView(tv(t(ACC_KEYS[a.type]), 12, secondaryTextColor, 1));
        TextView bv = tv("", 26, textColor, 3);
        amountText(bv, hid(money(accBalance(a), base)), 26);
        col.addView(bv);
        String px = exch(accBalance(a), base);
        if (!px.isEmpty()) {
            TextView ev = tv(hid(px), 12, secondaryTextColor, 1);
            ev.setTextDirection(View.TEXT_DIRECTION_LTR);
            col.addView(ev);
        }
        putW(r, col, 1f, 0, 0);
        bc.addView(r);
        put(c, bc, 0, 10);
        LinearLayout mv = box(true);
        final int idx = database.accounts.indexOf(a);
        putW(mv, btn(t("move_up"), secondarySurfaceColor, textColor, () -> {
            if (idx > 0) { Collections.swap(database.accounts, idx, idx - 1); saveDb(); render(); }
        }), 1f, 0, 6);
        putW(mv, btn(t("move_down"), secondarySurfaceColor, textColor, () -> {
            if (idx < database.accounts.size() - 1) { Collections.swap(database.accounts, idx, idx + 1); saveDb(); render(); }
        }), 1f, 6, 0);
        put(c, mv, 0, 10);
        final DateRange p = monthPeriod(off);
        put(c, periodNav(p.label, () -> { off--; render(); }, () -> { off++; render(); }, null), 0, 6);
        double[] s = sums(p.s, p.e, a.id, null);
        LinearLayout sc = card();
        LinearLayout sr = box(true);
        LinearLayout i1 = box(false);
        i1.addView(iconLabel(ICON_INCOME, t("income"), 12, secondaryTextColor));
        i1.addView(tv(money(s[0], base), 15, incomeColor, 2));
        LinearLayout i2 = box(false);
        i2.addView(iconLabel(ICON_EXPENSE, t("expense"), 12, secondaryTextColor));
        i2.addView(tv(money(s[1], base), 15, expenseColor, 2));
        putW(sr, i1, 1f, 0, 8);
        putW(sr, i2, 1f, 8, 0);
        sc.addView(sr);
        put(c, sc, 0, 8);
        histList(c, history(p.s, p.e, a.id, null), 100, null);
    }
}
