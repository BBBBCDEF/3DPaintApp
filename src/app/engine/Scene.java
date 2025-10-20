package app.engine;

import app.object.HasColor;
import app.object.LightPoint;
import app.object.Shape;

import java.awt.Color;
import java.io.Serializable;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Scene implements Serializable, HasColor {
    private final List<Shape> shapes = new CopyOnWriteArrayList<>();
    private final List<LightPoint> lights = new CopyOnWriteArrayList<>();
    private Color backgroundColor = Color.DARK_GRAY;

    public void addShape(Shape shape) {
        shapes.add(shape);
    }
    public void addLight(LightPoint light) {
        lights.add(light);
    }
    public List<LightPoint> getLights() {
        return lights;
    }
    public Color getColor() {
        return backgroundColor;
    }

    public IntersectionResult findClosestIntersection(Ray ray) {
        IntersectionResult closestHit = IntersectionResult.NO_HIT;
        for (Shape shape : shapes) {
            IntersectionResult result = shape.intersect(ray);
            if (result.isHit() && result.getDistance() < closestHit.getDistance()) {
                closestHit = result;
            }
        }
        return closestHit;
    }
    public List<Shape> getShapes() {
        return shapes;
    }

    public void removeShape(Shape shape) {
        shapes.remove(shape);
    }
    public void removeLight(LightPoint light) {
        lights.remove(light);
    }
    public void setColor(Color color) {
        this.backgroundColor = color;
    }

}