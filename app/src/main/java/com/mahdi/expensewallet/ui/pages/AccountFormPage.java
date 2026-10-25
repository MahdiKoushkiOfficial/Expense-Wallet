package com.mahdi.expensewallet.ui.pages;

import android.graphics.Color;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mahdi.expensewallet.model.Account;

import static com.mahdi.expensewallet.data.DatabaseStorage.database;
import static com.mahdi.expensewallet.data.DatabaseStorage.saveDb;
import static com.mahdi.expensewallet.ui.Navigator.pop;
import static com.mahdi.expensewallet.ui.Navigator.render;
import static com.mahdi.expensewallet.ui.Theme.secondarySurfaceColor;
import static com.mahdi.expensewallet.ui.Theme.secondaryTextColor;
import static com.mahdi.expensewallet.ui.Theme.textColor;
import static com.mahdi.expensewallet.ui.UiHelper.NUMS;
import static com.mahdi.expensewallet.ui.UiHelper.TXT;
import static com.mahdi.expensewallet.ui.UiHelper.box;
import static com.mahdi.expensewallet.ui.UiHelper.click;
import static com.mahdi.expensewallet.ui.UiHelper.dp;
import static com.mahdi.expensewallet.ui.UiHelper.edit;
import static com.mahdi.expensewallet.ui.UiHelper.header;
import static com.mahdi.expensewallet.ui.UiHelper.ico;
import static com.mahdi.expensewallet.ui.UiHelper.put;
import static com.mahdi.expensewallet.ui.UiHelper.putW;
import static com.mahdi.expensewallet.ui.UiHelper.saveBtn;
import static com.mahdi.expensewallet.ui.UiHelper.section;
import static com.mahdi.expensewallet.ui.UiHelper.shape;
import static com.mahdi.expensewallet.ui.UiHelper.tv;
import static com.mahdi.expensewallet.utils.Constants.ACC_COLORS;
import static com.mahdi.expensewallet.utils.Constants.ACC_ICONS;
import static com.mahdi.expensewallet.utils.Constants.ACC_KEYS;
import static com.mahdi.expensewallet.utils.Constants.WRAP;
import static com.mahdi.expensewallet.utils.CurrencyUtils.parseAmt;
import static com.mahdi.expensewallet.utils.Translator.t;

public class AccountFormPage extends Page {
    int type = 1;
    String name = null, bal = "";
    final Runnable after;

    public AccountFormPage(Runnable after) { this.after = after; }

    @Override public void build(LinearLayout c) {
        header(c, t("new_account"), true);
        if (name == null) name = t(ACC_KEYS[type]);
        c.addView(section(t("account_type")));
        LinearLayout tr = box(true);
        for (int i = 0; i < 3; i++) {
            final int k = i;
            LinearLayout cell = box(false);
            cell.setGravity(Gravity.CENTER_HORIZONTAL);
            cell.setPadding(dp(6), dp(14), dp(6), dp(14));
            cell.setBackground(shape(type == k ? ACC_COLORS[k] : secondarySurfaceColor, 16));
            cell.addView(ico(ACC_ICONS[k], 30, type == k ? Color.WHITE : secondaryTextColor));
            TextView l = tv(t(ACC_KEYS[k]), 12, type == k ? Color.WHITE : textColor, 2);
            l.setGravity(Gravity.CENTER);
            put(cell, l, WRAP, WRAP, 0, 6, 0, 0);
            click(cell, () -> {
                boolean def = name.equals(t(ACC_KEYS[type]));
                type = k;
                if (def) name = t(ACC_KEYS[type]);
                render();
            });
            putW(tr, cell, 1f, 3, 3);
        }
        put(c, tr, 6, 14);
        c.addView(section(t("account_name")));
        put(c, edit(t("account_name"), name, TXT, s -> name = s), 6, 14);
        c.addView(section(t("initial_balance")));
        put(c, edit("0", bal, NUMS, s -> bal = s), 6, 20);
        put(c, saveBtn(() -> {
            Account a = new Account();
            a.type = type;
            a.name = name.trim().isEmpty() ? t(ACC_KEYS[type]) : name.trim();
            a.initial = parseAmt(bal);
            database.accounts.add(a);
            saveDb();
            pop();
            if (after != null) after.run();
        }), 0, 0);
    }
}
