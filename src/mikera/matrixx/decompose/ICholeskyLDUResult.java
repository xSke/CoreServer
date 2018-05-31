/*
 * Decompiled with CFR 0_129.
 */
package mikera.matrixx.decompose;

import mikera.matrixx.decompose.ICholeskyResult;
import mikera.matrixx.impl.ADiagonalMatrix;

public interface ICholeskyLDUResult
extends ICholeskyResult {
    public ADiagonalMatrix getD();
}

