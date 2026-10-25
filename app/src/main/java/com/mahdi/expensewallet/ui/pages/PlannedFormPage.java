package com.mahdi.expensewallet.ui.pages;

import android.graphics.Color;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.mahdi.expensewallet.model.PlannedPayment;
import com.mahdi.expensewallet.ui.Callbacks.ExUpd;
import com.mahdi.expensewallet.utils.CalendarUtils;

import java.util.Arrays;

import static com.mahdi.expensewallet.data.DatabaseStorage.database;
import static com.mahdi.expensewallet.data.DatabaseStorage.saveDb;
import static com.mahdi.expensewallet.data.FinanceManager.acc;
import static com.mahdi.expensewallet.data.FinanceManager.accCur;
import static com.mahdi.expensewallet.data.FinanceManager.catSet;
import static com.mahdi.expensewallet.ui.Dialogs.confirm;
import static com.mahdi.expensewallet.ui.Dialogs.pickDate;
import static com.mahdi.expensewallet.ui.Navigator.pop;
import static com.mahdi.expensewallet.ui.Navigator.push;
import static com.mahdi.expensewallet.ui.Navigator.render;
import static com.mahdi.expensewallet.ui.Theme.accentColor;
import static com.mahdi.expensewallet.ui.Theme.expenseColor;
import static com.mahdi.expensewallet.ui.Theme.incomeColor;
import static com.mahdi.expensewallet.ui.Theme.secondaryTextColor;
import static com.mahdi.expensewallet.ui.UiHelper.NUM;
import static com.mahdi.expensewallet.ui.UiHelper.TXT;
import static com.mahdi.expensewallet.ui.UiHelper.accountChips;
import static com.mahdi.expensewallet.ui.UiHelper.actIcon;
import static com.mahdi.expensewallet.ui.UiHelper.amountCard;
import static com.mahdi.expensewallet.ui.UiHelper.box;
import static com.mahdi.expensewallet.ui.UiHelper.btn;
import static com.mahdi.expensewallet.ui.UiHelper.card;
import static com.mahdi.expensewallet.ui.UiHelper.catCard;
import static com.mahdi.expensewallet.ui.UiHelper.chips;
import static com.mahdi.expensewallet.ui.UiHelper.dateRow;
import static com.mahdi.expensewallet.ui.UiHelper.dp;
import static com.mahdi.expensewallet.ui.UiHelper.edit;
import static com.mahdi.expensewallet.ui.UiHelper.exLine;
import static com.mahdi.expensewallet.ui.UiHelper.header;
import static com.mahdi.expensewallet.ui.UiHelper.put;
import static com.mahdi.expensewallet.ui.UiHelper.putW;
import static com.mahdi.expensewallet.ui.UiHelper.saveBtn;
import static com.mahdi.expensewallet.ui.UiHelper.section;
import static com.mahdi.expensewallet.ui.UiHelper.seg;
import static com.mahdi.expensewallet.ui.UiHelper.toast;
import static com.mahdi.expensewallet.ui.UiHelper.tv;
import static com.mahdi.expensewallet.utils.Constants.ICON_EXPENSE;
import static com.mahdi.expensewallet.utils.Constants.ICON_INCOME;
import static com.mahdi.expensewallet.utils.Constants.WRAP;
import static com.mahdi.expensewallet.utils.CurrencyUtils.parseAmt;
import static com.mahdi.expensewallet.utils.CurrencyUtils.sym;
import static com.mahdi.expensewallet.utils.FormatUtils.dateStr;
import static com.mahdi.expensewallet.utils.FormatUtils.now;
import static com.mahdi.expensewallet.utils.Translator.t;

public class PlannedFormPage extends Page {
    final PlannedPayment e;
    int type = 0, unit = 0;
    String amt = "", title = "", desc = "", cat = "other", accId = "", every = "1";
    long next;

    public PlannedFormPage(PlannedPayment edit) {
        e = edit;
        if (!database.accounts.isEmpty()) accId = database.accounts.get(0).id;
        next = CalendarUtils.dayStart(CalendarUtils.addDays(now(), 1));
        if (edit != null) {
            type = edit.type; unit = edit.unit; every = String.valueOf(edit.n); cat = edit.category; accId = edit.accountId;
            title = edit.title; desc = edit.description; next = edit.next;
            amt = edit.amount == Math.floor(edit.amount) ? String.valueOf((long) edit.amount) : String.valueOf(edit.amount);
        }
    }

    @Override public void build(LinearLayout c) {
        View ok = actIcon("ic_check", this::save);
        if (e != null) {
            header(c, t("edit_planned"), true, actIcon("ic_delete", () -> confirm(t("delete"), t("delete_planned_q"), t("delete"), () -> {
                database.plannedPayments.remove(e);
                saveDb();
                pop();
            })), ok);
        } else header(c, t("new_planned"), true, ok);
        put(c, seg(new String[]{t("expense"), t("income")}, type, new int[]{expenseColor, incomeColor}, new String[]{ICON_EXPENSE, ICON_INCOME}, i -> {
            type = i;
            if (!Arrays.asList(catSet(type)).contains(cat)) cat = "other";
            render();
        }), 0, 12);
        final ExUpd[] eu = new ExUpd[1];
        EditText ae = edit("0", amt, NUM, txt -> { amt = txt; if (eu[0] != null) eu[0].upd(txt); });
        LinearLayout ac = amountCard(ae, sym(accCur(accId)));
        eu[0] = exLine(ac, accCur(accId));
        eu[0].upd(amt);
        put(c, ac, 0, 12);
        c.addView(section(t("account")));
        if (database.accounts.isEmpty())
            put(c, btn(t("add_account"), accentColor, Color.WHITE, () -> push(new AccountFormPage(null))), 6, 12);
        else
            put(c, accountChips(accId, id -> { accId = id; render(); }), 6, 12);
        c.addView(section(t("category")));
        put(c, catCard(cat, () -> push(new CategoryPickPage(catSet(type), cat, null, k -> cat = k))), 6, 12);
        LinearLayout dc = card();
        dc.addView(edit(t("title_hint"), title, TXT, txt -> title = txt));
        put(dc, edit(t("desc_hint"), desc, TXT, txt -> desc = txt), 10, 0);
        put(c, dc, 0, 12);
        LinearLayout rc = card();
        rc.addView(dateRow(t("first_due"), dateStr(next), () -> pickDate(next, l -> { next = l; render(); })));
        rc.addView(tv(t("repeat"), 13, secondaryTextColor, 1));
        put(rc, chips(new String[]{t("rep_once"), t("rep_day"), t("rep_week"), t("rep_month"), t("rep_year")}, unit, i -> { unit = i; render(); }), 6, 0);
        if (unit > 0) {
            LinearLayout er = box(true);
            er.setGravity(Gravity.CENTER_VERTICAL);
            putW(er, tv(t("every_n"), 14, secondaryTextColor, 1), 1f, 0, 8);
            put(er, edit("1", every, InputType.TYPE_CLASS_NUMBER, txt -> every = txt), dp(90), WRAP, 0, 0, 0, 0);
            put(rc, er, 10, 0);
        }
        put(c, rc, 0, 16);
        put(c, saveBtn(this::save), 0, 0);
    }

    void save() {
        double a = parseAmt(amt);
        if (a <= 0) { toast(t("err_amount")); return; }
        if (acc(accId) == null) { toast(t("err_account")); return; }
        PlannedPayment payment = e == null ? new PlannedPayment() : e;
        payment.type = type; payment.accountId = accId; payment.category = cat; payment.amount = a; payment.title = title.trim(); payment.description = desc.trim();
        payment.unit = unit; payment.n = Math.max(1, (int) parseAmt(every)); payment.next = CalendarUtils.dayStart(next);
        if (e == null) database.plannedPayments.add(payment);
        saveDb();
        pop();
    }
}
