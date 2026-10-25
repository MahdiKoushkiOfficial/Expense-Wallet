package com.mahdi.expensewallet.ui.pages;

import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import static com.mahdi.expensewallet.data.FinanceManager.goalTotal;
import static com.mahdi.expensewallet.data.FinanceManager.totalSaved;
import static com.mahdi.expensewallet.data.SettingsManager.base;
import static com.mahdi.expensewallet.data.SettingsManager.hide;
import static com.mahdi.expensewallet.ui.Navigator.push;
import static com.mahdi.expensewallet.ui.Theme.accentColor;
import static com.mahdi.expensewallet.ui.Theme.secondaryTextColor;
import static com.mahdi.expensewallet.ui.Theme.surfaceColor;
import static com.mahdi.expensewallet.ui.Theme.textColor;
import static com.mahdi.expensewallet.ui.UiHelper.amountText;
import static com.mahdi.expensewallet.ui.UiHelper.amtCol;
import static com.mahdi.expensewallet.ui.UiHelper.box;
import static com.mahdi.expensewallet.ui.UiHelper.circ;
import static com.mahdi.expensewallet.ui.UiHelper.click;
import static com.mahdi.expensewallet.ui.UiHelper.dp;
import static com.mahdi.expensewallet.ui.UiHelper.header;
import static com.mahdi.expensewallet.ui.UiHelper.put;
import static com.mahdi.expensewallet.ui.UiHelper.putW;
import static com.mahdi.expensewallet.ui.UiHelper.shape;
import static com.mahdi.expensewallet.ui.UiHelper.tv;
import static com.mahdi.expensewallet.utils.Constants.GOALS;
import static com.mahdi.expensewallet.utils.Constants.GOAL_COLORS;
import static com.mahdi.expensewallet.utils.Constants.GOAL_ICONS;
import static com.mahdi.expensewallet.utils.CurrencyUtils.exch;
import static com.mahdi.expensewallet.utils.CurrencyUtils.money;
import static com.mahdi.expensewallet.utils.FormatUtils.hid;
import static com.mahdi.expensewallet.utils.Translator.t;

public class SavingsPage extends Page {
    @Override public void build(LinearLayout c) {
        header(c, t("savings"), true);
        LinearLayout tc = box(false);
        tc.setBackground(shape(accentColor, 24));
        tc.setPadding(dp(20), dp(18), dp(20), dp(18));
        tc.addView(tv(t("total_saved"), 13, 0xCCFFFFFF, 1));
        TextView big = tv("", 30, Color.WHITE, 3);
        amountText(big, hid(money(totalSaved(), base)), 30);
        tc.addView(big);
        String tsx = hide ? "" : exch(totalSaved(), base);
        if (!tsx.isEmpty()) {
            TextView ev = tv(tsx, 13, 0xCCFFFFFF, 1);
            ev.setTextDirection(View.TEXT_DIRECTION_LTR);
            tc.addView(ev);
        }
        put(c, tc, 0, 8);
        put(c, tv(t("savings_intro"), 12, secondaryTextColor, 0), 0, 12);
        for (int i = 0; i < GOALS.length; i++) {
            final String g = GOALS[i];
            LinearLayout r = box(true);
            r.setGravity(Gravity.CENTER_VERTICAL);
            r.setBackground(shape(surfaceColor, 18));
            r.setPadding(dp(14), dp(14), dp(16), dp(14));
            put(r, circ(GOAL_ICONS[i], 46, GOAL_COLORS[i]), dp(46), dp(46), 0, 0, 12, 0);
            putW(r, tv(t("goal_" + g), 16, textColor, 2), 1f, 0, 8);
            r.addView(amtCol(hid(money(goalTotal(g), base)), textColor, 15, goalTotal(g), base, hide));
            click(r, () -> push(new GoalPage(g)));
            put(c, r, 0, 10);
        }
    }
}
