package com.mahdi.expensewallet.ui.pages;

import static com.mahdi.expensewallet.MainActivity.activity;
import static com.mahdi.expensewallet.ui.Navigator.gotoTab;
import static com.mahdi.expensewallet.ui.Navigator.tabIdx;

public abstract class TabPage extends Page {
    public TabPage() { tab = true; }
    @Override public void back() { if (tabIdx != 0) gotoTab(0); else activity.finish(); }
}
