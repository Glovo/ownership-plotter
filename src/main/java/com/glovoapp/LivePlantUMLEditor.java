package com.glovoapp;

import static javax.swing.SwingUtilities.invokeLater;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.undo.UndoManager;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.plantuml.FileFormat;
import net.sourceforge.plantuml.FileFormatOption;
import net.sourceforge.plantuml.SourceStringReader;

@Slf4j
public final class LivePlantUMLEditor extends JFrame {

    private final static String TEMPORARY_DIRECTORY = System.getProperty("java.io.tmpdir");

    static {
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
    }

    private final AtomicReference<String> saveFileNameReference = new AtomicReference<>(
        TEMPORARY_DIRECTORY + "/" + LivePlantUMLEditor.class.getSimpleName() + ".save"
    );

    private final ExecutorService diagramRenderThreadPool = Executors.newFixedThreadPool(10);

    private final ImagePanel imagePanel = new ImagePanel();

    private LivePlantUMLEditor() {
        super(LivePlantUMLEditor.class.getSimpleName());
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new GridLayout(1, 2));
        setSize(500, 500);

        imagePanel.setBackground(Color.DARK_GRAY);

        final JTextArea diagramTextArea = new JTextArea();
        diagramTextArea.setBackground(Color.BLACK);
        diagramTextArea.setBorder(
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        );
        diagramTextArea.setFont(new Font("Fira Mono", Font.PLAIN, 20));
        diagramTextArea.setCaretColor(Color.WHITE);
        diagramTextArea.setForeground(Color.LIGHT_GRAY);
        final UndoManager undoManager = new UndoManager();
        diagramTextArea.getDocument()
                       .addUndoableEditListener(event -> undoManager.addEdit(event.getEdit()));
        final InputMap inputMap = diagramTextArea.getInputMap(JComponent.WHEN_FOCUSED);
        final ActionMap actionMap = diagramTextArea.getActionMap();

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, Toolkit.getDefaultToolkit()
                                                                  .getMenuShortcutKeyMask()), "Undo");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_Y, Toolkit.getDefaultToolkit()
                                                                  .getMenuShortcutKeyMask()), "Redo");

        actionMap.put("Undo", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (undoManager.canUndo()) {
                    undoManager.undo();
                }
            }
        });
        actionMap.put("Redo", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (undoManager.canRedo()) {
                    undoManager.redo();
                }
            }
        });

        readFile(saveFileNameReference.get()).ifPresent(diagramText -> {
            diagramTextArea.setText(diagramText);
            onDiagramTextUpdate(diagramText);
        });
        diagramTextArea.getDocument()
                       .addDocumentListener(new DocumentListener() {
                           @Override
                           public final void insertUpdate(final DocumentEvent documentEvent) {
                               onDiagramTextUpdate(diagramTextArea.getText());
                           }

                           @Override
                           public final void removeUpdate(final DocumentEvent documentEvent) {
                               onDiagramTextUpdate(diagramTextArea.getText());
                           }

                           @Override
                           public final void changedUpdate(final DocumentEvent documentEvent) {
                               onDiagramTextUpdate(diagramTextArea.getText());
                           }
                       });
        addScrollable(diagramTextArea);
        addScrollable(imagePanel);

        setVisible(true);
    }

    public static void main(final String... args) {
        invokeLater(LivePlantUMLEditor::new);
    }

    @SneakyThrows
    private static void overwriteFile(final String fileName, final String data) {
        final FileWriter myWriter = new FileWriter(fileName);
        myWriter.write(data);
        myWriter.close();
    }

    @SneakyThrows
    private static Optional<String> readFile(final String fileName) {
        try {
            return Optional.of(
                String.join(
                    "\n",
                    Files.readAllLines(Paths.get(fileName))
                )
            );
        } catch (final NoSuchFileException exception) {
            return Optional.empty();
        }
    }

    private void addScrollable(final Component component) {
        final JScrollPane scrollPane = new JScrollPane(component);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
//        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
//        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        add(scrollPane);
    }

    @SneakyThrows
    private void onDiagramTextUpdate(final String diagramText) {
        final LocalDateTime updateStartTime = LocalDateTime.now();
        diagramRenderThreadPool.submit(() -> {
            final SourceStringReader diagramData = new SourceStringReader(diagramText);
            final PipedInputStream in = new PipedInputStream();
            final PipedOutputStream out = new PipedOutputStream(in);
            diagramRenderThreadPool.submit(() -> {
                try {
                    final String generationDescription = diagramData.generateImage(
                        out,
                        new FileFormatOption(FileFormat.PNG)
                    );
                    log.info("diagram image generated {}", generationDescription);
                    out.close();
                    saveFileNameReference.updateAndGet(saveFileName -> {
                        overwriteFile(saveFileName, diagramText);
                        return saveFileName;
                    });
                } catch (final IOException exception) {
                    throw new RuntimeException(exception);
                }
            });
            final BufferedImage image = ImageIO.read(in);
            imagePanel.setImage(image, updateStartTime);
            return null; // to make it callable
        });
    }

    private static final class ImagePanel extends JPanel {

        private final AtomicReference<LocalDateTime> lastUpdateStartTimeReference = new AtomicReference<>(null);
        private final AtomicReference<BufferedImage> imageReference = new AtomicReference<>(null);

        private void setImage(final BufferedImage newImage, final LocalDateTime updateStartTime) {
            if (updateStartTime.equals(lastUpdateStartTimeReference.updateAndGet(lastUpdateStartTime ->
                Optional.ofNullable(lastUpdateStartTime)
                        .filter(updateStartTime::isBefore)
                        .orElse(updateStartTime)))
            ) {
                imageReference.set(newImage);
                repaint();
            } else {
                log.info("old update, ignoring");
            }
        }

        @Override
        protected void paintComponent(final Graphics graphics) {
            super.paintComponent(graphics);
            Optional.of(imageReference)
                    .map(AtomicReference::get)
                    .ifPresent(image -> {
                        final int imageWidth = image.getWidth(this);
                        final int imageHeight = image.getHeight(this);
                        final int x = (getWidth() - imageWidth) / 2;
                        final int y = (getHeight() - imageHeight) / 2;
                        graphics.drawImage(image, x, y, this);
                    });
        }

    }

}
