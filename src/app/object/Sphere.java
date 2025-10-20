package app.object;

import app.engine.IntersectionResult;
import app.engine.Ray;
import app.util.Vector3D;

public class Sphere extends Shape {
    private double radius;
    private final Material material;

    public Sphere(Vector3D center, double radius, Material material) {
        this.center = center; this.radius = radius; this.material = material;
    }

    @Override
    public IntersectionResult intersect(Ray ray) {
        Vector3D oc = ray.getOrigin().subtract(center);
        double a = ray.getDirection().dot(ray.getDirection());
        double b = 2.0 * oc.dot(ray.getDirection());
        double c = oc.dot(oc) - radius * radius;
        double discriminant = b * b - 4 * a * c; //判別式
        if (discriminant < 0) return IntersectionResult.NO_HIT;
        double t = (-b - Math.sqrt(discriminant)) / (2.0 * a);
        if (t < 0.0001) {
            t = (-b + Math.sqrt(discriminant)) / (2.0 * a);
            if (t < 0.0001) return IntersectionResult.NO_HIT;
        }
        Vector3D hitPoint = ray.getOrigin().add(ray.getDirection().multiply(t));
        Vector3D normal = hitPoint.subtract(center).normalize();
        return new IntersectionResult(this, hitPoint, normal, t, ray);
    }
    @Override
    public Material getMaterial() { return material; }

    @Override
    public Vector3D getPosition() {
        return center;
    }

    @Override
    public double getSize() {
        return radius;
    }

    @Override
    public void setPosition(Vector3D center) {
        this.center = center;
    }

    @Override
    public void setSize(double size) {
        this.radius = size;
    }

    @Override
    public String toString(){
        return String.format("球(%s,大きさ:%.2f,反射率:%.2f)", center.toString(), radius, material.getReflectivity());
    }
}
