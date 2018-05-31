/*
 * Decompiled with CFR 0_129.
 */
package com.azureusnation.coreserver.net.packet.play.server;

import com.azureusnation.coreserver.net.PacketBuffer;
import com.azureusnation.coreserver.net.packet.ServerBoundPacket;
import java.io.IOException;
import mikera.vectorz.Vector2;
import mikera.vectorz.Vector3;

public class PacketSB06PlayerPositionAndLook
extends ServerBoundPacket {
    private double x;
    private double y;
    private double z;
    private float yaw;
    private float pitch;
    private boolean onGround;

    public boolean isOnGround() {
        return this.onGround;
    }

    public float getPitch() {
        return this.pitch;
    }

    public float getYaw() {
        return this.yaw;
    }

    public double getZ() {
        return this.z;
    }

    public double getY() {
        return this.y;
    }

    public double getX() {
        return this.x;
    }

    public Vector3 getPosition() {
        return new Vector3(this.x, this.y, this.z);
    }

    public Vector2 getRotation() {
        return new Vector2(this.yaw, this.pitch);
    }

    @Override
    public void read(PacketBuffer input) throws IOException {
        this.x = input.readDouble();
        this.y = input.readDouble();
        this.z = input.readDouble();
        this.yaw = input.readFloat();
        this.pitch = input.readFloat();
        this.onGround = input.readBoolean();
    }
}

