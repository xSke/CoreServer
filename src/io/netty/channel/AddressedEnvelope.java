/*
 * Decompiled with CFR 0_129.
 */
package io.netty.channel;

import io.netty.util.ReferenceCounted;
import java.net.SocketAddress;

public interface AddressedEnvelope<M, A extends SocketAddress>
extends ReferenceCounted {
    public M content();

    public A sender();

    public A recipient();
}

