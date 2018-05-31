/*
 * Decompiled with CFR 0_129.
 */
package com.azureusnation.coreserver.net;

import com.azureusnation.coreserver.chat.Chat;
import com.azureusnation.coreserver.entity.metadata.EntityMetadata;
import com.azureusnation.coreserver.item.ItemStack;
import com.azureusnation.coreserver.net.util.NetUtil;
import com.flowpowered.nbt.CompoundTag;
import com.flowpowered.nbt.Tag;
import com.flowpowered.nbt.stream.NBTInputStream;
import com.google.common.base.Charsets;
import com.google.gson.stream.JsonWriter;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufProcessor;
import io.netty.util.ReferenceCounted;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.GatheringByteChannel;
import java.nio.channels.ScatteringByteChannel;
import java.nio.charset.Charset;
import java.util.UUID;
import mikera.vectorz.Vector3;

public class PacketBuffer
extends ByteBuf {
    private ByteBuf backing;

    public PacketBuffer(ByteBuf backing) {
        this.backing = backing;
    }

    public ItemStack readItemStack() {
        short id = this.readShort();
        if (id == -1) {
            return new ItemStack(id);
        }
        byte itemCount = this.readByte();
        short itemDamage = this.readShort();
        byte hasNBT = this.readByte();
        CompoundTag compoundTag = null;
        if (hasNBT != 0) {
            compoundTag = (CompoundTag)this.readNBT();
        }
        return new ItemStack(id, itemCount, itemDamage, compoundTag);
    }

    public Tag readNBT() {
        ByteBufInputStream inputStream = new ByteBufInputStream(this);
        try {
            NBTInputStream nbtIS = new NBTInputStream(inputStream, false);
            return nbtIS.readTag();
        }
        catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void writeVarInt(int value) {
        NetUtil.writeVarInt(this.backing, value);
    }

    public int readVarInt() {
        return NetUtil.readVarInt(this.backing);
    }

    public void writeString(String value) {
        this.writeVarInt(value.getBytes(Charsets.UTF_8).length);
        this.writeBytes(value.getBytes(Charsets.UTF_8));
    }

    public String readString() {
        int length = this.readVarInt();
        byte[] stringBuf = new byte[length];
        this.readBytes(stringBuf);
        return new String(stringBuf, Charsets.UTF_8);
    }

    public void writeChat(Chat message) {
        try {
            StringWriter sw = new StringWriter();
            JsonWriter jw = new JsonWriter(sw);
            message.write(jw);
            jw.close();
            sw.close();
            this.writeString(sw.toString());
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Vector3 readPosition() {
        long val = this.readLong();
        int x = (int)(val >> 38);
        int y = (int)(val >> 26 & 4095L);
        int z = (int)(val << 38 >> 38);
        if ((double)x >= Math.pow(2.0, 25.0)) {
            x -= 24;
        }
        if ((double)y >= Math.pow(2.0, 11.0)) {
            y -= 14;
        }
        if ((double)z >= Math.pow(2.0, 25.0)) {
            z -= 24;
        }
        return new Vector3((double)x, (double)y, (double)z);
    }

    public void writePosition(Vector3 pos) {
        this.writeLong((long)((int)pos.x & 67108863) << 38 | (long)(((int)pos.y & 4095) << 26) | (long)((int)pos.z & 67108863));
    }

    public void writeEntityMetadata(EntityMetadata metadata) {
        try {
            metadata.serialize(this);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public UUID readUuid() {
        return new UUID(this.readLong(), this.readLong());
    }

    public void writeFixedPointInt(double x) {
        this.writeInt((int)(x * 32.0));
    }

    public void writeFixedPointByte(double x) {
        this.writeByte((int)(x * 32.0));
    }

    public void writeAngleByte(double angle) {
        this.writeByte((int)(angle / 360.0 * 256.0));
    }

    public void writeVelocity(Vector3 velocity) {
        this.writeShort((short)(velocity.x / 20.0 * 8000.0));
        this.writeShort((short)(velocity.y / 20.0 * 8000.0));
        this.writeShort((short)(velocity.z / 20.0 * 8000.0));
    }

    @Override
    public int capacity() {
        return this.backing.capacity();
    }

    @Override
    public ByteBuf capacity(int newCapacity) {
        return this.backing.capacity(newCapacity);
    }

    @Override
    public int maxCapacity() {
        return this.backing.maxCapacity();
    }

    @Override
    public ByteBufAllocator alloc() {
        return this.backing.alloc();
    }

    @Override
    public ByteOrder order() {
        return this.backing.order();
    }

    @Override
    public ByteBuf order(ByteOrder endianness) {
        return this.backing.order(endianness);
    }

    @Override
    public ByteBuf unwrap() {
        return this.backing.unwrap();
    }

    @Override
    public boolean isDirect() {
        return this.backing.isDirect();
    }

    @Override
    public int readerIndex() {
        return this.backing.readerIndex();
    }

    @Override
    public ByteBuf readerIndex(int readerIndex) {
        return this.backing.readerIndex(readerIndex);
    }

    @Override
    public int writerIndex() {
        return this.backing.writerIndex();
    }

    @Override
    public ByteBuf writerIndex(int writerIndex) {
        return this.backing.writerIndex(writerIndex);
    }

    @Override
    public ByteBuf setIndex(int readerIndex, int writerIndex) {
        return this.backing.setIndex(readerIndex, writerIndex);
    }

    @Override
    public int readableBytes() {
        return this.backing.readableBytes();
    }

    @Override
    public int writableBytes() {
        return this.backing.writableBytes();
    }

    @Override
    public int maxWritableBytes() {
        return this.backing.maxWritableBytes();
    }

    @Override
    public boolean isReadable() {
        return this.backing.isReadable();
    }

    @Override
    public boolean isReadable(int size) {
        return this.backing.isReadable(size);
    }

    @Override
    public boolean isWritable() {
        return this.backing.isWritable();
    }

    @Override
    public boolean isWritable(int size) {
        return this.backing.isWritable(size);
    }

    @Override
    public ByteBuf clear() {
        return this.backing.clear();
    }

    @Override
    public ByteBuf markReaderIndex() {
        return this.backing.markReaderIndex();
    }

    @Override
    public ByteBuf resetReaderIndex() {
        return this.backing.resetReaderIndex();
    }

    @Override
    public ByteBuf markWriterIndex() {
        return this.backing.markWriterIndex();
    }

    @Override
    public ByteBuf resetWriterIndex() {
        return this.backing.resetWriterIndex();
    }

    @Override
    public ByteBuf discardReadBytes() {
        return this.backing.discardReadBytes();
    }

    @Override
    public ByteBuf discardSomeReadBytes() {
        return this.backing.discardSomeReadBytes();
    }

    @Override
    public ByteBuf ensureWritable(int minWritableBytes) {
        return this.backing.ensureWritable(minWritableBytes);
    }

    @Override
    public int ensureWritable(int minWritableBytes, boolean force) {
        return this.backing.ensureWritable(minWritableBytes, force);
    }

    @Override
    public boolean getBoolean(int index) {
        return this.backing.getBoolean(index);
    }

    @Override
    public byte getByte(int index) {
        return this.backing.getByte(index);
    }

    @Override
    public short getUnsignedByte(int index) {
        return this.backing.getUnsignedByte(index);
    }

    @Override
    public short getShort(int index) {
        return this.backing.getShort(index);
    }

    @Override
    public int getUnsignedShort(int index) {
        return this.backing.getUnsignedShort(index);
    }

    @Override
    public int getMedium(int index) {
        return this.backing.getMedium(index);
    }

    @Override
    public int getUnsignedMedium(int index) {
        return this.backing.getUnsignedMedium(index);
    }

    @Override
    public int getInt(int index) {
        return this.backing.getInt(index);
    }

    @Override
    public long getUnsignedInt(int index) {
        return this.backing.getUnsignedInt(index);
    }

    @Override
    public long getLong(int index) {
        return this.backing.getLong(index);
    }

    @Override
    public char getChar(int index) {
        return this.backing.getChar(index);
    }

    @Override
    public float getFloat(int index) {
        return this.backing.getFloat(index);
    }

    @Override
    public double getDouble(int index) {
        return this.backing.getDouble(index);
    }

    @Override
    public ByteBuf getBytes(int index, ByteBuf dst) {
        return this.backing.getBytes(index, dst);
    }

    @Override
    public ByteBuf getBytes(int index, ByteBuf dst, int length) {
        return this.backing.getBytes(index, dst, length);
    }

    @Override
    public ByteBuf getBytes(int index, ByteBuf dst, int dstIndex, int length) {
        return this.backing.getBytes(index, dst, dstIndex, length);
    }

    @Override
    public ByteBuf getBytes(int index, byte[] dst) {
        return this.backing.getBytes(index, dst);
    }

    @Override
    public ByteBuf getBytes(int index, byte[] dst, int dstIndex, int length) {
        return this.backing.getBytes(index, dst, dstIndex, length);
    }

    @Override
    public ByteBuf getBytes(int index, ByteBuffer dst) {
        return this.backing.getBytes(index, dst);
    }

    @Override
    public ByteBuf getBytes(int index, OutputStream out, int length) throws IOException {
        return this.backing.getBytes(index, out, length);
    }

    @Override
    public int getBytes(int index, GatheringByteChannel out, int length) throws IOException {
        return this.backing.getBytes(index, out, length);
    }

    @Override
    public ByteBuf setBoolean(int index, boolean value) {
        return this.backing.setBoolean(index, value);
    }

    @Override
    public ByteBuf setByte(int index, int value) {
        return this.backing.setByte(index, value);
    }

    @Override
    public ByteBuf setShort(int index, int value) {
        return this.backing.setShort(index, value);
    }

    @Override
    public ByteBuf setMedium(int index, int value) {
        return this.backing.setMedium(index, value);
    }

    @Override
    public ByteBuf setInt(int index, int value) {
        return this.backing.setInt(index, value);
    }

    @Override
    public ByteBuf setLong(int index, long value) {
        return this.backing.setLong(index, value);
    }

    @Override
    public ByteBuf setChar(int index, int value) {
        return this.backing.setChar(index, value);
    }

    @Override
    public ByteBuf setFloat(int index, float value) {
        return this.backing.setFloat(index, value);
    }

    @Override
    public ByteBuf setDouble(int index, double value) {
        return this.backing.setDouble(index, value);
    }

    @Override
    public ByteBuf setBytes(int index, ByteBuf src) {
        return this.backing.setBytes(index, src);
    }

    @Override
    public ByteBuf setBytes(int index, ByteBuf src, int length) {
        return this.backing.setBytes(index, src, length);
    }

    @Override
    public ByteBuf setBytes(int index, ByteBuf src, int srcIndex, int length) {
        return this.backing.setBytes(index, src, srcIndex, length);
    }

    @Override
    public ByteBuf setBytes(int index, byte[] src) {
        return this.backing.setBytes(index, src);
    }

    @Override
    public ByteBuf setBytes(int index, byte[] src, int srcIndex, int length) {
        return this.backing.setBytes(index, src, srcIndex, length);
    }

    @Override
    public ByteBuf setBytes(int index, ByteBuffer src) {
        return this.backing.setBytes(index, src);
    }

    @Override
    public int setBytes(int index, InputStream in, int length) throws IOException {
        return this.backing.setBytes(index, in, length);
    }

    @Override
    public int setBytes(int index, ScatteringByteChannel in, int length) throws IOException {
        return this.backing.setBytes(index, in, length);
    }

    @Override
    public ByteBuf setZero(int index, int length) {
        return this.backing.setZero(index, length);
    }

    @Override
    public boolean readBoolean() {
        return this.backing.readBoolean();
    }

    @Override
    public byte readByte() {
        return this.backing.readByte();
    }

    @Override
    public short readUnsignedByte() {
        return this.backing.readUnsignedByte();
    }

    @Override
    public short readShort() {
        return this.backing.readShort();
    }

    @Override
    public int readUnsignedShort() {
        return this.backing.readUnsignedShort();
    }

    @Override
    public int readMedium() {
        return this.backing.readMedium();
    }

    @Override
    public int readUnsignedMedium() {
        return this.backing.readUnsignedMedium();
    }

    @Override
    public int readInt() {
        return this.backing.readInt();
    }

    @Override
    public long readUnsignedInt() {
        return this.backing.readUnsignedInt();
    }

    @Override
    public long readLong() {
        return this.backing.readLong();
    }

    @Override
    public char readChar() {
        return this.backing.readChar();
    }

    @Override
    public float readFloat() {
        return this.backing.readFloat();
    }

    @Override
    public double readDouble() {
        return this.backing.readDouble();
    }

    @Override
    public ByteBuf readBytes(int length) {
        return this.backing.readBytes(length);
    }

    @Override
    public ByteBuf readSlice(int length) {
        return this.backing.readSlice(length);
    }

    @Override
    public ByteBuf readBytes(ByteBuf dst) {
        return this.backing.readBytes(dst);
    }

    @Override
    public ByteBuf readBytes(ByteBuf dst, int length) {
        return this.backing.readBytes(dst, length);
    }

    @Override
    public ByteBuf readBytes(ByteBuf dst, int dstIndex, int length) {
        return this.backing.readBytes(dst, dstIndex, length);
    }

    @Override
    public ByteBuf readBytes(byte[] dst) {
        return this.backing.readBytes(dst);
    }

    @Override
    public ByteBuf readBytes(byte[] dst, int dstIndex, int length) {
        return this.backing.readBytes(dst, dstIndex, length);
    }

    @Override
    public ByteBuf readBytes(ByteBuffer dst) {
        return this.backing.readBytes(dst);
    }

    @Override
    public ByteBuf readBytes(OutputStream out, int length) throws IOException {
        return this.backing.readBytes(out, length);
    }

    @Override
    public int readBytes(GatheringByteChannel out, int length) throws IOException {
        return this.backing.readBytes(out, length);
    }

    @Override
    public ByteBuf skipBytes(int length) {
        return this.backing.skipBytes(length);
    }

    @Override
    public ByteBuf writeBoolean(boolean value) {
        return this.backing.writeBoolean(value);
    }

    @Override
    public ByteBuf writeByte(int value) {
        return this.backing.writeByte(value);
    }

    @Override
    public ByteBuf writeShort(int value) {
        return this.backing.writeShort(value);
    }

    @Override
    public ByteBuf writeMedium(int value) {
        return this.backing.writeMedium(value);
    }

    @Override
    public ByteBuf writeInt(int value) {
        return this.backing.writeInt(value);
    }

    @Override
    public ByteBuf writeLong(long value) {
        return this.backing.writeLong(value);
    }

    @Override
    public ByteBuf writeChar(int value) {
        return this.backing.writeChar(value);
    }

    @Override
    public ByteBuf writeFloat(float value) {
        return this.backing.writeFloat(value);
    }

    @Override
    public ByteBuf writeDouble(double value) {
        return this.backing.writeDouble(value);
    }

    @Override
    public ByteBuf writeBytes(ByteBuf src) {
        return this.backing.writeBytes(src);
    }

    @Override
    public ByteBuf writeBytes(ByteBuf src, int length) {
        return this.backing.writeBytes(src, length);
    }

    @Override
    public ByteBuf writeBytes(ByteBuf src, int srcIndex, int length) {
        return this.backing.writeBytes(src, srcIndex, length);
    }

    @Override
    public ByteBuf writeBytes(byte[] src) {
        return this.backing.writeBytes(src);
    }

    @Override
    public ByteBuf writeBytes(byte[] src, int srcIndex, int length) {
        return this.backing.writeBytes(src, srcIndex, length);
    }

    @Override
    public ByteBuf writeBytes(ByteBuffer src) {
        return this.backing.writeBytes(src);
    }

    @Override
    public int writeBytes(InputStream in, int length) throws IOException {
        return this.backing.writeBytes(in, length);
    }

    @Override
    public int writeBytes(ScatteringByteChannel in, int length) throws IOException {
        return this.backing.writeBytes(in, length);
    }

    @Override
    public ByteBuf writeZero(int length) {
        return this.backing.writeZero(length);
    }

    @Override
    public int indexOf(int fromIndex, int toIndex, byte value) {
        return this.backing.indexOf(fromIndex, toIndex, value);
    }

    @Override
    public int bytesBefore(byte value) {
        return this.backing.bytesBefore(value);
    }

    @Override
    public int bytesBefore(int length, byte value) {
        return this.backing.bytesBefore(length, value);
    }

    @Override
    public int bytesBefore(int index, int length, byte value) {
        return this.backing.bytesBefore(index, length, value);
    }

    @Override
    public int forEachByte(ByteBufProcessor processor) {
        return this.backing.forEachByte(processor);
    }

    @Override
    public int forEachByte(int index, int length, ByteBufProcessor processor) {
        return this.backing.forEachByte(index, length, processor);
    }

    @Override
    public int forEachByteDesc(ByteBufProcessor processor) {
        return this.backing.forEachByteDesc(processor);
    }

    @Override
    public int forEachByteDesc(int index, int length, ByteBufProcessor processor) {
        return this.backing.forEachByteDesc(index, length, processor);
    }

    @Override
    public ByteBuf copy() {
        return this.backing.copy();
    }

    @Override
    public ByteBuf copy(int index, int length) {
        return this.backing.copy(index, length);
    }

    @Override
    public ByteBuf slice() {
        return this.backing.slice();
    }

    @Override
    public ByteBuf slice(int index, int length) {
        return this.backing.slice(index, length);
    }

    @Override
    public ByteBuf duplicate() {
        return this.backing.duplicate();
    }

    @Override
    public int nioBufferCount() {
        return this.backing.nioBufferCount();
    }

    @Override
    public ByteBuffer nioBuffer() {
        return this.backing.nioBuffer();
    }

    @Override
    public ByteBuffer nioBuffer(int index, int length) {
        return this.backing.nioBuffer(index, length);
    }

    @Override
    public ByteBuffer internalNioBuffer(int index, int length) {
        return this.backing.internalNioBuffer(index, length);
    }

    @Override
    public ByteBuffer[] nioBuffers() {
        return this.backing.nioBuffers();
    }

    @Override
    public ByteBuffer[] nioBuffers(int index, int length) {
        return this.backing.nioBuffers(index, length);
    }

    @Override
    public boolean hasArray() {
        return this.backing.hasArray();
    }

    @Override
    public byte[] array() {
        return this.backing.array();
    }

    @Override
    public int arrayOffset() {
        return this.backing.arrayOffset();
    }

    @Override
    public boolean hasMemoryAddress() {
        return this.backing.hasMemoryAddress();
    }

    @Override
    public long memoryAddress() {
        return this.backing.memoryAddress();
    }

    @Override
    public String toString(Charset charset) {
        return this.backing.toString(charset);
    }

    @Override
    public String toString(int index, int length, Charset charset) {
        return this.backing.toString(index, length, charset);
    }

    @Override
    public int hashCode() {
        return this.backing.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return this.backing.equals(obj);
    }

    @Override
    public int compareTo(ByteBuf buffer) {
        return this.backing.compareTo(buffer);
    }

    @Override
    public String toString() {
        return this.backing.toString();
    }

    @Override
    public ByteBuf retain(int increment) {
        return this.backing.retain(increment);
    }

    @Override
    public ByteBuf retain() {
        return this.backing.retain();
    }

    @Override
    public int refCnt() {
        return this.backing.refCnt();
    }

    @Override
    public boolean release() {
        return this.backing.release();
    }

    @Override
    public boolean release(int decrement) {
        return this.backing.release(decrement);
    }
}

