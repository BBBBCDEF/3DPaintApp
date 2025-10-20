package app.engine;

import app.object.Shape;
import app.util.Vector3D;

public class IntersectionResult {
    public static final IntersectionResult NO_HIT = new IntersectionResult(null, null, null, Double.MAX_VALUE, null); // Rayを追加
    private final Shape shape;
    private final Vector3D hitPoint;
    private final Vector3D normal;
    private final double distance;
    private final Ray ray;

    public IntersectionResult(Shape shape, Vector3D hitPoint, Vector3D normal, double distance, Ray ray) {
        this.shape = shape;
        this.hitPoint = hitPoint;
        this.normal = normal;
        this.distance = distance;
        this.ray = ray;
    }

    public boolean isHit() {
        return shape != null;
    }

    public Shape getShape() {
        return shape;
    }

    public Vector3D getHitPoint() {
        return hitPoint;
    }

    public Vector3D getNormal() {
        return normal;
    }

    public double getDistance() {
        return distance;
    }

    public Ray getRay() {
        return ray;
    }
}