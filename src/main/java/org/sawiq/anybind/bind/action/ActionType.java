package org.sawiq.anybind.bind.action;

public enum ActionType {
    OPEN_URL("anybind.action.open_url", false),
    OPEN_PATH("anybind.action.open_path", true),
    RUN_COMMAND("anybind.action.run_command", true),
    GAME_CHAT("anybind.action.game_chat", false);

    private final String translationKey;
    private final boolean dangerous;

    ActionType(String translationKey, boolean dangerous) {
        this.translationKey = translationKey;
        this.dangerous = dangerous;
    }

    public String translationKey() {
        return translationKey;
    }

    public boolean dangerous() {
        return dangerous;
    }
}
