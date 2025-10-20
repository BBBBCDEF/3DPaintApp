package app.object;

import app.util.Vector3D;

import java.io.Serializable;

public class LightPoint implements Placeable, Serializable {
    private Vector3D position;
    private double intensity;

    public LightPoint(Vector3D position, double intensity) {
        this.position = position;
        this.intensity = intensity;
    }

    @Override
    public Vector3D getPosition() {
        return position;
    }

    public double getIntensity() {
        return intensity;
    }

    @Override
    public void setPosition(Vector3D position) {
        this.position = position;
    }

    public void setIntensity(double intensity) {
        this.intensity = intensity;
    }

    @Override
    public String toString() {
        return String.format("光源(%s,明るさ:%.2f)", position.toString(), intensity);
    }
}