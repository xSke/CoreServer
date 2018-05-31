/*
 * Decompiled with CFR 0_129.
 */
package com.flowpowered.nbt.itemmap;

import com.flowpowered.nbt.IntTag;
import com.flowpowered.nbt.Tag;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class StringMapReader {
    public static List<Tag<?>> readFile(File f) {
        ArrayList list = new ArrayList();
        try {
            FileInputStream fis = new FileInputStream(f);
            DataInputStream dis = new DataInputStream(fis);
            boolean eof = false;
            while (!eof) {
                int value;
                try {
                    value = dis.readInt();
                }
                catch (EOFException e) {
                    eof = true;
                    continue;
                }
                String key = dis.readUTF();
                list.add(new IntTag(key, value));
            }
            return list;
        }
        catch (IOException ioe) {
            return null;
        }
    }
}

