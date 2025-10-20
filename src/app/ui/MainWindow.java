package app.ui;

import app.engine.Camera;
import app.engine.Scene;
import app.object.*;
import app.util.Vector3D;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import java.net.URL;

public class MainWindow extends JFrame {
    public static final String APPLICATION_TITLE = "Raytracing Simulator";

    public static final long serialVersionUID = 0;

    private DrawingPanel drawingPanel;
    private ControlPanel controlPanel;
    private final MenuBar menuBar;

    public MainWindow() {
        setTitle(APPLICATION_TITLE);
        setSize(1040, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        System.setProperty("apple.laf.useScreenMenuBar", "true");

        loadIcon();

        addListener();

        Scene scene = new Scene();
        Camera camera = new Camera();

        LightPoint defaultLight = new LightPoint(new Vector3D(5, 5, 0), 1.0);

        drawingPanel = new DrawingPanel(scene, camera, this);
        controlPanel = new ControlPanel(drawingPanel, defaultLight);
        drawingPanel.setControlPanel(controlPanel);

        scene.addLight(defaultLight);

        add(drawingPanel, BorderLayout.CENTER);
        add(controlPanel, BorderLayout.EAST);

        menuBar = new MenuBar(this, drawingPanel, controlPanel);
        setJMenuBar(menuBar);

        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(600, 400));

    }
    public void loadIcon() {
        String[] iconFiles = {"/icon16.png", "/icon32.png", "/icon64.png", "/icon128.png", "/icon256.png", "/icon512.png"};
        List<Image> icons = new ArrayList<>();
        for (String iconFile : iconFiles) {
            URL url = getClass().getResource(iconFile);
            if (url != null) {
                icons.add(Toolkit.getDefaultToolkit().getImage(url));
            } else {
                System.err.println(iconFile + " not found");
            }
        }
        if (!icons.isEmpty()) {
            setIconImages(icons);
        }
        try {
            Taskbar taskbar = Taskbar.getTaskbar();
            URL url = getClass().getResource("/iconmac.png");
            if (url != null) {
                Image image = ImageIO.read(url);
                taskbar.setIconImage(image);
            }
        } catch (UnsupportedOperationException e) {
            System.err.println("Taskbar icon setting is not supported.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addListener() {
        addWindowFocusListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                drawingPanel.shutdown();
            }
            @Override
            public void windowGainedFocus(WindowEvent e) {
                drawingPanel.setIsSimulating(true);
            }

            @Override
            public void windowLostFocus(WindowEvent e) {
                drawingPanel.setIsSimulating(false);
            }
        });
    }
    public MenuBar getMenuBarFromMainFrame() {
        return menuBar;
    }
}
