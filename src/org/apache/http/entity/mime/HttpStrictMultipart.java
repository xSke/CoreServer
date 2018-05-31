/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.entity.mime;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.List;
import org.apache.http.entity.mime.AbstractMultipartForm;
import org.apache.http.entity.mime.FormBodyPart;
import org.apache.http.entity.mime.Header;
import org.apache.http.entity.mime.MinimalField;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
class HttpStrictMultipart
extends AbstractMultipartForm {
    private final List<FormBodyPart> parts;

    public HttpStrictMultipart(String subType, Charset charset, String boundary, List<FormBodyPart> parts) {
        super(subType, charset, boundary);
        this.parts = parts;
    }

    @Override
    public List<FormBodyPart> getBodyParts() {
        return this.parts;
    }

    @Override
    protected void formatMultipartHeader(FormBodyPart part, OutputStream out) throws IOException {
        Header header = part.getHeader();
        for (MinimalField field : header) {
            HttpStrictMultipart.writeField(field, out);
        }
    }
}

