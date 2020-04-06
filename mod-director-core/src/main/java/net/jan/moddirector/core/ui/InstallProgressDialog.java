package net.jan.moddirector.core.ui;

import net.jan.moddirector.core.manage.ProgressCallback;

import javax.swing.*;
import javax.swing.border.TitledBorder;

public class InstallProgressDialog extends JDialog {
    private final JPanel contentPanel;

    public InstallProgressDialog() {
        contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setSize(400, 200);

        setContentPane(contentPanel);
        setSize(400, 200);
    }

    public ProgressCallback createProgressCallback(String title, String initialMessage) {
        return new VisualProgressCallback(title, initialMessage);
    }

    private class VisualProgressCallback implements ProgressCallback {
        private final JProgressBar progressBar;
        private int currentStep;
        boolean done;

        private VisualProgressCallback(String title, String initialMessage) {
            progressBar = new JProgressBar();
            progressBar.setString(initialMessage);
            progressBar.setStringPainted(true);
            progressBar.setBorder(new TitledBorder(title));

            SwingUtilities.invokeLater(() -> {
                contentPanel.add(progressBar);
                contentPanel.revalidate();
            });
            done = false;
            setSteps(1);
        }

        @Override
        public void setSteps(int steps) {
            currentStep = 0;
            SwingUtilities.invokeLater(() -> {
                progressBar.setMinimum(0);
                progressBar.setMaximum(steps * 100);
            });
        }

        @Override
        public void reportProgress(long current, long max) {
            int steppedPercent = (int) ((current * 100) / max) + (currentStep * 100);
            SwingUtilities.invokeLater(() -> progressBar.setValue(steppedPercent));
        }

        @Override
        public void message(String message) {
            SwingUtilities.invokeLater(() -> progressBar.setString(message));
        }

        @Override
        public void step() {
            currentStep++;
            reportProgress(0, 1);
        }

        @Override
        public void done() {
            if(done) {
                return;
            }

            synchronized(contentPanel) {
                SwingUtilities.invokeLater(() -> contentPanel.remove(progressBar));
                done = true;
            }
        }

        @Override
        public void title(String newTitle) {
            progressBar.setBorder(new TitledBorder(newTitle));
        }

        @Override
        public void indeterminate(boolean isIndeterminate) {
            progressBar.setIndeterminate(isIndeterminate);
        }
    }
}
