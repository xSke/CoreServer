/*
 * Decompiled with CFR 0_129.
 */
package com.azureusnation.coreserver.minigame;

import com.azureusnation.coreserver.chat.Chat;
import com.azureusnation.coreserver.chat.ChatBuilder;
import com.azureusnation.coreserver.entity.Player;
import com.azureusnation.coreserver.event.PlayerDigBlockEvent;
import com.azureusnation.coreserver.event.PlayerSendMessageEvent;
import com.azureusnation.coreserver.marker.CuboidMarker;
import com.azureusnation.coreserver.marker.Marker;
import com.azureusnation.coreserver.marker.PointMarker;
import com.azureusnation.coreserver.minigame.Minigame;
import com.azureusnation.coreserver.room.Room;
import com.azureusnation.coreserver.schematic.Schematic;
import com.azureusnation.coreserver.util.Cuboid;
import com.google.common.eventbus.Subscribe;
import java.util.Map;
import mikera.vectorz.Vector3;

public class MarkerSetup
extends Minigame {
    private int pendingId;
    private Operation pendingOperation;
    private Vector3 firstCornerPosition;

    @Subscribe
    public void onPlayerSendMessage(PlayerSendMessageEvent e) {
        String[] args;
        if (e.getMessage().startsWith("/") && (args = e.getMessage().substring(1).split(" ")).length > 0 && args[0].equals("marker")) {
            if (args.length > 1) {
                if (args[1].equals("point")) {
                    if (args.length == 2) {
                        this.pendingOperation = Operation.POINT;
                        this.pendingId = -1;
                        e.getPlayer().sendMessage(Chat.builder().text("Creating a new point marker, punch a block").build());
                    } else if (!(this.getRoom().getSchematic().getMarkers().get(Integer.parseInt(args[2])) instanceof PointMarker)) {
                        e.getPlayer().sendMessage(Chat.builder().text("Marker with ID " + args[2] + " isn't a point marker").build());
                    } else if (!this.getRoom().getSchematic().getMarkers().containsKey(Integer.parseInt(args[2]))) {
                        e.getPlayer().sendMessage(Chat.builder().text("No marker with ID " + args[2]).build());
                    } else {
                        this.pendingOperation = Operation.POINT;
                        this.pendingId = Integer.parseInt(args[2]);
                        e.getPlayer().sendMessage(Chat.builder().text("Setting a point marker's position, punch a block").build());
                    }
                } else if (args[1].equals("cuboid")) {
                    if (args.length == 2) {
                        this.pendingOperation = Operation.CORNER_1;
                        this.pendingId = -1;
                        e.getPlayer().sendMessage(Chat.builder().text("Creating a new cuboid marker, punch a block for first corner").build());
                    } else if (!(this.getRoom().getSchematic().getMarkers().get(Integer.parseInt(args[2])) instanceof CuboidMarker)) {
                        e.getPlayer().sendMessage(Chat.builder().text("Marker with ID " + args[2] + " isn't a cuboid marker").build());
                    } else if (!this.getRoom().getSchematic().getMarkers().containsKey(Integer.parseInt(args[2]))) {
                        e.getPlayer().sendMessage(Chat.builder().text("No marker with ID " + args[2]).build());
                    } else {
                        this.pendingOperation = Operation.CORNER_1;
                        this.pendingId = Integer.parseInt(args[2]);
                        e.getPlayer().sendMessage(Chat.builder().text("Setting a point marker's position, punch a block for first corner").build());
                    }
                }
            } else {
                e.getPlayer().sendMessage(Chat.builder().text("Commands:").build());
                e.getPlayer().sendMessage(Chat.builder().text("/marker point").build());
                e.getPlayer().sendMessage(Chat.builder().text("/marker point <id>").build());
                e.getPlayer().sendMessage(Chat.builder().text("/marker cuboid").build());
                e.getPlayer().sendMessage(Chat.builder().text("/marker cuboid <id>").build());
            }
        }
    }

    @Subscribe
    public void onPlayerDigBlock(PlayerDigBlockEvent e) {
        e.setCancelled();
        if (this.pendingOperation != null) {
            switch (this.pendingOperation) {
                case POINT: {
                    if (this.pendingId == -1) {
                        this.pendingId = Marker.nextId++;
                        this.getRoom().getSchematic().getMarkers().put(this.pendingId, new PointMarker(this.pendingId, e.getPosition()));
                    }
                    ((PointMarker)this.getRoom().getSchematic().getMarkers().get(this.pendingId)).getPosition().set(e.getPosition());
                    e.getPlayer().sendMessage(Chat.builder().text("Set a point marker with ID " + this.pendingId + " to position " + e.getPosition().toString()).build());
                    this.pendingId = -1;
                    this.pendingOperation = null;
                    break;
                }
                case CORNER_1: {
                    e.getPlayer().sendMessage(Chat.builder().text("Punch a block for second corner").build());
                    this.firstCornerPosition = e.getPosition();
                    this.pendingOperation = Operation.CORNER_2;
                    break;
                }
                case CORNER_2: {
                    if (this.pendingId == -1) {
                        this.pendingId = Marker.nextId++;
                        this.getRoom().getSchematic().getMarkers().put(this.pendingId, new CuboidMarker(this.pendingId, new Cuboid(this.firstCornerPosition, e.getPosition())));
                    }
                    ((CuboidMarker)this.getRoom().getSchematic().getMarkers().get(this.pendingId)).getCuboid().getA().set(this.firstCornerPosition);
                    ((CuboidMarker)this.getRoom().getSchematic().getMarkers().get(this.pendingId)).getCuboid().getB().set(e.getPosition());
                    ((CuboidMarker)this.getRoom().getSchematic().getMarkers().get(this.pendingId)).getCuboid().fixCorners();
                    e.getPlayer().sendMessage(Chat.builder().text("Set a cuboid marker with ID " + this.pendingId + " to position " + this.firstCornerPosition.toString() + "," + e.getPosition().toString()).build());
                    this.pendingId = -1;
                    this.pendingOperation = null;
                }
            }
            this.getRoom().getSchematic().saveMarkers();
        }
    }

    public static enum Operation {
        POINT,
        CORNER_1,
        CORNER_2;
        

        private Operation() {
        }
    }

}

