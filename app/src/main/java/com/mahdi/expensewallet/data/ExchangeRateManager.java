package com.mahdi.expensewallet.data;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

import org.json.JSONObject;

import static com.mahdi.expensewallet.MainActivity.activity;
import static com.mahdi.expensewallet.data.SettingsManager.base;
import static com.mahdi.expensewallet.data.SettingsManager.sharedPreferences;
import static com.mahdi.expensewallet.ui.Navigator.render;
import static com.mahdi.expensewallet.ui.Navigator.stack;
import static com.mahdi.expensewallet.ui.UiHelper.toast;
import static com.mahdi.expensewallet.utils.Constants.CURS;
import static com.mahdi.expensewallet.utils.Constants.RATES_URL;
import static com.mahdi.expensewallet.utils.FormatUtils.now;
import static com.mahdi.expensewallet.utils.Translator.t;

/** Online exchange rates and currency conversion. */
public final class ExchangeRateManager {
    private ExchangeRateManager() { }

    public static HashMap<String, Double> rates = new HashMap<>();
    public static long ratesTime = 0;
    public static boolean ratesBusy = false, ratesMissing = false;

    public static void loadRates() {
        try {
            JSONObject o = new JSONObject(sharedPreferences.getString("rates", "{}"));
            ratesTime = sharedPreferences.getLong("ratesTime", 0);
            for (String c : CURS) if (o.has(c)) rates.put(c, o.getDouble(c));
        } catch (Exception ignored) { }
    }

    public static void fetchRates(final boolean force) {
        if (ratesBusy) return;
        if (!force && now() - ratesTime < 6 * 3600 * 1000L && rates.size() >= CURS.length) return;
        ratesBusy = true;
        new Thread(() -> {
            boolean ok = false;
            try {
                HttpURLConnection c = (HttpURLConnection) new URL(RATES_URL).openConnection();
                c.setConnectTimeout(10000);
                c.setReadTimeout(10000);
                BufferedReader r = new BufferedReader(new InputStreamReader(c.getInputStream(), "UTF-8"));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = r.readLine()) != null) sb.append(line);
                r.close();
                JSONObject rt = new JSONObject(sb.toString()).getJSONObject("rates");
                JSONObject save = new JSONObject();
                HashMap<String, Double> m = new HashMap<>();
                for (String cu : CURS) {
                    double v = "USD".equals(cu) ? 1.0 : rt.getDouble(cu);
                    m.put(cu, v);
                    save.put(cu, v);
                }
                rates = m;
                ratesTime = now();
                sharedPreferences.edit().putString("rates", save.toString()).putLong("ratesTime", ratesTime).apply();
                ok = true;
            } catch (Exception ignored) { }
            final boolean fine = ok;
            activity.runOnUiThread(() -> {
                ratesBusy = false;
                if (!fine && force) toast(t("rates_failed"));
                if (fine && force) toast(t("rates_updated"));
                if (!stack.isEmpty()) render();
            });
        }).start();
    }

    /** Converts to the base currency; unavailable rates flag ratesMissing and count as 0. */
    public static double toBase(double v, String cur) {
        if (cur.equals(base)) return v;
        Double a = rates.get(cur), b = rates.get(base);
        if (a == null || b == null || a == 0) { ratesMissing = true; return 0; }
        return v / a * b;
    }

    public static double conv(double v, String from, String to) {
        if (from.equals(to)) return v;
        Double a = rates.get(from), b = rates.get(to);
        if (a == null || b == null || a == 0) return Double.NaN;
        return v / a * b;
    }

    /** Back to the initial values (a new activity instance used to start from these). */
    public static void reset() {
        rates = new HashMap<>();
        ratesTime = 0;
        ratesBusy = false;
        ratesMissing = false;
    }
}
