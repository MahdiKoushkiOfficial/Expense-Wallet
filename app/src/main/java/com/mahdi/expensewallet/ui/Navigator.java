package com.mahdi.expensewallet.ui;

import android.annotation.SuppressLint;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.mahdi.expensewallet.ui.pages.Page;

import java.util.ArrayList;

import static com.mahdi.expensewallet.MainActivity.activity;
import static com.mahdi.expensewallet.data.ExchangeRateManager.ratesMissing;
import static com.mahdi.expensewallet.ui.SharedWidgets.buildBar;
import static com.mahdi.expensewallet.ui.Theme.applyTheme;
import static com.mahdi.expensewallet.ui.Theme.backgroundColor;
import static com.mahdi.expensewallet.ui.UiHelper.dp;
import static com.mahdi.expensewallet.utils.Constants.MATCH;
import static com.mahdi.expensewallet.utils.Constants.WRAP;
import static com.mahdi.expensewallet.utils.Translator.fa;

/** Screen shell and navigation: page stack, tabs, rendering and system insets. */
@SuppressLint("StaticFieldLeak")
public final class Navigator {
    private Navigator() { }

    public static int insetT, insetB, tabIdx = 0;

    public static FrameLayout root;
    public static LinearLayout frame, body, bar;
    public static ScrollView scroll;
    public static Page lastPage;
    public static final ArrayList<Page> stack = new ArrayList<>();
    public static Page[] tabs;

    public static void buildShell() {
        root = new FrameLayout(activity);
        frame = new LinearLayout(activity);
        frame.setOrientation(LinearLayout.VERTICAL);
        frame.setClipChildren(false);
        scroll = new ScrollView(activity);
        scroll.setVerticalScrollBarEnabled(false);
        scroll.setClipChildren(false);
        body = new LinearLayout(activity);
        body.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(body, new FrameLayout.LayoutParams(MATCH, WRAP));
        frame.addView(scroll, new LinearLayout.LayoutParams(MATCH, 0, 1f));
        bar = new LinearLayout(activity);
        bar.setClipChildren(false);
        bar.setClipToPadding(false);
        frame.addView(bar, new LinearLayout.LayoutParams(MATCH, WRAP));
        root.addView(frame, new FrameLayout.LayoutParams(MATCH, MATCH));
        activity.setContentView(root);
        root.setOnApplyWindowInsetsListener((v, ins) -> {
            insetT = ins.getSystemWindowInsetTop();
            insetB = ins.getSystemWindowInsetBottom();
            applyInsets();
            return ins;
        });
    }

    public static void applyInsets() {
        if (stack.isEmpty()) return;
        boolean tab = top().tab;
        frame.setPadding(0, insetT, 0, tab ? 0 : insetB);
        bar.setPadding(0, 0, 0, insetB);
    }

    public static void render() {
        applyTheme();
        Page p = top();
        int sy = scroll.getScrollY();
        boolean same = p == lastPage;
        ratesMissing = false;
        int dir = fa() ? View.LAYOUT_DIRECTION_RTL : View.LAYOUT_DIRECTION_LTR;
        root.setLayoutDirection(dir);
        root.setBackgroundColor(backgroundColor);
        body.removeAllViews();
        body.setPadding(dp(16), dp(8), dp(16), dp(24));
        p.build(body);
        bar.removeAllViews();
        if (p.tab) { bar.setVisibility(View.VISIBLE); buildBar(); } else bar.setVisibility(View.GONE);
        applyInsets();
        lastPage = p;
        final int fy = sy;
        if (same) scroll.post(() -> scroll.scrollTo(0, fy)); else scroll.scrollTo(0, 0);
    }

    public static Page top() { return stack.get(stack.size() - 1); }

    public static void push(Page p) { stack.add(p); render(); }

    public static void pop() {
        if (stack.size() > 1) { stack.remove(stack.size() - 1); render(); } else activity.finish();
    }

    public static void gotoTab(int i) {
        tabIdx = i;
        stack.clear();
        stack.add(tabs[i]);
        render();
    }

    /** Back to the initial values (a new activity instance used to start from these). */
    public static void reset() {
        insetT = 0;
        insetB = 0;
        tabIdx = 0;
        root = null;
        frame = null;
        body = null;
        bar = null;
        scroll = null;
        lastPage = null;
        stack.clear();
        tabs = null;
    }
}
