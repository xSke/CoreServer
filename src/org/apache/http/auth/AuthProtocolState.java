/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.auth;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public enum AuthProtocolState {
    UNCHALLENGED,
    CHALLENGED,
    HANDSHAKE,
    FAILURE,
    SUCCESS;
    

    private AuthProtocolState() {
    }
}

