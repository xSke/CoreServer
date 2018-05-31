/*
 * Decompiled with CFR 0_129.
 */
package mikera.vectorz.functions;

import mikera.transformz.ATransform;
import mikera.vectorz.AVector;

public abstract class ScalarFunction
extends ATransform {
    public abstract double calculate(AVector var1);

    @Override
    public int outputDimensions() {
        return 1;
    }

    @Override
    public void transform(AVector src, AVector dest) {
        assert (dest.length() == 1);
        dest.set(0, this.calculate(src));
    }
}

