package com.slideshow.server;

import javax.swing.*;
import java.awt.*;

class FlatButton extends JButton {

    private final boolean accent;

    FlatButton(String text) {
        this(text, false);
    }

    FlatButton(String text, boolean accent) {
        super(text);
        this.accent = accent;
        setForeground(ServerTheme.TEXT_LIGHT);
        setFont(ServerTheme.FONT_BODY);
        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setOpaque(false);
        setAlignmentX(Component.LEFT_ALIGNMENT);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
    }

    private Color outlineColor() {
        Color base = accent ? ServerTheme.OUTLINE_ACCENT : ServerTheme.OUTLINE;
        if (!isEnabled()) return ServerTheme.OUTLINE_SOFT;
        if (getModel().isRollover() || getModel().isPressed()) return brighten(base, 0.25);
        return base;
    }

    private Color fillColor() {
        if (getModel().isPressed()) return brighten(ServerTheme.PANEL_BG, 0.10);
        if (getModel().isRollover()) return brighten(ServerTheme.PANEL_BG, 0.06);
        return ServerTheme.PANEL_BG;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int arc = 10;
        g2.setColor(fillColor());
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
        g2.setStroke(new BasicStroke(1.3f));
        g2.setColor(outlineColor());
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
        g2.dispose();

        super.paintComponent(g);
    }

    private static Color brighten(Color c, double t) {
        int r = (int) (c.getRed() * (1 - t) + 255 * t);
        int g = (int) (c.getGreen() * (1 - t) + 255 * t);
        int b = (int) (c.getBlue() * (1 - t) + 255 * t);
        return new Color(clamp(r), clamp(g), clamp(b));
    }

    private static int clamp(int v) { return Math.max(0, Math.min(255, v)); }
}
