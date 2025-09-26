package org.bukkitmodders.copycat.util;

import org.bukkit.Location;
import org.joml.Matrix4d;

public class MatrixUtil {

    // Angle constants
    private static final double SNAP_THRESHOLD = 45.0;
    private static final double FULL_ROTATION = 360.0;
    private static final double PITCH_DOWN_THRESHOLD = 45.0;

    // Pre-calculated rotation matrices for player orientations
    private static final Matrix4d PLAYER_ROTATION_0 = new Matrix4d().rotateY(Math.toRadians(180));
    private static final Matrix4d PLAYER_ROTATION_90 = new Matrix4d().rotateY(Math.toRadians(90));
    private static final Matrix4d PLAYER_ROTATION_180 = new Matrix4d().rotateY(Math.toRadians(180));
    private static final Matrix4d PLAYER_ROTATION_270 = new Matrix4d().rotateY(-Math.toRadians(270));

    // Pre-calculated rotation matrices for pitch adjustments
    private static final Matrix4d X_ROTATION_NEG_90 = new Matrix4d().rotateX(Math.toRadians(-90));
    private static final Matrix4d X_ROTATION_POS_90 = new Matrix4d().rotateX(Math.toRadians(90));
    private static final Matrix4d Z_ROTATION_NEG_90 = new Matrix4d().rotateZ(Math.toRadians(-90));
    private static final Matrix4d Z_ROTATION_POS_90 = new Matrix4d().rotateZ(Math.toRadians(90));

    public static Matrix4d calculateRotation(Location location) {
        if (location == null) {
            return new Matrix4d().identity();
        }

        double normalizedYaw = normalizeYaw(location.getYaw());
        boolean isPitchingDown = location.getPitch() > PITCH_DOWN_THRESHOLD;

        return buildRotationMatrix(normalizedYaw, isPitchingDown);
    }

    private static double normalizeYaw(float rawYaw) {
        double yaw = Math.abs(rawYaw);
        yaw %= FULL_ROTATION;

        double baseAngle = snapToCardinalDirection(yaw);

        if (rawYaw < 0) {
            baseAngle = FULL_ROTATION - baseAngle;
        }

        return baseAngle;
    }

    private static double snapToCardinalDirection(double yaw) {
        if (yaw > (90 - SNAP_THRESHOLD) && yaw <= (90 + SNAP_THRESHOLD)) {
            return 90;
        } else if (yaw > (180 - SNAP_THRESHOLD) && yaw <= (180 + SNAP_THRESHOLD)) {
            return 180;
        } else if (yaw > (270 - SNAP_THRESHOLD) && yaw <= (270 + SNAP_THRESHOLD)) {
            return 270;
        }
        return 0;
    }

    private static Matrix4d buildRotationMatrix(double baseAngle, boolean isPitchingDown) {
        Matrix4d orientation = new Matrix4d().identity();

        if (baseAngle == 0 || baseAngle == FULL_ROTATION) {
            applyRotationForAngle0(orientation, isPitchingDown);
        } else if (baseAngle == 90) {
            applyRotationForAngle90(orientation, isPitchingDown);
        } else if (baseAngle == 180) {
            applyRotationForAngle180(orientation, isPitchingDown);
        } else if (baseAngle == 270) {
            applyRotationForAngle270(orientation, isPitchingDown);
        }

        return orientation;
    }

    private static void applyRotationForAngle0(Matrix4d orientation, boolean isPitchingDown) {
        if (isPitchingDown) {
            orientation.mul(X_ROTATION_POS_90);
        }
        orientation.mul(PLAYER_ROTATION_0);
    }

    private static void applyRotationForAngle90(Matrix4d orientation, boolean isPitchingDown) {
        if (isPitchingDown) {
            orientation.mul(Z_ROTATION_POS_90);
        }
        orientation.mul(PLAYER_ROTATION_90);
    }

    private static void applyRotationForAngle180(Matrix4d orientation, boolean isPitchingDown) {
        if (isPitchingDown) {
            orientation.mul(X_ROTATION_NEG_90);
        }
        orientation.mul(PLAYER_ROTATION_180);
    }

    private static void applyRotationForAngle270(Matrix4d orientation, boolean isPitchingDown) {
        if (isPitchingDown) {
            orientation.mul(Z_ROTATION_NEG_90);
        }
        orientation.mul(PLAYER_ROTATION_270);
    }
}
