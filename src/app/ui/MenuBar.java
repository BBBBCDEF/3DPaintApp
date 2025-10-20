package app.ui;

import app.object.*;
import app.object.Shape;
import app.util.RegistryManager;
import app.util.SaveData;
import app.util.Vector3D;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.Random;

public class MenuBar extends JMenuBar{
    private final MainWindow mainWindow;
    private final DrawingPanel drawingPanel;
    private final ControlPanel controlPanel;
    private File file = null;
    private final Random random;
    private final JMenu fileMenu;
    private final JMenuItem saveMenuItem;
    private final JMenuItem overwriteMenuItem;
    private final JMenuItem loadMenuItem;
    private final JMenuItem saveAndCloseMenuItem;
    private final JMenuItem closeMenuItem;
    private final JMenuItem settingsMenuItem;
    private final JMenuItem initializeMenuItem;
    private final JMenu editMenu;
    private final JMenuItem addObjectMenuItem;
    private final JMenuItem removeObjectMenuItem;
    private final JMenuItem clearAllObjectMenuItem;
    private final JMenuItem setPositionCameraMenuItem;
    private final JMenuItem setPositionAlongViewMenuItem;
    private final JMenuItem quickReplaceShapeMenuItem;
    private final JMenuItem resizeBiggerMenuItem;
    private final JMenuItem resizeSmallerMenuItem;
    private final JMenuItem moveForwardXMenuItem;
    private final JMenuItem moveBackwardXMenuItem;
    private final JMenuItem moveForwardYMenuItem;
    private final JMenuItem moveBackwardYMenuItem;
    private final JMenuItem moveForwardZMenuItem;
    private final JMenuItem moveBackwardZMenuItem;
    private final JMenuItem randomRecolorMenuItem;
    private final JMenuItem cameraResetMenuItem;
    private final JMenu simulationMenu;
    private final JMenuItem stopSimulationMenuItem;
    private final JMenuItem startSimulationMenuItem;
    private final JMenuItem rebootThreadMenuItem;
    private final JMenu benchmarkMenu;
    private final JMenuItem startBenchmarkMenuItem;

    public MenuBar(MainWindow mainWindow, DrawingPanel drawingPanel, ControlPanel controlPanel) {
        this.mainWindow = mainWindow;
        this.drawingPanel = drawingPanel;
        this.controlPanel = controlPanel;

        random = new Random();

        //ファイル
        fileMenu = new JMenu("ファイル");
        saveMenuItem = new JMenuItem("保存");
        saveMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        overwriteMenuItem = new JMenuItem("上書き保存");
        overwriteMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx() | InputEvent.SHIFT_DOWN_MASK));
        loadMenuItem = new JMenuItem("開く");
        loadMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        saveAndCloseMenuItem = new JMenuItem("保存してシーンを閉じる");

        closeMenuItem = new JMenuItem("保存せずにシーンを閉じる");

        settingsMenuItem = new JMenuItem("設定");
        settingsMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_COMMA, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        initializeMenuItem = new JMenuItem("保存されたデータを初期化");

        saveMenuItem.addActionListener(e -> saveScene());
        overwriteMenuItem.addActionListener(e -> overwriteScene());
        loadMenuItem.addActionListener(e -> loadScene());
        saveAndCloseMenuItem.addActionListener(e -> closeAndSaveScene());
        closeMenuItem.addActionListener(e -> closeScene());
        settingsMenuItem.addActionListener(e -> openSettingsWindow());
        initializeMenuItem.addActionListener(e -> initializeData());

        fileMenu.add(saveMenuItem);
        fileMenu.add(overwriteMenuItem);
        fileMenu.add(loadMenuItem);
        fileMenu.add(saveAndCloseMenuItem);
        fileMenu.add(closeMenuItem);
        fileMenu.addSeparator();
        fileMenu.add(settingsMenuItem);
        fileMenu.addSeparator();
        fileMenu.add(initializeMenuItem);

        //編集
        editMenu = new JMenu("編集");
        addObjectMenuItem = new JMenuItem("オブジェクトを追加/変更を適用");
        addObjectMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        removeObjectMenuItem = new JMenuItem("選択したオブジェクトを削除");
        removeObjectMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        clearAllObjectMenuItem = new JMenuItem("全てのオブジェクトを削除");
        clearAllObjectMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx() | InputEvent.SHIFT_DOWN_MASK));
        setPositionCameraMenuItem = new JMenuItem("カメラの位置に座標をセット");
        setPositionCameraMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.ALT_DOWN_MASK));
        setPositionAlongViewMenuItem = new JMenuItem("カメラの前方に座標をセット");
        setPositionAlongViewMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.ALT_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK));
        quickReplaceShapeMenuItem = new JMenuItem("選択したオブジェクトの形状を即時変更");
        quickReplaceShapeMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.ALT_DOWN_MASK));
        moveForwardXMenuItem = new JMenuItem("選択したオブジェクトを+X方向に移動");
        moveForwardXMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        moveBackwardXMenuItem = new JMenuItem("選択したオブジェクトを-X方向に移動");
        moveBackwardXMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        moveForwardYMenuItem = new JMenuItem("選択したオブジェクトを+Y方向に移動");
        moveForwardYMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_UP, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        moveBackwardYMenuItem = new JMenuItem("選択したオブジェクトを-Y方向に移動");
        moveBackwardYMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        moveForwardZMenuItem = new JMenuItem("選択したオブジェクトを+Z方向に移動");
        moveForwardZMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_SLASH, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        moveBackwardZMenuItem = new JMenuItem("選択したオブジェクトを-Z方向に移動");
        moveBackwardZMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_QUOTE, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        resizeBiggerMenuItem = new JMenuItem("選択したオブジェクトを大きく");
        resizeBiggerMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        resizeSmallerMenuItem = new JMenuItem("選択したオブジェクトを小さく");
        resizeSmallerMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        randomRecolorMenuItem = new JMenuItem("選択したオブジェクトの色をランダムに変更");
        randomRecolorMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        cameraResetMenuItem = new JMenuItem("カメラの視点を水平に戻す");
        cameraResetMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));

        addObjectMenuItem.addActionListener(e -> controlPanel.getAddButton().doClick());
        removeObjectMenuItem.addActionListener(e -> controlPanel.removeSelectedObjectWithDialog());
        clearAllObjectMenuItem.addActionListener(e -> controlPanel.clearAllObjectsWithDialog(true));
        setPositionCameraMenuItem.addActionListener(e -> controlPanel.setTextFieldPosition(drawingPanel.getCamera().getPosition()));
        setPositionAlongViewMenuItem.addActionListener(e -> setPositionAlongView());
        quickReplaceShapeMenuItem.addActionListener(e -> quickReplace());
        moveForwardXMenuItem.addActionListener(e -> move(new Vector3D(1, 0, 0)));
        moveBackwardXMenuItem.addActionListener(e -> move(new Vector3D(-1, 0, 0)));
        moveForwardYMenuItem.addActionListener(e -> move(new Vector3D(0, 1, 0)));
        moveBackwardYMenuItem.addActionListener(e -> move(new Vector3D(0, -1, 0)));
        moveForwardZMenuItem.addActionListener(e -> move(new Vector3D(0, 0, 1)));
        moveBackwardZMenuItem.addActionListener(e -> move(new Vector3D(0, 0, -1)));
        resizeBiggerMenuItem.addActionListener(e -> resize(true));
        resizeSmallerMenuItem.addActionListener(e -> resize(false));
        randomRecolorMenuItem.addActionListener(e -> randomRecolor());
        cameraResetMenuItem.addActionListener(e -> drawingPanel.getCamera().cameraReset());

        editMenu.add(addObjectMenuItem);
        editMenu.add(removeObjectMenuItem);
        editMenu.add(clearAllObjectMenuItem);
        editMenu.addSeparator();
        editMenu.add(setPositionCameraMenuItem);
        editMenu.add(setPositionAlongViewMenuItem);
        editMenu.addSeparator();
        editMenu.add(quickReplaceShapeMenuItem);
        editMenu.add(randomRecolorMenuItem);
        editMenu.add(resizeBiggerMenuItem);
        editMenu.add(resizeSmallerMenuItem);
        editMenu.addSeparator();
        editMenu.add(moveForwardXMenuItem);
        editMenu.add(moveBackwardXMenuItem);
        editMenu.add(moveForwardYMenuItem);
        editMenu.add(moveBackwardYMenuItem);
        editMenu.add(moveForwardZMenuItem);
        editMenu.add(moveBackwardZMenuItem);
        editMenu.addSeparator();
        editMenu.add(cameraResetMenuItem);


        //シミュレーション
        simulationMenu = new JMenu("シミュレーション");
        stopSimulationMenuItem = new JMenuItem("レンダリングを停止");
        stopSimulationMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.ALT_DOWN_MASK));
        startSimulationMenuItem = new JMenuItem("レンダリングを再開");
        startSimulationMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.ALT_DOWN_MASK));
        rebootThreadMenuItem = new JMenuItem("レンダリングスレッドを再起動");
        rebootThreadMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F3, InputEvent.ALT_DOWN_MASK));

        stopSimulationMenuItem.addActionListener(e -> stopSimulation());
        startSimulationMenuItem.addActionListener(e -> startSimulation());
        rebootThreadMenuItem.addActionListener(e -> drawingPanel.rebootThread());

        simulationMenu.add(stopSimulationMenuItem);
        simulationMenu.add(startSimulationMenuItem);
        simulationMenu.add(rebootThreadMenuItem);

        //ベンチマーク
        benchmarkMenu = new JMenu("ベンチマーク");
        startBenchmarkMenuItem = new JMenuItem("ベンチマークを開始");
        startBenchmarkMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        //showScoreMenuItem = new JMenuItem("ベンチマークスコアを表示");

        startBenchmarkMenuItem.addActionListener(e -> runBenchmark());

        benchmarkMenu.add(startBenchmarkMenuItem);
        //benchmarkMenu.add(showScoreMenuItem);

        add(fileMenu);
        add(editMenu);
        add(simulationMenu);
        add(benchmarkMenu);

        refreshFileMenuItemsState();
        startSimulationMenuItem.setEnabled(false);

    }
    public void saveScene() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("保存");
        FileNameExtensionFilter filter = new FileNameExtensionFilter("RayTracingSimulator標準ファイル (*.rts)", "rts");
        fileChooser.setFileFilter(filter);
        fileChooser.setAcceptAllFileFilterUsed(false);

        while (true) {
            int userSelection = fileChooser.showSaveDialog(null);
            if (userSelection != JFileChooser.APPROVE_OPTION) {
                System.out.println("保存がキャンセルされました。");
                return;
            }

            File fileToSave = fileChooser.getSelectedFile();
            if (!fileToSave.getName().toLowerCase().endsWith(".rts")) {
                fileToSave = new File(fileToSave.getAbsolutePath() + ".rts");
            }

            if (fileToSave.exists()) {
                int overwrite = JOptionPane.showConfirmDialog(
                        null,
                        String.format("すでに\"%s\"が存在します。\n上書きしますか？", fileToSave.getName()),
                        "上書き確認",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE
                );
                if (overwrite != JOptionPane.YES_OPTION) {
                    continue;
                }
            }

            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(fileToSave))) {
                SaveData saveData = new SaveData(MainWindow.serialVersionUID, drawingPanel.getScene(), drawingPanel.getCamera(), controlPanel.getSelectedColor());
                oos.writeObject(saveData);
                file = new File(fileToSave.getAbsolutePath());
                System.out.println("ファイルを保存しました: " + fileToSave.getAbsolutePath());
            } catch (Exception e) {
                e.printStackTrace();
            }
            break;
        }

        refreshFileMenuItemsState();
    }


    public void saveSceneWhenBenchmark(File file) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            SaveData saveData = new SaveData(MainWindow.serialVersionUID, drawingPanel.getScene(), drawingPanel.getCamera(), controlPanel.getSelectedColor());
            oos.writeObject(saveData);
        } catch (Exception e) {
            e.printStackTrace();
        }
        refreshFileMenuItemsState();
    }
    public void overwriteScene() {
        if(file != null) {
            try(ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
                SaveData saveData = new SaveData(MainWindow.serialVersionUID, drawingPanel.getScene(), drawingPanel.getCamera(), controlPanel.getSelectedColor());
                oos.writeObject(saveData);
            }catch(Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void loadScene() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("読み込み");
        FileNameExtensionFilter filter = new FileNameExtensionFilter("データファイル (*.rts)", "rts");
        fileChooser.setFileFilter(filter);
        fileChooser.setAcceptAllFileFilterUsed(false);
        int userSelection = fileChooser.showOpenDialog(null);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToOpen = fileChooser.getSelectedFile();

            try (java.io.ObjectInputStream ois = new java.io.ObjectInputStream(new java.io.FileInputStream(fileToOpen))) {
                SaveData loadedData = (SaveData) ois.readObject();
                if(MainWindow.serialVersionUID != loadedData.getSerialVersionUID()) {
                    int result = JOptionPane.showConfirmDialog(mainWindow, "このシーンは古いRaytracingSimulator用です。開きますか？", "確認", JOptionPane.DEFAULT_OPTION);
                    if(result != JOptionPane.YES_OPTION) {
                        return;
                    }
                }
                controlPanel.clearAllObjects(false);
                for(LightPoint lightPoint : loadedData.getScene().getLights()) {
                    controlPanel.addSceneObject(lightPoint);
                }
                for(app.object.Shape shape : loadedData.getScene().getShapes()) {
                    controlPanel.addSceneObject(shape);
                }
                controlPanel.setSelectedColor(loadedData.getSelectedColor());

                drawingPanel.getCamera().setPosition(loadedData.getCamera().getPosition());
                drawingPanel.getCamera().setForward(loadedData.getCamera().getForward());
                drawingPanel.getCamera().setUp(loadedData.getCamera().getUp());

                file = new File(fileToOpen.getAbsolutePath());
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(mainWindow, "ファイルの読み込みに失敗しました。", "エラー", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            System.out.println("読み込みがキャンセルされました。");
        }
        refreshFileMenuItemsState();
    }
    public void loadSceneWhenBenchmark(File file) {
        try (java.io.ObjectInputStream ois = new java.io.ObjectInputStream(new java.io.FileInputStream(file))) {
            SaveData loadedData = (SaveData) ois.readObject();
            controlPanel.clearAllObjects(false);
            for(LightPoint lightPoint : loadedData.getScene().getLights()) {
                controlPanel.addSceneObject(lightPoint);
            }
            for(app.object.Shape shape : loadedData.getScene().getShapes()) {
                controlPanel.addSceneObject(shape);
            }
            controlPanel.setSelectedColor(loadedData.getSelectedColor());

            drawingPanel.getCamera().setPosition(loadedData.getCamera().getPosition());
            drawingPanel.getCamera().setForward(loadedData.getCamera().getForward());
            drawingPanel.getCamera().setUp(loadedData.getCamera().getUp());

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(mainWindow, "ベンチマーク実行前のシーンの復元に失敗しました。", "エラー", JOptionPane.ERROR_MESSAGE);
        }
    }
    public void closeAndSaveScene(){
        if(file != null) {
            overwriteScene();
        }else {
            saveScene();
        }
        refreshFileMenuItemsState();
    }
    public void closeScene() {
        int result = JOptionPane.showConfirmDialog(mainWindow, "保存せずにシーンを閉じますか？", "確認", JOptionPane.DEFAULT_OPTION);
        if(result == JOptionPane.YES_OPTION) {
            file = null;
            controlPanel.clearAllObjects(true);
            refreshFileMenuItemsState();
        }
    }

    public void openSettingsWindow() {
        SettingsWindow settingsWindow = new SettingsWindow(drawingPanel);
        settingsWindow.setVisible(true);
    }

    public void refreshFileMenuItemsState() {
        if(file == null) {
            overwriteMenuItem.setEnabled(false);
        }else {
            overwriteMenuItem.setEnabled(true);
        }
    }
    public void disableAllMenuBar() {
        setEnabled(false);

    }
    public void enableAllMenuBar() {
        setEnabled(true);
    }
    public void stopSimulation() {
        drawingPanel.setIsSimulating(false);
        refreshSimulationMenuItemsState();
    }
    public void startSimulation() {
        drawingPanel.setIsSimulating(true);
        refreshSimulationMenuItemsState();
    }
    public void refreshSimulationMenuItemsState() {
        if(drawingPanel.isSimulating()) {
            startSimulationMenuItem.setEnabled(false);
            stopSimulationMenuItem.setEnabled(true);
        }else {
            startSimulationMenuItem.setEnabled(true);
            stopSimulationMenuItem.setEnabled(false);
        }
    }
    public void runBenchmark() {
        int result = JOptionPane.showConfirmDialog(
                mainWindow,
                "ベンチマークを開始しますか？\n" +
                        "所要時間: 約1分",
                "確認",
                JOptionPane.YES_NO_OPTION
        );
        if(result == JOptionPane.YES_OPTION) {
            drawingPanel.runBenchmark();
        }
    }
    public void setPositionAlongView() {
        double r;
        try {
            r = controlPanel.getSizeFieldValue();
        }catch(NumberFormatException e) {
            return;
        }
        controlPanel.setTextFieldPosition(drawingPanel.getCamera().getPointAlongView(r * 1.1));
    }
    public void quickReplace() {
        Placeable selectedObject = controlPanel.getSelectedObject();
        int index = controlPanel.getObjectList().getSelectedIndex();
        if(selectedObject instanceof Shape) {
            Vector3D position = selectedObject.getPosition();
            Color color = ((app.object.Shape) selectedObject).getMaterial().getColor();
            double size = ((app.object.Shape) selectedObject).getSize();
            double reflectivity = ((Shape) selectedObject).getMaterial().getReflectivity();
            controlPanel.removeSelectedObject();
            if(selectedObject instanceof Cube) {
                controlPanel.addSceneObject(index, new Tetrahedron(position, size, new Material(color, reflectivity)));
            }else if(selectedObject instanceof Tetrahedron) {
                controlPanel.addSceneObject(index, new Sphere(position, size, new Material(color, reflectivity)));
            }else if(selectedObject instanceof Sphere) {
                controlPanel.addSceneObject(index, new Cube(position, size, new Material(color, reflectivity)));
            }
            controlPanel.getObjectList().setSelectedIndex(index);

        }
    }
    public void move(Vector3D v) {
        Placeable selectedObject = controlPanel.getSelectedObject();
        int index = controlPanel.getObjectList().getSelectedIndex();
        if(selectedObject instanceof app.object.Shape) {
            v.normalize().multiply(((app.object.Shape) selectedObject).getSize() / 2);
            Vector3D position = selectedObject.getPosition().add(v);
            Color color = ((app.object.Shape) selectedObject).getMaterial().getColor();
            double size = ((app.object.Shape) selectedObject).getSize();
            double reflectivity = ((Shape) selectedObject).getMaterial().getReflectivity();
            controlPanel.removeSelectedObject();
            if(selectedObject instanceof Cube) {
                controlPanel.addSceneObject(index, new Cube(position, size, new Material(color, reflectivity)));
            }else if(selectedObject instanceof Tetrahedron) {
                controlPanel.addSceneObject(index, new Tetrahedron(position, size, new Material(color, reflectivity)));
            }else if(selectedObject instanceof Sphere) {
                controlPanel.addSceneObject(index, new Sphere(position, size, new Material(color, reflectivity)));
            }else return;
            controlPanel.getObjectList().setSelectedIndex(index);
        }else if(selectedObject instanceof LightPoint) {
            v.normalize().multiply(0.5);
            Vector3D position = selectedObject.getPosition().add(v);
            double intensity = ((LightPoint) selectedObject).getIntensity();
            controlPanel.removeSelectedObject();
            controlPanel.addSceneObject(index, new LightPoint(position, intensity));
            controlPanel.getObjectList().setSelectedIndex(index);
        }
    }
    public void resize(boolean b) {
        Placeable selectedObject = controlPanel.getSelectedObject();
        int index = controlPanel.getObjectList().getSelectedIndex();
        if(selectedObject instanceof app.object.Shape) {
            Vector3D position = selectedObject.getPosition();
            Color color = ((app.object.Shape) selectedObject).getMaterial().getColor();
            double size = ((app.object.Shape) selectedObject).getSize();
            if(b) {
                size *= 2;
            }else {
                size /= 2;
            }
            double reflectivity = ((Shape) selectedObject).getMaterial().getReflectivity();
            controlPanel.removeSelectedObject();
            if(selectedObject instanceof Cube) {
                controlPanel.addSceneObject(index, new Cube(position, size, new Material(color, reflectivity)));
            }else if(selectedObject instanceof Tetrahedron) {
                controlPanel.addSceneObject(index, new Tetrahedron(position, size, new Material(color, reflectivity)));
            }else if(selectedObject instanceof Sphere) {
                controlPanel.addSceneObject(index, new Sphere(position, size, new Material(color, reflectivity)));
            }else return;
            controlPanel.getObjectList().setSelectedIndex(index);
        }else if(selectedObject instanceof LightPoint) {
            Vector3D position = selectedObject.getPosition();
            double intensity = ((LightPoint) selectedObject).getIntensity();
            controlPanel.removeSelectedObject();
            if(b) {
                intensity *= 2;
            }else {
                intensity /= 2;
            }
            controlPanel.addSceneObject(index, new LightPoint(position, intensity));
            controlPanel.getObjectList().setSelectedIndex(index);
        }
    }
    public void randomRecolor() {
        Placeable selectedObject = controlPanel.getSelectedObject();
        if(selectedObject instanceof app.object.Shape) {
            int index = controlPanel.getObjectList().getSelectedIndex();
            Vector3D position = selectedObject.getPosition();
            Color color = new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256));
            double size = ((app.object.Shape) selectedObject).getSize();
            double reflectivity = ((Shape) selectedObject).getMaterial().getReflectivity();
            controlPanel.removeSelectedObject();
            if(selectedObject instanceof Cube) {
                controlPanel.addSceneObject(index, new Cube(position, size, new Material(color, reflectivity)));
            }else if(selectedObject instanceof Tetrahedron) {
                controlPanel.addSceneObject(index, new Tetrahedron(position, size, new Material(color, reflectivity)));
            }else if(selectedObject instanceof Sphere) {
                controlPanel.addSceneObject(index, new Sphere(position, size, new Material(color, reflectivity)));
            }else return;
            controlPanel.getObjectList().setSelectedIndex(index);
        }
    }
    public void initializeData() {
        Object[] options = {"はい", "いいえ"};
        int result = JOptionPane.showOptionDialog(
                mainWindow,
                "設定とベンチマークスコアを初期化しますか？この操作は取り消せません。\n",
                "確認",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE,
                null,
                options,
                options[1]
        );

        if(result == JOptionPane.YES_OPTION) {
            RegistryManager.allClear();
            drawingPanel.loadSettings();

        }
    }
}
