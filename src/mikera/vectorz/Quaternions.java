/*
 * Decompiled with CFR 0_129.
 */
package mikera.vectorz;

import mikera.vectorz.Vector3;
import mikera.vectorz.Vector4;

public class Quaternions {
    public static Vector4 conjugate(Vector4 a) {
        return new Vector4(- a.x, - a.y, - a.z, a.t);
    }

    public static Vector4 mul(Vector4 a, Vector4 b) {
        double rt = a.t * b.t - a.x * b.x - a.y * b.y - a.z * b.z;
        double rx = a.t * b.x + a.x * b.t + a.y * b.z - a.z * b.y;
        double ry = a.t * b.y - a.x * b.z + a.y * b.t + a.z * b.x;
        double rz = a.t * b.z + a.x * b.y - a.y * b.x + a.z * b.t;
        return new Vector4(rx, ry, rz, rt);
    }

    public static Vector3 rotate(Vector4 a, Vector3 b) {
        double rx = a.t * b.x + a.y * b.z - a.z * b.y;
        double ry = a.t * b.y - a.x * b.z + a.z * b.x;
        double rz = a.t * b.z + a.x * b.y - a.y * b.x;
        return new Vector3(rx, ry, rz);
    }

    public static Vector4 axisAngle(Vector3 axis, double angle) {
        double ha = angle;
        double ca = Math.cos(ha);
        double sa = Math.sin(ha);
        double x = axis.x;
        double y = axis.y;
        double z = axis.z;
        double d = x * x + y * y + z * z;
        double ff = sa;
        ff = d < 0.99999 || d > 1.00001 ? (ff /= Math.sqrt(d)) : (ff -= (d - 1.0) * 0.5);
        return new Vector4(ff * x, ff * y, ff * z, ca);
    }

    public static Vector4 normalise(Vector4 a) {
        double dd = a.elementSquaredSum();
        if (dd > 0.0) {
            double d = 1.0 / Math.sqrt(dd);
            return new Vector4(a.x * d, a.y * d, a.z * d, a.t * d);
        }
        return new Vector4(0.0, 0.0, 0.0, 1.0);
    }

    public static Vector4 invert(Vector4 a) {
        double dd = a.elementSquaredSum();
        double ff = 1.0 / dd;
        return new Vector4((- a.x) * ff, (- a.y) * ff, (- a.z) * ff, a.t * ff);
    }
}

