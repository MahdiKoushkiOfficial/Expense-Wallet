package com.mahdi.expensewallet.ui;

import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.view.View;

import static com.mahdi.expensewallet.MainActivity.activity;
import static com.mahdi.expensewallet.data.SettingsManager.theme;

/** Colors, fonts and density of the current theme. */
public final class Theme {
    private Theme() { }

    public static Typeface[] fonts = new Typeface[4];
    public static boolean dark;
    public static int backgroundColor, surfaceColor, secondarySurfaceColor, textColor, secondaryTextColor, dividerColor, accentColor, incomeColor, expenseColor, warningColor;

    public static float density;

    public static int blend(int a, int b, float t) {
        int r = (int) (Color.red(a) + (Color.red(b) - Color.red(a)) * t);
        int g = (int) (Color.green(a) + (Color.green(b) - Color.green(a)) * t);
        int bl = (int) (Color.blue(a) + (Color.blue(b) - Color.blue(a)) * t);
        return Color.rgb(r, g, bl);
    }

    public static void applyTheme() {
        boolean night = (activity.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
        dark = theme == 2 || (theme == 0 && night);
        accentColor = dark ? 0xFF2DD4BF : 0xFF0D9488;
        incomeColor = dark ? 0xFF4ADE80 : 0xFF16A34A;
        expenseColor = dark ? 0xFFFB7185 : 0xFFE11D48;
        warningColor = 0xFFF59E0B;
        if (dark) {
            backgroundColor = 0xFF0D1117; surfaceColor = 0xFF171C24; secondarySurfaceColor = 0xFF222A35; textColor = 0xFFF1F5F9; secondaryTextColor = 0xFF94A3B8; dividerColor = 0xFF2A3340;
        } else {
            backgroundColor = 0xFFF3F5F9; surfaceColor = 0xFFFFFFFF; secondarySurfaceColor = 0xFFEAEEF4; textColor = 0xFF0F172A; secondaryTextColor = 0xFF64748B; dividerColor = 0xFFE2E8F0;
        }
        try {
            activity.getWindow().setBackgroundDrawable(new ColorDrawable(backgroundColor));
            activity.getWindow().setStatusBarColor(backgroundColor);
            activity.getWindow().setNavigationBarColor(backgroundColor);
            View d = activity.getWindow().getDecorView();
            int f = d.getSystemUiVisibility();
            int light = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | 0x10;
            d.setSystemUiVisibility(dark ? (f & ~light) : (f | light));
        } catch (Exception ignored) { }
    }

    /** Back to the initial values (a new activity instance used to start from these). */
    public static void reset() {
        fonts = new Typeface[4];
        dark = false;
        backgroundColor = 0;
        surfaceColor = 0;
        secondarySurfaceColor = 0;
        textColor = 0;
        secondaryTextColor = 0;
        dividerColor = 0;
        accentColor = 0;
        incomeColor = 0;
        expenseColor = 0;
        warningColor = 0;
        density = 0;
    }
}
