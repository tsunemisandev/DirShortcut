package com.dirshortcut;

import com.dirshortcut.ui.MainWindow;

import javax.swing.*;

public class Main {

    public static void main(String[] args) {
        // Use system look and feel for native OS appearance
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            MainWindow window = new MainWindow();
            window.setVisible(true);
        });
    }
}
