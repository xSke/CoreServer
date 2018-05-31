/*
 * Decompiled with CFR 0_129.
 */
package io.netty.channel.sctp;

import com.sun.nio.sctp.MessageInfo;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufHolder;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.DefaultByteBufHolder;
import io.netty.util.ReferenceCounted;

public final class SctpMessage
extends DefaultByteBufHolder {
    private final int streamIdentifier;
    private final int protocolIdentifier;
    private final boolean unordered;
    private final MessageInfo msgInfo;

    public SctpMessage(int protocolIdentifier, int streamIdentifier, ByteBuf payloadBuffer) {
        this(protocolIdentifier, streamIdentifier, false, payloadBuffer);
    }

    public SctpMessage(int protocolIdentifier, int streamIdentifier, boolean unordered, ByteBuf payloadBuffer) {
        super(payloadBuffer);
        this.protocolIdentifier = protocolIdentifier;
        this.streamIdentifier = streamIdentifier;
        this.unordered = unordered;
        this.msgInfo = null;
    }

    public SctpMessage(MessageInfo msgInfo, ByteBuf payloadBuffer) {
        super(payloadBuffer);
        if (msgInfo == null) {
            throw new NullPointerException("msgInfo");
        }
        this.msgInfo = msgInfo;
        this.streamIdentifier = msgInfo.streamNumber();
        this.protocolIdentifier = msgInfo.payloadProtocolID();
        this.unordered = msgInfo.isUnordered();
    }

    public int streamIdentifier() {
        return this.streamIdentifier;
    }

    public int protocolIdentifier() {
        return this.protocolIdentifier;
    }

    public boolean isUnordered() {
        return this.unordered;
    }

    public MessageInfo messageInfo() {
        return this.msgInfo;
    }

    public boolean isComplete() {
        if (this.msgInfo != null) {
            return this.msgInfo.isComplete();
        }
        return true;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        SctpMessage sctpFrame = (SctpMessage)o;
        if (this.protocolIdentifier != sctpFrame.protocolIdentifier) {
            return false;
        }
        if (this.streamIdentifier != sctpFrame.streamIdentifier) {
            return false;
        }
        if (this.unordered != sctpFrame.unordered) {
            return false;
        }
        if (!this.content().equals(sctpFrame.content())) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        int result = this.streamIdentifier;
        result = 31 * result + this.protocolIdentifier;
        result = 31 * result + this.content().hashCode();
        return result;
    }

    @Override
    public SctpMessage copy() {
        if (this.msgInfo == null) {
            return new SctpMessage(this.protocolIdentifier, this.streamIdentifier, this.unordered, this.content().copy());
        }
        return new SctpMessage(this.msgInfo, this.content().copy());
    }

    @Override
    public SctpMessage duplicate() {
        if (this.msgInfo == null) {
            return new SctpMessage(this.protocolIdentifier, this.streamIdentifier, this.unordered, this.content().duplicate());
        }
        return new SctpMessage(this.msgInfo, this.content().copy());
    }

    @Override
    public SctpMessage retain() {
        super.retain();
        return this;
    }

    @Override
    public SctpMessage retain(int increment) {
        super.retain(increment);
        return this;
    }

    @Override
    public String toString() {
        if (this.refCnt() == 0) {
            return "SctpFrame{streamIdentifier=" + this.streamIdentifier + ", protocolIdentifier=" + this.protocolIdentifier + ", unordered=" + this.unordered + ", data=(FREED)}";
        }
        return "SctpFrame{streamIdentifier=" + this.streamIdentifier + ", protocolIdentifier=" + this.protocolIdentifier + ", unordered=" + this.unordered + ", data=" + ByteBufUtil.hexDump(this.content()) + '}';
    }
}

