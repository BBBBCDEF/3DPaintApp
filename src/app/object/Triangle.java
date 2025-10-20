package app.object;

import app.engine.IntersectionResult;
import app.engine.Ray;
import app.util.Vector3D;

public class Triangle extends Shape {
    private final Vector3D v0, v1, v2;
    private final Vector3D n0, n1, n2;
    private final Material material;

    public Triangle(Vector3D v0, Vector3D v1, Vector3D v2, Vector3D n0, Vector3D n1, Vector3D n2, Material material) {
        this.v0 = v0; this.v1 = v1; this.v2 = v2;
        this.n0 = n0; this.n1 = n1; this.n2 = n2;
        this.material = material;
    }

    @Override
    public IntersectionResult intersect(Ray ray) {
        final double EPSILON = 1e-8;
        Vector3D edge1 = v1.subtract(v0);
        Vector3D edge2 = v2.subtract(v0);
        Vector3D h = ray.getDirection().cross(edge2);
        double a = edge1.dot(h);
        if (a > -EPSILON && a < EPSILON) return IntersectionResult.NO_HIT;

        double f = 1.0 / a;
        Vector3D s = ray.getOrigin().subtract(v0);
        double u = f * s.dot(h); //重心座標u
        if (u < 0.0 || u > 1.0) return IntersectionResult.NO_HIT;

        Vector3D q = s.cross(edge1);
        double v = f * ray.getDirection().dot(q); //重心座標v
        if (v < 0.0 || u + v > 1.0) return IntersectionResult.NO_HIT;

        double t = f * edge2.dot(q);
        if (t > EPSILON) {
            Vector3D hitPoint = ray.getOrigin().add(ray.getDirection().multiply(t));

            //重心座標を使いピクセルごとの法線を補間計算
            double w = 1.0 - u - v;
            Vector3D interpolatedNormal = n0.multiply(w).add(n1.multiply(u)).add(n2.multiply(v)).normalize();

            return new IntersectionResult(this, hitPoint, interpolatedNormal, t, ray);
        }
        return IntersectionResult.NO_HIT;
    }

    @Override
    public Material getMaterial() { return material; }

    @Override
    public Vector3D getPosition() {
        return null;
    }

    @Override
    public double getSize() {
        return 0;
    }

    @Override
    public void setPosition(Vector3D center) {}

    @Override
    public void setSize(double size) {}
}