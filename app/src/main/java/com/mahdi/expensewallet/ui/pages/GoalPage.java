package com.mahdi.expensewallet.ui.pages;

import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mahdi.expensewallet.model.Saving;

import java.util.ArrayList;
import java.util.Arrays;

import static com.mahdi.expensewallet.data.DatabaseStorage.database;
import static com.mahdi.expensewallet.data.DatabaseStorage.saveDb;
import static com.mahdi.expensewallet.data.FinanceManager.accCur;
import static com.mahdi.expensewallet.data.FinanceManager.accName;
import static com.mahdi.expensewallet.data.FinanceManager.goalTotal;
import static com.mahdi.expensewallet.data.SettingsManager.base;
import static com.mahdi.expensewallet.data.SettingsManager.hide;
import static com.mahdi.expensewallet.ui.Dialogs.confirm;
import static com.mahdi.expensewallet.ui.Navigator.push;
import static com.mahdi.expensewallet.ui.Navigator.render;
import static com.mahdi.expensewallet.ui.Theme.accentColor;
import static com.mahdi.expensewallet.ui.Theme.secondaryTextColor;
import static com.mahdi.expensewallet.ui.Theme.surfaceColor;
import static com.mahdi.expensewallet.ui.Theme.textColor;
import static com.mahdi.expensewallet.ui.UiHelper.actIcon;
import static com.mahdi.expensewallet.ui.UiHelper.amountText;
import static com.mahdi.expensewallet.ui.UiHelper.amtCol;
import static com.mahdi.expensewallet.ui.UiHelper.box;
import static com.mahdi.expensewallet.ui.UiHelper.btn;
import static com.mahdi.expensewallet.ui.UiHelper.circ;
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
import static com.mahdi.expensewallet.utils.FormatUtils.dateStr;
import static com.mahdi.expensewallet.utils.FormatUtils.hid;
import static com.mahdi.expensewallet.utils.Translator.t;

public class GoalPage extends Page {
    final String g;

    public GoalPage(String g) { this.g = g; }

    @Override public void build(LinearLayout c) {
        int gi = Math.max(0, Arrays.asList(GOALS).indexOf(g));
        header(c, t("goal_" + g), true);
        LinearLayout top = box(true);
        top.setGravity(Gravity.CENTER_VERTICAL);
        put(top, circ(GOAL_ICONS[gi], 56, GOAL_COLORS[gi]), dp(56), dp(56), 0, 0, 14, 0);
        LinearLayout col = box(false);
        col.addView(tv(t("saved_so_far"), 12, secondaryTextColor, 1));
        TextView a = tv("", 24, textColor, 3);
        amountText(a, hid(money(goalTotal(g), base)), 24);
        col.addView(a);
        String gx = hide ? "" : exch(goalTotal(g), base);
        if (!gx.isEmpty()) {
            TextView ev = tv(gx, 12, secondaryTextColor, 1);
            ev.setTextDirection(View.TEXT_DIRECTION_LTR);
            col.addView(ev);
        }
        putW(top, col, 1f, 0, 0);
        put(c, top, 0, 14);
        put(c, btn(t("add_to_savings"), accentColor, Color.WHITE, () -> push(new SavingFormPage(g))), 0, 12);
        ArrayList<Saving> l = new ArrayList<>();
        for (Saving s : database.savings) if (s.goal.equals(g)) l.add(s);
        l.sort((x, y) -> Long.compare(y.time, x.time));
        if (l.isEmpty()) {
            TextView e = tv(t("no_savings_yet"), 14, secondaryTextColor, 0);
            e.setGravity(Gravity.CENTER);
            put(c, e, 16, 16);
        }
        for (final Saving s : l) {
            LinearLayout r = box(true);
            r.setGravity(Gravity.CENTER_VERTICAL);
            r.setBackground(shape(surfaceColor, 16));
            r.setPadding(dp(14), dp(10), dp(6), dp(10));
            LinearLayout cl = box(false);
            cl.addView(tv(dateStr(s.time), 14, textColor, 2));
            cl.addView(tv(accName(s.accountId), 12, secondaryTextColor, 0));
            putW(r, cl, 1f, 0, 8);
            r.addView(amtCol("−" + money(s.amount, accCur(s.accountId)), textColor, 15, s.amount, accCur(s.accountId), false));
            r.addView(actIcon("ic_delete", () -> confirm(t("delete"), t("delete_saving_q"), t("delete"), () -> {
                database.savings.remove(s);
                saveDb();
                render();
            })));
            put(c, r, 0, 8);
        }
    }
}
