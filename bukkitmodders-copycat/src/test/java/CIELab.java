

import java.awt.color.ColorSpace;
import java.awt.color.ICC_Profile;

public class CIELab extends ColorSpace {

	public static CIELab getInstance() {

		return Holder.INSTANCE;
	}

	@Override
	public float[] fromCIEXYZ(float[] colorvalue) {
		double l = f(colorvalue[1]);
		double L = 116.0 * l - 16.0;
		double a = 500.0 * (f(colorvalue[0]) - l);
		double b = 200.0 * (l - f(colorvalue[2]));
		return new float[] { (float) L, (float) a, (float) b };
	}

	public float[] rgb2lab(int R, int G, int B, int alpha) {
		// http://www.brucelindbloom.com

		float r, g, b, X, Y, Z, fx, fy, fz, xr, yr, zr;
		float Ls, as, bs;
		float eps = 216.f / 24389.f;
		float k = 24389.f / 27.f;

		float Xr = 0.964221f; // reference white D50
		float Yr = 1.0f;
		float Zr = 0.825211f;

		// RGB to XYZ
		r = R / 255.f; // R 0..1
		g = G / 255.f; // G 0..1
		b = B / 255.f; // B 0..1

		// assuming sRGB (D65)
		if (r <= 0.04045)
			r = r / 12;
		else
			r = (float) Math.pow((r + 0.055) / 1.055, 2.4);

		if (g <= 0.04045)
			g = g / 12;
		else
			g = (float) Math.pow((g + 0.055) / 1.055, 2.4);

		if (b <= 0.04045)
			b = b / 12;
		else
			b = (float) Math.pow((b + 0.055) / 1.055, 2.4);

		X = 0.436052025f * r + 0.385081593f * g + 0.143087414f * b;
		Y = 0.222491598f * r + 0.71688606f * g + 0.060621486f * b;
		Z = 0.013929122f * r + 0.097097002f * g + 0.71418547f * b;

		// XYZ to Lab
		xr = X / Xr;
		yr = Y / Yr;
		zr = Z / Zr;

		if (xr > eps)
			fx = (float) Math.pow(xr, 1 / 3.);
		else
			fx = (float) ((k * xr + 16.) / 116.);

		if (yr > eps)
			fy = (float) Math.pow(yr, 1 / 3.);
		else
			fy = (float) ((k * yr + 16.) / 116.);

		if (zr > eps)
			fz = (float) Math.pow(zr, 1 / 3.);
		else
			fz = (float) ((k * zr + 16.) / 116);

		Ls = (116 * fy) - 16;
		as = 500 * (fx - fy);
		bs = 200 * (fy - fz);

		float[] lab = new float[4];
		lab[0] = (float) (2.55 * Ls + .5);
		lab[1] = (float) (as + .5);
		lab[2] = (float) (bs + .5);
		lab[3] = alpha;

		return lab;
	}

	@Override
	public float[] fromRGB(float[] rgbvalue) {
		float[] xyz = CIEXYZ.fromRGB(rgbvalue);
		return fromCIEXYZ(xyz);
	}

	@Override
	public float getMaxValue(int component) {
		return 128f;
	}

	@Override
	public float getMinValue(int component) {
		return (component == 0) ? 0f : -128f;
	}

	@Override
	public String getName(int idx) {
		return String.valueOf("Lab".charAt(idx));
	}

	@Override
	public float[] toCIEXYZ(float[] colorvalue) {
		double i = (colorvalue[0] + 16.0) * (1.0 / 116.0);
		double X = fInv(i + colorvalue[1] * (1.0 / 500.0));
		double Y = fInv(i);
		double Z = fInv(i - colorvalue[2] * (1.0 / 200.0));
		return new float[] { (float) X, (float) Y, (float) Z };
	}

	@Override
	public float[] toRGB(float[] colorvalue) {
		float[] xyz = toCIEXYZ(colorvalue);
		return CIEXYZ.toRGB(xyz);
	}

	CIELab() {
		super(ColorSpace.TYPE_Lab, 3);
	}

	private static double f(double x) {
		if (x > 216.0 / 24389.0) {
			return Math.cbrt(x);
		} else {
			return (841.0 / 108.0) * x + N;
		}
	}

	private static double fInv(double x) {
		if (x > 6.0 / 29.0) {
			return x * x * x;
		} else {
			return (108.0 / 841.0) * (x - N);
		}
	}

	private Object readResolve() {
		return getInstance();
	}

	private static class Holder {
		static final CIELab INSTANCE = new CIELab();
	}

	private static final long serialVersionUID = 5027741380892134289L;

	private static final ColorSpace CIEXYZ = ColorSpace.getInstance(ColorSpace.CS_CIEXYZ);

	private static final double N = 4.0 / 29.0;

}