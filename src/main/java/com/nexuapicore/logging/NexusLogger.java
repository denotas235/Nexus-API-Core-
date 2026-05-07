package com.nexuapicore.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class NexusLogger {
    private static final ConcurrentHashMap<String, NexusLogger> instances = new ConcurrentHashMap<>();
    private final Logger logger;
    private final String system;
    private final SmartSpamFilter spamFilter;
    private static final AtomicLong globalSequence = new AtomicLong(0);
    
    private NexusLogger(String system) {
        this.system = system;
        this.logger = LoggerFactory.getLogger(system.toLowerCase());
        this.spamFilter = new SmartSpamFilter();
    }
    
    public static NexusLogger get(String system) {
        return instances.computeIfAbsent(system, NexusLogger::new);
    }
    
    public void log(LogCategory category, LogLevel level, String msg) {
        String filtered = spamFilter.filter(category, msg);
        if (filtered == null) return; // suprimido pelo filtro anti-spam
        
        long seq = globalSequence.incrementAndGet();
        String formatted = String.format("[#%d] [%s] [%s] [%s] %s", seq, system, category, level, filtered);
        
        switch (level) {
            case TRACE: logger.trace(formatted); break;
            case DEBUG: logger.debug(formatted); break;
            case PERF:  logger.info(formatted); break;
            case WARN:  logger.warn(formatted); break;
            case ERROR: logger.error(formatted); break;
            case FATAL: logger.error("FATAL: " + formatted); break;
            default:    logger.info(formatted); break;
        }
        
        // Em caso de erro ou fatal, acionar snapshot (se configurado)
        if (level == LogLevel.ERROR || level == LogLevel.FATAL) {
            DiagnosticSnapshot.capture(system, category, msg);
        }
    }
    
    // Atalhos para categorias comuns
    public void init(String msg)  { log(LogCategory.INIT,  LogLevel.INFO, msg); }
    public void gpu(String msg)   { log(LogCategory.GPU,   LogLevel.GPU, msg); }
    public void perf(String msg)  { log(LogCategory.PERF,  LogLevel.PERF, msg); }
    public void render(String msg){ log(LogCategory.RENDER, LogLevel.RENDER, msg); }
    public void shader(String msg){ log(LogCategory.SHADER, LogLevel.INFO, msg); }
    public void astc(String msg)  { log(LogCategory.ASTC,  LogLevel.INFO, msg); }
    public void cache(String msg) { log(LogCategory.CACHE, LogLevel.INFO, msg); }
    public void lod(String msg)   { log(LogCategory.LOD,   LogLevel.INFO, msg); }
    public void gen(String msg)   { log(LogCategory.GEN,   LogLevel.INFO, msg); }
    public void sync(String msg)  { log(LogCategory.SYNC,  LogLevel.INFO, msg); }
    public void audio(String msg) { log(LogCategory.AUDIO, LogLevel.INFO, msg); }
    public void warn(String msg)  { log(LogCategory.CORE,  LogLevel.WARN, msg); }
    public void error(String msg) { log(LogCategory.CORE,  LogLevel.ERROR, msg); }
    
    public void performanceSnapshot(double fps, double frameTime) {
        String stress = frameTime < 18 ? "LOW" : (frameTime < 25 ? "MEDIUM" : (frameTime < 33 ? "HIGH" : "CRITICAL"));
        log(LogCategory.PERF, LogLevel.PERF, String.format("FPS: %.1f | FrameTime: %.1fms | Stress: %s", fps, frameTime, stress));
    }
}
