package com.mahdi.expensewallet.ui;

import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mahdi.expensewallet.model.DateRange;
import com.mahdi.expensewallet.model.DuePayment;
import com.mahdi.expensewallet.model.Saving;
import com.mahdi.expensewallet.model.Transaction;
import com.mahdi.expensewallet.ui.Callbacks.Str;
import com.mahdi.expensewallet.ui.pages.AccountFormPage;
import com.mahdi.expensewallet.ui.pages.BudgetFormPage;
import com.mahdi.expensewallet.ui.pages.GoalPage;
import com.mahdi.expensewallet.ui.pages.PlannedFormPage;
import com.mahdi.expensewallet.ui.pages.TransactionPage;
import com.mahdi.expensewallet.utils.CalendarUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.mahdi.expensewallet.MainActivity.activity;
import static com.mahdi.expensewallet.data.DatabaseStorage.database;
import static com.mahdi.expensewallet.data.FinanceManager.accCur;
import static com.mahdi.expensewallet.data.FinanceManager.accName;
import static com.mahdi.expensewallet.data.FinanceManager.availableBudgetCats;
import static com.mahdi.expensewallet.data.FinanceManager.catColor;
import static com.mahdi.expensewallet.data.FinanceManager.catIcon;
import static com.mahdi.expensewallet.data.FinanceManager.catName;
import static com.mahdi.expensewallet.data.FinanceManager.deleteDue;
import static com.mahdi.expensewallet.data.FinanceManager.payDue;
import static com.mahdi.expensewallet.data.FinanceManager.skipDue;
import static com.mahdi.expensewallet.data.FinanceManager.sums;
import static com.mahdi.expensewallet.data.FinanceManager.timeOf;
import static com.mahdi.expensewallet.data.SettingsManager.base;
import static com.mahdi.expensewallet.ui.Dialogs.dlgB;
import static com.mahdi.expensewallet.ui.Dialogs.showDlg;
import static com.mahdi.expensewallet.ui.Navigator.bar;
import static com.mahdi.expensewallet.ui.Navigator.gotoTab;
import static com.mahdi.expensewallet.ui.Navigator.push;
import static com.mahdi.expensewallet.ui.Navigator.tabIdx;
import static com.mahdi.expensewallet.ui.Theme.accentColor;
import static com.mahdi.expensewallet.ui.Theme.expenseColor;
import static com.mahdi.expensewallet.ui.Theme.incomeColor;
import static com.mahdi.expensewallet.ui.Theme.secondarySurfaceColor;
import static com.mahdi.expensewallet.ui.Theme.secondaryTextColor;
import static com.mahdi.expensewallet.ui.Theme.surfaceColor;
import static com.mahdi.expensewallet.ui.Theme.textColor;
import static com.mahdi.expensewallet.ui.Theme.warningColor;
import static com.mahdi.expensewallet.ui.UiHelper.actIcon;
import static com.mahdi.expensewallet.ui.UiHelper.amountText;
import static com.mahdi.expensewallet.ui.UiHelper.amtCol;
import static com.mahdi.expensewallet.ui.UiHelper.box;
import static com.mahdi.expensewallet.ui.UiHelper.btn;
import static com.mahdi.expensewallet.ui.UiHelper.card;
import static com.mahdi.expensewallet.ui.UiHelper.catRow;
import static com.mahdi.expensewallet.ui.UiHelper.circ;
import static com.mahdi.expensewallet.ui.UiHelper.click;
import static com.mahdi.expensewallet.ui.UiHelper.dp;
import static com.mahdi.expensewallet.ui.UiHelper.ico;
import static com.mahdi.expensewallet.ui.UiHelper.put;
import static com.mahdi.expensewallet.ui.UiHelper.putW;
import static com.mahdi.expensewallet.ui.UiHelper.section;
import static com.mahdi.expensewallet.ui.UiHelper.shape;
import static com.mahdi.expensewallet.ui.UiHelper.toast;
import static com.mahdi.expensewallet.ui.UiHelper.tv;
import static com.mahdi.expensewallet.utils.Constants.GOALS;
import static com.mahdi.expensewallet.utils.Constants.GOAL_COLORS;
import static com.mahdi.expensewallet.utils.Constants.GOAL_ICONS;
import static com.mahdi.expensewallet.utils.Constants.ICON_EXPENSE;
import static com.mahdi.expensewallet.utils.Constants.ICON_INCOME;
import static com.mahdi.expensewallet.utils.Constants.WRAP;
import static com.mahdi.expensewallet.utils.CurrencyUtils.money;
import static com.mahdi.expensewallet.utils.CurrencyUtils.signed;
import static com.mahdi.expensewallet.utils.FormatUtils.dateStr;
import static com.mahdi.expensewallet.utils.FormatUtils.dayLabel;
import static com.mahdi.expensewallet.utils.FormatUtils.monthPeriod;
import static com.mahdi.expensewallet.utils.FormatUtils.tagsView;
import static com.mahdi.expensewallet.utils.Translator.t;

/** Bottom bar and widgets shared by several pages (transaction rows, add dialog, ...). */
public final class SharedWidgets {
    private SharedWidgets() { }

    public static void buildBar() {
        bar.setOrientation(LinearLayout.HORIZONTAL);
        bar.setBackgroundColor(surfaceColor);
        bar.setGravity(Gravity.CENTER_VERTICAL);
        int[] order = {0, 1, -1, 3, 2};
        String[] icons = {"ic_home", "ic_account_balance_wallet", "ic_settings", "ic_category"};
        String[] names = {"nav_home", "nav_accounts", "nav_settings", "nav_categories"};
        for (final int o : order) {
            if (o < 0) {
                FrameLayout f = new FrameLayout(activity);
                f.setClipChildren(false);
                FrameLayout c = circ("ic_add", 56, accentColor);
                c.setTranslationY(-dp(16));
                c.setElevation(dp(6));
                c.setOnClickListener(v -> addDialog());
                FrameLayout.LayoutParams l = new FrameLayout.LayoutParams(dp(56), dp(56));
                l.gravity = Gravity.CENTER;
                f.addView(c, l);
                bar.addView(f, new LinearLayout.LayoutParams(0, dp(58), 1f));
            } else {
                boolean sel = tabIdx == o;
                LinearLayout s = box(false);
                s.setGravity(Gravity.CENTER);
                s.setPadding(0, dp(8), 0, dp(8));
                s.addView(ico(icons[o], 24, sel ? accentColor : secondaryTextColor));
                TextView lb = tv(t(names[o]), 11, sel ? accentColor : secondaryTextColor, sel ? 2 : 1);
                lb.setGravity(Gravity.CENTER);
                lb.setSingleLine(true);
                lb.setEllipsize(TextUtils.TruncateAt.END);
                put(s, lb, WRAP, WRAP, 0, 2, 0, 0);
                click(s, () -> gotoTab(o));
                bar.addView(s, new LinearLayout.LayoutParams(0, WRAP, 1f));
            }
        }
    }

    public static void needAcc(Runnable r) {
        if (database.accounts.isEmpty()) {
            toast(t("need_account"));
            push(new AccountFormPage(null));
        } else r.run();
    }

    public static View addOption(String icon, String label, int color, Runnable r) {
        LinearLayout c = box(false);
        c.setGravity(Gravity.CENTER);
        c.setBackground(shape(secondarySurfaceColor, 18));
        c.setPadding(dp(8), dp(18), dp(8), dp(18));
        c.setMinimumHeight(dp(116));
        c.addView(circ(icon, 46, color));
        TextView l = tv(label, 15, textColor, 2);
        l.setGravity(Gravity.CENTER);
        put(c, l, WRAP, WRAP, 0, 10, 0, 0);
        click(c, r);
        return c;
    }

    public static void addDialog() {
        LinearLayout w = box(false);
        w.setPadding(dp(18), dp(20), dp(18), dp(18));
        TextView title = tv(t("add_new"), 18, textColor, 3);
        title.setGravity(Gravity.CENTER);
        put(w, title, 0, 16);
        final AlertDialog[] dd = new AlertDialog[1];
        LinearLayout r1 = box(true), r2 = box(true);
        putW(r1, addOption(ICON_EXPENSE, t("expense"), expenseColor, () -> { dd[0].dismiss(); needAcc(() -> push(new TransactionPage(null, 0))); }), 1f, 0, 6);
        putW(r1, addOption(ICON_INCOME, t("income"), incomeColor, () -> { dd[0].dismiss(); needAcc(() -> push(new TransactionPage(null, 1))); }), 1f, 6, 0);
        putW(r2, addOption("ic_repeat", t("planned"), 0xFF6366F1, () -> { dd[0].dismiss(); needAcc(() -> push(new PlannedFormPage(null))); }), 1f, 0, 6);
        putW(r2, addOption("ic_paid", t("budget"), warningColor, () -> { dd[0].dismiss(); openNewBudget(); }), 1f, 6, 0);
        put(w, r1, 0, 12);
        put(w, r2, 0, 0);
        AlertDialog d = showDlg(dlgB().setView(w));
        dd[0] = d;
        Window win = d.getWindow();
        win.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        win.setDimAmount(0.7f);
        if (Build.VERSION.SDK_INT >= 31) {
            try {
                win.addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
                WindowManager.LayoutParams lp = win.getAttributes();
                lp.setBlurBehindRadius(40);
                win.setAttributes(lp);
            } catch (Exception ignored) { }
        }
    }

    public static View txnRow(final Transaction transaction) {
        LinearLayout r = box(true);
        r.setGravity(Gravity.CENTER_VERTICAL);
        r.setBackground(shape(surfaceColor, 16));
        r.setPadding(dp(12), dp(12), dp(14), dp(12));
        put(r, circ(catIcon(transaction.category), 44, catColor(transaction.category)), dp(44), dp(44), 0, 0, 12, 0);
        LinearLayout col = box(false);
        TextView a = tv(transaction.title.isEmpty() ? catName(transaction.category) : transaction.title, 15, textColor, 2);
        a.setSingleLine(true);
        a.setEllipsize(TextUtils.TruncateAt.END);
        col.addView(a);
        TextView b = tv(catName(transaction.category) + " · " + accName(transaction.accountId) + (transaction.tags.isEmpty() ? "" : " · " + tagsView(transaction.tags)), 12, secondaryTextColor, 0);
        b.setSingleLine(true);
        b.setEllipsize(TextUtils.TruncateAt.END);
        col.addView(b);
        putW(r, col, 1f, 0, 8);
        String cur = accCur(transaction.accountId);
        r.addView(amtCol(signed(transaction.amount, cur, transaction.type), transaction.type == 1 ? incomeColor : expenseColor, 15, transaction.amount, cur, false));
        click(r, () -> push(new TransactionPage(transaction, transaction.type)));
        return r;
    }

    public static View saveRow(final Saving saving) {
        int gi = Arrays.asList(GOALS).indexOf(saving.goal);
        if (gi < 0) gi = GOALS.length - 1;
        LinearLayout r = box(true);
        r.setGravity(Gravity.CENTER_VERTICAL);
        r.setBackground(shape(surfaceColor, 16));
        r.setPadding(dp(12), dp(12), dp(14), dp(12));
        put(r, circ(GOAL_ICONS[gi], 44, GOAL_COLORS[gi]), dp(44), dp(44), 0, 0, 12, 0);
        LinearLayout col = box(false);
        col.addView(tv(t("saving_to", t("goal_" + saving.goal)), 15, textColor, 2));
        col.addView(tv(accName(saving.accountId), 12, secondaryTextColor, 0));
        putW(r, col, 1f, 0, 8);
        r.addView(amtCol("−" + money(saving.amount, accCur(saving.accountId)), textColor, 15, saving.amount, accCur(saving.accountId), false));
        final String goal = saving.goal;
        click(r, () -> push(new GoalPage(goal)));
        return r;
    }

    public static void histList(LinearLayout c, List<Object> items, int limit, Runnable more) {
        if (items.isEmpty()) {
            TextView e = tv(t("no_transactions"), 14, secondaryTextColor, 0);
            e.setGravity(Gravity.CENTER);
            put(c, e, 20, 20);
            return;
        }
        long lastDay = -1;
        int n = 0;
        for (Object o : items) {
            if (n >= limit) {
                if (more != null) put(c, btn(t("show_more"), secondarySurfaceColor, textColor, more), 8, 0);
                break;
            }
            long ts = timeOf(o), day = CalendarUtils.dayStart(ts);
            if (day != lastDay) { put(c, section(dayLabel(ts)), 14, 6); lastDay = day; }
            put(c, o instanceof Transaction ? txnRow((Transaction) o) : saveRow((Saving) o), 0, 8);
            n++;
        }
    }

    public static View dueCard(final DuePayment duePayment, boolean overdue, boolean deletable) {
        LinearLayout c = card();
        c.setPadding(dp(14), dp(12), dp(14), dp(12));
        LinearLayout r = box(true);
        r.setGravity(Gravity.CENTER_VERTICAL);
        put(r, circ(catIcon(duePayment.category), 40, catColor(duePayment.category)), dp(40), dp(40), 0, 0, 12, 0);
        LinearLayout col = box(false);
        TextView a = tv(duePayment.title, 15, textColor, 2);
        a.setSingleLine(true);
        a.setEllipsize(TextUtils.TruncateAt.END);
        col.addView(a);
        col.addView(tv(dateStr(duePayment.when) + " · " + accName(duePayment.accountId), 12, overdue ? expenseColor : secondaryTextColor, overdue ? 1 : 0));
        putW(r, col, 1f, 0, 8);
        r.addView(amtCol(signed(duePayment.amount, accCur(duePayment.accountId), duePayment.type), duePayment.type == 1 ? incomeColor : expenseColor, 15, duePayment.amount, accCur(duePayment.accountId), false));
        if (deletable) r.addView(actIcon("ic_delete", () -> deleteDue(duePayment)));
        c.addView(r);
        LinearLayout br = box(true);
        putW(br, btn(t("pay"), accentColor, Color.WHITE, () -> payDue(duePayment)), 1f, 0, 6);
        putW(br, btn(t("skip"), secondarySurfaceColor, textColor, () -> skipDue(duePayment)), 1f, 6, 0);
        put(c, br, 10, 0);
        return c;
    }

    /** Category rows. amounts=true adds period totals (Categories tab); false is a plain picker row. */
    public static void catList(LinearLayout c, ArrayList<String> keys, String sel, boolean amounts, final Str cb) {
        final DateRange p = monthPeriod(0);
        for (final String k : keys) {
            double[] all = amounts ? sums(0, Long.MAX_VALUE, null, k) : null;
            double[] per = amounts ? sums(p.s, p.e, null, k) : null;
            LinearLayout r = catRow(k, amounts ? t("n_transactions", (int) all[3]) : null);
            boolean on = k.equals(sel);
            if (on) {
                GradientDrawable g = shape(surfaceColor, 16);
                g.setStroke(dp(2), accentColor);
                r.setBackground(g);
            }
            if (amounts && per[3] > 0) {
                LinearLayout am = box(false);
                am.setGravity(Gravity.END);
                if (per[1] > 0) {
                    TextView a = tv("", 14, expenseColor, 2);
                    amountText(a, "−" + money(per[1], base), 14);
                    am.addView(a);
                }
                if (per[0] > 0) {
                    TextView a = tv("", 14, incomeColor, 2);
                    amountText(a, "+" + money(per[0], base), 14);
                    am.addView(a);
                }
                r.addView(am);
            }
            if (on) r.addView(ico("ic_check", 22, accentColor));
            click(r, () -> cb.f(k));
            put(c, r, 0, 8);
        }
    }

    /** Opens the new-budget page; preset is used only when that category is still free. */
    public static void openBudgetForm(String preset) {
        ArrayList<String> av = availableBudgetCats();
        if (av.isEmpty()) { toast(t("budgets_all_used")); return; }
        push(new BudgetFormPage(preset != null && av.contains(preset) ? preset : (av.contains("other") ? "other" : av.get(0))));
    }

    public static void openNewBudget() { openBudgetForm(null); }
}
