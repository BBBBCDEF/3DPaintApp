package app.object;

import java.awt.Color;
import java.io.Serializable;

public class Material implements Serializable, HasColor {
    private Color color;
    private double reflectivity;
    private double shininess;
    private Color specularColor;

    public Material(Color color, double reflectivity, double shininess, Color specularColor) {
        this.color = color;
        this.reflectivity = reflectivity;
        this.shininess = shininess;
        this.specularColor = specularColor;
    }

    public Material(Color color, double reflectivity) {
        this(color, reflectivity, reflectivity * 100, color);
    }

    public Color getColor() {
        return color;
    }
    public void setColor(Color color) {
        this.color = color;
    }

    public double getReflectivity() {
        return reflectivity;
    }

    public double getShininess() {
        return shininess;
    }

    public Color getSpecularColor() {
        return specularColor;
    }
}