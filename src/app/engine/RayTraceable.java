package app.engine;

public interface RayTraceable {
    IntersectionResult intersect(Ray ray);
}
