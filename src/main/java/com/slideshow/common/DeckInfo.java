package com.slideshow.common;

import java.io.Serializable;


public class DeckInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String id;
    private final String name;
    private final int slideCount;
    private final String sourceType; 

    public DeckInfo(String id, String name, int slideCount, String sourceType) {
        this.id = id;
        this.name = name;
        this.slideCount = slideCount;
        this.sourceType = sourceType;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public int getSlideCount() { return slideCount; }
    public String getSourceType() { return sourceType; }

    @Override
    public String toString() {
        return name + " (" + sourceType + ", " + slideCount + " diapositivas)";
    }
}
