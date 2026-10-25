package com.mahdi.expensewallet.data;

import android.content.SharedPreferences;

import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

/** User settings (language, base currency, theme, reminder, ...) stored in SharedPreferences. */
public final class SettingsManager {
    private SettingsManager() { }

    public static SharedPreferences sharedPreferences;

    public static String uname = "", base = "USD", lang = "en";
    public static int theme = 0, calType = 0, fiscal = 1, remH = 20, remM = 0;
    public static boolean hide = false, remOn = false, onboarded = false;
    public static double buffer = 0;
    /** Optional second currency used for the approximate "≈" lines. Empty = none. */
    public static String exchCur = "";

    public static void loadSettings() {
        uname = sharedPreferences.getString("uname", "");
        base = sharedPreferences.getString("base", "USD");
        String dl = Locale.getDefault().getLanguage();
        lang = sharedPreferences.getString("lang", ("de".equals(dl) || "fa".equals(dl)) ? dl : "en");
        theme = sharedPreferences.getInt("theme", 0);
        calType = sharedPreferences.getInt("cal", "fa".equals(lang) ? 1 : 0);
        fiscal = Math.max(1, Math.min(28, sharedPreferences.getInt("fiscal", 1)));
        hide = sharedPreferences.getBoolean("hide", false);
        remOn = sharedPreferences.getBoolean("reminder", false);
        remH = sharedPreferences.getInt("remH", 20);
        remM = sharedPreferences.getInt("remM", 0);
        buffer = Double.longBitsToDouble(sharedPreferences.getLong("buffer", 0L));
        onboarded = sharedPreferences.getBoolean("onboarded", false);
        exchCur = sharedPreferences.getString("exch", "");
        if (exchCur.equals(base)) exchCur = "";
    }

    public static void saveSettings() {
        sharedPreferences.edit().putString("uname", uname).putString("base", base).putString("lang", lang)
                .putInt("theme", theme).putInt("cal", calType).putInt("fiscal", fiscal)
                .putBoolean("hide", hide).putBoolean("reminder", remOn).putInt("remH", remH)
                .putInt("remM", remM).putLong("buffer", Double.doubleToLongBits(buffer))
                .putBoolean("onboarded", onboarded).putString("exch", exchCur).apply();
    }

    public static JSONObject settingsJs() throws JSONException {
        JSONObject o = new JSONObject();
        o.put("uname", uname); o.put("base", base); o.put("lang", lang); o.put("theme", theme);
        o.put("cal", calType); o.put("fiscal", fiscal); o.put("hide", hide); o.put("reminder", remOn);
        o.put("remH", remH); o.put("remM", remM); o.put("buffer", buffer); o.put("exch", exchCur);
        return o;
    }

    /** Back to the initial values (a new activity instance used to start from these). */
    public static void reset() {
        sharedPreferences = null;
        uname = "";
        base = "USD";
        lang = "en";
        theme = 0;
        calType = 0;
        fiscal = 1;
        remH = 20;
        remM = 0;
        hide = false;
        remOn = false;
        onboarded = false;
        buffer = 0;
        exchCur = "";
    }
}
