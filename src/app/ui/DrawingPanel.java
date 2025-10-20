package app.ui;

import app.engine.*;
import app.object.*;
import app.util.RegistryManager;
import app.util.Vector3D;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.lang.management.ManagementFactory;

import com.sun.management.OperatingSystemMXBean;

public class DrawingPanel extends JPanel {
    private final MainWindow mainWindow;
    private Scene scene;
    private Camera camera;
    private ControlPanel controlPanel = null;
    private int lastMouseX, lastMouseY;
    private final Set<Integer> pressedKeys = new HashSet<>();
    private final Timer moveTimer;
    private volatile boolean isRendering = false;
    private BufferedImage renderBuffer;
    private BufferedImage displayBuffer;
    private ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    private long lastFpsUpdateTime = System.nanoTime();
    private int frameCount = 0;
    private double currentFps = 0.0;
    private int maxFps;
    private OperatingSystemMXBean osBean;
    private double currentCpuLoad = 0.0;
    private long currentMemoryUsage = 0;
    private long currentAllocatedMemory = 0;
    private Timer renderLoop;
    private int upscalingRatio = 1;
    private int reflectionMaxDepth = 10;
    private boolean showDebugInfo = true;
    private volatile boolean isSimulating = true;
    private boolean isBenchmarking = false;
    private int mouseSensitivity = 100;
    private int totalFrames = 0;
    private boolean isLoadingBenchmark = false;

    public DrawingPanel(Scene scene, Camera camera, MainWindow mainWindow) {
        this.mainWindow = mainWindow;
        this.scene = scene;
        this.camera = camera;
        this.setPreferredSize(new Dimension(740, 572));

        loadSettings();

        setFocusable(true);

        try {
            osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        } catch (ClassCastException e) {
            System.err.println("Warning: Could not cast to OperatingSystemMXBean. CPU load may not be available.");
            osBean = null;
        }

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                requestFocusInWindow();
                lastMouseX = e.getX();
                lastMouseY = e.getY();
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                int dx = e.getX() - lastMouseX;
                int dy = e.getY() - lastMouseY;
                lastMouseX = e.getX();
                lastMouseY = e.getY();
                if(!isBenchmarking) {
                    rotateCamera(dx, dy, mouseSensitivity);
                }
            }
        });

        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(e -> {
            if (e.getID() == KeyEvent.KEY_PRESSED) {
                pressedKeys.add(e.getKeyCode());
            } else if (e.getID() == KeyEvent.KEY_RELEASED) {
                pressedKeys.remove(e.getKeyCode());
            }
            return false;
        });

        moveTimer = new Timer(16, e -> {
            if(!isBenchmarking) {
                if (pressedKeys.contains(KeyEvent.VK_W)) {
                    moveCamera(0, 0, 0.1);
                }
                if (pressedKeys.contains(KeyEvent.VK_S)) {
                    moveCamera(0, 0, -0.1);
                }
                if (pressedKeys.contains(KeyEvent.VK_A)) {
                    moveCamera(-0.1, 0, 0);
                }
                if (pressedKeys.contains(KeyEvent.VK_D)) {
                    moveCamera(0.1, 0, 0);
                }
                if (pressedKeys.contains(KeyEvent.VK_R)) {
                    moveCamera(0, 0.1, 0);
                }
                if (pressedKeys.contains(KeyEvent.VK_F)) {
                    moveCamera(0, -0.1, 0);
                }
            }
        });
        moveTimer.start();
        initRenderingComponents();
    }
    public void initRenderingComponents() {
        if (executor == null || executor.isShutdown()) {
            executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        }

        if (renderLoop != null && renderLoop.isRunning()) {
            renderLoop.stop();
        }
        renderLoop = new Timer(950 / maxFps, e -> render());
        renderLoop.start();
    }

    public void render() {
        if(!isSimulating) return;
        if (isRendering) return;
        isRendering = true;

        int width;
        int height;

        if(upscalingRatio > 1) {
            width = getWidth() / upscalingRatio;
            height = getHeight() / upscalingRatio;
        }else {
            width = getWidth();
            height = getHeight();
        }

        if (renderBuffer == null || renderBuffer.getWidth() != width || renderBuffer.getHeight() != height) {
            renderBuffer = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            displayBuffer = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        }

        BufferedImage target = renderBuffer;

        CountDownLatch latch = new CountDownLatch(height);

        for (int y = 0; y < height; y++) {
            final int row = y;
            executor.submit(() -> {
                for (int x = 0; x < width; x++) {
                    Ray ray = camera.createRay(x, row, width, height);
                    IntersectionResult result = scene.findClosestIntersection(ray);
                    Color color = result.isHit() ? calculateShadingWithRayTracing(result, 0) : scene.getColor();
                    target.setRGB(x, row, color.getRGB());
                }
                latch.countDown();
            });
        }

        new Thread(() -> {
            try {
                latch.await();

                synchronized (this) {
                    BufferedImage tmp = displayBuffer;
                    displayBuffer = renderBuffer;
                    renderBuffer = tmp;
                }

                SwingUtilities.invokeLater(this::repaint);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                isRendering = false;
            }
        }).start();
    }


    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if(isBenchmarking && !isLoadingBenchmark)totalFrames++;

        synchronized (this) {
            Graphics2D g2d = (Graphics2D) g;

            g2d.setColor(scene.getColor());
            g2d.fillRect(0, 0, getWidth(), getHeight());

            if (displayBuffer != null) {
                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int drawWidth = displayBuffer.getWidth() * upscalingRatio;
                int drawHeight = displayBuffer.getHeight() * upscalingRatio;

                g2d.drawImage(displayBuffer, 0, 0, drawWidth, drawHeight, this);
            }
        }

        frameCount++;
        long now = System.nanoTime();
        long elapsedNanos = now - lastFpsUpdateTime;

        if (elapsedNanos >= 500_000_000L) {
            currentFps = (double) frameCount / (elapsedNanos / 1_000_000_000.0);
            frameCount = 0;
            lastFpsUpdateTime = now;

            if (osBean != null) {
                currentCpuLoad = osBean.getSystemCpuLoad() * 100.0;
            }
            Runtime runtime = Runtime.getRuntime();
            currentMemoryUsage = (runtime.totalMemory() - runtime.freeMemory());
            currentAllocatedMemory = runtime.maxMemory();
        }
        if(showDebugInfo || isBenchmarking){
            g.setColor(Color.LIGHT_GRAY);
            g.setFont(new Font("Monospaced", Font.BOLD, 14));
            if(isBenchmarking) {
                g.drawString("Raytracing Simulator Benchmark Release 1", 10, 20);
            }else {
                g.drawString("Raytracing Simulator", 10, 20);
            }
            g.drawString(String.format("%.2f fps, Max %d, Display: %dx%d", currentFps, maxFps, this.getSize().width, this.getSize().height), 10, 40);
            if (osBean != null) {
                if(currentCpuLoad >= 90) {
                    g.drawString(String.format("CPU: "), 10, 60);
                    g.setColor(new Color(240, 0, 0));
                    g.drawString(String.format("%.0f%%", currentCpuLoad), 50, 60);
                    g.setColor(Color.LIGHT_GRAY);
                    g.drawString(String.format(", %d valid cores", Runtime.getRuntime().availableProcessors()), 75, 60);
                }else{
                    g.drawString(String.format("CPU: "), 10, 60);
                    g.drawString(String.format("%.0f%%", currentCpuLoad), 50, 60);
                    g.drawString(String.format(", %d valid cores", Runtime.getRuntime().availableProcessors()), 75, 60);
                }
            }else {
                g.drawString("Could not cast to OperatingSystemMXBean.", 10, 60);
            }
            g.drawString(String.format("Mem: %d%% %.0f/%.0f MB",
                    Math.round((double) currentMemoryUsage / currentAllocatedMemory * 100),
                    currentMemoryUsage / (1024.0 * 1024.0),
                    currentAllocatedMemory / (1024.0 * 1024.0)
            ), 10, 80);
            if(upscalingRatio == 1) {
                g.drawString("Rendering scale: 1x", 10, 100);
            }else{
                g.drawString(String.format("Rendering scale: 1/%dx", upscalingRatio), 10, 100);
            }
            g.drawString(String.format("Max reflection depth: %d", reflectionMaxDepth), 10, 120);
            g.drawString(String.format("XYZ: %f / %f / %f", camera.getPosition().getX(), camera.getPosition().getY(), camera.getPosition().getZ()), 10, 140);
            double horizontalAngle = Math.toDegrees(Math.atan2(camera.getForward().getX(), camera.getForward().getZ()));
            double verticalAngle = Math.toDegrees(Math.atan2(camera.getForward().getY(), Math.sqrt(camera.getForward().getX() * camera.getForward().getX() + camera.getForward().getZ() * camera.getForward().getZ())));
            g.drawString(String.format("Facing: %f / %f", horizontalAngle, verticalAngle), 10, 160);
            g.drawString(String.format("Total objects: %d", scene.getLights().size() + scene.getShapes().size()), 10, 180);
            if(isLoadingBenchmark) {
                g.drawString("Loading...", 10, 200);
            }else if(isBenchmarking) {
                g.drawString(String.format("Benchmarking... Score: %d", totalFrames), 10, 200);
            }
        }
        Toolkit.getDefaultToolkit().sync();
    }

    private Color calculateShadingWithRayTracing(IntersectionResult result, int depth) {
        if (depth > reflectionMaxDepth) return result.getShape().getMaterial().getColor();

        Material material = result.getShape().getMaterial();
        Vector3D hitPoint = result.getHitPoint();
        Vector3D normal = result.getNormal();
        Vector3D viewDir = result.getRay().getDirection().normalize().multiply(-1); //視線ベクトル
        Color baseColor = material.getColor();
        float baseR = baseColor.getRed() / 255.0f;
        float baseG = baseColor.getGreen() / 255.0f;
        float baseB = baseColor.getBlue() / 255.0f;
        float[] ambient = {0.05f, 0.05f, 0.05f};
        float[] pixelColor = {
                baseR * ambient[0],
                baseG * ambient[1],
                baseB * ambient[2]
        };

        //拡散ライティングと鏡面反射の計算
        for (LightPoint light : scene.getLights()) {
            Vector3D lightDir = light.getPosition().subtract(hitPoint).normalize();
            double diff = Math.max(0, normal.dot(lightDir));

            //シャドウ判定
            Ray shadowRay = new Ray(hitPoint.add(normal.multiply(0.0001)), lightDir);
            IntersectionResult shadowResult = scene.findClosestIntersection(shadowRay);
            if (!shadowResult.isHit() || shadowResult.getDistance() > hitPoint.distance(light.getPosition())) {
                float lightFactor = (float)(diff * light.getIntensity());

                //拡散反射光の追加
                pixelColor[0] += baseR * lightFactor;
                pixelColor[1] += baseG * lightFactor;
                pixelColor[2] += baseB * lightFactor;

                //反射ベクトルを計算
                Vector3D reflectDir = lightDir.subtract(normal.multiply(2 * lightDir.dot(normal))).normalize();
                //視線ベクトルと反射ベクトルの内積を取りそれを光沢度で累乗
                double spec = Math.pow(Math.max(0, viewDir.dot(reflectDir)), material.getShininess());
                float specularFactor = (float)(spec * light.getIntensity());

                pixelColor[0] += baseR * specularFactor;
                pixelColor[1] += baseG * specularFactor;
                pixelColor[2] += baseB * specularFactor;
            }
        }

        //純粋な鏡面反射
        double reflectivity = material.getReflectivity();
        if (reflectivity > 0.0) {
            Vector3D incident = result.getRay().getDirection().normalize();
            Vector3D reflectedDir = incident.subtract(normal.multiply(2 * incident.dot(normal))).normalize();
            Ray reflectedRay = new Ray(hitPoint.add(normal.multiply(0.0001)), reflectedDir);
            IntersectionResult reflectedResult = scene.findClosestIntersection(reflectedRay);

            Color reflectedColor;
            if (reflectedResult.isHit()) {
                reflectedColor = calculateShadingWithRayTracing(reflectedResult, depth + 1);
            } else {
                reflectedColor = scene.getColor(); //背景色で代用
            }

            pixelColor[0] = (float) ((1 - reflectivity) * pixelColor[0] + reflectivity * reflectedColor.getRed() / 255.0);
            pixelColor[1] = (float) ((1 - reflectivity) * pixelColor[1] + reflectivity * reflectedColor.getGreen() / 255.0);
            pixelColor[2] = (float) ((1 - reflectivity) * pixelColor[2] + reflectivity * reflectedColor.getBlue() / 255.0);
        }

        float r = Math.min(1f, pixelColor[0]);
        float g = Math.min(1f, pixelColor[1]);
        float b = Math.min(1f, pixelColor[2]);

        return new Color(r, g, b);
    }

    public void moveCamera(double dx, double dy, double dz) {
        camera.moveRelative(new Vector3D(dx, dy, dz));
    }
    public void rotateCamera(int dx, int dy, int mouseSensitivity) {
        camera.rotateYaw(-dx * mouseSensitivity * 0.002);
        camera.rotatePitch(-dy * mouseSensitivity * 0.002);
    }
    public void runBenchmark() {
        if (isBenchmarking) return;
        //強引にフルスクリーン解除
        isSimulating = true;
        mainWindow.setVisible(false);
        mainWindow.setVisible(true);
        isLoadingBenchmark = true;
        File tempFile = new File("./", "temp.rts");
        mainWindow.getMenuBarFromMainFrame().saveSceneWhenBenchmark(tempFile);
        mainWindow.getMenuBarFromMainFrame().disableAllMenuBar();
        rebootThread();
        totalFrames = 0;
        mainWindow.setSize(new Dimension(1040, 600));
        mainWindow.setResizable(false);
        controlPanel.disableAllComponents();
        Color[] colors = new Color[]{Color.RED, Color.ORANGE, Color.YELLOW, new Color(184, 210, 0), Color.GREEN, new Color(0, 144, 144), Color.CYAN, Color.BLUE, Color.MAGENTA, Color.PINK};
        int upscalingRatioSwap = upscalingRatio;
        upscalingRatio = 1;
        int maxFpsSwap = maxFps;
        maxFps = 60;
        int reflectionMaxDepthSwap = reflectionMaxDepth;
        reflectionMaxDepth = 127;
        controlPanel.clearAllObjects(true);
        camera.setUp(new Vector3D(0, 1, 0));
        camera.setPosition(new Vector3D(9, 0, 7));
        camera.setForward(new Vector3D(-1, 0, -0.5).normalize());

        Timer timerMain1 = new Timer(16, e -> {
            rotateCamera(-1, 0, 25);
            moveCamera(-0.05, 0, 0);
        });
        Timer timerMain2 = new Timer(16, e -> {
            moveCamera(-0.05, 0, 0);
        });
        Timer timerMain3 = new Timer(16, e -> {
            moveCamera(0.05, 0, 0);
        });
        Timer stopTimer3 = new Timer(20000, e -> {
            timerMain3.stop();
            ((Timer) e.getSource()).stop();
            scene.getShapes().forEach(scene::removeShape);
            camera.setPosition(new Vector3D(0, 0, 5));
            camera.setForward(new Vector3D(0, 0, -1));
            camera.setUp(new Vector3D(0, 1, 0));
            isBenchmarking = false;
            upscalingRatio = upscalingRatioSwap;
            maxFps = maxFpsSwap;
            reflectionMaxDepth = reflectionMaxDepthSwap;
            controlPanel.enableAllComponents();
            mainWindow.setResizable(true);
            mainWindow.getMenuBarFromMainFrame().loadSceneWhenBenchmark(tempFile);
            tempFile.delete();
            JOptionPane.showMessageDialog(this, String.format("ベンチマークスコア: %d", totalFrames), "結果", JOptionPane.PLAIN_MESSAGE);
            java.util.List<Integer> scores = new ArrayList<>(Arrays.stream(RegistryManager.loadBenchmarkScore()).boxed().toList());
            scores.add(totalFrames);
            RegistryManager.saveBenchmarkScore(scores.stream().mapToInt(Integer::intValue).toArray());
            loadSettings();
            rebootThread();
            controlPanel.getRemoveButton().setEnabled(false);
            mainWindow.getMenuBarFromMainFrame().enableAllMenuBar();
        });
        Timer stopTimer2 = new Timer(20000, e -> {
            isLoadingBenchmark = true;
            timerMain2.stop();
            ((Timer) e.getSource()).stop();
            scene.getShapes().forEach(scene::removeShape);
            for(int i = 0; i < 19; i++) {
                scene.addShape(new Cube(new Vector3D(0, 0, i * 4 - 40), 3, new Material(colors[i % colors.length], 0.05 * (i % colors.length) + 0.25)));
            }
            camera.setPosition(new Vector3D(8, 0, 25));
            camera.setForward(new Vector3D(-1, 0, -0).normalize());
            timerMain3.start();
            stopTimer3.start();
            isLoadingBenchmark = false;
        });
        Timer stopTimer1 = new Timer(20000, e -> {
            isLoadingBenchmark = true;
            timerMain1.stop();
            ((Timer) e.getSource()).stop();
            scene.getShapes().forEach(scene::removeShape);
            for(int i = 0; i < 19; i++) {
                scene.addShape(new Tetrahedron(new Vector3D(0, 0, i * 4 - 40), 4, new Material(colors[i % colors.length], 0.05 * (i % colors.length) + 0.25)));
            }
            camera.setPosition(new Vector3D(0, 8, 20));
            camera.setForward(new Vector3D(1, 0, 0).normalize());
            camera.rotatePitch(-90);
            timerMain2.start();
            stopTimer2.start();
            isLoadingBenchmark = false;
        });

        new Timer(500, e -> {
            isLoadingBenchmark = false;
            ((Timer)e.getSource()).stop();
            for(int i = 0; i < 49; i++) {
                scene.addShape(new Sphere(new Vector3D(0, 0, i * 4 - 70), 2, new Material(colors[i % colors.length], 0.05 * (i % colors.length) + 0.25)));
            }
            timerMain1.start();
            stopTimer1.start();
            isBenchmarking = true;
        }).start();
    }

    public void shutdown() {
        executor.shutdown();
    }
    public void setMaxFps(int fps) {
        this.maxFps = fps;
        renderLoop.stop();
        renderLoop = new Timer(1000 / maxFps, e -> render());
        renderLoop.start();
    }
    public void loadSettings() {
        maxFps = RegistryManager.loadMaxFps(30);
        upscalingRatio = RegistryManager.loadUpscalingRatio(1);
        reflectionMaxDepth = RegistryManager.loadReflectionMaxDepth(10);
        scene.setColor(RegistryManager.loadBackgroundColor(Color.DARK_GRAY));
        showDebugInfo = RegistryManager.loadShowDebugInfo(true);
        int[] scores = RegistryManager.loadBenchmarkScore();
        int max = 0;
        for(int i = 0; i < scores.length; i++) {
            if(max < scores[i]) {
                max = scores[i];
            }
        }
        totalFrames = max;
    }
    public void setUpscalingRatio(int r) {
        this.upscalingRatio = r;
    }
    public void setReflectionMaxDepth(int i) {
        this.reflectionMaxDepth = i;
    }
    public void setShowDebugInfo(boolean b) {
        this.showDebugInfo = b;
    }
    public void setIsSimulating(boolean b) {
        if(!isBenchmarking) {
            isSimulating = b;
        }
        if(isSimulating) {
            mainWindow.setTitle(MainWindow.APPLICATION_TITLE);
        }else{
            mainWindow.setTitle(MainWindow.APPLICATION_TITLE + "(Paused)");
        }

    }
    public void setIsBenchmarking(boolean b) {
        isBenchmarking = b;
    }
    public void setScene(Scene scene) {
        this.scene = scene;
    }
    public void setCamera(Camera camera) {
        this.camera = camera;
    }
    public int getReflectionMaxDepth() {
        return reflectionMaxDepth;
    }
    public int getMaxFps() {
        return maxFps;
    }
    public int getUpscalingRatio() {
        return upscalingRatio;
    }
    public int getTotalFrames() {
        return totalFrames;
    }
    public Scene getScene() {
        return scene;
    }
    public Camera getCamera() {
        return camera;
    }
    public MainWindow getRaytracingSimulator() {
        return mainWindow;
    }
    public ControlPanel getControlPanel() {
        return controlPanel;
    }
    public boolean isShowDebugInfo() {
        return showDebugInfo;
    }
    public boolean isSimulating() {
        return isSimulating;
    }
    public boolean isBenchmarking() {
        return isBenchmarking;
    }
    public void rebootThread() {
        renderLoop.stop();
        shutdown();
        initRenderingComponents();
    }
    public void setControlPanel(ControlPanel controlPanel) {
        this.controlPanel = controlPanel;
    }
}