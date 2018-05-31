/*
 * Decompiled with CFR 0_129.
 */
package com.azureusnation.coreserver.chat;

import com.azureusnation.coreserver.chat.Chat;
import com.azureusnation.coreserver.chat.ChatColor;
import com.azureusnation.coreserver.chat.ChatComponent;
import com.azureusnation.coreserver.chat.ClickEvent;
import com.azureusnation.coreserver.chat.HoverEvent;
import java.util.ArrayList;
import java.util.List;

public class ChatBuilder {
    private List<ChatComponent> components = new ArrayList<ChatComponent>();
    private ChatColor currentColor = ChatColor.WHITE;
    private boolean currentBold;
    private boolean currentUnderlined;
    private boolean currentItalic;
    private boolean currentStrikethrough;
    private boolean currentObfuscated;
    private String currentInsertion = "";
    private ClickEvent currentClickEvent;
    private HoverEvent currentHoverEvent;

    ChatBuilder() {
    }

    public ChatBuilder text(String text) {
        ChatComponent newComponent = new ChatComponent();
        newComponent.setText(text);
        newComponent.setColor(this.currentColor);
        newComponent.setBold(this.currentBold);
        newComponent.setUnderlined(this.currentUnderlined);
        newComponent.setItalic(this.currentItalic);
        newComponent.setStrikethrough(this.currentStrikethrough);
        newComponent.setObfuscated(this.currentObfuscated);
        newComponent.setInsertion(this.currentInsertion);
        newComponent.setClickEvent(this.currentClickEvent);
        newComponent.setHoverEvent(this.currentHoverEvent);
        this.components.add(newComponent);
        return this;
    }

    public ChatBuilder bold(boolean bold) {
        this.currentBold = bold;
        return this;
    }

    public ChatBuilder underlined(boolean underlined) {
        this.currentUnderlined = underlined;
        return this;
    }

    public ChatBuilder italic(boolean italic) {
        this.currentItalic = italic;
        return this;
    }

    public ChatBuilder strikethrough(boolean strikethrough) {
        this.currentStrikethrough = strikethrough;
        return this;
    }

    public ChatBuilder obfuscated(boolean obfuscated) {
        this.currentObfuscated = obfuscated;
        return this;
    }

    public ChatBuilder clickEvent(ClickEvent.Action action, String value) {
        this.currentClickEvent = new ClickEvent(action, value);
        return this;
    }

    public ChatBuilder clickEvent(ClickEvent.Action action, Number value) {
        this.currentClickEvent = new ClickEvent(action, value);
        return this;
    }

    public ChatBuilder hoverEvent(HoverEvent.Action action, String value) {
        this.currentHoverEvent = new HoverEvent(action, value);
        return this;
    }

    public ChatBuilder hoverEvent(HoverEvent.Action action, Chat value) {
        this.currentHoverEvent = new HoverEvent(action, value);
        return this;
    }

    public ChatBuilder color(ChatColor color) {
        this.currentColor = color;
        return this;
    }

    public Chat build() {
        return new Chat(this.components);
    }
}

