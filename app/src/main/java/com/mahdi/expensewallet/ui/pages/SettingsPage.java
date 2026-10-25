package com.mahdi.expensewallet.ui.pages;

import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;

import com.mahdi.expensewallet.ui.components.M3Switch;
import com.mahdi.expensewallet.utils.CalendarUtils;

import java.util.Arrays;
import java.util.Calendar;

import static com.mahdi.expensewallet.MainActivity.activity;
import static com.mahdi.expensewallet.data.BackupManager.startExport;
import static com.mahdi.expensewallet.data.BackupManager.startImport;
import static com.mahdi.expensewallet.data.ExchangeRateManager.fetchRates;
import static com.mahdi.expensewallet.data.ExchangeRateManager.ratesBusy;
import static com.mahdi.expensewallet.data.ExchangeRateManager.ratesTime;
import static com.mahdi.expensewallet.data.SettingsManager.base;
import static com.mahdi.expensewallet.data.SettingsManager.calType;
import static com.mahdi.expensewallet.data.SettingsManager.exchCur;
import static com.mahdi.expensewallet.data.SettingsManager.fiscal;
import static com.mahdi.expensewallet.data.SettingsManager.lang;
import static com.mahdi.expensewallet.data.SettingsManager.remH;
import static com.mahdi.expensewallet.data.SettingsManager.remM;
import static com.mahdi.expensewallet.data.SettingsManager.remOn;
import static com.mahdi.expensewallet.data.SettingsManager.saveSettings;
import static com.mahdi.expensewallet.data.SettingsManager.theme;
import static com.mahdi.expensewallet.data.SettingsManager.uname;
import static com.mahdi.expensewallet.notification.NotificationUtils.hasNotifPermission;
import static com.mahdi.expensewallet.notification.NotificationUtils.requestNotifPermission;
import static com.mahdi.expensewallet.notification.NotificationUtils.schedule;
import static com.mahdi.expensewallet.ui.Dialogs.confirm;
import static com.mahdi.expensewallet.ui.Dialogs.inputDlg;
import static com.mahdi.expensewallet.ui.Dialogs.listDlg;
import static com.mahdi.expensewallet.ui.Dialogs.pickTime;
import static com.mahdi.expensewallet.ui.Navigator.push;
import static com.mahdi.expensewallet.ui.Navigator.render;
import static com.mahdi.expensewallet.ui.SettingsActions.chooseBase;
import static com.mahdi.expensewallet.ui.SettingsActions.chooseExchange;
import static com.mahdi.expensewallet.ui.SettingsActions.fiscalDlg;
import static com.mahdi.expensewallet.ui.SettingsActions.testNotification;
import static com.mahdi.expensewallet.ui.SettingsActions.wipeAll;
import static com.mahdi.expensewallet.ui.Theme.accentColor;
import static com.mahdi.expensewallet.ui.Theme.secondaryTextColor;
import static com.mahdi.expensewallet.ui.Theme.textColor;
import static com.mahdi.expensewallet.ui.UiHelper.TXT;
import static com.mahdi.expensewallet.ui.UiHelper.box;
import static com.mahdi.expensewallet.ui.UiHelper.card;
import static com.mahdi.expensewallet.ui.UiHelper.dp;
import static com.mahdi.expensewallet.ui.UiHelper.header;
import static com.mahdi.expensewallet.ui.UiHelper.ico;
import static com.mahdi.expensewallet.ui.UiHelper.put;
import static com.mahdi.expensewallet.ui.UiHelper.putW;
import static com.mahdi.expensewallet.ui.UiHelper.section;
import static com.mahdi.expensewallet.ui.UiHelper.sep;
import static com.mahdi.expensewallet.ui.UiHelper.settingRow;
import static com.mahdi.expensewallet.ui.UiHelper.tv;
import static com.mahdi.expensewallet.utils.Constants.APP_VERSION;
import static com.mahdi.expensewallet.utils.CurrencyUtils.curLabel;
import static com.mahdi.expensewallet.utils.FormatUtils.dateStr;
import static com.mahdi.expensewallet.utils.FormatUtils.jal;
import static com.mahdi.expensewallet.utils.FormatUtils.timeStr;
import static com.mahdi.expensewallet.utils.Translator.setLang;
import static com.mahdi.expensewallet.utils.Translator.t;

public class SettingsPage extends TabPage {
    View group(View... rows) {
        LinearLayout g = card();
        g.setPadding(dp(14), dp(4), dp(14), dp(4));
        for (int i = 0; i < rows.length; i++) {
            if (i > 0) g.addView(sep());
            g.addView(rows[i]);
        }
        return g;
    }

    @Override public void build(LinearLayout c) {
        header(c, t("settings"), false);
        final String[] themes = {t("theme_auto"), t("theme_light"), t("theme_dark")};
        final String[] langs = {"English", "Deutsch", "فارسی"};
        final String[] lcodes = {"en", "de", "fa"};
        c.addView(section(t("general")));
        put(c, group(
                settingRow("ic_person", t("your_name"), uname.isEmpty() ? t("not_set") : uname,
                        () -> inputDlg(t("your_name"), t("name_hint"), uname, TXT, s -> { uname = s.trim(); saveSettings(); render(); })),
                settingRow("ic_language", t("language"), langs[Math.max(0, Arrays.asList(lcodes).indexOf(lang))],
                        () -> listDlg(t("language"), langs, i -> setLang(lcodes[i])))
        ), 6, 14);
        c.addView(section(t("appearance")));
        put(c, group(
                settingRow(theme == 2 ? "ic_dark_mode" : theme == 1 ? "ic_light_mode" : "ic_brightness_auto", t("theme"), themes[theme],
                        () -> listDlg(t("theme"), themes, i -> { theme = i; saveSettings(); render(); })),
                settingRow("ic_calendar_month", t("calendar"), t(jal() ? "cal_jalali" : "cal_gregorian"),
                        () -> listDlg(t("calendar"), new String[]{t("cal_gregorian"), t("cal_jalali")}, i -> { calType = i; saveSettings(); render(); })),
                settingRow("ic_month_start", t("fiscal_start"), t("fiscal_start_value", fiscal), () -> fiscalDlg())
        ), 6, 14);
        c.addView(section(t("currency")));
        put(c, group(
                settingRow("ic_currency_exchange", t("base_currency"), curLabel(base), () -> chooseBase()),
                settingRow("ic_exchange_currency", t("exchange_currency"), exchCur.isEmpty() ? t("none") : curLabel(exchCur), () -> chooseExchange()),
                settingRow("ic_repeat", t("update_rates"),
                        ratesTime == 0 ? t("rates_never") : t("rates_updated_at", dateStr(ratesTime) + " " + timeStr(ratesTime)) + (ratesBusy ? " …" : ""),
                        () -> fetchRates(true))
        ), 6, 14);
        c.addView(section(t("reminders")));
        LinearLayout rem = box(true);
        rem.setGravity(Gravity.CENTER_VERTICAL);
        rem.setPadding(dp(4), dp(10), dp(4), dp(10));
        put(rem, ico("ic_daily_reminder", 24, accentColor), dp(24), dp(24), 0, 0, 14, 0);
        LinearLayout rc = box(false);
        rc.addView(tv(t("daily_reminder"), 15, textColor, 1));
        rc.addView(tv(t("daily_reminder_sub"), 12, secondaryTextColor, 0));
        putW(rem, rc, 1f, 0, 8);
        M3Switch sw = new M3Switch(activity);
        sw.setChecked(remOn);
        sw.setOnChange(v -> {
            remOn = v;
            saveSettings();
            if (v && !hasNotifPermission()) requestNotifPermission();
            schedule(activity, v, remH, remM);
            render();
        });
        rem.addView(sw);
        View timeRow = settingRow("ic_reminder_time", t("reminder_time"), timeStr(CalendarUtils.ms(2000, 1, 1, remH, remM, false)), () ->
                pickTime(CalendarUtils.ms(2000, 1, 1, remH, remM, false), l -> {
                    Calendar cl = Calendar.getInstance();
                    cl.setTimeInMillis(l);
                    remH = cl.get(Calendar.HOUR_OF_DAY);
                    remM = cl.get(Calendar.MINUTE);
                    saveSettings();
                    if (remOn) schedule(activity, true, remH, remM);
                    render();
                }));
        View testRow = settingRow("ic_notifications", t("test_notification"), t("test_notification_sub"), () -> testNotification());
        put(c, remOn ? group(rem, timeRow, testRow) : group(rem, testRow), 6, 14);
        c.addView(section(t("data")));
        put(c, group(
                settingRow("ic_upload", t("export_backup"), t("export_backup_sub"), () -> startExport()),
                settingRow("ic_download", t("restore_backup"), t("restore_backup_sub"), () -> startImport()),
                settingRow("ic_delete_forever", t("delete_all"), t("delete_all_sub"), () -> confirm(t("delete_all"), t("delete_all_q"), t("delete"), () -> wipeAll()))
        ), 6, 14);
        c.addView(section(t("about")));
        put(c, group(
                settingRow("ic_privacy_tip", t("privacy_policy"), null, () -> push(new InfoPage("privacy_policy", "privacy_body"))),
                settingRow("ic_new_releases", t("versions"), t("version_n", APP_VERSION), () -> push(new InfoPage("versions", "versions_body"))),
                settingRow("ic_copyright", t("attributions"), null, () -> push(new InfoPage("attributions", "attributions_body"))),
                settingRow("ic_info", t("developer"), null, () -> push(new DeveloperPage()))
        ), 6, 8);
    }
}
