/*
 * Decompiled with CFR 0_129.
 */
package com.azureusnation.coreserver.schematic;

import com.azureusnation.coreserver.marker.CuboidMarker;
import com.azureusnation.coreserver.marker.Marker;
import com.azureusnation.coreserver.util.Cuboid;
import com.flowpowered.nbt.CompoundTag;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import mikera.vectorz.Vector3;

public class Schematic {
    private File file;
    private int width;
    private int height;
    private int length;
    private byte[] blocks;
    private byte[] blockData;
    private Collection<CompoundTag> entities;
    private Collection<CompoundTag> tileEntities;
    private Map<Integer, Marker> markers;

    public Schematic(File file, int width, int height, int length, byte[] blocks, byte[] blockData, Collection<CompoundTag> entities, Collection<CompoundTag> tileEntities, Map<Integer, Marker> markers) {
        this.file = file;
        this.width = width;
        this.height = height;
        this.length = length;
        this.blocks = blocks;
        this.blockData = blockData;
        this.entities = entities;
        this.tileEntities = tileEntities;
        this.markers = markers;
    }

    public Collection<CompoundTag> getTileEntities() {
        return this.tileEntities;
    }

    public Collection<CompoundTag> getEntities() {
        return this.entities;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public int getLength() {
        return this.length;
    }

    public byte getBlock(int x, int y, int z) {
        if (x < 0 || y < 0 || z < 0) {
            return 0;
        }
        if (x >= this.width || y >= this.height || z >= this.length) {
            return 0;
        }
        int idx = y * this.width * this.length + z * this.width + x;
        return this.blocks[idx];
    }

    public byte getBlockData(int x, int y, int z) {
        if (x < 0 || y < 0 || z < 0) {
            return 0;
        }
        if (x >= this.width || y >= this.height || z >= this.length) {
            return 0;
        }
        int idx = y * this.width * this.length + z * this.width + x;
        return this.blockData[idx];
    }

    public void saveMarkers() {
        File markersFile = new File(this.file.getParentFile(), this.file.getName().split("\\.")[0] + ".markers");
        try {
            try (FileWriter fw = new FileWriter(markersFile);){
                new GsonBuilder().setPrettyPrinting().create().toJson(this.markers, (Appendable)fw);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Map<Integer, Marker> getMarkers() {
        return this.markers;
    }

    public Set<Marker> getMarkersWithKey(String key) {
        return this.markers.values().stream().filter(m -> m.getProperties().containsKey(key)).collect(Collectors.toSet());
    }

    public Set<Marker> getCuboidMarkersContainingPoint(Vector3 point) {
        return this.markers.values().stream().filter(m -> m instanceof CuboidMarker).filter(m -> ((CuboidMarker)m).getCuboid().contains(point)).collect(Collectors.toSet());
    }

    public Set<Marker> getCuboidMarkersWithKeyContainingPoint(String key, Vector3 point) {
        return this.markers.values().stream().filter(m -> m.getProperties().containsKey(key)).filter(m -> m instanceof CuboidMarker).filter(m -> ((CuboidMarker)m).getCuboid().contains(point)).collect(Collectors.toSet());
    }
}

