package com.mahdi.expensewallet.utils;

import java.util.HashSet;

import org.json.JSONException;
import org.json.JSONObject;

/** Short random ids for accounts, transactions and the other records. */
public final class IdUtils {
    private IdUtils() { }

    /** Short random ids such as "k3f9a1x7q2": 10 lowercase letters/digits, never reused while the app runs. */
    public static final java.util.Set<String> USED_IDS = new HashSet<>();
    public static final java.security.SecureRandom RND = new java.security.SecureRandom();
    public static final String ID_CHARS = "abcdefghijklmnopqrstuvwxyz0123456789";

    public static String uid() {
        while (true) {
            StringBuilder b = new StringBuilder();
            for (int i = 0; i < 10; i++) b.append(ID_CHARS.charAt(RND.nextInt(ID_CHARS.length())));
            if (USED_IDS.add(b.toString())) return b.toString();
        }
    }

    /** Reads an id from a backup (any non-empty string) and reserves it so uid() never repeats it. */
    public static String readId(JSONObject o) throws JSONException {
        String id = o.getString("id");
        if (id.isEmpty()) throw new JSONException("empty id");
        USED_IDS.add(id);
        return id;
    }
}
