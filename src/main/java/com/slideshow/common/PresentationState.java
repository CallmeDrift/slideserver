package com.slideshow.common;

import java.io.Serializable;

/**
 * Foto del estado actual de la presentación. Viaja por RMI, por eso es Serializable
 * y no expone imagenes sino solo los datos que el control necesita.
 */
public class PresentationState implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String deckId;
    private final String deckName;
    private final int slideIndex;  
    private final int totalSlides;
    private final boolean fullScreen;
    private final boolean presentationOpen; 
    private final String lastActionBy;      

    public PresentationState(String deckId, String deckName, int slideIndex, int totalSlides,
                              boolean fullScreen, boolean presentationOpen, String lastActionBy) {
        this.deckId = deckId;
        this.deckName = deckName;
        this.slideIndex = slideIndex;
        this.totalSlides = totalSlides;
        this.fullScreen = fullScreen;
        this.presentationOpen = presentationOpen;
        this.lastActionBy = lastActionBy;
    }

    public static PresentationState empty() {
        return new PresentationState(null, null, -1, 0, false, false, null);
    }

    public String getDeckId() { return deckId; }
    public String getDeckName() { return deckName; }
    public int getSlideIndex() { return slideIndex; }
    public int getTotalSlides() { return totalSlides; }
    public boolean isFullScreen() { return fullScreen; }
    public boolean isPresentationOpen() { return presentationOpen; }
    public String getLastActionBy() { return lastActionBy; }

    @Override
    public String toString() {
        if (!presentationOpen) return "Sin presentación activa";
        return String.format("%s - diapositiva %d/%d%s", deckName, slideIndex + 1, totalSlides,
                fullScreen ? " [FULLSCREEN]" : "");
    }
}
