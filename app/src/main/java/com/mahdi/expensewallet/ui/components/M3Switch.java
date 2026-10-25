package com.mahdi.expensewallet.ui.components;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.View;

import com.mahdi.expensewallet.ui.Callbacks.OnChange;

import static com.mahdi.expensewallet.ui.Theme.accentColor;
import static com.mahdi.expensewallet.ui.Theme.blend;
import static com.mahdi.expensewallet.ui.Theme.dark;
import static com.mahdi.expensewallet.ui.UiHelper.dp;

/** Material Design 3 style switch drawn in code: clearly visible on and off, in light and dark themes. */
public class M3Switch extends View {
    public boolean on;
    public float pos;
    public OnChange listener;
    public final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
    public final RectF rf = new RectF();
    public android.animation.ValueAnimator anim;

    public M3Switch(Context c) {
        super(c);
        setClickable(true);
    }

    public void setChecked(boolean v) { on = v; pos = v ? 1f : 0f; invalidate(); }

    public boolean isChecked() { return on; }

    public void setOnChange(OnChange l) { listener = l; }

    @Override protected void onMeasure(int w, int h) { setMeasuredDimension(dp(52), dp(32)); }

    @Override public boolean performClick() {
        on = !on;
        if (anim != null) anim.cancel();
        anim = android.animation.ValueAnimator.ofFloat(pos, on ? 1f : 0f);
        anim.setDuration(160);
        anim.addUpdateListener(a -> { pos = (Float) a.getAnimatedValue(); invalidate(); });
        anim.start();
        if (listener != null) listener.f(on);
        return super.performClick();
    }

    @Override protected void onDraw(Canvas cv) {
        float w = getWidth(), h = getHeight(), t = pos;
        int offTrack = dark ? 0xFF2B3340 : 0xFFE3E8EF;
        int outline = dark ? 0xFF9AA7B8 : 0xFF6B7788;
        rf.set(dp(1), dp(1), w - dp(1), h - dp(1));
        p.setStyle(Paint.Style.FILL);
        p.setColor(blend(offTrack, accentColor, t));
        cv.drawRoundRect(rf, h / 2, h / 2, p);
        if (t < 1f) {
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(dp(2));
            p.setColor((outline & 0x00FFFFFF) | ((int) (255 * (1f - t)) << 24));
            cv.drawRoundRect(rf, h / 2, h / 2, p);
        }
        float r = dp(8) + (dp(12) - dp(8)) * t;
        float x = dp(16) + (w - dp(32)) * t;
        if (getLayoutDirection() == View.LAYOUT_DIRECTION_RTL) x = w - x;
        p.setStyle(Paint.Style.FILL);
        p.setColor(blend(outline, dark ? 0xFF00201D : Color.WHITE, t));
        cv.drawCircle(x, h / 2, r, p);
    }
}
