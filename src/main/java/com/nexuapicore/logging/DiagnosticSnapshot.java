package com.nexuapicore.logging;

import java.io.File;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.Arrays;

public class DiagnosticSnapshot {
    private static final String SNAPSHOT_DIR = "logs/snapshots/";
    
    public static void capture(String system, LogCategory category, String trigger) {
        try {
            File dir = new File(SNAPSHOT_DIR + system + "_" + LocalDateTime.now().toString().replace(':', '-'));
            dir.mkdirs();
            File file = new File(dir, "snapshot.txt");
            try (PrintWriter pw = new PrintWriter(file)) {
                pw.println("=== NEXUS DIAGNOSTIC SNAPSHOT ===");
                pw.println("Timestamp: " + LocalDateTime.now());
                pw.println("System: " + system);
                pw.println("Trigger: " + category + " - " + trigger);
                pw.println("Thread: " + Thread.currentThread().getName());
                pw.println("Memory: " + Runtime.getRuntime().totalMemory());
                pw.println("==================================");
                Arrays.stream(Thread.currentThread().getStackTrace()).forEach(pw::println);
            }
        } catch (Exception ignored) {}
    }
}
