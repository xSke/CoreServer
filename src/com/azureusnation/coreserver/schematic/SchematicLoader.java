/*
 * Decompiled with CFR 0_129.
 */
package com.azureusnation.coreserver.schematic;

import com.azureusnation.coreserver.marker.Marker;
import com.azureusnation.coreserver.schematic.Schematic;
import com.flowpowered.nbt.ByteArrayTag;
import com.flowpowered.nbt.CompoundMap;
import com.flowpowered.nbt.CompoundTag;
import com.flowpowered.nbt.ShortTag;
import com.flowpowered.nbt.Tag;
import com.flowpowered.nbt.stream.NBTInputStream;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class SchematicLoader {
    public static Schematic load(File file) {
        try {
            NBTInputStream nis = new NBTInputStream(new FileInputStream(file));
            CompoundTag tag = (CompoundTag)nis.readTag();
            short height = ((ShortTag)tag.getValue().get("Height")).getValue();
            short length = ((ShortTag)tag.getValue().get("Length")).getValue();
            short width = ((ShortTag)tag.getValue().get("Width")).getValue();
            byte[] blocks = ((ByteArrayTag)tag.getValue().get("Blocks")).getValue();
            byte[] blockData = ((ByteArrayTag)tag.getValue().get("Data")).getValue();
            Collection entities = (Collection)tag.getValue().get("Entities").getValue();
            Collection tileEntities = (Collection)tag.getValue().get("TileEntities").getValue();
            File markersFile = new File(file.getParentFile(), file.getName().split("\\.")[0] + ".markers");
            HashMap<Integer, Marker> markers = markersFile.exists() ? (Map)new Gson().fromJson((Reader)new FileReader(markersFile), new TypeToken<Map<Integer, Marker>>(){}.getType()) : new HashMap<Integer, Marker>();
            Marker.nextId = markers.keySet().stream().sorted().findFirst().orElseGet(() -> -1) + 1;
            return new Schematic(file, width, height, length, blocks, blockData, entities, tileEntities, markers);
        }
        catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

}

