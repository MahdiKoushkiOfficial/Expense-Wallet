package com.mahdi.expensewallet.utils;

import android.view.ViewGroup;

/** App-wide constants: links, currencies, category lists, colors, icon names and request codes. */
public final class Constants {
    private Constants() { }

    public static final String GITHUB_URL = "https://github.com/YOUR_USERNAME";
    public static final String LINKEDIN_URL = "https://www.linkedin.com/in/YOUR_USERNAME";
    public static final String APP_VERSION = "1.0.0";
    /** One request that returns USD, EUR, CHF and IRR. Replace if you get a better IRR source. */
    public static final String RATES_URL = "https://open.er-api.com/v6/latest/USD";

    public static final String PREFS = "wallet_prefs";
    public static final String[] CURS = {"USD", "EUR", "CHF", "IRR"};
    public static final String[] CATS = {"food_drinks", "bills_fees", "transport", "groceries", "entertainment",
            "shopping", "gifts", "health", "investments", "loans", "car", "work", "home", "restaurant",
            "family", "social_life", "food_delivery", "travel", "fitness", "self_development", "clothing",
            "beauty", "education", "pet", "salary", "other"};
    public static final int[] CAT_COLORS = {0xFFF59E0B, 0xFF64748B, 0xFF0EA5E9, 0xFF22C55E, 0xFFA855F7,
            0xFFEC4899, 0xFFEF4444, 0xFF14B8A6, 0xFF6366F1, 0xFF8B5CF6, 0xFF0284C7, 0xFF78716C, 0xFFD97706,
            0xFFF97316, 0xFFE11D48, 0xFF06B6D4, 0xFFEA580C, 0xFF2563EB, 0xFF16A34A, 0xFF9333EA, 0xFFDB2777,
            0xFFC026D3, 0xFF4F46E5, 0xFF92400E, 0xFF059669, 0xFF94A3B8};
    public static final String[] INCOME_CATS = {"salary", "work", "investments", "gifts", "loans", "other"};
    public static final String[] GOALS = {"travel", "education", "emergency", "big_purchase", "other"};
    public static final String[] GOAL_ICONS = {"cat_travel", "cat_education", "ic_emergency", "ic_shopping_cart_checkout", "ic_more_horiz"};
    public static final int[] GOAL_COLORS = {0xFF2563EB, 0xFF4F46E5, 0xFFEF4444, 0xFFF59E0B, 0xFF64748B};
    public static final String[] ACC_ICONS = {"acc_credit_card", "acc_bank", "acc_cash"};
    public static final String[] ACC_KEYS = {"acc_type_credit", "acc_type_bank", "acc_type_cash"};
    public static final int[] ACC_COLORS = {0xFF7C5CFF, 0xFF2F80ED, 0xFF22A06B};
    public static final String[] JALALI_EN = {"Farvardin", "Ordibehesht", "Khordad", "Tir", "Mordad", "Shahrivar",
            "Mehr", "Aban", "Azar", "Dey", "Bahman", "Esfand"};
    public static final String[] JALALI_FA = {"فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور",
            "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند"};
    /** Income points down (money comes in), expense points up (money goes out). Used everywhere. */
    public static final String ICON_INCOME = "ic_arrow_downward", ICON_EXPENSE = "ic_arrow_upward";
    /** New icons fall back to an older one until their PNG exists in drawable-nodpi. */
    public static final String[][] ICON_FALLBACK = {{"ic_month_start", "ic_calendar_month"}, {"ic_exchange_currency", "ic_paid"},
            {"ic_daily_reminder", "ic_notifications"}, {"ic_reminder_time", "ic_calendar_month"}, {"ic_buffer_danger", "ic_warning"}};
    public static final int MATCH = ViewGroup.LayoutParams.MATCH_PARENT;
    public static final int WRAP = ViewGroup.LayoutParams.WRAP_CONTENT;
    public static final long DAY = 86400000L;

    public static final String[] PERIODS = {"daily", "weekly", "monthly"};

    public static final int REQ_EXPORT = 11, REQ_IMPORT = 12, REQ_NOTIF = 13;
}
