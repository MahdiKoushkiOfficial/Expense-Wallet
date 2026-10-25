package com.mahdi.expensewallet.ui.pages;

import android.text.InputType;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.mahdi.expensewallet.model.Transaction;

import java.util.ArrayList;

import static com.mahdi.expensewallet.data.DatabaseStorage.database;
import static com.mahdi.expensewallet.data.FinanceManager.accName;
import static com.mahdi.expensewallet.data.FinanceManager.catName;
import static com.mahdi.expensewallet.data.FinanceManager.timeOf;
import static com.mahdi.expensewallet.ui.SharedWidgets.histList;
import static com.mahdi.expensewallet.ui.UiHelper.box;
import static com.mahdi.expensewallet.ui.UiHelper.edit;
import static com.mahdi.expensewallet.ui.UiHelper.header;
import static com.mahdi.expensewallet.ui.UiHelper.put;
import static com.mahdi.expensewallet.utils.Translator.loc;
import static com.mahdi.expensewallet.utils.Translator.t;

public class SearchPage extends Page {
    String q = "";
    LinearLayout results;

    @Override public void build(LinearLayout c) {
        header(c, t("search"), true);
        EditText e = edit(t("search_hint"), q, InputType.TYPE_CLASS_TEXT, s -> { q = s; fill(); });
        put(c, e, 0, 12);
        results = box(false);
        c.addView(results);
        fill();
        e.requestFocus();
    }

    void fill() {
        results.removeAllViews();
        String s = q.trim().toLowerCase(loc());
        if (s.isEmpty()) return;
        ArrayList<Object> l = new ArrayList<>();
        for (Transaction x : database.transactions) {
            if (!x.settled) continue;
            String hay = (x.title + " " + x.description + " " + x.tags + " " + catName(x.category) + " " + accName(x.accountId) + " " + x.amount).toLowerCase(loc());
            if (hay.contains(s) || hay.contains(s.replace("#", ""))) l.add(x);
        }
        l.sort((a, b) -> Long.compare(timeOf(b), timeOf(a)));
        histList(results, l, 100, null);
    }
}
