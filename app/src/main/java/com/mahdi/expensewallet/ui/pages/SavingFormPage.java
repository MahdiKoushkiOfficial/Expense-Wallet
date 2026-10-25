package com.mahdi.expensewallet.ui.pages;

import android.widget.EditText;
import android.widget.LinearLayout;

import com.mahdi.expensewallet.model.Saving;
import com.mahdi.expensewallet.ui.Callbacks.ExUpd;

import static com.mahdi.expensewallet.data.DatabaseStorage.database;
import static com.mahdi.expensewallet.data.DatabaseStorage.saveDb;
import static com.mahdi.expensewallet.data.FinanceManager.acc;
import static com.mahdi.expensewallet.data.FinanceManager.accCur;
import static com.mahdi.expensewallet.ui.Dialogs.pickDate;
import static com.mahdi.expensewallet.ui.Navigator.pop;
import static com.mahdi.expensewallet.ui.Navigator.render;
import static com.mahdi.expensewallet.ui.Theme.secondaryTextColor;
import static com.mahdi.expensewallet.ui.UiHelper.NUM;
import static com.mahdi.expensewallet.ui.UiHelper.accountChips;
import static com.mahdi.expensewallet.ui.UiHelper.actIcon;
import static com.mahdi.expensewallet.ui.UiHelper.amountCard;
import static com.mahdi.expensewallet.ui.UiHelper.card;
import static com.mahdi.expensewallet.ui.UiHelper.dateRow;
import static com.mahdi.expensewallet.ui.UiHelper.edit;
import static com.mahdi.expensewallet.ui.UiHelper.exLine;
import static com.mahdi.expensewallet.ui.UiHelper.header;
import static com.mahdi.expensewallet.ui.UiHelper.put;
import static com.mahdi.expensewallet.ui.UiHelper.saveBtn;
import static com.mahdi.expensewallet.ui.UiHelper.section;
import static com.mahdi.expensewallet.ui.UiHelper.toast;
import static com.mahdi.expensewallet.ui.UiHelper.tv;
import static com.mahdi.expensewallet.utils.CurrencyUtils.parseAmt;
import static com.mahdi.expensewallet.utils.CurrencyUtils.sym;
import static com.mahdi.expensewallet.utils.FormatUtils.dateStr;
import static com.mahdi.expensewallet.utils.FormatUtils.now;
import static com.mahdi.expensewallet.utils.Translator.t;

public class SavingFormPage extends Page {
    final String g;
    String amt = "", accId = "";
    long time = now();

    public SavingFormPage(String g) {
        this.g = g;
        if (!database.accounts.isEmpty()) accId = database.accounts.get(0).id;
    }

    @Override public void build(LinearLayout c) {
        header(c, t("add_to_savings"), true, actIcon("ic_check", this::save));
        put(c, tv(t("saving_note"), 12, secondaryTextColor, 0), 0, 12);
        final ExUpd[] eu = new ExUpd[1];
        EditText ae = edit("0", amt, NUM, txt -> { amt = txt; if (eu[0] != null) eu[0].upd(txt); });
        LinearLayout ac = amountCard(ae, sym(accCur(accId)));
        eu[0] = exLine(ac, accCur(accId));
        eu[0].upd(amt);
        put(c, ac, 0, 12);
        c.addView(section(t("from_account")));
        if (!database.accounts.isEmpty()) put(c, accountChips(accId, id -> { accId = id; render(); }), 6, 12);
        LinearLayout dc = card();
        dc.addView(dateRow(t("date"), dateStr(time), () -> pickDate(time, l -> { time = l; render(); })));
        put(c, dc, 0, 16);
        put(c, saveBtn(this::save), 0, 0);
    }

    void save() {
        double a = parseAmt(amt);
        if (a <= 0) { toast(t("err_amount")); return; }
        if (acc(accId) == null) { toast(t("err_account")); return; }
        Saving saving = new Saving();
        saving.goal = g; saving.accountId = accId; saving.amount = a; saving.time = time;
        database.savings.add(saving);
        saveDb();
        pop();
    }
}
