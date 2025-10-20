package app.object;

import app.engine.RayTraceable;
import app.engine.IntersectionResult;
import app.engine.Ray;
import app.util.Vector3D;

import java.io.Serializable;

public abstract class Shape implements Placeable, Serializable, RayTraceable {
    protected Vector3D center;
    public abstract IntersectionResult intersect(Ray ray);
    public abstract Material getMaterial();
    public abstract double getSize();
    public abstract void setSize(double size);
}
