/*
 * Decompiled with CFR 0_129.
 */
package io.netty.channel.unix;

import io.netty.channel.Channel;
import io.netty.channel.unix.FileDescriptor;

public interface UnixChannel
extends Channel {
    public FileDescriptor fd();
}

