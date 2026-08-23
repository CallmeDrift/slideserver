package com.slideshow.server;

import java.awt.*;

/**
 * Paleta visual del servidor
 */
final class ServerTheme {

    private ServerTheme() { }

    static final Color APP_BG         = new Color(8, 8, 8);    
    static final Color PANEL_BG       = new Color(14, 14, 14);   
    static final Color FIELD_BG       = new Color(16, 16, 16);  
    static final Color VIEWER_BG      = Color.BLACK;             

    static final Color OUTLINE        = new Color(95, 95, 95);
    static final Color OUTLINE_SOFT   = new Color(55, 55, 55);
    static final Color OUTLINE_ACCENT = new Color(150, 150, 150); 

    static final Color TEXT_LIGHT     = new Color(225, 225, 225);
    static final Color TEXT_MUTED     = new Color(130, 130, 130);

    static final Color OK             = new Color(150, 190, 160);
    static final Color WARN           = new Color(200, 120, 120);

    static final Font FONT_LABEL   = new Font("SansSerif", Font.BOLD, 12);
    static final Font FONT_SMALL   = new Font("SansSerif", Font.PLAIN, 11);
    static final Font FONT_BODY    = new Font("SansSerif", Font.PLAIN, 12);
}
