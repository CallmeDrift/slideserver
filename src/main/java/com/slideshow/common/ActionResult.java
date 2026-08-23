package com.slideshow.common;

import java.io.Serializable;

public class ActionResult implements Serializable {
    private static final long serialVersionUID = 1L;

    private final ActionStatus status;
    private final String message;
    private final PresentationState state;
    private final String actionId;

    public ActionResult(ActionStatus status, String message, PresentationState state, String actionId) {
        this.status = status;
        this.message = message;
        this.state = state;
        this.actionId = actionId;
    }

    public static ActionResult executed(PresentationState state, String actionId) {
        return new ActionResult(ActionStatus.EXECUTED, "Acción ejecutada", state, actionId);
    }

    public static ActionResult replay(PresentationState state, String actionId) {
        return new ActionResult(ActionStatus.IDEMPOTENT_REPLAY,
                "Esta acción (actionId) ya había sido procesada; se devuelve el resultado original", state, actionId);
    }

    public static ActionResult cooldown(PresentationState state, String actionId, long msRestante) {
        return new ActionResult(ActionStatus.COOLDOWN_REJECTED,
                "Cooldown activo, esperá " + msRestante + " ms antes de intentar otra acción distinta", state, actionId);
    }

    public static ActionResult unauthorized(String actionId) {
        return new ActionResult(ActionStatus.UNAUTHORIZED, "Control no autorizado o no registrado", null, actionId);
    }

    public static ActionResult error(String msg, PresentationState state, String actionId) {
        return new ActionResult(ActionStatus.ERROR, msg, state, actionId);
    }

    public ActionStatus getStatus() { return status; }
    public String getMessage() { return message; }
    public PresentationState getState() { return state; }
    public String getActionId() { return actionId; }
}
