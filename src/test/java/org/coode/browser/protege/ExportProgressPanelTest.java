package org.coode.browser.protege;

import java.awt.Component;
import java.awt.Container;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class ExportProgressPanelTest {
    @Test public void appliesWorkerProgressOnTheEventThreadAndSupportsCancellation() throws Exception {
        ExportProgressPanel[] panel = new ExportProgressPanel[1];
        boolean[] dismissed = {false};
        SwingUtilities.invokeAndWait(() -> panel[0] = new ExportProgressPanel(() -> dismissed[0] = true));
        try {
            panel[0].updateProgress("Class: Example", 250, 1000);
            SwingUtilities.invokeAndWait(() -> {
                panel[0].refreshProgress();
                assertEquals(250, find(panel[0], JProgressBar.class).get(0).getValue());
                assertFalse(find(panel[0], JProgressBar.class).get(0).isIndeterminate());
                assertTrue(find(panel[0], JLabel.class).stream().anyMatch(label -> label.getText().contains("250 / 1,000")));
                JButton cancel = button(panel[0], "Cancel");
                cancel.doClick();
                assertTrue(panel[0].isCancelled());
                assertFalse(cancel.isEnabled());
                panel[0].finish(null, new java.util.concurrent.CancellationException());
                assertTrue(find(panel[0], JLabel.class).stream().anyMatch(label -> label.getText().contains("cancelled")));
                button(panel[0], "Dismiss").doClick();
                assertTrue(dismissed[0]);
            });
        } finally { SwingUtilities.invokeAndWait(() -> panel[0].stopTimer()); }
    }

    @Test public void completionOffersOpenWithoutAutomaticallyLaunchingABrowser() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            ExportProgressPanel panel = new ExportProgressPanel(() -> {});
            try {
                panel.updateProgress("Complete", 20, 20);
                panel.finish(new File("/tmp/owldoc-progress-test/index.html"), null);
                assertTrue(button(panel, "Open").isVisible());
                assertEquals(1000, find(panel, JProgressBar.class).get(0).getValue());
                assertTrue(find(panel, JLabel.class).stream().anyMatch(label -> label.getText().contains("20 entities")));
            } finally { panel.stopTimer(); }
        });
    }

    @Test public void failureIsShownInline() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            ExportProgressPanel panel = new ExportProgressPanel(() -> {});
            try {
                panel.finish(null, new IllegalStateException("Output directory is not writable"));
                assertFalse(button(panel, "Open").isVisible());
                assertTrue(find(panel, JLabel.class).stream().anyMatch(label -> label.getText().contains("not writable")));
                assertTrue(button(panel, "Dismiss").isEnabled());
            } finally { panel.stopTimer(); }
        });
    }

    private static JButton button(Container panel, String label) {
        return find(panel, JButton.class).stream().filter(button -> label.equals(button.getText())).findFirst().get();
    }

    private static <T> List<T> find(Container parent, Class<T> type) {
        List<T> found = new ArrayList<>();
        for (Component child : parent.getComponents()) {
            if (type.isInstance(child)) found.add(type.cast(child));
            if (child instanceof Container) found.addAll(find((Container) child, type));
        }
        return found;
    }
}
