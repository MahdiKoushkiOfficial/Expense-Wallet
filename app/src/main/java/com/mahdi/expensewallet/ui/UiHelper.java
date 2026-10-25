package com.mahdi.expensewallet.ui;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.net.Uri;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mahdi.expensewallet.model.Account;
import com.mahdi.expensewallet.ui.Callbacks.ExUpd;
import com.mahdi.expensewallet.ui.Callbacks.IntCb;
import com.mahdi.expensewallet.ui.Callbacks.Str;

import java.util.HashMap;

import static com.mahdi.expensewallet.MainActivity.activity;
import static com.mahdi.expensewallet.data.DatabaseStorage.database;
import static com.mahdi.expensewallet.data.FinanceManager.catColor;
import static com.mahdi.expensewallet.data.FinanceManager.catIcon;
import static com.mahdi.expensewallet.data.FinanceManager.catName;
import static com.mahdi.expensewallet.data.SettingsManager.base;
import static com.mahdi.expensewallet.ui.Navigator.top;
import static com.mahdi.expensewallet.ui.Theme.accentColor;
import static com.mahdi.expensewallet.ui.Theme.density;
import static com.mahdi.expensewallet.ui.Theme.dividerColor;
import static com.mahdi.expensewallet.ui.Theme.fonts;
import static com.mahdi.expensewallet.ui.Theme.secondarySurfaceColor;
import static com.mahdi.expensewallet.ui.Theme.secondaryTextColor;
import static com.mahdi.expensewallet.ui.Theme.surfaceColor;
import static com.mahdi.expensewallet.ui.Theme.textColor;
import static com.mahdi.expensewallet.utils.Constants.ACC_ICONS;
import static com.mahdi.expensewallet.utils.Constants.ICON_FALLBACK;
import static com.mahdi.expensewallet.utils.Constants.MATCH;
import static com.mahdi.expensewallet.utils.Constants.WRAP;
import static com.mahdi.expensewallet.utils.CurrencyUtils.exch;
import static com.mahdi.expensewallet.utils.CurrencyUtils.parseAmt;
import static com.mahdi.expensewallet.utils.CurrencyUtils.sym;
import static com.mahdi.expensewallet.utils.Translator.fa;
import static com.mahdi.expensewallet.utils.Translator.t;

/** View factories and form widgets used by every page. */
public final class UiHelper {
    private UiHelper() { }

    public static int dp(float v) { return Math.round(v * density); }

    public static void toast(String s) { Toast.makeText(activity, s, Toast.LENGTH_SHORT).show(); }

    public static TextView tv(String s, float sp, int color, int style) {
        TextView v = new TextView(activity);
        v.setText(s);
        v.setTextSize(sp);
        v.setTextColor(color);
        v.setTypeface(fonts[Math.max(0, Math.min(3, style))]);
        return v;
    }

    public static LinearLayout box(boolean horiz) {
        LinearLayout l = new LinearLayout(activity);
        l.setOrientation(horiz ? LinearLayout.HORIZONTAL : LinearLayout.VERTICAL);
        return l;
    }

    public static GradientDrawable shape(int color, float rDp) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(color);
        g.setCornerRadius(dp(rDp));
        return g;
    }

    public static GradientDrawable oval(int color) {
        GradientDrawable g = new GradientDrawable();
        g.setShape(GradientDrawable.OVAL);
        g.setColor(color);
        return g;
    }

    public static <T extends View> T put(LinearLayout p, T v, int w, int h, float ms, float mt, float me, float mb) {
        LinearLayout.LayoutParams l = new LinearLayout.LayoutParams(w, h);
        l.setMarginStart(dp(ms)); l.setMarginEnd(dp(me)); l.topMargin = dp(mt); l.bottomMargin = dp(mb);
        p.addView(v, l);
        return v;
    }

    public static <T extends View> T putW(LinearLayout p, T v, float weight, float ms, float me) {
        LinearLayout.LayoutParams l = new LinearLayout.LayoutParams(0, WRAP, weight);
        l.setMarginStart(dp(ms)); l.setMarginEnd(dp(me));
        p.addView(v, l);
        return v;
    }

    public static <T extends View> T put(LinearLayout p, T v, float mt, float mb) { return put(p, v, MATCH, WRAP, 0, mt, 0, mb); }

    public static void click(View v, final Runnable r) {
        Drawable bg = v.getBackground();
        v.setBackground(new RippleDrawable(ColorStateList.valueOf(0x30808080), bg, bg == null ? new ColorDrawable(Color.WHITE) : null));
        v.setClickable(true);
        v.setOnClickListener(x -> r.run());
    }

    public static LinearLayout card() {
        LinearLayout c = box(false);
        c.setBackground(shape(surfaceColor, 20));
        c.setPadding(dp(16), dp(16), dp(16), dp(16));
        return c;
    }

    public static TextView btn(String text, int bg, int fg, Runnable r) {
        TextView b = tv(text, 15, fg, 2);
        b.setGravity(Gravity.CENTER);
        b.setPadding(dp(18), dp(16), dp(18), dp(16));
        b.setMinHeight(dp(52));
        b.setBackground(shape(bg, 14));
        click(b, r);
        return b;
    }

    public static final HashMap<String, Integer> iconIds = new HashMap<>();

    public static Drawable drw(String name) {
        Integer id = iconIds.get(name);
        if (id == null) {
            int i = activity.getResources().getIdentifier(name + "_light", "drawable", activity.getPackageName());
            if (i == 0) i = activity.getResources().getIdentifier(name, "drawable", activity.getPackageName());
            if (i == 0) for (String[] fb : ICON_FALLBACK) if (fb[0].equals(name)) {
                i = activity.getResources().getIdentifier(fb[1] + "_light", "drawable", activity.getPackageName());
                break;
            }
            id = i;
            iconIds.put(name, id);
        }
        if (id == 0) return null;
        return activity.getResources().getDrawable(id, activity.getTheme());
    }

    public static ImageView ico(String name, float sizeDp, int tint) {
        ImageView v = new ImageView(activity);
        Drawable d = drw(name);
        if (d != null) { v.setImageDrawable(d); v.setColorFilter(tint, PorterDuff.Mode.SRC_IN); }
        v.setScaleType(ImageView.ScaleType.FIT_CENTER);
        v.setLayoutParams(new LinearLayout.LayoutParams(dp(sizeDp), dp(sizeDp)));
        return v;
    }

    public static FrameLayout circ(String icon, float sizeDp, int color) {
        FrameLayout f = new FrameLayout(activity);
        f.setBackground(oval(color));
        float in = sizeDp * 0.55f;
        ImageView i = ico(icon, in, Color.WHITE);
        FrameLayout.LayoutParams l = new FrameLayout.LayoutParams(dp(in), dp(in));
        l.gravity = Gravity.CENTER;
        f.addView(i, l);
        f.setLayoutParams(new LinearLayout.LayoutParams(dp(sizeDp), dp(sizeDp)));
        return f;
    }

    public static ImageView actIcon(String name, final Runnable r) {
        ImageView v = ico(name, 24, textColor);
        v.setPadding(dp(8), dp(8), dp(8), dp(8));
        v.setLayoutParams(new LinearLayout.LayoutParams(dp(40), dp(40)));
        click(v, r);
        return v;
    }

    public static void header(LinearLayout c, String title, boolean back, View... acts) {
        LinearLayout h = box(true);
        h.setGravity(Gravity.CENTER_VERTICAL);
        if (back) {
            ImageView b = ico("ic_arrow_back", 24, textColor);
            b.setPadding(dp(8), dp(8), dp(8), dp(8));
            b.setScaleX(fa() ? -1 : 1);
            click(b, () -> top().back());
            put(h, b, dp(40), dp(40), 0, 0, 4, 0);
        }
        TextView tt = tv(title, 22, textColor, 3);
        tt.setSingleLine(true);
        tt.setEllipsize(TextUtils.TruncateAt.END);
        putW(h, tt, 1f, 0, 0);
        for (View a : acts) h.addView(a);
        put(c, h, 4, 12);
    }

    public static TextView section(String s) {
        TextView v = tv(s, 13, secondaryTextColor, 2);
        v.setAllCaps(false);
        return v;
    }

    public static void amountText(TextView v, String s, float maxSp) {
        float sp = maxSp;
        int n = s.length();
        if (n > 9) sp = maxSp * 9f / n;
        if (sp < maxSp * 0.5f) sp = maxSp * 0.5f;
        v.setTextSize(sp);
        v.setText(s);
        v.setSingleLine(true);
        v.setTextDirection(View.TEXT_DIRECTION_LTR);
    }

    public static EditText edit(String hint, String init, int type, final Str cb) {
        EditText e = new EditText(activity);
        e.setHint(hint);
        e.setText(init);
        e.setInputType(type);
        e.setTextColor(textColor);
        e.setHintTextColor(secondaryTextColor);
        e.setTypeface(fonts[0]);
        e.setTextSize(16);
        e.setBackground(shape(secondarySurfaceColor, 12));
        e.setPadding(dp(14), dp(12), dp(14), dp(12));
        e.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int a, int b, int c) { }
            public void onTextChanged(CharSequence s, int a, int b, int c) { }
            public void afterTextChanged(Editable s) { if (cb != null) cb.f(s.toString()); }
        });
        return e;
    }

    public static int NUM = InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL;
    public static int NUMS = InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED;
    public static int TXT = InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES;

    public static View chip(String text, boolean sel, Runnable r) {
        TextView c = tv(text, 14, sel ? Color.WHITE : textColor, 1);
        c.setPadding(dp(16), dp(9), dp(16), dp(9));
        c.setBackground(shape(sel ? accentColor : secondarySurfaceColor, 20));
        click(c, r);
        return c;
    }

    public static HorizontalScrollView chips(String[] labels, int sel, final IntCb cb) {
        HorizontalScrollView hs = new HorizontalScrollView(activity);
        hs.setHorizontalScrollBarEnabled(false);
        LinearLayout row = box(true);
        for (int i = 0; i < labels.length; i++) {
            final int k = i;
            put(row, chip(labels[i], i == sel, () -> cb.f(k)), WRAP, WRAP, 0, 0, 8, 0);
        }
        hs.addView(row);
        return hs;
    }

    public static View progressBar(double ratio, int color) {
        int fill = (int) Math.max(2, Math.min(100, Math.round(ratio * 100)));
        LinearLayout pb = box(true);
        pb.setWeightSum(100);
        pb.setBackground(shape(secondarySurfaceColor, 5));
        View fv = new View(activity);
        fv.setBackground(shape(color, 5));
        pb.addView(fv, new LinearLayout.LayoutParams(0, dp(10), fill));
        if (fill < 100) pb.addView(new View(activity), new LinearLayout.LayoutParams(0, dp(10), 100 - fill));
        return pb;
    }

    /** Equal-width segmented buttons with centered content (Expense/Income, Daily/Weekly/Monthly...). */
    public static View seg(String[] labels, int sel, int[] colors, final IntCb cb) { return seg(labels, sel, colors, null, cb); }

    public static View seg(String[] labels, int sel, int[] colors, String[] icons, final IntCb cb) {
        LinearLayout row = box(true);
        for (int i = 0; i < labels.length; i++) {
            final int k = i;
            boolean on = i == sel;
            LinearLayout p = box(true);
            p.setGravity(Gravity.CENTER);
            p.setMinimumHeight(dp(58));
            p.setPadding(dp(8), dp(12), dp(8), dp(12));
            p.setBackground(shape(on ? colors[Math.min(i, colors.length - 1)] : secondarySurfaceColor, 14));
            if (icons != null) put(p, ico(icons[i], 20, on ? Color.WHITE : secondaryTextColor), dp(20), dp(20), 0, 0, 8, 0);
            TextView lb = tv(labels[i], 15, on ? Color.WHITE : textColor, 2);
            lb.setGravity(Gravity.CENTER);
            p.addView(lb);
            click(p, () -> cb.f(k));
            putW(row, p, 1f, i == 0 ? 0 : 4, i == labels.length - 1 ? 0 : 4);
        }
        return row;
    }

    /** Small label with a leading icon (used for Income / Expense captions). */
    public static View iconLabel(String icon, String text, float sp, int color) {
        LinearLayout r = box(true);
        r.setGravity(Gravity.CENTER_VERTICAL);
        put(r, ico(icon, sp + 3, color), dp(sp + 3), dp(sp + 3), 0, 0, 4, 0);
        r.addView(tv(text, sp, color, 1));
        return r;
    }

    /** Right-hand amount column: main amount + (optional) approximate value in the exchange currency below it. */
    public static LinearLayout amtCol(String main, int color, float sp, double v, String cur, boolean secret) {
        LinearLayout am = box(false);
        am.setGravity(Gravity.END);
        TextView av = tv("", sp, color, 2);
        amountText(av, main, sp);
        am.addView(av);
        String ex = secret ? "" : exch(v, cur);
        if (!ex.isEmpty()) {
            TextView cv = tv(ex, 11, secondaryTextColor, 0);
            cv.setTextDirection(View.TEXT_DIRECTION_LTR);
            am.addView(cv);
        }
        return am;
    }

    /** Adds a centered live "≈ exchange" line under an amount card; call upd(text) as the user types. */
    public static ExUpd exLine(LinearLayout card, final String cur) {
        final TextView x = tv("", 13, secondaryTextColor, 1);
        x.setGravity(Gravity.CENTER);
        x.setTextDirection(View.TEXT_DIRECTION_LTR);
        x.setVisibility(View.GONE);
        card.addView(x, new LinearLayout.LayoutParams(MATCH, WRAP));
        return txt -> {
            double v = parseAmt(txt);
            String ex = v > 0 ? exch(v, cur) : "";
            x.setText(ex);
            x.setVisibility(ex.isEmpty() ? View.GONE : View.VISIBLE);
        };
    }

    /** Amount card: number centered, currency symbol stays in the corner, both vertically centered. */
    public static LinearLayout amountCard(EditText ae, String symbol) {
        LinearLayout card = card();
        FrameLayout f = new FrameLayout(activity);
        ae.setBackground(null);
        ae.setTextSize(30);
        ae.setTypeface(fonts[3]);
        ae.setGravity(Gravity.CENTER);
        ae.setIncludeFontPadding(false);
        ae.setTextDirection(View.TEXT_DIRECTION_LTR);
        ae.setPadding(dp(64), 0, dp(64), 0);
        f.addView(ae, new FrameLayout.LayoutParams(MATCH, dp(64)));
        TextView sy = tv(symbol, 22, secondaryTextColor, 2);
        sy.setGravity(Gravity.CENTER_VERTICAL);
        sy.setIncludeFontPadding(false);
        FrameLayout.LayoutParams l = new FrameLayout.LayoutParams(WRAP, dp(64), Gravity.END);
        l.setMarginEnd(dp(2));
        f.addView(sy, l);
        card.addView(f, new LinearLayout.LayoutParams(MATCH, WRAP));
        return card;
    }

    /** Single category card (icon + name). Tapping opens the category picker page. */
    public static View catCard(String cat, Runnable open) {
        LinearLayout r = box(true);
        r.setGravity(Gravity.CENTER_VERTICAL);
        r.setBackground(shape(surfaceColor, 16));
        r.setPadding(dp(14), dp(12), dp(14), dp(12));
        put(r, circ(catIcon(cat), 44, catColor(cat)), dp(44), dp(44), 0, 0, 12, 0);
        putW(r, tv(catName(cat), 16, textColor, 2), 1f, 0, 8);
        ImageView ch = ico("ic_chevron_right", 22, secondaryTextColor);
        ch.setScaleX(fa() ? -1 : 1);
        r.addView(ch);
        click(r, open);
        return r;
    }

    /** Category list row: icon + name (+ optional small line). Callers add right-side views. */
    public static LinearLayout catRow(String k, String sub) {
        LinearLayout r = box(true);
        r.setGravity(Gravity.CENTER_VERTICAL);
        r.setBackground(shape(surfaceColor, 16));
        r.setPadding(dp(12), dp(12), dp(14), dp(12));
        put(r, circ(catIcon(k), 44, catColor(k)), dp(44), dp(44), 0, 0, 12, 0);
        LinearLayout col = box(false);
        col.addView(tv(catName(k), 15, textColor, 2));
        if (sub != null) col.addView(tv(sub, 12, secondaryTextColor, 0));
        putW(r, col, 1f, 0, 8);
        return r;
    }

    /** Account chips: type icon + name + currency symbol. */
    public static View accountChips(String selId, final Str cb) {
        HorizontalScrollView hs = new HorizontalScrollView(activity);
        hs.setHorizontalScrollBarEnabled(false);
        LinearLayout row = box(true);
        for (final Account a : database.accounts) {
            boolean on = a.id.equals(selId);
            LinearLayout ch = box(true);
            ch.setGravity(Gravity.CENTER_VERTICAL);
            ch.setPadding(dp(14), dp(10), dp(16), dp(10));
            ch.setBackground(shape(on ? accentColor : secondarySurfaceColor, 22));
            put(ch, ico(ACC_ICONS[a.type], 18, on ? Color.WHITE : secondaryTextColor), dp(18), dp(18), 0, 0, 8, 0);
            ch.addView(tv(a.name + " · " + sym(base), 14, on ? Color.WHITE : textColor, 1));
            click(ch, () -> cb.f(a.id));
            put(row, ch, WRAP, WRAP, 0, 0, 8, 0);
        }
        hs.addView(row);
        return hs;
    }

    public static TextView saveBtn(Runnable r) {
        TextView b = btn(t("save"), accentColor, Color.WHITE, r);
        b.setTextSize(17);
        b.setMinHeight(dp(60));
        b.setPadding(dp(18), dp(18), dp(18), dp(18));
        return b;
    }

    public static View dateRow(String label, String value, Runnable r) {
        LinearLayout row = box(true);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, dp(10), 0, dp(10));
        putW(row, tv(label, 14, secondaryTextColor, 1), 1f, 0, 0);
        row.addView(tv(value, 15, accentColor, 2));
        click(row, r);
        return row;
    }

    public static View sep() {
        View v = new View(activity);
        v.setBackgroundColor(dividerColor);
        v.setLayoutParams(new LinearLayout.LayoutParams(MATCH, 1));
        return v;
    }

    public static View settingRow(String icon, String title, String value, Runnable r) {
        LinearLayout row = box(true);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(4), dp(12), dp(4), dp(12));
        put(row, ico(icon, 24, accentColor), dp(24), dp(24), 0, 0, 14, 0);
        LinearLayout col = box(false);
        col.addView(tv(title, 15, textColor, 1));
        if (value != null && !value.isEmpty()) col.addView(tv(value, 12, secondaryTextColor, 0));
        putW(row, col, 1f, 0, 0);
        if (r != null) click(row, r);
        return row;
    }

    /** Period navigation row. Returns the row; callbacks change the offset. */
    public static View periodNav(String label, final Runnable prev, final Runnable next, final Runnable tap) {
        LinearLayout r = box(true);
        r.setGravity(Gravity.CENTER_VERTICAL);
        ImageView l = actIcon("ic_chevron_left", prev);
        l.setScaleX(fa() ? -1 : 1);
        ImageView n = actIcon("ic_chevron_right", next);
        n.setScaleX(fa() ? -1 : 1);
        r.addView(l);
        TextView t = tv(label, 16, textColor, 2);
        t.setGravity(Gravity.CENTER);
        if (tap != null) click(t, tap);
        t.setPadding(dp(8), dp(8), dp(8), dp(8));
        putW(r, t, 1f, 0, 0);
        r.addView(n);
        return r;
    }

    public static void openUrl(String u) {
        try { activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(u))); } catch (Exception e) { toast(u); }
    }

    public static View settingRowCard(String title, String value, Runnable r) {
        LinearLayout c = card();
        c.setPadding(dp(14), dp(8), dp(14), dp(8));
        c.addView(settingRow("ic_paid", title, value, r));
        return c;
    }

    /** Back to the initial values (a new activity instance used to start from these). */
    public static void reset() {
        iconIds.clear();
        NUM = InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL;
        NUMS = InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED;
        TXT = InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES;
    }
}
