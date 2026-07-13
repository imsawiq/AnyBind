package org.sawiq.anybind.bind;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.platform.InputConstants;
import org.sawiq.anybind.bind.action.Action;
import org.sawiq.anybind.bind.action.ActionType;
import org.sawiq.anybind.bind.action.OpenUrlAction;

import java.util.EnumSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class Bind {

    private final String id;
    private String name;
    private final List<String> keyTranslations;
    private final EnumSet<Modifier> modifiers;
    private Action action;
    private boolean enabled;
    private boolean confirmed;

    public Bind(String id, String name, String keyTranslation, Set<Modifier> modifiers,
                Action action, boolean enabled, boolean confirmed) {
        this(id, name, keyTranslation == null || keyTranslation.isBlank()
                        ? List.of()
                        : List.of(keyTranslation),
                modifiers, action, enabled, confirmed);
    }

    public Bind(String id, String name, List<String> keyTranslations, Set<Modifier> modifiers,
                Action action, boolean enabled, boolean confirmed) {
        this.id = id;
        this.name = name == null ? "" : name;
        this.keyTranslations = new ArrayList<>();
        if (keyTranslations != null) {
            for (String keyTranslation : keyTranslations) {
                addKeyTranslation(keyTranslation);
            }
        }
        this.modifiers = modifiers == null ? EnumSet.noneOf(Modifier.class) : EnumSet.copyOf(modifiers);
        this.action = action == null ? new OpenUrlAction("") : action;
        this.enabled = enabled;
        this.confirmed = confirmed;
    }

    public static Bind create() {
        return new Bind(UUID.randomUUID().toString(), "New bind", "",
                EnumSet.noneOf(Modifier.class), new OpenUrlAction(""), true, false);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name == null ? "" : name;
    }

    public String getKeyTranslation() {
        return keyTranslations.isEmpty() ? "" : keyTranslations.getFirst();
    }

    public List<String> getKeyTranslations() {
        return Collections.unmodifiableList(keyTranslations);
    }

    public void setKeyTranslation(String keyTranslation) {
        clearKeyTranslations();
        addKeyTranslation(keyTranslation);
    }

    public void addKeyTranslation(String keyTranslation) {
        String normalized = keyTranslation == null ? "" : keyTranslation.trim();
        if (!normalized.isEmpty() && !keyTranslations.contains(normalized)) {
            keyTranslations.add(normalized);
        }
    }

    public void removeKeyTranslation(String keyTranslation) {
        keyTranslations.remove(keyTranslation);
    }

    public void clearKeyTranslations() {
        keyTranslations.clear();
    }

    public boolean isBound() {
        return !keyTranslations.isEmpty();
    }

    public InputConstants.Key getKey() {
        return getKeys().stream().findFirst().orElse(InputConstants.UNKNOWN);
    }

    public List<InputConstants.Key> getKeys() {
        List<InputConstants.Key> keys = new ArrayList<>();
        for (String keyTranslation : keyTranslations) {
            InputConstants.Key key = parseKey(keyTranslation);
            if (key != InputConstants.UNKNOWN) {
                keys.add(key);
            }
        }
        return keys;
    }

    private InputConstants.Key parseKey(String keyTranslation) {
        if (keyTranslation == null || keyTranslation.isEmpty()) {
            return InputConstants.UNKNOWN;
        }
        try {
            return InputConstants.getKey(keyTranslation);
        } catch (IllegalArgumentException exception) {
            return InputConstants.UNKNOWN;
        }
    }

    public EnumSet<Modifier> getModifiers() {
        return modifiers;
    }

    public void toggleModifier(Modifier m) {
        if (modifiers.contains(m)) {
            modifiers.remove(m);
        } else {
            modifiers.add(m);
        }
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        if (action != null) {
            this.action = action;
        }
    }

    public ActionType getActionType() {
        return action.type();
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public void setConfirmed(boolean confirmed) {
        this.confirmed = confirmed;
    }

    public String comboDisplay() {
        StringBuilder sb = new StringBuilder();
        for (Modifier m : Modifier.values()) {
            if (modifiers.contains(m)) {
                sb.append(m.label()).append(" + ");
            }
        }
        if (isBound()) {
            for (int i = 0; i < keyTranslations.size(); i++) {
                if (i > 0) {
                    sb.append(" / ");
                }
                sb.append(parseKey(keyTranslations.get(i)).getDisplayName().getString());
            }
        } else {
            sb.append("...");
        }
        return sb.toString();
    }

    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("id", id);
        json.addProperty("name", name);
        json.addProperty("key", getKeyTranslation());
        JsonArray keys = new JsonArray();
        for (String keyTranslation : keyTranslations) {
            keys.add(keyTranslation);
        }
        json.add("keys", keys);
        JsonArray mods = new JsonArray();
        for (Modifier m : modifiers) {
            mods.add(m.name());
        }
        json.add("modifiers", mods);
        json.add("action", action.toJson());
        json.addProperty("enabled", enabled);
        json.addProperty("confirmed", confirmed);
        return json;
    }

    public static Bind fromJson(JsonObject json) {
        String id = json.has("id") ? json.get("id").getAsString() : UUID.randomUUID().toString();
        String name = json.has("name") ? json.get("name").getAsString() : "Bind";
        List<String> keys = new ArrayList<>();
        if (json.has("keys") && json.get("keys").isJsonArray()) {
            for (var el : json.getAsJsonArray("keys")) {
                if (el.isJsonPrimitive()) {
                    keys.add(el.getAsString());
                }
            }
        }
        if (keys.isEmpty() && json.has("key")) {
            keys.add(json.get("key").getAsString());
        }
        EnumSet<Modifier> mods = EnumSet.noneOf(Modifier.class);
        if (json.has("modifiers") && json.get("modifiers").isJsonArray()) {
            for (var el : json.getAsJsonArray("modifiers")) {
                try {
                    mods.add(Modifier.valueOf(el.getAsString()));
                } catch (IllegalArgumentException ignored) {
                }
            }
        }
        Action action = json.has("action") && json.get("action").isJsonObject()
                ? Action.fromJson(json.getAsJsonObject("action"))
                : new OpenUrlAction("");
        boolean enabled = !json.has("enabled") || json.get("enabled").getAsBoolean();
        boolean confirmed = json.has("confirmed") && json.get("confirmed").getAsBoolean();
        return new Bind(id, name, keys, mods, action, enabled, confirmed);
    }
}
