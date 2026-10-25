package com.mahdi.expensewallet.ui.pages;

import android.widget.LinearLayout;

import com.mahdi.expensewallet.ui.Callbacks.Str;

import java.util.ArrayList;

import static com.mahdi.expensewallet.data.FinanceManager.sortedCats;
import static com.mahdi.expensewallet.ui.Navigator.pop;
import static com.mahdi.expensewallet.ui.SharedWidgets.catList;
import static com.mahdi.expensewallet.ui.UiHelper.header;
import static com.mahdi.expensewallet.utils.Translator.t;

/** Picker: the same list as the Categories tab without amounts. Always opens at the top; the chosen row is highlighted. */
public class CategoryPickPage extends Page {
    final String[] set;
    final String sel;
    final java.util.Set<String> exclude;
    final Str cb;

    public CategoryPickPage(String[] set, String sel, java.util.Set<String> exclude, Str cb) {
        this.set = set; this.sel = sel; this.exclude = exclude; this.cb = cb;
    }

    @Override public void build(LinearLayout c) {
        header(c, t("choose_category"), true);
        ArrayList<String> keys = sortedCats(set);
        if (exclude != null) keys.removeAll(exclude);
        catList(c, keys, sel, false, k -> { cb.f(k); pop(); });
    }
}
