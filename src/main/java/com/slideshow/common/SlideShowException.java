package com.slideshow.common;

/** Excepción de negocio para errores del contrato: control no autorizado, deck inexistente, etc. */
public class SlideShowException extends Exception {
    public SlideShowException(String message) {
        super(message);
    }
}
