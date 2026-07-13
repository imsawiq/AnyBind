package org.sawiq.anybind.bind.action;

import com.google.gson.JsonObject;
import org.sawiq.anybind.Anybind;
import org.sawiq.anybind.client.files.DesktopLauncher;

import java.io.IOException;
import java.net.URI;
import java.util.Locale;
import net.minecraft.client.Minecraft;

public class OpenUrlAction extends Action {

    private String url;

    public OpenUrlAction(String url) {
        this.url = url == null ? "" : url;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url == null ? "" : url;
    }

    @Override
    public ActionType type() {
        return ActionType.OPEN_URL;
    }

    @Override
    public String summary() {
        return url.isBlank() ? "—" : url;
    }

    @Override
    public void execute(Minecraft client) {
        String target = url.trim();
        if (target.isEmpty()) {
            return;
        }
        if (!target.contains("://")) {
            target = "https://" + target;
        }
        try {
            URI uri = URI.create(target);
            String scheme = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase(Locale.ROOT);
            if (!scheme.equals("http") && !scheme.equals("https")) {
                sendStatus(client, "anybind.msg.invalid_url");
                return;
            }
            DesktopLauncher.browse(uri);
        } catch (IllegalArgumentException | IOException | SecurityException exception) {
            sendStatus(client, "anybind.msg.invalid_url");
            Anybind.LOGGER.warn("AnyBind: failed to open URL '{}'", target, exception);
        }
    }

    @Override
    protected void writeJson(JsonObject json) {
        json.addProperty("url", url);
    }

    static OpenUrlAction read(JsonObject json) {
        return new OpenUrlAction(json.has("url") ? json.get("url").getAsString() : "");
    }
}
