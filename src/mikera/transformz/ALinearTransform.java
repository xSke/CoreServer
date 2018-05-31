/*
 * Decompiled with CFR 0_129.
 */
package mikera.transformz;

import mikera.transformz.AAffineTransform;
import mikera.transformz.ATranslation;
import mikera.transformz.Transformz;
import mikera.transformz.impl.IdentityTranslation;

public abstract class ALinearTransform
extends AAffineTransform {
    @Override
    public boolean isLinear() {
        return true;
    }

    @Override
    public IdentityTranslation getTranslation() {
        return Transformz.identityTranslation(this.outputDimensions());
    }
}

