package app.object;

import app.util.Vector3D;

import java.io.Serializable;

public class Cube extends ComposedShape implements Serializable {
    public Cube(Vector3D center, double size, Material material) {
        super(material);
        this.center = center;
        this.size = size;

        double s = size / 2.0;

        Vector3D[] v = new Vector3D[8];
        v[0] = center.add(new Vector3D(-s, -s, -s)); v[1] = center.add(new Vector3D( s, -s, -s));
        v[2] = center.add(new Vector3D( s,  s, -s)); v[3] = center.add(new Vector3D(-s,  s, -s));
        v[4] = center.add(new Vector3D(-s, -s,  s)); v[5] = center.add(new Vector3D( s, -s,  s));
        v[6] = center.add(new Vector3D( s,  s,  s)); v[7] = center.add(new Vector3D(-s,  s,  s));

        //各面の法線ベクトル
        Vector3D nFront  = new Vector3D(0, 0, -1);
        Vector3D nBack   = new Vector3D(0, 0, 1);
        Vector3D nTop    = new Vector3D(0, 1, 0);
        Vector3D nBottom = new Vector3D(0, -1, 0);
        Vector3D nRight  = new Vector3D(1, 0, 0);
        Vector3D nLeft   = new Vector3D(-1, 0, 0);

        //前面
        faces.add(new Triangle(v[0], v[3], v[2], nFront, nFront, nFront, material));
        faces.add(new Triangle(v[0], v[2], v[1], nFront, nFront, nFront, material));

        //背面
        faces.add(new Triangle(v[4], v[5], v[6], nBack, nBack, nBack, material));
        faces.add(new Triangle(v[4], v[6], v[7], nBack, nBack, nBack, material));

        //上面
        faces.add(new Triangle(v[3], v[7], v[6], nTop, nTop, nTop, material));
        faces.add(new Triangle(v[3], v[6], v[2], nTop, nTop, nTop, material));

        //底面
        faces.add(new Triangle(v[0], v[1], v[5], nBottom, nBottom, nBottom, material));
        faces.add(new Triangle(v[0], v[5], v[4], nBottom, nBottom, nBottom, material));

        //右面
        faces.add(new Triangle(v[1], v[2], v[6], nRight, nRight, nRight, material));
        faces.add(new Triangle(v[1], v[6], v[5], nRight, nRight, nRight, material));

        //左面
        faces.add(new Triangle(v[4], v[7], v[3], nLeft, nLeft, nLeft, material));
        faces.add(new Triangle(v[4], v[3], v[0], nLeft, nLeft, nLeft, material));
    }

    @Override
    public String toString() {
        return String.format("立方体(%s,大きさ:%.2f,反射率:%.2f)", center.toString(), size, material.getReflectivity());
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