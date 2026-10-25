package com.mahdi.expensewallet.ui.pages;

import android.widget.LinearLayout;

import static com.mahdi.expensewallet.data.FinanceManager.sortedCats;
import static com.mahdi.expensewallet.ui.Navigator.push;
import static com.mahdi.expensewallet.ui.SharedWidgets.catList;
import static com.mahdi.expensewallet.ui.Theme.secondaryTextColor;
import static com.mahdi.expensewallet.ui.UiHelper.header;
import static com.mahdi.expensewallet.ui.UiHelper.put;
import static com.mahdi.expensewallet.ui.UiHelper.tv;
import static com.mahdi.expensewallet.utils.Constants.CATS;
import static com.mahdi.expensewallet.utils.Translator.t;

public class CategoriesPage extends TabPage {
    @Override public void build(LinearLayout c) {
        header(c, t("categories"), false);
        put(c, tv(t("categories_hint"), 12, secondaryTextColor, 0), 0, 10);
        catList(c, sortedCats(CATS), null, true, k -> push(new CategoryPage(k)));
    }
}
