/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.entity.mime;

import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.Header;
import org.apache.http.entity.mime.MinimalField;
import org.apache.http.entity.mime.content.AbstractContentBody;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.util.Args;

public class FormBodyPart {
    private final String name;
    private final Header header;
    private final ContentBody body;

    public FormBodyPart(String name, ContentBody body) {
        Args.notNull(name, "Name");
        Args.notNull(body, "Body");
        this.name = name;
        this.body = body;
        this.header = new Header();
        this.generateContentDisp(body);
        this.generateContentType(body);
        this.generateTransferEncoding(body);
    }

    public String getName() {
        return this.name;
    }

    public ContentBody getBody() {
        return this.body;
    }

    public Header getHeader() {
        return this.header;
    }

    public void addField(String name, String value) {
        Args.notNull(name, "Field name");
        this.header.addField(new MinimalField(name, value));
    }

    protected void generateContentDisp(ContentBody body) {
        StringBuilder buffer = new StringBuilder();
        buffer.append("form-data; name=\"");
        buffer.append(this.getName());
        buffer.append("\"");
        if (body.getFilename() != null) {
            buffer.append("; filename=\"");
            buffer.append(body.getFilename());
            buffer.append("\"");
        }
        this.addField("Content-Disposition", buffer.toString());
    }

    protected void generateContentType(ContentBody body) {
        ContentType contentType = body instanceof AbstractContentBody ? ((AbstractContentBody)body).getContentType() : null;
        if (contentType != null) {
            this.addField("Content-Type", contentType.toString());
        } else {
            StringBuilder buffer = new StringBuilder();
            buffer.append(body.getMimeType());
            if (body.getCharset() != null) {
                buffer.append("; charset=");
                buffer.append(body.getCharset());
            }
            this.addField("Content-Type", buffer.toString());
        }
    }

    protected void generateTransferEncoding(ContentBody body) {
        this.addField("Content-Transfer-Encoding", body.getTransferEncoding());
    }
}

