/*
 * Decompiled with CFR 0_129.
 */
package com.flowpowered.nbt.regionfile;

import com.flowpowered.nbt.Tag;
import com.flowpowered.nbt.stream.NBTInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.InflaterInputStream;

public class SimpleRegionFileReader {
    private static int EXPECTED_VERSION = 1;

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static List<Tag<?>> readFile(File f) {
        RandomAccessFile raf;
        ArrayList i;
        try {
            raf = new RandomAccessFile(f, "r");
        }
        catch (FileNotFoundException e) {
            return null;
        }
        try {
            int i2;
            int version = raf.readInt();
            if (version != EXPECTED_VERSION) {
                List list = null;
                return list;
            }
            int segmentSize = raf.readInt();
            int segmentMask = (1 << segmentSize) - 1;
            int entries = raf.readInt();
            ArrayList list = new ArrayList(entries);
            int[] blockSegmentStart = new int[entries];
            int[] blockActualLength = new int[entries];
            for (i2 = 0; i2 < entries; ++i2) {
                blockSegmentStart[i2] = raf.readInt();
                blockActualLength[i2] = raf.readInt();
            }
            for (i2 = 0; i2 < entries; ++i2) {
                if (blockActualLength[i2] == 0) {
                    list.add(null);
                    continue;
                }
                byte[] data = new byte[blockActualLength[i2]];
                raf.seek(blockSegmentStart[i2] << segmentSize);
                raf.readFully(data);
                ByteArrayInputStream in = new ByteArrayInputStream(data);
                InflaterInputStream iis = new InflaterInputStream(in);
                NBTInputStream ns = new NBTInputStream(iis, false);
                try {
                    Tag t = ns.readTag();
                    list.add(t);
                }
                catch (IOException ioe) {
                    list.add(null);
                }
                try {
                    ns.close();
                    continue;
                }
                catch (IOException ioe) {
                    // empty catch block
                }
            }
            i = list;
        }
        catch (IOException ioe) {
            List segmentSize = null;
            return segmentSize;
        }
        finally {
            try {
                raf.close();
            }
            catch (IOException ioe) {}
        }
        return i;
    }
}

