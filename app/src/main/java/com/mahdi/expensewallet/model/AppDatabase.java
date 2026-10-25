package com.mahdi.expensewallet.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static com.mahdi.expensewallet.utils.Constants.PERIODS;
import static com.mahdi.expensewallet.utils.ValidationUtils.checkCat;
import static com.mahdi.expensewallet.utils.ValidationUtils.periodIndex;

public class AppDatabase {
    public ArrayList<Account> accounts = new ArrayList<>();
    public ArrayList<Transaction> transactions = new ArrayList<>();
    public ArrayList<PlannedPayment> plannedPayments = new ArrayList<>();
    public ArrayList<Saving> savings = new ArrayList<>();
    /** category -> {periodType(0 daily, 1 weekly, 2 monthly), limit in base currency}. In the file: {"period": "monthly", "limit": 88.5} */
    public LinkedHashMap<String, double[]> budgets = new LinkedHashMap<>();

    public JSONObject js() throws JSONException {
        JSONObject o = new JSONObject();
        JSONArray a = new JSONArray();
        for (Account x : accounts) a.put(x.js());
        o.put("accounts", a);
        a = new JSONArray();
        for (Transaction x : transactions) a.put(x.js());
        o.put("transactions", a);
        a = new JSONArray();
        for (PlannedPayment x : plannedPayments) a.put(x.js());
        o.put("plannedPayments", a);
        a = new JSONArray();
        for (Saving x : savings) a.put(x.js());
        o.put("savings", a);
        JSONObject b = new JSONObject();
        for (Map.Entry<String, double[]> e : budgets.entrySet()) {
            JSONObject v = new JSONObject();
            v.put("period", PERIODS[(int) e.getValue()[0]]);
            v.put("limit", e.getValue()[1]);
            b.put(e.getKey(), v);
        }
        o.put("budgets", b);
        return o;
    }

    public static AppDatabase of(JSONObject o) throws JSONException {
        AppDatabase database = new AppDatabase();
        JSONArray a = o.optJSONArray("accounts");
        for (int i = 0; a != null && i < a.length(); i++) database.accounts.add(Account.of(a.getJSONObject(i)));
        a = o.optJSONArray("transactions");
        for (int i = 0; a != null && i < a.length(); i++) database.transactions.add(Transaction.of(a.getJSONObject(i)));
        a = o.optJSONArray("plannedPayments");
        for (int i = 0; a != null && i < a.length(); i++) database.plannedPayments.add(PlannedPayment.of(a.getJSONObject(i)));
        a = o.optJSONArray("savings");
        for (int i = 0; a != null && i < a.length(); i++) database.savings.add(Saving.of(a.getJSONObject(i)));
        JSONObject b = o.optJSONObject("budgets");
        if (b != null) {
            java.util.Iterator<String> it = b.keys();
            while (it.hasNext()) {
                String k = it.next();
                checkCat(k);
                JSONObject v = b.getJSONObject(k);
                database.budgets.put(k, new double[]{periodIndex(v.getString("period")), v.getDouble("limit")});
            }
        }
        return database;
    }
}
