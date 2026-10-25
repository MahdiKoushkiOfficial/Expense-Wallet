package com.mahdi.expensewallet.utils;

import java.util.Arrays;

import org.json.JSONException;

import static com.mahdi.expensewallet.utils.Constants.CATS;
import static com.mahdi.expensewallet.utils.Constants.PERIODS;

/** Validation helpers used while reading JSON (database and backups). */
public final class ValidationUtils {
    private ValidationUtils() { }

    public static int periodIndex(String s) throws JSONException {
        for (int i = 0; i < PERIODS.length; i++) if (PERIODS[i].equals(s)) return i;
        throw new JSONException("bad period: " + s);
    }

    public static void checkCat(String cat) throws JSONException {
        if (!Arrays.asList(CATS).contains(cat)) throw new JSONException("unknown category: " + cat);
    }
}
