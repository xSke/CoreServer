/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.entity.mime.content;

public interface ContentDescriptor {
    public String getMimeType();

    public String getMediaType();

    public String getSubType();

    public String getCharset();

    public String getTransferEncoding();

    public long getContentLength();
}

