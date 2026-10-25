package com.mahdi.expensewallet.ui;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.NumberPicker;

import com.mahdi.expensewallet.ui.Callbacks.IntCb;
import com.mahdi.expensewallet.ui.Callbacks.LongCb;
import com.mahdi.expensewallet.ui.Callbacks.Str;
import com.mahdi.expensewallet.utils.CalendarUtils;

import java.util.Calendar;

import static com.mahdi.expensewallet.MainActivity.activity;
import static com.mahdi.expensewallet.ui.Theme.dark;
import static com.mahdi.expensewallet.ui.UiHelper.box;
import static com.mahdi.expensewallet.ui.UiHelper.dp;
import static com.mahdi.expensewallet.ui.UiHelper.edit;
import static com.mahdi.expensewallet.ui.UiHelper.putW;
import static com.mahdi.expensewallet.utils.FormatUtils.jal;
import static com.mahdi.expensewallet.utils.FormatUtils.monthName;
import static com.mahdi.expensewallet.utils.Translator.fa;
import static com.mahdi.expensewallet.utils.Translator.t;

/** Dialogs and pickers. */
public final class Dialogs {
    private Dialogs() { }

    public static AlertDialog.Builder dlgB() {
        return new AlertDialog.Builder(activity, dark ? android.R.style.Theme_Material_Dialog_Alert : android.R.style.Theme_Material_Light_Dialog_Alert);
    }

    public static AlertDialog showDlg(AlertDialog.Builder b) {
        AlertDialog d = b.create();
        d.show();
        d.getWindow().getDecorView().setLayoutDirection(fa() ? View.LAYOUT_DIRECTION_RTL : View.LAYOUT_DIRECTION_LTR);
        return d;
    }

    public static void confirm(String title, String msg, String yes, final Runnable r) {
        showDlg(dlgB().setTitle(title).setMessage(msg).setNegativeButton(t("cancel"), null)
                .setPositiveButton(yes, (d, w) -> r.run()));
    }

    public static void inputDlg(String title, String hint, String init, int type, final Str cb) {
        final EditText e = edit(hint, init, type, null);
        LinearLayout w = box(false);
        w.setPadding(dp(20), dp(8), dp(20), 0);
        w.addView(e);
        showDlg(dlgB().setTitle(title).setView(w).setNegativeButton(t("cancel"), null)
                .setPositiveButton(t("ok"), (d, x) -> cb.f(e.getText().toString())));
    }

    public static void listDlg(String title, String[] items, final IntCb cb) {
        showDlg(dlgB().setTitle(title).setItems(items, (d, i) -> cb.f(i)));
    }

    public static Context dlgCtx() {
        return new android.view.ContextThemeWrapper(activity, dark ? android.R.style.Theme_Material_Dialog_Alert : android.R.style.Theme_Material_Light_Dialog_Alert);
    }

    public static NumberPicker np(int min, int max, int val) {
        NumberPicker p = new NumberPicker(dlgCtx());
        p.setMinValue(min);
        p.setMaxValue(max);
        p.setValue(Math.max(min, Math.min(max, val)));
        p.setWrapSelectorWheel(false);
        return p;
    }

    /** Date picker working in the selected calendar. Keeps the time of day of init. */
    public static void pickDate(final long init, final LongCb cb) {
        final boolean j = jal();
        final int[] a = CalendarUtils.ymd(init, j);
        final Calendar tc = Calendar.getInstance();
        tc.setTimeInMillis(init);
        final NumberPicker py = np(j ? 1380 : 2000, j ? 1460 : 2100, a[0]);
        final NumberPicker pm = np(1, 12, a[1]);
        String[] names = new String[12];
        for (int i = 0; i < 12; i++) names[i] = monthName(i + 1, j);
        pm.setDisplayedValues(names);
        final NumberPicker pd = np(1, 31, a[2]);
        NumberPicker.OnValueChangeListener fix = (p, o, n) -> pd.setMaxValue(CalendarUtils.dim(py.getValue(), pm.getValue(), j));
        py.setOnValueChangedListener(fix);
        pm.setOnValueChangedListener(fix);
        pd.setMaxValue(CalendarUtils.dim(a[0], a[1], j));
        pd.setValue(a[2]);
        LinearLayout row = box(true);
        row.setGravity(Gravity.CENTER);
        row.setPadding(dp(8), dp(8), dp(8), dp(8));
        putW(row, pd, 1f, 0, 0);
        putW(row, pm, 2f, 0, 0);
        putW(row, py, 1.3f, 0, 0);
        showDlg(dlgB().setView(row).setNegativeButton(t("cancel"), null).setPositiveButton(t("ok"), (d, w) -> {
            int dd = Math.min(pd.getValue(), CalendarUtils.dim(py.getValue(), pm.getValue(), j));
            cb.f(CalendarUtils.ms(py.getValue(), pm.getValue(), dd, tc.get(Calendar.HOUR_OF_DAY), tc.get(Calendar.MINUTE), j));
        }));
    }

    public static void pickTime(final long init, final LongCb cb) {
        final Calendar c = Calendar.getInstance();
        c.setTimeInMillis(init);
        new TimePickerDialog(activity, dark ? android.R.style.Theme_Material_Dialog_Alert : android.R.style.Theme_Material_Light_Dialog_Alert, (v, h, m) -> {
            c.set(Calendar.HOUR_OF_DAY, h);
            c.set(Calendar.MINUTE, m);
            cb.f(c.getTimeInMillis());
        }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true).show();
    }
}
