package org.sawiq.anybind.bind.action;

import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

public class GameChatAction extends Action {

    private String message;

    public GameChatAction(String message) {
        this.message = message == null ? "" : message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message == null ? "" : message;
    }

    @Override
    public ActionType type() {
        return ActionType.GAME_CHAT;
    }

    @Override
    public String summary() {
        return message.isBlank() ? "—" : message;
    }

    @Override
    public void execute(Minecraft client) {
        LocalPlayer player = client.player;
        if (player == null) {
            return;
        }
        String text = message.trim();
        if (text.isEmpty()) {
            return;
        }
        if (text.startsWith("/")) {
            player.connection.sendCommand(text.substring(1));
        } else {
            player.connection.sendChat(text);
        }
    }

    @Override
    protected void writeJson(JsonObject json) {
        json.addProperty("message", message);
    }

    static GameChatAction read(JsonObject json) {
        return new GameChatAction(json.has("message") ? json.get("message").getAsString() : "");
    }
}
