package com.mahdi.expensewallet.ui.pages;

import android.widget.LinearLayout;
import android.widget.TextView;

import static com.mahdi.expensewallet.ui.Theme.textColor;
import static com.mahdi.expensewallet.ui.UiHelper.card;
import static com.mahdi.expensewallet.ui.UiHelper.header;
import static com.mahdi.expensewallet.ui.UiHelper.put;
import static com.mahdi.expensewallet.ui.UiHelper.tv;
import static com.mahdi.expensewallet.utils.Translator.t;

public class InfoPage extends Page {
    final String titleKey, bodyKey;

    public InfoPage(String titleKey, String bodyKey) { this.titleKey = titleKey; this.bodyKey = bodyKey; }

    @Override public void build(LinearLayout c) {
        header(c, t(titleKey), true);
        LinearLayout cd = card();
        TextView b = tv(t(bodyKey), 14, textColor, 0);
        b.setLineSpacing(0, 1.25f);
        cd.addView(b);
        put(c, cd, 0, 0);
    }
}
