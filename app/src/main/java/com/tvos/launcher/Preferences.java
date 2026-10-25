package com.tvos.launcher;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

final class Preferences {
    final SharedPreferences data;
    Preferences(Context c) { data = c.getSharedPreferences("tvos.preferences", Context.MODE_PRIVATE); }
    int columns() { return Math.max(4, Math.min(7, data.getInt("columns", 6))); }
    int glass() { return Math.max(1, Math.min(3, data.getInt("glass", 2))); }
    boolean motion() { return data.getBoolean("motion", true); }
    int idleMinutes() { return Math.max(0, data.getInt("idle", 0)); }
    String backdrop() { return data.getString("backdrop", "aurora"); }
    String image() { return data.getString("image", ""); }
    long revision() { return data.getLong("revision", 0); }
    void number(String key, int value) { data.edit().putInt(key, value).putLong("revision", revision() + 1).apply(); }
    void text(String key, String value) { data.edit().putString(key, value).putLong("revision", revision() + 1).apply(); }
    void motion(boolean enabled) { data.edit().putBoolean("motion", enabled).putLong("revision", revision() + 1).apply(); }
    void order(List<String> packages) { text("order", String.join("\n", packages)); }
    List<String> order() {
        String value = data.getString("order", "");
        return value.isEmpty() ? new ArrayList<>() : new ArrayList<>(Arrays.asList(value.split("\n")));
    }
    void reset() { data.edit().clear().putLong("revision", revision() + 1).apply(); }
}
