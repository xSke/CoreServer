/*
 * Decompiled with CFR 0_129.
 */
package mikera.matrixx.decompose.impl.svd;

import mikera.matrixx.AMatrix;
import mikera.matrixx.Matrix;
import mikera.matrixx.algo.impl.Constants;
import mikera.matrixx.decompose.ISVDResult;
import mikera.matrixx.decompose.impl.svd.SVDResult;
import mikera.matrixx.impl.DiagonalMatrix;
import mikera.vectorz.AVector;
import mikera.vectorz.Vector;

public class ThinSVD {
    public static ISVDResult decompose(AMatrix a) {
        return ThinSVD.decompose(Matrix.create(a));
    }

    public static ISVDResult decompose(Matrix matrix) {
        return ThinSVD.decomposeInternal(matrix.clone());
    }

    private static ISVDResult decomposeInternal(Matrix a) {
        double t;
        int k;
        int i;
        int cc;
        int i2;
        int j;
        int j2;
        int rc = a.rowCount();
        if (rc < (cc = a.columnCount())) {
            throw new IllegalArgumentException("Wrong matrix size: rows < columns");
        }
        int n = Math.min(rc, cc);
        Matrix u = Matrix.create(rc, n);
        Vector s = Vector.createLength(cc);
        Matrix v = Matrix.create(cc, cc);
        Vector e = Vector.createLength(cc);
        Vector work = Vector.createLength(rc);
        int nct = Math.min(rc - 1, cc);
        int nrt = Math.max(0, Math.min(cc - 2, rc));
        for (int k2 = 0; k2 < Math.max(nct, nrt); ++k2) {
            int i3;
            int i4;
            double t2;
            if (k2 < nct) {
                for (i3 = k2; i3 < rc; ++i3) {
                    s.set(k2, Math.hypot(s.get(k2), a.get(i3, k2)));
                }
                if (Math.abs(s.get(k2)) > Constants.EPS) {
                    if (a.get(k2, k2) < 0.0) {
                        s.set(k2, - s.get(k2));
                    }
                    for (i3 = k2; i3 < rc; ++i3) {
                        a.set(i3, k2, a.get(i3, k2) / s.get(k2));
                    }
                    a.addAt(k2, k2, 1.0);
                }
                s.set(k2, - s.get(k2));
            }
            for (j = k2 + 1; j < cc; ++j) {
                if (k2 < nct && Math.abs(s.get(k2)) > Constants.EPS) {
                    t2 = 0.0;
                    for (i4 = k2; i4 < rc; ++i4) {
                        t2 += a.get(i4, k2) * a.get(i4, j);
                    }
                    t2 = (- t2) / a.get(k2, k2);
                    for (i4 = k2; i4 < rc; ++i4) {
                        a.addAt(i4, j, t2 * a.get(i4, k2));
                    }
                }
                e.set(j, a.get(k2, j));
            }
            if (k2 < nct) {
                for (i3 = k2; i3 < rc; ++i3) {
                    u.set(i3, k2, a.get(i3, k2));
                }
            }
            if (k2 >= nrt) continue;
            e.set(k2, 0.0);
            for (i3 = k2 + 1; i3 < cc; ++i3) {
                e.set(k2, Math.hypot(e.get(k2), e.get(i3)));
            }
            if (Math.abs(e.get(k2)) > Constants.EPS) {
                if (e.get(k2 + 1) < 0.0) {
                    e.set(k2, - e.get(k2));
                }
                for (i3 = k2 + 1; i3 < cc; ++i3) {
                    e.set(i3, e.get(i3) / e.get(k2));
                }
                e.addAt(k2 + 1, 1.0);
            }
            e.set(k2, - e.get(k2));
            if (k2 + 1 < rc && Math.abs(e.get(k2)) > Constants.EPS) {
                for (j = k2 + 1; j < cc; ++j) {
                    for (int i5 = k2 + 1; i5 < rc; ++i5) {
                        work.addAt(i5, e.get(j) * a.get(i5, j));
                    }
                }
                for (j = k2 + 1; j < cc; ++j) {
                    t2 = (- e.get(j)) / e.get(k2 + 1);
                    for (i4 = k2 + 1; i4 < rc; ++i4) {
                        a.addAt(i4, j, t2 * work.get(i4));
                    }
                }
            }
            for (i3 = k2 + 1; i3 < cc; ++i3) {
                v.set(i3, k2, e.get(i3));
            }
        }
        int p = Math.min(cc, rc + 1);
        if (nct < cc) {
            s.set(nct, a.get(nct, nct));
        }
        if (rc < p) {
            s.set(p - 1, 0.0);
        }
        if (nrt + 1 < p) {
            e.set(nrt, a.get(nrt, p - 1));
        }
        e.set(p - 1, 0.0);
        for (j = nct; j < n; ++j) {
            for (i = 0; i < rc; ++i) {
                u.set(i, j, 0.0);
            }
            u.set(j, j, 1.0);
        }
        for (k = nct - 1; k >= 0; --k) {
            if (Math.abs(s.get(k)) > Constants.EPS) {
                for (j2 = k + 1; j2 < n; ++j2) {
                    t = 0.0;
                    for (i2 = k; i2 < rc; ++i2) {
                        t += u.get(i2, k) * u.get(i2, j2);
                    }
                    t = (- t) / u.get(k, k);
                    for (i2 = k; i2 < rc; ++i2) {
                        u.addAt(i2, j2, t * u.get(i2, k));
                    }
                }
                for (i = k; i < rc; ++i) {
                    u.set(i, k, - u.get(i, k));
                }
                u.addAt(k, k, 1.0);
                for (i = 0; i < k - 1; ++i) {
                    u.set(i, k, 0.0);
                }
                continue;
            }
            for (i = 0; i < rc; ++i) {
                u.set(i, k, 0.0);
            }
            u.set(k, k, 1.0);
        }
        for (k = n - 1; k >= 0; --k) {
            if (k < nrt & Math.abs(e.get(k)) > Constants.EPS) {
                for (j2 = k + 1; j2 < n; ++j2) {
                    t = 0.0;
                    for (i2 = k + 1; i2 < cc; ++i2) {
                        t += v.get(i2, k) * v.get(i2, j2);
                    }
                    t = (- t) / v.get(k + 1, k);
                    for (i2 = k + 1; i2 < cc; ++i2) {
                        v.addAt(i2, j2, t * v.get(i2, k));
                    }
                }
            }
            for (i = 0; i < cc; ++i) {
                v.set(i, k, 0.0);
            }
            v.set(k, k, 1.0);
        }
        int pp = p - 1;
        int iter = 0;
        double eps = Math.pow(2.0, -52.0);
        double tiny = Math.pow(2.0, -966.0);
        block34 : while (p > 0) {
            int kase;
            int k3;
            for (k3 = p - 2; k3 >= -1 && k3 != -1; --k3) {
                if (Math.abs(e.get(k3)) > tiny + eps * (Math.abs(s.get(k3)) + Math.abs(s.get(k3 + 1)))) continue;
                e.set(k3, 0.0);
                break;
            }
            if (k3 == p - 2) {
                kase = 4;
            } else {
                int ks;
                for (ks = p - 1; ks >= k3 && ks != k3; --ks) {
                    double t3 = (ks != p ? Math.abs(e.get(ks)) : 0.0) + (ks != k3 + 1 ? Math.abs(e.get(ks - 1)) : 0.0);
                    if (Math.abs(s.get(ks)) > tiny + eps * t3) continue;
                    s.set(ks, 0.0);
                    break;
                }
                if (ks == k3) {
                    kase = 3;
                } else if (ks == p - 1) {
                    kase = 1;
                } else {
                    kase = 2;
                    k3 = ks;
                }
            }
            ++k3;
            switch (kase) {
                int i6;
                double t4;
                double cs;
                double sj;
                int j3;
                double sn;
                case 1: {
                    double f = e.get(p - 2);
                    e.set(p - 2, 0.0);
                    for (j3 = p - 2; j3 >= k3; --j3) {
                        sj = s.unsafeGet(j3);
                        t4 = Math.hypot(sj, f);
                        cs = sj / t4;
                        sn = f / t4;
                        s.set(j3, j3, t4);
                        if (j3 != k3) {
                            f = (- sn) * e.get(j3 - 1);
                            e.set(j3 - 1, cs * e.get(j3 - 1));
                        }
                        for (i6 = 0; i6 < cc; ++i6) {
                            t4 = cs * v.get(i6, j3) + sn * v.get(i6, p - 1);
                            v.set(i6, p - 1, (- sn) * v.get(i6, j3) + cs * v.get(i6, p - 1));
                            v.set(i6, j3, t4);
                        }
                    }
                    continue block34;
                }
                case 2: {
                    double f = e.get(k3 - 1);
                    e.set(k3 - 1, 0.0);
                    for (j3 = k3; j3 < p; ++j3) {
                        sj = s.unsafeGet(j3);
                        t4 = Math.hypot(sj, f);
                        cs = sj / t4;
                        sn = f / t4;
                        s.set(j3, j3, t4);
                        f = (- sn) * e.get(j3);
                        e.set(j3, cs * e.get(j3));
                        for (i6 = 0; i6 < rc; ++i6) {
                            t4 = cs * u.get(i6, j3) + sn * u.get(i6, k3 - 1);
                            u.set(i6, k3 - 1, (- sn) * u.get(i6, j3) + cs * u.get(i6, k3 - 1));
                            u.set(i6, j3, t4);
                        }
                    }
                    continue block34;
                }
                case 3: {
                    double scale = Math.max(Math.max(Math.max(Math.max(Math.abs(s.get(p - 1)), Math.abs(s.get(p - 2))), Math.abs(e.get(p - 2))), Math.abs(s.get(k3))), Math.abs(e.get(k3)));
                    double sp = s.get(p - 1) / scale;
                    double spm1 = s.get(p - 2) / scale;
                    double epm1 = e.get(p - 2) / scale;
                    double sk = s.get(k3) / scale;
                    double ek = e.get(k3) / scale;
                    double b = ((spm1 + sp) * (spm1 - sp) + epm1 * epm1) / 2.0;
                    double c = sp * epm1 * (sp * epm1);
                    double shift = 0.0;
                    if (b != 0.0 | c != 0.0) {
                        shift = Math.sqrt(b * b + c);
                        if (b < 0.0) {
                            shift = - shift;
                        }
                        shift = c / (b + shift);
                    }
                    double f = (sk + sp) * (sk - sp) + shift;
                    double g = sk * ek;
                    for (int j4 = k3; j4 < p - 1; ++j4) {
                        int i7;
                        double t5 = Math.hypot(f, g);
                        double cs2 = f / t5;
                        double sn2 = g / t5;
                        if (j4 != k3) {
                            e.set(j4 - 1, t5);
                        }
                        double sj2 = s.unsafeGet(j4);
                        f = cs2 * sj2 + sn2 * e.get(j4);
                        e.set(j4, cs2 * e.get(j4) - sn2 * sj2);
                        g = sn2 * s.get(j4 + 1);
                        s.set(j4 + 1, cs2 * s.get(j4 + 1));
                        for (i7 = 0; i7 < cc; ++i7) {
                            t5 = cs2 * v.get(i7, j4) + sn2 * v.get(i7, j4 + 1);
                            v.set(i7, j4 + 1, (- sn2) * v.get(i7, j4) + cs2 * v.get(i7, j4 + 1));
                            v.set(i7, j4, t5);
                        }
                        t5 = Math.hypot(f, g);
                        cs2 = f / t5;
                        sn2 = g / t5;
                        s.set(j4, t5);
                        f = cs2 * e.get(j4) + sn2 * s.get(j4 + 1);
                        s.set(j4 + 1, (- sn2) * e.get(j4) + cs2 * s.get(j4 + 1));
                        g = sn2 * e.get(j4 + 1);
                        e.set(j4 + 1, e.get(j4 + 1) * cs2);
                        if (j4 >= rc - 1) continue;
                        for (i7 = 0; i7 < rc; ++i7) {
                            t5 = cs2 * u.get(i7, j4) + sn2 * u.get(i7, j4 + 1);
                            u.set(i7, j4 + 1, (- sn2) * u.get(i7, j4) + cs2 * u.get(i7, j4 + 1));
                            u.set(i7, j4, t5);
                        }
                    }
                    e.set(p - 2, f);
                    ++iter;
                    break;
                }
                case 4: {
                    double skk = s.get(k3);
                    if (skk <= 0.0) {
                        s.set(k3, - skk);
                        for (int i8 = 0; i8 <= pp; ++i8) {
                            v.set(i8, k3, - v.get(i8, k3));
                        }
                    }
                    while (k3 < pp && s.get(k3) < s.get(k3 + 1)) {
                        double t6 = s.get(k3);
                        s.set(k3, s.get(k3 + 1));
                        s.set(k3 + 1, t6);
                        if (k3 < cc - 1) {
                            v.swapColumns(k3, k3 + 1);
                        }
                        if (k3 < rc - 1) {
                            u.swapColumns(k3, k3 + 1);
                        }
                        ++k3;
                    }
                    iter = 0;
                    --p;
                }
            }
        }
        return new SVDResult(u, DiagonalMatrix.wrap(s), v, s);
    }
}

