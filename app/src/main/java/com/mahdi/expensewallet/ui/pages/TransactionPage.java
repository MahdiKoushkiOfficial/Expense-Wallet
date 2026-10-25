package com.mahdi.expensewallet.ui.pages;

import android.graphics.Color;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mahdi.expensewallet.model.Transaction;
import com.mahdi.expensewallet.ui.components.M3Switch;
import com.mahdi.expensewallet.utils.CalendarUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import static com.mahdi.expensewallet.MainActivity.activity;
import static com.mahdi.expensewallet.data.DatabaseStorage.database;
import static com.mahdi.expensewallet.data.DatabaseStorage.saveDb;
import static com.mahdi.expensewallet.data.FinanceManager.acc;
import static com.mahdi.expensewallet.data.FinanceManager.accCur;
import static com.mahdi.expensewallet.data.FinanceManager.budgetWarning;
import static com.mahdi.expensewallet.data.FinanceManager.catSet;
import static com.mahdi.expensewallet.ui.Dialogs.confirm;
import static com.mahdi.expensewallet.ui.Dialogs.pickDate;
import static com.mahdi.expensewallet.ui.Dialogs.pickTime;
import static com.mahdi.expensewallet.ui.Navigator.pop;
import static com.mahdi.expensewallet.ui.Navigator.push;
import static com.mahdi.expensewallet.ui.Navigator.render;
import static com.mahdi.expensewallet.ui.Theme.accentColor;
import static com.mahdi.expensewallet.ui.Theme.expenseColor;
import static com.mahdi.expensewallet.ui.Theme.incomeColor;
import static com.mahdi.expensewallet.ui.Theme.secondaryTextColor;
import static com.mahdi.expensewallet.ui.Theme.textColor;
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
import static com.mahdi.expensewallet.ui.UiHelper.edit;
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
import static com.mahdi.expensewallet.utils.CurrencyUtils.exch;
import static com.mahdi.expensewallet.utils.CurrencyUtils.parseAmt;
import static com.mahdi.expensewallet.utils.CurrencyUtils.sym;
import static com.mahdi.expensewallet.utils.FormatUtils.dateStr;
import static com.mahdi.expensewallet.utils.FormatUtils.normTags;
import static com.mahdi.expensewallet.utils.FormatUtils.now;
import static com.mahdi.expensewallet.utils.FormatUtils.timeStr;
import static com.mahdi.expensewallet.utils.Translator.t;

public class TransactionPage extends Page {
    final Transaction e;
    int type;
    String amt = "", title = "", desc = "", tags = "", cat = "other", accId;
    long time = now(), due = CalendarUtils.dayStart(CalendarUtils.addDays(now(), 1));
    boolean settled = true;
    EditText tagEdit;
    TextView convView;

    public TransactionPage(Transaction edit, int type) {
        e = edit;
        this.type = type;
        accId = database.accounts.isEmpty() ? "" : database.accounts.get(0).id;
        long best = -1;
        for (Transaction x : database.transactions) if (x.time > best && acc(x.accountId) != null) { best = x.time; accId = x.accountId; }
        if (edit != null) {
            amt = edit.amount == Math.floor(edit.amount) ? String.valueOf((long) edit.amount) : String.valueOf(edit.amount);
            title = edit.title; desc = edit.description; tags = edit.tags; cat = edit.category; accId = edit.accountId;
            time = edit.time; settled = edit.settled;
            due = edit.dueDate > 0 ? edit.dueDate : CalendarUtils.dayStart(CalendarUtils.addDays(now(), 1));
        }
    }

    void updateConv() {
        if (convView == null) return;
        double v = parseAmt(amt);
        String ex = v > 0 ? exch(v, accCur(accId)) : "";
        convView.setText(ex);
        convView.setVisibility(ex.isEmpty() ? View.GONE : View.VISIBLE);
    }

    @Override public void build(LinearLayout c) {
        View ok = actIcon("ic_check", this::save);
        if (this.e != null) {
            View del = actIcon("ic_delete", () -> confirm(t("delete"), t("delete_txn_q"), t("delete"), () -> {
                database.transactions.remove(e);
                saveDb();
                pop();
            }));
            header(c, t("edit_transaction"), true, del, ok);
        } else {
            header(c, t(type == 1 ? "add_income" : "add_expense"), true, ok);
        }
        put(c, seg(new String[]{t("expense"), t("income")}, type, new int[]{expenseColor, incomeColor}, new String[]{ICON_EXPENSE, ICON_INCOME}, i -> {
            type = i;
            if (!Arrays.asList(catSet(type)).contains(cat)) cat = "other";
            render();
        }), 0, 12);

        EditText ae = edit("0", amt, NUM, txt -> { amt = txt; updateConv(); });
        LinearLayout ac = amountCard(ae, sym(accCur(accId)));
        convView = tv("", 13, secondaryTextColor, 1);
        convView.setGravity(Gravity.CENTER);
        convView.setTextDirection(View.TEXT_DIRECTION_LTR);
        ac.addView(convView);
        updateConv();
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
        put(dc, edit(t("desc_hint"), desc, TXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE, txt -> desc = txt), 10, 0);
        tagEdit = edit(t("tags_hint"), tags, InputType.TYPE_CLASS_TEXT, txt -> tags = txt);
        put(dc, tagEdit, 10, 0);
        final HashMap<String, Integer> freq = new HashMap<>();
        for (Transaction x : database.transactions) if (!x.tags.isEmpty()) for (String g : x.tags.split(",")) {
            Integer o = freq.get(g);
            freq.put(g, o == null ? 1 : o + 1);
        }
        if (!freq.isEmpty()) {
            final ArrayList<String> tl = new ArrayList<>(freq.keySet());
            tl.sort((a, b) -> freq.get(b) - freq.get(a));
            while (tl.size() > 10) tl.remove(tl.size() - 1);
            String[] lab = new String[tl.size()];
            for (int i = 0; i < lab.length; i++) lab[i] = "#" + tl.get(i);
            put(dc, chips(lab, -1, i -> {
                String cur2 = tagEdit.getText().toString();
                tagEdit.setText((cur2.trim().isEmpty() ? "" : cur2.trim() + ", ") + tl.get(i));
            }), 8, 0);
        }
        put(c, dc, 0, 12);

        LinearLayout tc = card();
        LinearLayout sr = box(true);
        sr.setGravity(Gravity.CENTER_VERTICAL);
        putW(sr, tv(t("settled"), 15, textColor, 2), 1f, 0, 0);
        M3Switch sw = new M3Switch(activity);
        sw.setChecked(settled);
        sw.setOnChange(v -> { settled = v; render(); });
        sr.addView(sw);
        tc.addView(sr);
        if (settled) {
            tc.addView(dateRow(t("date"), dateStr(time), () -> pickDate(time, l -> { time = l; render(); })));
            tc.addView(dateRow(t("time"), timeStr(time), () -> pickTime(time, l -> { time = l; render(); })));
        } else {
            tc.addView(tv(t("unsettled_hint"), 12, secondaryTextColor, 0));
            tc.addView(dateRow(t("due_date"), dateStr(due), () -> pickDate(due, l -> { due = l; render(); })));
        }
        put(c, tc, 0, 16);
        put(c, saveBtn(this::save), 0, 0);
    }

    void save() {
        double a = parseAmt(amt);
        if (a <= 0) { toast(t("err_amount")); return; }
        if (acc(accId) == null) { toast(t("err_account")); return; }
        final Transaction transaction = e == null ? new Transaction() : e;
        transaction.type = type; transaction.accountId = accId; transaction.category = cat; transaction.amount = a; transaction.title = title.trim(); transaction.description = desc.trim();
        transaction.tags = normTags(tags); transaction.settled = settled; transaction.time = time; transaction.dueDate = settled ? 0 : CalendarUtils.dayStart(due);
        transaction.planned = transaction.planned || !settled;
        final Runnable commit = () -> {
            if (e == null) database.transactions.add(transaction);
            saveDb();
            pop();
        };
        if (type == 0 && settled) {
            String w = budgetWarning(transaction);
            if (w != null) { confirm(t("budget_over_title"), w, t("save_anyway"), commit); return; }
        }
        commit.run();
    }
}
