package com.mahdi.expensewallet.model;

public class DuePayment {
    public Transaction t;
    public PlannedPayment r;
    public long when;
    public int type;
    public String category, accountId, title;
    public double amount;
}
