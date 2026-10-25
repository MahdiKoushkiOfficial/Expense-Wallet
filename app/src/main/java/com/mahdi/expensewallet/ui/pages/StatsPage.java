package com.mahdi.expensewallet.ui.pages;

import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mahdi.expensewallet.model.DateRange;
import com.mahdi.expensewallet.model.Transaction;
import com.mahdi.expensewallet.ui.components.PieView;

import java.util.ArrayList;
import java.util.HashMap;

import static com.mahdi.expensewallet.MainActivity.activity;
import static com.mahdi.expensewallet.data.DatabaseStorage.database;
import static com.mahdi.expensewallet.data.ExchangeRateManager.toBase;
import static com.mahdi.expensewallet.data.FinanceManager.accCur;
import static com.mahdi.expensewallet.data.FinanceManager.catColor;
import static com.mahdi.expensewallet.data.FinanceManager.catIcon;
import static com.mahdi.expensewallet.data.FinanceManager.catName;
import static com.mahdi.expensewallet.data.SettingsManager.base;
import static com.mahdi.expensewallet.ui.Navigator.push;
import static com.mahdi.expensewallet.ui.Navigator.render;
import static com.mahdi.expensewallet.ui.Theme.expenseColor;
import static com.mahdi.expensewallet.ui.Theme.incomeColor;
import static com.mahdi.expensewallet.ui.Theme.secondaryTextColor;
import static com.mahdi.expensewallet.ui.Theme.surfaceColor;
import static com.mahdi.expensewallet.ui.Theme.textColor;
import static com.mahdi.expensewallet.ui.UiHelper.amountText;
import static com.mahdi.expensewallet.ui.UiHelper.box;
import static com.mahdi.expensewallet.ui.UiHelper.circ;
import static com.mahdi.expensewallet.ui.UiHelper.click;
import static com.mahdi.expensewallet.ui.UiHelper.dp;
import static com.mahdi.expensewallet.ui.UiHelper.header;
import static com.mahdi.expensewallet.ui.UiHelper.periodNav;
import static com.mahdi.expensewallet.ui.UiHelper.put;
import static com.mahdi.expensewallet.ui.UiHelper.putW;
import static com.mahdi.expensewallet.ui.UiHelper.seg;
import static com.mahdi.expensewallet.ui.UiHelper.shape;
import static com.mahdi.expensewallet.ui.UiHelper.tv;
import static com.mahdi.expensewallet.utils.Constants.ICON_EXPENSE;
import static com.mahdi.expensewallet.utils.Constants.ICON_INCOME;
import static com.mahdi.expensewallet.utils.Constants.MATCH;
import static com.mahdi.expensewallet.utils.CurrencyUtils.money;
import static com.mahdi.expensewallet.utils.FormatUtils.monthPeriod;
import static com.mahdi.expensewallet.utils.Translator.digits;
import static com.mahdi.expensewallet.utils.Translator.t;

public class StatsPage extends Page {
    int type = 0, off = 0;

    @Override public void build(LinearLayout c) {
        header(c, t("statistics"), true);
        put(c, seg(new String[]{t("expense"), t("income")}, type, new int[]{expenseColor, incomeColor}, new String[]{ICON_EXPENSE, ICON_INCOME}, i -> { type = i; render(); }), 0, 10);
        final DateRange p = monthPeriod(off);
        put(c, periodNav(p.label, () -> { off--; render(); }, () -> { off++; render(); }, null), 0, 8);
        final HashMap<String, Double> m = new HashMap<>();
        double total = 0;
        for (Transaction x : database.transactions) {
            if (!x.settled || x.type != type || x.time < p.s || x.time > p.e) continue;
            double v = toBase(x.amount, accCur(x.accountId));
            Double o = m.get(x.category);
            m.put(x.category, (o == null ? 0 : o) + v);
            total += v;
        }
        final ArrayList<String> keys = new ArrayList<>(m.keySet());
        keys.sort((a, b) -> Double.compare(m.get(b), m.get(a)));
        PieView pv = new PieView(activity);
        pv.v = new float[keys.size()];
        pv.col = new int[keys.size()];
        for (int i = 0; i < keys.size(); i++) { pv.v[i] = m.get(keys.get(i)).floatValue(); pv.col[i] = catColor(keys.get(i)); }
        pv.center = money(total, base);
        pv.label = t(type == 1 ? "income" : "expense");
        pv.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        put(c, pv, MATCH, dp(260), 0, 4, 0, 12);
        if (keys.isEmpty()) {
            TextView e = tv(t("no_data_period"), 14, secondaryTextColor, 0);
            e.setGravity(Gravity.CENTER);
            put(c, e, 6, 6);
        }
        for (final String k : keys) {
            double v = m.get(k);
            LinearLayout r = box(true);
            r.setGravity(Gravity.CENTER_VERTICAL);
            r.setBackground(shape(surfaceColor, 16));
            r.setPadding(dp(12), dp(10), dp(14), dp(10));
            put(r, circ(catIcon(k), 40, catColor(k)), dp(40), dp(40), 0, 0, 12, 0);
            LinearLayout col = box(false);
            col.addView(tv(catName(k), 15, textColor, 2));
            col.addView(tv(digits(String.valueOf(Math.round(v / total * 100))) + "%", 12, secondaryTextColor, 0));
            putW(r, col, 1f, 0, 8);
            TextView a = tv("", 14, textColor, 2);
            amountText(a, money(v, base), 14);
            r.addView(a);
            click(r, () -> push(new CategoryPage(k)));
            put(c, r, 0, 8);
        }
    }
}
