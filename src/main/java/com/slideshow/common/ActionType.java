package com.slideshow.common;

import java.io.Serializable;

public enum ActionType implements Serializable {
    START_PRESENTATION,   
    NEXT_SLIDE,           
    PREVIOUS_SLIDE,       
    GO_TO_SLIDE,         
    TOGGLE_FULLSCREEN     
}
