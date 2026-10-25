package com.mahdi.expensewallet.model;

/** What the Home page shows for budgets: an optional alert plus at most ONE card. */
public class HomeBudgetSummary {
    public BudgetSummary shown;      // highest spending % among budgets that are NOT over their limit (null = no card)
    public BudgetSummary overOne;    // the over-limit budget when exactly one is over
    public int overCount;  // budgets strictly over their limit
}
