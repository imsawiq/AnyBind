package org.sawiq.anybind.client;

import org.sawiq.anybind.bind.Bind;

public final class KeyCapture {

    private static Bind capturing;

    private KeyCapture() {
    }

    public static void begin(Bind bind) {
        capturing = bind;
    }

    public static void cancel() {
        capturing = null;
    }

    public static void cancel(Bind bind) {
        if (capturing == bind) {
            capturing = null;
        }
    }

    public static boolean isCapturing() {
        return capturing != null;
    }

    public static boolean isCapturing(Bind bind) {
        return capturing == bind;
    }

    public static Bind current() {
        return capturing;
    }
}
