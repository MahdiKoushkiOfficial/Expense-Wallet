package com.mahdi.expensewallet.model;

import org.json.JSONException;
import org.json.JSONObject;

import static com.mahdi.expensewallet.utils.IdUtils.readId;
import static com.mahdi.expensewallet.utils.IdUtils.uid;

public class Account {
    public String id = uid(), name = "";
    public int type = 1;
    public double initial = 0;
    public JSONObject js() throws JSONException {
        JSONObject o = new JSONObject();
        o.put("id", id); o.put("name", name); o.put("type", type); o.put("initial", initial);
        return o;
    }
    public static Account of(JSONObject o) throws JSONException {
        Account a = new Account();
        a.id = readId(o); a.name = o.getString("name"); a.type = o.getInt("type"); a.initial = o.getDouble("initial");
        if (a.type < 0 || a.type > 2) throw new JSONException("bad account type");
        return a;
    }
}
