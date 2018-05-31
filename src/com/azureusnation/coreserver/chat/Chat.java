/*
 * Decompiled with CFR 0_129.
 */
package com.azureusnation.coreserver.chat;

import com.azureusnation.coreserver.chat.ChatBuilder;
import com.azureusnation.coreserver.chat.ChatComponent;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.util.List;

public class Chat {
    private List<ChatComponent> components;

    public Chat(List<ChatComponent> components) {
        this.components = components;
    }

    public void write(JsonWriter writer) {
        try {
            writer.beginObject();
            writer.name("text");
            writer.value("");
            writer.name("extra");
            writer.beginArray();
            for (ChatComponent component : this.components) {
                component.write(writer);
            }
            writer.endArray();
            writer.endObject();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static ChatBuilder builder() {
        return new ChatBuilder();
    }
}

