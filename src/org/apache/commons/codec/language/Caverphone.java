/*
 * Decompiled with CFR 0_129.
 */
package org.apache.commons.codec.language;

import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.StringEncoder;
import org.apache.commons.codec.language.Caverphone2;

public class Caverphone
implements StringEncoder {
    private final Caverphone2 encoder = new Caverphone2();

    public String caverphone(String source) {
        return this.encoder.encode(source);
    }

    public Object encode(Object pObject) throws EncoderException {
        if (!(pObject instanceof String)) {
            throw new EncoderException("Parameter supplied to Caverphone encode is not of type java.lang.String");
        }
        return this.caverphone((String)pObject);
    }

    public String encode(String pString) {
        return this.caverphone(pString);
    }

    public boolean isCaverphoneEqual(String str1, String str2) {
        return this.caverphone(str1).equals(this.caverphone(str2));
    }
}

