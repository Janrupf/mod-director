package net.jan.moddirector.core.ui.page;

import net.jan.moddirector.core.configuration.modpack.ModpackConfiguration;
import net.jan.moddirector.core.configuration.modpack.ModpackIconConfiguration;
import net.jan.moddirector.core.manage.ProgressCallback;
import net.jan.moddirector.core.ui.ImageLoader;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;

public class ProgressPage extends JPanel {
    public ProgressPage(ModpackConfiguration configuration, String title) {

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font(titleLabel.getFont().getName(), Font.BOLD, 20));
        titleLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, titleLabel.getMinimumSize().height));
        add(titleLabel);

        ModpackIconConfiguration icon = configuration.icon();
        if (icon != null) {
            JLabel iconLabel = ImageLoader.createLabelForImage(icon.path(), icon.width(), icon.height());
            iconLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, iconLabel.getMaximumSize().height));
            add(iconLabel);
        }
    }

    public ProgressCallback createProgressCallback(String title, String initialMessage) {
        return new VisualProgressCallback(title, initialMessage);
    }

    private class VisualProgressCallback implements ProgressCallback {
        private JProgressBar progressBar;
        private int currentStep;
        boolean done;

        private VisualProgressCallback(String title, String initialMessage) {
            SwingUtilities.invokeLater(() -> {
                progressBar = new JProgressBar();
                progressBar.setString(initialMessage);
                progressBar.setStringPainted(true);
                progressBar.setBorder(new TitledBorder(title));

                add(progressBar);
                revalidate();
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
                remove(progressBar);
                revalidate();
            });
            done = true;

        }

        @Override
        public void title(String newTitle) {
            SwingUtilities.invokeLater(() -> progressBar.setBorder(new TitledBorder(newTitle)));
        }

        @Override
        public void indeterminate(boolean isIndeterminate) {
            SwingUtilities.invokeLater(() -> progressBar.setIndeterminate(isIndeterminate));
        }
    }
}
