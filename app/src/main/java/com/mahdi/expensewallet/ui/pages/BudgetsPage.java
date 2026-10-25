package com.mahdi.expensewallet.ui.pages;

import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mahdi.expensewallet.model.BudgetSummary;

import java.util.ArrayList;

import static com.mahdi.expensewallet.data.DatabaseStorage.database;
import static com.mahdi.expensewallet.data.DatabaseStorage.saveDb;
import static com.mahdi.expensewallet.data.FinanceManager.budgetColor;
import static com.mahdi.expensewallet.data.FinanceManager.budgetStatus;
import static com.mahdi.expensewallet.data.FinanceManager.catColor;
import static com.mahdi.expensewallet.data.FinanceManager.catIcon;
import static com.mahdi.expensewallet.data.FinanceManager.catName;
import static com.mahdi.expensewallet.data.SettingsManager.base;
import static com.mahdi.expensewallet.ui.Dialogs.confirm;
import static com.mahdi.expensewallet.ui.Navigator.render;
import static com.mahdi.expensewallet.ui.SharedWidgets.openNewBudget;
import static com.mahdi.expensewallet.ui.Theme.secondaryTextColor;
import static com.mahdi.expensewallet.ui.Theme.textColor;
import static com.mahdi.expensewallet.ui.UiHelper.actIcon;
import static com.mahdi.expensewallet.ui.UiHelper.amtCol;
import static com.mahdi.expensewallet.ui.UiHelper.box;
import static com.mahdi.expensewallet.ui.UiHelper.card;
import static com.mahdi.expensewallet.ui.UiHelper.circ;
import static com.mahdi.expensewallet.ui.UiHelper.dp;
import static com.mahdi.expensewallet.ui.UiHelper.header;
import static com.mahdi.expensewallet.ui.UiHelper.periodNav;
import static com.mahdi.expensewallet.ui.UiHelper.progressBar;
import static com.mahdi.expensewallet.ui.UiHelper.put;
import static com.mahdi.expensewallet.ui.UiHelper.putW;
import static com.mahdi.expensewallet.ui.UiHelper.tv;
import static com.mahdi.expensewallet.utils.CurrencyUtils.money;
import static com.mahdi.expensewallet.utils.CurrencyUtils.sym;
import static com.mahdi.expensewallet.utils.FormatUtils.monthPeriod;
import static com.mahdi.expensewallet.utils.Translator.digits;
import static com.mahdi.expensewallet.utils.Translator.t;

public class BudgetsPage extends Page {
    int off = 0; // month shown here; independent of Home, starts at the current month

    @Override public void build(LinearLayout c) {
        header(c, t("budgets"), true, actIcon("ic_add", () -> openNewBudget()));
        put(c, tv(t("budgets_intro", sym(base)), 13, secondaryTextColor, 0), 0, 8);
        put(c, periodNav(monthPeriod(off).label, () -> { off--; render(); }, () -> { off++; render(); }, null), 0, 8);
        ArrayList<BudgetSummary> shown = budgetStatus(off);
        if (shown.isEmpty()) {
            TextView e = tv(t(database.budgets.isEmpty() ? "no_budgets" : "no_budgets_here"), 14, secondaryTextColor, 0);
            e.setGravity(Gravity.CENTER);
            put(c, e, 20, 20);
        }
        for (final BudgetSummary b : shown) {
            LinearLayout cd = card();
            cd.setPadding(dp(14), dp(10), dp(6), dp(14));
            LinearLayout r = box(true);
            r.setGravity(Gravity.CENTER_VERTICAL);
            put(r, circ(catIcon(b.category), 42, catColor(b.category)), dp(42), dp(42), 0, 0, 12, 0);
            LinearLayout col = box(false);
            col.addView(tv(catName(b.category), 15, textColor, 2));
            col.addView(tv(t("bp_" + b.type), 12, secondaryTextColor, 0));
            putW(r, col, 1f, 0, 8);
            r.addView(amtCol(money(b.limit, base), textColor, 15, b.limit, base, false));
            r.addView(actIcon("ic_delete", () -> confirm(t("delete"), t("delete_budget_q"), t("delete"), () -> {
                database.budgets.remove(b.category);
                saveDb();
                render();
            })));
            cd.addView(r);
            LinearLayout pw = box(false);
            pw.setPadding(0, 0, dp(8), 0);
            pw.addView(progressBar(Math.min(1.0, b.ratio), budgetColor(b.ratio)));
            put(cd, pw, 6, 6);
            TextView sv = tv(digits(String.valueOf(Math.round(b.ratio * 100))) + "% · " + money(b.spent, base) + " / " + money(b.limit, base), 12, secondaryTextColor, 1);
            sv.setTextDirection(View.TEXT_DIRECTION_LTR);
            LinearLayout sw = box(false);
            sw.setPadding(0, 0, dp(8), 0);
            sw.addView(sv);
            cd.addView(sw);
            put(c, cd, 0, 10);
        }
    }
}
