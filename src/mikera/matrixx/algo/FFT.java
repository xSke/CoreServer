/*
 * Decompiled with CFR 0_129.
 */
package mikera.matrixx.algo;

public class FFT {
    private int n;
    private int nBluestein;
    private int[] ip;
    private double[] w;
    private int nw;
    private int nc;
    private double[] wtable;
    private double[] wtable_r;
    private double[] bk1;
    private double[] bk2;
    private Plans plan;
    private static final int[] factors = new int[]{4, 2, 3, 5};
    private static final double PI = 3.141592653589793;
    private static final double TWO_PI = 6.283185307179586;

    public static int nextPow2(int x) {
        if (x < 1) {
            throw new IllegalArgumentException("x must be greater or equal 1");
        }
        if ((x & x - 1) == 0) {
            return x;
        }
        x |= x >>> 1;
        x |= x >>> 2;
        x |= x >>> 4;
        x |= x >>> 8;
        x |= x >>> 16;
        return x + 1;
    }

    public static int prevPow2(int x) {
        if (x < 1) {
            throw new IllegalArgumentException("x must be greater or equal 1");
        }
        return (int)Math.pow(2.0, Math.floor(Math.log(x) / Math.log(2.0)));
    }

    public static boolean isPowerOf2(int x) {
        if (x <= 0) {
            return false;
        }
        return (x & x - 1) == 0;
    }

    public FFT(int n) {
        if (n < 1) {
            throw new IllegalArgumentException("n must be greater than 0");
        }
        this.n = n;
        if (!FFT.isPowerOf2(n)) {
            if (FFT.getReminder(n, factors) >= 211) {
                this.plan = Plans.BLUESTEIN;
                this.nBluestein = FFT.nextPow2(n * 2 - 1);
                this.bk1 = new double[2 * this.nBluestein];
                this.bk2 = new double[2 * this.nBluestein];
                this.ip = new int[2 + (int)Math.ceil(2 + (1 << (int)(Math.log((double)this.nBluestein + 0.5) / Math.log(2.0)) / 2))];
                this.w = new double[this.nBluestein];
                int twon = 2 * this.nBluestein;
                this.nw = this.ip[0];
                if (twon > this.nw << 2) {
                    this.nw = twon >> 2;
                    this.makewt(this.nw);
                }
                this.nc = this.ip[1];
                if (this.nBluestein > this.nc << 2) {
                    this.nc = this.nBluestein >> 2;
                    this.makect(this.nc, this.w, this.nw);
                }
                this.bluesteini();
            } else {
                this.plan = Plans.MIXED_RADIX;
                this.wtable = new double[4 * n + 15];
                this.wtable_r = new double[2 * n + 15];
                this.cffti();
                this.rffti();
            }
        } else {
            this.plan = Plans.SPLIT_RADIX;
            this.ip = new int[2 + (int)Math.ceil(2 + (1 << (int)(Math.log((double)n + 0.5) / Math.log(2.0)) / 2))];
            this.w = new double[n];
            int twon = 2 * n;
            this.nw = this.ip[0];
            if (twon > this.nw << 2) {
                this.nw = twon >> 2;
                this.makewt(this.nw);
            }
            this.nc = this.ip[1];
            if (n > this.nc << 2) {
                this.nc = n >> 2;
                this.makect(this.nc, this.w, this.nw);
            }
        }
    }

    public void complexForward(double[] a) {
        this.complexForward(a, 0);
    }

    public void complexForward(double[] a, int offa) {
        if (this.n == 1) {
            return;
        }
        switch (this.plan) {
            case SPLIT_RADIX: {
                this.cftbsub(2 * this.n, a, offa, this.ip, this.nw, this.w);
                break;
            }
            case MIXED_RADIX: {
                this.cfftf(a, offa, -1);
                break;
            }
            case BLUESTEIN: {
                this.bluestein_complex(a, offa, -1);
            }
        }
    }

    public void complexInverse(double[] a, boolean scale) {
        this.complexInverse(a, 0, scale);
    }

    public void complexInverse(double[] a, int offa, boolean scale) {
        if (this.n == 1) {
            return;
        }
        switch (this.plan) {
            case SPLIT_RADIX: {
                this.cftfsub(2 * this.n, a, offa, this.ip, this.nw, this.w);
                break;
            }
            case MIXED_RADIX: {
                this.cfftf(a, offa, 1);
                break;
            }
            case BLUESTEIN: {
                this.bluestein_complex(a, offa, 1);
            }
        }
        if (scale) {
            this.scale(this.n, a, offa, true);
        }
    }

    public void realForward(double[] a) {
        this.realForward(a, 0);
    }

    public void realForward(double[] a, int offa) {
        if (this.n == 1) {
            return;
        }
        switch (this.plan) {
            case SPLIT_RADIX: {
                if (this.n > 4) {
                    this.cftfsub(this.n, a, offa, this.ip, this.nw, this.w);
                    this.rftfsub(this.n, a, offa, this.nc, this.w, this.nw);
                } else if (this.n == 4) {
                    this.cftx020(a, offa);
                }
                double xi = a[offa] - a[offa + 1];
                double[] arrd = a;
                int n = offa;
                arrd[n] = arrd[n] + a[offa + 1];
                a[offa + 1] = xi;
                break;
            }
            case MIXED_RADIX: {
                this.rfftf(a, offa);
                for (int k = this.n - 1; k >= 2; --k) {
                    int idx = offa + k;
                    double tmp = a[idx];
                    a[idx] = a[idx - 1];
                    a[idx - 1] = tmp;
                }
                break;
            }
            case BLUESTEIN: {
                this.bluestein_real_forward(a, offa);
            }
        }
    }

    public void realForwardFull(double[] a) {
        this.realForwardFull(a, 0);
    }

    public void realForwardFull(double[] a, int offa) {
        int twon = 2 * this.n;
        switch (this.plan) {
            case SPLIT_RADIX: {
                this.realForward(a, offa);
                for (int k = 0; k < this.n / 2; ++k) {
                    int idx1 = 2 * k;
                    int idx2 = offa + (twon - idx1) % twon;
                    a[idx2] = a[offa + idx1];
                    a[idx2 + 1] = - a[offa + idx1 + 1];
                }
                a[offa + this.n] = - a[offa + 1];
                a[offa + 1] = 0.0;
                break;
            }
            case MIXED_RADIX: {
                int k;
                this.rfftf(a, offa);
                int m = this.n % 2 == 0 ? this.n / 2 : (this.n + 1) / 2;
                for (k = 1; k < m; ++k) {
                    int idx1 = offa + twon - 2 * k;
                    int idx2 = offa + 2 * k;
                    a[idx1 + 1] = - a[idx2];
                    a[idx1] = a[idx2 - 1];
                }
                for (k = 1; k < this.n; ++k) {
                    int idx = offa + this.n - k;
                    double tmp = a[idx + 1];
                    a[idx + 1] = a[idx];
                    a[idx] = tmp;
                }
                a[offa + 1] = 0.0;
                break;
            }
            case BLUESTEIN: {
                this.bluestein_real_full(a, offa, -1);
            }
        }
    }

    public void realInverse(double[] a, boolean scale) {
        this.realInverse(a, 0, scale);
    }

    public void realInverse(double[] a, int offa, boolean scale) {
        if (this.n == 1) {
            return;
        }
        switch (this.plan) {
            case SPLIT_RADIX: {
                a[offa + 1] = 0.5 * (a[offa] - a[offa + 1]);
                double[] arrd = a;
                int n = offa;
                arrd[n] = arrd[n] - a[offa + 1];
                if (this.n > 4) {
                    this.rftfsub(this.n, a, offa, this.nc, this.w, this.nw);
                    this.cftbsub(this.n, a, offa, this.ip, this.nw, this.w);
                } else if (this.n == 4) {
                    this.cftxc020(a, offa);
                }
                if (!scale) break;
                this.scale(this.n / 2, a, offa, false);
                break;
            }
            case MIXED_RADIX: {
                for (int k = 2; k < this.n; ++k) {
                    int idx = offa + k;
                    double tmp = a[idx - 1];
                    a[idx - 1] = a[idx];
                    a[idx] = tmp;
                }
                this.rfftb(a, offa);
                if (!scale) break;
                this.scale(this.n, a, offa, false);
                break;
            }
            case BLUESTEIN: {
                this.bluestein_real_inverse(a, offa);
                if (!scale) break;
                this.scale(this.n, a, offa, false);
            }
        }
    }

    public void realInverseFull(double[] a, boolean scale) {
        this.realInverseFull(a, 0, scale);
    }

    public void realInverseFull(double[] a, int offa, boolean scale) {
        int twon = 2 * this.n;
        switch (this.plan) {
            case SPLIT_RADIX: {
                this.realInverse2(a, offa, scale);
                for (int k = 0; k < this.n / 2; ++k) {
                    int idx1 = 2 * k;
                    int idx2 = offa + (twon - idx1) % twon;
                    a[idx2] = a[offa + idx1];
                    a[idx2 + 1] = - a[offa + idx1 + 1];
                }
                a[offa + this.n] = - a[offa + 1];
                a[offa + 1] = 0.0;
                break;
            }
            case MIXED_RADIX: {
                int k;
                this.rfftf(a, offa);
                if (scale) {
                    this.scale(this.n, a, offa, false);
                }
                int m = this.n % 2 == 0 ? this.n / 2 : (this.n + 1) / 2;
                for (k = 1; k < m; ++k) {
                    int idx1 = offa + 2 * k;
                    int idx2 = offa + twon - 2 * k;
                    a[idx1] = - a[idx1];
                    a[idx2 + 1] = - a[idx1];
                    a[idx2] = a[idx1 - 1];
                }
                for (k = 1; k < this.n; ++k) {
                    int idx = offa + this.n - k;
                    double tmp = a[idx + 1];
                    a[idx + 1] = a[idx];
                    a[idx] = tmp;
                }
                a[offa + 1] = 0.0;
                break;
            }
            case BLUESTEIN: {
                this.bluestein_real_full(a, offa, 1);
                if (!scale) break;
                this.scale(this.n, a, offa, true);
            }
        }
    }

    protected void realInverse2(double[] a, int offa, boolean scale) {
        if (this.n == 1) {
            return;
        }
        switch (this.plan) {
            case SPLIT_RADIX: {
                if (this.n > 4) {
                    this.cftfsub(this.n, a, offa, this.ip, this.nw, this.w);
                    this.rftbsub(this.n, a, offa, this.nc, this.w, this.nw);
                } else if (this.n == 4) {
                    this.cftbsub(this.n, a, offa, this.ip, this.nw, this.w);
                }
                double xi = a[offa] - a[offa + 1];
                double[] arrd = a;
                int n = offa;
                arrd[n] = arrd[n] + a[offa + 1];
                a[offa + 1] = xi;
                if (!scale) break;
                this.scale(this.n, a, offa, false);
                break;
            }
            case MIXED_RADIX: {
                int m;
                int i;
                this.rfftf(a, offa);
                for (int k = this.n - 1; k >= 2; --k) {
                    int idx = offa + k;
                    double tmp = a[idx];
                    a[idx] = a[idx - 1];
                    a[idx - 1] = tmp;
                }
                if (scale) {
                    this.scale(this.n, a, offa, false);
                }
                if (this.n % 2 == 0) {
                    m = this.n / 2;
                    for (i = 1; i < m; ++i) {
                        int idx = offa + 2 * i + 1;
                        a[idx] = - a[idx];
                    }
                } else {
                    m = (this.n - 1) / 2;
                    for (i = 0; i < m; ++i) {
                        int idx = offa + 2 * i + 1;
                        a[idx] = - a[idx];
                    }
                }
                break;
            }
            case BLUESTEIN: {
                this.bluestein_real_inverse2(a, offa);
                if (!scale) break;
                this.scale(this.n, a, offa, false);
            }
        }
    }

    private static int getReminder(int n, int[] factors) {
        int reminder = n;
        if (n <= 0) {
            throw new IllegalArgumentException("n must be positive integer");
        }
        for (int i = 0; i < factors.length && reminder != 1; ++i) {
            int factor = factors[i];
            while (reminder % factor == 0) {
                reminder /= factor;
            }
        }
        return reminder;
    }

    void cffti(int n, int offw) {
        int idx;
        int i;
        if (n == 1) {
            return;
        }
        int twon = 2 * n;
        int fourn = 4 * n;
        int ntry = 0;
        int nl = n;
        int nf = 0;
        int j = 0;
        block0 : do {
            ntry = ++j <= 4 ? factors[j - 1] : (ntry += 2);
            do {
                int nq;
                int nr;
                if ((nr = nl - ntry * (nq = nl / ntry)) != 0) continue block0;
                this.wtable[offw + ++nf + 1 + fourn] = ntry;
                nl = nq;
                if (ntry != 2 || nf == 1) continue;
                for (i = 2; i <= nf; ++i) {
                    int ib = nf - i + 2;
                    idx = ib + fourn;
                    this.wtable[offw + idx + 1] = this.wtable[offw + idx];
                }
                this.wtable[offw + 2 + fourn] = 2.0;
            } while (nl != 1);
            break;
        } while (true);
        this.wtable[offw + fourn] = n;
        this.wtable[offw + 1 + fourn] = nf;
        double argh = 6.283185307179586 / (double)n;
        i = 1;
        int l1 = 1;
        for (int k1 = 1; k1 <= nf; ++k1) {
            int ip = (int)this.wtable[offw + k1 + 1 + fourn];
            int ld = 0;
            int l2 = l1 * ip;
            int ido = n / l2;
            int idot = ido + ido + 2;
            int ipm = ip - 1;
            for (j = 1; j <= ipm; ++j) {
                int i1 = i;
                this.wtable[offw + i - 1 + twon] = 1.0;
                this.wtable[offw + i + twon] = 0.0;
                double fi = 0.0;
                double argld = (double)(ld += l1) * argh;
                for (int ii = 4; ii <= idot; ii += 2) {
                    double arg = (fi += 1.0) * argld;
                    idx = (i += 2) + twon;
                    this.wtable[offw + idx - 1] = Math.cos(arg);
                    this.wtable[offw + idx] = Math.sin(arg);
                }
                if (ip <= 5) continue;
                int idx1 = i1 + twon;
                int idx2 = i + twon;
                this.wtable[offw + idx1 - 1] = this.wtable[offw + idx2 - 1];
                this.wtable[offw + idx1] = this.wtable[offw + idx2];
            }
            l1 = l2;
        }
    }

    void cffti() {
        int idx;
        int i;
        if (this.n == 1) {
            return;
        }
        int twon = 2 * this.n;
        int fourn = 4 * this.n;
        int ntry = 0;
        int nl = this.n;
        int nf = 0;
        int j = 0;
        block0 : do {
            ntry = ++j <= 4 ? factors[j - 1] : (ntry += 2);
            do {
                int nr;
                int nq;
                if ((nr = nl - ntry * (nq = nl / ntry)) != 0) continue block0;
                this.wtable[++nf + 1 + fourn] = ntry;
                nl = nq;
                if (ntry != 2 || nf == 1) continue;
                for (i = 2; i <= nf; ++i) {
                    int ib = nf - i + 2;
                    idx = ib + fourn;
                    this.wtable[idx + 1] = this.wtable[idx];
                }
                this.wtable[2 + fourn] = 2.0;
            } while (nl != 1);
            break;
        } while (true);
        this.wtable[fourn] = this.n;
        this.wtable[1 + fourn] = nf;
        double argh = 6.283185307179586 / (double)this.n;
        i = 1;
        int l1 = 1;
        for (int k1 = 1; k1 <= nf; ++k1) {
            int ip = (int)this.wtable[k1 + 1 + fourn];
            int ld = 0;
            int l2 = l1 * ip;
            int ido = this.n / l2;
            int idot = ido + ido + 2;
            int ipm = ip - 1;
            for (j = 1; j <= ipm; ++j) {
                int i1 = i;
                this.wtable[i - 1 + twon] = 1.0;
                this.wtable[i + twon] = 0.0;
                double fi = 0.0;
                double argld = (double)(ld += l1) * argh;
                for (int ii = 4; ii <= idot; ii += 2) {
                    double arg = (fi += 1.0) * argld;
                    idx = (i += 2) + twon;
                    this.wtable[idx - 1] = Math.cos(arg);
                    this.wtable[idx] = Math.sin(arg);
                }
                if (ip <= 5) continue;
                int idx1 = i1 + twon;
                int idx2 = i + twon;
                this.wtable[idx1 - 1] = this.wtable[idx2 - 1];
                this.wtable[idx1] = this.wtable[idx2];
            }
            l1 = l2;
        }
    }

    void rffti() {
        int i;
        int idx;
        if (this.n == 1) {
            return;
        }
        int twon = 2 * this.n;
        int ntry = 0;
        int nl = this.n;
        int nf = 0;
        int j = 0;
        block0 : do {
            ntry = ++j <= 4 ? factors[j - 1] : (ntry += 2);
            do {
                int nq;
                int nr;
                if ((nr = nl - ntry * (nq = nl / ntry)) != 0) continue block0;
                this.wtable_r[++nf + 1 + twon] = ntry;
                nl = nq;
                if (ntry != 2 || nf == 1) continue;
                for (i = 2; i <= nf; ++i) {
                    int ib = nf - i + 2;
                    idx = ib + twon;
                    this.wtable_r[idx + 1] = this.wtable_r[idx];
                }
                this.wtable_r[2 + twon] = 2.0;
            } while (nl != 1);
            break;
        } while (true);
        this.wtable_r[twon] = this.n;
        this.wtable_r[1 + twon] = nf;
        double argh = 6.283185307179586 / (double)this.n;
        int is = 0;
        int nfm1 = nf - 1;
        int l1 = 1;
        if (nfm1 == 0) {
            return;
        }
        for (int k1 = 1; k1 <= nfm1; ++k1) {
            int ip = (int)this.wtable_r[k1 + 1 + twon];
            int ld = 0;
            int l2 = l1 * ip;
            int ido = this.n / l2;
            int ipm = ip - 1;
            for (j = 1; j <= ipm; ++j) {
                i = is;
                double argld = (double)(ld += l1) * argh;
                double fi = 0.0;
                for (int ii = 3; ii <= ido; ii += 2) {
                    double arg = (fi += 1.0) * argld;
                    idx = (i += 2) + this.n;
                    this.wtable_r[idx - 2] = Math.cos(arg);
                    this.wtable_r[idx - 1] = Math.sin(arg);
                }
                is += ido;
            }
            l1 = l2;
        }
    }

    private void bluesteini() {
        int k = 0;
        double pi_n = 3.141592653589793 / (double)this.n;
        this.bk1[0] = 1.0;
        this.bk1[1] = 0.0;
        for (int i = 1; i < this.n; ++i) {
            if ((k += 2 * i - 1) >= 2 * this.n) {
                k -= 2 * this.n;
            }
            double arg = pi_n * (double)k;
            this.bk1[2 * i] = Math.cos(arg);
            this.bk1[2 * i + 1] = Math.sin(arg);
        }
        double scale = 1.0 / (double)this.nBluestein;
        this.bk2[0] = this.bk1[0] * scale;
        this.bk2[1] = this.bk1[1] * scale;
        for (int i = 2; i < 2 * this.n; i += 2) {
            this.bk2[i] = this.bk1[i] * scale;
            this.bk2[i + 1] = this.bk1[i + 1] * scale;
            this.bk2[2 * this.nBluestein - i] = this.bk2[i];
            this.bk2[2 * this.nBluestein - i + 1] = this.bk2[i + 1];
        }
        this.cftbsub(2 * this.nBluestein, this.bk2, 0, this.ip, this.nw, this.w);
    }

    private void makewt(int nw) {
        this.ip[0] = nw;
        this.ip[1] = 1;
        if (nw > 2) {
            int j;
            int nwh = nw >> 1;
            double delta = 0.7853981633974483 / (double)nwh;
            double delta2 = delta * 2.0;
            double wn4r = Math.cos(delta * (double)nwh);
            this.w[0] = 1.0;
            this.w[1] = wn4r;
            if (nwh == 4) {
                this.w[2] = Math.cos(delta2);
                this.w[3] = Math.sin(delta2);
            } else if (nwh > 4) {
                this.makeipt(nw);
                this.w[2] = 0.5 / Math.cos(delta2);
                this.w[3] = 0.5 / Math.cos(delta * 6.0);
                for (j = 4; j < nwh; j += 4) {
                    double deltaj = delta * (double)j;
                    double deltaj3 = 3.0 * deltaj;
                    this.w[j] = Math.cos(deltaj);
                    this.w[j + 1] = Math.sin(deltaj);
                    this.w[j + 2] = Math.cos(deltaj3);
                    this.w[j + 3] = - Math.sin(deltaj3);
                }
            }
            int nw0 = 0;
            while (nwh > 2) {
                double wk1i;
                double wk1r;
                int nw1 = nw0 + nwh;
                this.w[nw1] = 1.0;
                this.w[nw1 + 1] = wn4r;
                if ((nwh >>= 1) == 4) {
                    wk1r = this.w[nw0 + 4];
                    wk1i = this.w[nw0 + 5];
                    this.w[nw1 + 2] = wk1r;
                    this.w[nw1 + 3] = wk1i;
                } else if (nwh > 4) {
                    wk1r = this.w[nw0 + 4];
                    double wk3r = this.w[nw0 + 6];
                    this.w[nw1 + 2] = 0.5 / wk1r;
                    this.w[nw1 + 3] = 0.5 / wk3r;
                    for (j = 4; j < nwh; j += 4) {
                        int idx1 = nw0 + 2 * j;
                        int idx2 = nw1 + j;
                        wk1r = this.w[idx1];
                        wk1i = this.w[idx1 + 1];
                        wk3r = this.w[idx1 + 2];
                        double wk3i = this.w[idx1 + 3];
                        this.w[idx2] = wk1r;
                        this.w[idx2 + 1] = wk1i;
                        this.w[idx2 + 2] = wk3r;
                        this.w[idx2 + 3] = wk3i;
                    }
                }
                nw0 = nw1;
            }
        }
    }

    private void makeipt(int nw) {
        this.ip[2] = 0;
        this.ip[3] = 16;
        int m = 2;
        for (int l = nw; l > 32; l >>= 2) {
            int m2 = m << 1;
            int q = m2 << 3;
            for (int j = m; j < m2; ++j) {
                int p;
                this.ip[m + j] = p = this.ip[j] << 2;
                this.ip[m2 + j] = p + q;
            }
            m = m2;
        }
    }

    private void makect(int nc, double[] c, int startc) {
        this.ip[1] = nc;
        if (nc > 1) {
            int nch = nc >> 1;
            double delta = 0.7853981633974483 / (double)nch;
            c[startc] = Math.cos(delta * (double)nch);
            c[startc + nch] = 0.5 * c[startc];
            for (int j = 1; j < nch; ++j) {
                double deltaj = delta * (double)j;
                c[startc + j] = 0.5 * Math.cos(deltaj);
                c[startc + nc - j] = 0.5 * Math.sin(deltaj);
            }
        }
    }

    private void bluestein_complex(double[] a, int offa, int isign) {
        int idx2;
        int i;
        int idx4;
        int idx3;
        int idx1;
        double[] ak = new double[2 * this.nBluestein];
        if (isign > 0) {
            for (i = 0; i < this.n; ++i) {
                idx1 = 2 * i;
                idx2 = idx1 + 1;
                idx3 = offa + idx1;
                idx4 = offa + idx2;
                ak[idx1] = a[idx3] * this.bk1[idx1] - a[idx4] * this.bk1[idx2];
                ak[idx2] = a[idx3] * this.bk1[idx2] + a[idx4] * this.bk1[idx1];
            }
        } else {
            for (i = 0; i < this.n; ++i) {
                idx1 = 2 * i;
                idx2 = idx1 + 1;
                idx3 = offa + idx1;
                idx4 = offa + idx2;
                ak[idx1] = a[idx3] * this.bk1[idx1] + a[idx4] * this.bk1[idx2];
                ak[idx2] = (- a[idx3]) * this.bk1[idx2] + a[idx4] * this.bk1[idx1];
            }
        }
        this.cftbsub(2 * this.nBluestein, ak, 0, this.ip, this.nw, this.w);
        if (isign > 0) {
            for (i = 0; i < this.nBluestein; ++i) {
                idx1 = 2 * i;
                idx2 = idx1 + 1;
                double im = (- ak[idx1]) * this.bk2[idx2] + ak[idx2] * this.bk2[idx1];
                ak[idx1] = ak[idx1] * this.bk2[idx1] + ak[idx2] * this.bk2[idx2];
                ak[idx2] = im;
            }
        } else {
            for (i = 0; i < this.nBluestein; ++i) {
                idx1 = 2 * i;
                idx2 = idx1 + 1;
                double im = ak[idx1] * this.bk2[idx2] + ak[idx2] * this.bk2[idx1];
                ak[idx1] = ak[idx1] * this.bk2[idx1] - ak[idx2] * this.bk2[idx2];
                ak[idx2] = im;
            }
        }
        this.cftfsub(2 * this.nBluestein, ak, 0, this.ip, this.nw, this.w);
        if (isign > 0) {
            for (i = 0; i < this.n; ++i) {
                idx1 = 2 * i;
                idx2 = idx1 + 1;
                idx3 = offa + idx1;
                idx4 = offa + idx2;
                a[idx3] = this.bk1[idx1] * ak[idx1] - this.bk1[idx2] * ak[idx2];
                a[idx4] = this.bk1[idx2] * ak[idx1] + this.bk1[idx1] * ak[idx2];
            }
        } else {
            for (i = 0; i < this.n; ++i) {
                idx1 = 2 * i;
                idx2 = idx1 + 1;
                idx3 = offa + idx1;
                idx4 = offa + idx2;
                a[idx3] = this.bk1[idx1] * ak[idx1] + this.bk1[idx2] * ak[idx2];
                a[idx4] = (- this.bk1[idx2]) * ak[idx1] + this.bk1[idx1] * ak[idx2];
            }
        }
    }

    private void bluestein_real_full(double[] a, int offa, int isign) {
        int idx2;
        int i;
        int idx3;
        int idx1;
        double[] ak = new double[2 * this.nBluestein];
        if (isign > 0) {
            for (i = 0; i < this.n; ++i) {
                idx1 = 2 * i;
                idx2 = idx1 + 1;
                idx3 = offa + i;
                ak[idx1] = a[idx3] * this.bk1[idx1];
                ak[idx2] = a[idx3] * this.bk1[idx2];
            }
        } else {
            for (i = 0; i < this.n; ++i) {
                idx1 = 2 * i;
                idx2 = idx1 + 1;
                idx3 = offa + i;
                ak[idx1] = a[idx3] * this.bk1[idx1];
                ak[idx2] = (- a[idx3]) * this.bk1[idx2];
            }
        }
        this.cftbsub(2 * this.nBluestein, ak, 0, this.ip, this.nw, this.w);
        if (isign > 0) {
            for (i = 0; i < this.nBluestein; ++i) {
                idx1 = 2 * i;
                idx2 = idx1 + 1;
                double im = (- ak[idx1]) * this.bk2[idx2] + ak[idx2] * this.bk2[idx1];
                ak[idx1] = ak[idx1] * this.bk2[idx1] + ak[idx2] * this.bk2[idx2];
                ak[idx2] = im;
            }
        } else {
            for (i = 0; i < this.nBluestein; ++i) {
                idx1 = 2 * i;
                idx2 = idx1 + 1;
                double im = ak[idx1] * this.bk2[idx2] + ak[idx2] * this.bk2[idx1];
                ak[idx1] = ak[idx1] * this.bk2[idx1] - ak[idx2] * this.bk2[idx2];
                ak[idx2] = im;
            }
        }
        this.cftfsub(2 * this.nBluestein, ak, 0, this.ip, this.nw, this.w);
        if (isign > 0) {
            for (i = 0; i < this.n; ++i) {
                idx1 = 2 * i;
                idx2 = idx1 + 1;
                a[offa + idx1] = this.bk1[idx1] * ak[idx1] - this.bk1[idx2] * ak[idx2];
                a[offa + idx2] = this.bk1[idx2] * ak[idx1] + this.bk1[idx1] * ak[idx2];
            }
        } else {
            for (i = 0; i < this.n; ++i) {
                idx1 = 2 * i;
                idx2 = idx1 + 1;
                a[offa + idx1] = this.bk1[idx1] * ak[idx1] + this.bk1[idx2] * ak[idx2];
                a[offa + idx2] = (- this.bk1[idx2]) * ak[idx1] + this.bk1[idx1] * ak[idx2];
            }
        }
    }

    private void bluestein_real_forward(double[] a, int offa) {
        int idx2;
        int i;
        int idx1;
        double[] ak = new double[2 * this.nBluestein];
        for (i = 0; i < this.n; ++i) {
            idx1 = 2 * i;
            idx2 = idx1 + 1;
            int idx3 = offa + i;
            ak[idx1] = a[idx3] * this.bk1[idx1];
            ak[idx2] = (- a[idx3]) * this.bk1[idx2];
        }
        this.cftbsub(2 * this.nBluestein, ak, 0, this.ip, this.nw, this.w);
        for (i = 0; i < this.nBluestein; ++i) {
            idx1 = 2 * i;
            idx2 = idx1 + 1;
            double im = ak[idx1] * this.bk2[idx2] + ak[idx2] * this.bk2[idx1];
            ak[idx1] = ak[idx1] * this.bk2[idx1] - ak[idx2] * this.bk2[idx2];
            ak[idx2] = im;
        }
        this.cftfsub(2 * this.nBluestein, ak, 0, this.ip, this.nw, this.w);
        if (this.n % 2 == 0) {
            a[offa] = this.bk1[0] * ak[0] + this.bk1[1] * ak[1];
            a[offa + 1] = this.bk1[this.n] * ak[this.n] + this.bk1[this.n + 1] * ak[this.n + 1];
            for (i = 1; i < this.n / 2; ++i) {
                idx1 = 2 * i;
                idx2 = idx1 + 1;
                a[offa + idx1] = this.bk1[idx1] * ak[idx1] + this.bk1[idx2] * ak[idx2];
                a[offa + idx2] = (- this.bk1[idx2]) * ak[idx1] + this.bk1[idx1] * ak[idx2];
            }
        } else {
            a[offa] = this.bk1[0] * ak[0] + this.bk1[1] * ak[1];
            a[offa + 1] = (- this.bk1[this.n]) * ak[this.n - 1] + this.bk1[this.n - 1] * ak[this.n];
            for (i = 1; i < (this.n - 1) / 2; ++i) {
                idx1 = 2 * i;
                idx2 = idx1 + 1;
                a[offa + idx1] = this.bk1[idx1] * ak[idx1] + this.bk1[idx2] * ak[idx2];
                a[offa + idx2] = (- this.bk1[idx2]) * ak[idx1] + this.bk1[idx1] * ak[idx2];
            }
            a[offa + this.n - 1] = this.bk1[this.n - 1] * ak[this.n - 1] + this.bk1[this.n] * ak[this.n];
        }
    }

    private void bluestein_real_inverse(double[] a, int offa) {
        int idx2;
        int idx3;
        int i;
        int idx4;
        int idx1;
        double[] ak = new double[2 * this.nBluestein];
        if (this.n % 2 == 0) {
            ak[0] = a[offa] * this.bk1[0];
            ak[1] = a[offa] * this.bk1[1];
            for (i = 1; i < this.n / 2; ++i) {
                idx1 = 2 * i;
                idx2 = idx1 + 1;
                idx3 = offa + idx1;
                idx4 = offa + idx2;
                ak[idx1] = a[idx3] * this.bk1[idx1] - a[idx4] * this.bk1[idx2];
                ak[idx2] = a[idx3] * this.bk1[idx2] + a[idx4] * this.bk1[idx1];
            }
            ak[this.n] = a[offa + 1] * this.bk1[this.n];
            ak[this.n + 1] = a[offa + 1] * this.bk1[this.n + 1];
            for (i = this.n / 2 + 1; i < this.n; ++i) {
                idx1 = 2 * i;
                idx2 = idx1 + 1;
                idx3 = offa + 2 * this.n - idx1;
                idx4 = idx3 + 1;
                ak[idx1] = a[idx3] * this.bk1[idx1] + a[idx4] * this.bk1[idx2];
                ak[idx2] = a[idx3] * this.bk1[idx2] - a[idx4] * this.bk1[idx1];
            }
        } else {
            ak[0] = a[offa] * this.bk1[0];
            ak[1] = a[offa] * this.bk1[1];
            for (i = 1; i < (this.n - 1) / 2; ++i) {
                idx1 = 2 * i;
                idx2 = idx1 + 1;
                idx3 = offa + idx1;
                idx4 = offa + idx2;
                ak[idx1] = a[idx3] * this.bk1[idx1] - a[idx4] * this.bk1[idx2];
                ak[idx2] = a[idx3] * this.bk1[idx2] + a[idx4] * this.bk1[idx1];
            }
            ak[this.n - 1] = a[offa + this.n - 1] * this.bk1[this.n - 1] - a[offa + 1] * this.bk1[this.n];
            ak[this.n] = a[offa + this.n - 1] * this.bk1[this.n] + a[offa + 1] * this.bk1[this.n - 1];
            ak[this.n + 1] = a[offa + this.n - 1] * this.bk1[this.n + 1] + a[offa + 1] * this.bk1[this.n + 2];
            ak[this.n + 2] = a[offa + this.n - 1] * this.bk1[this.n + 2] - a[offa + 1] * this.bk1[this.n + 1];
            for (i = (this.n - 1) / 2 + 2; i < this.n; ++i) {
                idx1 = 2 * i;
                idx2 = idx1 + 1;
                idx3 = offa + 2 * this.n - idx1;
                idx4 = idx3 + 1;
                ak[idx1] = a[idx3] * this.bk1[idx1] + a[idx4] * this.bk1[idx2];
                ak[idx2] = a[idx3] * this.bk1[idx2] - a[idx4] * this.bk1[idx1];
            }
        }
        this.cftbsub(2 * this.nBluestein, ak, 0, this.ip, this.nw, this.w);
        for (i = 0; i < this.nBluestein; ++i) {
            idx1 = 2 * i;
            idx2 = idx1 + 1;
            double im = (- ak[idx1]) * this.bk2[idx2] + ak[idx2] * this.bk2[idx1];
            ak[idx1] = ak[idx1] * this.bk2[idx1] + ak[idx2] * this.bk2[idx2];
            ak[idx2] = im;
        }
        this.cftfsub(2 * this.nBluestein, ak, 0, this.ip, this.nw, this.w);
        for (i = 0; i < this.n; ++i) {
            idx1 = 2 * i;
            idx2 = idx1 + 1;
            a[offa + i] = this.bk1[idx1] * ak[idx1] - this.bk1[idx2] * ak[idx2];
        }
    }

    private void bluestein_real_inverse2(double[] a, int offa) {
        int idx2;
        int i;
        int idx1;
        double[] ak = new double[2 * this.nBluestein];
        for (i = 0; i < this.n; ++i) {
            idx1 = 2 * i;
            idx2 = idx1 + 1;
            int idx3 = offa + i;
            ak[idx1] = a[idx3] * this.bk1[idx1];
            ak[idx2] = a[idx3] * this.bk1[idx2];
        }
        this.cftbsub(2 * this.nBluestein, ak, 0, this.ip, this.nw, this.w);
        for (i = 0; i < this.nBluestein; ++i) {
            idx1 = 2 * i;
            idx2 = idx1 + 1;
            double im = (- ak[idx1]) * this.bk2[idx2] + ak[idx2] * this.bk2[idx1];
            ak[idx1] = ak[idx1] * this.bk2[idx1] + ak[idx2] * this.bk2[idx2];
            ak[idx2] = im;
        }
        this.cftfsub(2 * this.nBluestein, ak, 0, this.ip, this.nw, this.w);
        if (this.n % 2 == 0) {
            a[offa] = this.bk1[0] * ak[0] - this.bk1[1] * ak[1];
            a[offa + 1] = this.bk1[this.n] * ak[this.n] - this.bk1[this.n + 1] * ak[this.n + 1];
            for (i = 1; i < this.n / 2; ++i) {
                idx1 = 2 * i;
                idx2 = idx1 + 1;
                a[offa + idx1] = this.bk1[idx1] * ak[idx1] - this.bk1[idx2] * ak[idx2];
                a[offa + idx2] = this.bk1[idx2] * ak[idx1] + this.bk1[idx1] * ak[idx2];
            }
        } else {
            a[offa] = this.bk1[0] * ak[0] - this.bk1[1] * ak[1];
            a[offa + 1] = this.bk1[this.n] * ak[this.n - 1] + this.bk1[this.n - 1] * ak[this.n];
            for (i = 1; i < (this.n - 1) / 2; ++i) {
                idx1 = 2 * i;
                idx2 = idx1 + 1;
                a[offa + idx1] = this.bk1[idx1] * ak[idx1] - this.bk1[idx2] * ak[idx2];
                a[offa + idx2] = this.bk1[idx2] * ak[idx1] + this.bk1[idx1] * ak[idx2];
            }
            a[offa + this.n - 1] = this.bk1[this.n - 1] * ak[this.n - 1] - this.bk1[this.n] * ak[this.n];
        }
    }

    void rfftf(double[] a, int offa) {
        if (this.n == 1) {
            return;
        }
        double[] ch = new double[this.n];
        int twon = 2 * this.n;
        int nf = (int)this.wtable_r[1 + twon];
        int na = 1;
        int l2 = this.n;
        int iw = twon - 1;
        for (int k1 = 1; k1 <= nf; ++k1) {
            int kh = nf - k1;
            int ip = (int)this.wtable_r[kh + 2 + twon];
            int l1 = l2 / ip;
            int ido = this.n / l2;
            int idl1 = ido * l1;
            iw -= (ip - 1) * ido;
            na = 1 - na;
            switch (ip) {
                case 2: {
                    if (na == 0) {
                        this.radf2(ido, l1, a, offa, ch, 0, iw);
                        break;
                    }
                    this.radf2(ido, l1, ch, 0, a, offa, iw);
                    break;
                }
                case 3: {
                    if (na == 0) {
                        this.radf3(ido, l1, a, offa, ch, 0, iw);
                        break;
                    }
                    this.radf3(ido, l1, ch, 0, a, offa, iw);
                    break;
                }
                case 4: {
                    if (na == 0) {
                        this.radf4(ido, l1, a, offa, ch, 0, iw);
                        break;
                    }
                    this.radf4(ido, l1, ch, 0, a, offa, iw);
                    break;
                }
                case 5: {
                    if (na == 0) {
                        this.radf5(ido, l1, a, offa, ch, 0, iw);
                        break;
                    }
                    this.radf5(ido, l1, ch, 0, a, offa, iw);
                    break;
                }
                default: {
                    if (ido == 1) {
                        na = 1 - na;
                    }
                    if (na == 0) {
                        this.radfg(ido, ip, l1, idl1, a, offa, ch, 0, iw);
                        na = 1;
                        break;
                    }
                    this.radfg(ido, ip, l1, idl1, ch, 0, a, offa, iw);
                    na = 0;
                }
            }
            l2 = l1;
        }
        if (na == 1) {
            return;
        }
        System.arraycopy(ch, 0, a, offa, this.n);
    }

    void rfftb(double[] a, int offa) {
        if (this.n == 1) {
            return;
        }
        double[] ch = new double[this.n];
        int twon = 2 * this.n;
        int nf = (int)this.wtable_r[1 + twon];
        int na = 0;
        int l1 = 1;
        int iw = this.n;
        for (int k1 = 1; k1 <= nf; ++k1) {
            int ip = (int)this.wtable_r[k1 + 1 + twon];
            int l2 = ip * l1;
            int ido = this.n / l2;
            int idl1 = ido * l1;
            switch (ip) {
                case 2: {
                    if (na == 0) {
                        this.radb2(ido, l1, a, offa, ch, 0, iw);
                    } else {
                        this.radb2(ido, l1, ch, 0, a, offa, iw);
                    }
                    na = 1 - na;
                    break;
                }
                case 3: {
                    if (na == 0) {
                        this.radb3(ido, l1, a, offa, ch, 0, iw);
                    } else {
                        this.radb3(ido, l1, ch, 0, a, offa, iw);
                    }
                    na = 1 - na;
                    break;
                }
                case 4: {
                    if (na == 0) {
                        this.radb4(ido, l1, a, offa, ch, 0, iw);
                    } else {
                        this.radb4(ido, l1, ch, 0, a, offa, iw);
                    }
                    na = 1 - na;
                    break;
                }
                case 5: {
                    if (na == 0) {
                        this.radb5(ido, l1, a, offa, ch, 0, iw);
                    } else {
                        this.radb5(ido, l1, ch, 0, a, offa, iw);
                    }
                    na = 1 - na;
                    break;
                }
                default: {
                    if (na == 0) {
                        this.radbg(ido, ip, l1, idl1, a, offa, ch, 0, iw);
                    } else {
                        this.radbg(ido, ip, l1, idl1, ch, 0, a, offa, iw);
                    }
                    if (ido != 1) break;
                    na = 1 - na;
                }
            }
            l1 = l2;
            iw += (ip - 1) * ido;
        }
        if (na == 0) {
            return;
        }
        System.arraycopy(ch, 0, a, offa, this.n);
    }

    void radf2(int ido, int l1, double[] in, int in_off, double[] out, int out_off, int offset) {
        int idx2;
        int oidx1;
        int k;
        int iw1 = offset;
        int idx0 = l1 * ido;
        int idx1 = 2 * ido;
        for (k = 0; k < l1; ++k) {
            oidx1 = out_off + k * idx1;
            int oidx2 = oidx1 + idx1 - 1;
            int iidx1 = in_off + k * ido;
            int iidx2 = iidx1 + idx0;
            double i1r = in[iidx1];
            double i2r = in[iidx2];
            out[oidx1] = i1r + i2r;
            out[oidx2] = i1r - i2r;
        }
        if (ido < 2) {
            return;
        }
        if (ido != 2) {
            for (k = 0; k < l1; ++k) {
                idx1 = k * ido;
                idx2 = 2 * idx1;
                int idx3 = idx2 + ido;
                int idx4 = idx1 + idx0;
                for (int i = 2; i < ido; i += 2) {
                    int ic = ido - i;
                    int widx1 = i - 1 + iw1;
                    int oidx12 = out_off + i + idx2;
                    int oidx2 = out_off + ic + idx3;
                    int iidx1 = in_off + i + idx1;
                    int iidx2 = in_off + i + idx4;
                    double a1i = in[iidx1 - 1];
                    double a1r = in[iidx1];
                    double a2i = in[iidx2 - 1];
                    double a2r = in[iidx2];
                    double w1r = this.wtable_r[widx1 - 1];
                    double w1i = this.wtable_r[widx1];
                    double t1r = w1r * a2i + w1i * a2r;
                    double t1i = w1r * a2r - w1i * a2i;
                    out[oidx12] = a1r + t1i;
                    out[oidx12 - 1] = a1i + t1r;
                    out[oidx2] = t1i - a1r;
                    out[oidx2 - 1] = a1i - t1r;
                }
            }
            if (ido % 2 == 1) {
                return;
            }
        }
        idx2 = 2 * idx1;
        for (k = 0; k < l1; ++k) {
            idx1 = k * ido;
            oidx1 = out_off + idx2 + ido;
            int iidx1 = in_off + ido - 1 + idx1;
            out[oidx1] = - in[iidx1 + idx0];
            out[oidx1 - 1] = in[iidx1];
        }
    }

    void radb2(int ido, int l1, double[] in, int in_off, double[] out, int out_off, int offset) {
        int idx3;
        int idx1;
        int idx2;
        int k;
        int iw1 = offset;
        int idx0 = l1 * ido;
        for (k = 0; k < l1; ++k) {
            idx1 = k * ido;
            idx2 = 2 * idx1;
            idx3 = idx2 + ido;
            int oidx1 = out_off + idx1;
            int iidx1 = in_off + idx2;
            int iidx2 = in_off + ido - 1 + idx3;
            double i1r = in[iidx1];
            double i2r = in[iidx2];
            out[oidx1] = i1r + i2r;
            out[oidx1 + idx0] = i1r - i2r;
        }
        if (ido < 2) {
            return;
        }
        if (ido != 2) {
            for (k = 0; k < l1; ++k) {
                idx1 = k * ido;
                idx2 = 2 * idx1;
                idx3 = idx2 + ido;
                int idx4 = idx1 + idx0;
                for (int i = 2; i < ido; i += 2) {
                    int ic = ido - i;
                    int idx5 = i - 1 + iw1;
                    int idx6 = out_off + i;
                    int idx7 = in_off + i;
                    int idx8 = in_off + ic;
                    double w1r = this.wtable_r[idx5 - 1];
                    double w1i = this.wtable_r[idx5];
                    int iidx1 = idx7 + idx2;
                    int iidx2 = idx8 + idx3;
                    int oidx1 = idx6 + idx1;
                    int oidx2 = idx6 + idx4;
                    double t1r = in[iidx1 - 1] - in[iidx2 - 1];
                    double t1i = in[iidx1] + in[iidx2];
                    double i1i = in[iidx1];
                    double i1r = in[iidx1 - 1];
                    double i2i = in[iidx2];
                    double i2r = in[iidx2 - 1];
                    out[oidx1 - 1] = i1r + i2r;
                    out[oidx1] = i1i - i2i;
                    out[oidx2 - 1] = w1r * t1r - w1i * t1i;
                    out[oidx2] = w1r * t1i + w1i * t1r;
                }
            }
            if (ido % 2 == 1) {
                return;
            }
        }
        for (k = 0; k < l1; ++k) {
            idx1 = k * ido;
            idx2 = 2 * idx1;
            int oidx1 = out_off + ido - 1 + idx1;
            int iidx1 = in_off + idx2 + ido;
            out[oidx1] = 2.0 * in[iidx1 - 1];
            out[oidx1 + idx0] = -2.0 * in[iidx1];
        }
    }

    void radf3(int ido, int l1, double[] in, int in_off, double[] out, int out_off, int offset) {
        int k;
        double cr2;
        double taur = -0.5;
        double taui = 0.8660254037844387;
        int iw1 = offset;
        int iw2 = iw1 + ido;
        int idx0 = l1 * ido;
        for (k = 0; k < l1; ++k) {
            int idx1 = k * ido;
            int idx3 = 2 * idx0;
            int idx4 = (3 * k + 1) * ido;
            int iidx1 = in_off + idx1;
            int iidx2 = iidx1 + idx0;
            int iidx3 = iidx1 + idx3;
            double i1r = in[iidx1];
            double i2r = in[iidx2];
            double i3r = in[iidx3];
            cr2 = i2r + i3r;
            out[out_off + 3 * idx1] = i1r + cr2;
            out[out_off + idx4 + ido] = 0.8660254037844387 * (i3r - i2r);
            out[out_off + ido - 1 + idx4] = i1r + -0.5 * cr2;
        }
        if (ido == 1) {
            return;
        }
        for (k = 0; k < l1; ++k) {
            int idx3 = k * ido;
            int idx4 = 3 * idx3;
            int idx5 = idx3 + idx0;
            int idx6 = idx5 + idx0;
            int idx7 = idx4 + ido;
            int idx8 = idx7 + ido;
            for (int i = 2; i < ido; i += 2) {
                int ic = ido - i;
                int widx1 = i - 1 + iw1;
                int widx2 = i - 1 + iw2;
                double w1r = this.wtable_r[widx1 - 1];
                double w1i = this.wtable_r[widx1];
                double w2r = this.wtable_r[widx2 - 1];
                double w2i = this.wtable_r[widx2];
                int idx9 = in_off + i;
                int idx10 = out_off + i;
                int idx11 = out_off + ic;
                int iidx1 = idx9 + idx3;
                int iidx2 = idx9 + idx5;
                int iidx3 = idx9 + idx6;
                double i1i = in[iidx1 - 1];
                double i1r = in[iidx1];
                double i2i = in[iidx2 - 1];
                double i2r = in[iidx2];
                double i3i = in[iidx3 - 1];
                double i3r = in[iidx3];
                double dr2 = w1r * i2i + w1i * i2r;
                double di2 = w1r * i2r - w1i * i2i;
                double dr3 = w2r * i3i + w2i * i3r;
                double di3 = w2r * i3r - w2i * i3i;
                cr2 = dr2 + dr3;
                double ci2 = di2 + di3;
                double tr2 = i1i + -0.5 * cr2;
                double ti2 = i1r + -0.5 * ci2;
                double tr3 = 0.8660254037844387 * (di2 - di3);
                double ti3 = 0.8660254037844387 * (dr3 - dr2);
                int oidx1 = idx10 + idx4;
                int oidx2 = idx11 + idx7;
                int oidx3 = idx10 + idx8;
                out[oidx1 - 1] = i1i + cr2;
                out[oidx1] = i1r + ci2;
                out[oidx2 - 1] = tr2 - tr3;
                out[oidx2] = ti3 - ti2;
                out[oidx3 - 1] = tr2 + tr3;
                out[oidx3] = ti2 + ti3;
            }
        }
    }

    void radb3(int ido, int l1, double[] in, int in_off, double[] out, int out_off, int offset) {
        double tr2;
        double ci3;
        double cr2;
        double taur = -0.5;
        double taui = 0.8660254037844387;
        int iw1 = offset;
        int iw2 = iw1 + ido;
        for (int k = 0; k < l1; ++k) {
            int idx1 = k * ido;
            int iidx1 = in_off + 3 * idx1;
            int iidx2 = iidx1 + 2 * ido;
            double i1i = in[iidx1];
            tr2 = 2.0 * in[iidx2 - 1];
            cr2 = i1i + -0.5 * tr2;
            ci3 = 1.7320508075688774 * in[iidx2];
            out[out_off + idx1] = i1i + tr2;
            out[out_off + (k + l1) * ido] = cr2 - ci3;
            out[out_off + (k + 2 * l1) * ido] = cr2 + ci3;
        }
        if (ido == 1) {
            return;
        }
        int idx0 = l1 * ido;
        for (int k = 0; k < l1; ++k) {
            int idx1 = k * ido;
            int idx2 = 3 * idx1;
            int idx3 = idx2 + ido;
            int idx4 = idx3 + ido;
            int idx5 = idx1 + idx0;
            int idx6 = idx5 + idx0;
            for (int i = 2; i < ido; i += 2) {
                int ic = ido - i;
                int idx7 = in_off + i;
                int idx8 = in_off + ic;
                int idx9 = out_off + i;
                int iidx1 = idx7 + idx2;
                int iidx2 = idx7 + idx4;
                int iidx3 = idx8 + idx3;
                double i1i = in[iidx1 - 1];
                double i1r = in[iidx1];
                double i2i = in[iidx2 - 1];
                double i2r = in[iidx2];
                double i3i = in[iidx3 - 1];
                double i3r = in[iidx3];
                tr2 = i2i + i3i;
                cr2 = i1i + -0.5 * tr2;
                double ti2 = i2r - i3r;
                double ci2 = i1r + -0.5 * ti2;
                double cr3 = 0.8660254037844387 * (i2i - i3i);
                ci3 = 0.8660254037844387 * (i2r + i3r);
                double dr2 = cr2 - ci3;
                double dr3 = cr2 + ci3;
                double di2 = ci2 + cr3;
                double di3 = ci2 - cr3;
                int widx1 = i - 1 + iw1;
                int widx2 = i - 1 + iw2;
                double w1r = this.wtable_r[widx1 - 1];
                double w1i = this.wtable_r[widx1];
                double w2r = this.wtable_r[widx2 - 1];
                double w2i = this.wtable_r[widx2];
                int oidx1 = idx9 + idx1;
                int oidx2 = idx9 + idx5;
                int oidx3 = idx9 + idx6;
                out[oidx1 - 1] = i1i + tr2;
                out[oidx1] = i1r + ti2;
                out[oidx2 - 1] = w1r * dr2 - w1i * di2;
                out[oidx2] = w1r * di2 + w1i * dr2;
                out[oidx3 - 1] = w2r * dr3 - w2i * di3;
                out[oidx3] = w2r * di3 + w2i * dr3;
            }
        }
    }

    void radf4(int ido, int l1, double[] in, int in_off, double[] out, int out_off, int offset) {
        int idx8;
        double ti1;
        double tr2;
        double tr1;
        int k;
        int idx1;
        int idx5;
        int idx3;
        int idx6;
        int idx2;
        int idx4;
        double hsqt2 = 0.7071067811865476;
        int iw1 = offset;
        int iw2 = offset + ido;
        int iw3 = iw2 + ido;
        int idx0 = l1 * ido;
        for (k = 0; k < l1; ++k) {
            idx1 = k * ido;
            idx2 = 4 * idx1;
            idx3 = idx1 + idx0;
            idx4 = idx3 + idx0;
            idx5 = idx4 + idx0;
            idx6 = idx2 + ido;
            double i1r = in[in_off + idx1];
            double i2r = in[in_off + idx3];
            double i3r = in[in_off + idx4];
            double i4r = in[in_off + idx5];
            tr1 = i2r + i4r;
            tr2 = i1r + i3r;
            int oidx1 = out_off + idx2;
            int oidx2 = out_off + idx6 + ido;
            out[oidx1] = tr1 + tr2;
            out[oidx2 - 1 + ido + ido] = tr2 - tr1;
            out[oidx2 - 1] = i1r - i3r;
            out[oidx2] = i4r - i2r;
        }
        if (ido < 2) {
            return;
        }
        if (ido != 2) {
            for (k = 0; k < l1; ++k) {
                idx1 = k * ido;
                idx2 = idx1 + idx0;
                idx3 = idx2 + idx0;
                idx4 = idx3 + idx0;
                idx5 = 4 * idx1;
                idx6 = idx5 + ido;
                int idx7 = idx6 + ido;
                idx8 = idx7 + ido;
                for (int i = 2; i < ido; i += 2) {
                    int ic = ido - i;
                    int widx1 = i - 1 + iw1;
                    int widx2 = i - 1 + iw2;
                    int widx3 = i - 1 + iw3;
                    double w1r = this.wtable_r[widx1 - 1];
                    double w1i = this.wtable_r[widx1];
                    double w2r = this.wtable_r[widx2 - 1];
                    double w2i = this.wtable_r[widx2];
                    double w3r = this.wtable_r[widx3 - 1];
                    double w3i = this.wtable_r[widx3];
                    int idx9 = in_off + i;
                    int idx10 = out_off + i;
                    int idx11 = out_off + ic;
                    int iidx1 = idx9 + idx1;
                    int iidx2 = idx9 + idx2;
                    int iidx3 = idx9 + idx3;
                    int iidx4 = idx9 + idx4;
                    double i1i = in[iidx1 - 1];
                    double i1r = in[iidx1];
                    double i2i = in[iidx2 - 1];
                    double i2r = in[iidx2];
                    double i3i = in[iidx3 - 1];
                    double i3r = in[iidx3];
                    double i4i = in[iidx4 - 1];
                    double i4r = in[iidx4];
                    double cr2 = w1r * i2i + w1i * i2r;
                    double ci2 = w1r * i2r - w1i * i2i;
                    double cr3 = w2r * i3i + w2i * i3r;
                    double ci3 = w2r * i3r - w2i * i3i;
                    double cr4 = w3r * i4i + w3i * i4r;
                    double ci4 = w3r * i4r - w3i * i4i;
                    tr1 = cr2 + cr4;
                    double tr4 = cr4 - cr2;
                    ti1 = ci2 + ci4;
                    double ti4 = ci2 - ci4;
                    double ti2 = i1r + ci3;
                    double ti3 = i1r - ci3;
                    tr2 = i1i + cr3;
                    double tr3 = i1i - cr3;
                    int oidx1 = idx10 + idx5;
                    int oidx2 = idx11 + idx6;
                    int oidx3 = idx10 + idx7;
                    int oidx4 = idx11 + idx8;
                    out[oidx1 - 1] = tr1 + tr2;
                    out[oidx4 - 1] = tr2 - tr1;
                    out[oidx1] = ti1 + ti2;
                    out[oidx4] = ti1 - ti2;
                    out[oidx3 - 1] = ti4 + tr3;
                    out[oidx2 - 1] = tr3 - ti4;
                    out[oidx3] = tr4 + ti3;
                    out[oidx2] = tr4 - ti3;
                }
            }
            if (ido % 2 == 1) {
                return;
            }
        }
        for (k = 0; k < l1; ++k) {
            idx1 = k * ido;
            idx2 = 4 * idx1;
            idx3 = idx1 + idx0;
            idx4 = idx3 + idx0;
            idx5 = idx4 + idx0;
            idx6 = idx2 + ido;
            int idx7 = idx6 + ido;
            idx8 = idx7 + ido;
            int idx9 = in_off + ido;
            int idx10 = out_off + ido;
            double i1i = in[idx9 - 1 + idx1];
            double i2i = in[idx9 - 1 + idx3];
            double i3i = in[idx9 - 1 + idx4];
            double i4i = in[idx9 - 1 + idx5];
            ti1 = -0.7071067811865476 * (i2i + i4i);
            tr1 = 0.7071067811865476 * (i2i - i4i);
            out[idx10 - 1 + idx2] = tr1 + i1i;
            out[idx10 - 1 + idx7] = i1i - tr1;
            out[out_off + idx6] = ti1 - i3i;
            out[out_off + idx8] = ti1 + i3i;
        }
    }

    void radb4(int ido, int l1, double[] in, int in_off, double[] out, int out_off, int offset) {
        double ti2;
        int idx1;
        int idx8;
        int idx5;
        int idx6;
        int idx7;
        double ti1;
        int idx4;
        int idx2;
        double tr2;
        int idx3;
        double tr1;
        double tr4;
        int k;
        double tr3;
        double sqrt2 = 1.4142135623730951;
        int iw1 = offset;
        int iw2 = iw1 + ido;
        int iw3 = iw2 + ido;
        int idx0 = l1 * ido;
        for (k = 0; k < l1; ++k) {
            idx1 = k * ido;
            idx2 = 4 * idx1;
            idx3 = idx1 + idx0;
            idx4 = idx3 + idx0;
            idx5 = idx4 + idx0;
            idx6 = idx2 + ido;
            idx7 = idx6 + ido;
            idx8 = idx7 + ido;
            double i1r = in[in_off + idx2];
            double i2r = in[in_off + idx7];
            double i3r = in[in_off + ido - 1 + idx8];
            double i4r = in[in_off + ido - 1 + idx6];
            tr1 = i1r - i3r;
            tr2 = i1r + i3r;
            tr3 = i4r + i4r;
            tr4 = i2r + i2r;
            out[out_off + idx1] = tr2 + tr3;
            out[out_off + idx3] = tr1 - tr4;
            out[out_off + idx4] = tr2 - tr3;
            out[out_off + idx5] = tr1 + tr4;
        }
        if (ido < 2) {
            return;
        }
        if (ido != 2) {
            for (k = 0; k < l1; ++k) {
                idx1 = k * ido;
                idx2 = idx1 + idx0;
                idx3 = idx2 + idx0;
                idx4 = idx3 + idx0;
                idx5 = 4 * idx1;
                idx6 = idx5 + ido;
                idx7 = idx6 + ido;
                idx8 = idx7 + ido;
                for (int i = 2; i < ido; i += 2) {
                    int ic = ido - i;
                    int widx1 = i - 1 + iw1;
                    int widx2 = i - 1 + iw2;
                    int widx3 = i - 1 + iw3;
                    double w1r = this.wtable_r[widx1 - 1];
                    double w1i = this.wtable_r[widx1];
                    double w2r = this.wtable_r[widx2 - 1];
                    double w2i = this.wtable_r[widx2];
                    double w3r = this.wtable_r[widx3 - 1];
                    double w3i = this.wtable_r[widx3];
                    int idx12 = in_off + i;
                    int idx13 = in_off + ic;
                    int idx14 = out_off + i;
                    int iidx1 = idx12 + idx5;
                    int iidx2 = idx13 + idx6;
                    int iidx3 = idx12 + idx7;
                    int iidx4 = idx13 + idx8;
                    double i1i = in[iidx1 - 1];
                    double i1r = in[iidx1];
                    double i2i = in[iidx2 - 1];
                    double i2r = in[iidx2];
                    double i3i = in[iidx3 - 1];
                    double i3r = in[iidx3];
                    double i4i = in[iidx4 - 1];
                    double i4r = in[iidx4];
                    ti1 = i1r + i4r;
                    ti2 = i1r - i4r;
                    double ti3 = i3r - i2r;
                    tr4 = i3r + i2r;
                    tr1 = i1i - i4i;
                    tr2 = i1i + i4i;
                    double ti4 = i3i - i2i;
                    tr3 = i3i + i2i;
                    double cr3 = tr2 - tr3;
                    double ci3 = ti2 - ti3;
                    double cr2 = tr1 - tr4;
                    double cr4 = tr1 + tr4;
                    double ci2 = ti1 + ti4;
                    double ci4 = ti1 - ti4;
                    int oidx1 = idx14 + idx1;
                    int oidx2 = idx14 + idx2;
                    int oidx3 = idx14 + idx3;
                    int oidx4 = idx14 + idx4;
                    out[oidx1 - 1] = tr2 + tr3;
                    out[oidx1] = ti2 + ti3;
                    out[oidx2 - 1] = w1r * cr2 - w1i * ci2;
                    out[oidx2] = w1r * ci2 + w1i * cr2;
                    out[oidx3 - 1] = w2r * cr3 - w2i * ci3;
                    out[oidx3] = w2r * ci3 + w2i * cr3;
                    out[oidx4 - 1] = w3r * cr4 - w3i * ci4;
                    out[oidx4] = w3r * ci4 + w3i * cr4;
                }
            }
            if (ido % 2 == 1) {
                return;
            }
        }
        for (k = 0; k < l1; ++k) {
            idx1 = k * ido;
            idx2 = 4 * idx1;
            idx3 = idx1 + idx0;
            idx4 = idx3 + idx0;
            idx5 = idx4 + idx0;
            idx6 = idx2 + ido;
            idx7 = idx6 + ido;
            idx8 = idx7 + ido;
            int idx9 = in_off + ido;
            int idx10 = out_off + ido;
            double i1r = in[idx9 - 1 + idx2];
            double i2r = in[idx9 - 1 + idx7];
            double i3r = in[in_off + idx6];
            double i4r = in[in_off + idx8];
            ti1 = i3r + i4r;
            ti2 = i4r - i3r;
            tr1 = i1r - i2r;
            tr2 = i1r + i2r;
            out[idx10 - 1 + idx1] = tr2 + tr2;
            out[idx10 - 1 + idx3] = 1.4142135623730951 * (tr1 - ti1);
            out[idx10 - 1 + idx4] = ti2 + ti2;
            out[idx10 - 1 + idx5] = -1.4142135623730951 * (tr1 + ti1);
        }
    }

    void radf5(int ido, int l1, double[] in, int in_off, double[] out, int out_off, int offset) {
        double cr2;
        double ci5;
        double ci4;
        double cr3;
        int idx7;
        int idx6;
        int idx9;
        int idx8;
        int k;
        int idx10;
        int idx5;
        int idx4;
        int idx1;
        int idx3;
        int idx2;
        double tr11 = 0.30901699437494745;
        double ti11 = 0.9510565162951535;
        double tr12 = -0.8090169943749473;
        double ti12 = 0.5877852522924732;
        int iw1 = offset;
        int iw2 = iw1 + ido;
        int iw3 = iw2 + ido;
        int iw4 = iw3 + ido;
        int idx0 = l1 * ido;
        for (k = 0; k < l1; ++k) {
            idx1 = k * ido;
            idx2 = 5 * idx1;
            idx3 = idx2 + ido;
            idx4 = idx3 + ido;
            idx5 = idx4 + ido;
            idx6 = idx5 + ido;
            idx7 = idx1 + idx0;
            idx8 = idx7 + idx0;
            idx9 = idx8 + idx0;
            idx10 = idx9 + idx0;
            int idx11 = out_off + ido - 1;
            double i1r = in[in_off + idx1];
            double i2r = in[in_off + idx7];
            double i3r = in[in_off + idx8];
            double i4r = in[in_off + idx9];
            double i5r = in[in_off + idx10];
            cr2 = i5r + i2r;
            ci5 = i5r - i2r;
            cr3 = i4r + i3r;
            ci4 = i4r - i3r;
            out[out_off + idx2] = i1r + cr2 + cr3;
            out[idx11 + idx3] = i1r + 0.30901699437494745 * cr2 + -0.8090169943749473 * cr3;
            out[out_off + idx4] = 0.9510565162951535 * ci5 + 0.5877852522924732 * ci4;
            out[idx11 + idx5] = i1r + -0.8090169943749473 * cr2 + 0.30901699437494745 * cr3;
            out[out_off + idx6] = 0.5877852522924732 * ci5 - 0.9510565162951535 * ci4;
        }
        if (ido == 1) {
            return;
        }
        for (k = 0; k < l1; ++k) {
            idx1 = k * ido;
            idx2 = 5 * idx1;
            idx3 = idx2 + ido;
            idx4 = idx3 + ido;
            idx5 = idx4 + ido;
            idx6 = idx5 + ido;
            idx7 = idx1 + idx0;
            idx8 = idx7 + idx0;
            idx9 = idx8 + idx0;
            idx10 = idx9 + idx0;
            for (int i = 2; i < ido; i += 2) {
                int widx1 = i - 1 + iw1;
                int widx2 = i - 1 + iw2;
                int widx3 = i - 1 + iw3;
                int widx4 = i - 1 + iw4;
                double w1r = this.wtable_r[widx1 - 1];
                double w1i = this.wtable_r[widx1];
                double w2r = this.wtable_r[widx2 - 1];
                double w2i = this.wtable_r[widx2];
                double w3r = this.wtable_r[widx3 - 1];
                double w3i = this.wtable_r[widx3];
                double w4r = this.wtable_r[widx4 - 1];
                double w4i = this.wtable_r[widx4];
                int ic = ido - i;
                int idx15 = in_off + i;
                int idx16 = out_off + i;
                int idx17 = out_off + ic;
                int iidx1 = idx15 + idx1;
                int iidx2 = idx15 + idx7;
                int iidx3 = idx15 + idx8;
                int iidx4 = idx15 + idx9;
                int iidx5 = idx15 + idx10;
                double i1i = in[iidx1 - 1];
                double i1r = in[iidx1];
                double i2i = in[iidx2 - 1];
                double i2r = in[iidx2];
                double i3i = in[iidx3 - 1];
                double i3r = in[iidx3];
                double i4i = in[iidx4 - 1];
                double i4r = in[iidx4];
                double i5i = in[iidx5 - 1];
                double i5r = in[iidx5];
                double dr2 = w1r * i2i + w1i * i2r;
                double di2 = w1r * i2r - w1i * i2i;
                double dr3 = w2r * i3i + w2i * i3r;
                double di3 = w2r * i3r - w2i * i3i;
                double dr4 = w3r * i4i + w3i * i4r;
                double di4 = w3r * i4r - w3i * i4i;
                double dr5 = w4r * i5i + w4i * i5r;
                double di5 = w4r * i5r - w4i * i5i;
                cr2 = dr2 + dr5;
                ci5 = dr5 - dr2;
                double cr5 = di2 - di5;
                double ci2 = di2 + di5;
                cr3 = dr3 + dr4;
                ci4 = dr4 - dr3;
                double cr4 = di3 - di4;
                double ci3 = di3 + di4;
                double tr2 = i1i + 0.30901699437494745 * cr2 + -0.8090169943749473 * cr3;
                double ti2 = i1r + 0.30901699437494745 * ci2 + -0.8090169943749473 * ci3;
                double tr3 = i1i + -0.8090169943749473 * cr2 + 0.30901699437494745 * cr3;
                double ti3 = i1r + -0.8090169943749473 * ci2 + 0.30901699437494745 * ci3;
                double tr5 = 0.9510565162951535 * cr5 + 0.5877852522924732 * cr4;
                double ti5 = 0.9510565162951535 * ci5 + 0.5877852522924732 * ci4;
                double tr4 = 0.5877852522924732 * cr5 - 0.9510565162951535 * cr4;
                double ti4 = 0.5877852522924732 * ci5 - 0.9510565162951535 * ci4;
                int oidx1 = idx16 + idx2;
                int oidx2 = idx17 + idx3;
                int oidx3 = idx16 + idx4;
                int oidx4 = idx17 + idx5;
                int oidx5 = idx16 + idx6;
                out[oidx1 - 1] = i1i + cr2 + cr3;
                out[oidx1] = i1r + ci2 + ci3;
                out[oidx3 - 1] = tr2 + tr5;
                out[oidx2 - 1] = tr2 - tr5;
                out[oidx3] = ti2 + ti5;
                out[oidx2] = ti5 - ti2;
                out[oidx5 - 1] = tr3 + tr4;
                out[oidx4 - 1] = tr3 - tr4;
                out[oidx5] = ti3 + ti4;
                out[oidx4] = ti4 - ti3;
            }
        }
    }

    void radb5(int ido, int l1, double[] in, int in_off, double[] out, int out_off, int offset) {
        double ti5;
        double cr2;
        double ci5;
        double ci4;
        double cr3;
        double tr3;
        int idx7;
        int idx6;
        int idx9;
        int idx8;
        int k;
        double tr2;
        int idx10;
        double ti4;
        int idx5;
        int idx4;
        int idx1;
        int idx3;
        int idx2;
        double tr11 = 0.30901699437494745;
        double ti11 = 0.9510565162951535;
        double tr12 = -0.8090169943749473;
        double ti12 = 0.5877852522924732;
        int iw1 = offset;
        int iw2 = iw1 + ido;
        int iw3 = iw2 + ido;
        int iw4 = iw3 + ido;
        int idx0 = l1 * ido;
        for (k = 0; k < l1; ++k) {
            idx1 = k * ido;
            idx2 = 5 * idx1;
            idx3 = idx2 + ido;
            idx4 = idx3 + ido;
            idx5 = idx4 + ido;
            idx6 = idx5 + ido;
            idx7 = idx1 + idx0;
            idx8 = idx7 + idx0;
            idx9 = idx8 + idx0;
            idx10 = idx9 + idx0;
            int idx11 = in_off + ido - 1;
            double i1r = in[in_off + idx2];
            ti5 = 2.0 * in[in_off + idx4];
            ti4 = 2.0 * in[in_off + idx6];
            tr2 = 2.0 * in[idx11 + idx3];
            tr3 = 2.0 * in[idx11 + idx5];
            cr2 = i1r + 0.30901699437494745 * tr2 + -0.8090169943749473 * tr3;
            cr3 = i1r + -0.8090169943749473 * tr2 + 0.30901699437494745 * tr3;
            ci5 = 0.9510565162951535 * ti5 + 0.5877852522924732 * ti4;
            ci4 = 0.5877852522924732 * ti5 - 0.9510565162951535 * ti4;
            out[out_off + idx1] = i1r + tr2 + tr3;
            out[out_off + idx7] = cr2 - ci5;
            out[out_off + idx8] = cr3 - ci4;
            out[out_off + idx9] = cr3 + ci4;
            out[out_off + idx10] = cr2 + ci5;
        }
        if (ido == 1) {
            return;
        }
        for (k = 0; k < l1; ++k) {
            idx1 = k * ido;
            idx2 = 5 * idx1;
            idx3 = idx2 + ido;
            idx4 = idx3 + ido;
            idx5 = idx4 + ido;
            idx6 = idx5 + ido;
            idx7 = idx1 + idx0;
            idx8 = idx7 + idx0;
            idx9 = idx8 + idx0;
            idx10 = idx9 + idx0;
            for (int i = 2; i < ido; i += 2) {
                int ic = ido - i;
                int widx1 = i - 1 + iw1;
                int widx2 = i - 1 + iw2;
                int widx3 = i - 1 + iw3;
                int widx4 = i - 1 + iw4;
                double w1r = this.wtable_r[widx1 - 1];
                double w1i = this.wtable_r[widx1];
                double w2r = this.wtable_r[widx2 - 1];
                double w2i = this.wtable_r[widx2];
                double w3r = this.wtable_r[widx3 - 1];
                double w3i = this.wtable_r[widx3];
                double w4r = this.wtable_r[widx4 - 1];
                double w4i = this.wtable_r[widx4];
                int idx15 = in_off + i;
                int idx16 = in_off + ic;
                int idx17 = out_off + i;
                int iidx1 = idx15 + idx2;
                int iidx2 = idx16 + idx3;
                int iidx3 = idx15 + idx4;
                int iidx4 = idx16 + idx5;
                int iidx5 = idx15 + idx6;
                double i1i = in[iidx1 - 1];
                double i1r = in[iidx1];
                double i2i = in[iidx2 - 1];
                double i2r = in[iidx2];
                double i3i = in[iidx3 - 1];
                double i3r = in[iidx3];
                double i4i = in[iidx4 - 1];
                double i4r = in[iidx4];
                double i5i = in[iidx5 - 1];
                double i5r = in[iidx5];
                ti5 = i3r + i2r;
                double ti2 = i3r - i2r;
                ti4 = i5r + i4r;
                double ti3 = i5r - i4r;
                double tr5 = i3i - i2i;
                tr2 = i3i + i2i;
                double tr4 = i5i - i4i;
                tr3 = i5i + i4i;
                cr2 = i1i + 0.30901699437494745 * tr2 + -0.8090169943749473 * tr3;
                double ci2 = i1r + 0.30901699437494745 * ti2 + -0.8090169943749473 * ti3;
                cr3 = i1i + -0.8090169943749473 * tr2 + 0.30901699437494745 * tr3;
                double ci3 = i1r + -0.8090169943749473 * ti2 + 0.30901699437494745 * ti3;
                double cr5 = 0.9510565162951535 * tr5 + 0.5877852522924732 * tr4;
                ci5 = 0.9510565162951535 * ti5 + 0.5877852522924732 * ti4;
                double cr4 = 0.5877852522924732 * tr5 - 0.9510565162951535 * tr4;
                ci4 = 0.5877852522924732 * ti5 - 0.9510565162951535 * ti4;
                double dr3 = cr3 - ci4;
                double dr4 = cr3 + ci4;
                double di3 = ci3 + cr4;
                double di4 = ci3 - cr4;
                double dr5 = cr2 + ci5;
                double dr2 = cr2 - ci5;
                double di5 = ci2 - cr5;
                double di2 = ci2 + cr5;
                int oidx1 = idx17 + idx1;
                int oidx2 = idx17 + idx7;
                int oidx3 = idx17 + idx8;
                int oidx4 = idx17 + idx9;
                int oidx5 = idx17 + idx10;
                out[oidx1 - 1] = i1i + tr2 + tr3;
                out[oidx1] = i1r + ti2 + ti3;
                out[oidx2 - 1] = w1r * dr2 - w1i * di2;
                out[oidx2] = w1r * di2 + w1i * dr2;
                out[oidx3 - 1] = w2r * dr3 - w2i * di3;
                out[oidx3] = w2r * di3 + w2i * dr3;
                out[oidx4 - 1] = w3r * dr4 - w3i * di4;
                out[oidx4] = w3r * di4 + w3i * dr4;
                out[oidx5 - 1] = w4r * dr5 - w4i * di5;
                out[oidx5] = w4r * di5 + w4i * dr5;
            }
        }
    }

    void radfg(int ido, int ip, int l1, int idl1, double[] in, int in_off, double[] out, int out_off, int offset) {
        int idx2;
        int k;
        int k2;
        int i;
        double o1i;
        int jc;
        int idx4;
        int oidx1;
        double o1r;
        int oidx12;
        int idx22;
        int idx3;
        int idx32;
        int idx42;
        int idx1;
        int iidx1;
        int j;
        int j2;
        int idx5;
        int idx6;
        int k3;
        int idx12;
        int idx13;
        int idx23;
        int i2;
        int j3;
        int iw1 = offset;
        double arg = 6.283185307179586 / (double)ip;
        double dcp = Math.cos(arg);
        double dsp = Math.sin(arg);
        int ipph = (ip + 1) / 2;
        int nbd = (ido - 1) / 2;
        if (ido != 1) {
            double o2i;
            int idij;
            int iidx2;
            int idx33;
            double o2r;
            int oidx13;
            int is;
            double o1i2;
            int oidx2;
            int iidx12;
            double w1i;
            double o1r2;
            double w1r;
            for (int ik = 0; ik < idl1; ++ik) {
                out[out_off + ik] = in[in_off + ik];
            }
            for (j3 = 1; j3 < ip; ++j3) {
                idx1 = j3 * l1 * ido;
                for (k3 = 0; k3 < l1; ++k3) {
                    idx23 = k3 * ido + idx1;
                    out[out_off + idx23] = in[in_off + idx23];
                }
            }
            if (nbd <= l1) {
                is = - ido;
                for (j3 = 1; j3 < ip; ++j3) {
                    idij = (is += ido) - 1;
                    idx1 = j3 * l1 * ido;
                    for (int i3 = 2; i3 < ido; i3 += 2) {
                        idx23 = (idij += 2) + iw1;
                        int idx43 = in_off + i3;
                        int idx52 = out_off + i3;
                        w1r = this.wtable_r[idx23 - 1];
                        w1i = this.wtable_r[idx23];
                        for (k2 = 0; k2 < l1; ++k2) {
                            idx33 = k2 * ido + idx1;
                            int oidx14 = idx52 + idx33;
                            iidx12 = idx43 + idx33;
                            double i1i = in[iidx12 - 1];
                            double i1r = in[iidx12];
                            out[oidx14 - 1] = w1r * i1i + w1i * i1r;
                            out[oidx14] = w1r * i1r - w1i * i1i;
                        }
                    }
                }
            } else {
                is = - ido;
                for (j3 = 1; j3 < ip; ++j3) {
                    is += ido;
                    idx1 = j3 * l1 * ido;
                    for (k3 = 0; k3 < l1; ++k3) {
                        idij = is - 1;
                        int idx34 = k3 * ido + idx1;
                        for (i = 2; i < ido; i += 2) {
                            int idx24 = (idij += 2) + iw1;
                            w1r = this.wtable_r[idx24 - 1];
                            w1i = this.wtable_r[idx24];
                            oidx1 = out_off + i + idx34;
                            int iidx13 = in_off + i + idx34;
                            double i1i = in[iidx13 - 1];
                            double i1r = in[iidx13];
                            out[oidx1 - 1] = w1r * i1i + w1i * i1r;
                            out[oidx1] = w1r * i1r - w1i * i1i;
                        }
                    }
                }
            }
            if (nbd >= l1) {
                for (j3 = 1; j3 < ipph; ++j3) {
                    jc = ip - j3;
                    idx1 = j3 * l1 * ido;
                    idx2 = jc * l1 * ido;
                    for (k = 0; k < l1; ++k) {
                        idx32 = k * ido + idx1;
                        idx42 = k * ido + idx2;
                        for (i2 = 2; i2 < ido; i2 += 2) {
                            int idx53 = in_off + i2;
                            int idx62 = out_off + i2;
                            iidx12 = idx53 + idx32;
                            iidx2 = idx53 + idx42;
                            oidx13 = idx62 + idx32;
                            oidx2 = idx62 + idx42;
                            o1i2 = out[oidx13 - 1];
                            o1r2 = out[oidx13];
                            o2i = out[oidx2 - 1];
                            o2r = out[oidx2];
                            in[iidx12 - 1] = o1i2 + o2i;
                            in[iidx12] = o1r2 + o2r;
                            in[iidx2 - 1] = o1r2 - o2r;
                            in[iidx2] = o2i - o1i2;
                        }
                    }
                }
            } else {
                for (j3 = 1; j3 < ipph; ++j3) {
                    jc = ip - j3;
                    idx1 = j3 * l1 * ido;
                    idx2 = jc * l1 * ido;
                    for (int i4 = 2; i4 < ido; i4 += 2) {
                        int idx54 = in_off + i4;
                        int idx63 = out_off + i4;
                        for (k2 = 0; k2 < l1; ++k2) {
                            idx33 = k2 * ido + idx1;
                            int idx44 = k2 * ido + idx2;
                            iidx12 = idx54 + idx33;
                            iidx2 = idx54 + idx44;
                            oidx13 = idx63 + idx33;
                            oidx2 = idx63 + idx44;
                            o1i2 = out[oidx13 - 1];
                            o1r2 = out[oidx13];
                            o2i = out[oidx2 - 1];
                            o2r = out[oidx2];
                            in[iidx12 - 1] = o1i2 + o2i;
                            in[iidx12] = o1r2 + o2r;
                            in[iidx2 - 1] = o1r2 - o2r;
                            in[iidx2] = o2i - o1i2;
                        }
                    }
                }
            }
        } else {
            System.arraycopy(out, out_off, in, in_off, idl1);
        }
        for (j3 = 1; j3 < ipph; ++j3) {
            jc = ip - j3;
            idx1 = j3 * l1 * ido;
            idx2 = jc * l1 * ido;
            for (k = 0; k < l1; ++k) {
                idx32 = k * ido + idx1;
                idx42 = k * ido + idx2;
                oidx1 = out_off + idx32;
                int oidx2 = out_off + idx42;
                double o1r3 = out[oidx1];
                double o2r = out[oidx2];
                in[in_off + idx32] = o1r3 + o2r;
                in[in_off + idx42] = o2r - o1r3;
            }
        }
        double ar1 = 1.0;
        double ai1 = 0.0;
        int idx0 = (ip - 1) * idl1;
        for (int l = 1; l < ipph; ++l) {
            int idx45;
            int lc = ip - l;
            double ar1h = dcp * ar1 - dsp * ai1;
            ai1 = dcp * ai1 + dsp * ar1;
            ar1 = ar1h;
            idx13 = l * idl1;
            idx23 = lc * idl1;
            for (int ik = 0; ik < idl1; ++ik) {
                idx3 = out_off + ik;
                idx45 = in_off + ik;
                out[idx3 + idx13] = in[idx45] + ar1 * in[idx45 + idl1];
                out[idx3 + idx23] = ai1 * in[idx45 + idx0];
            }
            double dc2 = ar1;
            double ds2 = ai1;
            double ar2 = ar1;
            double ai2 = ai1;
            for (int j4 = 2; j4 < ipph; ++j4) {
                jc = ip - j4;
                double ar2h = dc2 * ar2 - ds2 * ai2;
                ai2 = dc2 * ai2 + ds2 * ar2;
                ar2 = ar2h;
                idx3 = j4 * idl1;
                idx45 = jc * idl1;
                for (int ik = 0; ik < idl1; ++ik) {
                    idx5 = out_off + ik;
                    idx6 = in_off + ik;
                    double[] arrd = out;
                    int n = idx5 + idx13;
                    arrd[n] = arrd[n] + ar2 * in[idx6 + idx3];
                    double[] arrd2 = out;
                    int n2 = idx5 + idx23;
                    arrd2[n2] = arrd2[n2] + ai2 * in[idx6 + idx45];
                }
            }
        }
        for (int j5 = 1; j5 < ipph; ++j5) {
            idx13 = j5 * idl1;
            for (int ik = 0; ik < idl1; ++ik) {
                double[] arrd = out;
                int n = out_off + ik;
                arrd[n] = arrd[n] + in[in_off + ik + idx13];
            }
        }
        if (ido >= l1) {
            for (int k4 = 0; k4 < l1; ++k4) {
                idx13 = k4 * ido;
                idx23 = idx13 * ip;
                for (i = 0; i < ido; ++i) {
                    in[in_off + i + idx23] = out[out_off + i + idx13];
                }
            }
        } else {
            for (int i5 = 0; i5 < ido; ++i5) {
                for (k3 = 0; k3 < l1; ++k3) {
                    idx12 = k3 * ido;
                    in[in_off + i5 + idx12 * ip] = out[out_off + i5 + idx12];
                }
            }
        }
        int idx01 = ip * ido;
        for (j = 1; j < ipph; ++j) {
            jc = ip - j;
            j2 = 2 * j;
            idx12 = j * l1 * ido;
            idx22 = jc * l1 * ido;
            idx3 = j2 * ido;
            for (k2 = 0; k2 < l1; ++k2) {
                idx4 = k2 * ido;
                idx5 = idx4 + idx12;
                idx6 = idx4 + idx22;
                int idx7 = k2 * idx01;
                in[in_off + ido - 1 + idx3 - ido + idx7] = out[out_off + idx5];
                in[in_off + idx3 + idx7] = out[out_off + idx6];
            }
        }
        if (ido == 1) {
            return;
        }
        if (nbd >= l1) {
            for (j = 1; j < ipph; ++j) {
                jc = ip - j;
                j2 = 2 * j;
                idx12 = j * l1 * ido;
                idx22 = jc * l1 * ido;
                idx3 = j2 * ido;
                for (k2 = 0; k2 < l1; ++k2) {
                    idx4 = k2 * idx01;
                    idx5 = k2 * ido;
                    for (int i6 = 2; i6 < ido; i6 += 2) {
                        int ic = ido - i6;
                        int idx64 = in_off + i6;
                        int idx7 = in_off + ic;
                        int idx8 = out_off + i6;
                        iidx1 = idx64 + idx3 + idx4;
                        int iidx2 = idx7 + idx3 - ido + idx4;
                        oidx12 = idx8 + idx5 + idx12;
                        int oidx2 = idx8 + idx5 + idx22;
                        o1i = out[oidx12 - 1];
                        o1r = out[oidx12];
                        double o2i = out[oidx2 - 1];
                        double o2r = out[oidx2];
                        in[iidx1 - 1] = o1i + o2i;
                        in[iidx2 - 1] = o1i - o2i;
                        in[iidx1] = o1r + o2r;
                        in[iidx2] = o2r - o1r;
                    }
                }
            }
        } else {
            for (j = 1; j < ipph; ++j) {
                jc = ip - j;
                j2 = 2 * j;
                idx12 = j * l1 * ido;
                idx22 = jc * l1 * ido;
                idx3 = j2 * ido;
                for (i2 = 2; i2 < ido; i2 += 2) {
                    int ic = ido - i2;
                    int idx65 = in_off + i2;
                    int idx7 = in_off + ic;
                    int idx8 = out_off + i2;
                    for (int k5 = 0; k5 < l1; ++k5) {
                        int idx46 = k5 * idx01;
                        int idx55 = k5 * ido;
                        iidx1 = idx65 + idx3 + idx46;
                        int iidx2 = idx7 + idx3 - ido + idx46;
                        oidx12 = idx8 + idx55 + idx12;
                        int oidx2 = idx8 + idx55 + idx22;
                        o1i = out[oidx12 - 1];
                        o1r = out[oidx12];
                        double o2i = out[oidx2 - 1];
                        double o2r = out[oidx2];
                        in[iidx1 - 1] = o1i + o2i;
                        in[iidx2 - 1] = o1i - o2i;
                        in[iidx1] = o1r + o2r;
                        in[iidx2] = o2r - o1r;
                    }
                }
            }
        }
    }

    void radbg(int ido, int ip, int l1, int idl1, double[] in, int in_off, double[] out, int out_off, int offset) {
        int idx2;
        int oidx2;
        double i2i;
        double i1i;
        int idx3;
        int idx1;
        double i1r;
        int iidx2;
        int i;
        int idx32;
        int idx4;
        int idx5;
        double i2r;
        int k;
        int iidx1;
        int idx52;
        int oidx1;
        int iidx22;
        int idx42;
        int iidx12;
        int idx22;
        int k2;
        int idx33;
        int iidx13;
        int j;
        int idx23;
        int j2;
        int oidx12;
        int jc;
        double i1r2;
        int k3;
        double i2r2;
        int idx12;
        double o1i;
        double o1r;
        int idx24;
        int idx13;
        int iw1 = offset;
        double arg = 6.283185307179586 / (double)ip;
        double dcp = Math.cos(arg);
        double dsp = Math.sin(arg);
        int nbd = (ido - 1) / 2;
        int ipph = (ip + 1) / 2;
        int idx0 = ip * ido;
        if (ido >= l1) {
            for (int k4 = 0; k4 < l1; ++k4) {
                idx13 = k4 * ido;
                idx24 = k4 * idx0;
                for (int i2 = 0; i2 < ido; ++i2) {
                    out[out_off + i2 + idx13] = in[in_off + i2 + idx24];
                }
            }
        } else {
            for (int i3 = 0; i3 < ido; ++i3) {
                idx13 = out_off + i3;
                idx24 = in_off + i3;
                for (int k5 = 0; k5 < l1; ++k5) {
                    out[idx13 + k5 * ido] = in[idx24 + k5 * idx0];
                }
            }
        }
        int iidx0 = in_off + ido - 1;
        for (j = 1; j < ipph; ++j) {
            jc = ip - j;
            int j22 = 2 * j;
            idx12 = j * l1 * ido;
            idx23 = jc * l1 * ido;
            idx3 = j22 * ido;
            for (k = 0; k < l1; ++k) {
                idx42 = k * ido;
                idx52 = idx42 * ip;
                iidx12 = iidx0 + idx3 + idx52 - ido;
                iidx22 = in_off + idx3 + idx52;
                i1r = in[iidx12];
                i2r = in[iidx22];
                out[out_off + idx42 + idx12] = i1r + i1r;
                out[out_off + idx42 + idx23] = i2r + i2r;
            }
        }
        if (ido != 1) {
            int oidx13;
            double a2i;
            double a2r;
            int iidx14;
            int ic;
            int iidx23;
            int oidx22;
            double a1r;
            double a1i;
            if (nbd >= l1) {
                for (j = 1; j < ipph; ++j) {
                    jc = ip - j;
                    idx12 = j * l1 * ido;
                    idx23 = jc * l1 * ido;
                    idx3 = 2 * j * ido;
                    for (k = 0; k < l1; ++k) {
                        idx42 = k * ido + idx12;
                        idx52 = k * ido + idx23;
                        int idx6 = k * ip * ido + idx3;
                        for (int i4 = 2; i4 < ido; i4 += 2) {
                            ic = ido - i4;
                            int idx7 = out_off + i4;
                            int idx8 = in_off + ic;
                            int idx9 = in_off + i4;
                            oidx13 = idx7 + idx42;
                            oidx22 = idx7 + idx52;
                            iidx14 = idx9 + idx6;
                            iidx23 = idx8 + idx6 - ido;
                            a1i = in[iidx14 - 1];
                            a1r = in[iidx14];
                            a2i = in[iidx23 - 1];
                            a2r = in[iidx23];
                            out[oidx13 - 1] = a1i + a2i;
                            out[oidx22 - 1] = a1i - a2i;
                            out[oidx13] = a1r - a2r;
                            out[oidx22] = a1r + a2r;
                        }
                    }
                }
            } else {
                for (j = 1; j < ipph; ++j) {
                    jc = ip - j;
                    idx12 = j * l1 * ido;
                    idx23 = jc * l1 * ido;
                    idx3 = 2 * j * ido;
                    for (i = 2; i < ido; i += 2) {
                        ic = ido - i;
                        int idx7 = out_off + i;
                        int idx8 = in_off + ic;
                        int idx9 = in_off + i;
                        for (int k6 = 0; k6 < l1; ++k6) {
                            int idx43 = k6 * ido + idx12;
                            int idx53 = k6 * ido + idx23;
                            int idx6 = k6 * ip * ido + idx3;
                            oidx13 = idx7 + idx43;
                            oidx22 = idx7 + idx53;
                            iidx14 = idx9 + idx6;
                            iidx23 = idx8 + idx6 - ido;
                            a1i = in[iidx14 - 1];
                            a1r = in[iidx14];
                            a2i = in[iidx23 - 1];
                            a2r = in[iidx23];
                            out[oidx13 - 1] = a1i + a2i;
                            out[oidx22 - 1] = a1i - a2i;
                            out[oidx13] = a1r - a2r;
                            out[oidx22] = a1r + a2r;
                        }
                    }
                }
            }
        }
        double ar1 = 1.0;
        double ai1 = 0.0;
        int idx01 = (ip - 1) * idl1;
        for (int l = 1; l < ipph; ++l) {
            int lc = ip - l;
            double ar1h = dcp * ar1 - dsp * ai1;
            ai1 = dcp * ai1 + dsp * ar1;
            ar1 = ar1h;
            idx1 = l * idl1;
            idx22 = lc * idl1;
            for (int ik = 0; ik < idl1; ++ik) {
                idx33 = in_off + ik;
                int idx44 = out_off + ik;
                in[idx33 + idx1] = out[idx44] + ar1 * out[idx44 + idl1];
                in[idx33 + idx22] = ai1 * out[idx44 + idx01];
            }
            double dc2 = ar1;
            double ds2 = ai1;
            double ar2 = ar1;
            double ai2 = ai1;
            for (int j3 = 2; j3 < ipph; ++j3) {
                jc = ip - j3;
                double ar2h = dc2 * ar2 - ds2 * ai2;
                ai2 = dc2 * ai2 + ds2 * ar2;
                ar2 = ar2h;
                int idx54 = j3 * idl1;
                int idx6 = jc * idl1;
                for (int ik = 0; ik < idl1; ++ik) {
                    int idx7 = in_off + ik;
                    int idx8 = out_off + ik;
                    double[] arrd = in;
                    int n = idx7 + idx1;
                    arrd[n] = arrd[n] + ar2 * out[idx8 + idx54];
                    double[] arrd2 = in;
                    int n2 = idx7 + idx22;
                    arrd2[n2] = arrd2[n2] + ai2 * out[idx8 + idx6];
                }
            }
        }
        for (j2 = 1; j2 < ipph; ++j2) {
            idx1 = j2 * idl1;
            for (int ik = 0; ik < idl1; ++ik) {
                idx2 = out_off + ik;
                double[] arrd = out;
                int n = idx2;
                arrd[n] = arrd[n] + out[idx2 + idx1];
            }
        }
        for (j2 = 1; j2 < ipph; ++j2) {
            jc = ip - j2;
            idx1 = j2 * l1 * ido;
            idx22 = jc * l1 * ido;
            for (k = 0; k < l1; ++k) {
                idx33 = k * ido;
                int oidx14 = out_off + idx33;
                iidx12 = in_off + idx33 + idx1;
                iidx22 = in_off + idx33 + idx22;
                i1r = in[iidx12];
                i2r = in[iidx22];
                out[oidx14 + idx1] = i1r - i2r;
                out[oidx14 + idx22] = i1r + i2r;
            }
        }
        if (ido == 1) {
            return;
        }
        if (nbd >= l1) {
            for (j2 = 1; j2 < ipph; ++j2) {
                jc = ip - j2;
                idx1 = j2 * l1 * ido;
                idx22 = jc * l1 * ido;
                for (k = 0; k < l1; ++k) {
                    idx33 = k * ido;
                    for (int i5 = 2; i5 < ido; i5 += 2) {
                        idx4 = out_off + i5;
                        idx5 = in_off + i5;
                        oidx1 = idx4 + idx33 + idx1;
                        oidx2 = idx4 + idx33 + idx22;
                        iidx1 = idx5 + idx33 + idx1;
                        iidx2 = idx5 + idx33 + idx22;
                        i1i = in[iidx1 - 1];
                        i1r2 = in[iidx1];
                        i2i = in[iidx2 - 1];
                        i2r2 = in[iidx2];
                        out[oidx1 - 1] = i1i - i2r2;
                        out[oidx2 - 1] = i1i + i2r2;
                        out[oidx1] = i1r2 + i2i;
                        out[oidx2] = i1r2 - i2i;
                    }
                }
            }
        } else {
            for (j2 = 1; j2 < ipph; ++j2) {
                jc = ip - j2;
                idx1 = j2 * l1 * ido;
                idx22 = jc * l1 * ido;
                for (i = 2; i < ido; i += 2) {
                    idx42 = out_off + i;
                    idx52 = in_off + i;
                    for (k3 = 0; k3 < l1; ++k3) {
                        idx32 = k3 * ido;
                        oidx1 = idx42 + idx32 + idx1;
                        oidx2 = idx42 + idx32 + idx22;
                        iidx1 = idx52 + idx32 + idx1;
                        iidx2 = idx52 + idx32 + idx22;
                        i1i = in[iidx1 - 1];
                        i1r2 = in[iidx1];
                        i2i = in[iidx2 - 1];
                        i2r2 = in[iidx2];
                        out[oidx1 - 1] = i1i - i2r2;
                        out[oidx2 - 1] = i1i + i2r2;
                        out[oidx1] = i1r2 + i2i;
                        out[oidx2] = i1r2 - i2i;
                    }
                }
            }
        }
        System.arraycopy(out, out_off, in, in_off, idl1);
        for (j2 = 1; j2 < ip; ++j2) {
            idx1 = j2 * l1 * ido;
            for (k2 = 0; k2 < l1; ++k2) {
                idx2 = k2 * ido + idx1;
                in[in_off + idx2] = out[out_off + idx2];
            }
        }
        if (nbd <= l1) {
            int is = - ido;
            for (j2 = 1; j2 < ip; ++j2) {
                int idij = (is += ido) - 1;
                idx1 = j2 * l1 * ido;
                for (int i6 = 2; i6 < ido; i6 += 2) {
                    idx2 = (idij += 2) + iw1;
                    double w1r = this.wtable_r[idx2 - 1];
                    double w1i = this.wtable_r[idx2];
                    idx42 = in_off + i6;
                    idx52 = out_off + i6;
                    for (k3 = 0; k3 < l1; ++k3) {
                        idx32 = k3 * ido + idx1;
                        iidx13 = idx42 + idx32;
                        oidx12 = idx52 + idx32;
                        o1i = out[oidx12 - 1];
                        o1r = out[oidx12];
                        in[iidx13 - 1] = w1r * o1i - w1i * o1r;
                        in[iidx13] = w1r * o1r + w1i * o1i;
                    }
                }
            }
        } else {
            int is = - ido;
            for (j2 = 1; j2 < ip; ++j2) {
                is += ido;
                idx1 = j2 * l1 * ido;
                for (k2 = 0; k2 < l1; ++k2) {
                    int idij = is - 1;
                    int idx34 = k2 * ido + idx1;
                    for (int i7 = 2; i7 < ido; i7 += 2) {
                        int idx25 = (idij += 2) + iw1;
                        double w1r = this.wtable_r[idx25 - 1];
                        double w1i = this.wtable_r[idx25];
                        idx4 = in_off + i7;
                        idx5 = out_off + i7;
                        iidx13 = idx4 + idx34;
                        oidx12 = idx5 + idx34;
                        o1i = out[oidx12 - 1];
                        o1r = out[oidx12];
                        in[iidx13 - 1] = w1r * o1i - w1i * o1r;
                        in[iidx13] = w1r * o1r + w1i * o1i;
                    }
                }
            }
        }
    }

    void cfftf(double[] a, int offa, int isign) {
        int[] nac = new int[1];
        int twon = 2 * this.n;
        double[] ch = new double[twon];
        int iw1 = twon;
        int iw2 = 4 * this.n;
        nac[0] = 0;
        int nf = (int)this.wtable[1 + iw2];
        int na = 0;
        int l1 = 1;
        int iw = iw1;
        for (int k1 = 2; k1 <= nf + 1; ++k1) {
            int ip = (int)this.wtable[k1 + iw2];
            int l2 = ip * l1;
            int ido = this.n / l2;
            int idot = ido + ido;
            int idl1 = idot * l1;
            switch (ip) {
                case 4: {
                    if (na == 0) {
                        this.passf4(idot, l1, a, offa, ch, 0, iw, isign);
                    } else {
                        this.passf4(idot, l1, ch, 0, a, offa, iw, isign);
                    }
                    na = 1 - na;
                    break;
                }
                case 2: {
                    if (na == 0) {
                        this.passf2(idot, l1, a, offa, ch, 0, iw, isign);
                    } else {
                        this.passf2(idot, l1, ch, 0, a, offa, iw, isign);
                    }
                    na = 1 - na;
                    break;
                }
                case 3: {
                    if (na == 0) {
                        this.passf3(idot, l1, a, offa, ch, 0, iw, isign);
                    } else {
                        this.passf3(idot, l1, ch, 0, a, offa, iw, isign);
                    }
                    na = 1 - na;
                    break;
                }
                case 5: {
                    if (na == 0) {
                        this.passf5(idot, l1, a, offa, ch, 0, iw, isign);
                    } else {
                        this.passf5(idot, l1, ch, 0, a, offa, iw, isign);
                    }
                    na = 1 - na;
                    break;
                }
                default: {
                    if (na == 0) {
                        this.passfg(nac, idot, ip, l1, idl1, a, offa, ch, 0, iw, isign);
                    } else {
                        this.passfg(nac, idot, ip, l1, idl1, ch, 0, a, offa, iw, isign);
                    }
                    if (nac[0] == 0) break;
                    na = 1 - na;
                }
            }
            l1 = l2;
            iw += (ip - 1) * idot;
        }
        if (na == 0) {
            return;
        }
        System.arraycopy(ch, 0, a, offa, twon);
    }

    void passf2(int ido, int l1, double[] in, int in_off, double[] out, int out_off, int offset, int isign) {
        int iw1 = offset;
        int idx = ido * l1;
        if (ido <= 2) {
            for (int k = 0; k < l1; ++k) {
                int idx0 = k * ido;
                int iidx1 = in_off + 2 * idx0;
                int iidx2 = iidx1 + ido;
                double a1r = in[iidx1];
                double a1i = in[iidx1 + 1];
                double a2r = in[iidx2];
                double a2i = in[iidx2 + 1];
                int oidx1 = out_off + idx0;
                int oidx2 = oidx1 + idx;
                out[oidx1] = a1r + a2r;
                out[oidx1 + 1] = a1i + a2i;
                out[oidx2] = a1r - a2r;
                out[oidx2 + 1] = a1i - a2i;
            }
        } else {
            for (int k = 0; k < l1; ++k) {
                for (int i = 0; i < ido - 1; i += 2) {
                    int idx0 = k * ido;
                    int iidx1 = in_off + i + 2 * idx0;
                    int iidx2 = iidx1 + ido;
                    double i1r = in[iidx1];
                    double i1i = in[iidx1 + 1];
                    double i2r = in[iidx2];
                    double i2i = in[iidx2 + 1];
                    int widx1 = i + iw1;
                    double w1r = this.wtable[widx1];
                    double w1i = (double)isign * this.wtable[widx1 + 1];
                    double t1r = i1r - i2r;
                    double t1i = i1i - i2i;
                    int oidx1 = out_off + i + idx0;
                    int oidx2 = oidx1 + idx;
                    out[oidx1] = i1r + i2r;
                    out[oidx1 + 1] = i1i + i2i;
                    out[oidx2] = w1r * t1r - w1i * t1i;
                    out[oidx2 + 1] = w1r * t1i + w1i * t1r;
                }
            }
        }
    }

    void passf3(int ido, int l1, double[] in, int in_off, double[] out, int out_off, int offset, int isign) {
        double taur = -0.5;
        double taui = 0.8660254037844387;
        int iw1 = offset;
        int iw2 = iw1 + ido;
        int idxt = l1 * ido;
        if (ido == 2) {
            for (int k = 1; k <= l1; ++k) {
                int iidx1 = in_off + (3 * k - 2) * ido;
                int iidx2 = iidx1 + ido;
                int iidx3 = iidx1 - ido;
                double i1r = in[iidx1];
                double i1i = in[iidx1 + 1];
                double i2r = in[iidx2];
                double i2i = in[iidx2 + 1];
                double i3r = in[iidx3];
                double i3i = in[iidx3 + 1];
                double tr2 = i1r + i2r;
                double cr2 = i3r + -0.5 * tr2;
                double ti2 = i1i + i2i;
                double ci2 = i3i + -0.5 * ti2;
                double cr3 = (double)isign * 0.8660254037844387 * (i1r - i2r);
                double ci3 = (double)isign * 0.8660254037844387 * (i1i - i2i);
                int oidx1 = out_off + (k - 1) * ido;
                int oidx2 = oidx1 + idxt;
                int oidx3 = oidx2 + idxt;
                out[oidx1] = in[iidx3] + tr2;
                out[oidx1 + 1] = i3i + ti2;
                out[oidx2] = cr2 - ci3;
                out[oidx2 + 1] = ci2 + cr3;
                out[oidx3] = cr2 + ci3;
                out[oidx3 + 1] = ci2 - cr3;
            }
        } else {
            for (int k = 1; k <= l1; ++k) {
                int idx1 = in_off + (3 * k - 2) * ido;
                int idx2 = out_off + (k - 1) * ido;
                for (int i = 0; i < ido - 1; i += 2) {
                    int iidx1 = i + idx1;
                    int iidx2 = iidx1 + ido;
                    int iidx3 = iidx1 - ido;
                    double a1r = in[iidx1];
                    double a1i = in[iidx1 + 1];
                    double a2r = in[iidx2];
                    double a2i = in[iidx2 + 1];
                    double a3r = in[iidx3];
                    double a3i = in[iidx3 + 1];
                    double tr2 = a1r + a2r;
                    double cr2 = a3r + -0.5 * tr2;
                    double ti2 = a1i + a2i;
                    double ci2 = a3i + -0.5 * ti2;
                    double cr3 = (double)isign * 0.8660254037844387 * (a1r - a2r);
                    double ci3 = (double)isign * 0.8660254037844387 * (a1i - a2i);
                    double dr2 = cr2 - ci3;
                    double dr3 = cr2 + ci3;
                    double di2 = ci2 + cr3;
                    double di3 = ci2 - cr3;
                    int widx1 = i + iw1;
                    int widx2 = i + iw2;
                    double w1r = this.wtable[widx1];
                    double w1i = (double)isign * this.wtable[widx1 + 1];
                    double w2r = this.wtable[widx2];
                    double w2i = (double)isign * this.wtable[widx2 + 1];
                    int oidx1 = i + idx2;
                    int oidx2 = oidx1 + idxt;
                    int oidx3 = oidx2 + idxt;
                    out[oidx1] = a3r + tr2;
                    out[oidx1 + 1] = a3i + ti2;
                    out[oidx2] = w1r * dr2 - w1i * di2;
                    out[oidx2 + 1] = w1r * di2 + w1i * dr2;
                    out[oidx3] = w2r * dr3 - w2i * di3;
                    out[oidx3 + 1] = w2r * di3 + w2i * dr3;
                }
            }
        }
    }

    void passf4(int ido, int l1, double[] in, int in_off, double[] out, int out_off, int offset, int isign) {
        int iw1 = offset;
        int iw2 = iw1 + ido;
        int iw3 = iw2 + ido;
        int idx0 = l1 * ido;
        if (ido == 2) {
            for (int k = 0; k < l1; ++k) {
                int idxt1 = k * ido;
                int iidx1 = in_off + 4 * idxt1 + 1;
                int iidx2 = iidx1 + ido;
                int iidx3 = iidx2 + ido;
                int iidx4 = iidx3 + ido;
                double i1i = in[iidx1 - 1];
                double i1r = in[iidx1];
                double i2i = in[iidx2 - 1];
                double i2r = in[iidx2];
                double i3i = in[iidx3 - 1];
                double i3r = in[iidx3];
                double i4i = in[iidx4 - 1];
                double i4r = in[iidx4];
                double ti1 = i1r - i3r;
                double ti2 = i1r + i3r;
                double tr4 = i4r - i2r;
                double ti3 = i2r + i4r;
                double tr1 = i1i - i3i;
                double tr2 = i1i + i3i;
                double ti4 = i2i - i4i;
                double tr3 = i2i + i4i;
                int oidx1 = out_off + idxt1;
                int oidx2 = oidx1 + idx0;
                int oidx3 = oidx2 + idx0;
                int oidx4 = oidx3 + idx0;
                out[oidx1] = tr2 + tr3;
                out[oidx1 + 1] = ti2 + ti3;
                out[oidx2] = tr1 + (double)isign * tr4;
                out[oidx2 + 1] = ti1 + (double)isign * ti4;
                out[oidx3] = tr2 - tr3;
                out[oidx3 + 1] = ti2 - ti3;
                out[oidx4] = tr1 - (double)isign * tr4;
                out[oidx4 + 1] = ti1 - (double)isign * ti4;
            }
        } else {
            for (int k = 0; k < l1; ++k) {
                int idx1 = k * ido;
                int idx2 = in_off + 1 + 4 * idx1;
                for (int i = 0; i < ido - 1; i += 2) {
                    int iidx1 = i + idx2;
                    int iidx2 = iidx1 + ido;
                    int iidx3 = iidx2 + ido;
                    int iidx4 = iidx3 + ido;
                    double i1i = in[iidx1 - 1];
                    double i1r = in[iidx1];
                    double i2i = in[iidx2 - 1];
                    double i2r = in[iidx2];
                    double i3i = in[iidx3 - 1];
                    double i3r = in[iidx3];
                    double i4i = in[iidx4 - 1];
                    double i4r = in[iidx4];
                    double ti1 = i1r - i3r;
                    double ti2 = i1r + i3r;
                    double ti3 = i2r + i4r;
                    double tr4 = i4r - i2r;
                    double tr1 = i1i - i3i;
                    double tr2 = i1i + i3i;
                    double ti4 = i2i - i4i;
                    double tr3 = i2i + i4i;
                    double cr3 = tr2 - tr3;
                    double ci3 = ti2 - ti3;
                    double cr2 = tr1 + (double)isign * tr4;
                    double cr4 = tr1 - (double)isign * tr4;
                    double ci2 = ti1 + (double)isign * ti4;
                    double ci4 = ti1 - (double)isign * ti4;
                    int widx1 = i + iw1;
                    int widx2 = i + iw2;
                    int widx3 = i + iw3;
                    double w1r = this.wtable[widx1];
                    double w1i = (double)isign * this.wtable[widx1 + 1];
                    double w2r = this.wtable[widx2];
                    double w2i = (double)isign * this.wtable[widx2 + 1];
                    double w3r = this.wtable[widx3];
                    double w3i = (double)isign * this.wtable[widx3 + 1];
                    int oidx1 = out_off + i + idx1;
                    int oidx2 = oidx1 + idx0;
                    int oidx3 = oidx2 + idx0;
                    int oidx4 = oidx3 + idx0;
                    out[oidx1] = tr2 + tr3;
                    out[oidx1 + 1] = ti2 + ti3;
                    out[oidx2] = w1r * cr2 - w1i * ci2;
                    out[oidx2 + 1] = w1r * ci2 + w1i * cr2;
                    out[oidx3] = w2r * cr3 - w2i * ci3;
                    out[oidx3 + 1] = w2r * ci3 + w2i * cr3;
                    out[oidx4] = w3r * cr4 - w3i * ci4;
                    out[oidx4 + 1] = w3r * ci4 + w3i * cr4;
                }
            }
        }
    }

    void passf5(int ido, int l1, double[] in, int in_off, double[] out, int out_off, int offset, int isign) {
        double tr11 = 0.30901699437494745;
        double ti11 = 0.9510565162951535;
        double tr12 = -0.8090169943749473;
        double ti12 = 0.5877852522924732;
        int iw1 = offset;
        int iw2 = iw1 + ido;
        int iw3 = iw2 + ido;
        int iw4 = iw3 + ido;
        int idx0 = l1 * ido;
        if (ido == 2) {
            for (int k = 1; k <= l1; ++k) {
                int iidx1 = in_off + (5 * k - 4) * ido + 1;
                int iidx2 = iidx1 + ido;
                int iidx3 = iidx1 - ido;
                int iidx4 = iidx2 + ido;
                int iidx5 = iidx4 + ido;
                double i1i = in[iidx1 - 1];
                double i1r = in[iidx1];
                double i2i = in[iidx2 - 1];
                double i2r = in[iidx2];
                double i3i = in[iidx3 - 1];
                double i3r = in[iidx3];
                double i4i = in[iidx4 - 1];
                double i4r = in[iidx4];
                double i5i = in[iidx5 - 1];
                double i5r = in[iidx5];
                double ti5 = i1r - i5r;
                double ti2 = i1r + i5r;
                double ti4 = i2r - i4r;
                double ti3 = i2r + i4r;
                double tr5 = i1i - i5i;
                double tr2 = i1i + i5i;
                double tr4 = i2i - i4i;
                double tr3 = i2i + i4i;
                double cr2 = i3i + 0.30901699437494745 * tr2 + -0.8090169943749473 * tr3;
                double ci2 = i3r + 0.30901699437494745 * ti2 + -0.8090169943749473 * ti3;
                double cr3 = i3i + -0.8090169943749473 * tr2 + 0.30901699437494745 * tr3;
                double ci3 = i3r + -0.8090169943749473 * ti2 + 0.30901699437494745 * ti3;
                double cr5 = (double)isign * (0.9510565162951535 * tr5 + 0.5877852522924732 * tr4);
                double ci5 = (double)isign * (0.9510565162951535 * ti5 + 0.5877852522924732 * ti4);
                double cr4 = (double)isign * (0.5877852522924732 * tr5 - 0.9510565162951535 * tr4);
                double ci4 = (double)isign * (0.5877852522924732 * ti5 - 0.9510565162951535 * ti4);
                int oidx1 = out_off + (k - 1) * ido;
                int oidx2 = oidx1 + idx0;
                int oidx3 = oidx2 + idx0;
                int oidx4 = oidx3 + idx0;
                int oidx5 = oidx4 + idx0;
                out[oidx1] = i3i + tr2 + tr3;
                out[oidx1 + 1] = i3r + ti2 + ti3;
                out[oidx2] = cr2 - ci5;
                out[oidx2 + 1] = ci2 + cr5;
                out[oidx3] = cr3 - ci4;
                out[oidx3 + 1] = ci3 + cr4;
                out[oidx4] = cr3 + ci4;
                out[oidx4 + 1] = ci3 - cr4;
                out[oidx5] = cr2 + ci5;
                out[oidx5 + 1] = ci2 - cr5;
            }
        } else {
            for (int k = 1; k <= l1; ++k) {
                int idx1 = in_off + 1 + (k * 5 - 4) * ido;
                int idx2 = out_off + (k - 1) * ido;
                for (int i = 0; i < ido - 1; i += 2) {
                    int iidx1 = i + idx1;
                    int iidx2 = iidx1 + ido;
                    int iidx3 = iidx1 - ido;
                    int iidx4 = iidx2 + ido;
                    int iidx5 = iidx4 + ido;
                    double i1i = in[iidx1 - 1];
                    double i1r = in[iidx1];
                    double i2i = in[iidx2 - 1];
                    double i2r = in[iidx2];
                    double i3i = in[iidx3 - 1];
                    double i3r = in[iidx3];
                    double i4i = in[iidx4 - 1];
                    double i4r = in[iidx4];
                    double i5i = in[iidx5 - 1];
                    double i5r = in[iidx5];
                    double ti5 = i1r - i5r;
                    double ti2 = i1r + i5r;
                    double ti4 = i2r - i4r;
                    double ti3 = i2r + i4r;
                    double tr5 = i1i - i5i;
                    double tr2 = i1i + i5i;
                    double tr4 = i2i - i4i;
                    double tr3 = i2i + i4i;
                    double cr2 = i3i + 0.30901699437494745 * tr2 + -0.8090169943749473 * tr3;
                    double ci2 = i3r + 0.30901699437494745 * ti2 + -0.8090169943749473 * ti3;
                    double cr3 = i3i + -0.8090169943749473 * tr2 + 0.30901699437494745 * tr3;
                    double ci3 = i3r + -0.8090169943749473 * ti2 + 0.30901699437494745 * ti3;
                    double cr5 = (double)isign * (0.9510565162951535 * tr5 + 0.5877852522924732 * tr4);
                    double ci5 = (double)isign * (0.9510565162951535 * ti5 + 0.5877852522924732 * ti4);
                    double cr4 = (double)isign * (0.5877852522924732 * tr5 - 0.9510565162951535 * tr4);
                    double ci4 = (double)isign * (0.5877852522924732 * ti5 - 0.9510565162951535 * ti4);
                    double dr3 = cr3 - ci4;
                    double dr4 = cr3 + ci4;
                    double di3 = ci3 + cr4;
                    double di4 = ci3 - cr4;
                    double dr5 = cr2 + ci5;
                    double dr2 = cr2 - ci5;
                    double di5 = ci2 - cr5;
                    double di2 = ci2 + cr5;
                    int widx1 = i + iw1;
                    int widx2 = i + iw2;
                    int widx3 = i + iw3;
                    int widx4 = i + iw4;
                    double w1r = this.wtable[widx1];
                    double w1i = (double)isign * this.wtable[widx1 + 1];
                    double w2r = this.wtable[widx2];
                    double w2i = (double)isign * this.wtable[widx2 + 1];
                    double w3r = this.wtable[widx3];
                    double w3i = (double)isign * this.wtable[widx3 + 1];
                    double w4r = this.wtable[widx4];
                    double w4i = (double)isign * this.wtable[widx4 + 1];
                    int oidx1 = i + idx2;
                    int oidx2 = oidx1 + idx0;
                    int oidx3 = oidx2 + idx0;
                    int oidx4 = oidx3 + idx0;
                    int oidx5 = oidx4 + idx0;
                    out[oidx1] = i3i + tr2 + tr3;
                    out[oidx1 + 1] = i3r + ti2 + ti3;
                    out[oidx2] = w1r * dr2 - w1i * di2;
                    out[oidx2 + 1] = w1r * di2 + w1i * dr2;
                    out[oidx3] = w2r * dr3 - w2i * di3;
                    out[oidx3 + 1] = w2r * di3 + w2i * dr3;
                    out[oidx4] = w3r * dr4 - w3i * di4;
                    out[oidx4 + 1] = w3r * di4 + w3i * dr4;
                    out[oidx5] = w4r * dr5 - w4i * di5;
                    out[oidx5 + 1] = w4r * di5 + w4i * dr5;
                }
            }
        }
    }

    void passfg(int[] nac, int ido, int ip, int l1, int idl1, double[] in, int in_off, double[] out, int out_off, int offset, int isign) {
        int idx3;
        int idx1;
        int idx12;
        double w1r;
        int j;
        int jc;
        double w1i;
        int ik;
        int oidx1;
        int j2;
        int iidx1;
        int j3;
        int idx2;
        int idxt1;
        int idxt2;
        int k;
        int idxt3;
        double i1r;
        int iw1 = offset;
        int idot = ido / 2;
        int ipph = (ip + 1) / 2;
        int idp = ip * ido;
        if (ido >= l1) {
            for (j = 1; j < ipph; ++j) {
                jc = ip - j;
                int idx13 = j * ido;
                int idx22 = jc * ido;
                for (int k2 = 0; k2 < l1; ++k2) {
                    int idx32 = k2 * ido;
                    int idx4 = idx32 + idx13 * l1;
                    int idx5 = idx32 + idx22 * l1;
                    int idx6 = idx32 * ip;
                    for (int i = 0; i < ido; ++i) {
                        oidx1 = out_off + i;
                        double i1r2 = in[in_off + i + idx13 + idx6];
                        double i2r = in[in_off + i + idx22 + idx6];
                        out[oidx1 + idx4] = i1r2 + i2r;
                        out[oidx1 + idx5] = i1r2 - i2r;
                    }
                }
            }
            for (int k3 = 0; k3 < l1; ++k3) {
                idxt1 = k3 * ido;
                idxt2 = idxt1 * ip;
                for (int i = 0; i < ido; ++i) {
                    out[out_off + i + idxt1] = in[in_off + i + idxt2];
                }
            }
        } else {
            for (j = 1; j < ipph; ++j) {
                jc = ip - j;
                idxt1 = j * l1 * ido;
                idxt2 = jc * l1 * ido;
                idxt3 = j * ido;
                int idxt4 = jc * ido;
                for (int i = 0; i < ido; ++i) {
                    for (int k4 = 0; k4 < l1; ++k4) {
                        int idx14 = k4 * ido;
                        int idx23 = idx14 * ip;
                        int idx33 = out_off + i;
                        int idx4 = in_off + i;
                        i1r = in[idx4 + idxt3 + idx23];
                        double i2r = in[idx4 + idxt4 + idx23];
                        out[idx33 + idx14 + idxt1] = i1r + i2r;
                        out[idx33 + idx14 + idxt2] = i1r - i2r;
                    }
                }
            }
            for (int i = 0; i < ido; ++i) {
                for (int k5 = 0; k5 < l1; ++k5) {
                    idx12 = k5 * ido;
                    out[out_off + i + idx12] = in[in_off + i + idx12 * ip];
                }
            }
        }
        int idl = 2 - ido;
        int inc = 0;
        int idxt0 = (ip - 1) * idl1;
        for (int l = 1; l < ipph; ++l) {
            int lc = ip - l;
            idxt1 = l * idl1;
            idxt2 = lc * idl1;
            idxt3 = (idl += ido) + iw1;
            w1r = this.wtable[idxt3 - 2];
            w1i = (double)isign * this.wtable[idxt3 - 1];
            for (ik = 0; ik < idl1; ++ik) {
                int idx15 = in_off + ik;
                int idx24 = out_off + ik;
                in[idx15 + idxt1] = out[idx24] + w1r * out[idx24 + idl1];
                in[idx15 + idxt2] = w1i * out[idx24 + idxt0];
            }
            int idlj = idl;
            inc += ido;
            for (int j4 = 2; j4 < ipph; ++j4) {
                jc = ip - j4;
                if ((idlj += inc) > idp) {
                    idlj -= idp;
                }
                int idxt4 = idlj + iw1;
                double w2r = this.wtable[idxt4 - 2];
                double w2i = (double)isign * this.wtable[idxt4 - 1];
                int idxt5 = j4 * idl1;
                int idxt6 = jc * idl1;
                for (int ik2 = 0; ik2 < idl1; ++ik2) {
                    int idx16 = in_off + ik2;
                    int idx25 = out_off + ik2;
                    double[] arrd = in;
                    int n = idx16 + idxt1;
                    arrd[n] = arrd[n] + w2r * out[idx25 + idxt5];
                    double[] arrd2 = in;
                    int n2 = idx16 + idxt2;
                    arrd2[n2] = arrd2[n2] + w2i * out[idx25 + idxt6];
                }
            }
        }
        for (j3 = 1; j3 < ipph; ++j3) {
            int idxt12 = j3 * idl1;
            for (int ik3 = 0; ik3 < idl1; ++ik3) {
                int idx17 = out_off + ik3;
                double[] arrd = out;
                int n = idx17;
                arrd[n] = arrd[n] + out[idx17 + idxt12];
            }
        }
        for (j3 = 1; j3 < ipph; ++j3) {
            jc = ip - j3;
            idx12 = j3 * idl1;
            int idx26 = jc * idl1;
            for (ik = 1; ik < idl1; ik += 2) {
                idx3 = out_off + ik;
                int idx4 = in_off + ik;
                iidx1 = idx4 + idx12;
                int iidx2 = idx4 + idx26;
                double i1i = in[iidx1 - 1];
                i1r = in[iidx1];
                double i2i = in[iidx2 - 1];
                double i2r = in[iidx2];
                int oidx12 = idx3 + idx12;
                int oidx2 = idx3 + idx26;
                out[oidx12 - 1] = i1i - i2r;
                out[oidx2 - 1] = i1i + i2r;
                out[oidx12] = i1r + i2i;
                out[oidx2] = i1r - i2i;
            }
        }
        nac[0] = 1;
        if (ido == 2) {
            return;
        }
        nac[0] = 0;
        System.arraycopy(out, out_off, in, in_off, idl1);
        int idx0 = l1 * ido;
        for (j2 = 1; j2 < ip; ++j2) {
            idx1 = j2 * idx0;
            for (k = 0; k < l1; ++k) {
                idx2 = k * ido;
                int oidx13 = out_off + idx2 + idx1;
                iidx1 = in_off + idx2 + idx1;
                in[iidx1] = out[oidx13];
                in[iidx1 + 1] = out[oidx13 + 1];
            }
        }
        if (idot <= l1) {
            int idij = 0;
            for (j2 = 1; j2 < ip; ++j2) {
                idij += 2;
                idx1 = j2 * l1 * ido;
                for (int i = 3; i < ido; i += 2) {
                    idx2 = (idij += 2) + iw1 - 1;
                    w1r = this.wtable[idx2 - 1];
                    w1i = (double)isign * this.wtable[idx2];
                    int idx34 = in_off + i;
                    int idx4 = out_off + i;
                    for (int k6 = 0; k6 < l1; ++k6) {
                        int idx5 = k6 * ido + idx1;
                        int iidx12 = idx34 + idx5;
                        int oidx14 = idx4 + idx5;
                        double o1i = out[oidx14 - 1];
                        double o1r = out[oidx14];
                        in[iidx12 - 1] = w1r * o1i - w1i * o1r;
                        in[iidx12] = w1r * o1r + w1i * o1i;
                    }
                }
            }
        } else {
            int idj = 2 - ido;
            for (j2 = 1; j2 < ip; ++j2) {
                idj += ido;
                idx1 = j2 * l1 * ido;
                for (k = 0; k < l1; ++k) {
                    int idij = idj;
                    idx3 = k * ido + idx1;
                    for (int i = 3; i < ido; i += 2) {
                        int idx27 = (idij += 2) - 1 + iw1;
                        w1r = this.wtable[idx27 - 1];
                        w1i = (double)isign * this.wtable[idx27];
                        int iidx13 = in_off + i + idx3;
                        oidx1 = out_off + i + idx3;
                        double o1i = out[oidx1 - 1];
                        double o1r = out[oidx1];
                        in[iidx13 - 1] = w1r * o1i - w1i * o1r;
                        in[iidx13] = w1r * o1r + w1i * o1i;
                    }
                }
            }
        }
    }

    private void cftfsub(int n, double[] a, int offa, int[] ip, int nw, double[] w) {
        if (n > 8) {
            if (n > 32) {
                this.cftf1st(n, a, offa, w, nw - (n >> 2));
                if (n > 512) {
                    this.cftrec4(n, a, offa, nw, w);
                } else if (n > 128) {
                    this.cftleaf(n, 1, a, offa, nw, w);
                } else {
                    this.cftfx41(n, a, offa, nw, w);
                }
                this.bitrv2(n, ip, a, offa);
            } else if (n == 32) {
                this.cftf161(a, offa, w, nw - 8);
                this.bitrv216(a, offa);
            } else {
                this.cftf081(a, offa, w, 0);
                this.bitrv208(a, offa);
            }
        } else if (n == 8) {
            this.cftf040(a, offa);
        } else if (n == 4) {
            this.cftxb020(a, offa);
        }
    }

    private void cftbsub(int n, double[] a, int offa, int[] ip, int nw, double[] w) {
        if (n > 8) {
            if (n > 32) {
                this.cftb1st(n, a, offa, w, nw - (n >> 2));
                if (n > 512) {
                    this.cftrec4(n, a, offa, nw, w);
                } else if (n > 128) {
                    this.cftleaf(n, 1, a, offa, nw, w);
                } else {
                    this.cftfx41(n, a, offa, nw, w);
                }
                this.bitrv2conj(n, ip, a, offa);
            } else if (n == 32) {
                this.cftf161(a, offa, w, nw - 8);
                this.bitrv216neg(a, offa);
            } else {
                this.cftf081(a, offa, w, 0);
                this.bitrv208neg(a, offa);
            }
        } else if (n == 8) {
            this.cftb040(a, offa);
        } else if (n == 4) {
            this.cftxb020(a, offa);
        }
    }

    private void bitrv2(int n, int[] ip, double[] a, int offa) {
        int l;
        int m = 1;
        for (l = n >> 2; l > 8; l >>= 2) {
            m <<= 1;
        }
        int nh = n >> 1;
        int nm = 4 * m;
        if (l == 8) {
            for (int k = 0; k < m; ++k) {
                double yi;
                int idx1;
                double xr;
                int k1;
                double xi;
                int idx2;
                int j1;
                double yr;
                int idx0 = 4 * k;
                for (int j = 0; j < k; ++j) {
                    j1 = 4 * j + 2 * ip[m + k];
                    k1 = idx0 + 2 * ip[m + j];
                    idx1 = offa + j1;
                    idx2 = offa + k1;
                    xr = a[idx1];
                    xi = a[idx1 + 1];
                    yr = a[idx2];
                    yi = a[idx2 + 1];
                    a[idx1] = yr;
                    a[idx1 + 1] = yi;
                    a[idx2] = xr;
                    a[idx2 + 1] = xi;
                    idx1 = offa + (j1 += nm);
                    idx2 = offa + (k1 += 2 * nm);
                    xr = a[idx1];
                    xi = a[idx1 + 1];
                    yr = a[idx2];
                    yi = a[idx2 + 1];
                    a[idx1] = yr;
                    a[idx1 + 1] = yi;
                    a[idx2] = xr;
                    a[idx2 + 1] = xi;
                    idx1 = offa + (j1 += nm);
                    idx2 = offa + (k1 -= nm);
                    xr = a[idx1];
                    xi = a[idx1 + 1];
                    yr = a[idx2];
                    yi = a[idx2 + 1];
                    a[idx1] = yr;
                    a[idx1 + 1] = yi;
                    a[idx2] = xr;
                    a[idx2 + 1] = xi;
                    idx1 = offa + (j1 += nm);
                    idx2 = offa + (k1 += 2 * nm);
                    xr = a[idx1];
                    xi = a[idx1 + 1];
                    yr = a[idx2];
                    yi = a[idx2 + 1];
                    a[idx1] = yr;
                    a[idx1 + 1] = yi;
                    a[idx2] = xr;
                    a[idx2 + 1] = xi;
                    idx1 = offa + (j1 += nh);
                    idx2 = offa + (k1 += 2);
                    xr = a[idx1];
                    xi = a[idx1 + 1];
                    yr = a[idx2];
                    yi = a[idx2 + 1];
                    a[idx1] = yr;
                    a[idx1 + 1] = yi;
                    a[idx2] = xr;
                    a[idx2 + 1] = xi;
                    idx1 = offa + (j1 -= nm);
                    idx2 = offa + (k1 -= 2 * nm);
                    xr = a[idx1];
                    xi = a[idx1 + 1];
                    yr = a[idx2];
                    yi = a[idx2 + 1];
                    a[idx1] = yr;
                    a[idx1 + 1] = yi;
                    a[idx2] = xr;
                    a[idx2 + 1] = xi;
                    idx1 = offa + (j1 -= nm);
                    idx2 = offa + (k1 += nm);
                    xr = a[idx1];
                    xi = a[idx1 + 1];
                    yr = a[idx2];
                    yi = a[idx2 + 1];
                    a[idx1] = yr;
                    a[idx1 + 1] = yi;
                    a[idx2] = xr;
                    a[idx2 + 1] = xi;
                    idx1 = offa + (j1 -= nm);
                    idx2 = offa + (k1 -= 2 * nm);
                    xr = a[idx1];
                    xi = a[idx1 + 1];
                    yr = a[idx2];
                    yi = a[idx2 + 1];
                    a[idx1] = yr;
                    a[idx1 + 1] = yi;
                    a[idx2] = xr;
                    a[idx2 + 1] = xi;
                    idx1 = offa + (j1 += 2);
                    idx2 = offa + (k1 += nh);
                    xr = a[idx1];
                    xi = a[idx1 + 1];
                    yr = a[idx2];
                    yi = a[idx2 + 1];
                    a[idx1] = yr;
                    a[idx1 + 1] = yi;
                    a[idx2] = xr;
                    a[idx2 + 1] = xi;
                    idx1 = offa + (j1 += nm);
                    idx2 = offa + (k1 += 2 * nm);
                    xr = a[idx1];
                    xi = a[idx1 + 1];
                    yr = a[idx2];
                    yi = a[idx2 + 1];
                    a[idx1] = yr;
                    a[idx1 + 1] = yi;
                    a[idx2] = xr;
                    a[idx2 + 1] = xi;
                    idx1 = offa + (j1 += nm);
                    idx2 = offa + (k1 -= nm);
                    xr = a[idx1];
                    xi = a[idx1 + 1];
                    yr = a[idx2];
                    yi = a[idx2 + 1];
                    a[idx1] = yr;
                    a[idx1 + 1] = yi;
                    a[idx2] = xr;
                    a[idx2 + 1] = xi;
                    idx1 = offa + (j1 += nm);
                    idx2 = offa + (k1 += 2 * nm);
                    xr = a[idx1];
                    xi = a[idx1 + 1];
                    yr = a[idx2];
                    yi = a[idx2 + 1];
                    a[idx1] = yr;
                    a[idx1 + 1] = yi;
                    a[idx2] = xr;
                    a[idx2 + 1] = xi;
                    idx1 = offa + (j1 -= nh);
                    idx2 = offa + (k1 -= 2);
                    xr = a[idx1];
                    xi = a[idx1 + 1];
                    yr = a[idx2];
                    yi = a[idx2 + 1];
                    a[idx1] = yr;
                    a[idx1 + 1] = yi;
                    a[idx2] = xr;
                    a[idx2 + 1] = xi;
                    idx1 = offa + (j1 -= nm);
                    idx2 = offa + (k1 -= 2 * nm);
                    xr = a[idx1];
                    xi = a[idx1 + 1];
                    yr = a[idx2];
                    yi = a[idx2 + 1];
                    a[idx1] = yr;
                    a[idx1 + 1] = yi;
                    a[idx2] = xr;
                    a[idx2 + 1] = xi;
                    idx1 = offa + (j1 -= nm);
                    idx2 = offa + (k1 += nm);
                    xr = a[idx1];
                    xi = a[idx1 + 1];
                    yr = a[idx2];
                    yi = a[idx2 + 1];
                    a[idx1] = yr;
                    a[idx1 + 1] = yi;
                    a[idx2] = xr;
                    a[idx2 + 1] = xi;
                    idx1 = offa + (j1 -= nm);
                    idx2 = offa + (k1 -= 2 * nm);
                    xr = a[idx1];
                    xi = a[idx1 + 1];
                    yr = a[idx2];
                    yi = a[idx2 + 1];
                    a[idx1] = yr;
                    a[idx1 + 1] = yi;
                    a[idx2] = xr;
                    a[idx2 + 1] = xi;
                }
                k1 = idx0 + 2 * ip[m + k];
                j1 = k1 + 2;
                idx1 = offa + j1;
                idx2 = offa + (k1 += nh);
                xr = a[idx1];
                xi = a[idx1 + 1];
                yr = a[idx2];
                yi = a[idx2 + 1];
                a[idx1] = yr;
                a[idx1 + 1] = yi;
                a[idx2] = xr;
                a[idx2 + 1] = xi;
                idx1 = offa + (j1 += nm);
                idx2 = offa + (k1 += 2 * nm);
                xr = a[idx1];
                xi = a[idx1 + 1];
                yr = a[idx2];
                yi = a[idx2 + 1];
                a[idx1] = yr;
                a[idx1 + 1] = yi;
                a[idx2] = xr;
                a[idx2 + 1] = xi;
                idx1 = offa + (j1 += nm);
                idx2 = offa + (k1 -= nm);
                xr = a[idx1];
                xi = a[idx1 + 1];
                yr = a[idx2];
                yi = a[idx2 + 1];
                a[idx1] = yr;
                a[idx1 + 1] = yi;
                a[idx2] = xr;
                a[idx2 + 1] = xi;
                idx1 = offa + (j1 -= 2);
                idx2 = offa + (k1 -= nh);
                xr = a[idx1];
                xi = a[idx1 + 1];
                yr = a[idx2];
                yi = a[idx2 + 1];
                a[idx1] = yr;
                a[idx1 + 1] = yi;
                a[idx2] = xr;
                a[idx2 + 1] = xi;
                idx1 = offa + (j1 += nh + 2);
                idx2 = offa + (k1 += nh + 2);
                xr = a[idx1];
                xi = a[idx1 + 1];
                yr = a[idx2];
                yi = a[idx2 + 1];
                a[idx1] = yr;
                a[idx1 + 1] = yi;
                a[idx2] = xr;
                a[idx2 + 1] = xi;
                idx1 = offa + (j1 -= nh - nm);
                idx2 = offa + (k1 += 2 * nm - 2);
                xr = a[idx1];
                xi = a[idx1 + 1];
                yr = a[idx2];
                yi = a[idx2 + 1];
                a[idx1] = yr;
                a[idx1 + 1] = yi;
                a[idx2] = xr;
                a[idx2 + 1] = xi;
            }
        } else {
            for (int k = 0; k < m; ++k) {
                double xi;
                int j1;
                int idx2;
                double yr;
                double yi;
                int idx1;
                double xr;
                int k1;
                int idx0 = 4 * k;
                for (int j = 0; j < k; ++j) {
                    j1 = 4 * j + ip[m + k];
                    k1 = idx0 + ip[m + j];
                    idx1 = offa + j1;
                    idx2 = offa + k1;
                    xr = a[idx1];
                    xi = a[idx1 + 1];
                    yr = a[idx2];
                    yi = a[idx2 + 1];
                    a[idx1] = yr;
                    a[idx1 + 1] = yi;
                    a[idx2] = xr;
                    a[idx2 + 1] = xi;
                    idx1 = offa + (j1 += nm);
                    idx2 = offa + (k1 += nm);
                    xr = a[idx1];
                    xi = a[idx1 + 1];
                    yr = a[idx2];
                    yi = a[idx2 + 1];
                    a[idx1] = yr;
                    a[idx1 + 1] = yi;
                    a[idx2] = xr;
                    a[idx2 + 1] = xi;
                    idx1 = offa + (j1 += nh);
                    idx2 = offa + (k1 += 2);
                    xr = a[idx1];
                    xi = a[idx1 + 1];
                    yr = a[idx2];
                    yi = a[idx2 + 1];
                    a[idx1] = yr;
                    a[idx1 + 1] = yi;
                    a[idx2] = xr;
                    a[idx2 + 1] = xi;
                    idx1 = offa + (j1 -= nm);
                    idx2 = offa + (k1 -= nm);
                    xr = a[idx1];
                    xi = a[idx1 + 1];
                    yr = a[idx2];
                    yi = a[idx2 + 1];
                    a[idx1] = yr;
                    a[idx1 + 1] = yi;
                    a[idx2] = xr;
                    a[idx2 + 1] = xi;
                    idx1 = offa + (j1 += 2);
                    idx2 = offa + (k1 += nh);
                    xr = a[idx1];
                    xi = a[idx1 + 1];
                    yr = a[idx2];
                    yi = a[idx2 + 1];
                    a[idx1] = yr;
                    a[idx1 + 1] = yi;
                    a[idx2] = xr;
                    a[idx2 + 1] = xi;
                    idx1 = offa + (j1 += nm);
                    idx2 = offa + (k1 += nm);
                    xr = a[idx1];
                    xi = a[idx1 + 1];
                    yr = a[idx2];
                    yi = a[idx2 + 1];
                    a[idx1] = yr;
                    a[idx1 + 1] = yi;
                    a[idx2] = xr;
                    a[idx2 + 1] = xi;
                    idx1 = offa + (j1 -= nh);
                    idx2 = offa + (k1 -= 2);
                    xr = a[idx1];
                    xi = a[idx1 + 1];
                    yr = a[idx2];
                    yi = a[idx2 + 1];
                    a[idx1] = yr;
                    a[idx1 + 1] = yi;
                    a[idx2] = xr;
                    a[idx2 + 1] = xi;
                    idx1 = offa + (j1 -= nm);
                    idx2 = offa + (k1 -= nm);
                    xr = a[idx1];
                    xi = a[idx1 + 1];
                    yr = a[idx2];
                    yi = a[idx2 + 1];
                    a[idx1] = yr;
                    a[idx1 + 1] = yi;
                    a[idx2] = xr;
                    a[idx2 + 1] = xi;
                }
                k1 = idx0 + ip[m + k];
                j1 = k1 + 2;
                idx1 = offa + j1;
                idx2 = offa + (k1 += nh);
                xr = a[idx1];
                xi = a[idx1 + 1];
                yr = a[idx2];
                yi = a[idx2 + 1];
                a[idx1] = yr;
                a[idx1 + 1] = yi;
                a[idx2] = xr;
                a[idx2 + 1] = xi;
                idx1 = offa + (j1 += nm);
                idx2 = offa + (k1 += nm);
                xr = a[idx1];
                xi = a[idx1 + 1];
                yr = a[idx2];
                yi = a[idx2 + 1];
                a[idx1] = yr;
                a[idx1 + 1] = yi;
                a[idx2] = xr;
                a[idx2 + 1] = xi;
            }
        }
    }

    private void bitrv2conj(int n, int[] ip, double[] a, int offa) {
        int l;
        int m = 1;
        for (l = n >> 2; l > 8; l >>= 2) {
            m <<= 1;
        }
        int nh = n >> 1;
        int nm = 4 * m;
        if (l == 8) {
            for (int k = 0; k < m; ++k) {
                double yi;
                int idx1;
                double xr;
                int k1;
                double xi;
                int idx2;
                int j1;
                double yr;
                int idx0 = 4 * k;
                for (int j = 0; j < k; ++j) {
                    j1 = 4 * j + 2 * ip[m + k];
                    k1 = idx0 + 2 * ip[m + j];
                    idx1 = offa + j1;
                    idx2 = offa + k1;
                    xr = a[idx1];
                    xi = - a[idx1 + 1];
                    yr = a[idx2];
                    yi = - a[idx2 + 1];
                    a[idx1] = yr;
                    a[idx1 + 1] = yi;
                    a[idx2] = xr;
                    a[idx2 + 1] = xi;
                    idx1 = offa + (j1 += nm);
                    idx2 = offa + (k1 += 2 * nm);
                    xr = a[idx1];
                    xi = - a[idx1 + 1];
                    yr = a[idx2];
                    yi = - a[idx2 + 1];
                    a[idx1] = yr;
                    a[idx1 + 1] = yi;
                    a[idx2] = xr;
                    a[idx2 + 1] = xi;
                    idx1 = offa + (j1 += nm);
                    idx2 = offa + (k1 -= nm);
                    xr = a[idx1];
                    xi = - a[idx1 + 1];
                    yr = a[idx2];
                    yi = - a[idx2 + 1];
                    a[idx1] = yr;
                    a[idx1 + 1] = yi;
                    a[idx2] = xr;
                    a[idx2 + 1] = xi;
                    idx1 = offa + (j1 += nm);
                    idx2 = offa + (k1 += 2 * nm);
                    xr = a[idx1];
                    xi = - a[idx1 + 1];
                    yr = a[idx2];
                    yi = - a[idx2 + 1];
                    a[idx1] = yr;
                    a[idx1 + 1] = yi;
                    a[idx2] = xr;
                    a[idx2 + 1] = xi;
                    idx1 = offa + (j1 += nh);
                    idx2 = offa + (k1 += 2);
                    xr = a[idx1];
                    xi = - a[idx1 + 1];
                    yr = a[idx2];
                    yi = - a[idx2 + 1];
                    a[idx1] = yr;
                    a[idx1 + 1] = yi;
                    a[idx2] = xr;
                    a[idx2 + 1] = xi;
                    idx1 = offa + (j1 -= nm);
                    idx2 = offa + (k1 -= 2 * nm);
                    xr = a[idx1];
                    xi = - a[idx1 + 1];
                    yr = a[idx2];
                    yi = - a[idx2 + 1];
                    a[idx1] = yr;
                    a[idx1 + 1] = yi;
                    a[idx2] = xr;
                    a[idx2 + 1] = xi;
                    idx1 = offa + (j1 -= nm);
                    idx2 = offa + (k1 += nm);
                    xr = a[idx1];
                    xi = - a[idx1 + 1];
                    yr = a[idx2];
                    yi = - a[idx2 + 1];
                    a[idx1] = yr;
                    a[idx1 + 1] = yi;
                    a[idx2] = xr;
                    a[idx2 + 1] = xi;
                    idx1 = offa + (j1 -= nm);
                    idx2 = offa + (k1 -= 2 * nm);
                    xr = a[idx1];
                    xi = - a[idx1 + 1];
                    yr = a[idx2];
                    yi = - a[idx2 + 1];
                    a[idx1] = yr;
                    a[idx1 + 1] = yi;
                    a[idx2] = xr;
                    a[idx2 + 1] = xi;
                    idx1 = offa + (j1 += 2);
                    idx2 = offa + (k1 += nh);
                    xr = a[idx1];
                    xi = - a[idx1 + 1];
                    yr = a[idx2];
                    yi = - a[idx2 + 1];
                    a[idx1] = yr;
                    a[idx1 + 1] = yi;
                    a[idx2] = xr;
                    a[idx2 + 1] = xi;
                    idx1 = offa + (j1 += nm);
                    idx2 = offa + (k1 += 2 * nm);
                    xr = a[idx1];
                    xi = - a[idx1 + 1];
                    yr = a[idx2];
                    yi = - a[idx2 + 1];
                    a[idx1] = yr;
                    a[idx1 + 1] = yi;
                    a[idx2] = xr;
                    a[idx2 + 1] = xi;
                    idx1 = offa + (j1 += nm);
                    idx2 = offa + (k1 -= nm);
                    xr = a[idx1];
                    xi = - a[idx1 + 1];
                    yr = a[idx2];
                    yi = - a[idx2 + 1];
                    a[idx1] = yr;
                    a[idx1 + 1] = yi;
                    a[idx2] = xr;
                    a[idx2 + 1] = xi;
                    idx1 = offa + (j1 += nm);
                    idx2 = offa + (k1 += 2 * nm);
                    xr = a[idx1];
                    xi = - a[idx1 + 1];
                    yr = a[idx2];
                    yi = - a[idx2 + 1];
                    a[idx1] = yr;
                    a[idx1 + 1] = yi;
                    a[idx2] = xr;
                    a[idx2 + 1] = xi;
                    idx1 = offa + (j1 -= nh);
                    idx2 = offa + (k1 -= 2);
                    xr = a[idx1];
                    xi = - a[idx1 + 1];
                    yr = a[idx2];
                    yi = - a[idx2 + 1];
                    a[idx1] = yr;
                    a[idx1 + 1] = yi;
                    a[idx2] = xr;
                    a[idx2 + 1] = xi;
                    idx1 = offa + (j1 -= nm);
                    idx2 = offa + (k1 -= 2 * nm);
                    xr = a[idx1];
                    xi = - a[idx1 + 1];
                    yr = a[idx2];
                    yi = - a[idx2 + 1];
                    a[idx1] = yr;
                    a[idx1 + 1] = yi;
                    a[idx2] = xr;
                    a[idx2 + 1] = xi;
                    idx1 = offa + (j1 -= nm);
                    idx2 = offa + (k1 += nm);
                    xr = a[idx1];
                    xi = - a[idx1 + 1];
                    yr = a[idx2];
                    yi = - a[idx2 + 1];
                    a[idx1] = yr;
                    a[idx1 + 1] = yi;
                    a[idx2] = xr;
                    a[idx2 + 1] = xi;
                    idx1 = offa + (j1 -= nm);
                    idx2 = offa + (k1 -= 2 * nm);
                    xr = a[idx1];
                    xi = - a[idx1 + 1];
                    yr = a[idx2];
                    yi = - a[idx2 + 1];
                    a[idx1] = yr;
                    a[idx1 + 1] = yi;
                    a[idx2] = xr;
                    a[idx2 + 1] = xi;
                }
                k1 = idx0 + 2 * ip[m + k];
                j1 = k1 + 2;
                idx1 = offa + j1;
                idx2 = offa + (k1 += nh);
                a[idx1 - 1] = - a[idx1 - 1];
                xr = a[idx1];
                xi = - a[idx1 + 1];
                yr = a[idx2];
                yi = - a[idx2 + 1];
                a[idx1] = yr;
                a[idx1 + 1] = yi;
                a[idx2] = xr;
                a[idx2 + 1] = xi;
                a[idx2 + 3] = - a[idx2 + 3];
                idx1 = offa + (j1 += nm);
                idx2 = offa + (k1 += 2 * nm);
                xr = a[idx1];
                xi = - a[idx1 + 1];
                yr = a[idx2];
                yi = - a[idx2 + 1];
                a[idx1] = yr;
                a[idx1 + 1] = yi;
                a[idx2] = xr;
                a[idx2 + 1] = xi;
                idx1 = offa + (j1 += nm);
                idx2 = offa + (k1 -= nm);
                xr = a[idx1];
                xi = - a[idx1 + 1];
                yr = a[idx2];
                yi = - a[idx2 + 1];
                a[idx1] = yr;
                a[idx1 + 1] = yi;
                a[idx2] = xr;
                a[idx2 + 1] = xi;
                idx1 = offa + (j1 -= 2);
                idx2 = offa + (k1 -= nh);
                xr = a[idx1];
                xi = - a[idx1 + 1];
                yr = a[idx2];
                yi = - a[idx2 + 1];
                a[idx1] = yr;
                a[idx1 + 1] = yi;
                a[idx2] = xr;
                a[idx2 + 1] = xi;
                idx1 = offa + (j1 += nh + 2);
                idx2 = offa + (k1 += nh + 2);
                xr = a[idx1];
                xi = - a[idx1 + 1];
                yr = a[idx2];
                yi = - a[idx2 + 1];
                a[idx1] = yr;
                a[idx1 + 1] = yi;
                a[idx2] = xr;
                a[idx2 + 1] = xi;
                idx1 = offa + (j1 -= nh - nm);
                idx2 = offa + (k1 += 2 * nm - 2);
                a[idx1 - 1] = - a[idx1 - 1];
                xr = a[idx1];
                xi = - a[idx1 + 1];
                yr = a[idx2];
                yi = - a[idx2 + 1];
                a[idx1] = yr;
                a[idx1 + 1] = yi;
                a[idx2] = xr;
                a[idx2 + 1] = xi;
                a[idx2 + 3] = - a[idx2 + 3];
            }
        } else {
            for (int k = 0; k < m; ++k) {
                double xi;
                int j1;
                int idx2;
                double yr;
                double yi;
                int idx1;
                double xr;
                int k1;
                int idx0 = 4 * k;
                for (int j = 0; j < k; ++j) {
                    j1 = 4 * j + ip[m + k];
                    k1 = idx0 + ip[m + j];
                    idx1 = offa + j1;
                    idx2 = offa + k1;
                    xr = a[idx1];
                    xi = - a[idx1 + 1];
                    yr = a[idx2];
                    yi = - a[idx2 + 1];
                    a[idx1] = yr;
                    a[idx1 + 1] = yi;
                    a[idx2] = xr;
                    a[idx2 + 1] = xi;
                    idx1 = offa + (j1 += nm);
                    idx2 = offa + (k1 += nm);
                    xr = a[idx1];
                    xi = - a[idx1 + 1];
                    yr = a[idx2];
                    yi = - a[idx2 + 1];
                    a[idx1] = yr;
                    a[idx1 + 1] = yi;
                    a[idx2] = xr;
                    a[idx2 + 1] = xi;
                    idx1 = offa + (j1 += nh);
                    idx2 = offa + (k1 += 2);
                    xr = a[idx1];
                    xi = - a[idx1 + 1];
                    yr = a[idx2];
                    yi = - a[idx2 + 1];
                    a[idx1] = yr;
                    a[idx1 + 1] = yi;
                    a[idx2] = xr;
                    a[idx2 + 1] = xi;
                    idx1 = offa + (j1 -= nm);
                    idx2 = offa + (k1 -= nm);
                    xr = a[idx1];
                    xi = - a[idx1 + 1];
                    yr = a[idx2];
                    yi = - a[idx2 + 1];
                    a[idx1] = yr;
                    a[idx1 + 1] = yi;
                    a[idx2] = xr;
                    a[idx2 + 1] = xi;
                    idx1 = offa + (j1 += 2);
                    idx2 = offa + (k1 += nh);
                    xr = a[idx1];
                    xi = - a[idx1 + 1];
                    yr = a[idx2];
                    yi = - a[idx2 + 1];
                    a[idx1] = yr;
                    a[idx1 + 1] = yi;
                    a[idx2] = xr;
                    a[idx2 + 1] = xi;
                    idx1 = offa + (j1 += nm);
                    idx2 = offa + (k1 += nm);
                    xr = a[idx1];
                    xi = - a[idx1 + 1];
                    yr = a[idx2];
                    yi = - a[idx2 + 1];
                    a[idx1] = yr;
                    a[idx1 + 1] = yi;
                    a[idx2] = xr;
                    a[idx2 + 1] = xi;
                    idx1 = offa + (j1 -= nh);
                    idx2 = offa + (k1 -= 2);
                    xr = a[idx1];
                    xi = - a[idx1 + 1];
                    yr = a[idx2];
                    yi = - a[idx2 + 1];
                    a[idx1] = yr;
                    a[idx1 + 1] = yi;
                    a[idx2] = xr;
                    a[idx2 + 1] = xi;
                    idx1 = offa + (j1 -= nm);
                    idx2 = offa + (k1 -= nm);
                    xr = a[idx1];
                    xi = - a[idx1 + 1];
                    yr = a[idx2];
                    yi = - a[idx2 + 1];
                    a[idx1] = yr;
                    a[idx1 + 1] = yi;
                    a[idx2] = xr;
                    a[idx2 + 1] = xi;
                }
                k1 = idx0 + ip[m + k];
                j1 = k1 + 2;
                idx1 = offa + j1;
                idx2 = offa + (k1 += nh);
                a[idx1 - 1] = - a[idx1 - 1];
                xr = a[idx1];
                xi = - a[idx1 + 1];
                yr = a[idx2];
                yi = - a[idx2 + 1];
                a[idx1] = yr;
                a[idx1 + 1] = yi;
                a[idx2] = xr;
                a[idx2 + 1] = xi;
                a[idx2 + 3] = - a[idx2 + 3];
                idx1 = offa + (j1 += nm);
                idx2 = offa + (k1 += nm);
                a[idx1 - 1] = - a[idx1 - 1];
                xr = a[idx1];
                xi = - a[idx1 + 1];
                yr = a[idx2];
                yi = - a[idx2 + 1];
                a[idx1] = yr;
                a[idx1 + 1] = yi;
                a[idx2] = xr;
                a[idx2 + 1] = xi;
                a[idx2 + 3] = - a[idx2 + 3];
            }
        }
    }

    private void bitrv216(double[] a, int offa) {
        double x1r = a[offa + 2];
        double x1i = a[offa + 3];
        double x2r = a[offa + 4];
        double x2i = a[offa + 5];
        double x3r = a[offa + 6];
        double x3i = a[offa + 7];
        double x4r = a[offa + 8];
        double x4i = a[offa + 9];
        double x5r = a[offa + 10];
        double x5i = a[offa + 11];
        double x7r = a[offa + 14];
        double x7i = a[offa + 15];
        double x8r = a[offa + 16];
        double x8i = a[offa + 17];
        double x10r = a[offa + 20];
        double x10i = a[offa + 21];
        double x11r = a[offa + 22];
        double x11i = a[offa + 23];
        double x12r = a[offa + 24];
        double x12i = a[offa + 25];
        double x13r = a[offa + 26];
        double x13i = a[offa + 27];
        double x14r = a[offa + 28];
        double x14i = a[offa + 29];
        a[offa + 2] = x8r;
        a[offa + 3] = x8i;
        a[offa + 4] = x4r;
        a[offa + 5] = x4i;
        a[offa + 6] = x12r;
        a[offa + 7] = x12i;
        a[offa + 8] = x2r;
        a[offa + 9] = x2i;
        a[offa + 10] = x10r;
        a[offa + 11] = x10i;
        a[offa + 14] = x14r;
        a[offa + 15] = x14i;
        a[offa + 16] = x1r;
        a[offa + 17] = x1i;
        a[offa + 20] = x5r;
        a[offa + 21] = x5i;
        a[offa + 22] = x13r;
        a[offa + 23] = x13i;
        a[offa + 24] = x3r;
        a[offa + 25] = x3i;
        a[offa + 26] = x11r;
        a[offa + 27] = x11i;
        a[offa + 28] = x7r;
        a[offa + 29] = x7i;
    }

    private void bitrv216neg(double[] a, int offa) {
        double x1r = a[offa + 2];
        double x1i = a[offa + 3];
        double x2r = a[offa + 4];
        double x2i = a[offa + 5];
        double x3r = a[offa + 6];
        double x3i = a[offa + 7];
        double x4r = a[offa + 8];
        double x4i = a[offa + 9];
        double x5r = a[offa + 10];
        double x5i = a[offa + 11];
        double x6r = a[offa + 12];
        double x6i = a[offa + 13];
        double x7r = a[offa + 14];
        double x7i = a[offa + 15];
        double x8r = a[offa + 16];
        double x8i = a[offa + 17];
        double x9r = a[offa + 18];
        double x9i = a[offa + 19];
        double x10r = a[offa + 20];
        double x10i = a[offa + 21];
        double x11r = a[offa + 22];
        double x11i = a[offa + 23];
        double x12r = a[offa + 24];
        double x12i = a[offa + 25];
        double x13r = a[offa + 26];
        double x13i = a[offa + 27];
        double x14r = a[offa + 28];
        double x14i = a[offa + 29];
        double x15r = a[offa + 30];
        double x15i = a[offa + 31];
        a[offa + 2] = x15r;
        a[offa + 3] = x15i;
        a[offa + 4] = x7r;
        a[offa + 5] = x7i;
        a[offa + 6] = x11r;
        a[offa + 7] = x11i;
        a[offa + 8] = x3r;
        a[offa + 9] = x3i;
        a[offa + 10] = x13r;
        a[offa + 11] = x13i;
        a[offa + 12] = x5r;
        a[offa + 13] = x5i;
        a[offa + 14] = x9r;
        a[offa + 15] = x9i;
        a[offa + 16] = x1r;
        a[offa + 17] = x1i;
        a[offa + 18] = x14r;
        a[offa + 19] = x14i;
        a[offa + 20] = x6r;
        a[offa + 21] = x6i;
        a[offa + 22] = x10r;
        a[offa + 23] = x10i;
        a[offa + 24] = x2r;
        a[offa + 25] = x2i;
        a[offa + 26] = x12r;
        a[offa + 27] = x12i;
        a[offa + 28] = x4r;
        a[offa + 29] = x4i;
        a[offa + 30] = x8r;
        a[offa + 31] = x8i;
    }

    private void bitrv208(double[] a, int offa) {
        double x1r = a[offa + 2];
        double x1i = a[offa + 3];
        double x3r = a[offa + 6];
        double x3i = a[offa + 7];
        double x4r = a[offa + 8];
        double x4i = a[offa + 9];
        double x6r = a[offa + 12];
        double x6i = a[offa + 13];
        a[offa + 2] = x4r;
        a[offa + 3] = x4i;
        a[offa + 6] = x6r;
        a[offa + 7] = x6i;
        a[offa + 8] = x1r;
        a[offa + 9] = x1i;
        a[offa + 12] = x3r;
        a[offa + 13] = x3i;
    }

    private void bitrv208neg(double[] a, int offa) {
        double x1r = a[offa + 2];
        double x1i = a[offa + 3];
        double x2r = a[offa + 4];
        double x2i = a[offa + 5];
        double x3r = a[offa + 6];
        double x3i = a[offa + 7];
        double x4r = a[offa + 8];
        double x4i = a[offa + 9];
        double x5r = a[offa + 10];
        double x5i = a[offa + 11];
        double x6r = a[offa + 12];
        double x6i = a[offa + 13];
        double x7r = a[offa + 14];
        double x7i = a[offa + 15];
        a[offa + 2] = x7r;
        a[offa + 3] = x7i;
        a[offa + 4] = x3r;
        a[offa + 5] = x3i;
        a[offa + 6] = x5r;
        a[offa + 7] = x5i;
        a[offa + 8] = x1r;
        a[offa + 9] = x1i;
        a[offa + 10] = x6r;
        a[offa + 11] = x6i;
        a[offa + 12] = x2r;
        a[offa + 13] = x2i;
        a[offa + 14] = x4r;
        a[offa + 15] = x4i;
    }

    private void cftf1st(int n, double[] a, int offa, double[] w, int startw) {
        int j0;
        double wk3r;
        double wk1i;
        int m;
        double wk1r;
        int idx0;
        double wk3i;
        int mh = n >> 3;
        int j1 = m = 2 * mh;
        int j2 = j1 + m;
        int j3 = j2 + m;
        int idx1 = offa + j1;
        int idx2 = offa + j2;
        int idx3 = offa + j3;
        double x0r = a[offa] + a[idx2];
        double x0i = a[offa + 1] + a[idx2 + 1];
        double x1r = a[offa] - a[idx2];
        double x1i = a[offa + 1] - a[idx2 + 1];
        double x2r = a[idx1] + a[idx3];
        double x2i = a[idx1 + 1] + a[idx3 + 1];
        double x3r = a[idx1] - a[idx3];
        double x3i = a[idx1 + 1] - a[idx3 + 1];
        a[offa] = x0r + x2r;
        a[offa + 1] = x0i + x2i;
        a[idx1] = x0r - x2r;
        a[idx1 + 1] = x0i - x2i;
        a[idx2] = x1r - x3i;
        a[idx2 + 1] = x1i + x3r;
        a[idx3] = x1r + x3i;
        a[idx3 + 1] = x1i - x3r;
        double wn4r = w[startw + 1];
        double csc1 = w[startw + 2];
        double csc3 = w[startw + 3];
        double wd1r = 1.0;
        double wd1i = 0.0;
        double wd3r = 1.0;
        double wd3i = 0.0;
        int k = 0;
        for (int j = 2; j < mh - 2; j += 4) {
            int idx4 = startw + (k += 4);
            wk1r = csc1 * (wd1r + w[idx4]);
            wk1i = csc1 * (wd1i + w[idx4 + 1]);
            wk3r = csc3 * (wd3r + w[idx4 + 2]);
            wk3i = csc3 * (wd3i + w[idx4 + 3]);
            wd1r = w[idx4];
            wd1i = w[idx4 + 1];
            wd3r = w[idx4 + 2];
            wd3i = w[idx4 + 3];
            j1 = j + m;
            j2 = j1 + m;
            j3 = j2 + m;
            idx1 = offa + j1;
            idx2 = offa + j2;
            idx3 = offa + j3;
            int idx5 = offa + j;
            x0r = a[idx5] + a[idx2];
            x0i = a[idx5 + 1] + a[idx2 + 1];
            x1r = a[idx5] - a[idx2];
            x1i = a[idx5 + 1] - a[idx2 + 1];
            double y0r = a[idx5 + 2] + a[idx2 + 2];
            double y0i = a[idx5 + 3] + a[idx2 + 3];
            double y1r = a[idx5 + 2] - a[idx2 + 2];
            double y1i = a[idx5 + 3] - a[idx2 + 3];
            x2r = a[idx1] + a[idx3];
            x2i = a[idx1 + 1] + a[idx3 + 1];
            x3r = a[idx1] - a[idx3];
            x3i = a[idx1 + 1] - a[idx3 + 1];
            double y2r = a[idx1 + 2] + a[idx3 + 2];
            double y2i = a[idx1 + 3] + a[idx3 + 3];
            double y3r = a[idx1 + 2] - a[idx3 + 2];
            double y3i = a[idx1 + 3] - a[idx3 + 3];
            a[idx5] = x0r + x2r;
            a[idx5 + 1] = x0i + x2i;
            a[idx5 + 2] = y0r + y2r;
            a[idx5 + 3] = y0i + y2i;
            a[idx1] = x0r - x2r;
            a[idx1 + 1] = x0i - x2i;
            a[idx1 + 2] = y0r - y2r;
            a[idx1 + 3] = y0i - y2i;
            x0r = x1r - x3i;
            x0i = x1i + x3r;
            a[idx2] = wk1r * x0r - wk1i * x0i;
            a[idx2 + 1] = wk1r * x0i + wk1i * x0r;
            x0r = y1r - y3i;
            x0i = y1i + y3r;
            a[idx2 + 2] = wd1r * x0r - wd1i * x0i;
            a[idx2 + 3] = wd1r * x0i + wd1i * x0r;
            x0r = x1r + x3i;
            x0i = x1i - x3r;
            a[idx3] = wk3r * x0r + wk3i * x0i;
            a[idx3 + 1] = wk3r * x0i - wk3i * x0r;
            x0r = y1r + y3i;
            x0i = y1i - y3r;
            a[idx3 + 2] = wd3r * x0r + wd3i * x0i;
            a[idx3 + 3] = wd3r * x0i - wd3i * x0r;
            j0 = m - j;
            j1 = j0 + m;
            j2 = j1 + m;
            j3 = j2 + m;
            idx0 = offa + j0;
            idx1 = offa + j1;
            idx2 = offa + j2;
            idx3 = offa + j3;
            x0r = a[idx0] + a[idx2];
            x0i = a[idx0 + 1] + a[idx2 + 1];
            x1r = a[idx0] - a[idx2];
            x1i = a[idx0 + 1] - a[idx2 + 1];
            y0r = a[idx0 - 2] + a[idx2 - 2];
            y0i = a[idx0 - 1] + a[idx2 - 1];
            y1r = a[idx0 - 2] - a[idx2 - 2];
            y1i = a[idx0 - 1] - a[idx2 - 1];
            x2r = a[idx1] + a[idx3];
            x2i = a[idx1 + 1] + a[idx3 + 1];
            x3r = a[idx1] - a[idx3];
            x3i = a[idx1 + 1] - a[idx3 + 1];
            y2r = a[idx1 - 2] + a[idx3 - 2];
            y2i = a[idx1 - 1] + a[idx3 - 1];
            y3r = a[idx1 - 2] - a[idx3 - 2];
            y3i = a[idx1 - 1] - a[idx3 - 1];
            a[idx0] = x0r + x2r;
            a[idx0 + 1] = x0i + x2i;
            a[idx0 - 2] = y0r + y2r;
            a[idx0 - 1] = y0i + y2i;
            a[idx1] = x0r - x2r;
            a[idx1 + 1] = x0i - x2i;
            a[idx1 - 2] = y0r - y2r;
            a[idx1 - 1] = y0i - y2i;
            x0r = x1r - x3i;
            x0i = x1i + x3r;
            a[idx2] = wk1i * x0r - wk1r * x0i;
            a[idx2 + 1] = wk1i * x0i + wk1r * x0r;
            x0r = y1r - y3i;
            x0i = y1i + y3r;
            a[idx2 - 2] = wd1i * x0r - wd1r * x0i;
            a[idx2 - 1] = wd1i * x0i + wd1r * x0r;
            x0r = x1r + x3i;
            x0i = x1i - x3r;
            a[idx3] = wk3i * x0r + wk3r * x0i;
            a[idx3 + 1] = wk3i * x0i - wk3r * x0r;
            x0r = y1r + y3i;
            x0i = y1i - y3r;
            a[offa + j3 - 2] = wd3i * x0r + wd3r * x0i;
            a[offa + j3 - 1] = wd3i * x0i - wd3r * x0r;
        }
        wk1r = csc1 * (wd1r + wn4r);
        wk1i = csc1 * (wd1i + wn4r);
        wk3r = csc3 * (wd3r - wn4r);
        wk3i = csc3 * (wd3i - wn4r);
        j0 = mh;
        j1 = j0 + m;
        j2 = j1 + m;
        j3 = j2 + m;
        idx0 = offa + j0;
        idx1 = offa + j1;
        idx2 = offa + j2;
        idx3 = offa + j3;
        x0r = a[idx0 - 2] + a[idx2 - 2];
        x0i = a[idx0 - 1] + a[idx2 - 1];
        x1r = a[idx0 - 2] - a[idx2 - 2];
        x1i = a[idx0 - 1] - a[idx2 - 1];
        x2r = a[idx1 - 2] + a[idx3 - 2];
        x2i = a[idx1 - 1] + a[idx3 - 1];
        x3r = a[idx1 - 2] - a[idx3 - 2];
        x3i = a[idx1 - 1] - a[idx3 - 1];
        a[idx0 - 2] = x0r + x2r;
        a[idx0 - 1] = x0i + x2i;
        a[idx1 - 2] = x0r - x2r;
        a[idx1 - 1] = x0i - x2i;
        x0r = x1r - x3i;
        x0i = x1i + x3r;
        a[idx2 - 2] = wk1r * x0r - wk1i * x0i;
        a[idx2 - 1] = wk1r * x0i + wk1i * x0r;
        x0r = x1r + x3i;
        x0i = x1i - x3r;
        a[idx3 - 2] = wk3r * x0r + wk3i * x0i;
        a[idx3 - 1] = wk3r * x0i - wk3i * x0r;
        x0r = a[idx0] + a[idx2];
        x0i = a[idx0 + 1] + a[idx2 + 1];
        x1r = a[idx0] - a[idx2];
        x1i = a[idx0 + 1] - a[idx2 + 1];
        x2r = a[idx1] + a[idx3];
        x2i = a[idx1 + 1] + a[idx3 + 1];
        x3r = a[idx1] - a[idx3];
        x3i = a[idx1 + 1] - a[idx3 + 1];
        a[idx0] = x0r + x2r;
        a[idx0 + 1] = x0i + x2i;
        a[idx1] = x0r - x2r;
        a[idx1 + 1] = x0i - x2i;
        x0r = x1r - x3i;
        x0i = x1i + x3r;
        a[idx2] = wn4r * (x0r - x0i);
        a[idx2 + 1] = wn4r * (x0i + x0r);
        x0r = x1r + x3i;
        x0i = x1i - x3r;
        a[idx3] = (- wn4r) * (x0r + x0i);
        a[idx3 + 1] = (- wn4r) * (x0i - x0r);
        x0r = a[idx0 + 2] + a[idx2 + 2];
        x0i = a[idx0 + 3] + a[idx2 + 3];
        x1r = a[idx0 + 2] - a[idx2 + 2];
        x1i = a[idx0 + 3] - a[idx2 + 3];
        x2r = a[idx1 + 2] + a[idx3 + 2];
        x2i = a[idx1 + 3] + a[idx3 + 3];
        x3r = a[idx1 + 2] - a[idx3 + 2];
        x3i = a[idx1 + 3] - a[idx3 + 3];
        a[idx0 + 2] = x0r + x2r;
        a[idx0 + 3] = x0i + x2i;
        a[idx1 + 2] = x0r - x2r;
        a[idx1 + 3] = x0i - x2i;
        x0r = x1r - x3i;
        x0i = x1i + x3r;
        a[idx2 + 2] = wk1i * x0r - wk1r * x0i;
        a[idx2 + 3] = wk1i * x0i + wk1r * x0r;
        x0r = x1r + x3i;
        x0i = x1i - x3r;
        a[idx3 + 2] = wk3i * x0r + wk3r * x0i;
        a[idx3 + 3] = wk3i * x0i - wk3r * x0r;
    }

    private void cftb1st(int n, double[] a, int offa, double[] w, int startw) {
        int j0;
        double wk3r;
        double wk1i;
        int m;
        double wk1r;
        int idx0;
        double wk3i;
        int mh = n >> 3;
        int j1 = m = 2 * mh;
        int j2 = j1 + m;
        int j3 = j2 + m;
        int idx1 = offa + j1;
        int idx2 = offa + j2;
        int idx3 = offa + j3;
        double x0r = a[offa] + a[idx2];
        double x0i = - a[offa + 1] - a[idx2 + 1];
        double x1r = a[offa] - a[idx2];
        double x1i = - a[offa + 1] + a[idx2 + 1];
        double x2r = a[idx1] + a[idx3];
        double x2i = a[idx1 + 1] + a[idx3 + 1];
        double x3r = a[idx1] - a[idx3];
        double x3i = a[idx1 + 1] - a[idx3 + 1];
        a[offa] = x0r + x2r;
        a[offa + 1] = x0i - x2i;
        a[idx1] = x0r - x2r;
        a[idx1 + 1] = x0i + x2i;
        a[idx2] = x1r + x3i;
        a[idx2 + 1] = x1i + x3r;
        a[idx3] = x1r - x3i;
        a[idx3 + 1] = x1i - x3r;
        double wn4r = w[startw + 1];
        double csc1 = w[startw + 2];
        double csc3 = w[startw + 3];
        double wd1r = 1.0;
        double wd1i = 0.0;
        double wd3r = 1.0;
        double wd3i = 0.0;
        int k = 0;
        for (int j = 2; j < mh - 2; j += 4) {
            int idx4 = startw + (k += 4);
            wk1r = csc1 * (wd1r + w[idx4]);
            wk1i = csc1 * (wd1i + w[idx4 + 1]);
            wk3r = csc3 * (wd3r + w[idx4 + 2]);
            wk3i = csc3 * (wd3i + w[idx4 + 3]);
            wd1r = w[idx4];
            wd1i = w[idx4 + 1];
            wd3r = w[idx4 + 2];
            wd3i = w[idx4 + 3];
            j1 = j + m;
            j2 = j1 + m;
            j3 = j2 + m;
            idx1 = offa + j1;
            idx2 = offa + j2;
            idx3 = offa + j3;
            int idx5 = offa + j;
            x0r = a[idx5] + a[idx2];
            x0i = - a[idx5 + 1] - a[idx2 + 1];
            x1r = a[idx5] - a[offa + j2];
            x1i = - a[idx5 + 1] + a[idx2 + 1];
            double y0r = a[idx5 + 2] + a[idx2 + 2];
            double y0i = - a[idx5 + 3] - a[idx2 + 3];
            double y1r = a[idx5 + 2] - a[idx2 + 2];
            double y1i = - a[idx5 + 3] + a[idx2 + 3];
            x2r = a[idx1] + a[idx3];
            x2i = a[idx1 + 1] + a[idx3 + 1];
            x3r = a[idx1] - a[idx3];
            x3i = a[idx1 + 1] - a[idx3 + 1];
            double y2r = a[idx1 + 2] + a[idx3 + 2];
            double y2i = a[idx1 + 3] + a[idx3 + 3];
            double y3r = a[idx1 + 2] - a[idx3 + 2];
            double y3i = a[idx1 + 3] - a[idx3 + 3];
            a[idx5] = x0r + x2r;
            a[idx5 + 1] = x0i - x2i;
            a[idx5 + 2] = y0r + y2r;
            a[idx5 + 3] = y0i - y2i;
            a[idx1] = x0r - x2r;
            a[idx1 + 1] = x0i + x2i;
            a[idx1 + 2] = y0r - y2r;
            a[idx1 + 3] = y0i + y2i;
            x0r = x1r + x3i;
            x0i = x1i + x3r;
            a[idx2] = wk1r * x0r - wk1i * x0i;
            a[idx2 + 1] = wk1r * x0i + wk1i * x0r;
            x0r = y1r + y3i;
            x0i = y1i + y3r;
            a[idx2 + 2] = wd1r * x0r - wd1i * x0i;
            a[idx2 + 3] = wd1r * x0i + wd1i * x0r;
            x0r = x1r - x3i;
            x0i = x1i - x3r;
            a[idx3] = wk3r * x0r + wk3i * x0i;
            a[idx3 + 1] = wk3r * x0i - wk3i * x0r;
            x0r = y1r - y3i;
            x0i = y1i - y3r;
            a[idx3 + 2] = wd3r * x0r + wd3i * x0i;
            a[idx3 + 3] = wd3r * x0i - wd3i * x0r;
            j0 = m - j;
            j1 = j0 + m;
            j2 = j1 + m;
            j3 = j2 + m;
            idx0 = offa + j0;
            idx1 = offa + j1;
            idx2 = offa + j2;
            idx3 = offa + j3;
            x0r = a[idx0] + a[idx2];
            x0i = - a[idx0 + 1] - a[idx2 + 1];
            x1r = a[idx0] - a[idx2];
            x1i = - a[idx0 + 1] + a[idx2 + 1];
            y0r = a[idx0 - 2] + a[idx2 - 2];
            y0i = - a[idx0 - 1] - a[idx2 - 1];
            y1r = a[idx0 - 2] - a[idx2 - 2];
            y1i = - a[idx0 - 1] + a[idx2 - 1];
            x2r = a[idx1] + a[idx3];
            x2i = a[idx1 + 1] + a[idx3 + 1];
            x3r = a[idx1] - a[idx3];
            x3i = a[idx1 + 1] - a[idx3 + 1];
            y2r = a[idx1 - 2] + a[idx3 - 2];
            y2i = a[idx1 - 1] + a[idx3 - 1];
            y3r = a[idx1 - 2] - a[idx3 - 2];
            y3i = a[idx1 - 1] - a[idx3 - 1];
            a[idx0] = x0r + x2r;
            a[idx0 + 1] = x0i - x2i;
            a[idx0 - 2] = y0r + y2r;
            a[idx0 - 1] = y0i - y2i;
            a[idx1] = x0r - x2r;
            a[idx1 + 1] = x0i + x2i;
            a[idx1 - 2] = y0r - y2r;
            a[idx1 - 1] = y0i + y2i;
            x0r = x1r + x3i;
            x0i = x1i + x3r;
            a[idx2] = wk1i * x0r - wk1r * x0i;
            a[idx2 + 1] = wk1i * x0i + wk1r * x0r;
            x0r = y1r + y3i;
            x0i = y1i + y3r;
            a[idx2 - 2] = wd1i * x0r - wd1r * x0i;
            a[idx2 - 1] = wd1i * x0i + wd1r * x0r;
            x0r = x1r - x3i;
            x0i = x1i - x3r;
            a[idx3] = wk3i * x0r + wk3r * x0i;
            a[idx3 + 1] = wk3i * x0i - wk3r * x0r;
            x0r = y1r - y3i;
            x0i = y1i - y3r;
            a[idx3 - 2] = wd3i * x0r + wd3r * x0i;
            a[idx3 - 1] = wd3i * x0i - wd3r * x0r;
        }
        wk1r = csc1 * (wd1r + wn4r);
        wk1i = csc1 * (wd1i + wn4r);
        wk3r = csc3 * (wd3r - wn4r);
        wk3i = csc3 * (wd3i - wn4r);
        j0 = mh;
        j1 = j0 + m;
        j2 = j1 + m;
        j3 = j2 + m;
        idx0 = offa + j0;
        idx1 = offa + j1;
        idx2 = offa + j2;
        idx3 = offa + j3;
        x0r = a[idx0 - 2] + a[idx2 - 2];
        x0i = - a[idx0 - 1] - a[idx2 - 1];
        x1r = a[idx0 - 2] - a[idx2 - 2];
        x1i = - a[idx0 - 1] + a[idx2 - 1];
        x2r = a[idx1 - 2] + a[idx3 - 2];
        x2i = a[idx1 - 1] + a[idx3 - 1];
        x3r = a[idx1 - 2] - a[idx3 - 2];
        x3i = a[idx1 - 1] - a[idx3 - 1];
        a[idx0 - 2] = x0r + x2r;
        a[idx0 - 1] = x0i - x2i;
        a[idx1 - 2] = x0r - x2r;
        a[idx1 - 1] = x0i + x2i;
        x0r = x1r + x3i;
        x0i = x1i + x3r;
        a[idx2 - 2] = wk1r * x0r - wk1i * x0i;
        a[idx2 - 1] = wk1r * x0i + wk1i * x0r;
        x0r = x1r - x3i;
        x0i = x1i - x3r;
        a[idx3 - 2] = wk3r * x0r + wk3i * x0i;
        a[idx3 - 1] = wk3r * x0i - wk3i * x0r;
        x0r = a[idx0] + a[idx2];
        x0i = - a[idx0 + 1] - a[idx2 + 1];
        x1r = a[idx0] - a[idx2];
        x1i = - a[idx0 + 1] + a[idx2 + 1];
        x2r = a[idx1] + a[idx3];
        x2i = a[idx1 + 1] + a[idx3 + 1];
        x3r = a[idx1] - a[idx3];
        x3i = a[idx1 + 1] - a[idx3 + 1];
        a[idx0] = x0r + x2r;
        a[idx0 + 1] = x0i - x2i;
        a[idx1] = x0r - x2r;
        a[idx1 + 1] = x0i + x2i;
        x0r = x1r + x3i;
        x0i = x1i + x3r;
        a[idx2] = wn4r * (x0r - x0i);
        a[idx2 + 1] = wn4r * (x0i + x0r);
        x0r = x1r - x3i;
        x0i = x1i - x3r;
        a[idx3] = (- wn4r) * (x0r + x0i);
        a[idx3 + 1] = (- wn4r) * (x0i - x0r);
        x0r = a[idx0 + 2] + a[idx2 + 2];
        x0i = - a[idx0 + 3] - a[idx2 + 3];
        x1r = a[idx0 + 2] - a[idx2 + 2];
        x1i = - a[idx0 + 3] + a[idx2 + 3];
        x2r = a[idx1 + 2] + a[idx3 + 2];
        x2i = a[idx1 + 3] + a[idx3 + 3];
        x3r = a[idx1 + 2] - a[idx3 + 2];
        x3i = a[idx1 + 3] - a[idx3 + 3];
        a[idx0 + 2] = x0r + x2r;
        a[idx0 + 3] = x0i - x2i;
        a[idx1 + 2] = x0r - x2r;
        a[idx1 + 3] = x0i + x2i;
        x0r = x1r + x3i;
        x0i = x1i + x3r;
        a[idx2 + 2] = wk1i * x0r - wk1r * x0i;
        a[idx2 + 3] = wk1i * x0i + wk1r * x0r;
        x0r = x1r - x3i;
        x0i = x1i - x3r;
        a[idx3 + 2] = wk3i * x0r + wk3r * x0i;
        a[idx3 + 3] = wk3i * x0i - wk3r * x0r;
    }

    private void cftrec4(int n, double[] a, int offa, int nw, double[] w) {
        int m = n;
        int idx1 = offa + n;
        while (m > 512) {
            this.cftmdl1(m, a, idx1 - m, w, nw - ((m >>= 2) >> 1));
        }
        this.cftleaf(m, 1, a, idx1 - m, nw, w);
        int k = 0;
        int idx2 = offa - m;
        for (int j = n - m; j > 0; j -= m) {
            int isplt = this.cfttree(m, j, ++k, a, offa, nw, w);
            this.cftleaf(m, isplt, a, idx2 + j, nw, w);
        }
    }

    private int cfttree(int n, int j, int k, double[] a, int offa, int nw, double[] w) {
        int isplt;
        int idx1 = offa - n;
        if ((k & 3) != 0) {
            isplt = k & 1;
            if (isplt != 0) {
                this.cftmdl1(n, a, idx1 + j, w, nw - (n >> 1));
            } else {
                this.cftmdl2(n, a, idx1 + j, w, nw - n);
            }
        } else {
            int m = n;
            int i = k;
            while ((i & 3) == 0) {
                m <<= 2;
                i >>= 2;
            }
            isplt = i & 1;
            int idx2 = offa + j;
            if (isplt != 0) {
                while (m > 128) {
                    this.cftmdl1(m, a, idx2 - m, w, nw - (m >> 1));
                    m >>= 2;
                }
            } else {
                while (m > 128) {
                    this.cftmdl2(m, a, idx2 - m, w, nw - m);
                    m >>= 2;
                }
            }
        }
        return isplt;
    }

    private void cftleaf(int n, int isplt, double[] a, int offa, int nw, double[] w) {
        if (n == 512) {
            this.cftmdl1(128, a, offa, w, nw - 64);
            this.cftf161(a, offa, w, nw - 8);
            this.cftf162(a, offa + 32, w, nw - 32);
            this.cftf161(a, offa + 64, w, nw - 8);
            this.cftf161(a, offa + 96, w, nw - 8);
            this.cftmdl2(128, a, offa + 128, w, nw - 128);
            this.cftf161(a, offa + 128, w, nw - 8);
            this.cftf162(a, offa + 160, w, nw - 32);
            this.cftf161(a, offa + 192, w, nw - 8);
            this.cftf162(a, offa + 224, w, nw - 32);
            this.cftmdl1(128, a, offa + 256, w, nw - 64);
            this.cftf161(a, offa + 256, w, nw - 8);
            this.cftf162(a, offa + 288, w, nw - 32);
            this.cftf161(a, offa + 320, w, nw - 8);
            this.cftf161(a, offa + 352, w, nw - 8);
            if (isplt != 0) {
                this.cftmdl1(128, a, offa + 384, w, nw - 64);
                this.cftf161(a, offa + 480, w, nw - 8);
            } else {
                this.cftmdl2(128, a, offa + 384, w, nw - 128);
                this.cftf162(a, offa + 480, w, nw - 32);
            }
            this.cftf161(a, offa + 384, w, nw - 8);
            this.cftf162(a, offa + 416, w, nw - 32);
            this.cftf161(a, offa + 448, w, nw - 8);
        } else {
            this.cftmdl1(64, a, offa, w, nw - 32);
            this.cftf081(a, offa, w, nw - 8);
            this.cftf082(a, offa + 16, w, nw - 8);
            this.cftf081(a, offa + 32, w, nw - 8);
            this.cftf081(a, offa + 48, w, nw - 8);
            this.cftmdl2(64, a, offa + 64, w, nw - 64);
            this.cftf081(a, offa + 64, w, nw - 8);
            this.cftf082(a, offa + 80, w, nw - 8);
            this.cftf081(a, offa + 96, w, nw - 8);
            this.cftf082(a, offa + 112, w, nw - 8);
            this.cftmdl1(64, a, offa + 128, w, nw - 32);
            this.cftf081(a, offa + 128, w, nw - 8);
            this.cftf082(a, offa + 144, w, nw - 8);
            this.cftf081(a, offa + 160, w, nw - 8);
            this.cftf081(a, offa + 176, w, nw - 8);
            if (isplt != 0) {
                this.cftmdl1(64, a, offa + 192, w, nw - 32);
                this.cftf081(a, offa + 240, w, nw - 8);
            } else {
                this.cftmdl2(64, a, offa + 192, w, nw - 64);
                this.cftf082(a, offa + 240, w, nw - 8);
            }
            this.cftf081(a, offa + 192, w, nw - 8);
            this.cftf082(a, offa + 208, w, nw - 8);
            this.cftf081(a, offa + 224, w, nw - 8);
        }
    }

    private void cftmdl1(int n, double[] a, int offa, double[] w, int startw) {
        int j0;
        int idx0;
        int m;
        int mh = n >> 3;
        int j1 = m = 2 * mh;
        int j2 = j1 + m;
        int j3 = j2 + m;
        int idx1 = offa + j1;
        int idx2 = offa + j2;
        int idx3 = offa + j3;
        double x0r = a[offa] + a[idx2];
        double x0i = a[offa + 1] + a[idx2 + 1];
        double x1r = a[offa] - a[idx2];
        double x1i = a[offa + 1] - a[idx2 + 1];
        double x2r = a[idx1] + a[idx3];
        double x2i = a[idx1 + 1] + a[idx3 + 1];
        double x3r = a[idx1] - a[idx3];
        double x3i = a[idx1 + 1] - a[idx3 + 1];
        a[offa] = x0r + x2r;
        a[offa + 1] = x0i + x2i;
        a[idx1] = x0r - x2r;
        a[idx1 + 1] = x0i - x2i;
        a[idx2] = x1r - x3i;
        a[idx2 + 1] = x1i + x3r;
        a[idx3] = x1r + x3i;
        a[idx3 + 1] = x1i - x3r;
        double wn4r = w[startw + 1];
        int k = 0;
        for (int j = 2; j < mh; j += 2) {
            int idx4 = startw + (k += 4);
            double wk1r = w[idx4];
            double wk1i = w[idx4 + 1];
            double wk3r = w[idx4 + 2];
            double wk3i = w[idx4 + 3];
            j1 = j + m;
            j2 = j1 + m;
            j3 = j2 + m;
            idx1 = offa + j1;
            idx2 = offa + j2;
            idx3 = offa + j3;
            int idx5 = offa + j;
            x0r = a[idx5] + a[idx2];
            x0i = a[idx5 + 1] + a[idx2 + 1];
            x1r = a[idx5] - a[idx2];
            x1i = a[idx5 + 1] - a[idx2 + 1];
            x2r = a[idx1] + a[idx3];
            x2i = a[idx1 + 1] + a[idx3 + 1];
            x3r = a[idx1] - a[idx3];
            x3i = a[idx1 + 1] - a[idx3 + 1];
            a[idx5] = x0r + x2r;
            a[idx5 + 1] = x0i + x2i;
            a[idx1] = x0r - x2r;
            a[idx1 + 1] = x0i - x2i;
            x0r = x1r - x3i;
            x0i = x1i + x3r;
            a[idx2] = wk1r * x0r - wk1i * x0i;
            a[idx2 + 1] = wk1r * x0i + wk1i * x0r;
            x0r = x1r + x3i;
            x0i = x1i - x3r;
            a[idx3] = wk3r * x0r + wk3i * x0i;
            a[idx3 + 1] = wk3r * x0i - wk3i * x0r;
            j0 = m - j;
            j1 = j0 + m;
            j2 = j1 + m;
            j3 = j2 + m;
            idx0 = offa + j0;
            idx1 = offa + j1;
            idx2 = offa + j2;
            idx3 = offa + j3;
            x0r = a[idx0] + a[idx2];
            x0i = a[idx0 + 1] + a[idx2 + 1];
            x1r = a[idx0] - a[idx2];
            x1i = a[idx0 + 1] - a[idx2 + 1];
            x2r = a[idx1] + a[idx3];
            x2i = a[idx1 + 1] + a[idx3 + 1];
            x3r = a[idx1] - a[idx3];
            x3i = a[idx1 + 1] - a[idx3 + 1];
            a[idx0] = x0r + x2r;
            a[idx0 + 1] = x0i + x2i;
            a[idx1] = x0r - x2r;
            a[idx1 + 1] = x0i - x2i;
            x0r = x1r - x3i;
            x0i = x1i + x3r;
            a[idx2] = wk1i * x0r - wk1r * x0i;
            a[idx2 + 1] = wk1i * x0i + wk1r * x0r;
            x0r = x1r + x3i;
            x0i = x1i - x3r;
            a[idx3] = wk3i * x0r + wk3r * x0i;
            a[idx3 + 1] = wk3i * x0i - wk3r * x0r;
        }
        j0 = mh;
        j1 = j0 + m;
        j2 = j1 + m;
        j3 = j2 + m;
        idx0 = offa + j0;
        idx1 = offa + j1;
        idx2 = offa + j2;
        idx3 = offa + j3;
        x0r = a[idx0] + a[idx2];
        x0i = a[idx0 + 1] + a[idx2 + 1];
        x1r = a[idx0] - a[idx2];
        x1i = a[idx0 + 1] - a[idx2 + 1];
        x2r = a[idx1] + a[idx3];
        x2i = a[idx1 + 1] + a[idx3 + 1];
        x3r = a[idx1] - a[idx3];
        x3i = a[idx1 + 1] - a[idx3 + 1];
        a[idx0] = x0r + x2r;
        a[idx0 + 1] = x0i + x2i;
        a[idx1] = x0r - x2r;
        a[idx1 + 1] = x0i - x2i;
        x0r = x1r - x3i;
        x0i = x1i + x3r;
        a[idx2] = wn4r * (x0r - x0i);
        a[idx2 + 1] = wn4r * (x0i + x0r);
        x0r = x1r + x3i;
        x0i = x1i - x3r;
        a[idx3] = (- wn4r) * (x0r + x0i);
        a[idx3 + 1] = (- wn4r) * (x0i - x0r);
    }

    private void cftmdl2(int n, double[] a, int offa, double[] w, int startw) {
        int idx0;
        double y2i;
        double y2r;
        int j0;
        double wk1r;
        double wk1i;
        int mh = n >> 3;
        int m = 2 * mh;
        double wn4r = w[startw + 1];
        int j1 = m;
        int j2 = j1 + m;
        int j3 = j2 + m;
        int idx1 = offa + j1;
        int idx2 = offa + j2;
        int idx3 = offa + j3;
        double x0r = a[offa] - a[idx2 + 1];
        double x0i = a[offa + 1] + a[idx2];
        double x1r = a[offa] + a[idx2 + 1];
        double x1i = a[offa + 1] - a[idx2];
        double x2r = a[idx1] - a[idx3 + 1];
        double x2i = a[idx1 + 1] + a[idx3];
        double x3r = a[idx1] + a[idx3 + 1];
        double x3i = a[idx1 + 1] - a[idx3];
        double y0r = wn4r * (x2r - x2i);
        double y0i = wn4r * (x2i + x2r);
        a[offa] = x0r + y0r;
        a[offa + 1] = x0i + y0i;
        a[idx1] = x0r - y0r;
        a[idx1 + 1] = x0i - y0i;
        y0r = wn4r * (x3r - x3i);
        y0i = wn4r * (x3i + x3r);
        a[idx2] = x1r - y0i;
        a[idx2 + 1] = x1i + y0r;
        a[idx3] = x1r + y0i;
        a[idx3 + 1] = x1i - y0r;
        int k = 0;
        int kr = 2 * m;
        for (int j = 2; j < mh; j += 2) {
            int idx4 = startw + (k += 4);
            wk1r = w[idx4];
            wk1i = w[idx4 + 1];
            double wk3r = w[idx4 + 2];
            double wk3i = w[idx4 + 3];
            int idx5 = startw + (kr -= 4);
            double wd1i = w[idx5];
            double wd1r = w[idx5 + 1];
            double wd3i = w[idx5 + 2];
            double wd3r = w[idx5 + 3];
            j1 = j + m;
            j2 = j1 + m;
            j3 = j2 + m;
            idx1 = offa + j1;
            idx2 = offa + j2;
            idx3 = offa + j3;
            int idx6 = offa + j;
            x0r = a[idx6] - a[idx2 + 1];
            x0i = a[idx6 + 1] + a[idx2];
            x1r = a[idx6] + a[idx2 + 1];
            x1i = a[idx6 + 1] - a[idx2];
            x2r = a[idx1] - a[idx3 + 1];
            x2i = a[idx1 + 1] + a[idx3];
            x3r = a[idx1] + a[idx3 + 1];
            x3i = a[idx1 + 1] - a[idx3];
            y0r = wk1r * x0r - wk1i * x0i;
            y0i = wk1r * x0i + wk1i * x0r;
            y2r = wd1r * x2r - wd1i * x2i;
            y2i = wd1r * x2i + wd1i * x2r;
            a[idx6] = y0r + y2r;
            a[idx6 + 1] = y0i + y2i;
            a[idx1] = y0r - y2r;
            a[idx1 + 1] = y0i - y2i;
            y0r = wk3r * x1r + wk3i * x1i;
            y0i = wk3r * x1i - wk3i * x1r;
            y2r = wd3r * x3r + wd3i * x3i;
            y2i = wd3r * x3i - wd3i * x3r;
            a[idx2] = y0r + y2r;
            a[idx2 + 1] = y0i + y2i;
            a[idx3] = y0r - y2r;
            a[idx3 + 1] = y0i - y2i;
            j0 = m - j;
            j1 = j0 + m;
            j2 = j1 + m;
            j3 = j2 + m;
            idx0 = offa + j0;
            idx1 = offa + j1;
            idx2 = offa + j2;
            idx3 = offa + j3;
            x0r = a[idx0] - a[idx2 + 1];
            x0i = a[idx0 + 1] + a[idx2];
            x1r = a[idx0] + a[idx2 + 1];
            x1i = a[idx0 + 1] - a[idx2];
            x2r = a[idx1] - a[idx3 + 1];
            x2i = a[idx1 + 1] + a[idx3];
            x3r = a[idx1] + a[idx3 + 1];
            x3i = a[idx1 + 1] - a[idx3];
            y0r = wd1i * x0r - wd1r * x0i;
            y0i = wd1i * x0i + wd1r * x0r;
            y2r = wk1i * x2r - wk1r * x2i;
            y2i = wk1i * x2i + wk1r * x2r;
            a[idx0] = y0r + y2r;
            a[idx0 + 1] = y0i + y2i;
            a[idx1] = y0r - y2r;
            a[idx1 + 1] = y0i - y2i;
            y0r = wd3i * x1r + wd3r * x1i;
            y0i = wd3i * x1i - wd3r * x1r;
            y2r = wk3i * x3r + wk3r * x3i;
            y2i = wk3i * x3i - wk3r * x3r;
            a[idx2] = y0r + y2r;
            a[idx2 + 1] = y0i + y2i;
            a[idx3] = y0r - y2r;
            a[idx3 + 1] = y0i - y2i;
        }
        wk1r = w[startw + m];
        wk1i = w[startw + m + 1];
        j0 = mh;
        j1 = j0 + m;
        j2 = j1 + m;
        j3 = j2 + m;
        idx0 = offa + j0;
        idx1 = offa + j1;
        idx2 = offa + j2;
        idx3 = offa + j3;
        x0r = a[idx0] - a[idx2 + 1];
        x0i = a[idx0 + 1] + a[idx2];
        x1r = a[idx0] + a[idx2 + 1];
        x1i = a[idx0 + 1] - a[idx2];
        x2r = a[idx1] - a[idx3 + 1];
        x2i = a[idx1 + 1] + a[idx3];
        x3r = a[idx1] + a[idx3 + 1];
        x3i = a[idx1 + 1] - a[idx3];
        y0r = wk1r * x0r - wk1i * x0i;
        y0i = wk1r * x0i + wk1i * x0r;
        y2r = wk1i * x2r - wk1r * x2i;
        y2i = wk1i * x2i + wk1r * x2r;
        a[idx0] = y0r + y2r;
        a[idx0 + 1] = y0i + y2i;
        a[idx1] = y0r - y2r;
        a[idx1 + 1] = y0i - y2i;
        y0r = wk1i * x1r - wk1r * x1i;
        y0i = wk1i * x1i + wk1r * x1r;
        y2r = wk1r * x3r - wk1i * x3i;
        y2i = wk1r * x3i + wk1i * x3r;
        a[idx2] = y0r - y2r;
        a[idx2 + 1] = y0i - y2i;
        a[idx3] = y0r + y2r;
        a[idx3 + 1] = y0i + y2i;
    }

    private void cftfx41(int n, double[] a, int offa, int nw, double[] w) {
        if (n == 128) {
            this.cftf161(a, offa, w, nw - 8);
            this.cftf162(a, offa + 32, w, nw - 32);
            this.cftf161(a, offa + 64, w, nw - 8);
            this.cftf161(a, offa + 96, w, nw - 8);
        } else {
            this.cftf081(a, offa, w, nw - 8);
            this.cftf082(a, offa + 16, w, nw - 8);
            this.cftf081(a, offa + 32, w, nw - 8);
            this.cftf081(a, offa + 48, w, nw - 8);
        }
    }

    private void cftf161(double[] a, int offa, double[] w, int startw) {
        double wn4r = w[startw + 1];
        double wk1r = w[startw + 2];
        double wk1i = w[startw + 3];
        double x0r = a[offa] + a[offa + 16];
        double x0i = a[offa + 1] + a[offa + 17];
        double x1r = a[offa] - a[offa + 16];
        double x1i = a[offa + 1] - a[offa + 17];
        double x2r = a[offa + 8] + a[offa + 24];
        double x2i = a[offa + 9] + a[offa + 25];
        double x3r = a[offa + 8] - a[offa + 24];
        double x3i = a[offa + 9] - a[offa + 25];
        double y0r = x0r + x2r;
        double y0i = x0i + x2i;
        double y4r = x0r - x2r;
        double y4i = x0i - x2i;
        double y8r = x1r - x3i;
        double y8i = x1i + x3r;
        double y12r = x1r + x3i;
        double y12i = x1i - x3r;
        x0r = a[offa + 2] + a[offa + 18];
        x0i = a[offa + 3] + a[offa + 19];
        x1r = a[offa + 2] - a[offa + 18];
        x1i = a[offa + 3] - a[offa + 19];
        x2r = a[offa + 10] + a[offa + 26];
        x2i = a[offa + 11] + a[offa + 27];
        x3r = a[offa + 10] - a[offa + 26];
        x3i = a[offa + 11] - a[offa + 27];
        double y1r = x0r + x2r;
        double y1i = x0i + x2i;
        double y5r = x0r - x2r;
        double y5i = x0i - x2i;
        x0r = x1r - x3i;
        x0i = x1i + x3r;
        double y9r = wk1r * x0r - wk1i * x0i;
        double y9i = wk1r * x0i + wk1i * x0r;
        x0r = x1r + x3i;
        x0i = x1i - x3r;
        double y13r = wk1i * x0r - wk1r * x0i;
        double y13i = wk1i * x0i + wk1r * x0r;
        x0r = a[offa + 4] + a[offa + 20];
        x0i = a[offa + 5] + a[offa + 21];
        x1r = a[offa + 4] - a[offa + 20];
        x1i = a[offa + 5] - a[offa + 21];
        x2r = a[offa + 12] + a[offa + 28];
        x2i = a[offa + 13] + a[offa + 29];
        x3r = a[offa + 12] - a[offa + 28];
        x3i = a[offa + 13] - a[offa + 29];
        double y2r = x0r + x2r;
        double y2i = x0i + x2i;
        double y6r = x0r - x2r;
        double y6i = x0i - x2i;
        x0r = x1r - x3i;
        x0i = x1i + x3r;
        double y10r = wn4r * (x0r - x0i);
        double y10i = wn4r * (x0i + x0r);
        x0r = x1r + x3i;
        x0i = x1i - x3r;
        double y14r = wn4r * (x0r + x0i);
        double y14i = wn4r * (x0i - x0r);
        x0r = a[offa + 6] + a[offa + 22];
        x0i = a[offa + 7] + a[offa + 23];
        x1r = a[offa + 6] - a[offa + 22];
        x1i = a[offa + 7] - a[offa + 23];
        x2r = a[offa + 14] + a[offa + 30];
        x2i = a[offa + 15] + a[offa + 31];
        x3r = a[offa + 14] - a[offa + 30];
        x3i = a[offa + 15] - a[offa + 31];
        double y3r = x0r + x2r;
        double y3i = x0i + x2i;
        double y7r = x0r - x2r;
        double y7i = x0i - x2i;
        x0r = x1r - x3i;
        x0i = x1i + x3r;
        double y11r = wk1i * x0r - wk1r * x0i;
        double y11i = wk1i * x0i + wk1r * x0r;
        x0r = x1r + x3i;
        x0i = x1i - x3r;
        double y15r = wk1r * x0r - wk1i * x0i;
        double y15i = wk1r * x0i + wk1i * x0r;
        x0r = y12r - y14r;
        x0i = y12i - y14i;
        x1r = y12r + y14r;
        x1i = y12i + y14i;
        x2r = y13r - y15r;
        x2i = y13i - y15i;
        x3r = y13r + y15r;
        x3i = y13i + y15i;
        a[offa + 24] = x0r + x2r;
        a[offa + 25] = x0i + x2i;
        a[offa + 26] = x0r - x2r;
        a[offa + 27] = x0i - x2i;
        a[offa + 28] = x1r - x3i;
        a[offa + 29] = x1i + x3r;
        a[offa + 30] = x1r + x3i;
        a[offa + 31] = x1i - x3r;
        x0r = y8r + y10r;
        x0i = y8i + y10i;
        x1r = y8r - y10r;
        x1i = y8i - y10i;
        x2r = y9r + y11r;
        x2i = y9i + y11i;
        x3r = y9r - y11r;
        x3i = y9i - y11i;
        a[offa + 16] = x0r + x2r;
        a[offa + 17] = x0i + x2i;
        a[offa + 18] = x0r - x2r;
        a[offa + 19] = x0i - x2i;
        a[offa + 20] = x1r - x3i;
        a[offa + 21] = x1i + x3r;
        a[offa + 22] = x1r + x3i;
        a[offa + 23] = x1i - x3r;
        x0r = y5r - y7i;
        x0i = y5i + y7r;
        x2r = wn4r * (x0r - x0i);
        x2i = wn4r * (x0i + x0r);
        x0r = y5r + y7i;
        x0i = y5i - y7r;
        x3r = wn4r * (x0r - x0i);
        x3i = wn4r * (x0i + x0r);
        x0r = y4r - y6i;
        x0i = y4i + y6r;
        x1r = y4r + y6i;
        x1i = y4i - y6r;
        a[offa + 8] = x0r + x2r;
        a[offa + 9] = x0i + x2i;
        a[offa + 10] = x0r - x2r;
        a[offa + 11] = x0i - x2i;
        a[offa + 12] = x1r - x3i;
        a[offa + 13] = x1i + x3r;
        a[offa + 14] = x1r + x3i;
        a[offa + 15] = x1i - x3r;
        x0r = y0r + y2r;
        x0i = y0i + y2i;
        x1r = y0r - y2r;
        x1i = y0i - y2i;
        x2r = y1r + y3r;
        x2i = y1i + y3i;
        x3r = y1r - y3r;
        x3i = y1i - y3i;
        a[offa] = x0r + x2r;
        a[offa + 1] = x0i + x2i;
        a[offa + 2] = x0r - x2r;
        a[offa + 3] = x0i - x2i;
        a[offa + 4] = x1r - x3i;
        a[offa + 5] = x1i + x3r;
        a[offa + 6] = x1r + x3i;
        a[offa + 7] = x1i - x3r;
    }

    private void cftf162(double[] a, int offa, double[] w, int startw) {
        double wn4r = w[startw + 1];
        double wk1r = w[startw + 4];
        double wk1i = w[startw + 5];
        double wk3r = w[startw + 6];
        double wk3i = - w[startw + 7];
        double wk2r = w[startw + 8];
        double wk2i = w[startw + 9];
        double x1r = a[offa] - a[offa + 17];
        double x1i = a[offa + 1] + a[offa + 16];
        double x0r = a[offa + 8] - a[offa + 25];
        double x0i = a[offa + 9] + a[offa + 24];
        double x2r = wn4r * (x0r - x0i);
        double x2i = wn4r * (x0i + x0r);
        double y0r = x1r + x2r;
        double y0i = x1i + x2i;
        double y4r = x1r - x2r;
        double y4i = x1i - x2i;
        x1r = a[offa] + a[offa + 17];
        x1i = a[offa + 1] - a[offa + 16];
        x0r = a[offa + 8] + a[offa + 25];
        x0i = a[offa + 9] - a[offa + 24];
        x2r = wn4r * (x0r - x0i);
        x2i = wn4r * (x0i + x0r);
        double y8r = x1r - x2i;
        double y8i = x1i + x2r;
        double y12r = x1r + x2i;
        double y12i = x1i - x2r;
        x0r = a[offa + 2] - a[offa + 19];
        x0i = a[offa + 3] + a[offa + 18];
        x1r = wk1r * x0r - wk1i * x0i;
        x1i = wk1r * x0i + wk1i * x0r;
        x0r = a[offa + 10] - a[offa + 27];
        x0i = a[offa + 11] + a[offa + 26];
        x2r = wk3i * x0r - wk3r * x0i;
        x2i = wk3i * x0i + wk3r * x0r;
        double y1r = x1r + x2r;
        double y1i = x1i + x2i;
        double y5r = x1r - x2r;
        double y5i = x1i - x2i;
        x0r = a[offa + 2] + a[offa + 19];
        x0i = a[offa + 3] - a[offa + 18];
        x1r = wk3r * x0r - wk3i * x0i;
        x1i = wk3r * x0i + wk3i * x0r;
        x0r = a[offa + 10] + a[offa + 27];
        x0i = a[offa + 11] - a[offa + 26];
        x2r = wk1r * x0r + wk1i * x0i;
        x2i = wk1r * x0i - wk1i * x0r;
        double y9r = x1r - x2r;
        double y9i = x1i - x2i;
        double y13r = x1r + x2r;
        double y13i = x1i + x2i;
        x0r = a[offa + 4] - a[offa + 21];
        x0i = a[offa + 5] + a[offa + 20];
        x1r = wk2r * x0r - wk2i * x0i;
        x1i = wk2r * x0i + wk2i * x0r;
        x0r = a[offa + 12] - a[offa + 29];
        x0i = a[offa + 13] + a[offa + 28];
        x2r = wk2i * x0r - wk2r * x0i;
        x2i = wk2i * x0i + wk2r * x0r;
        double y2r = x1r + x2r;
        double y2i = x1i + x2i;
        double y6r = x1r - x2r;
        double y6i = x1i - x2i;
        x0r = a[offa + 4] + a[offa + 21];
        x0i = a[offa + 5] - a[offa + 20];
        x1r = wk2i * x0r - wk2r * x0i;
        x1i = wk2i * x0i + wk2r * x0r;
        x0r = a[offa + 12] + a[offa + 29];
        x0i = a[offa + 13] - a[offa + 28];
        x2r = wk2r * x0r - wk2i * x0i;
        x2i = wk2r * x0i + wk2i * x0r;
        double y10r = x1r - x2r;
        double y10i = x1i - x2i;
        double y14r = x1r + x2r;
        double y14i = x1i + x2i;
        x0r = a[offa + 6] - a[offa + 23];
        x0i = a[offa + 7] + a[offa + 22];
        x1r = wk3r * x0r - wk3i * x0i;
        x1i = wk3r * x0i + wk3i * x0r;
        x0r = a[offa + 14] - a[offa + 31];
        x0i = a[offa + 15] + a[offa + 30];
        x2r = wk1i * x0r - wk1r * x0i;
        x2i = wk1i * x0i + wk1r * x0r;
        double y3r = x1r + x2r;
        double y3i = x1i + x2i;
        double y7r = x1r - x2r;
        double y7i = x1i - x2i;
        x0r = a[offa + 6] + a[offa + 23];
        x0i = a[offa + 7] - a[offa + 22];
        x1r = wk1i * x0r + wk1r * x0i;
        x1i = wk1i * x0i - wk1r * x0r;
        x0r = a[offa + 14] + a[offa + 31];
        x0i = a[offa + 15] - a[offa + 30];
        x2r = wk3i * x0r - wk3r * x0i;
        x2i = wk3i * x0i + wk3r * x0r;
        double y11r = x1r + x2r;
        double y11i = x1i + x2i;
        double y15r = x1r - x2r;
        double y15i = x1i - x2i;
        x1r = y0r + y2r;
        x1i = y0i + y2i;
        x2r = y1r + y3r;
        x2i = y1i + y3i;
        a[offa] = x1r + x2r;
        a[offa + 1] = x1i + x2i;
        a[offa + 2] = x1r - x2r;
        a[offa + 3] = x1i - x2i;
        x1r = y0r - y2r;
        x1i = y0i - y2i;
        x2r = y1r - y3r;
        x2i = y1i - y3i;
        a[offa + 4] = x1r - x2i;
        a[offa + 5] = x1i + x2r;
        a[offa + 6] = x1r + x2i;
        a[offa + 7] = x1i - x2r;
        x1r = y4r - y6i;
        x1i = y4i + y6r;
        x0r = y5r - y7i;
        x0i = y5i + y7r;
        x2r = wn4r * (x0r - x0i);
        x2i = wn4r * (x0i + x0r);
        a[offa + 8] = x1r + x2r;
        a[offa + 9] = x1i + x2i;
        a[offa + 10] = x1r - x2r;
        a[offa + 11] = x1i - x2i;
        x1r = y4r + y6i;
        x1i = y4i - y6r;
        x0r = y5r + y7i;
        x0i = y5i - y7r;
        x2r = wn4r * (x0r - x0i);
        x2i = wn4r * (x0i + x0r);
        a[offa + 12] = x1r - x2i;
        a[offa + 13] = x1i + x2r;
        a[offa + 14] = x1r + x2i;
        a[offa + 15] = x1i - x2r;
        x1r = y8r + y10r;
        x1i = y8i + y10i;
        x2r = y9r - y11r;
        x2i = y9i - y11i;
        a[offa + 16] = x1r + x2r;
        a[offa + 17] = x1i + x2i;
        a[offa + 18] = x1r - x2r;
        a[offa + 19] = x1i - x2i;
        x1r = y8r - y10r;
        x1i = y8i - y10i;
        x2r = y9r + y11r;
        x2i = y9i + y11i;
        a[offa + 20] = x1r - x2i;
        a[offa + 21] = x1i + x2r;
        a[offa + 22] = x1r + x2i;
        a[offa + 23] = x1i - x2r;
        x1r = y12r - y14i;
        x1i = y12i + y14r;
        x0r = y13r + y15i;
        x0i = y13i - y15r;
        x2r = wn4r * (x0r - x0i);
        x2i = wn4r * (x0i + x0r);
        a[offa + 24] = x1r + x2r;
        a[offa + 25] = x1i + x2i;
        a[offa + 26] = x1r - x2r;
        a[offa + 27] = x1i - x2i;
        x1r = y12r + y14i;
        x1i = y12i - y14r;
        x0r = y13r - y15i;
        x0i = y13i + y15r;
        x2r = wn4r * (x0r - x0i);
        x2i = wn4r * (x0i + x0r);
        a[offa + 28] = x1r - x2i;
        a[offa + 29] = x1i + x2r;
        a[offa + 30] = x1r + x2i;
        a[offa + 31] = x1i - x2r;
    }

    private void cftf081(double[] a, int offa, double[] w, int startw) {
        double wn4r = w[startw + 1];
        double x0r = a[offa] + a[offa + 8];
        double x0i = a[offa + 1] + a[offa + 9];
        double x1r = a[offa] - a[offa + 8];
        double x1i = a[offa + 1] - a[offa + 9];
        double x2r = a[offa + 4] + a[offa + 12];
        double x2i = a[offa + 5] + a[offa + 13];
        double x3r = a[offa + 4] - a[offa + 12];
        double x3i = a[offa + 5] - a[offa + 13];
        double y0r = x0r + x2r;
        double y0i = x0i + x2i;
        double y2r = x0r - x2r;
        double y2i = x0i - x2i;
        double y1r = x1r - x3i;
        double y1i = x1i + x3r;
        double y3r = x1r + x3i;
        double y3i = x1i - x3r;
        x0r = a[offa + 2] + a[offa + 10];
        x0i = a[offa + 3] + a[offa + 11];
        x1r = a[offa + 2] - a[offa + 10];
        x1i = a[offa + 3] - a[offa + 11];
        x2r = a[offa + 6] + a[offa + 14];
        x2i = a[offa + 7] + a[offa + 15];
        x3r = a[offa + 6] - a[offa + 14];
        x3i = a[offa + 7] - a[offa + 15];
        double y4r = x0r + x2r;
        double y4i = x0i + x2i;
        double y6r = x0r - x2r;
        double y6i = x0i - x2i;
        x0r = x1r - x3i;
        x0i = x1i + x3r;
        x2r = x1r + x3i;
        x2i = x1i - x3r;
        double y5r = wn4r * (x0r - x0i);
        double y5i = wn4r * (x0r + x0i);
        double y7r = wn4r * (x2r - x2i);
        double y7i = wn4r * (x2r + x2i);
        a[offa + 8] = y1r + y5r;
        a[offa + 9] = y1i + y5i;
        a[offa + 10] = y1r - y5r;
        a[offa + 11] = y1i - y5i;
        a[offa + 12] = y3r - y7i;
        a[offa + 13] = y3i + y7r;
        a[offa + 14] = y3r + y7i;
        a[offa + 15] = y3i - y7r;
        a[offa] = y0r + y4r;
        a[offa + 1] = y0i + y4i;
        a[offa + 2] = y0r - y4r;
        a[offa + 3] = y0i - y4i;
        a[offa + 4] = y2r - y6i;
        a[offa + 5] = y2i + y6r;
        a[offa + 6] = y2r + y6i;
        a[offa + 7] = y2i - y6r;
    }

    private void cftf082(double[] a, int offa, double[] w, int startw) {
        double wn4r = w[startw + 1];
        double wk1r = w[startw + 2];
        double wk1i = w[startw + 3];
        double y0r = a[offa] - a[offa + 9];
        double y0i = a[offa + 1] + a[offa + 8];
        double y1r = a[offa] + a[offa + 9];
        double y1i = a[offa + 1] - a[offa + 8];
        double x0r = a[offa + 4] - a[offa + 13];
        double x0i = a[offa + 5] + a[offa + 12];
        double y2r = wn4r * (x0r - x0i);
        double y2i = wn4r * (x0i + x0r);
        x0r = a[offa + 4] + a[offa + 13];
        x0i = a[offa + 5] - a[offa + 12];
        double y3r = wn4r * (x0r - x0i);
        double y3i = wn4r * (x0i + x0r);
        x0r = a[offa + 2] - a[offa + 11];
        x0i = a[offa + 3] + a[offa + 10];
        double y4r = wk1r * x0r - wk1i * x0i;
        double y4i = wk1r * x0i + wk1i * x0r;
        x0r = a[offa + 2] + a[offa + 11];
        x0i = a[offa + 3] - a[offa + 10];
        double y5r = wk1i * x0r - wk1r * x0i;
        double y5i = wk1i * x0i + wk1r * x0r;
        x0r = a[offa + 6] - a[offa + 15];
        x0i = a[offa + 7] + a[offa + 14];
        double y6r = wk1i * x0r - wk1r * x0i;
        double y6i = wk1i * x0i + wk1r * x0r;
        x0r = a[offa + 6] + a[offa + 15];
        x0i = a[offa + 7] - a[offa + 14];
        double y7r = wk1r * x0r - wk1i * x0i;
        double y7i = wk1r * x0i + wk1i * x0r;
        x0r = y0r + y2r;
        x0i = y0i + y2i;
        double x1r = y4r + y6r;
        double x1i = y4i + y6i;
        a[offa] = x0r + x1r;
        a[offa + 1] = x0i + x1i;
        a[offa + 2] = x0r - x1r;
        a[offa + 3] = x0i - x1i;
        x0r = y0r - y2r;
        x0i = y0i - y2i;
        x1r = y4r - y6r;
        x1i = y4i - y6i;
        a[offa + 4] = x0r - x1i;
        a[offa + 5] = x0i + x1r;
        a[offa + 6] = x0r + x1i;
        a[offa + 7] = x0i - x1r;
        x0r = y1r - y3i;
        x0i = y1i + y3r;
        x1r = y5r - y7r;
        x1i = y5i - y7i;
        a[offa + 8] = x0r + x1r;
        a[offa + 9] = x0i + x1i;
        a[offa + 10] = x0r - x1r;
        a[offa + 11] = x0i - x1i;
        x0r = y1r + y3i;
        x0i = y1i - y3r;
        x1r = y5r + y7r;
        x1i = y5i + y7i;
        a[offa + 12] = x0r - x1i;
        a[offa + 13] = x0i + x1r;
        a[offa + 14] = x0r + x1i;
        a[offa + 15] = x0i - x1r;
    }

    private void cftf040(double[] a, int offa) {
        double x0r = a[offa] + a[offa + 4];
        double x0i = a[offa + 1] + a[offa + 5];
        double x1r = a[offa] - a[offa + 4];
        double x1i = a[offa + 1] - a[offa + 5];
        double x2r = a[offa + 2] + a[offa + 6];
        double x2i = a[offa + 3] + a[offa + 7];
        double x3r = a[offa + 2] - a[offa + 6];
        double x3i = a[offa + 3] - a[offa + 7];
        a[offa] = x0r + x2r;
        a[offa + 1] = x0i + x2i;
        a[offa + 2] = x1r - x3i;
        a[offa + 3] = x1i + x3r;
        a[offa + 4] = x0r - x2r;
        a[offa + 5] = x0i - x2i;
        a[offa + 6] = x1r + x3i;
        a[offa + 7] = x1i - x3r;
    }

    private void cftb040(double[] a, int offa) {
        double x0r = a[offa] + a[offa + 4];
        double x0i = a[offa + 1] + a[offa + 5];
        double x1r = a[offa] - a[offa + 4];
        double x1i = a[offa + 1] - a[offa + 5];
        double x2r = a[offa + 2] + a[offa + 6];
        double x2i = a[offa + 3] + a[offa + 7];
        double x3r = a[offa + 2] - a[offa + 6];
        double x3i = a[offa + 3] - a[offa + 7];
        a[offa] = x0r + x2r;
        a[offa + 1] = x0i + x2i;
        a[offa + 2] = x1r + x3i;
        a[offa + 3] = x1i - x3r;
        a[offa + 4] = x0r - x2r;
        a[offa + 5] = x0i - x2i;
        a[offa + 6] = x1r - x3i;
        a[offa + 7] = x1i + x3r;
    }

    private void cftx020(double[] a, int offa) {
        double x0r = a[offa] - a[offa + 2];
        double x0i = - a[offa + 1] + a[offa + 3];
        double[] arrd = a;
        int n = offa;
        arrd[n] = arrd[n] + a[offa + 2];
        double[] arrd2 = a;
        int n2 = offa + 1;
        arrd2[n2] = arrd2[n2] + a[offa + 3];
        a[offa + 2] = x0r;
        a[offa + 3] = x0i;
    }

    private void cftxb020(double[] a, int offa) {
        double x0r = a[offa] - a[offa + 2];
        double x0i = a[offa + 1] - a[offa + 3];
        double[] arrd = a;
        int n = offa;
        arrd[n] = arrd[n] + a[offa + 2];
        double[] arrd2 = a;
        int n2 = offa + 1;
        arrd2[n2] = arrd2[n2] + a[offa + 3];
        a[offa + 2] = x0r;
        a[offa + 3] = x0i;
    }

    private void cftxc020(double[] a, int offa) {
        double x0r = a[offa] - a[offa + 2];
        double x0i = a[offa + 1] + a[offa + 3];
        double[] arrd = a;
        int n = offa;
        arrd[n] = arrd[n] + a[offa + 2];
        double[] arrd2 = a;
        int n2 = offa + 1;
        arrd2[n2] = arrd2[n2] - a[offa + 3];
        a[offa + 2] = x0r;
        a[offa + 3] = x0i;
    }

    private void rftfsub(int n, double[] a, int offa, int nc, double[] c, int startc) {
        int m = n >> 1;
        int ks = 2 * nc / m;
        int kk = 0;
        for (int j = 2; j < m; j += 2) {
            int k = n - j;
            double wkr = 0.5 - c[startc + nc - (kk += ks)];
            double wki = c[startc + kk];
            int idx1 = offa + j;
            int idx2 = offa + k;
            double xr = a[idx1] - a[idx2];
            double xi = a[idx1 + 1] + a[idx2 + 1];
            double yr = wkr * xr - wki * xi;
            double yi = wkr * xi + wki * xr;
            double[] arrd = a;
            int n2 = idx1;
            arrd[n2] = arrd[n2] - yr;
            a[idx1 + 1] = yi - a[idx1 + 1];
            double[] arrd2 = a;
            int n3 = idx2;
            arrd2[n3] = arrd2[n3] + yr;
            a[idx2 + 1] = yi - a[idx2 + 1];
        }
        a[offa + m + 1] = - a[offa + m + 1];
    }

    private void rftbsub(int n, double[] a, int offa, int nc, double[] c, int startc) {
        int m = n >> 1;
        int ks = 2 * nc / m;
        int kk = 0;
        for (int j = 2; j < m; j += 2) {
            int k = n - j;
            double wkr = 0.5 - c[startc + nc - (kk += ks)];
            double wki = c[startc + kk];
            int idx1 = offa + j;
            int idx2 = offa + k;
            double xr = a[idx1] - a[idx2];
            double xi = a[idx1 + 1] + a[idx2 + 1];
            double yr = wkr * xr - wki * xi;
            double yi = wkr * xi + wki * xr;
            double[] arrd = a;
            int n2 = idx1;
            arrd[n2] = arrd[n2] - yr;
            double[] arrd2 = a;
            int n3 = idx1 + 1;
            arrd2[n3] = arrd2[n3] - yi;
            double[] arrd3 = a;
            int n4 = idx2;
            arrd3[n4] = arrd3[n4] + yr;
            double[] arrd4 = a;
            int n5 = idx2 + 1;
            arrd4[n5] = arrd4[n5] - yi;
        }
    }

    private void scale(double m, double[] a, int offa, boolean complex) {
        double norm = 1.0 / m;
        int n2 = complex ? 2 * this.n : this.n;
        int i = offa;
        while (i < offa + n2) {
            double[] arrd = a;
            int n = i++;
            arrd[n] = arrd[n] * norm;
        }
    }

    private static enum Plans {
        SPLIT_RADIX,
        MIXED_RADIX,
        BLUESTEIN;
        

        private Plans() {
        }
    }

}

