package com.slideshow.common;

import java.io.Serializable;

public enum ActionStatus implements Serializable {
    EXECUTED,            
    IDEMPOTENT_REPLAY,   
    COOLDOWN_REJECTED,  
    UNAUTHORIZED,       
    ERROR                
}
