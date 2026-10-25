package com.mahdi.expensewallet.utils;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import static com.mahdi.expensewallet.data.ExchangeRateManager.conv;
import static com.mahdi.expensewallet.data.SettingsManager.exchCur;
import static com.mahdi.expensewallet.utils.Translator.digits;
import static com.mahdi.expensewallet.utils.Translator.fa;
import static com.mahdi.expensewallet.utils.Translator.t;

/** Currency symbols, amount formatting and parsing. */
public final class CurrencyUtils {
    private CurrencyUtils() { }

    public static String sym(String cur) {
        switch (cur) {
            case "USD": return "$";
            case "EUR": return "€";
            case "CHF": return "CHF";
            default: return fa() ? "ریال" : "IRR";
        }
    }

    public static String num(double v, String cur) {
        DecimalFormat df = new DecimalFormat("IRR".equals(cur) ? "#,##0" : "#,##0.00", DecimalFormatSymbols.getInstance(Locale.US));
        return digits(df.format(v));
    }

    public static String money(double v, String cur) { return num(v, cur) + " " + sym(cur); }

    public static String signed(double v, String cur, int type) { return (type == 1 ? "+" : "−") + money(Math.abs(v), cur); }

    public static double parseAmt(String s) {
        if (s == null) return 0;
        StringBuilder b = new StringBuilder();
        for (char ch : s.toCharArray()) {
            if (ch >= '۰' && ch <= '۹') b.append((char) ('0' + (ch - '۰')));
            else if (ch >= '٠' && ch <= '٩') b.append((char) ('0' + (ch - '٠')));
            else if (ch == '٫' || ch == '،') b.append('.');
            else if (ch == '٬') continue;
            else b.append(ch);
        }
        String x = b.toString().trim().replace(" ", "");
        if (x.contains(",") && x.contains(".")) x = x.replace(",", "");
        else x = x.replace(',', '.');
        try { return Double.parseDouble(x); } catch (Exception e) { return 0; }
    }

    /** Approximate value in the optional exchange currency ("" when none / same currency / no rate). */
    public static String exch(double v, String from) {
        if (exchCur.isEmpty() || exchCur.equals(from)) return "";
        double r = conv(v, from, exchCur);
        return Double.isNaN(r) ? "" : "≈ " + money(r, exchCur);
    }

    public static String curLabel(String cu) { return t("cur_" + cu.toLowerCase(Locale.ROOT)) + " " + sym(cu); }

    public static double round2(double v, String cur) { return "IRR".equals(cur) ? Math.round(v) : Math.round(v * 100) / 100.0; }
}
