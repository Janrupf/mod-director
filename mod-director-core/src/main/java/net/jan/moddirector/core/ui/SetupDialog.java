package net.jan.moddirector.core.ui;

import net.jan.moddirector.core.configuration.modpack.ModpackConfiguration;
import net.jan.moddirector.core.configuration.modpack.ModpackIconConfiguration;
import net.jan.moddirector.core.manage.ProgressCallback;
import net.jan.moddirector.core.manage.select.InstallSelector;
import net.jan.moddirector.core.ui.page.ModSelectionPage;
import net.jan.moddirector.core.ui.page.ProgressPage;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.concurrent.CountDownLatch;

public class SetupDialog extends JDialog {
    private static final int HEIGHT = 400;
    private static final int WIDTH = (int) (HEIGHT * /* golden ratio */ 1.618);

    private final ModpackConfiguration configuration;

    private final JButton nextButton;
    private CountDownLatch nextLatch;

    public SetupDialog(ModpackConfiguration configuration) {
        this.configuration = configuration;

        this.nextButton = new JButton("Next");
        this.nextButton.addActionListener((e) -> nextLatch.countDown());

        setTitle(configuration.packName());
        setSize(WIDTH, HEIGHT);
    }

    public ProgressPage navigateToProgressPage(String title) {
        ProgressPage page = new ProgressPage(configuration, title);
        return updateContent(page, false);
    }

    public ModSelectionPage navigateToSelectionPage(InstallSelector installSelector) {
        ModSelectionPage page = new ModSelectionPage(installSelector);
        return updateContent(page, true);
    }

    private <T extends JPanel> T updateContent(T newContent, boolean hasNextButton) {
        newContent.setPreferredSize(new Dimension(WIDTH - 30, 0));
        newContent.setMaximumSize(new Dimension(WIDTH - 30, Integer.MAX_VALUE));

        JPanel wrapperPanel = new JPanel();
        wrapperPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        wrapperPanel.setLayout(new BoxLayout(wrapperPanel, BoxLayout.PAGE_AXIS));

        JScrollPane scrollPane = new JScrollPane(newContent);
        scrollPane.setSize(WIDTH, HEIGHT - 30);
        scrollPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, HEIGHT - 55));
        wrapperPanel.add(scrollPane);

        JPanel creditsPanel = new JPanel();
        creditsPanel.setMaximumSize(new Dimension(WIDTH, 30));
        creditsPanel.setLayout(new BorderLayout());

        wrapperPanel.add(Box.createVerticalStrut(5));

        creditsPanel.add(new JLabel("Powered by ModDirector"), BorderLayout.WEST);
        wrapperPanel.add(creditsPanel);

        if(hasNextButton) {
            creditsPanel.add(nextButton, BorderLayout.EAST);
            nextButton.setAlignmentX(Component.RIGHT_ALIGNMENT);
            nextLatch = new CountDownLatch(1);
        } else {
            nextLatch = null;
        }

        wrapperPanel.setSize(WIDTH, HEIGHT);

        setContentPane(wrapperPanel);
        revalidate();

        return newContent;
    }

    public void waitForNext() throws InterruptedException {
        if(nextLatch == null) {
            throw new IllegalStateException("Can't wait for a next press on a page which has no next button");
        }

        nextLatch.await();
    }
}
