package com.slideshow.server;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class SlideViewerFrame extends JPanel {

    private final JLabel imageLabel = new JLabel();
    private BufferedImage currentImage;
    private boolean fullScreen = false;
    private final GraphicsDevice graphicsDevice;
    private JFrame hostFrame;
    private Rectangle hostBoundsBeforeFullScreen;

    public SlideViewerFrame() {
        graphicsDevice = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        setLayout(new BorderLayout());
        setBackground(Color.BLACK);

        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        imageLabel.setVerticalAlignment(SwingConstants.CENTER);
        imageLabel.setBackground(Color.BLACK);
        imageLabel.setOpaque(true);
        add(imageLabel, BorderLayout.CENTER);

        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                repaintScaled();
            }
        });
    }

    public void setHostFrame(JFrame hostFrame) {
        this.hostFrame = hostFrame;
    }

    public void showSlide(BufferedImage image) {
        SwingUtilities.invokeLater(() -> {
            this.currentImage = image;
            repaintScaled();
        });
    }

    public void showBlank(String message) {
        SwingUtilities.invokeLater(() -> {
            this.currentImage = null;
            imageLabel.setIcon(null);
            imageLabel.setForeground(Color.LIGHT_GRAY);
            imageLabel.setText(message);
        });
    }

    private void repaintScaled() {
        if (currentImage == null) return;
        int panelW = Math.max(imageLabel.getWidth(), 100);
        int panelH = Math.max(imageLabel.getHeight(), 100);

        double scale = Math.min(
                (double) panelW / currentImage.getWidth(),
                (double) panelH / currentImage.getHeight());
        int w = Math.max(1, (int) (currentImage.getWidth() * scale));
        int h = Math.max(1, (int) (currentImage.getHeight() * scale));

        Image scaled = currentImage.getScaledInstance(w, h, Image.SCALE_SMOOTH);
        imageLabel.setText(null);
        imageLabel.setIcon(new ImageIcon(scaled));
    }

    public boolean isFullScreenActive() {
        return fullScreen;
    }

    public boolean toggleFullScreen() {
        boolean[] result = new boolean[1];
        Runnable r = () -> {
            if (hostFrame == null) {
                result[0] = fullScreen;
                return;
            }
            if (!fullScreen) {
                hostBoundsBeforeFullScreen = hostFrame.getBounds();
                hostFrame.dispose();
                hostFrame.setUndecorated(true);
                graphicsDevice.setFullScreenWindow(hostFrame);
                hostFrame.setVisible(true);
                fullScreen = true;
            } else {
                graphicsDevice.setFullScreenWindow(null);
                hostFrame.dispose();
                hostFrame.setUndecorated(false);
                if (hostBoundsBeforeFullScreen != null) {
                    hostFrame.setBounds(hostBoundsBeforeFullScreen);
                }
                hostFrame.setVisible(true);
                fullScreen = false;
            }
            result[0] = fullScreen;
            repaintScaled();
        };
        if (SwingUtilities.isEventDispatchThread()) {
            r.run();
        } else {
            try {
                SwingUtilities.invokeAndWait(r);
            } catch (Exception ignored) { }
        }
        return result[0];
    }
}
