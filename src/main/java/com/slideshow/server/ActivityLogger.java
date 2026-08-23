package com.slideshow.server;

import com.slideshow.common.ActionStatus;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Lleva el historial de "quién hizo qué y cuándo" para mostrarlo en el panel
 * del servidor. Notifica a la UI cada vez que se agrega una entrada.
 */
public class ActivityLogger {

    public record Entry(LocalDateTime timestamp, String username, String controlId,
            String action, ActionStatus status, String detail) {
        private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("HH:mm:ss");

        public String formattedTime() {
            return timestamp.format(FMT);
        }
    }

    private final List<Entry> entries = new LinkedList<>();
    private final List<Consumer<Entry>> listeners = new CopyOnWriteArrayList<>();
    private static final int MAX_ENTRIES = 500;

    public synchronized void log(String username, String controlId, String action,
            ActionStatus status, String detail) {
        Entry e = new Entry(LocalDateTime.now(), username, controlId, action, status, detail);
        entries.add(0, e); // más reciente primero
        while (entries.size() > MAX_ENTRIES) {
            entries.remove(entries.size() - 1);
        }
        for (Consumer<Entry> l : listeners) {
            l.accept(e);
        }
    }

    public void addListener(Consumer<Entry> listener) {
        listeners.add(listener);
    }

    public synchronized List<Entry> snapshot() {
        return new LinkedList<>(entries);
    }
}
