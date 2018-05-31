/*
 * Decompiled with CFR 0_129.
 */
package com.flowpowered.nbt.exception;

import com.flowpowered.nbt.Tag;
import java.io.PrintStream;

public class InvalidTagException
extends Exception {
    public InvalidTagException(Tag t) {
        System.out.println("Invalid tag: " + t.toString() + " encountered!");
    }
}

