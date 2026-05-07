package com.nexuapicore.logging;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AsyncLogWriter {
    private static final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "NexusLogWriter");
        t.setDaemon(true);
        return t;
    });
    
    public static void writeAsync(String file, String line) {
        executor.submit(() -> {
            try (PrintWriter pw = new PrintWriter(new FileWriter(file, true))) {
                pw.println(line);
            } catch (IOException ignored) {}
        });
    }
}
