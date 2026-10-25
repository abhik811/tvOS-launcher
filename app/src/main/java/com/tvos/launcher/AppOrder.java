package com.tvos.launcher;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

/** Pure ordering rules; absent apps cannot leave empty dock positions. */
final class AppOrder {
    static List<String> arrange(List<String> installed, List<String> preferred) {
        LinkedHashSet<String> available = new LinkedHashSet<>(installed);
        List<String> result = new ArrayList<>();
        for (String id : preferred) if (available.remove(id)) result.add(id);
        result.addAll(available);
        return result;
    }
    static List<String> move(List<String> original, String id, int destination) {
        List<String> result = new ArrayList<>(original);
        if (!result.remove(id)) return result;
        result.add(Math.max(0, Math.min(destination, result.size())), id);
        return result;
    }
}
