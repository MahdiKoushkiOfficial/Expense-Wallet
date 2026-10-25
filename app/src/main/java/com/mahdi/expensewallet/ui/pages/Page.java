package com.mahdi.expensewallet.ui.pages;

import android.widget.LinearLayout;

import static com.mahdi.expensewallet.ui.Navigator.pop;

public abstract class Page {
    public boolean tab = false;
    public void back() { pop(); }
    public abstract void build(LinearLayout c);
}
