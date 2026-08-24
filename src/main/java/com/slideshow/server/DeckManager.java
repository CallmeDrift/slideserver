package com.slideshow.server;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFSlide;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Encargado de cargar decks (repito, TCG referencia :3 capaz y lo llamo manzanitas) desde disco
 * y mantenerlos disponibles en memoria por id.
 */
public class DeckManager {

    private final Map<String, Deck> decks = new ConcurrentHashMap<>();

    public Deck loadFromFile(File file) throws IOException {
        String name = file.getName().toLowerCase(Locale.ROOT);
        if (name.endsWith(".pdf")) {
            return loadFromPdf(file);
        }
        if (name.endsWith(".pptx")) {
            return loadFromPptx(file);
        }
        if (isSupportedImage(name)) {
            return loadFromImageFile(file);
        }
        throw new IOException("Formato no soportado: " + file.getName() + " (soportados: .pdf, .pptx, imágenes)");
    }

    public Deck loadFromPdf(File pdfFile) throws IOException {
        String id = UUID.randomUUID().toString();
        List<BufferedImage> images = new ArrayList<>();

        try (PDDocument document = Loader.loadPDF(pdfFile)) {
            PDFRenderer renderer = new PDFRenderer(document);
            int pageCount = document.getNumberOfPages();
            for (int i = 0; i < pageCount; i++) {
                BufferedImage img = renderer.renderImageWithDPI(i, 150);
                images.add(img);
            }
        }

        if (images.isEmpty()) {
            throw new IOException("El PDF no tiene páginas (por alguna razon): " + pdfFile.getName());
        }

        String name = pdfFile.getName();
        Deck deck = new Deck(id, name, "PDF", images);
        decks.put(id, deck);
        return deck;
    }

    public Deck loadFromPptx(File pptxFile) throws IOException {
        String id = UUID.randomUUID().toString();
        List<BufferedImage> images = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(pptxFile);
             XMLSlideShow ppt = new XMLSlideShow(fis)) {
            Dimension pgsize = ppt.getPageSize();
            for (XSLFSlide slide : ppt.getSlides()) {
                BufferedImage img = new BufferedImage(pgsize.width, pgsize.height, BufferedImage.TYPE_INT_RGB);
                Graphics2D graphics = img.createGraphics();
                try {
                    graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                    graphics.setPaint(Color.WHITE);
                    graphics.fillRect(0, 0, pgsize.width, pgsize.height);
                    slide.draw(graphics);
                } finally {
                    graphics.dispose();
                }
                images.add(img);
            }
        }

        if (images.isEmpty()) {
            throw new IOException("El PPTX no tiene diapositivas: " + pptxFile.getName());
        }

        Deck deck = new Deck(id, pptxFile.getName(), "PPTX", images);
        decks.put(id, deck);
        return deck;
    }

    public Deck loadFromImageFile(File imageFile) throws IOException {
        BufferedImage img = ImageIO.read(imageFile);
        if (img == null) {
            throw new IOException("No se pudo leer la imagen: " + imageFile.getName());
        }
        String id = UUID.randomUUID().toString();
        Deck deck = new Deck(id, imageFile.getName(), "IMAGE_FILE", List.of(img));
        decks.put(id, deck);
        return deck;
    }

    public Deck loadFromPngFolder(File folder) throws IOException {
        if (!folder.isDirectory()) {
            throw new IOException("No es una carpeta: " + folder.getAbsolutePath());
        }
        File[] files = folder.listFiles((dir, name) -> isSupportedImage(name.toLowerCase(Locale.ROOT)));
        if (files == null || files.length == 0) {
            throw new IOException("La carpeta no contiene imágenes soportadas: " + folder.getAbsolutePath());
        }
        Arrays.sort(files, Comparator.comparing(File::getName));

        List<BufferedImage> images = new ArrayList<>();
        for (File f : files) {
            BufferedImage img = ImageIO.read(f);
            if (img == null) {
                throw new IOException("No se pudo leer la imagen: " + f.getName());
            }
            images.add(img);
        }

        String id = UUID.randomUUID().toString();
        Deck deck = new Deck(id, folder.getName(), "IMAGE_FOLDER", images);
        decks.put(id, deck);
        return deck;
    }

    private boolean isSupportedImage(String fileNameLower) {
        return fileNameLower.endsWith(".png")
                || fileNameLower.endsWith(".jpg")
                || fileNameLower.endsWith(".jpeg")
                || fileNameLower.endsWith(".bmp")
                || fileNameLower.endsWith(".gif");
    }

    public Deck get(String deckId) {
        return decks.get(deckId);
    }

    public Collection<Deck> listAll() {
        return decks.values();
    }
}
