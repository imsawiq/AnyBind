package org.sawiq.anybind.bind.action;

import com.google.gson.JsonObject;
import org.sawiq.anybind.Anybind;
import org.sawiq.anybind.client.files.DesktopLauncher;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import net.minecraft.client.Minecraft;

public class OpenPathAction extends Action {

    private String path;

    public OpenPathAction(String path) {
        this.path = path == null ? "" : path;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path == null ? "" : path;
    }

    @Override
    public ActionType type() {
        return ActionType.OPEN_PATH;
    }

    @Override
    public String summary() {
        return path.isBlank() ? "—" : path;
    }

    @Override
    public void execute(Minecraft client) {
        String target = path.trim();
        if (target.isEmpty()) {
            return;
        }
        try {
            Path selectedPath = Path.of(target).toAbsolutePath().normalize();
            if (!Files.exists(selectedPath)) {
                sendStatus(client, "anybind.msg.path_not_found");
                return;
            }
            DesktopLauncher.open(selectedPath);
        } catch (InvalidPathException | IOException | SecurityException exception) {
            sendStatus(client, "anybind.msg.action_failed");
            Anybind.LOGGER.warn("AnyBind: failed to open path '{}'", target, exception);
        }
    }

    @Override
    protected void writeJson(JsonObject json) {
        json.addProperty("path", path);
    }

    static OpenPathAction read(JsonObject json) {
        return new OpenPathAction(json.has("path") ? json.get("path").getAsString() : "");
    }
}
