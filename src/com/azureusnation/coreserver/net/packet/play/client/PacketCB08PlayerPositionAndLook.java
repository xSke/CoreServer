/*
 * Decompiled with CFR 0_129.
 */
package com.azureusnation.coreserver.net.packet.play.client;

import com.azureusnation.coreserver.net.PacketBuffer;
import com.azureusnation.coreserver.net.packet.ClientBoundPacket;
import io.netty.buffer.ByteBuf;
import java.io.IOException;
import java.util.EnumSet;
import mikera.vectorz.Vector2;
import mikera.vectorz.Vector3;

public class PacketCB08PlayerPositionAndLook
extends ClientBoundPacket {
    double x;
    double y;
    double z;
    float yaw;
    float pitch;
    EnumSet<RelativeCoordinates> flags;

    public PacketCB08PlayerPositionAndLook(Vector3 coordinates, Vector2 rotation, EnumSet<RelativeCoordinates> flags) {
        this(coordinates.x, coordinates.y, coordinates.z, (float)rotation.x, (float)rotation.y, flags);
    }

    public PacketCB08PlayerPositionAndLook(double x, double y, double z, float yaw, float pitch, EnumSet<RelativeCoordinates> flags) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        this.flags = flags;
    }

    @Override
    public void write(PacketBuffer input) throws IOException {
        input.writeDouble(this.x);
        input.writeDouble(this.y);
        input.writeDouble(this.z);
        input.writeFloat(this.yaw);
        input.writeFloat(this.pitch);
        byte bits = 0;
        if (this.flags.contains((Object)RelativeCoordinates.X)) {
            bits = (byte)(bits | true ? 1 : 0);
        }
        if (this.flags.contains((Object)RelativeCoordinates.Y)) {
            bits = (byte)(bits | 2);
        }
        if (this.flags.contains((Object)RelativeCoordinates.Z)) {
            bits = (byte)(bits | 4);
        }
        if (this.flags.contains((Object)RelativeCoordinates.Y_ROT)) {
            bits = (byte)(bits | 8);
        }
        if (this.flags.contains((Object)RelativeCoordinates.X_ROT)) {
            bits = (byte)(bits | 16);
        }
        input.writeByte(bits);
    }

    public static enum RelativeCoordinates {
        X,
        Y,
        Z,
        Y_ROT,
        X_ROT;
        

        private RelativeCoordinates() {
        }
    }

}

