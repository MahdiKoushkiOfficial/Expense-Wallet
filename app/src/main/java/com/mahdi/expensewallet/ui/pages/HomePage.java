package com.mahdi.expensewallet.ui.pages;

import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mahdi.expensewallet.model.BudgetSummary;
import com.mahdi.expensewallet.model.DateRange;
import com.mahdi.expensewallet.model.DuePayment;
import com.mahdi.expensewallet.model.HomeBudgetSummary;
import com.mahdi.expensewallet.utils.CalendarUtils;

import java.util.ArrayList;

import static com.mahdi.expensewallet.MainActivity.activity;
import static com.mahdi.expensewallet.data.DatabaseStorage.database;
import static com.mahdi.expensewallet.data.ExchangeRateManager.fetchRates;
import static com.mahdi.expensewallet.data.ExchangeRateManager.ratesMissing;
import static com.mahdi.expensewallet.data.FinanceManager.budgetColor;
import static com.mahdi.expensewallet.data.FinanceManager.budgetStatus;
import static com.mahdi.expensewallet.data.FinanceManager.bufferFill;
import static com.mahdi.expensewallet.data.FinanceManager.catColor;
import static com.mahdi.expensewallet.data.FinanceManager.catIcon;
import static com.mahdi.expensewallet.data.FinanceManager.catName;
import static com.mahdi.expensewallet.data.FinanceManager.dues;
import static com.mahdi.expensewallet.data.FinanceManager.history;
import static com.mahdi.expensewallet.data.FinanceManager.overPct;
import static com.mahdi.expensewallet.data.FinanceManager.pickHomeBudget;
import static com.mahdi.expensewallet.data.FinanceManager.sums;
import static com.mahdi.expensewallet.data.FinanceManager.totalBalance;
import static com.mahdi.expensewallet.data.FinanceManager.totalSaved;
import static com.mahdi.expensewallet.data.SettingsManager.base;
import static com.mahdi.expensewallet.data.SettingsManager.buffer;
import static com.mahdi.expensewallet.data.SettingsManager.hide;
import static com.mahdi.expensewallet.data.SettingsManager.saveSettings;
import static com.mahdi.expensewallet.data.SettingsManager.sharedPreferences;
import static com.mahdi.expensewallet.data.SettingsManager.uname;
import static com.mahdi.expensewallet.ui.Dialogs.inputDlg;
import static com.mahdi.expensewallet.ui.Dialogs.listDlg;
import static com.mahdi.expensewallet.ui.Navigator.push;
import static com.mahdi.expensewallet.ui.Navigator.render;
import static com.mahdi.expensewallet.ui.SharedWidgets.addDialog;
import static com.mahdi.expensewallet.ui.SharedWidgets.dueCard;
import static com.mahdi.expensewallet.ui.SharedWidgets.histList;
import static com.mahdi.expensewallet.ui.Theme.accentColor;
import static com.mahdi.expensewallet.ui.Theme.expenseColor;
import static com.mahdi.expensewallet.ui.Theme.incomeColor;
import static com.mahdi.expensewallet.ui.Theme.secondarySurfaceColor;
import static com.mahdi.expensewallet.ui.Theme.secondaryTextColor;
import static com.mahdi.expensewallet.ui.Theme.surfaceColor;
import static com.mahdi.expensewallet.ui.Theme.textColor;
import static com.mahdi.expensewallet.ui.Theme.warningColor;
import static com.mahdi.expensewallet.ui.UiHelper.NUM;
import static com.mahdi.expensewallet.ui.UiHelper.actIcon;
import static com.mahdi.expensewallet.ui.UiHelper.amountText;
import static com.mahdi.expensewallet.ui.UiHelper.box;
import static com.mahdi.expensewallet.ui.UiHelper.btn;
import static com.mahdi.expensewallet.ui.UiHelper.card;
import static com.mahdi.expensewallet.ui.UiHelper.circ;
import static com.mahdi.expensewallet.ui.UiHelper.click;
import static com.mahdi.expensewallet.ui.UiHelper.dp;
import static com.mahdi.expensewallet.ui.UiHelper.ico;
import static com.mahdi.expensewallet.ui.UiHelper.periodNav;
import static com.mahdi.expensewallet.ui.UiHelper.progressBar;
import static com.mahdi.expensewallet.ui.UiHelper.put;
import static com.mahdi.expensewallet.ui.UiHelper.putW;
import static com.mahdi.expensewallet.ui.UiHelper.section;
import static com.mahdi.expensewallet.ui.UiHelper.shape;
import static com.mahdi.expensewallet.ui.UiHelper.tv;
import static com.mahdi.expensewallet.utils.Constants.DAY;
import static com.mahdi.expensewallet.utils.Constants.ICON_EXPENSE;
import static com.mahdi.expensewallet.utils.Constants.ICON_INCOME;
import static com.mahdi.expensewallet.utils.Constants.WRAP;
import static com.mahdi.expensewallet.utils.CurrencyUtils.exch;
import static com.mahdi.expensewallet.utils.CurrencyUtils.money;
import static com.mahdi.expensewallet.utils.CurrencyUtils.parseAmt;
import static com.mahdi.expensewallet.utils.CurrencyUtils.sym;
import static com.mahdi.expensewallet.utils.FormatUtils.hid;
import static com.mahdi.expensewallet.utils.FormatUtils.monthPeriod;
import static com.mahdi.expensewallet.utils.FormatUtils.now;
import static com.mahdi.expensewallet.utils.Translator.digits;
import static com.mahdi.expensewallet.utils.Translator.t;

public class HomePage extends TabPage {
    int mode = 0, off = 0, limit = 30;

    DateRange period() {
        if (mode == 0) return monthPeriod(off);
        DateRange p = new DateRange();
        long n = now();
        p.e = CalendarUtils.dayStart(n) + DAY - 1;
        if (mode == 1) { p.s = CalendarUtils.dayStart(CalendarUtils.addDays(n, -29)); p.label = t("last_30"); }
        else if (mode == 2) { p.s = CalendarUtils.dayStart(CalendarUtils.addDays(n, -6)); p.label = t("last_7"); }
        else { p.s = 0; p.e = Long.MAX_VALUE; p.label = t("all_time"); }
        return p;
    }

    void pickMode() {
        listDlg(t("period"), new String[]{t("fiscal_month"), t("last_30"), t("last_7"), t("all_time")}, i -> { mode = i; off = 0; limit = 30; render(); });
    }

    View statBox(String icon, String label, String val) {
        LinearLayout b = box(false);
        LinearLayout r = box(true);
        r.setGravity(Gravity.CENTER_VERTICAL);
        put(r, ico(icon, 16, Color.WHITE), dp(16), dp(16), 0, 0, 6, 0);
        r.addView(tv(label, 12, 0xCCFFFFFF, 1));
        b.addView(r);
        TextView v = tv("", 16, Color.WHITE, 2);
        amountText(v, val, 16);
        b.addView(v);
        return b;
    }

    View tile(String icon, String label, final Runnable r) {
        LinearLayout t = box(false);
        t.setGravity(Gravity.CENTER_HORIZONTAL);
        t.setBackground(shape(surfaceColor, 18));
        t.setPadding(dp(8), dp(12), dp(8), dp(10));
        t.addView(circ(icon, 38, accentColor));
        TextView l = tv(label, 12, textColor, 1);
        l.setGravity(Gravity.CENTER);
        l.setSingleLine(true);
        put(t, l, WRAP, WRAP, 0, 8, 0, 0);
        click(t, r);
        return t;
    }

    /** Alert for over-limit budgets (only there), then ONE card: the most critical budget that is NOT over its limit. */
    void budgetSection(LinearLayout c) {
        HomeBudgetSummary homeBudget = pickHomeBudget(budgetStatus(mode == 0 ? off : 0));
        if (homeBudget.overCount > 0) {
            LinearLayout al = card();
            al.setPadding(dp(14), dp(12), dp(14), dp(12));
            LinearLayout ar = box(true);
            ar.setGravity(Gravity.CENTER_VERTICAL);
            put(ar, ico("ic_warning", 22, expenseColor), dp(22), dp(22), 0, 0, 10, 0);
            String msg = homeBudget.overCount == 1
                    ? t("budget_exceeded", catName(homeBudget.overOne.category), overPct(homeBudget.overOne.ratio))
                    : t("budgets_over_many", homeBudget.overCount);
            putW(ar, tv(msg, 14, textColor, 2), 1f, 0, 0);
            al.addView(ar);
            put(c, al, 0, 8);
        }
        if (homeBudget.shown == null) return;
        BudgetSummary budget = homeBudget.shown;
        LinearLayout bc = card();
        bc.setPadding(dp(14), dp(12), dp(14), dp(14));
        LinearLayout r = box(true);
        r.setGravity(Gravity.CENTER_VERTICAL);
        put(r, circ(catIcon(budget.category), 34, catColor(budget.category)), dp(34), dp(34), 0, 0, 10, 0);
        putW(r, tv(catName(budget.category), 15, textColor, 2), 1f, 0, 8);
        r.addView(tv(digits(String.valueOf(Math.round(budget.ratio * 100))) + "%", 14, budgetColor(budget.ratio), 2));
        bc.addView(r);
        put(bc, progressBar(Math.min(1.0, budget.ratio), budgetColor(budget.ratio)), 10, 0);
        click(bc, () -> push(new BudgetsPage()));
        put(c, bc, 0, 12);
    }

    @Override public void build(LinearLayout c) {
        final DateRange p = period();
        LinearLayout h = box(true);
        h.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout who = box(false);
        who.addView(tv(uname.isEmpty() ? t("app_title") : t("hello", uname), 22, textColor, 3));
        who.addView(tv(t("overview"), 13, secondaryTextColor, 0));
        putW(h, who, 1f, 0, 0);
        h.addView(actIcon("ic_search", () -> push(new SearchPage())));
        h.addView(actIcon(hide ? "ic_visibility_off" : "ic_visibility", () -> { hide = !hide; saveSettings(); render(); }));
        put(c, h, 4, 8);
        if (mode == 0) {
            put(c, periodNav(p.label, () -> { off--; limit = 30; render(); }, () -> { off++; limit = 30; render(); }, this::pickMode), 0, 8);
        } else {
            TextView pl = tv(p.label, 16, textColor, 2);
            pl.setGravity(Gravity.CENTER);
            pl.setPadding(dp(8), dp(10), dp(8), dp(10));
            click(pl, this::pickMode);
            put(c, pl, 0, 8);
        }

        double bal = totalBalance();
        double[] sm = sums(p.s, p.e, null, null);
        double saved = totalSaved();
        LinearLayout bc = box(false);
        bc.setBackground(shape(accentColor, 24));
        bc.setPadding(dp(20), dp(18), dp(20), dp(18));
        bc.addView(tv(t("balance"), 13, 0xCCFFFFFF, 1));
        TextView big = tv("", 34, Color.WHITE, 3);
        amountText(big, hid(money(bal, base)), 34);
        put(bc, big, 2, 0);
        String bx = exch(bal, base);
        if (!bx.isEmpty()) {
            TextView ev = tv(hid(bx), 13, 0xCCFFFFFF, 1);
            ev.setTextDirection(View.TEXT_DIRECTION_LTR);
            bc.addView(ev);
        }
        LinearLayout sr = box(true);
        putW(sr, statBox(ICON_INCOME, t("income"), hid(money(sm[0], base))), 1f, 0, 8);
        putW(sr, statBox(ICON_EXPENSE, t("expense"), hid(money(sm[1], base))), 1f, 8, 0);
        put(bc, sr, 12, 0);
        if (saved > 0 || sm[2] > 0)
            put(bc, tv(t("saved_line", hid(money(sm[2], base)), hid(money(saved, base))), 12, 0xCCFFFFFF, 1), 12, 0);
        put(c, bc, 0, 12);

        if (ratesMissing) {
            LinearLayout w = card();
            w.addView(tv(t("rates_missing"), 13, warningColor, 1));
            put(w, btn(t("retry"), secondarySurfaceColor, textColor, () -> fetchRates(true)), 8, 0);
            put(c, w, 0, 12);
        }

        LinearLayout bf = card();
        if (buffer <= 0) {
            bf.addView(tv(t("buffer"), 15, textColor, 2));
            bf.addView(tv(t("buffer_none"), 13, secondaryTextColor, 0));
        } else {
            double diff = bal - buffer;
            boolean below = diff < 0;
            double fill = bufferFill(bal, buffer);
            LinearLayout br = box(true);
            putW(br, tv(t("buffer"), 15, textColor, 2), 1f, 0, 0);
            TextView bv = tv(hid(money(buffer, base)), 14, secondaryTextColor, 1);
            bv.setTextDirection(View.TEXT_DIRECTION_LTR);
            br.addView(bv);
            bf.addView(br);
            LinearLayout dl = box(true);
            dl.setGravity(Gravity.CENTER_VERTICAL);
            if (below) put(dl, ico("ic_buffer_danger", 18, expenseColor), dp(18), dp(18), 0, 0, 6, 0);
            dl.addView(tv(hid(t(diff >= 0 ? "buffer_above" : "buffer_below", money(Math.abs(diff), base))), 13, diff >= 0 ? incomeColor : expenseColor, 1));
            bf.addView(dl);
            put(bf, progressBar(fill, fill >= 0.8 ? expenseColor : incomeColor), 10, 0);
        }
        click(bf, () -> inputDlg(t("buffer"), t("buffer_hint", sym(base)), buffer > 0 ? String.valueOf(buffer) : "", NUM,
                txt -> { buffer = Math.max(0, parseAmt(txt)); saveSettings(); render(); }));
        put(c, bf, 0, 12);

        HorizontalScrollView hs = new HorizontalScrollView(activity);
        hs.setHorizontalScrollBarEnabled(false);
        LinearLayout tr = box(true);
        put(tr, tile("ic_repeat", t("planned"), () -> push(new PlannedPage())), dp(96), WRAP, 0, 0, 8, 0);
        put(tr, tile("ic_paid", t("budgets"), () -> push(new BudgetsPage())), dp(96), WRAP, 0, 0, 8, 0);
        put(tr, tile("ic_savings", t("savings"), () -> push(new SavingsPage())), dp(96), WRAP, 0, 0, 8, 0);
        put(tr, tile("ic_pie_chart", t("statistics"), () -> push(new StatsPage())), dp(96), WRAP, 0, 0, 8, 0);
        put(tr, tile("ic_calendar_month", t("forecast"), () -> push(new ForecastPage())), dp(96), WRAP, 0, 0, 0, 0);
        hs.addView(tr);
        put(c, hs, 0, 14);

        budgetSection(c);

        ArrayList<DuePayment> overD = new ArrayList<>(), upD = new ArrayList<>();
        long horizon = (mode == 0 && off >= 0) ? p.e : now() + 30 * DAY;
        for (DuePayment d : dues(horizon)) { if (d.when <= now()) overD.add(d); else upD.add(d); }
        if (!overD.isEmpty()) {
            TextView st = section(t("overdue") + " (" + digits(String.valueOf(overD.size())) + ")");
            st.setTextColor(expenseColor);
            put(c, st, 8, 6);
            for (int i = 0; i < Math.min(3, overD.size()); i++) put(c, dueCard(overD.get(i), true, false), 0, 8);
        }
        if (!upD.isEmpty()) {
            put(c, section(t("upcoming") + " (" + digits(String.valueOf(upD.size())) + ")"), 8, 6);
            for (int i = 0; i < Math.min(3, upD.size()); i++) put(c, dueCard(upD.get(i), false, false), 0, 8);
        }
        if (overD.size() > 3 || upD.size() > 3)
            put(c, btn(t("see_all_planned"), secondarySurfaceColor, textColor, () -> push(new PlannedPage())), 4, 8);

        addJourney(c);

        put(c, tv(t("history"), 18, textColor, 3), 12, 4);
        histList(c, history(p.s, p.e, null, null), limit, () -> { limit += 30; render(); });
    }

    void addJourney(LinearLayout c) {
        String dismissed = "," + sharedPreferences.getString("dismissed", "") + ",";
        ArrayList<String[]> cards = new ArrayList<>();
        int n = database.transactions.size();
        if (n == 0) cards.add(new String[]{"j_first", "ic_add"});
        if (n >= 1 && database.plannedPayments.isEmpty()) cards.add(new String[]{"j_planned", "ic_repeat"});
        if (n >= 3 && database.budgets.isEmpty()) cards.add(new String[]{"j_budget", "ic_paid"});
        if (n >= 3 && database.savings.isEmpty()) cards.add(new String[]{"j_savings", "ic_savings"});
        if (n >= 7) cards.add(new String[]{"j_stats", "ic_pie_chart"});
        int shown = 0;
        for (final String[] cd : cards) {
            if (shown >= 2 || dismissed.contains("," + cd[0] + ",")) continue;
            LinearLayout w = card();
            w.setPadding(dp(14), dp(12), dp(6), dp(12));
            LinearLayout r = box(true);
            r.setGravity(Gravity.CENTER_VERTICAL);
            put(r, circ(cd[1], 40, accentColor), dp(40), dp(40), 0, 0, 12, 0);
            putW(r, tv(t(cd[0]), 13, textColor, 1), 1f, 0, 4);
            r.addView(actIcon("ic_close", () -> {
                sharedPreferences.edit().putString("dismissed", sharedPreferences.getString("dismissed", "") + "," + cd[0]).apply();
                render();
            }));
            w.addView(r);
            click(w, () -> {
                if ("j_first".equals(cd[0])) addDialog();
                else if ("j_planned".equals(cd[0])) push(new PlannedPage());
                else if ("j_budget".equals(cd[0])) push(new BudgetsPage());
                else if ("j_savings".equals(cd[0])) push(new SavingsPage());
                else {
                    sharedPreferences.edit().putString("dismissed", sharedPreferences.getString("dismissed", "") + "," + cd[0]).apply();
                    push(new StatsPage());
                }
            });
            put(c, w, 0, 8);
            shown++;
        }
    }
}
