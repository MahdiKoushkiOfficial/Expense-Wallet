package com.mahdi.expensewallet.data;

import com.mahdi.expensewallet.model.AppDatabase;

import org.json.JSONException;
import org.json.JSONObject;

import static com.mahdi.expensewallet.data.SettingsManager.onboarded;
import static com.mahdi.expensewallet.data.SettingsManager.saveSettings;
import static com.mahdi.expensewallet.data.SettingsManager.sharedPreferences;

/** Loads and saves the AppDatabase as JSON in SharedPreferences. */
public final class DatabaseStorage {
    private DatabaseStorage() { }

    public static AppDatabase database = new AppDatabase();

    public static void loadDb() {
        try {
            database = AppDatabase.of(new JSONObject(sharedPreferences.getString("db", "{}")));
        } catch (Exception e) {
            // unreadable (e.g. saved by an older format): start clean with the welcome screen
            database = new AppDatabase();
            if (onboarded) { onboarded = false; saveSettings(); }
        }
    }

    public static void saveDb() {
        try { sharedPreferences.edit().putString("db", database.js().toString()).apply(); } catch (JSONException ignored) { }
    }

    /** Back to the initial values (a new activity instance used to start from these). */
    public static void reset() {
        database = new AppDatabase();
    }
}
