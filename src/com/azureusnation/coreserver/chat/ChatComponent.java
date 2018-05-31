/*
 * Decompiled with CFR 0_129.
 */
package com.azureusnation.coreserver.chat;

import com.azureusnation.coreserver.chat.Chat;
import com.azureusnation.coreserver.chat.ChatColor;
import com.azureusnation.coreserver.chat.ClickEvent;
import com.azureusnation.coreserver.chat.HoverEvent;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;

public class ChatComponent {
    private String text = "";
    private ChatColor color = ChatColor.WHITE;
    private boolean bold = false;
    private boolean underlined = false;
    private boolean italic = false;
    private boolean strikethrough = false;
    private boolean obfuscated = false;
    private String insertion = "";
    private ClickEvent clickEvent = null;
    private HoverEvent hoverEvent = null;

    public void write(JsonWriter writer) {
        try {
            writer.beginObject();
            writer.name("color");
            writer.value(this.color.name().toLowerCase());
            writer.name("bold");
            writer.value(this.bold);
            writer.name("underlined");
            writer.value(this.underlined);
            writer.name("italic");
            writer.value(this.italic);
            writer.name("strikethrough");
            writer.value(this.strikethrough);
            writer.name("obfuscated");
            writer.value(this.obfuscated);
            writer.name("text");
            writer.value(this.text);
            if (!this.insertion.isEmpty()) {
                writer.name("insertion");
                writer.value(this.insertion);
            }
            if (this.clickEvent != null) {
                writer.name("clickEvent");
                writer.beginObject();
                writer.name("action");
                writer.value(this.clickEvent.getAction().name().toLowerCase());
                writer.name("value");
                if (this.clickEvent.getValue() instanceof String) {
                    writer.value((String)this.clickEvent.getValue());
                } else if (this.clickEvent.getValue() instanceof Number) {
                    writer.value((Number)this.clickEvent.getValue());
                }
                writer.endObject();
            }
            if (this.hoverEvent != null) {
                writer.name("hoverEvent");
                writer.beginObject();
                writer.name("action");
                writer.value(this.hoverEvent.getAction().name().toLowerCase());
                writer.name("value");
                if (this.hoverEvent.getValue() instanceof String) {
                    writer.value((String)this.hoverEvent.getValue());
                } else if (this.hoverEvent.getValue() instanceof ChatComponent) {
                    ((ChatComponent)this.hoverEvent.getValue()).write(writer);
                } else if (this.hoverEvent.getValue() instanceof Chat) {
                    ((Chat)this.hoverEvent.getValue()).write(writer);
                }
                writer.endObject();
            }
            writer.endObject();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    String getText() {
        return this.text;
    }

    void setText(String text) {
        this.text = text;
    }

    ChatColor getColor() {
        return this.color;
    }

    void setColor(ChatColor color) {
        this.color = color;
    }

    boolean isBold() {
        return this.bold;
    }

    void setBold(boolean bold) {
        this.bold = bold;
    }

    boolean isUnderlined() {
        return this.underlined;
    }

    void setUnderlined(boolean underlined) {
        this.underlined = underlined;
    }

    boolean isItalic() {
        return this.italic;
    }

    void setItalic(boolean italic) {
        this.italic = italic;
    }

    boolean isStrikethrough() {
        return this.strikethrough;
    }

    void setStrikethrough(boolean strikethrough) {
        this.strikethrough = strikethrough;
    }

    boolean isObfuscated() {
        return this.obfuscated;
    }

    void setObfuscated(boolean obfuscated) {
        this.obfuscated = obfuscated;
    }

    String getInsertion() {
        return this.insertion;
    }

    void setInsertion(String insertion) {
        this.insertion = insertion;
    }

    ClickEvent getClickEvent() {
        return this.clickEvent;
    }

    void setClickEvent(ClickEvent clickEvent) {
        this.clickEvent = clickEvent;
    }

    HoverEvent getHoverEvent() {
        return this.hoverEvent;
    }

    void setHoverEvent(HoverEvent hoverEvent) {
        this.hoverEvent = hoverEvent;
    }
}

