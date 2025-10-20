package app;

import app.ui.MainWindow;

import javax.swing.*;

public class RaytracingSimulator {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainWindow().setVisible(true));
    }
}
