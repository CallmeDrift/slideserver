package com.slideshow.server;

import com.slideshow.common.ActionResult;
import com.slideshow.common.DeckInfo;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.rmi.RemoteException;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * UI del servidor
 */
public class ServerControlPanel extends JFrame {

    private static final Logger LOGGER = Logger.getLogger(ServerControlPanel.class.getName());

    private final SlideShowServerImpl server;
    private final SlideViewerFrame viewer;
    private final DefaultListModel<DeckInfo> deckListModel = new DefaultListModel<>();
    private final JList<DeckInfo> deckList = new JList<>(deckListModel);

    private final JLabel stateLabel = new JLabel("Sin presentación activa");

    public ServerControlPanel(SlideShowServerImpl server, SlideViewerFrame viewer) {
        super("Servidor de diapositivas");
        this.server = server;
        this.viewer = viewer;

        applyGlobalTheme();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280, 760);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(8, 8));
        getContentPane().setBackground(ServerTheme.APP_BG);

        add(buildMainView(), BorderLayout.CENTER);
        viewer.setHostFrame(this);

        server.getActivityLogger().addListener(this::onActivityEntry);
        refreshDeckList();

        setVisible(true);
    }

    /**
     * Ajusta los defaults de Swing (listas, scrollbars, tooltips, diálogos)
     * para que sigan la misma línea estética negra/gris del resto de la app.
     */
    private static void applyGlobalTheme() {
        UIManager.put("Panel.background", ServerTheme.APP_BG);
        UIManager.put("OptionPane.background", ServerTheme.APP_BG);
        UIManager.put("OptionPane.messageForeground", ServerTheme.TEXT_LIGHT);
        UIManager.put("List.background", ServerTheme.FIELD_BG);
        UIManager.put("List.foreground", ServerTheme.TEXT_LIGHT);
        UIManager.put("List.selectionBackground", ServerTheme.OUTLINE_SOFT);
        UIManager.put("List.selectionForeground", ServerTheme.TEXT_LIGHT);
        UIManager.put("ScrollPane.background", ServerTheme.FIELD_BG);
        UIManager.put("Viewport.background", ServerTheme.FIELD_BG);
        UIManager.put("Label.foreground", ServerTheme.TEXT_LIGHT);
        UIManager.put("ToolTip.background", ServerTheme.PANEL_BG);
        UIManager.put("ToolTip.foreground", ServerTheme.TEXT_LIGHT);
    }

    private JComponent buildMainView() {
        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        root.setBackground(ServerTheme.APP_BG);

        viewer.setBorder(BorderFactory.createLineBorder(ServerTheme.OUTLINE, 1));
        root.add(viewer, BorderLayout.CENTER);
        root.add(buildSideControls(), BorderLayout.EAST);
        return root;
    }

    private JComponent buildSideControls() {
        JPanel side = new JPanel();
        side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));
        side.setPreferredSize(new Dimension(280, 0));
        side.setBackground(ServerTheme.PANEL_BG);
        side.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ServerTheme.OUTLINE_SOFT, 1),
                new EmptyBorder(14, 14, 14, 14)));

        JLabel decksLabel = new JLabel("DECKS CARGADOS");
        decksLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        decksLabel.setFont(ServerTheme.FONT_LABEL);
        decksLabel.setForeground(ServerTheme.TEXT_MUTED);
        side.add(decksLabel);
        side.add(Box.createVerticalStrut(8));

        deckList.setBackground(ServerTheme.FIELD_BG);
        deckList.setForeground(ServerTheme.TEXT_LIGHT);
        deckList.setFont(ServerTheme.FONT_BODY);
        deckList.setSelectionBackground(ServerTheme.OUTLINE_SOFT);
        deckList.setSelectionForeground(ServerTheme.TEXT_LIGHT);

        JScrollPane deckScroll = new JScrollPane(deckList);
        deckScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        deckScroll.setPreferredSize(new Dimension(260, 220));
        deckScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 220));
        deckScroll.setBorder(BorderFactory.createLineBorder(ServerTheme.OUTLINE, 1));
        deckScroll.getViewport().setBackground(ServerTheme.FIELD_BG);
        side.add(deckScroll);
        side.add(Box.createVerticalStrut(14));

        JButton loadPdfBtn = new FlatButton("Abrir archivo...");
        loadPdfBtn.addActionListener(e -> loadFile());

        JButton loadFolderBtn = new FlatButton("Abrir carpeta PNG...");
        loadFolderBtn.addActionListener(e -> loadPngFolder());

        JButton startBtn = new FlatButton("Iniciar deck seleccionado", true);
        startBtn.addActionListener(e -> startSelectedDeck());

        JButton prev = new FlatButton("Anterior");
        prev.addActionListener(e -> callServerAction(() ->
                server.previousSlide(SlideShowServerImpl.SERVER_CONTROL_ID, UUID.randomUUID().toString())));

        JButton next = new FlatButton("Siguiente");
        next.addActionListener(e -> callServerAction(() ->
                server.nextSlide(SlideShowServerImpl.SERVER_CONTROL_ID, UUID.randomUUID().toString())));

        JButton goTo = new FlatButton("Ir a...");
        goTo.addActionListener(e -> {
            String s = JOptionPane.showInputDialog(this, "Número de diapositiva (empieza en 1):");
            if (s == null || s.isBlank()) return;
            try {
                int idx = Integer.parseInt(s.trim()) - 1;
                callServerAction(() -> server.goToSlide(SlideShowServerImpl.SERVER_CONTROL_ID, UUID.randomUUID().toString(), idx));
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Número inválido");
            }
        });

        JButton fs = new FlatButton("Pantalla completa", true);
        fs.addActionListener(e -> callServerAction(() ->
                server.toggleFullScreen(SlideShowServerImpl.SERVER_CONTROL_ID, UUID.randomUUID().toString())));

        side.add(loadPdfBtn);
        side.add(Box.createVerticalStrut(6));
        side.add(loadFolderBtn);
        side.add(Box.createVerticalStrut(6));
        side.add(startBtn);
        side.add(Box.createVerticalStrut(16));
        side.add(separator());
        side.add(Box.createVerticalStrut(16));
        side.add(prev);
        side.add(Box.createVerticalStrut(6));
        side.add(next);
        side.add(Box.createVerticalStrut(6));
        side.add(goTo);
        side.add(Box.createVerticalStrut(6));
        side.add(fs);
        side.add(Box.createVerticalStrut(16));

        stateLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        stateLabel.setFont(ServerTheme.FONT_SMALL);
        stateLabel.setForeground(ServerTheme.TEXT_MUTED);
        side.add(stateLabel);
        side.add(Box.createVerticalGlue());

        return side;
    }

    private JComponent separator() {
        JPanel line = new JPanel();
        line.setBackground(ServerTheme.OUTLINE_SOFT);
        line.setAlignmentX(Component.LEFT_ALIGNMENT);
        line.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        line.setPreferredSize(new Dimension(1, 1));
        return line;
    }

    // Acciones de carga 

    private void loadFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setAcceptAllFileFilterUsed(true);
        chooser.addChoosableFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Presentaciones y archivos soportados", "pdf", "pptx", "png", "jpg", "jpeg", "bmp", "gif"));
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            new SwingWorker<Void, Void>() {
                Exception error;
                @Override protected Void doInBackground() {
                    try { server.loadFile(file); } catch (Exception ex) { error = ex; }
                    return null;
                }
                @Override protected void done() {
                    if (error != null) {
                        JOptionPane.showMessageDialog(ServerControlPanel.this, "Error cargando archivo: " + error.getMessage());
                    }
                    refreshDeckList();
                }
            }.execute();
        }
    }

    private void loadPngFolder() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File folder = chooser.getSelectedFile();
            try {
                server.loadPngFolder(folder);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error cargando carpeta: " + ex.getMessage());
            }
            refreshDeckList();
        }
    }

    private void startSelectedDeck() {
        DeckInfo selected = deckList.getSelectedValue();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Seleccioná un deck primero");
            return;
        }
        callServerAction(() -> server.startPresentation(
                SlideShowServerImpl.SERVER_CONTROL_ID, UUID.randomUUID().toString(), selected.getId()));
    }

    private void refreshDeckList() {
        deckListModel.clear();
        for (DeckInfo d : server.getDeckManager().listAll().stream().map(com.slideshow.server.Deck::toDeckInfo).toList()) {
            deckListModel.addElement(d);
        }
    }

    private void callServerAction(RemoteAction call) {
        try {
            ActionResult result = call.execute();
            stateLabel.setText(result.getState() != null ? result.getState().toString() : result.getMessage());
            if (result.getStatus() == com.slideshow.common.ActionStatus.ERROR
                    || result.getStatus() == com.slideshow.common.ActionStatus.COOLDOWN_REJECTED) {
                // No es un error grave, solo se refleja en el label/log; se evita interrumpir con diálogos.
            }
        } catch (RemoteException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Error comunicándose con el servidor: " + ex.getMessage()
            );
        }
    }

    @FunctionalInterface
    private interface RemoteAction {
        ActionResult execute() throws RemoteException;
    }

    private void onActivityEntry(ActivityLogger.Entry entry) {
        LOGGER.info(() -> "[ACTIVIDAD] "
            + "hora=" + entry.formattedTime()
            + " | usuario=" + entry.username()
            + " | accion=" + entry.action()
            + " | resultado=" + entry.status()
            + " | detalle=" + entry.detail());
    }
}
