package com.slideshow.common;

/** Excepción para errores del contrato: control no autorizado, archivo inexistente, etc. */
public class SlideShowException extends Exception {
    public SlideShowException(String message) {
        super(message);
    }
}
