package org.bukkitmodders.copycat.util;

import javax.vecmath.Matrix4d;

import org.bukkit.Location;

public class MatrixUtil {

	private static Matrix4d PLAYER_DEG0 = new Matrix4d();
	private static Matrix4d PLAYER_DEG90 = new Matrix4d();
	private static Matrix4d PLAYER_DEG180 = new Matrix4d();
	private static Matrix4d PLAYER_DEG270 = new Matrix4d();

	private static Matrix4d XROTNEG90 = new Matrix4d();
	private static Matrix4d XROTPOS90 = new Matrix4d();

	private static Matrix4d ZROTNEG90 = new Matrix4d();
	private static Matrix4d ZROTPOS90 = new Matrix4d();

	static {

		Matrix4d rot = new Matrix4d();

		rot.rotY(Math.toRadians(180));
		PLAYER_DEG0.set(rot);

		rot.rotY(Math.toRadians(90));
		PLAYER_DEG90.set(rot);

		rot.rotY(Math.toRadians(0));
		PLAYER_DEG180.set(rot);

		rot.rotY(Math.toRadians(-90));
		PLAYER_DEG270.set(rot);

		rot.rotX(Math.toRadians(-90));
		XROTNEG90.set(rot);

		rot.rotX(Math.toRadians(90));
		XROTPOS90.set(rot);

		rot.rotZ(Math.toRadians(-90));
		ZROTNEG90.set(rot);

		rot.rotZ(Math.toRadians(90));
		ZROTPOS90.set(rot);

	}

	public static Matrix4d calculateRotation(Location location) {

		double yaw = Math.abs(location.getYaw());

		yaw %= 360;

		double baseAngle = 0;
		int sign = (location.getYaw() < 0) ? -1 : 1;

		// Snap to a basis vector
		if (yaw > (90 - 45) & yaw <= (90 + 45)) {
			baseAngle = 90;
		} else if (yaw > (180 - 45) & yaw <= (180 + 45)) {
			baseAngle = 180;
		} else if (yaw > (270 - 45) && yaw <= (270 + 45)) {
			baseAngle = 270;
		}

		if (sign < 0) {
			baseAngle = 360 - baseAngle;
		}

		double pitch = location.getPitch();

		boolean down = false;

		if (pitch > 45) {
			down = true;
		}

		Matrix4d orientation = new Matrix4d();
		orientation.setIdentity();

		if (baseAngle == 0 || baseAngle == 360) {

			if (down) {
				orientation.mul(XROTPOS90);
			}

			orientation.mul(PLAYER_DEG0);
		} else if (baseAngle == 90) {

			if (down) {
				orientation.mul(ZROTPOS90);
			}

			orientation.mul(PLAYER_DEG90);

		} else if (baseAngle == 180) {

			if (down) {
				orientation.mul(XROTNEG90);
			}

			orientation.mul(PLAYER_DEG180);
		} else if (baseAngle == 270) {

			if (down) {
				orientation.mul(ZROTNEG90);
			}

			orientation.mul(PLAYER_DEG270);
		}

		return orientation;
	}
}
