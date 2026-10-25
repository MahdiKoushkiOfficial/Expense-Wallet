package com.mahdi.expensewallet.utils;

import com.mahdi.expensewallet.model.DateRange;
import com.mahdi.expensewallet.model.PlannedPayment;

import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.Locale;

import static com.mahdi.expensewallet.data.SettingsManager.calType;
import static com.mahdi.expensewallet.data.SettingsManager.fiscal;
import static com.mahdi.expensewallet.data.SettingsManager.hide;
import static com.mahdi.expensewallet.utils.Constants.JALALI_EN;
import static com.mahdi.expensewallet.utils.Constants.JALALI_FA;
import static com.mahdi.expensewallet.utils.Translator.digits;
import static com.mahdi.expensewallet.utils.Translator.fa;
import static com.mahdi.expensewallet.utils.Translator.loc;
import static com.mahdi.expensewallet.utils.Translator.t;

/** Date, time and text formatting that depends on the chosen language and calendar. */
public final class FormatUtils {
    private FormatUtils() { }

    public static long now() { return System.currentTimeMillis(); }

    public static boolean jal() { return calType == 1; }

    public static String monthName(int m, boolean j) {
        if (j) return fa() ? JALALI_FA[m - 1] : JALALI_EN[m - 1];
        return DateFormatSymbols.getInstance(loc()).getMonths()[m - 1];
    }

    public static String dateStr(long ms) {
        int[] a = CalendarUtils.ymd(ms, jal());
        return digits(a[2] + " " + monthName(a[1], jal()) + " " + a[0]);
    }

    public static String shortDate(long ms) {
        int[] a = CalendarUtils.ymd(ms, jal());
        return digits(a[2] + " " + monthName(a[1], jal()));
    }

    /** "12 Oct" for the current year, "12 Oct 2025" for any other year. */
    public static String relDate(long ms, int currentYear) {
        int[] a = CalendarUtils.ymd(ms, jal());
        return digits(a[2] + " " + monthName(a[1], jal()) + (a[0] == currentYear ? "" : " " + a[0]));
    }

    public static String timeStr(long ms) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(ms);
        return digits(String.format(Locale.US, "%02d:%02d", c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE)));
    }

    public static String dayLabel(long ms) {
        long d = CalendarUtils.dayStart(ms), today = CalendarUtils.dayStart(now());
        if (d == today) return t("today");
        if (d == CalendarUtils.dayStart(today - 1)) return t("yesterday");
        return dateStr(ms);
    }

    /** Fiscal "month" period, offset by off months. */
    public static DateRange monthPeriod(int off) {
        boolean j = jal();
        int[] n = CalendarUtils.ymd(now(), j);
        int y = n[0], m = n[1];
        if (n[2] < fiscal) { m--; if (m == 0) { m = 12; y--; } }
        int idx = y * 12 + (m - 1) + off;
        y = Math.floorDiv(idx, 12);
        m = Math.floorMod(idx, 12) + 1;
        int ny = y, nm = m + 1;
        if (nm == 13) { nm = 1; ny++; }
        DateRange p = new DateRange();
        p.s = CalendarUtils.ms(y, m, fiscal, 0, 0, j);
        p.e = CalendarUtils.ms(ny, nm, fiscal, 0, 0, j) - 1;
        int cy = CalendarUtils.ymd(now(), j)[0];
        if (fiscal == 1) p.label = digits(monthName(m, j) + (y == cy ? "" : " " + y));
        else p.label = relDate(p.s, cy) + " – " + relDate(p.e, cy);
        return p;
    }

    public static long weekStart(long ms) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(CalendarUtils.dayStart(ms));
        int dow = c.get(Calendar.DAY_OF_WEEK); // Sun=1 .. Sat=7
        int first = jal() ? Calendar.SATURDAY : Calendar.MONDAY;
        c.add(Calendar.DAY_OF_MONTH, -Math.floorMod(dow - first, 7));
        return c.getTimeInMillis();
    }

    public static String hid(String s) { return hide ? "••••" : s; }

    public static String normTags(String raw) {
        LinkedHashMap<String, Boolean> m = new LinkedHashMap<>();
        for (String p : raw.split("[,\\s،]+")) {
            p = p.trim();
            while (p.startsWith("#")) p = p.substring(1);
            if (!p.isEmpty()) m.put(p.toLowerCase(Locale.ROOT), true);
        }
        StringBuilder b = new StringBuilder();
        for (String k : m.keySet()) { if (b.length() > 0) b.append(','); b.append(k); }
        return b.toString();
    }

    public static String tagsView(String tags) {
        if (tags == null || tags.isEmpty()) return "";
        StringBuilder b = new StringBuilder();
        for (String p : tags.split(",")) b.append(" #").append(p);
        return b.toString().trim();
    }

    public static String repeatText(PlannedPayment payment) {
        if (payment.unit == 0) return t("rep_once");
        String[] k = {"", "day", "week", "month", "year"};
        if (payment.n == 1) return t("rep_" + k[payment.unit]);
        return t("repeat_every", payment.n, t("unit_" + k[payment.unit]));
    }
}
