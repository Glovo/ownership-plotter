package com.glovoapp.ownership.shared;

import static java.awt.EventQueue.invokeLater;
import static java.lang.String.join;
import static java.lang.System.currentTimeMillis;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.awt.Font;
import java.awt.GridLayout;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import lombok.SneakyThrows;

public final class ProgressWindow extends JFrame {

    private final long startTimeMillis = currentTimeMillis();
    private final long totalOperationsCount;
    private final AtomicLong performedOperationsCount = new AtomicLong(0);
    private final JProgressBar progressBar = new JProgressBar(0, 100);
    private final JLabel progressText = new LabelWithDynamicTextSize();

    @SneakyThrows
    public static Callback start(final String title, final long totalOperationsCount) {
        final CompletableFuture<Callback> callbackFuture = new CompletableFuture<>();
        invokeLater(() -> new ProgressWindow(title, totalOperationsCount, callbackFuture));
        return callbackFuture.get();
    }

    private ProgressWindow(final String title,
                           final long totalOperationsCount,
                           final CompletableFuture<Callback> callbackFuture) {
        super(title);
        if (totalOperationsCount <= 0) {
            throw new IllegalArgumentException("total operations count must be greater than zero");
        }
        this.totalOperationsCount = totalOperationsCount;

        setVisible(true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new GridLayout(2, 1));

        add(progressBar);
        progressBar.setValue(0);
        progressBar.setStringPainted(true);

        add(progressText);

        callbackFuture.complete(new Callback());
    }

    private static final class LabelWithDynamicTextSize extends JLabel {

        @Override
        public final void validate() {
            final Font labelFont = getFont();
            final String labelText = getText();
            final int stringWidth = getFontMetrics(labelFont).stringWidth(labelText);
            final int componentWidth = getWidth();
            final double widthRatio = (double) componentWidth / (double) stringWidth;
            final int newFontSize = (int) (labelFont.getSize() * widthRatio);
            final int componentHeight = getHeight();
            final int fontSizeToUse = Math.min(newFontSize, componentHeight);

            setFont(new Font(labelFont.getName(), Font.PLAIN, fontSizeToUse));

            super.validate();
        }

    }

    public final class Callback {

        public final void incrementPerformedOperations() {
            long currentOperationsCount = performedOperationsCount.incrementAndGet();
            long oldPercentage = ((currentOperationsCount - 1) * 100) / totalOperationsCount;
            long currentPercentage = (currentOperationsCount * 100) / totalOperationsCount;
            if (oldPercentage != currentPercentage) {
                ProgressWindow.this.progressBar.setValue((int) currentPercentage);
                final long timeNow = currentTimeMillis();
                final long elapsedTime = timeNow - startTimeMillis;
                final long estimatedRemainingMillis = (elapsedTime * totalOperationsCount) / currentOperationsCount;
                progressText.setText(
                    "Estimated time to completion: " + formatDurationMillis(estimatedRemainingMillis)
                );
            }
        }

        private String formatDurationMillis(final long timeMillis) {
            final long timeSeconds = MILLISECONDS.toSeconds(timeMillis);
            final long hours = timeSeconds / 3_600;
            final long minutes = (timeSeconds % 3_600) / 60;
            final long seconds = timeSeconds % 60;
            return join(" ",
                (hours > 0 ? hours + "h" : ""),
                (minutes > 0 ? minutes + "m" : ""),
                (seconds > 0 ? seconds + "s" : "")
            );
        }

    }

}
