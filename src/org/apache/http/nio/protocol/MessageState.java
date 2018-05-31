/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.nio.protocol;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
enum MessageState {
    READY,
    INIT,
    ACK_EXPECTED,
    ACK,
    BODY_STREAM,
    COMPLETED;
    

    private MessageState() {
    }
}

