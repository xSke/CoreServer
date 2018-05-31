/*
 * Decompiled with CFR 0_129.
 */
package com.azureusnation.coreserver.minigame;

import com.azureusnation.coreserver.entity.Player;
import com.azureusnation.coreserver.minigame.Minigame;
import com.azureusnation.coreserver.room.Room;
import java.io.PrintStream;
import java.util.Collection;

public abstract class SimpleMinigame
extends Minigame {
    private int maxPlayers;
    private int playersToActivate;
    private int lobbyTimerValue;
    private int gameTimerValue;
    private int restartTimerValue;
    private MinigameState state = MinigameState.WAITING_FOR_PLAYERS;
    private int timerTicks;

    public SimpleMinigame(int maxPlayers, int playersToActivate, int lobbyTimerValue, int gameTimerValue, int restartTimerValue) {
        this.maxPlayers = maxPlayers;
        this.playersToActivate = playersToActivate;
        this.lobbyTimerValue = lobbyTimerValue;
        this.gameTimerValue = gameTimerValue;
        this.restartTimerValue = restartTimerValue;
    }

    @Override
    public void tick() {
        System.out.println((float)this.timerTicks / 20.0f);
        switch (this.state) {
            case WAITING_FOR_PLAYERS: {
                if (this.getRoom().getPlayers().size() < this.playersToActivate) break;
                this.timerTicks = this.lobbyTimerValue;
                this.state = MinigameState.WAITING_FOR_TIMEOUT;
                break;
            }
            case WAITING_FOR_TIMEOUT: {
                if (this.getRoom().getPlayers().size() < this.playersToActivate) {
                    this.state = MinigameState.WAITING_FOR_PLAYERS;
                }
                --this.timerTicks;
                if (this.timerTicks != 0) break;
                this.state = MinigameState.IN_PROGRESS;
                this.timerTicks = this.gameTimerValue;
                this.startGame();
                break;
            }
            case IN_PROGRESS: {
                this.tickGame();
                --this.timerTicks;
                if (this.timerTicks != 0) break;
                this.endGame0();
                break;
            }
            case WAITING_FOR_RESTART: {
                --this.timerTicks;
                if (this.timerTicks != 0) break;
                this.state = MinigameState.WAITING_FOR_PLAYERS;
                this.timerTicks = this.lobbyTimerValue;
                this.resetGame();
            }
        }
    }

    protected void endGame0() {
        this.state = MinigameState.WAITING_FOR_RESTART;
        this.timerTicks = this.restartTimerValue;
        this.endGame();
    }

    protected abstract void startGame();

    protected abstract void tickGame();

    protected abstract void endGame();

    protected abstract void resetGame();

    private static enum MinigameState {
        WAITING_FOR_PLAYERS,
        WAITING_FOR_TIMEOUT,
        IN_PROGRESS,
        WAITING_FOR_RESTART;
        

        private MinigameState() {
        }
    }

}

