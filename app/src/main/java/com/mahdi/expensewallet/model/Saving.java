package com.mahdi.expensewallet.model;

import com.mahdi.expensewallet.utils.CalendarUtils;

import java.util.Arrays;

import org.json.JSONException;
import org.json.JSONObject;

import static com.mahdi.expensewallet.utils.Constants.GOALS;
import static com.mahdi.expensewallet.utils.IdUtils.readId;
import static com.mahdi.expensewallet.utils.IdUtils.uid;

public class Saving {
    public String id = uid(), goal = "other", accountId = "";
    public double amount;
    public long time;
    public JSONObject js() throws JSONException {
        JSONObject o = new JSONObject();
        o.put("id", id); o.put("goal", goal); o.put("accountId", accountId); o.put("amount", amount);
        o.put("time", CalendarUtils.stamp(time));
        return o;
    }
    public static Saving of(JSONObject o) throws JSONException {
        Saving saving = new Saving();
        saving.id = readId(o); saving.goal = o.getString("goal"); saving.accountId = o.getString("accountId");
        if (!Arrays.asList(GOALS).contains(saving.goal)) throw new JSONException("unknown goal: " + saving.goal);
        saving.amount = o.getDouble("amount"); saving.time = CalendarUtils.parseStamp(o.getString("time"));
        return saving;
    }
}
