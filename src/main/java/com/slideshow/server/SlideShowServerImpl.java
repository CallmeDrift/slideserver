package com.slideshow.server;

import com.slideshow.common.*;


import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Implementación de la interfaz ISlideShowServer. 
 */
public class SlideShowServerImpl extends UnicastRemoteObject implements ISlideShowServer {

    public static final String SERVER_CONTROL_ID = "SERVER-LOCAL";

    private final DeckManager deckManager = new DeckManager();
    private final IdempotencyManager idempotencyManager = new IdempotencyManager();
    private final ActivityLogger activityLogger = new ActivityLogger();
    private final Map<String, ControlSession> sessions = new ConcurrentHashMap<>();
    private final SlideViewerFrame viewer;

    private final Object presentationLock = new Object();
    private Deck activeDeck;
    private int slideIndex = -1;
    private String lastActionBy = null;

    public SlideShowServerImpl(SlideViewerFrame viewer) throws RemoteException {
        super();
        this.viewer = viewer;
        viewer.showBlank("Esperando que se inicie una presentación...");

        ControlSession serverSession = new ControlSession(SERVER_CONTROL_ID, "SERVIDOR (local)");
        sessions.put(SERVER_CONTROL_ID, serverSession);
    }

    public ActivityLogger getActivityLogger() { return activityLogger; }
    public DeckManager getDeckManager() { return deckManager; }
    public Map<String, ControlSession> getSessions() { return sessions; }


    // Registro de controles
    @Override
    public String registerControl(String username) throws RemoteException {
        String controlId = UUID.randomUUID().toString();
        ControlSession session = new ControlSession(controlId, username);
        sessions.put(controlId, session);
        activityLogger.log(username, controlId, "CONEXION", ActionStatus.EXECUTED, "Control conectado");
        return controlId;
    }

    @Override
    public void unregisterControl(String controlId) throws RemoteException {
        ControlSession session = sessions.remove(controlId);
        if (session != null) {
            activityLogger.log(session.getUsername(), controlId, "DESCONEXION", ActionStatus.EXECUTED, "Control desconectado");
        }
    }

    public void setControlAllowed(String controlId, boolean allowed) {
        ControlSession s = sessions.get(controlId);
        if (s != null) s.setAllowed(allowed);
    }

    private ControlSession validate(String controlId) throws SlideShowException {
        ControlSession session = sessions.get(controlId);
        if (session == null) {
            throw new SlideShowException("Control no registrado: " + controlId);
        }
        if (!session.isAllowed()) {
            throw new SlideShowException("Control sin permiso para manejar las diapositivas: " + session.getUsername());
        }
        return session;
    }


    // Consultas
    @Override
    public List<DeckInfo> listDecks(String controlId) throws RemoteException, SlideShowException {
        validate(controlId);
        return deckManager.listAll().stream().map(Deck::toDeckInfo).collect(Collectors.toList());
    }

    @Override
    public PresentationState getState(String controlId) throws RemoteException, SlideShowException {
        validate(controlId);
        return snapshotState();
    }

    private PresentationState snapshotState() {
        synchronized (presentationLock) {
            if (activeDeck == null) {
                return PresentationState.empty();
            }
            return new PresentationState(
                    activeDeck.getId(), activeDeck.getName(), slideIndex,
                    activeDeck.getSlideCount(), viewer.isFullScreenActive(), true, lastActionBy);
        }
    }


    // Carga de archivos
    public Deck loadFile(File file) throws IOException {
        return deckManager.loadFromFile(file);
    }

    public Deck loadPdf(File pdfFile) throws IOException {
        return deckManager.loadFromPdf(pdfFile);
    }

    public Deck loadPngFolder(File folder) throws IOException {
        return deckManager.loadFromPngFolder(folder);
    }

    @Override
    public ActionResult startPresentation(String controlId, String actionId, String deckId) throws RemoteException {
        return runGuarded(controlId, actionId, "START_PRESENTATION", () -> {
            Deck deck = deckManager.get(deckId);
            if (deck == null) {
                return ActionResult.error("Deck no encontrado: " + deckId, snapshotState(), actionId);
            }
            synchronized (presentationLock) {
                activeDeck = deck;
                slideIndex = 0;
                lastActionBy = usernameOf(controlId);
                viewer.showSlide(deck.getSlide(0));
            }
            return ActionResult.executed(snapshotState(), actionId);
        });
    }

    @Override
    public ActionResult nextSlide(String controlId, String actionId) throws RemoteException {
        return runGuarded(controlId, actionId, "NEXT_SLIDE", () -> {
            synchronized (presentationLock) {
                if (activeDeck == null) {
                    return ActionResult.error("No hay presentación activa", snapshotState(), actionId);
                }
                if (slideIndex + 1 >= activeDeck.getSlideCount()) {
                    return ActionResult.error("Ya estás en la última diapositiva", snapshotState(), actionId);
                }
                slideIndex++;
                lastActionBy = usernameOf(controlId);
                viewer.showSlide(activeDeck.getSlide(slideIndex));
                return ActionResult.executed(snapshotState(), actionId);
            }
        });
    }

    @Override
    public ActionResult previousSlide(String controlId, String actionId) throws RemoteException {
        return runGuarded(controlId, actionId, "PREVIOUS_SLIDE", () -> {
            synchronized (presentationLock) {
                if (activeDeck == null) {
                    return ActionResult.error("No hay presentación activa", snapshotState(), actionId);
                }
                if (slideIndex - 1 < 0) {
                    return ActionResult.error("Ya estás en la primera diapositiva", snapshotState(), actionId);
                }
                slideIndex--;
                lastActionBy = usernameOf(controlId);
                viewer.showSlide(activeDeck.getSlide(slideIndex));
                return ActionResult.executed(snapshotState(), actionId);
            }
        });
    }

    @Override
    public ActionResult goToSlide(String controlId, String actionId, int requestedIndex) throws RemoteException {
        return runGuarded(controlId, actionId, "GO_TO_SLIDE[" + requestedIndex + "]", () -> {
            synchronized (presentationLock) {
                if (activeDeck == null) {
                    return ActionResult.error("No hay presentación activa", snapshotState(), actionId);
                }
                if (requestedIndex < 0 || requestedIndex >= activeDeck.getSlideCount()) {
                    return ActionResult.error("Índice fuera de rango: " + requestedIndex, snapshotState(), actionId);
                }
                slideIndex = requestedIndex;
                lastActionBy = usernameOf(controlId);
                viewer.showSlide(activeDeck.getSlide(slideIndex));
                return ActionResult.executed(snapshotState(), actionId);
            }
        });
    }

    @Override
    public ActionResult toggleFullScreen(String controlId, String actionId) throws RemoteException {
        return runGuarded(controlId, actionId, "TOGGLE_FULLSCREEN", () -> {
            viewer.toggleFullScreen();
            lastActionBy = usernameOf(controlId);
            return ActionResult.executed(snapshotState(), actionId);
        });
    }

    private String usernameOf(String controlId) {
        ControlSession s = sessions.get(controlId);
        return s != null ? s.getUsername() : controlId;
    }

    private interface ActionBody {
        ActionResult run();
    }

    private ActionResult runGuarded(String controlId, String actionId, String actionLabel, ActionBody body) {
        ControlSession session;
        try {
            session = validate(controlId);
        } catch (SlideShowException ex) {
            activityLogger.log(controlId, controlId, actionLabel, ActionStatus.UNAUTHORIZED, ex.getMessage());
            return ActionResult.unauthorized(actionId);
        }

        ActionResult result = idempotencyManager.execute(
                actionId,
                restanteMs -> ActionResult.cooldown(snapshotState(), actionId, restanteMs),
                body::run
        );

        session.registerAction(actionLabel + " -> " + result.getStatus());
        activityLogger.log(session.getUsername(), controlId, actionLabel, result.getStatus(), result.getMessage());
        return result;
    }

    public void shutdown() {
        idempotencyManager.shutdown();
    }
}
