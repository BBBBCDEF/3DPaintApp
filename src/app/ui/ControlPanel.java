package app.ui;

import app.object.LightPoint;
import app.engine.Scene;
import app.object.*;
import app.object.Shape;
import app.util.Vector3D;
import com.formdev.flatlaf.FlatDarkLaf;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Objects;

public class ControlPanel extends JPanel {
    private final Scene scene;
    private final DrawingPanel drawingPanel;
    private final JComboBox<String> objectTypeCombo;
    private final JTextField xField, yField, zField, sizeField, reflectivityField;
    private DefaultListModel<Placeable> objectListModel;
    private JList<Placeable> objectList;
    private Color selectedColor = Color.WHITE;
    private final JButton colorButton, addButton, removeButton, clearAllButton;
    private final JLabel sizeLabel;


    public ControlPanel(DrawingPanel drawingPanel, LightPoint light) {
        this.scene = drawingPanel.getScene();
        this.drawingPanel = drawingPanel;
        this.setPreferredSize(new Dimension(300, 400));
        this.setBackground(Color.DARK_GRAY);

        try{
            UIManager.setLookAndFeel(new FlatDarkLaf());
        }catch(Exception e) {
            JOptionPane.showMessageDialog(this, "ライブラリの読み込みに失敗しました", "起動エラー", JOptionPane.ERROR_MESSAGE);
        }

        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createTitledBorder("オブジェクト操作"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        add(new JLabel("種類:"), gbc);
        gbc.gridx = 1;
        objectTypeCombo = new JComboBox<>(new String[]{"立方体", "三角錐", "球", "光源"});
        add(objectTypeCombo, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        add(new JLabel("座標 (X,Y,Z):"), gbc);
        gbc.gridx = 1;
        JPanel cordPanel = new JPanel(new GridLayout(1, 3, 5, 5));
        xField = new JTextField("0");
        yField = new JTextField("0");
        zField = new JTextField("-5");
        cordPanel.add(xField);
        cordPanel.add(yField);
        cordPanel.add(zField);
        add(cordPanel, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        sizeLabel = new JLabel("大きさ");
        add(sizeLabel, gbc);
        gbc.gridx = 1;
        sizeField = new JTextField("1.0");
        add(sizeField, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        add(new JLabel("反射率 (0.0 - 1.0):"), gbc);

        gbc.gridx = 1;
        reflectivityField = new JTextField("0.3");
        add(reflectivityField, gbc);


        gbc.gridx = 0; gbc.gridy = 4;
        add(new JLabel("色:"), gbc);

        gbc.gridx = 1;
        colorButton = new JButton(" ");
        colorButton.setBackground(selectedColor);
        colorButton.setBorderPainted(true);

        colorButton.addActionListener(e -> {
            Color newColor = JColorChooser.showDialog(this, "色を選択", selectedColor);
            if (newColor != null) {
                selectedColor = newColor;
                colorButton.setBackground(selectedColor);
            }
        });
        add(colorButton, gbc);

        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        addButton = new JButton("オブジェクトを追加");
        add(addButton, gbc);

        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 2;
        add(new JLabel("オブジェクト一覧:"), gbc);

        objectListModel = new DefaultListModel<>();
        objectListModel.addElement(light);
        objectList = new JList<>(objectListModel);
        objectList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        objectList.setVisibleRowCount(5);
        JScrollPane scrollPane = new JScrollPane(objectList);

        gbc.gridy = 7;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        add(scrollPane, gbc);

        gbc.gridy = 8;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weighty = 0;
        removeButton = new JButton("選択したオブジェクトを削除");
        removeButton.setEnabled(false);
        removeButton.addActionListener(e -> removeSelectedObjectWithDialog());
        add(removeButton, gbc);
        gbc.gridy = 9;
        clearAllButton = new JButton("すべて削除");
        clearAllButton.addActionListener(e -> clearAllObjectsWithDialog(true));
        add(clearAllButton, gbc);

        objectList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                if (objectList.getSelectedIndex() != -1) {
                    updateFieldsFromSelectedObject();
                    addButton.setText("変更を反映");
                    removeButton.setEnabled(true);
                } else {
                    addButton.setText("オブジェクトを追加");
                    removeButton.setEnabled(false);
                }
            }
        });

        addButton.addActionListener(e -> {
            if(objectList.getSelectedIndex() == -1) {
                addSceneObject();
            }else {
                if(updateSelectedObject()) {
                    addButton.setText("オブジェクトを追加");
                    objectList.clearSelection();
                }

            }
        });

        objectList.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int index = objectList.locationToIndex(e.getPoint());
                Rectangle bounds = objectList.getCellBounds(index, index);
                if (bounds == null || !bounds.contains(e.getPoint())) {
                    objectList.clearSelection();
                }
            }
        });

        objectTypeCombo.addItemListener(e -> {
            String selectedType = (String) objectTypeCombo.getSelectedItem();
            switch(selectedType) {
                case "光源":
                    sizeLabel.setText("明るさ:");
                    reflectivityField.setText("");
                    break;
                case "立方体":
                    sizeLabel.setText("辺の長さ:");
                    reflectivityField.setText("0.3");
                    break;
                case "三角錐":
                    sizeLabel.setText("辺の長さ:");
                    reflectivityField.setText("0.3");
                    break;
                case "球":
                    sizeLabel.setText("半径:");
                    reflectivityField.setText("0.3");
            }
        });
    }

    public boolean addSceneObject() {
        try {
            String selectedType = (String) objectTypeCombo.getSelectedItem();
            double x = Double.parseDouble(xField.getText());
            double y = Double.parseDouble(yField.getText());
            double z = Double.parseDouble(zField.getText());
            double size = Double.parseDouble(sizeField.getText());
            double reflectivity = 0;
            if(!selectedType.equals("光源")) {
                reflectivity = Double.parseDouble(reflectivityField.getText());
            }
            Vector3D center = new Vector3D(x, y, z);
            if(size <= 0) {
                throw new NumberFormatException();
            }
            if(reflectivity < 0 || reflectivity > 1.0) {
                throw new NumberFormatException();
            }
            Material material = new Material(selectedColor, reflectivity);

            Placeable newObject = null;
            switch (Objects.requireNonNull(selectedType)) {
                case "立方体" -> newObject = new Cube(center, size, material);
                case "三角錐" -> newObject = new Tetrahedron(center, size, material);
                case "球" -> newObject = new Sphere(center, size, material);
                case "光源" -> newObject = new LightPoint(center, size);
            }

            if (newObject instanceof Shape) {
                scene.addShape((Shape) newObject);
                objectListModel.addElement(newObject);
            }else if(newObject instanceof LightPoint) {
                scene.addLight((LightPoint) newObject);
                objectListModel.addElement(newObject);
            }
            return true;

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "無効な数値が入力されました。", "エラー", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
    public boolean addSceneObject(int index) {
        try {
            String selectedType = (String) objectTypeCombo.getSelectedItem();
            double x = Double.parseDouble(xField.getText());
            double y = Double.parseDouble(yField.getText());
            double z = Double.parseDouble(zField.getText());
            double size = Double.parseDouble(sizeField.getText());
            double reflectivity = 0;
            if(!selectedType.equals("光源")) {
                reflectivity = Double.parseDouble(reflectivityField.getText());
            }
            Vector3D center = new Vector3D(x, y, z);
            if(size <= 0) {
                throw new NumberFormatException();
            }
            if(reflectivity < 0 || reflectivity > 1.0) {
                throw new NumberFormatException();
            }
            Material material = new Material(selectedColor, reflectivity);

            Placeable newObject = null;
            switch (Objects.requireNonNull(selectedType)) {
                case "立方体" -> newObject = new Cube(center, size, material);
                case "三角錐" -> newObject = new Tetrahedron(center, size, material);
                case "球" -> newObject = new Sphere(center, size, material);
                case "光源" -> newObject = new LightPoint(center, size);
            }

            if (newObject instanceof Shape) {
                scene.addShape((Shape) newObject);
                objectListModel.add(index, newObject);
            }else if(newObject instanceof LightPoint) {
                scene.addLight((LightPoint) newObject);
                objectListModel.add(index, newObject);
            }
            return true;

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "無効な数値が入力されました。", "エラー", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
    public boolean addSceneObject(Placeable placeable) {
        if (placeable instanceof Shape) {
            scene.addShape((Shape) placeable);
            objectListModel.addElement(placeable);
        }else if(placeable instanceof LightPoint) {
            scene.addLight((LightPoint) placeable);
            objectListModel.addElement(placeable);
        }
        return true;
    }
    public boolean addSceneObject(int index, Placeable placeable) {
        if (placeable instanceof Shape) {
            scene.addShape((Shape) placeable);
            objectListModel.add(index, placeable);
        }else if(placeable instanceof LightPoint) {
            scene.addLight((LightPoint) placeable);
            objectListModel.add(index, placeable);
        }
        return true;
    }

    public boolean removeSelectedObjectWithDialog() {
        int index = objectList.getSelectedIndex();
        if(index == -1) {
            return false;
        }
        Object selected = objectListModel.get(objectList.getSelectedIndex());
        if (selected instanceof Shape) {
            scene.removeShape((Shape) selected);
            objectListModel.removeElement(selected);
            objectList.setSelectedIndex(Math.max(index - 1, 0));
            return true;
        }else if (selected instanceof LightPoint) {
            scene.removeLight((LightPoint) selected);
            objectListModel.removeElement(selected);
            objectList.setSelectedIndex(Math.max(index - 1, 0));
            return true;
        }
        return false;
    }
    public boolean removeSelectedObject() {
        int index = objectList.getSelectedIndex();
        if(index == -1) {
            return false;
        }
        Object selected = objectListModel.get(objectList.getSelectedIndex());
        if (selected instanceof Shape) {
            scene.removeShape((Shape) selected);
            objectListModel.removeElement(selected);
            objectList.setSelectedIndex(Math.max(index - 1, 0));
            return true;
        }else if (selected instanceof LightPoint) {
            scene.removeLight((LightPoint) selected);
            objectListModel.removeElement(selected);
            objectList.setSelectedIndex(Math.max(index - 1, 0));
            return true;
        }
        return false;
    }

    private void updateFieldsFromSelectedObject() {
        Placeable selected = objectList.getSelectedValue();
        if (selected == null) return;

        if (selected instanceof Cube) {
            objectTypeCombo.setSelectedItem("立方体");
        } else if (selected instanceof Tetrahedron) {
            objectTypeCombo.setSelectedItem("三角錐");
        } else if (selected instanceof Sphere) {
            objectTypeCombo.setSelectedItem("球");
        } else if (selected instanceof LightPoint) {
            objectTypeCombo.setSelectedItem("光源");
        }


        if (selected instanceof Shape) {
            Shape shape = (Shape) selected;
            Vector3D center = shape.getPosition();
            xField.setText(String.valueOf(center.getX()));
            yField.setText(String.valueOf(center.getY()));
            zField.setText(String.valueOf(center.getZ()));
            sizeField.setText(String.valueOf(shape.getSize()));
            reflectivityField.setText(String.valueOf(shape.getMaterial().getReflectivity()));
            selectedColor = shape.getMaterial().getColor();
            colorButton.setBackground(selectedColor);
        } else if (selected instanceof LightPoint) {
            LightPoint light = (LightPoint) selected;
            Vector3D center = light.getPosition();
            xField.setText(String.valueOf(center.getX()));
            yField.setText(String.valueOf(center.getY()));
            zField.setText(String.valueOf(center.getZ()));
            sizeField.setText(String.valueOf(light.getIntensity()));
            reflectivityField.setText("");
            colorButton.setBackground(selectedColor);
        }
    }
    private boolean updateSelectedObject() {
        int selectedIndex = objectList.getSelectedIndex();
        Placeable selected = objectList.getSelectedValue();
        if (selected == null || selectedIndex == -1) return false;
        if(addSceneObject(selectedIndex)) {
            removeSelectedObjectWithDialog();
            return true;
        }else {
            return false;
        }
    }
    public void clearAllObjects(boolean isPlaceLightPoint) {
        for (int i = 0; i < objectListModel.size(); i++) {
            Object obj = objectListModel.get(i);
            if (obj instanceof Shape) {
                scene.removeShape((Shape) obj);
            } else if (obj instanceof LightPoint) {
                scene.removeLight((LightPoint) obj);
            }
        }

        objectListModel.clear();

        drawingPanel.render();
        LightPoint light = new LightPoint(new Vector3D(5, 5, 0), 1.0);
        if(isPlaceLightPoint) {
            scene.addLight(light);
            objectListModel.addElement(light);
        }
        drawingPanel.rebootThread();
    }
    public void clearAllObjectsWithDialog(boolean isPlaceLightPoint) {
        int result = JOptionPane.showConfirmDialog(
                this,
                "すべてのオブジェクトを削除しますか？",
                "確認",
                JOptionPane.YES_NO_OPTION
        );
        if(result == JOptionPane.YES_OPTION) {
            clearAllObjects(isPlaceLightPoint);
        }
    }
    public void disableAllComponents() {
        colorButton.setEnabled(false);
        addButton.setEnabled(false);
        clearAllButton.setEnabled(false);
        removeButton.setEnabled(false);
        xField.setEnabled(false);
        yField.setEnabled(false);
        zField.setEnabled(false);
        sizeField.setEnabled(false);
        reflectivityField.setEnabled(false);
        objectTypeCombo.setEnabled(false);
        objectList.setEnabled(false);
    }
    public void enableAllComponents() {
        colorButton.setEnabled(true);
        addButton.setEnabled(true);
        clearAllButton.setEnabled(true);
        removeButton.setEnabled(true);
        xField.setEnabled(true);
        yField.setEnabled(true);
        zField.setEnabled(true);
        sizeField.setEnabled(true);
        reflectivityField.setEnabled(true);
        objectTypeCombo.setEnabled(true);
        objectList.setEnabled(true);
    }
    public JComboBox<String> getObjectTypeCombo() {
        return objectTypeCombo;
    }
    public JList<Placeable> getObjectList() {
        return objectList;
    }

    public void setTextFieldPosition(Vector3D position) {
        xField.setText(String.valueOf(Math.round(position.getX() * 1000D) / 1000D));
        yField.setText(String.valueOf(Math.round(position.getY() * 1000D) / 1000D));
        zField.setText(String.valueOf(Math.round(position.getZ() * 1000D) / 1000D));
    }
    public Placeable getSelectedObject() {
        if(objectList.getSelectedIndex() == -1) {
            return null;
        }
        return objectListModel.getElementAt(objectList.getSelectedIndex());
    }


    public double getSizeFieldValue() throws NumberFormatException{
        double d = Double.parseDouble(sizeField.getText());
        return d;
    }

    public void setSelectedColor(Color selectedColor) {
        colorButton.setBackground(selectedColor);
        this.selectedColor = selectedColor;
    }
    public Color getSelectedColor() {
        return selectedColor;
    }
    public JButton getAddButton() {
        return addButton;
    }
    public JButton getRemoveButton() {
        return removeButton;
    }
}
