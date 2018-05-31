/*
 * Decompiled with CFR 0_129.
 */
package org.json.zip;

import java.io.PrintStream;
import org.json.zip.Huff;
import org.json.zip.MapKeep;
import org.json.zip.None;
import org.json.zip.PostMortem;
import org.json.zip.TrieKeep;

public abstract class JSONzip
implements None,
PostMortem {
    public static final int[] twos = new int[]{1, 2, 4, 8, 16, 32, 64, 128, 256, 512, 1024, 2048, 4096, 8192, 16384, 32768, 65536};
    public static final byte[] bcd = new byte[]{48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 46, 45, 43, 69};
    public static final long int4 = 16L;
    public static final long int7 = 128L;
    public static final long int14 = 16384L;
    public static final int end = 256;
    public static final int endOfNumber = bcd.length;
    public static final int maxSubstringLength = 10;
    public static final int minSubstringLength = 3;
    public static final boolean probe = false;
    public static final int substringLimit = 40;
    public static final int zipEmptyObject = 0;
    public static final int zipEmptyArray = 1;
    public static final int zipTrue = 2;
    public static final int zipFalse = 3;
    public static final int zipNull = 4;
    public static final int zipObject = 5;
    public static final int zipArrayString = 6;
    public static final int zipArrayValue = 7;
    protected final Huff namehuff = new Huff(257);
    protected final MapKeep namekeep = new MapKeep(9);
    protected final MapKeep stringkeep = new MapKeep(11);
    protected final Huff substringhuff = new Huff(257);
    protected final TrieKeep substringkeep = new TrieKeep(12);
    protected final MapKeep values = new MapKeep(10);

    protected JSONzip() {
        this.namehuff.tick(32, 125);
        this.namehuff.tick(97, 122);
        this.namehuff.tick(256);
        this.namehuff.tick(256);
        this.substringhuff.tick(32, 125);
        this.substringhuff.tick(97, 122);
        this.substringhuff.tick(256);
        this.substringhuff.tick(256);
    }

    protected void begin() {
        this.namehuff.generate();
        this.substringhuff.generate();
    }

    static void log() {
        JSONzip.log("\n");
    }

    static void log(int integer) {
        JSONzip.log("" + integer + " ");
    }

    static void log(int integer, int width) {
        JSONzip.log("" + integer + ":" + width + " ");
    }

    static void log(String string) {
        System.out.print(string);
    }

    static void logchar(int integer, int width) {
        if (integer > 32 && integer <= 125) {
            JSONzip.log("'" + (char)integer + "':" + width + " ");
        } else {
            JSONzip.log(integer, width);
        }
    }

    public boolean postMortem(PostMortem pm) {
        JSONzip that = (JSONzip)pm;
        return this.namehuff.postMortem(that.namehuff) && this.namekeep.postMortem(that.namekeep) && this.stringkeep.postMortem(that.stringkeep) && this.substringhuff.postMortem(that.substringhuff) && this.substringkeep.postMortem(that.substringkeep) && this.values.postMortem(that.values);
    }
}

