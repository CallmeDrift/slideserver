package com.slideshow.server;

import java.time.LocalDateTime;

/**
 * Sesión de un control conectado al servidor. Guarda quién es (username),
 * cuándo se conectó, cuál fue su última acción y si el servidor le tiene
 * permiso vigente para manejar las diapositivas.
 */
public class ControlSession {
    private final String controlId;
    private final String username;
    private final LocalDateTime connectedAt;
    private volatile boolean allowed = true;
    private volatile String lastAction = "-";
    private volatile LocalDateTime lastActionAt;

    public ControlSession(String controlId, String username) {
        this.controlId = controlId;
        this.username = username;
        this.connectedAt = LocalDateTime.now();
    }

    public String getControlId() { return controlId; }
    public String getUsername() { return username; }
    public LocalDateTime getConnectedAt() { return connectedAt; }
    public boolean isAllowed() { return allowed; }
    public void setAllowed(boolean allowed) { this.allowed = allowed; }
    public String getLastAction() { return lastAction; }
    public LocalDateTime getLastActionAt() { return lastActionAt; }

    public void registerAction(String action) {
        this.lastAction = action;
        this.lastActionAt = LocalDateTime.now();
    }
}
