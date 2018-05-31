/*
 * Decompiled with CFR 0_129.
 */
package com.azureusnation.coreserver.minigame;

import com.azureusnation.coreserver.entity.Player;
import com.azureusnation.coreserver.event.PlayerJoinEvent;
import com.azureusnation.coreserver.minigame.SimpleMinigame;
import com.azureusnation.coreserver.room.Room;
import com.google.common.eventbus.Subscribe;
import java.util.Collection;

public class MobHunt
extends SimpleMinigame {
    public MobHunt() {
        super(10, 2, 600, 1200, 200);
    }

    @Subscribe
    public void onPlayerJoin(PlayerJoinEvent e) {
    }

    @Override
    protected void resetGame() {
    }

    @Override
    protected void endGame() {
        for (Player player : this.getRoom().getPlayers()) {
            player.setInvisible(false);
        }
    }

    @Override
    protected void startGame() {
        for (Player player : this.getRoom().getPlayers()) {
            player.setInvisible(true);
        }
    }

    @Override
    protected void tickGame() {
    }
}

