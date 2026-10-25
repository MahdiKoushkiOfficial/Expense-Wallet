package com.mahdi.expensewallet.ui.components;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.View;

import static com.mahdi.expensewallet.ui.Theme.fonts;
import static com.mahdi.expensewallet.ui.Theme.secondarySurfaceColor;
import static com.mahdi.expensewallet.ui.Theme.secondaryTextColor;
import static com.mahdi.expensewallet.ui.Theme.textColor;

public class PieView extends View {
    public float[] v = new float[0];
    public int[] col = new int[0];
    public String center = "", label = "";
    public final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);

    public PieView(Context c) { super(c); }

    @Override protected void onDraw(Canvas cv) {
        float w = getWidth(), h = getHeight(), size = Math.min(w, h), sw = size * 0.17f;
        RectF r = new RectF(w / 2 - size / 2 + sw / 2, h / 2 - size / 2 + sw / 2, w / 2 + size / 2 - sw / 2, h / 2 + size / 2 - sw / 2);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(sw);
        float tot = 0;
        for (float x : v) tot += x;
        if (tot <= 0) {
            p.setColor(secondarySurfaceColor);
            cv.drawArc(r, 0, 360, false, p);
        } else {
            float a = -90;
            for (int i = 0; i < v.length; i++) {
                float sweep = v[i] / tot * 360f;
                p.setColor(col[i]);
                cv.drawArc(r, a, Math.max(0.6f, sweep - (v.length > 1 ? 1.5f : 0)), false, p);
                a += sweep;
            }
        }
        p.setStyle(Paint.Style.FILL);
        p.setTextAlign(Paint.Align.CENTER);
        p.setTypeface(fonts[3]);
        p.setColor(textColor);
        float ts = size * 0.1f;
        if (center.length() > 9) ts *= 9f / center.length();
        p.setTextSize(Math.max(ts, size * 0.05f));
        cv.drawText(center, w / 2, h / 2 + ts * 0.2f, p);
        p.setTypeface(fonts[1]);
        p.setColor(secondaryTextColor);
        p.setTextSize(size * 0.06f);
        cv.drawText(label, w / 2, h / 2 + ts * 0.2f + size * 0.1f, p);
    }
}
