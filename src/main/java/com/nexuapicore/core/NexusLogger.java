package com.nexuapicore.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * NexusLogger — logging padronizado para todo o ecossistema Nexus.
 * Formato: [Nexus|MaliOpt|Audio] [INIT|PERF|GPU|ASTC|CACHE|GEN] Mensagem
 */
public class NexusLogger {
    private final Logger logger;
    private final String tag;

    public enum Category {
        INIT, PERF, GPU, ASTC, CACHE, GEN, LOD, SYNC
    }

    public NexusLogger(String module, String subTag) {
        this.tag = String.format("[%s] [%s]", module, subTag);
        this.logger = LoggerFactory.getLogger(module.toLowerCase());
    }

    public NexusLogger(Class<?> clazz, String subTag) {
        this.tag = String.format("[%s] [%s]", clazz.getSimpleName(), subTag);
        this.logger = LoggerFactory.getLogger(clazz);
    }

    public void info(String msg) {
        logger.info("{} {}", tag, msg);
    }

    public void warn(String msg) {
        logger.warn("{} {}", tag, msg);
    }

    public void error(String msg) {
        logger.error("{} {}", tag, msg);
    }

    public void debug(String msg) {
        logger.debug("{} {}", tag, msg);
    }

    public void info(Category cat, String msg) {
        logger.info("{} [{}] {}", tag, cat.name(), msg);
    }

    public void warn(Category cat, String msg) {
        logger.warn("{} [{}] {}", tag, cat.name(), msg);
    }

    // Factory rápido
    public static NexusLogger forModule(String module, String subTag) {
        return new NexusLogger(module, subTag);
    }

    public static NexusLogger forClass(Class<?> clazz, String subTag) {
        return new NexusLogger(clazz, subTag);
    }
}
