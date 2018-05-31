/*
 * Decompiled with CFR 0_129.
 */
package com.azureusnation.coreserver.chat;

public class ClickEvent {
    private Action action;
    private Object value;

    public ClickEvent(Action action, Object value) {
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
        OPEN_URL,
        RUN_COMMAND,
        SUGGEST_COMMAND,
        CHANGE_PAGE;
        

        private Action() {
        }
    }

}

