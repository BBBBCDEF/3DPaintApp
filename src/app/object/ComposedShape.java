package app.object;

import app.engine.IntersectionResult;
import app.engine.Ray;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
public abstract class ComposedShape extends Shape implements Serializable {
    protected final List<Shape> faces = new ArrayList<>();
    protected final Material material;
    protected double size;

    public ComposedShape(Material material) { this.material = material; }

    @Override
    public IntersectionResult intersect(Ray ray) {
        IntersectionResult closestHit = IntersectionResult.NO_HIT;
        for(Shape face : faces) {
            IntersectionResult result = face.intersect(ray);
            if (result.isHit() && result.getDistance() < closestHit.getDistance()) {
                closestHit = result;
            }
        }
        return closestHit;
    }
    @Override
    public Material getMaterial() { return material; }
}
