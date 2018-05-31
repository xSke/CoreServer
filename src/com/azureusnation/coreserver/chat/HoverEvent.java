/*
 * Decompiled with CFR 0_129.
 */
package com.azureusnation.coreserver.chat;

public class HoverEvent {
    private Action action;
    private Object value;

    public HoverEvent(Action action, Object value) {
        this.action = action;
        this.value = value;
    }

    public Action getAction() {
        return this.action;
    }

    public Object getValue() {
        return this.value;
    }

    static enum Action {
        SHOW_TEXT,
        SHOW_ACHIEVEMENT,
        SHOW_ITEM,
        SHOW_ENTITY;
        

        private Action() {
        }

        public String getName() {
            return this.toString().toLowerCase();
        }
    }

}

