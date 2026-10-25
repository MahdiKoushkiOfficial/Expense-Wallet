package com.mahdi.expensewallet.data;

import com.mahdi.expensewallet.model.Account;
import com.mahdi.expensewallet.model.BudgetSummary;
import com.mahdi.expensewallet.model.DateRange;
import com.mahdi.expensewallet.model.DuePayment;
import com.mahdi.expensewallet.model.HomeBudgetSummary;
import com.mahdi.expensewallet.model.PlannedPayment;
import com.mahdi.expensewallet.model.Saving;
import com.mahdi.expensewallet.model.Transaction;
import com.mahdi.expensewallet.utils.CalendarUtils;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static com.mahdi.expensewallet.data.DatabaseStorage.database;
import static com.mahdi.expensewallet.data.DatabaseStorage.saveDb;
import static com.mahdi.expensewallet.data.ExchangeRateManager.toBase;
import static com.mahdi.expensewallet.data.SettingsManager.base;
import static com.mahdi.expensewallet.data.SettingsManager.fiscal;
import static com.mahdi.expensewallet.ui.Dialogs.confirm;
import static com.mahdi.expensewallet.ui.Navigator.render;
import static com.mahdi.expensewallet.ui.Theme.expenseColor;
import static com.mahdi.expensewallet.ui.Theme.incomeColor;
import static com.mahdi.expensewallet.utils.Constants.CATS;
import static com.mahdi.expensewallet.utils.Constants.CAT_COLORS;
import static com.mahdi.expensewallet.utils.Constants.DAY;
import static com.mahdi.expensewallet.utils.Constants.INCOME_CATS;
import static com.mahdi.expensewallet.utils.CurrencyUtils.money;
import static com.mahdi.expensewallet.utils.FormatUtils.jal;
import static com.mahdi.expensewallet.utils.FormatUtils.monthPeriod;
import static com.mahdi.expensewallet.utils.FormatUtils.now;
import static com.mahdi.expensewallet.utils.FormatUtils.weekStart;
import static com.mahdi.expensewallet.utils.Translator.loc;
import static com.mahdi.expensewallet.utils.Translator.t;

/** Balances, history, planned payments (dues) and budgets calculated from the database. */
public final class FinanceManager {
    private FinanceManager() { }

    public static Account acc(String id) {
        for (Account a : database.accounts) if (a.id.equals(id)) return a;
        return null;
    }

    public static String accName(String id) { Account a = acc(id); return a == null ? "?" : a.name; }

    /** Every account uses the base currency. */
    public static String accCur(String id) { return base; }

    public static double accBalance(Account a) {
        double b = a.initial;
        for (Transaction x : database.transactions) if (x.settled && x.accountId.equals(a.id)) b += x.type == 1 ? x.amount : -x.amount;
        for (Saving s : database.savings) if (s.accountId.equals(a.id)) b -= s.amount;
        return b;
    }

    public static double totalBalance() {
        double s = 0;
        for (Account a : database.accounts) s += accBalance(a);
        return s;
    }

    public static double totalSaved() {
        double s = 0;
        for (Saving x : database.savings) s += toBase(x.amount, accCur(x.accountId));
        return s;
    }

    /** {income, expense, saved, count} in base currency; null filters = all. */
    public static double[] sums(long s, long e, String accId, String cat) {
        double inc = 0, exp = 0, sav = 0, n = 0;
        for (Transaction x : database.transactions) {
            if (!x.settled || x.time < s || x.time > e) continue;
            if (accId != null && !x.accountId.equals(accId)) continue;
            if (cat != null && !x.category.equals(cat)) continue;
            double v = toBase(x.amount, accCur(x.accountId));
            if (x.type == 1) inc += v; else exp += v;
            n++;
        }
        if (cat == null) for (Saving x : database.savings) {
            if (x.time < s || x.time > e) continue;
            if (accId != null && !x.accountId.equals(accId)) continue;
            sav += toBase(x.amount, accCur(x.accountId));
        }
        return new double[]{inc, exp, sav, n};
    }

    public static ArrayList<Object> history(long s, long e, String accId, String cat) {
        ArrayList<Object> l = new ArrayList<>();
        for (Transaction x : database.transactions) {
            if (!x.settled || x.time < s || x.time > e) continue;
            if (accId != null && !x.accountId.equals(accId)) continue;
            if (cat != null && !x.category.equals(cat)) continue;
            l.add(x);
        }
        if (cat == null) for (Saving x : database.savings) {
            if (x.time < s || x.time > e) continue;
            if (accId != null && !x.accountId.equals(accId)) continue;
            l.add(x);
        }
        l.sort((a, b) -> Long.compare(timeOf(b), timeOf(a)));
        return l;
    }

    public static long timeOf(Object o) { return o instanceof Transaction ? ((Transaction) o).time : ((Saving) o).time; }

    public static ArrayList<String> sortedCats(String[] only) {
        final Collator col = Collator.getInstance(loc());
        final HashSet<String> used = new HashSet<>();
        for (Transaction x : database.transactions) used.add(x.category);
        ArrayList<String> l = new ArrayList<>();
        boolean hasOther = false;
        for (String c : only) { if ("other".equals(c)) hasOther = true; else l.add(c); }
        l.sort((a, b) -> {
            boolean ua = used.contains(a), ub = used.contains(b);
            if (ua != ub) return ua ? -1 : 1;
            return col.compare(t("cat_" + a), t("cat_" + b));
        });
        if (hasOther) l.add("other"); // "Other" is always the last item
        return l;
    }

    public static String catName(String k) { return t("cat_" + k); }

    public static int catColor(String k) {
        for (int i = 0; i < CATS.length; i++) if (CATS[i].equals(k)) return CAT_COLORS[i];
        return CAT_COLORS[CAT_COLORS.length - 1];
    }

    public static String catIcon(String k) {
        if ("loans".equals(k)) return "acc_credit_card";
        if ("salary".equals(k)) return "ic_paid";
        if ("other".equals(k)) return "ic_more_horiz";
        return "cat_" + k;
    }

    public static ArrayList<DuePayment> dues(long horizon) {
        ArrayList<DuePayment> l = new ArrayList<>();
        for (Transaction x : database.transactions) {
            if (x.settled || x.dueDate > horizon) continue;
            DuePayment duePayment = new DuePayment();
            duePayment.t = x; duePayment.when = x.dueDate; duePayment.type = x.type; duePayment.category = x.category; duePayment.accountId = x.accountId; duePayment.amount = x.amount;
            duePayment.title = x.title.isEmpty() ? catName(x.category) : x.title;
            l.add(duePayment);
        }
        for (PlannedPayment x : database.plannedPayments) {
            if (x.next > horizon) continue;
            DuePayment duePayment = new DuePayment();
            duePayment.r = x; duePayment.when = x.next; duePayment.type = x.type; duePayment.category = x.category; duePayment.accountId = x.accountId; duePayment.amount = x.amount;
            duePayment.title = x.title.isEmpty() ? catName(x.category) : x.title;
            l.add(duePayment);
        }
        l.sort((a, b) -> Long.compare(a.when, b.when));
        return l;
    }

    public static long nextOcc(PlannedPayment payment, long from) {
        switch (payment.unit) {
            case 1: return CalendarUtils.addDays(from, payment.n);
            case 2: return CalendarUtils.addDays(from, 7 * payment.n);
            case 3: return CalendarUtils.addMonths(from, payment.n, jal());
            case 4: return CalendarUtils.addMonths(from, 12 * payment.n, jal());
            default: return Long.MAX_VALUE;
        }
    }

    public static void advance(PlannedPayment payment) {
        if (payment.unit == 0) database.plannedPayments.remove(payment); else payment.next = nextOcc(payment, payment.next);
    }

    public static void applyPay(DuePayment duePayment) {
        if (duePayment.t != null) {
            duePayment.t.settled = true;
            duePayment.t.time = now();
            duePayment.t.planned = true;
            duePayment.t.dueDate = 0;
        } else {
            Transaction transaction = new Transaction();
            transaction.type = duePayment.r.type; transaction.accountId = duePayment.r.accountId; transaction.category = duePayment.r.category; transaction.amount = duePayment.r.amount;
            transaction.title = duePayment.r.title; transaction.description = duePayment.r.description; transaction.time = now(); transaction.planned = true;
            database.transactions.add(transaction);
            advance(duePayment.r);
        }
    }

    public static void applySkip(DuePayment duePayment) { if (duePayment.t != null) database.transactions.remove(duePayment.t); else advance(duePayment.r); }

    public static void applyDelete(DuePayment duePayment) { if (duePayment.t != null) database.transactions.remove(duePayment.t); else database.plannedPayments.remove(duePayment.r); }

    public static void payDue(DuePayment duePayment) { applyPay(duePayment); saveDb(); render(); }

    public static void skipDue(DuePayment duePayment) { applySkip(duePayment); saveDb(); render(); }

    public static void deleteDue(final DuePayment duePayment) {
        confirm(t("delete"), t("delete_planned_q"), t("delete"), () -> { applyDelete(duePayment); saveDb(); render(); });
    }

    /** Budget window (start) for a budget type that contains the time. */
    public static long[] budgetWindow(int type, long when) {
        if (type == 0) {
            long s = CalendarUtils.dayStart(when);
            return new long[]{s, s + DAY - 1};
        }

        if (type == 1) {
            long s = weekStart(when);
            return new long[]{s, CalendarUtils.addDays(s, 7) - 1};
        }

        // Monthly budget: calculate the fiscal month that CONTAINS 'when'.
        boolean j = jal();
        int[] d = CalendarUtils.ymd(when, j);
        int y = d[0];
        int m = d[1];

        if (d[2] < fiscal) {
            m--;
            if (m == 0) {
                m = 12;
                y--;
            }
        }

        int ny = y;
        int nm = m + 1;
        if (nm == 13) {
            nm = 1;
            ny++;
        }

        long s = CalendarUtils.ms(y, m, fiscal, 0, 0, j);
        long e = CalendarUtils.ms(ny, nm, fiscal, 0, 0, j) - 1;

        return new long[]{s, e};
    }

    public static double budgetSpent(String cat, long s, long e, String excludeId) {
        double v = 0;
        for (Transaction x : database.transactions) {
            if (!x.settled || x.type != 0 || !x.category.equals(cat) || x.time < s || x.time > e) continue;
            if (x.id.equals(excludeId)) continue;
            v += toBase(x.amount, accCur(x.accountId));
        }
        return v;
    }

    public static String budgetWarning(Transaction transaction) {
        double[] b = database.budgets.get(transaction.category);
        if (b == null) return null;
        long[] w = budgetWindow((int) b[0], transaction.time);
        double total = budgetSpent(transaction.category, w[0], w[1], transaction.id) + toBase(transaction.amount, accCur(transaction.accountId));
        if (total <= b[1]) return null;
        return t("budget_over_msg", catName(transaction.category), money(total, base), money(b[1], base));
    }

    /**
     * Home budget rule:
     *  - budgets over their limit appear ONLY in the alert (named when exactly one, counted when two or more);
     *  - the card below is the budget with the highest spending % among those NOT over (no threshold);
     *  - nothing below the alert when every budget is over, and nothing at all when there are no budgets.
     */
    public static HomeBudgetSummary pickHomeBudget(List<BudgetSummary> all) {
        HomeBudgetSummary homeBudget = new HomeBudgetSummary();
        for (BudgetSummary b : all) {
            if (b.ratio > 1) {
                homeBudget.overCount++;
                homeBudget.overOne = homeBudget.overCount == 1 ? b : null;
            } else if (homeBudget.shown == null || b.ratio > homeBudget.shown.ratio) {
                homeBudget.shown = b;
            }
        }
        return homeBudget;
    }

    /** "How far over": 118% spent -> 18. Never below 1 for an over-limit budget. */
    public static int overPct(double ratio) { return (int) Math.max(1, Math.round((ratio - 1) * 100)); }

    /** Progress bars: green up to 80%, red from 80% on. */
    public static int budgetColor(double ratio) { return ratio >= 0.8 ? expenseColor : incomeColor; }

    /** Buffer bar fill: the closer the balance gets to the buffer the fuller it is; full when at or below the buffer. */
    public static double bufferFill(double balance, double buffer) {
        if (buffer <= 0) return 0;
        if (balance <= buffer) return 1;
        return buffer / balance;
    }

    /**
     * Budgets as seen for the month at offset monthOff (0 = current month).
     * Monthly budgets follow that month. Daily / weekly budgets only exist for the current month
     * (today / this week, they reset by themselves) and are hidden in any other month.
     */
    public static ArrayList<BudgetSummary> budgetStatus(int monthOff) {
        ArrayList<BudgetSummary> l = new ArrayList<>();
        for (Map.Entry<String, double[]> e : database.budgets.entrySet()) {
            BudgetSummary budget = new BudgetSummary();
            budget.category = e.getKey();
            budget.type = (int) e.getValue()[0];
            budget.limit = e.getValue()[1];
            long[] w;
            if (budget.type == 2) { DateRange p = monthPeriod(monthOff); w = new long[]{p.s, p.e}; }
            else if (monthOff == 0) w = budgetWindow(budget.type, now());
            else continue;
            budget.spent = budgetSpent(budget.category, w[0], w[1], null);
            budget.ratio = budget.limit <= 0 ? 0 : budget.spent / budget.limit;
            l.add(budget);
        }
        return l;
    }

    public static String[] catSet(int type) {
        if (type == 1) return INCOME_CATS;
        ArrayList<String> l = new ArrayList<>(Arrays.asList(CATS));
        l.remove("salary");
        return l.toArray(new String[0]);
    }

    public static ArrayList<String> availableBudgetCats() {
        ArrayList<String> l = sortedCats(catSet(0));
        l.removeAll(database.budgets.keySet());
        return l;
    }

    public static double goalTotal(String g) {
        double v = 0;
        for (Saving s : database.savings) if (s.goal.equals(g)) v += toBase(s.amount, accCur(s.accountId));
        return v;
    }
}
