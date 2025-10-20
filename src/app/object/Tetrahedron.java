package app.object;

import app.util.Vector3D;

public class Tetrahedron extends ComposedShape {
    private Vector3D center;

    public Tetrahedron(Vector3D center, double size, Material material) {
        super(material);

        this.center = center;
        this.size = size;

        double s = size / 2.0;

        //頂点定義（正四面体）
        Vector3D[] v = new Vector3D[4];
        v[0] = center.add(new Vector3D( s,  s,  s));
        v[1] = center.add(new Vector3D( s, -s, -s));
        v[2] = center.add(new Vector3D(-s,  s, -s));
        v[3] = center.add(new Vector3D(-s, -s,  s));

        //各面の法線を計算
        faces.add(makeFlatTriangle(v[0], v[1], v[2], material));
        faces.add(makeFlatTriangle(v[0], v[2], v[3], material));
        faces.add(makeFlatTriangle(v[0], v[3], v[1], material));
        faces.add(makeFlatTriangle(v[1], v[3], v[2], material));
    }

    //面の法線を計算して各頂点に同じ法線を与える
    private Triangle makeFlatTriangle(Vector3D v0, Vector3D v1, Vector3D v2, Material material) {
        Vector3D normal = (v1.subtract(v0)).cross(v2.subtract(v0)).normalize();
        return new Triangle(v0, v1, v2, normal, normal, normal, material);
    }

    @Override
    public String toString() {
        return String.format("三角錐(%s,大きさ:%.2f,反射率:%.2f)", center.toString(), size, material.getReflectivity());
    }

    @Override
    public Vector3D getPosition() {
        return center;
    }

    @Override
    public double getSize() {
        return size;
    }

    @Override
    public void setPosition(Vector3D center) {
        this.center = center;
    }

    @Override
    public void setSize(double size) {
        this.size = size;
    }
}
