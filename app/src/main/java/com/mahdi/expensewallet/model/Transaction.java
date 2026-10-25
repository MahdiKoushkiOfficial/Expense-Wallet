package com.mahdi.expensewallet.model;

import com.mahdi.expensewallet.utils.CalendarUtils;

import org.json.JSONException;
import org.json.JSONObject;

import static com.mahdi.expensewallet.utils.IdUtils.readId;
import static com.mahdi.expensewallet.utils.IdUtils.uid;
import static com.mahdi.expensewallet.utils.ValidationUtils.checkCat;

public class Transaction {
    public String id = uid(), accountId = "", category = "other", title = "", description = "", tags = "";
    public int type = 0; // 0 expense, 1 income
    public double amount;
    public long time, dueDate;  // dueDate = 0 means "no due date"
    public boolean settled = true, planned = false;
    public JSONObject js() throws JSONException {
        JSONObject o = new JSONObject();
        o.put("id", id); o.put("accountId", accountId); o.put("category", category); o.put("title", title); o.put("description", description);
        o.put("tags", tags); o.put("type", type); o.put("amount", amount);
        o.put("time", CalendarUtils.stamp(time));
        o.put("dueDate", dueDate == 0 ? JSONObject.NULL : CalendarUtils.dateStamp(dueDate));
        o.put("settled", settled); o.put("planned", planned);
        return o;
    }
    public static Transaction of(JSONObject o) throws JSONException {
        Transaction transaction = new Transaction();
        transaction.id = readId(o); transaction.accountId = o.getString("accountId"); transaction.category = o.getString("category"); checkCat(transaction.category);
        transaction.title = o.optString("title"); transaction.description = o.optString("description"); transaction.tags = o.optString("tags");
        transaction.type = o.getInt("type"); transaction.amount = o.getDouble("amount");
        transaction.time = CalendarUtils.parseStamp(o.getString("time"));
        transaction.dueDate = o.isNull("dueDate") ? 0 : CalendarUtils.parseDate(o.getString("dueDate"));
        transaction.settled = o.getBoolean("settled"); transaction.planned = o.optBoolean("planned", false);
        return transaction;
    }
}
