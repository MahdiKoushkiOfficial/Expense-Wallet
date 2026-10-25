package com.mahdi.expensewallet.ui.pages;

import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mahdi.expensewallet.model.DateRange;
import com.mahdi.expensewallet.model.DuePayment;
import com.mahdi.expensewallet.model.PlannedPayment;
import com.mahdi.expensewallet.model.Transaction;

import java.util.ArrayList;

import static com.mahdi.expensewallet.data.DatabaseStorage.database;
import static com.mahdi.expensewallet.data.ExchangeRateManager.ratesMissing;
import static com.mahdi.expensewallet.data.ExchangeRateManager.toBase;
import static com.mahdi.expensewallet.data.FinanceManager.accCur;
import static com.mahdi.expensewallet.data.FinanceManager.catColor;
import static com.mahdi.expensewallet.data.FinanceManager.catIcon;
import static com.mahdi.expensewallet.data.FinanceManager.catName;
import static com.mahdi.expensewallet.data.FinanceManager.nextOcc;
import static com.mahdi.expensewallet.data.FinanceManager.totalBalance;
import static com.mahdi.expensewallet.data.SettingsManager.base;
import static com.mahdi.expensewallet.data.SettingsManager.buffer;
import static com.mahdi.expensewallet.ui.Navigator.render;
import static com.mahdi.expensewallet.ui.Theme.expenseColor;
import static com.mahdi.expensewallet.ui.Theme.incomeColor;
import static com.mahdi.expensewallet.ui.Theme.secondaryTextColor;
import static com.mahdi.expensewallet.ui.Theme.surfaceColor;
import static com.mahdi.expensewallet.ui.Theme.textColor;
import static com.mahdi.expensewallet.ui.Theme.warningColor;
import static com.mahdi.expensewallet.ui.UiHelper.amountText;
import static com.mahdi.expensewallet.ui.UiHelper.box;
import static com.mahdi.expensewallet.ui.UiHelper.card;
import static com.mahdi.expensewallet.ui.UiHelper.circ;
import static com.mahdi.expensewallet.ui.UiHelper.dp;
import static com.mahdi.expensewallet.ui.UiHelper.header;
import static com.mahdi.expensewallet.ui.UiHelper.periodNav;
import static com.mahdi.expensewallet.ui.UiHelper.put;
import static com.mahdi.expensewallet.ui.UiHelper.putW;
import static com.mahdi.expensewallet.ui.UiHelper.section;
import static com.mahdi.expensewallet.ui.UiHelper.sep;
import static com.mahdi.expensewallet.ui.UiHelper.shape;
import static com.mahdi.expensewallet.ui.UiHelper.tv;
import static com.mahdi.expensewallet.utils.Constants.MATCH;
import static com.mahdi.expensewallet.utils.CurrencyUtils.money;
import static com.mahdi.expensewallet.utils.CurrencyUtils.signed;
import static com.mahdi.expensewallet.utils.FormatUtils.dateStr;
import static com.mahdi.expensewallet.utils.FormatUtils.hid;
import static com.mahdi.expensewallet.utils.FormatUtils.monthPeriod;
import static com.mahdi.expensewallet.utils.FormatUtils.now;
import static com.mahdi.expensewallet.utils.Translator.t;

public class ForecastPage extends Page {
    int off = 0;

    @Override public void build(LinearLayout c) {
        header(c, t("forecast"), true);
        final DateRange p = monthPeriod(off);
        put(c, periodNav(p.label, () -> { if (off > 0) { off--; render(); } }, () -> { off++; render(); }, null), 0, 8);
        ArrayList<DuePayment> items = new ArrayList<>();
        for (Transaction x : database.transactions) {
            if (x.settled || x.dueDate > p.e) continue;
            DuePayment duePayment = new DuePayment();
            duePayment.t = x; duePayment.when = x.dueDate; duePayment.type = x.type; duePayment.category = x.category; duePayment.accountId = x.accountId; duePayment.amount = x.amount;
            duePayment.title = x.title.isEmpty() ? catName(x.category) : x.title;
            items.add(duePayment);
        }
        for (PlannedPayment r : database.plannedPayments) {
            long when = r.next;
            for (int i = 0; i < 400 && when <= p.e; i++) {
                DuePayment duePayment = new DuePayment();
                duePayment.r = r; duePayment.when = when; duePayment.type = r.type; duePayment.category = r.category; duePayment.accountId = r.accountId; duePayment.amount = r.amount;
                duePayment.title = r.title.isEmpty() ? catName(r.category) : r.title;
                items.add(duePayment);
                if (r.unit == 0) break;
                when = nextOcc(r, when);
            }
        }
        items.sort((a, b) -> Long.compare(a.when, b.when));
        double pin = 0, pout = 0;
        for (DuePayment d : items) {
            double v = toBase(d.amount, accCur(d.accountId));
            if (d.type == 1) pin += v; else pout += v;
        }
        double bal = totalBalance();
        double after = bal + pin - pout;
        LinearLayout bc = card();
        bc.addView(forecastLine(t("current_balance"), money(bal, base), textColor));
        bc.addView(forecastLine(t("planned_income"), "+" + money(pin, base), incomeColor));
        bc.addView(forecastLine(t("planned_expenses"), "−" + money(pout, base), expenseColor));
        View sp1 = sep();
        put(bc, sp1, MATCH, 1, 0, 10, 0, 10);
        bc.addView(forecastLine(t("balance_after"), money(after, base), after < 0 ? expenseColor : textColor));
        if (buffer > 0) {
            double diff = after - buffer;
            put(bc, tv(t(diff >= 0 ? "buffer_above" : "buffer_below", money(Math.abs(diff), base)), 13, diff >= 0 ? incomeColor : expenseColor, 1), 8, 0);
        }
        put(c, bc, 0, 12);
        if (ratesMissing) put(c, tv(t("rates_missing"), 12, warningColor, 1), 0, 10);
        put(c, section(t("planned_items")), 4, 6);
        if (items.isEmpty()) {
            TextView e = tv(t("nothing_planned"), 14, secondaryTextColor, 0);
            e.setGravity(Gravity.CENTER);
            put(c, e, 16, 16);
        }
        for (int i = 0; i < Math.min(60, items.size()); i++) {
            DuePayment duePayment = items.get(i);
            LinearLayout r = box(true);
            r.setGravity(Gravity.CENTER_VERTICAL);
            r.setBackground(shape(surfaceColor, 16));
            r.setPadding(dp(12), dp(10), dp(14), dp(10));
            put(r, circ(catIcon(duePayment.category), 40, catColor(duePayment.category)), dp(40), dp(40), 0, 0, 12, 0);
            LinearLayout col = box(false);
            col.addView(tv(duePayment.title, 14, textColor, 2));
            col.addView(tv(dateStr(duePayment.when) + (duePayment.when <= now() ? " · " + t("overdue") : ""), 12, duePayment.when <= now() ? expenseColor : secondaryTextColor, 0));
            putW(r, col, 1f, 0, 8);
            TextView a = tv("", 14, duePayment.type == 1 ? incomeColor : expenseColor, 2);
            amountText(a, signed(duePayment.amount, accCur(duePayment.accountId), duePayment.type), 14);
            r.addView(a);
            put(c, r, 0, 8);
        }
    }

    View forecastLine(String label, String val, int color) {
        LinearLayout r = box(true);
        r.setPadding(0, dp(4), 0, dp(4));
        putW(r, tv(label, 14, secondaryTextColor, 1), 1f, 0, 8);
        TextView v = tv("", 15, color, 2);
        amountText(v, hid(val), 15);
        r.addView(v);
        return r;
    }
}
