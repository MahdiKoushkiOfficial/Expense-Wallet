package com.mahdi.expensewallet.ui.pages;

import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;

import com.mahdi.expensewallet.model.DateRange;

import static com.mahdi.expensewallet.data.DatabaseStorage.database;
import static com.mahdi.expensewallet.data.FinanceManager.catColor;
import static com.mahdi.expensewallet.data.FinanceManager.catIcon;
import static com.mahdi.expensewallet.data.FinanceManager.catName;
import static com.mahdi.expensewallet.data.FinanceManager.history;
import static com.mahdi.expensewallet.data.FinanceManager.sums;
import static com.mahdi.expensewallet.data.SettingsManager.base;
import static com.mahdi.expensewallet.ui.Navigator.push;
import static com.mahdi.expensewallet.ui.Navigator.render;
import static com.mahdi.expensewallet.ui.SharedWidgets.histList;
import static com.mahdi.expensewallet.ui.SharedWidgets.openBudgetForm;
import static com.mahdi.expensewallet.ui.Theme.expenseColor;
import static com.mahdi.expensewallet.ui.Theme.incomeColor;
import static com.mahdi.expensewallet.ui.Theme.secondaryTextColor;
import static com.mahdi.expensewallet.ui.Theme.textColor;
import static com.mahdi.expensewallet.ui.UiHelper.box;
import static com.mahdi.expensewallet.ui.UiHelper.card;
import static com.mahdi.expensewallet.ui.UiHelper.chips;
import static com.mahdi.expensewallet.ui.UiHelper.circ;
import static com.mahdi.expensewallet.ui.UiHelper.dp;
import static com.mahdi.expensewallet.ui.UiHelper.header;
import static com.mahdi.expensewallet.ui.UiHelper.iconLabel;
import static com.mahdi.expensewallet.ui.UiHelper.periodNav;
import static com.mahdi.expensewallet.ui.UiHelper.put;
import static com.mahdi.expensewallet.ui.UiHelper.putW;
import static com.mahdi.expensewallet.ui.UiHelper.settingRowCard;
import static com.mahdi.expensewallet.ui.UiHelper.tv;
import static com.mahdi.expensewallet.utils.Constants.ICON_EXPENSE;
import static com.mahdi.expensewallet.utils.Constants.ICON_INCOME;
import static com.mahdi.expensewallet.utils.CurrencyUtils.money;
import static com.mahdi.expensewallet.utils.FormatUtils.monthPeriod;
import static com.mahdi.expensewallet.utils.Translator.t;

public class CategoryPage extends Page {
    final String k;
    int off = 0;
    boolean all = false;

    public CategoryPage(String k) { this.k = k; }

    View statBlock(String title, double[] s) {
        LinearLayout b = card();
        b.addView(tv(title, 13, secondaryTextColor, 1));
        b.addView(tv(t("n_transactions", (int) s[3]), 17, textColor, 3));
        LinearLayout r = box(true);
        if (s[1] > 0) {
            LinearLayout e = box(false);
            e.addView(iconLabel(ICON_EXPENSE, t("spent"), 12, secondaryTextColor));
            e.addView(tv(money(s[1], base), 14, expenseColor, 2));
            putW(r, e, 1f, 0, 8);
        }
        if (s[0] > 0) {
            LinearLayout e = box(false);
            e.addView(iconLabel(ICON_INCOME, t("received"), 12, secondaryTextColor));
            e.addView(tv(money(s[0], base), 14, incomeColor, 2));
            putW(r, e, 1f, 0, 0);
        }
        put(b, r, 8, 0);
        return b;
    }

    @Override public void build(LinearLayout c) {
        header(c, catName(k), true);
        LinearLayout top = box(true);
        top.setGravity(Gravity.CENTER_VERTICAL);
        put(top, circ(catIcon(k), 56, catColor(k)), dp(56), dp(56), 0, 0, 14, 0);
        putW(top, tv(catName(k), 20, textColor, 3), 1f, 0, 0);
        put(c, top, 0, 14);
        final DateRange p = monthPeriod(off);
        put(c, statBlock(t("all_time"), sums(0, Long.MAX_VALUE, null, k)), 0, 10);
        put(c, statBlock(t("this_period", monthPeriod(0).label), sums(monthPeriod(0).s, monthPeriod(0).e, null, k)), 0, 10);
        if (!"salary".equals(k)) {
            double[] b = database.budgets.get(k);
            put(c, settingRowCard(t("budget"), b == null ? t("not_set") : money(b[1], base) + " · " + t("bp_" + (int) b[0]),
                    () -> { if (database.budgets.containsKey(k)) push(new BudgetsPage()); else openBudgetForm(k); }), 0, 10);
        }
        put(c, chips(new String[]{p.label, t("all_time")}, all ? 1 : 0, i -> { all = i == 1; render(); }), 4, 6);
        if (!all) put(c, periodNav(p.label, () -> { off--; render(); }, () -> { off++; render(); }, null), 0, 4);
        histList(c, history(all ? 0 : p.s, all ? Long.MAX_VALUE : p.e, null, k), 200, null);
    }
}
