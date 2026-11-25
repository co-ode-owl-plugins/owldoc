package org.coode.browser.protege;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.io.File;
import java.util.Locale;
import javax.swing.*;
import org.apache.log4j.Logger;
import org.coode.html.OntologyExporter;
import org.protege.editor.core.ui.progress.BackgroundTask;
import org.protege.editor.core.Fonts;
import org.protege.editor.core.ui.util.NativeBrowserLauncher;

/** A status-area task. Worker updates never enqueue a Swing event per entity. */
final class ExportProgressPanel extends JPanel implements BackgroundTask, OntologyExporter.ProgressListener {
    private static final Logger LOGGER = Logger.getLogger(ExportProgressPanel.class);
    private final JLabel summary = new JLabel("OWLDoc — preparing export");
    private final JLabel detail = new JLabel("Preparing export");
    private final JProgressBar bar = new JProgressBar(0, 1000);
    private final JButton cancel = new JButton("Cancel");
    private final JButton open = new JButton("Open");
    private final Timer timer;
    private final Runnable dismiss;
    private final long started = System.nanoTime();
    private volatile Update latest = new Update("Preparing export", 0, 0);
    private volatile boolean cancelled;
    private boolean finished;

    ExportProgressPanel(Runnable dismiss) {
        super(new BorderLayout(8, 0));
        this.dismiss = dismiss;
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(3, 0, 3, 16));
        JPanel labels = new JPanel(new GridLayout(2, 1));
        labels.setOpaque(false);
        summary.setFont(Fonts.getSmallDialogFont());
        detail.setFont(Fonts.getSmallDialogFont());
        detail.setForeground(new java.awt.Color(105, 110, 118));
        cancel.setFont(Fonts.getSmallDialogFont());
        open.setFont(Fonts.getSmallDialogFont());
        labels.add(summary);
        labels.add(detail);
        add(labels, BorderLayout.CENTER);
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        controls.setOpaque(false);
        bar.setPreferredSize(new Dimension(80, 12));
        bar.setIndeterminate(true);
        bar.getAccessibleContext().setAccessibleName("OWLDoc export progress");
        controls.add(bar);
        controls.add(open);
        controls.add(cancel);
        open.setVisible(false);
        add(controls, BorderLayout.EAST);
        setPreferredSize(new Dimension(540, 34));
        setMaximumSize(new Dimension(620, 34));
        cancel.setToolTipText("Cancel after the current page finishes");
        cancel.addActionListener(event -> {
            if (finished) dismiss.run();
            else {
                cancelled = true;
                cancel.setEnabled(false);
                refreshProgress();
            }
        });
        timer = new Timer(250, event -> refreshProgress());
        timer.start();
    }

    @Override public void updateProgress(String stage, int completed, int total) {
        latest = new Update(stage, completed, total);
    }

    @Override public boolean isCancelled() { return cancelled; }

    void requestCancellation() { cancelled = true; }

    private String elapsed() {
        long seconds = (System.nanoTime() - started) / 1_000_000_000L;
        return String.format(Locale.ROOT, "%d:%02d", seconds / 60, seconds % 60);
    }

    void refreshProgress() {
        Update update = latest;
        int percent = update.total == 0 ? 0 : (int) (100L * update.completed / update.total);
        summary.setText(update.total == 0 ? "OWLDoc — preparing export · " + elapsed()
                : String.format(Locale.ROOT, "OWLDoc — %,d / %,d entities · %d%% · %s",
                        update.completed, update.total, percent, elapsed()));
        detail.setText(cancelled ? "Cancelling after the current page…" : update.stage);
        detail.setToolTipText(detail.getText());
        summary.setToolTipText(summary.getText());
        bar.setIndeterminate(update.total == 0);
        if (update.total > 0) bar.setValue((int) Math.min(1000, 1000L * update.completed / update.total));
    }

    /** Invoked on the event dispatch thread, after the worker and resource cleanup finish. */
    void finish(File index, Throwable failure) {
        timer.stop();
        finished = true;
        bar.setIndeterminate(false);
        if (index != null) {
            bar.setValue(1000);
            summary.setText(String.format(Locale.ROOT, "OWLDoc exported %,d entities · %s", latest.completed, elapsed()));
            detail.setText("Export complete");
            detail.setToolTipText(index.getParent());
            open.setVisible(true);
            open.addActionListener(event -> {
                try { NativeBrowserLauncher.openURL(index.toURI().toString()); }
                catch (Exception exception) { LOGGER.error("Could not open OWLDoc", exception); }
            });
        } else if (cancelled || failure instanceof java.util.concurrent.CancellationException) {
            summary.setText("OWLDoc export cancelled · " + elapsed());
            detail.setText("Partial files remain in the output directory");
        } else {
            summary.setText("OWLDoc export failed · " + elapsed());
            String message = failure == null ? "See the Protégé log for details" : failure.getMessage();
            detail.setText(message == null ? failure.getClass().getSimpleName() : message);
            detail.setToolTipText(detail.getText());
        }
        summary.setToolTipText(summary.getText());
        cancel.setText("Dismiss");
        cancel.setToolTipText("Dismiss export status");
        cancel.setEnabled(true);
    }

    void stopTimer() { timer.stop(); }

    @Override public String toString() { return "Exporting OWLDoc: " + latest.stage; }

    private static final class Update {
        final String stage;
        final int completed, total;
        Update(String stage, int completed, int total) {
            this.stage = stage;
            this.completed = completed;
            this.total = total;
        }
    }
}
