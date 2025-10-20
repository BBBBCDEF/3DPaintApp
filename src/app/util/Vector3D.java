package app.util;

import java.io.Serializable;

public class Vector3D implements Serializable {
    final double x, y, z;

    public Vector3D(double x, double y, double z) {
        this.x = x; this.y = y; this.z = z;
    }
    public Vector3D add(Vector3D v) {
        return new Vector3D(x + v.x, y + v.y, z + v.z);
    }
    public Vector3D subtract(Vector3D v) {
        return new Vector3D(x - v.x, y - v.y, z - v.z);
    }
    public Vector3D multiply(double s) {
        return new Vector3D(x * s, y * s, z * s);
    }
    public double dot(Vector3D v) {
        return x * v.x + y * v.y + z * v.z;
    }
    public Vector3D cross(Vector3D v) {
        return new Vector3D(y * v.z - z * v.y, z * v.x - x * v.z, x * v.y - y * v.x);
    }
    public double length() {
        return Math.sqrt(dot(this));
    }
    public Vector3D normalize() {
        double len = length(); return new Vector3D(x / len, y / len, z / len);
    }
    public double distance(Vector3D v) {
        return this.subtract(v).length();
    }
    public Vector3D rotateAroundAxis(Vector3D axis, double angle) {
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        Vector3D v = this;
        return axis.multiply(axis.dot(v) * (1 - cos))
                .add(v.multiply(cos))
                .add(axis.cross(v).multiply(sin));
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    @Override
    public String toString(){
        return String.format("X:%.1f,Y:%.1f,Z:%.1f\n", x, y, z);
    }
}
