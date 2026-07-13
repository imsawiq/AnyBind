package org.sawiq.anybind.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.fabricmc.loader.api.FabricLoader;
import org.sawiq.anybind.Anybind;
import org.sawiq.anybind.bind.Bind;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class BindConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final int FORMAT_VERSION = 1;

    private static BindConfig instance;

    private final Path file;
    private final List<Bind> binds = new ArrayList<>();
    private final Settings settings = new Settings();

    private BindConfig(Path file) {
        this.file = file;
    }

    public static BindConfig get() {
        if (instance == null) {
            Path path = FabricLoader.getInstance().getConfigDir().resolve("anybind.json");
            instance = new BindConfig(path);
            instance.load();
        }
        return instance;
    }

    public List<Bind> getBinds() {
        return binds;
    }

    public Settings getSettings() {
        return settings;
    }

    public void addBind(Bind bind) {
        binds.add(bind);
    }

    public void removeBind(Bind bind) {
        binds.remove(bind);
    }

    public void load() {
        binds.clear();
        if (!Files.exists(file)) {
            return;
        }
        try {
            String content = Files.readString(file, StandardCharsets.UTF_8);
            JsonElement root = com.google.gson.JsonParser.parseString(content);
            if (!root.isJsonObject()) {
                return;
            }
            JsonObject obj = root.getAsJsonObject();
            if (obj.has("settings") && obj.get("settings").isJsonObject()) {
                settings.readJson(obj.getAsJsonObject("settings"));
            }
            if (obj.has("binds") && obj.get("binds").isJsonArray()) {
                for (JsonElement el : obj.getAsJsonArray("binds")) {
                    if (el.isJsonObject()) {
                        try {
                            binds.add(Bind.fromJson(el.getAsJsonObject()));
                        } catch (RuntimeException exception) {
                            Anybind.LOGGER.warn("AnyBind: skipping malformed bind entry", exception);
                        }
                    }
                }
            }
        } catch (IOException | RuntimeException exception) {
            Anybind.LOGGER.error("AnyBind: failed to load config", exception);
        }
    }

    public void save() {
        JsonObject root = new JsonObject();
        root.addProperty("formatVersion", FORMAT_VERSION);
        root.add("settings", settings.toJson());
        JsonArray arr = new JsonArray();
        for (Bind bind : binds) {
            arr.add(bind.toJson());
        }
        root.add("binds", arr);
        try {
            Files.createDirectories(file.getParent());
            Files.writeString(file, GSON.toJson(root), StandardCharsets.UTF_8);
        } catch (IOException e) {
            Anybind.LOGGER.error("AnyBind: failed to save config", e);
        }
    }
}
