package org.sawiq.anybind.client.files;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.tinyfd.TinyFileDialogs;
import org.sawiq.anybind.Anybind;

import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public final class NativeFileDialogs {

    private static final AtomicBoolean DIALOG_OPEN = new AtomicBoolean();
    private static final ExecutorService DIALOG_EXECUTOR = Executors.newSingleThreadExecutor(task -> {
        Thread thread = new Thread(task, "AnyBind-file-dialog");
        thread.setDaemon(true);
        return thread;
    });

    private NativeFileDialogs() {
    }

    public static CompletableFuture<Optional<Path>> chooseFile(
            String title,
            String currentPath,
            List<String> filterPatterns,
            String filterDescription
    ) {
        List<String> patterns = filterPatterns == null ? List.of() : List.copyOf(filterPatterns);
        String initialLocation = initialLocation(currentPath);
        return openDialog(() -> {
            try (MemoryStack stack = MemoryStack.stackPush()) {
                PointerBuffer nativePatterns = null;
                if (!patterns.isEmpty()) {
                    nativePatterns = stack.mallocPointer(patterns.size());
                    for (String pattern : patterns) {
                        nativePatterns.put(stack.UTF8(pattern));
                    }
                    nativePatterns.flip();
                }
                return TinyFileDialogs.tinyfd_openFileDialog(
                        title,
                        initialLocation,
                        nativePatterns,
                        patterns.isEmpty() ? null : filterDescription,
                        false
                );
            }
        });
    }

    public static CompletableFuture<Optional<Path>> chooseFolder(String title, String currentPath) {
        String initialLocation = initialLocation(currentPath);
        return openDialog(() -> TinyFileDialogs.tinyfd_selectFolderDialog(title, initialLocation));
    }

    private static CompletableFuture<Optional<Path>> openDialog(DialogCall dialogCall) {
        if (!DIALOG_OPEN.compareAndSet(false, true)) {
            return CompletableFuture.completedFuture(Optional.empty());
        }

        return CompletableFuture.supplyAsync(() -> {
            try {
                return normalize(dialogCall.open());
            } catch (RuntimeException | LinkageError exception) {
                Anybind.LOGGER.warn("AnyBind: native file dialog failed", exception);
                return Optional.empty();
            } finally {
                DIALOG_OPEN.set(false);
            }
        }, DIALOG_EXECUTOR);
    }

    private static Optional<Path> normalize(String selectedPath) {
        if (selectedPath == null || selectedPath.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(Path.of(selectedPath).toAbsolutePath().normalize());
        } catch (InvalidPathException exception) {
            Anybind.LOGGER.warn("AnyBind: file dialog returned an invalid path", exception);
            return Optional.empty();
        }
    }

    private static String initialLocation(String rawPath) {
        if (rawPath != null && !rawPath.isBlank()) {
            try {
                Path path = Path.of(rawPath.trim()).toAbsolutePath().normalize();
                if (Files.exists(path)) {
                    return path.toString();
                }
                Path parent = path.getParent();
                if (parent != null && Files.isDirectory(parent)) {
                    return parent.toString();
                }
            } catch (InvalidPathException ignored) {
                // Fall through to the user's home directory.
            }
        }
        return System.getProperty("user.home", ".");
    }

    @FunctionalInterface
    private interface DialogCall {
        String open();
    }
}
