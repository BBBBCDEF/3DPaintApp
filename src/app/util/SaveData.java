package app.util;

import app.engine.Camera;
import app.engine.Scene;

import java.awt.*;
import java.io.Serializable;

public class SaveData implements Serializable {
    private long serialVersionUID;
    private Scene scene;
    private Camera camera;
    private Color selectedColor;

    public SaveData(long serialVersionUID, Scene scene, Camera camera, Color selectedColor) {
        this.serialVersionUID = serialVersionUID;
        this.scene = scene;
        this.camera = camera;
        this.selectedColor = selectedColor;
    }
    public long getSerialVersionUID() {
        return serialVersionUID;
    }
    public Scene getScene() {
        return scene;
    }
    public Camera getCamera() {
        return camera;
    }
    public Color getSelectedColor() {
        return selectedColor;
    }
}
