package org.sawiq.anybind.bind;

import org.lwjgl.glfw.GLFW;

public enum Modifier {
    CTRL(GLFW.GLFW_MOD_CONTROL, GLFW.GLFW_KEY_LEFT_CONTROL, GLFW.GLFW_KEY_RIGHT_CONTROL, "anybind.modifier.ctrl"),
    SHIFT(GLFW.GLFW_MOD_SHIFT, GLFW.GLFW_KEY_LEFT_SHIFT, GLFW.GLFW_KEY_RIGHT_SHIFT, "anybind.modifier.shift"),
    ALT(GLFW.GLFW_MOD_ALT, GLFW.GLFW_KEY_LEFT_ALT, GLFW.GLFW_KEY_RIGHT_ALT, "anybind.modifier.alt");

    private final int bit;
    private final int leftKey;
    private final int rightKey;
    private final String translationKey;

    Modifier(int bit, int leftKey, int rightKey, String translationKey) {
        this.bit = bit;
        this.leftKey = leftKey;
        this.rightKey = rightKey;
        this.translationKey = translationKey;
    }

    public int bit() {
        return bit;
    }

    public int leftKey() {
        return leftKey;
    }

    public int rightKey() {
        return rightKey;
    }

    public String translationKey() {
        return translationKey;
    }

    public String label() {
        String s = name().toLowerCase();
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
