package org.sawiq.anybind.bind.action;

import com.google.gson.JsonObject;
import org.sawiq.anybind.client.ClientMessages;
import net.minecraft.client.Minecraft;

public abstract class Action {

    public abstract ActionType type();

    public abstract String summary();

    public abstract void execute(Minecraft client);

    protected abstract void writeJson(JsonObject json);

    public final JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("type", type().name());
        writeJson(json);
        return json;
    }

    public static Action fromJson(JsonObject json) {
        String typeName = json.has("type") ? json.get("type").getAsString() : ActionType.OPEN_URL.name();
        ActionType type;
        try {
            type = ActionType.valueOf(typeName);
        } catch (IllegalArgumentException e) {
            type = ActionType.OPEN_URL;
        }
        return switch (type) {
            case OPEN_URL -> OpenUrlAction.read(json);
            case OPEN_PATH -> OpenPathAction.read(json);
            case RUN_COMMAND -> RunCommandAction.read(json);
            case GAME_CHAT -> GameChatAction.read(json);
        };
    }

    public static Action createDefault(ActionType type) {
        return switch (type) {
            case OPEN_URL -> new OpenUrlAction("");
            case OPEN_PATH -> new OpenPathAction("");
            case RUN_COMMAND -> new RunCommandAction(RunCommandAction.defaultShell(), "", "");
            case GAME_CHAT -> new GameChatAction("");
        };
    }

    protected static void sendStatus(Minecraft client, String translationKey, Object... args) {
        if (client == null) {
            return;
        }
        client.execute(() -> {
            ClientMessages.showOverlay(client, translationKey, args);
        });
    }
}
