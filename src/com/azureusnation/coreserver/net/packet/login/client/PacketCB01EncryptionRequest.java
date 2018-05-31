/*
 * Decompiled with CFR 0_129.
 */
package com.azureusnation.coreserver.net.packet.login.client;

import com.azureusnation.coreserver.net.PacketBuffer;
import com.azureusnation.coreserver.net.packet.ClientBoundPacket;
import io.netty.buffer.ByteBuf;
import java.io.IOException;

public class PacketCB01EncryptionRequest
extends ClientBoundPacket {
    String serverID;
    int publicKeyLength;
    byte[] publicKey;
    int verifyTokenLength;
    byte[] verifyToken;

    public PacketCB01EncryptionRequest() {
    }

    public PacketCB01EncryptionRequest(String serverID, int publicKeyLength, byte[] publicKey, int verifyTokenLength, byte[] verifyToken) {
        this.serverID = serverID;
        this.publicKeyLength = publicKeyLength;
        this.publicKey = publicKey;
        this.verifyTokenLength = verifyTokenLength;
        this.verifyToken = verifyToken;
    }

    @Override
    public void write(PacketBuffer input) throws IOException {
        input.writeString(this.serverID);
        input.writeVarInt(this.publicKeyLength);
        input.writeBytes(this.publicKey);
        input.writeVarInt(this.verifyTokenLength);
        input.writeBytes(this.verifyToken);
    }
}

