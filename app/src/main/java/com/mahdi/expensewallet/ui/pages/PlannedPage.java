package com.mahdi.expensewallet.ui.pages;

import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mahdi.expensewallet.model.DuePayment;
import com.mahdi.expensewallet.model.PlannedPayment;
import com.mahdi.expensewallet.model.Transaction;

import java.util.ArrayList;

import static com.mahdi.expensewallet.data.DatabaseStorage.database;
import static com.mahdi.expensewallet.data.DatabaseStorage.saveDb;
import static com.mahdi.expensewallet.data.FinanceManager.accCur;
import static com.mahdi.expensewallet.data.FinanceManager.catColor;
import static com.mahdi.expensewallet.data.FinanceManager.catIcon;
import static com.mahdi.expensewallet.data.FinanceManager.catName;
import static com.mahdi.expensewallet.data.FinanceManager.dues;
import static com.mahdi.expensewallet.ui.Dialogs.confirm;
import static com.mahdi.expensewallet.ui.Navigator.push;
import static com.mahdi.expensewallet.ui.Navigator.render;
import static com.mahdi.expensewallet.ui.SharedWidgets.dueCard;
import static com.mahdi.expensewallet.ui.SharedWidgets.needAcc;
import static com.mahdi.expensewallet.ui.SharedWidgets.txnRow;
import static com.mahdi.expensewallet.ui.Theme.expenseColor;
import static com.mahdi.expensewallet.ui.Theme.incomeColor;
import static com.mahdi.expensewallet.ui.Theme.secondaryTextColor;
import static com.mahdi.expensewallet.ui.Theme.surfaceColor;
import static com.mahdi.expensewallet.ui.Theme.textColor;
import static com.mahdi.expensewallet.ui.UiHelper.actIcon;
import static com.mahdi.expensewallet.ui.UiHelper.amtCol;
import static com.mahdi.expensewallet.ui.UiHelper.box;
import static com.mahdi.expensewallet.ui.UiHelper.circ;
import static com.mahdi.expensewallet.ui.UiHelper.click;
import static com.mahdi.expensewallet.ui.UiHelper.dp;
import static com.mahdi.expensewallet.ui.UiHelper.header;
import static com.mahdi.expensewallet.ui.UiHelper.put;
import static com.mahdi.expensewallet.ui.UiHelper.putW;
import static com.mahdi.expensewallet.ui.UiHelper.section;
import static com.mahdi.expensewallet.ui.UiHelper.shape;
import static com.mahdi.expensewallet.ui.UiHelper.tv;
import static com.mahdi.expensewallet.utils.Constants.DAY;
import static com.mahdi.expensewallet.utils.CurrencyUtils.signed;
import static com.mahdi.expensewallet.utils.FormatUtils.dateStr;
import static com.mahdi.expensewallet.utils.FormatUtils.now;
import static com.mahdi.expensewallet.utils.FormatUtils.repeatText;
import static com.mahdi.expensewallet.utils.Translator.t;

public class PlannedPage extends Page {
    @Override public void build(LinearLayout c) {
        header(c, t("planned"), true, actIcon("ic_add", () -> needAcc(() -> push(new PlannedFormPage(null)))));
        ArrayList<DuePayment> ds = dues(now() + 90 * DAY);
        if (!ds.isEmpty()) {
            put(c, section(t("due_soon")), 4, 6);
            for (DuePayment d : ds) put(c, dueCard(d, d.when <= now(), true), 0, 8);
        }
        put(c, section(t("planned_rules")), 12, 6);
        if (database.plannedPayments.isEmpty()) {
            TextView e = tv(t("no_planned"), 14, secondaryTextColor, 0);
            e.setGravity(Gravity.CENTER);
            put(c, e, 16, 16);
        }
        ArrayList<PlannedPayment> rs = new ArrayList<>(database.plannedPayments);
        rs.sort((a, b) -> Long.compare(a.next, b.next));
        for (final PlannedPayment r : rs) {
            LinearLayout row = box(true);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setBackground(shape(surfaceColor, 16));
            row.setPadding(dp(12), dp(8), dp(6), dp(8));
            put(row, circ(catIcon(r.category), 44, catColor(r.category)), dp(44), dp(44), 0, 0, 12, 0);
            LinearLayout col = box(false);
            col.addView(tv(r.title.isEmpty() ? catName(r.category) : r.title, 15, textColor, 2));
            col.addView(tv(t("next_on", dateStr(r.next)) + " · " + repeatText(r), 12, secondaryTextColor, 0));
            putW(row, col, 1f, 0, 8);
            row.addView(amtCol(signed(r.amount, accCur(r.accountId), r.type), r.type == 1 ? incomeColor : expenseColor, 15, r.amount, accCur(r.accountId), false));
            row.addView(actIcon("ic_delete", () -> confirm(t("delete"), t("delete_planned_q"), t("delete"), () -> {
                database.plannedPayments.remove(r);
                saveDb();
                render();
            })));
            click(row, () -> push(new PlannedFormPage(r)));
            put(c, row, 0, 8);
        }
        ArrayList<Transaction> done = new ArrayList<>();
        for (Transaction x : database.transactions) if (x.planned && x.settled) done.add(x);
        done.sort((a, b) -> Long.compare(b.time, a.time));
        if (!done.isEmpty()) {
            put(c, section(t("completed")), 12, 6);
            for (int i = 0; i < Math.min(30, done.size()); i++) {
                final Transaction transaction = done.get(i);
                LinearLayout row = box(true);
                row.setGravity(Gravity.CENTER_VERTICAL);
                putW(row, txnRow(transaction), 1f, 0, 4);
                row.addView(actIcon("ic_delete", () -> confirm(t("delete"), t("delete_txn_q"), t("delete"), () -> {
                    database.transactions.remove(transaction);
                    saveDb();
                    render();
                })));
                put(c, row, 0, 8);
            }
        }
    }
}
