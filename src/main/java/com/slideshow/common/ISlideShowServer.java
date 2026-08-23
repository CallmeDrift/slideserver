package com.slideshow.common;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * Contrato RMI entre el servidor y los controles
 *
 * Reglas del contrato:
 *  - Todo control debe registrarse primero con registerControl(username) y usar
 *    el controlId que le devuelve el servidor en cada llamada posterior.
 *  - Toda acción mutable (next/previous/goto/start/fullscreen) recibe un
 *    "actionId" generado por quien invoca (UUID recomendado). El servidor usa
 *    ese actionId para garantizar idempotencia: si el mismo actionId llega más
 *    de una vez (por reintentos, timeouts, dobles clics, etc.) el efecto se
 *    aplica UNA sola vez y las llamadas repetidas reciben el mismo ActionResult.
 *  - Adicionalmente hay un cooldown global de 1 segundo entre ejecuciones
 *    reales: si dos acciones DISTINTAS (por ejemplo "siguiente" de un control y
 *    "anterior" de otro) llegan casi al mismo tiempo, solo la primera se
 *    ejecuta; la segunda recibe ActionStatus.COOLDOWN_REJECTED.
 */
public interface ISlideShowServer extends Remote {

    String registerControl(String username) throws RemoteException;

    void unregisterControl(String controlId) throws RemoteException;

    List<DeckInfo> listDecks(String controlId) throws RemoteException, SlideShowException;

    PresentationState getState(String controlId) throws RemoteException, SlideShowException;

    ActionResult startPresentation(String controlId, String actionId, String deckId) throws RemoteException;

    ActionResult nextSlide(String controlId, String actionId) throws RemoteException;

    ActionResult previousSlide(String controlId, String actionId) throws RemoteException;

    ActionResult goToSlide(String controlId, String actionId, int slideIndex) throws RemoteException;

    ActionResult toggleFullScreen(String controlId, String actionId) throws RemoteException;
}
