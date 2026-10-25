package com.mahdi.expensewallet.model;

import com.mahdi.expensewallet.utils.CalendarUtils;

import org.json.JSONException;
import org.json.JSONObject;

import static com.mahdi.expensewallet.utils.IdUtils.readId;
import static com.mahdi.expensewallet.utils.IdUtils.uid;
import static com.mahdi.expensewallet.utils.ValidationUtils.checkCat;

/** Planned (recurring or one-time) payment. unit: 0 once, 1 day, 2 week, 3 month, 4 year. */
public class PlannedPayment {
    public String id = uid(), accountId = "", category = "other", title = "", description = "";
    public int type = 0, unit = 0, n = 1;
    public double amount;
    public long next;
    public JSONObject js() throws JSONException {
        JSONObject o = new JSONObject();
        o.put("id", id); o.put("accountId", accountId); o.put("category", category); o.put("title", title); o.put("description", description);
        o.put("type", type); o.put("unit", unit); o.put("n", n); o.put("amount", amount);
        o.put("next", CalendarUtils.dateStamp(next));
        return o;
    }
    public static PlannedPayment of(JSONObject o) throws JSONException {
        PlannedPayment payment = new PlannedPayment();
        payment.id = readId(o); payment.accountId = o.getString("accountId"); payment.category = o.getString("category"); checkCat(payment.category);
        payment.title = o.optString("title"); payment.description = o.optString("description"); payment.type = o.getInt("type");
        payment.unit = o.getInt("unit"); payment.n = Math.max(1, o.getInt("n")); payment.amount = o.getDouble("amount");
        if (payment.unit < 0 || payment.unit > 4) throw new JSONException("bad unit");
        payment.next = CalendarUtils.parseDate(o.getString("next"));
        return payment;
    }
}
