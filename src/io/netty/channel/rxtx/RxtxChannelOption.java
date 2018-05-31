/*
 * Decompiled with CFR 0_129.
 */
package io.netty.channel.rxtx;

import io.netty.channel.ChannelOption;
import io.netty.channel.rxtx.RxtxChannelConfig;

public final class RxtxChannelOption<T>
extends ChannelOption<T> {
    public static final RxtxChannelOption<Integer> BAUD_RATE = new RxtxChannelOption<T>("BAUD_RATE");
    public static final RxtxChannelOption<Boolean> DTR = new RxtxChannelOption<T>("DTR");
    public static final RxtxChannelOption<Boolean> RTS = new RxtxChannelOption<T>("RTS");
    public static final RxtxChannelOption<RxtxChannelConfig.Stopbits> STOP_BITS = new RxtxChannelOption<T>("STOP_BITS");
    public static final RxtxChannelOption<RxtxChannelConfig.Databits> DATA_BITS = new RxtxChannelOption<T>("DATA_BITS");
    public static final RxtxChannelOption<RxtxChannelConfig.Paritybit> PARITY_BIT = new RxtxChannelOption<T>("PARITY_BIT");
    public static final RxtxChannelOption<Integer> WAIT_TIME = new RxtxChannelOption<T>("WAIT_TIME");
    public static final RxtxChannelOption<Integer> READ_TIMEOUT = new RxtxChannelOption<T>("READ_TIMEOUT");

    private RxtxChannelOption(String name) {
        super(name);
    }
}

