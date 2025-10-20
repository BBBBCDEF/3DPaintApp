package app.ui;

import app.util.RegistryManager;

import javax.swing.*;
import java.awt.*;

public class SettingsWindow extends JFrame {
    private final DrawingPanel drawingPanel;
    private final JButton colorButton;
    private Color selectedColor;
    private JTextField reflectionDepthField;


    public SettingsWindow(DrawingPanel drawingPanel) {
        this.drawingPanel = drawingPanel;
        selectedColor = drawingPanel.getScene().getColor();
        setTitle("設定");
        setSize(350, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        setLayout(new BorderLayout());

        JPanel settingsPanel = new JPanel();
        settingsPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel fpsLabel = new JLabel(String.format("最大フレームレート: %10d fps", drawingPanel.getMaxFps()));
        settingsPanel.add(fpsLabel, gbc);

        JSlider fpsSlider = new JSlider(JSlider.HORIZONTAL, 10, 60, drawingPanel.getMaxFps());
        fpsSlider.setMinorTickSpacing(5);
        fpsSlider.setPaintTicks(true);
        fpsSlider.setPaintLabels(true);
        fpsSlider.setSnapToTicks(true);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        settingsPanel.add(fpsSlider, gbc);

        fpsSlider.addChangeListener(e -> {
            int newFps = fpsSlider.getValue();
            drawingPanel.setMaxFps(newFps);
            fpsLabel.setText(String.format("最大フレームレート: %10d fps", drawingPanel.getMaxFps()));
            RegistryManager.saveMaxFps(newFps);
        });

        gbc.gridx = 0;
        gbc.gridy = 2;
        JLabel upscalingLabel = new JLabel(String.format("レンダリング解像度:            1/%d", drawingPanel.getUpscalingRatio()));
        settingsPanel.add(upscalingLabel, gbc);

        JSlider upscalingSlider = new JSlider(JSlider.HORIZONTAL, 1, 4, drawingPanel.getUpscalingRatio());
        upscalingSlider.setMinorTickSpacing(1);
        upscalingSlider.setPaintTicks(true);
        upscalingSlider.setPaintLabels(true);
        upscalingSlider.setSnapToTicks(true);

        upscalingSlider.addChangeListener(e -> {
            int r = upscalingSlider.getValue();
            if(r == 3) {
            }else {
                drawingPanel.setUpscalingRatio(r);
                upscalingLabel.setText(String.format("レンダリング解像度:            1/%d", drawingPanel.getUpscalingRatio()));
                RegistryManager.saveUpscalingRatio(r);
            }
        });

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        settingsPanel.add(upscalingSlider, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 1;
        JLabel reflectionDepthLabel = new JLabel("反射の最大深さ:");
        settingsPanel.add(reflectionDepthLabel, gbc);

        reflectionDepthField = new JTextField(5);

        reflectionDepthField.setText(String.valueOf(drawingPanel.getReflectionMaxDepth()));

        gbc.gridx = 1;
        settingsPanel.add(reflectionDepthField, gbc);

        reflectionDepthField.addActionListener(e -> {changeReflectionMaxDepth();
        });

        gbc.gridy = 5;
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        settingsPanel.add(new JLabel("背景色:"), gbc);

        colorButton = new JButton(" ");
        colorButton.setBackground(selectedColor);
        colorButton.setOpaque(true);

        colorButton.addActionListener(e -> {
            Color newColor = JColorChooser.showDialog(this, "色を選択", selectedColor);
            if (newColor != null) {
                selectedColor = newColor;
                colorButton.setBackground(selectedColor);
                drawingPanel.getScene().setColor(selectedColor);
                RegistryManager.saveBackgroundColor(selectedColor);
            }
        });

        gbc.gridx = 1;
        settingsPanel.add(colorButton, gbc);

        add(settingsPanel, BorderLayout.CENTER);

        gbc.gridx = 0;
        gbc.gridy = 9;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.WEST;
        JLabel debugLabel = new JLabel("デバッグ情報を表示");
        settingsPanel.add(debugLabel, gbc);

        gbc.gridx = 1;
        JCheckBox checkBox = new JCheckBox();
        checkBox.setSelected(drawingPanel.isShowDebugInfo());
        settingsPanel.add(checkBox, gbc);

        gbc.gridy = 10;
        gbc.gridx = 0;
        JLabel scoreLabel = new JLabel(String.format("最高スコア: %d", drawingPanel.getTotalFrames()));
        settingsPanel.add(scoreLabel, gbc);

        gbc.gridx = 1;
        JButton benchmarkButton = new JButton("ベンチマーク実行");
        settingsPanel.add(benchmarkButton, gbc);

        benchmarkButton.addActionListener(e -> {

            int result = JOptionPane.showConfirmDialog(
                    this,
                    "ベンチマークを開始しますか？\n" +
                            "所要時間: 約1分",
                    "確認",
                    JOptionPane.YES_NO_OPTION
            );
            if(result == JOptionPane.YES_OPTION) {
                drawingPanel.runBenchmark();
                dispose();
            }
        });

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton closeButton = new JButton("完了");
        getRootPane().setDefaultButton(closeButton);
        closeButton.addActionListener(e -> {
            if(changeReflectionMaxDepth()) {
                dispose();
                drawingPanel.initRenderingComponents();
            }
        });
        checkBox.addActionListener(e -> {
            drawingPanel.setShowDebugInfo(checkBox.isSelected());
            RegistryManager.saveShowDebugInfo(checkBox.isSelected());
        });

        buttonPanel.add(closeButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    public boolean changeReflectionMaxDepth() {
        try {
            int depth = Integer.parseInt(reflectionDepthField.getText());
            if(depth >= 0 && depth < 128) {
                drawingPanel.setReflectionMaxDepth(depth);
                RegistryManager.saveReflectionMaxDepth(depth);
                return true;
            } else {
                JOptionPane.showMessageDialog(this, "0以上128未満の整数を入力してください。", "入力エラー", JOptionPane.ERROR_MESSAGE);
                reflectionDepthField.setText(String.valueOf(drawingPanel.getReflectionMaxDepth()));
                return false;
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "数値を入力してください。", "入力エラー", JOptionPane.ERROR_MESSAGE);
            reflectionDepthField.setText(String.valueOf(drawingPanel.getReflectionMaxDepth()));
        }
        return false;
    }
}
