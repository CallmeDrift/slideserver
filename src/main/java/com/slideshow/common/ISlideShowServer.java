package com.slideshow.common;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * Contrato RMI entre el servidor y los controles
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
