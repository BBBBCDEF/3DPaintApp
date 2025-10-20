package app.engine;

import app.object.Placeable;
import app.util.Vector3D;

import java.io.Serializable;

public class Camera implements Placeable, Serializable {
    private Vector3D position = new Vector3D(0, 0, 5);
    private Vector3D forward = new Vector3D(0, 0, -1);
    private Vector3D up = new Vector3D(0, 1, 0);

    public Ray createRay(int x, int y, int width, int height) {
        double fov = 70.0;
        double aspectRatio = (double) width / height;
        double scale = Math.tan(Math.toRadians(fov * 0.5));

        Vector3D right = forward.cross(up).normalize();
        Vector3D trueUp = right.cross(forward).normalize(); //カメラの上方向

        double px = (2 * (x + 0.5) / (double) width - 1) * aspectRatio * scale;
        double py = (1 - 2 * (y + 0.5) / (double) height) * scale;

        Vector3D direction = forward
                .add(right.multiply(px))
                .add(trueUp.multiply(py))
                .normalize();

        return new Ray(position, direction);
    }

    public void rotateYaw(double angleDegrees) {
        double radians = Math.toRadians(angleDegrees);
        forward = forward.rotateAroundAxis(up, radians).normalize();
    }

    public void rotatePitch(double angleDegrees) {
        double radians = Math.toRadians(angleDegrees);
        Vector3D right = forward.cross(up).normalize();
        forward = forward.rotateAroundAxis(right, radians).normalize();
        up = right.cross(forward).normalize();
    }

    public Vector3D getPosition() {
        return position;
    }
    public void setPosition(Vector3D position) {
        this.position = position;
    }

    public Vector3D getForward() {
        return forward;
    }
    public void setForward(Vector3D forward) {
        this.forward = forward;
    }
    public Vector3D getUp() {
        return up;
    }
    public void setUp(Vector3D up) {
        this.up = up;
    }
    public void moveRelative(Vector3D localDelta) {
        Vector3D right = forward.cross(up).normalize();
        Vector3D actualMove = right.multiply(localDelta.getX())
                .add(up.multiply(localDelta.getY()))
                .add(forward.multiply(localDelta.getZ()));
        position = position.add(actualMove);
    }
    public Vector3D getPointAlongView(double r) {
        return position.add(forward.normalize().multiply(r));
    }
    public void cameraReset() {
        forward = new Vector3D(Math.round(forward.getX()), Math.round(forward.getY()), Math.round(forward.getZ()));
        up = new Vector3D(Math.round(up.getX()), Math.round(up.getY()), Math.round(up.getZ()));
    }
}
