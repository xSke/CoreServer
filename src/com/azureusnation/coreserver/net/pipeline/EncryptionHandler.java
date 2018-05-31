/*
 * Decompiled with CFR 0_129.
 */
package com.azureusnation.coreserver.net.pipeline;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.spec.AlgorithmParameterSpec;
import java.util.List;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.IvParameterSpec;

public class EncryptionHandler
extends MessageToMessageCodec<ByteBuf, ByteBuf> {
    private CryptBuf encrypt;
    private CryptBuf decrypt;

    public EncryptionHandler(SecretKey secret) {
        try {
            this.encrypt = new CryptBuf(1, secret);
            this.decrypt = new CryptBuf(2, secret);
        }
        catch (GeneralSecurityException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
        this.encrypt.crypt(msg, out);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
        this.decrypt.crypt(msg, out);
    }

    private static class CryptBuf {
        private final Cipher cipher = Cipher.getInstance("AES/CFB8/NoPadding");

        private CryptBuf(int mode, SecretKey sharedSecret) throws GeneralSecurityException {
            this.cipher.init(mode, (Key)sharedSecret, new IvParameterSpec(sharedSecret.getEncoded()));
        }

        public void crypt(ByteBuf msg, List<Object> out) {
            ByteBuffer outBuffer = ByteBuffer.allocate(msg.readableBytes());
            try {
                this.cipher.update(msg.nioBuffer(), outBuffer);
            }
            catch (ShortBufferException e) {
                throw new AssertionError("Encryption buffer was too short", e);
            }
            outBuffer.flip();
            out.add(Unpooled.wrappedBuffer(outBuffer));
        }
    }

}

