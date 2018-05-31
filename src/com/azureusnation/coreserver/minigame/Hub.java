/*
 * Decompiled with CFR 0_129.
 */
package com.azureusnation.coreserver.minigame;

import com.azureusnation.coreserver.chat.Chat;
import com.azureusnation.coreserver.chat.ChatBuilder;
import com.azureusnation.coreserver.chat.ChatColor;
import com.azureusnation.coreserver.chat.ChatPosition;
import com.azureusnation.coreserver.entity.Player;
import com.azureusnation.coreserver.event.PlayerJoinEvent;
import com.azureusnation.coreserver.event.PlayerPlaceBlockEvent;
import com.azureusnation.coreserver.minigame.Minigame;
import com.azureusnation.coreserver.room.Room;
import com.google.common.eventbus.Subscribe;

public class Hub
extends Minigame {
    @Override
    public void start() {
        this.getRoom().setPlayerListHeader(Chat.builder().color(ChatColor.GOLD).text("Test").bold(true).text("Bold").color(ChatColor.RED).obfuscated(true).text("OneTwoThree").build());
        this.getRoom().setPlayerListFooter(Chat.builder().color(ChatColor.GOLD).text("Test").bold(true).text("Bold").color(ChatColor.RED).obfuscated(true).text("OneTwoThree").build());
    }

    @Subscribe
    public void onPlayerJoin(PlayerJoinEvent e) {
        this.getRoom().broadcastMessage(Chat.builder().color(ChatColor.GOLD).text(e.getPlayer().getName()).color(ChatColor.RED).text(" has joined ").color(ChatColor.GOLD).text(this.getRoom().getName()).build(), ChatPosition.CHAT);
    }

    @Subscribe
    public void onPlayerPlaceBlock(PlayerPlaceBlockEvent event) {
    }
}

