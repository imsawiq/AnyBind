package org.sawiq.anybind.client.gui;

import net.minecraft.network.chat.MutableComponent;

public final class UiColors {

    public static final int TITLE = 0xFFEDE6FF;
    public static final int TEXT = 0xFFD8D0E8;
    public static final int MUTED = 0xFF9E94B2;
    public static final int FADED = 0xFF777089;
    public static final int ACCENT = 0xFF7BE0C3;
    public static final int SUCCESS = 0xFF9DE38E;
    public static final int WARNING = 0xFFFFC36B;
    public static final int DANGER = 0xFFFF6F91;
    public static final int INFO = 0xFFA9B8FF;

    private UiColors() {
    }

    public static MutableComponent accent(MutableComponent text) {
        return color(text, ACCENT);
    }

    public static MutableComponent success(MutableComponent text) {
        return color(text, SUCCESS);
    }

    public static MutableComponent warning(MutableComponent text) {
        return color(text, WARNING);
    }

    public static MutableComponent danger(MutableComponent text) {
        return color(text, DANGER);
    }

    public static MutableComponent muted(MutableComponent text) {
        return color(text, MUTED);
    }

    public static MutableComponent faded(MutableComponent text) {
        return color(text, FADED);
    }

    public static MutableComponent color(MutableComponent text, int argb) {
        return text.withColor(argb & 0xFFFFFF);
    }
}
