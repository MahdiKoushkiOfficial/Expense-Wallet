package com.mahdi.expensewallet.utils;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

public class CalendarUtils {
    public static int[] g2j(int gy, int gm, int gd) {
        int[] gdm = {0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334};
        int jy;
        if (gy > 1600) { jy = 979; gy -= 1600; } else { jy = 0; gy -= 621; }
        int gy2 = gm > 2 ? gy + 1 : gy;
        int days = 365 * gy + (gy2 + 3) / 4 - (gy2 + 99) / 100 + (gy2 + 399) / 400 - 80 + gd + gdm[gm - 1];
        jy += 33 * (days / 12053);
        days %= 12053;
        jy += 4 * (days / 1461);
        days %= 1461;
        if (days > 365) { jy += (days - 1) / 365; days = (days - 1) % 365; }
        int jm, jd;
        if (days < 186) { jm = 1 + days / 31; jd = 1 + days % 31; }
        else { jm = 7 + (days - 186) / 30; jd = 1 + (days - 186) % 30; }
        return new int[]{jy, jm, jd};
    }

    public static int[] j2g(int jy, int jm, int jd) {
        int gy;
        if (jy > 979) { gy = 1600; jy -= 979; } else { gy = 621; }
        int days = 365 * jy + (jy / 33) * 8 + ((jy % 33) + 3) / 4 + 78 + jd
                + (jm < 7 ? (jm - 1) * 31 : ((jm - 7) * 30) + 186);
        gy += 400 * (days / 146097);
        days %= 146097;
        if (days > 36524) {
            gy += 100 * (--days / 36524);
            days %= 36524;
            if (days >= 365) days++;
        }
        gy += 4 * (days / 1461);
        days %= 1461;
        if (days > 365) { gy += (days - 1) / 365; days = (days - 1) % 365; }
        int gd = days + 1;
        int[] sal = {0, 31, ((gy % 4 == 0 && gy % 100 != 0) || (gy % 400 == 0)) ? 29 : 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
        int gm = 0;
        while (gm < 13 && gd > sal[gm]) { gd -= sal[gm]; gm++; }
        return new int[]{gy, gm, gd};
    }

    public static int[] ymd(long ms, boolean j) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(ms);
        int y = c.get(Calendar.YEAR), m = c.get(Calendar.MONTH) + 1, d = c.get(Calendar.DAY_OF_MONTH);
        return j ? g2j(y, m, d) : new int[]{y, m, d};
    }

    public static long ms(int y, int m, int d, int h, int mi, boolean j) {
        if (j) { int[] g = j2g(y, m, d); y = g[0]; m = g[1]; d = g[2]; }
        Calendar c = Calendar.getInstance();
        c.clear();
        c.set(y, m - 1, d, h, mi, 0);
        return c.getTimeInMillis();
    }

    public static int dim(int y, int m, boolean j) {
        if (!j) return new GregorianCalendar(y, m - 1, 1).getActualMaximum(Calendar.DAY_OF_MONTH);
        if (m <= 6) return 31;
        if (m <= 11) return 30;
        int[] g = j2g(y, 12, 30);
        int[] b = g2j(g[0], g[1], g[2]);
        return (b[0] == y && b[1] == 12 && b[2] == 30) ? 30 : 29;
    }

    public static long addMonths(long ms, int n, boolean j) {
        int[] a = ymd(ms, j);
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(ms);
        int idx = a[0] * 12 + (a[1] - 1) + n;
        int y = Math.floorDiv(idx, 12), m = Math.floorMod(idx, 12) + 1;
        return ms(y, m, Math.min(a[2], dim(y, m, j)), c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), j);
    }

    public static long addDays(long ms, int n) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(ms);
        c.add(Calendar.DAY_OF_MONTH, n);
        return c.getTimeInMillis();
    }

    /** "yyyyMMddHHmm": local wall-clock time, Gregorian, no time zone (e.g. 202611081020). */
    public static String stamp(long ms) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(ms);
        return String.format(Locale.US, "%04d%02d%02d%02d%02d", c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1,
                c.get(Calendar.DAY_OF_MONTH), c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE));
    }

    /** "yyyyMMdd" (e.g. 20261108). */
    public static String dateStamp(long ms) { return stamp(ms).substring(0, 8); }

    public static long parseStamp(String s) {
        if (s == null || !s.matches("\\d{12}")) throw new IllegalArgumentException("bad time: " + s);
        return build(s, Integer.parseInt(s.substring(8, 10)), Integer.parseInt(s.substring(10, 12)));
    }

    public static long parseDate(String s) {
        if (s == null || !s.matches("\\d{8}")) throw new IllegalArgumentException("bad date: " + s);
        return build(s, 0, 0);
    }

    private static long build(String s, int h, int mi) {
        Calendar c = Calendar.getInstance();
        c.clear();
        c.setLenient(false);
        c.set(Integer.parseInt(s.substring(0, 4)), Integer.parseInt(s.substring(4, 6)) - 1, Integer.parseInt(s.substring(6, 8)), h, mi, 0);
        return c.getTimeInMillis(); // throws IllegalArgumentException for impossible dates
    }

    public static long dayStart(long ms) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(ms);
        c.set(Calendar.HOUR_OF_DAY, 0); c.set(Calendar.MINUTE, 0); c.set(Calendar.SECOND, 0); c.set(Calendar.MILLISECOND, 0);
        return c.getTimeInMillis();
    }
}
