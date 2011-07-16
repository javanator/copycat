package org.bukkitmodders.copycat.util;

import javax.vecmath.Matrix4d;

import org.bukkit.Location;

public class MatrixUtil {

	private static Matrix4d PLAYER_NORTH_ROT = new Matrix4d();
	private static Matrix4d PLAYER_EAST_ROT = new Matrix4d();
	private static Matrix4d PLAYER_SOUTH_ROT = new Matrix4d();
	private static Matrix4d PLAYER_WEST_ROT = new Matrix4d();

	private static Matrix4d PLAYER_LOOK_DOWN_ROT = new Matrix4d();
	private static Matrix4d PLAYER_LOOK_UP_ROT = new Matrix4d();

	static {

		Matrix4d rot = new Matrix4d();

		rot.setIdentity();
		rot.rotY(Math.toRadians(180));
		PLAYER_NORTH_ROT.set(rot);

		rot.setIdentity();
		rot.rotY(Math.toRadians(90));
		PLAYER_EAST_ROT.set(rot);

		rot.setIdentity();
		rot.rotY(Math.toRadians(0));
		PLAYER_SOUTH_ROT.set(rot);

		rot.setIdentity();
		rot.rotY(Math.toRadians(-90));
		PLAYER_WEST_ROT.set(rot);

		rot.setIdentity();
		rot.rotX(-90);
		PLAYER_LOOK_DOWN_ROT.set(rot);
	}

	public static Matrix4d calculateOrientation(Location location) {

		double yaw = Math.abs(location.getYaw());

		yaw %= 360;

		double baseAngle = 0; // N
		int sign = (location.getYaw() < 0) ? -1 : 1;

		// Snap to a basis vector
		if (yaw > (90 - 45) & yaw < (90 + 45)) {
			baseAngle = 90; // E
		} else if (yaw > (180 - 45) & yaw < (180 + 45)) {
			baseAngle = 180; // S
		} else if (yaw > (270 - 45) && yaw < (270 + 45)) {
			baseAngle = 270; // W
		}

		if (sign < 0) {
			baseAngle = 360 - baseAngle;
		}

		double pitch = location.getPitch();

		boolean down = false;
		boolean up = false;

		if (pitch > 45) {
			down = true;
		} else if (pitch < -45) {
			up = true;
		}

		Matrix4d orientation = new Matrix4d();
		orientation.setIdentity();

		if (baseAngle == 0 || baseAngle == 360) {
			orientation.mul(PLAYER_NORTH_ROT);
		} else if (baseAngle == 90) {
			orientation.mul(PLAYER_EAST_ROT);
		} else if (baseAngle == 180) {
			orientation.mul(PLAYER_SOUTH_ROT);
		} else if (baseAngle == 270) {
			orientation.mul(PLAYER_WEST_ROT);
		}

		return orientation;
	}

}
