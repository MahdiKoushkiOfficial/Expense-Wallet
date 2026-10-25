package com.mahdi.expensewallet.ui.pages;

import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import static com.mahdi.expensewallet.ui.Theme.secondaryTextColor;
import static com.mahdi.expensewallet.ui.Theme.surfaceColor;
import static com.mahdi.expensewallet.ui.Theme.textColor;
import static com.mahdi.expensewallet.ui.UiHelper.box;
import static com.mahdi.expensewallet.ui.UiHelper.card;
import static com.mahdi.expensewallet.ui.UiHelper.click;
import static com.mahdi.expensewallet.ui.UiHelper.dp;
import static com.mahdi.expensewallet.ui.UiHelper.header;
import static com.mahdi.expensewallet.ui.UiHelper.ico;
import static com.mahdi.expensewallet.ui.UiHelper.openUrl;
import static com.mahdi.expensewallet.ui.UiHelper.put;
import static com.mahdi.expensewallet.ui.UiHelper.putW;
import static com.mahdi.expensewallet.ui.UiHelper.shape;
import static com.mahdi.expensewallet.ui.UiHelper.tv;
import static com.mahdi.expensewallet.utils.Constants.GITHUB_URL;
import static com.mahdi.expensewallet.utils.Constants.LINKEDIN_URL;
import static com.mahdi.expensewallet.utils.Translator.t;

public class DeveloperPage extends Page {
    @Override public void build(LinearLayout c) {
        header(c, t("developer"), true);
        LinearLayout cd = card();
        TextView b = tv(t("developer_body"), 14, textColor, 0);
        b.setLineSpacing(0, 1.25f);
        cd.addView(b);
        put(c, cd, 0, 14);
        put(c, linkRow("ic_github", "GitHub", GITHUB_URL), 0, 10);
        put(c, linkRow("ic_linkedin", "LinkedIn", LINKEDIN_URL), 0, 10);
    }

    View linkRow(String icon, String label, final String url) {
        LinearLayout r = box(true);
        r.setGravity(Gravity.CENTER_VERTICAL);
        r.setBackground(shape(surfaceColor, 16));
        r.setPadding(dp(16), dp(16), dp(16), dp(16));
        put(r, ico(icon, 28, textColor), dp(28), dp(28), 0, 0, 14, 0);
        LinearLayout col = box(false);
        col.addView(tv(label, 16, textColor, 2));
        TextView u = tv(url, 12, secondaryTextColor, 0);
        u.setSingleLine(true);
        u.setEllipsize(TextUtils.TruncateAt.END);
        u.setTextDirection(View.TEXT_DIRECTION_LTR);
        col.addView(u);
        putW(r, col, 1f, 0, 8);
        r.addView(ico("ic_open_in_new", 20, secondaryTextColor));
        click(r, () -> openUrl(url));
        return r;
    }
}
