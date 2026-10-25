package com.mahdi.expensewallet.ui.pages;

import android.widget.EditText;
import android.widget.LinearLayout;

import com.mahdi.expensewallet.ui.Callbacks.ExUpd;

import java.util.HashSet;

import static com.mahdi.expensewallet.data.DatabaseStorage.database;
import static com.mahdi.expensewallet.data.DatabaseStorage.saveDb;
import static com.mahdi.expensewallet.data.FinanceManager.catSet;
import static com.mahdi.expensewallet.data.SettingsManager.base;
import static com.mahdi.expensewallet.ui.Navigator.pop;
import static com.mahdi.expensewallet.ui.Navigator.push;
import static com.mahdi.expensewallet.ui.Navigator.render;
import static com.mahdi.expensewallet.ui.Theme.accentColor;
import static com.mahdi.expensewallet.ui.Theme.secondaryTextColor;
import static com.mahdi.expensewallet.ui.UiHelper.NUM;
import static com.mahdi.expensewallet.ui.UiHelper.actIcon;
import static com.mahdi.expensewallet.ui.UiHelper.amountCard;
import static com.mahdi.expensewallet.ui.UiHelper.catCard;
import static com.mahdi.expensewallet.ui.UiHelper.edit;
import static com.mahdi.expensewallet.ui.UiHelper.exLine;
import static com.mahdi.expensewallet.ui.UiHelper.header;
import static com.mahdi.expensewallet.ui.UiHelper.put;
import static com.mahdi.expensewallet.ui.UiHelper.saveBtn;
import static com.mahdi.expensewallet.ui.UiHelper.section;
import static com.mahdi.expensewallet.ui.UiHelper.seg;
import static com.mahdi.expensewallet.ui.UiHelper.toast;
import static com.mahdi.expensewallet.ui.UiHelper.tv;
import static com.mahdi.expensewallet.utils.CurrencyUtils.parseAmt;
import static com.mahdi.expensewallet.utils.CurrencyUtils.sym;
import static com.mahdi.expensewallet.utils.Translator.t;

public class BudgetFormPage extends Page {
    String cat, lim = "";
    int per = 2;

    public BudgetFormPage(String cat) { this.cat = cat; }

    @Override public void build(LinearLayout c) {
        header(c, t("new_budget"), true, actIcon("ic_check", this::save));
        c.addView(section(t("category")));
        put(c, catCard(cat, () -> push(new CategoryPickPage(catSet(0), cat, new HashSet<>(database.budgets.keySet()), k -> cat = k))), 6, 12);
        c.addView(section(t("period")));
        put(c, seg(new String[]{t("bp_0"), t("bp_1"), t("bp_2")}, per, new int[]{accentColor}, i -> { per = i; render(); }), 6, 12);
        c.addView(section(t("budget_limit", sym(base))));
        final ExUpd[] eu = new ExUpd[1];
        EditText le = edit("0", lim, NUM, txt -> { lim = txt; if (eu[0] != null) eu[0].upd(txt); });
        LinearLayout lc = amountCard(le, sym(base));
        eu[0] = exLine(lc, base);
        eu[0].upd(lim);
        put(c, lc, 6, 10);
        put(c, tv(t("budget_hint"), 12, secondaryTextColor, 0), 0, 16);
        put(c, saveBtn(this::save), 0, 0);
    }

    void save() {
        double v = parseAmt(lim);
        if (v <= 0) { toast(t("err_amount")); return; }
        if (database.budgets.containsKey(cat)) { toast(t("budget_exists")); return; }
        database.budgets.put(cat, new double[]{per, v});
        saveDb();
        pop();
    }
}
