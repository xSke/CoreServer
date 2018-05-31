/*
 * Decompiled with CFR 0_129.
 */
package com.azureusnation.coreserver.net.packet.login.server;

import com.azureusnation.coreserver.net.PacketBuffer;
import com.azureusnation.coreserver.net.packet.ServerBoundPacket;
import io.netty.buffer.ByteBuf;
import java.io.IOException;

public class PacketSB01EncryptionResponse
extends ServerBoundPacket {
    int sharedSecretLength;
    byte[] sharedSecret;
    int verifyTokenLength;
    byte[] verifyToken;

    public int getSharedSecretLength() {
        return this.sharedSecretLength;
    }

    public byte[] getSharedSecret() {
        return this.sharedSecret;
    }

    public int getVerifyTokenLength() {
        return this.verifyTokenLength;
    }

    public byte[] getVerifyToken() {
        return this.verifyToken;
    }

    @Override
    public void read(PacketBuffer input) throws IOException {
        this.sharedSecretLength = input.readVarInt();
        this.sharedSecret = new byte[this.sharedSecretLength];
        input.readBytes(this.sharedSecret);
        this.verifyTokenLength = input.readVarInt();
        this.verifyToken = new byte[this.sharedSecretLength];
        input.readBytes(this.verifyToken);
    }
}

