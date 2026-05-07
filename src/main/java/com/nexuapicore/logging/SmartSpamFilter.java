package com.nexuapicore.logging;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SmartSpamFilter {
    private final Map<String, Long> lastLogTime = new ConcurrentHashMap<>();
    private final Map<String, Integer> repetitionCount = new ConcurrentHashMap<>();
    private static final long COOLDOWN_MS = 1000; // 1 segundo entre mensagens iguais
    
    public String filter(LogCategory category, String msg) {
        String key = category.name() + ":" + msg;
        long now = System.currentTimeMillis();
        Long last = lastLogTime.get(key);
        if (last != null && (now - last) < COOLDOWN_MS) {
            repetitionCount.merge(key, 1, Integer::sum);
            return null; // suprime
        }
        int count = repetitionCount.getOrDefault(key, 0);
        lastLogTime.put(key, now);
        repetitionCount.remove(key);
        if (count > 0) {
            return msg + " (repeated " + count + " times)";
        }
        return msg;
    }
}
