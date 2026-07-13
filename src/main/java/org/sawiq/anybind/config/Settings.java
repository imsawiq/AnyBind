package org.sawiq.anybind.config;

import com.google.gson.JsonObject;

public class Settings {

    private boolean allowCommandExecution = false;

    public boolean isAllowCommandExecution() {
        return allowCommandExecution;
    }

    public void setAllowCommandExecution(boolean allowCommandExecution) {
        this.allowCommandExecution = allowCommandExecution;
    }

    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("allowCommandExecution", allowCommandExecution);
        return json;
    }

    public void readJson(JsonObject json) {
        if (json.has("allowCommandExecution")) {
            this.allowCommandExecution = json.get("allowCommandExecution").getAsBoolean();
        }
    }
}
