/*
 * Decompiled with CFR 0_129.
 */
package com.azureusnation.coreserver.net.util;

import io.netty.buffer.ByteBuf;

public class NetUtil {
    public static final int COMPRESSION_THRESHOLD = 300;
    public static int lastVarIntSize;

    public static void writeVarInt(ByteBuf out, int value) {
        do {
            if ((value & -128) == 0) {
                out.writeByte(value);
                return;
            }
            out.writeByte(value & 127 | 128);
            value >>>= 7;
        } while (true);
    }

    public static boolean canReadVarInt(ByteBuf buf) {
        byte in;
        if (buf.readableBytes() > 5) {
            return true;
        }
        int idx = buf.readerIndex();
        do {
            if (buf.readableBytes() >= 1) continue;
            buf.readerIndex(idx);
            return false;
        } while (((in = buf.readByte()) & 128) != 0);
        buf.readerIndex(idx);
        return true;
    }

    public static int readVarInt(ByteBuf in) {
        byte k;
        int i = 0;
        int j = 0;
        do {
            k = in.readByte();
            i |= (k & 127) << j++ * 7;
            if (j <= 5) continue;
            throw new RuntimeException("VarInt too big");
        } while ((k & 128) == 128);
        lastVarIntSize = j;
        return i;
    }
}

