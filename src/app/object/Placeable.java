package app.object;

import app.util.Vector3D;

public interface Placeable {
    Vector3D getPosition();
    void setPosition(Vector3D center);
}