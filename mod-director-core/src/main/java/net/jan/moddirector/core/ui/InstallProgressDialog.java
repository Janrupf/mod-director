package net.jan.moddirector.core.ui;

import net.jan.moddirector.core.configuration.ModpackConfiguration;
import net.jan.moddirector.core.manage.ProgressCallback;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;

public class InstallProgressDialog extends JDialog {
    private static final int HEIGHT = 400;
    private static final int WIDTH = (int) (HEIGHT * /* golden ratio */ 1.618);

    private final JPanel contentPanel;

    public InstallProgressDialog(ModpackConfiguration configuration) {
        contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

        setTitle(configuration.packName());

        JLabel titleLabel = new JLabel("Installing " + configuration.packName(), SwingConstants.CENTER);
        titleLabel.setFont(new Font(titleLabel.getFont().getName(), Font.BOLD, 20));
        titleLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, titleLabel.getMinimumSize().height));
        contentPanel.add(titleLabel);

        if (configuration.icon() != null) {
            JLabel iconLabel = ImageLoader.createLabelForImage(configuration.icon(), 64, 64);
            iconLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, iconLabel.getMaximumSize().height));
            contentPanel.add(iconLabel);
        }

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setSize(WIDTH, HEIGHT);

        setContentPane(contentPanel);
        setSize(WIDTH, HEIGHT);
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
            if (done) {
                return;
            }

            SwingUtilities.invokeLater(() -> {
                contentPanel.remove(progressBar);
                contentPanel.revalidate();
            });
            done = true;

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
