package com.slideshow.server;

import com.slideshow.common.ActionResult;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * idempotencia + cooldown
 */
public class IdempotencyManager {

    private static final long COOLDOWN_MS = 1000;
    private static final long ENTRY_TTL_MS = 5 * 60 * 1000; // 5 mim

    private record CachedResult(ActionResult result, long storedAt) {
    }

    private final Map<String, CachedResult> processedActions = new ConcurrentHashMap<>();
    private final Object executionLock = new Object();
    private volatile long lastExecutionAt = 0;

    private final ScheduledExecutorService cleaner = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "idempotency-cleaner");
        t.setDaemon(true);
        return t;
    });

    public IdempotencyManager() {
        cleaner.scheduleAtFixedRate(this::evictExpired, 1, 1, TimeUnit.MINUTES);
    }

    public ActionResult execute(String actionId,
            java.util.function.LongFunction<ActionResult> cooldownRejectedFactory,
            Supplier<ActionResult> action) {
        CachedResult cached = processedActions.get(actionId);
        if (cached != null) {
            return cached.result();
        }

        synchronized (executionLock) {
            cached = processedActions.get(actionId);
            if (cached != null) {
                return cached.result();
            }

            long now = System.currentTimeMillis();
            long elapsed = now - lastExecutionAt;
            if (elapsed < COOLDOWN_MS) {
                long restante = COOLDOWN_MS - elapsed;
                return cooldownRejectedFactory.apply(restante);
            }

            ActionResult result = action.get();
            lastExecutionAt = System.currentTimeMillis();
            processedActions.put(actionId, new CachedResult(result, lastExecutionAt));
            return result;
        }
    }

    private void evictExpired() {
        long now = System.currentTimeMillis();
        processedActions.entrySet().removeIf(e -> now - e.getValue().storedAt() > ENTRY_TTL_MS);
    }

    public void shutdown() {
        cleaner.shutdownNow();
    }
}
