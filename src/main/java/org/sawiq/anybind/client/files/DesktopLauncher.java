package org.sawiq.anybind.client.files;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.Locale;

public final class DesktopLauncher {

    private DesktopLauncher() {
    }

    public static void open(Path path) throws IOException {
        Path normalizedPath = path.toAbsolutePath().normalize();
        if (tryDesktop(Desktop.Action.OPEN, desktop -> desktop.open(normalizedPath.toFile()))) {
            return;
        }
        launchWithOperatingSystem(normalizedPath.toString());
    }

    public static void browse(URI uri) throws IOException {
        if (tryDesktop(Desktop.Action.BROWSE, desktop -> desktop.browse(uri))) {
            return;
        }
        launchWithOperatingSystem(uri.toString());
    }

    private static boolean tryDesktop(Desktop.Action action, DesktopOperation operation) throws IOException {
        try {
            if (!Desktop.isDesktopSupported()) {
                return false;
            }
            Desktop desktop = Desktop.getDesktop();
            if (!desktop.isSupported(action)) {
                return false;
            }
            operation.run(desktop);
            return true;
        } catch (UnsupportedOperationException | SecurityException exception) {
            return false;
        }
    }

    private static void launchWithOperatingSystem(String target) throws IOException {
        String osName = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        ProcessBuilder process = switch (platform(osName)) {
            case WINDOWS -> new ProcessBuilder("explorer.exe", target);
            case MACOS -> new ProcessBuilder("open", target);
            case UNIX -> new ProcessBuilder("xdg-open", target);
        };
        process.redirectOutput(ProcessBuilder.Redirect.DISCARD);
        process.redirectError(ProcessBuilder.Redirect.DISCARD);
        process.start();
    }

    private static Platform platform(String osName) {
        if (osName.contains("win")) {
            return Platform.WINDOWS;
        }
        if (osName.contains("mac")) {
            return Platform.MACOS;
        }
        return Platform.UNIX;
    }

    private enum Platform {
        WINDOWS,
        MACOS,
        UNIX
    }

    @FunctionalInterface
    private interface DesktopOperation {
        void run(Desktop desktop) throws IOException;
    }
}
