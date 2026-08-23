package com.slideshow.server;

import com.slideshow.common.DeckInfo;

import java.awt.image.BufferedImage;
import java.util.List;

/**
 * Representa un mazo (TCG referencia :3) de diapositivas ya cargado en memoria como imágenes,
 * sin importar si el origen fue un PDF o una carpeta de PNG.
 */
public class Deck {
    private final String id;
    private final String name;
    private final String sourceType; 
    private final List<BufferedImage> slides;

    public Deck(String id, String name, String sourceType, List<BufferedImage> slides) {
        this.id = id;
        this.name = name;
        this.sourceType = sourceType;
        this.slides = slides;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getSourceType() { return sourceType; }
    public int getSlideCount() { return slides.size(); }
    public BufferedImage getSlide(int index) { return slides.get(index); }

    public DeckInfo toDeckInfo() {
        return new DeckInfo(id, name, getSlideCount(), sourceType);
    }
}
