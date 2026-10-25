package com.mahdi.expensewallet.utils;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Typeface;

import java.util.Locale;

import static com.mahdi.expensewallet.MainActivity.activity;
import static com.mahdi.expensewallet.data.SettingsManager.lang;
import static com.mahdi.expensewallet.data.SettingsManager.saveSettings;
import static com.mahdi.expensewallet.ui.Navigator.render;
import static com.mahdi.expensewallet.ui.Theme.fonts;

/** Translations (strings.xml per language in res) and the language/font setup. */
public final class Translator {
    private Translator() { }

    public static Context langCtx;

    public static String strFor(Context base, String lang, String key) {
        try {
            Configuration c = new Configuration(base.getResources().getConfiguration());
            c.setLocale(new Locale(lang));
            Context lc = base.createConfigurationContext(c);
            int id = lc.getResources().getIdentifier(key, "string", base.getPackageName());
            if (id != 0) return lc.getResources().getString(id);
        } catch (Exception ignored) { }
        return key;
    }

    public static boolean fa() { return "fa".equals(lang); }

    public static Locale loc() { return new Locale(lang); }

    public static String t(String k) { return strFor(activity, lang, k); }

    public static String t(String k, Object... a) {
        String s = t(k);
        try { s = String.format(Locale.US, s, a); } catch (Exception ignored) { }
        return digits(s);
    }

    public static void applyLang() {
        try {
            Configuration c = new Configuration(activity.getResources().getConfiguration());
            c.setLocale(loc());
            langCtx = activity.createConfigurationContext(c);
        } catch (Exception e) { langCtx = activity; }
        String[] f = fa()
                ? new String[]{"Vazirmatn-Regular", "Vazirmatn-Medium", "Vazirmatn-Bold", "Vazirmatn-Bold"}
                : new String[]{"OpenSans-Regular", "OpenSans-SemiBold", "OpenSans-Bold", "OpenSans-ExtraBold"};
        for (int i = 0; i < 4; i++) {
            try { fonts[i] = Typeface.createFromAsset(activity.getAssets(), "fonts/" + f[i] + ".ttf"); }
            catch (Exception e) { fonts[i] = i == 0 ? Typeface.DEFAULT : Typeface.DEFAULT_BOLD; }
        }
    }

    public static void setLang(String l) {
        lang = l;
        saveSettings();
        applyLang();
        render();
    }

    /** Persian digits + separators when the app language is Persian. */
    public static String digits(String s) {
        if (!fa() || s == null) return s;
        s = s.replaceAll("(?<=\\d),(?=\\d)", "٬").replaceAll("(?<=\\d)\\.(?=\\d)", "٫");
        StringBuilder b = new StringBuilder();
        for (char ch : s.toCharArray()) b.append(ch >= '0' && ch <= '9' ? (char) ('۰' + (ch - '0')) : ch);
        return b.toString();
    }

    /** Back to the initial values (a new activity instance used to start from these). */
    public static void reset() {
        langCtx = null;
    }
}
