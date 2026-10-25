package com.tvos.launcher;

import java.util.Locale;

final class DeviceRules {
    enum Security { OPEN, PERSONAL, SYSTEM_ONLY }
    static boolean wifiControl(int api) { return api >= 26 && api <= 28; }
    static boolean bluetoothControl(int api) { return api >= 26 && api <= 32; }
    static Security security(String capabilities) {
        if (capabilities == null) return Security.SYSTEM_ONLY;
        String value = capabilities.toUpperCase(Locale.ROOT);
        if (value.contains("EAP") || value.contains("WEP") || value.contains("WAPI")) return Security.SYSTEM_ONLY;
        if (value.contains("PSK")) return Security.PERSONAL;
        return value.matches("(\\[(ESS|IBSS|WPS)\\])*") ? Security.OPEN : Security.SYSTEM_ONLY;
    }
    static String quoted(String input) { return "\"" + input.replace("\\", "\\\\").replace("\"", "\\\"") + "\""; }
}
