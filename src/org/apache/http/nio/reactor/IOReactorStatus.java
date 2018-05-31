/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.nio.reactor;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public enum IOReactorStatus {
    INACTIVE,
    ACTIVE,
    SHUTDOWN_REQUEST,
    SHUTTING_DOWN,
    SHUT_DOWN;
    

    private IOReactorStatus() {
    }
}

