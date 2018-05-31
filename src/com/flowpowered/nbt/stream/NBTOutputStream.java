/*
 * Decompiled with CFR 0_129.
 */
package com.flowpowered.nbt.stream;

import com.flowpowered.nbt.ByteArrayTag;
import com.flowpowered.nbt.ByteTag;
import com.flowpowered.nbt.CompoundMap;
import com.flowpowered.nbt.CompoundTag;
import com.flowpowered.nbt.DoubleTag;
import com.flowpowered.nbt.EndTag;
import com.flowpowered.nbt.FloatTag;
import com.flowpowered.nbt.IntArrayTag;
import com.flowpowered.nbt.IntTag;
import com.flowpowered.nbt.ListTag;
import com.flowpowered.nbt.LongTag;
import com.flowpowered.nbt.NBTConstants;
import com.flowpowered.nbt.ShortArrayTag;
import com.flowpowered.nbt.ShortTag;
import com.flowpowered.nbt.StringTag;
import com.flowpowered.nbt.Tag;
import com.flowpowered.nbt.TagType;
import com.flowpowered.nbt.stream.EndianSwitchableOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Iterator;
import java.util.zip.GZIPOutputStream;

public final class NBTOutputStream
implements Closeable {
    private final EndianSwitchableOutputStream os;

    public NBTOutputStream(OutputStream os) throws IOException {
        this(os, true, ByteOrder.BIG_ENDIAN);
    }

    public NBTOutputStream(OutputStream os, boolean compressed) throws IOException {
        this(os, compressed, ByteOrder.BIG_ENDIAN);
    }

    public NBTOutputStream(OutputStream os, boolean compressed, ByteOrder endianness) throws IOException {
        this.os = new EndianSwitchableOutputStream(compressed ? new GZIPOutputStream(os) : os, endianness);
    }

    public void writeTag(Tag<?> tag) throws IOException {
        String name = tag.getName();
        byte[] nameBytes = name.getBytes(NBTConstants.CHARSET.name());
        this.os.writeByte(tag.getType().getId());
        this.os.writeShort(nameBytes.length);
        this.os.write(nameBytes);
        if (tag.getType() == TagType.TAG_END) {
            throw new IOException("Named TAG_End not permitted.");
        }
        this.writeTagPayload(tag);
    }

    private void writeTagPayload(Tag<?> tag) throws IOException {
        switch (tag.getType()) {
            case TAG_END: {
                this.writeEndTagPayload((EndTag)tag);
                break;
            }
            case TAG_BYTE: {
                this.writeByteTagPayload((ByteTag)tag);
                break;
            }
            case TAG_SHORT: {
                this.writeShortTagPayload((ShortTag)tag);
                break;
            }
            case TAG_INT: {
                this.writeIntTagPayload((IntTag)tag);
                break;
            }
            case TAG_LONG: {
                this.writeLongTagPayload((LongTag)tag);
                break;
            }
            case TAG_FLOAT: {
                this.writeFloatTagPayload((FloatTag)tag);
                break;
            }
            case TAG_DOUBLE: {
                this.writeDoubleTagPayload((DoubleTag)tag);
                break;
            }
            case TAG_BYTE_ARRAY: {
                this.writeByteArrayTagPayload((ByteArrayTag)tag);
                break;
            }
            case TAG_STRING: {
                this.writeStringTagPayload((StringTag)tag);
                break;
            }
            case TAG_LIST: {
                this.writeListTagPayload((ListTag)tag);
                break;
            }
            case TAG_COMPOUND: {
                this.writeCompoundTagPayload((CompoundTag)tag);
                break;
            }
            case TAG_INT_ARRAY: {
                this.writeIntArrayTagPayload((IntArrayTag)tag);
                break;
            }
            case TAG_SHORT_ARRAY: {
                this.writeShortArrayTagPayload((ShortArrayTag)tag);
                break;
            }
            default: {
                throw new IOException("Invalid tag type: " + (Object)((Object)tag.getType()) + ".");
            }
        }
    }

    private void writeByteTagPayload(ByteTag tag) throws IOException {
        this.os.writeByte(tag.getValue().byteValue());
    }

    private void writeByteArrayTagPayload(ByteArrayTag tag) throws IOException {
        byte[] bytes = tag.getValue();
        this.os.writeInt(bytes.length);
        this.os.write(bytes);
    }

    private void writeCompoundTagPayload(CompoundTag tag) throws IOException {
        for (Tag childTag : tag.getValue().values()) {
            this.writeTag(childTag);
        }
        this.os.writeByte(TagType.TAG_END.getId());
    }

    private void writeListTagPayload(ListTag<?> tag) throws IOException {
        Class clazz = tag.getElementType();
        Object tags = tag.getValue();
        int size = tags.size();
        this.os.writeByte(TagType.getByTagClass(clazz).getId());
        this.os.writeInt(size);
        Iterator iterator = tags.iterator();
        while (iterator.hasNext()) {
            Tag tag1 = (Tag)iterator.next();
            this.writeTagPayload(tag1);
        }
    }

    private void writeStringTagPayload(StringTag tag) throws IOException {
        byte[] bytes = tag.getValue().getBytes(NBTConstants.CHARSET.name());
        this.os.writeShort(bytes.length);
        this.os.write(bytes);
    }

    private void writeDoubleTagPayload(DoubleTag tag) throws IOException {
        this.os.writeDouble(tag.getValue());
    }

    private void writeFloatTagPayload(FloatTag tag) throws IOException {
        this.os.writeFloat(tag.getValue().floatValue());
    }

    private void writeLongTagPayload(LongTag tag) throws IOException {
        this.os.writeLong(tag.getValue());
    }

    private void writeIntTagPayload(IntTag tag) throws IOException {
        this.os.writeInt(tag.getValue());
    }

    private void writeShortTagPayload(ShortTag tag) throws IOException {
        this.os.writeShort(tag.getValue().shortValue());
    }

    private void writeIntArrayTagPayload(IntArrayTag tag) throws IOException {
        int[] ints = tag.getValue();
        this.os.writeInt(ints.length);
        for (int i = 0; i < ints.length; ++i) {
            this.os.writeInt(ints[i]);
        }
    }

    private void writeShortArrayTagPayload(ShortArrayTag tag) throws IOException {
        short[] shorts = tag.getValue();
        this.os.writeInt(shorts.length);
        for (int i = 0; i < shorts.length; ++i) {
            this.os.writeShort(shorts[i]);
        }
    }

    private void writeEndTagPayload(EndTag tag) {
    }

    @Override
    public void close() throws IOException {
        this.os.close();
    }

    public ByteOrder getEndianness() {
        return this.os.getEndianness();
    }

    public void flush() throws IOException {
        this.os.flush();
    }

}

