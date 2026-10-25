package com.mahdi.expensewallet.ui;

/** Small callback interfaces. */
public final class Callbacks {
    private Callbacks() { }

    public interface Str { void f(String s); }
    public interface IntCb { void f(int i); }
    public interface LongCb { void f(long l); }

    public interface ExUpd { void upd(String amountText); }

    public interface OnChange { void f(boolean on); }
}
