package org.sawiq.anybind.client.files;

import org.sawiq.anybind.bind.action.RunCommandAction;

import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

public final class CommandFileSelection {

    private CommandFileSelection() {
    }

    public static List<String> filterPatterns(RunCommandAction.Shell shell) {
        return switch (shell) {
            case CMD -> List.of("*.bat", "*.cmd", "*.exe", "*.com");
            case POWERSHELL, POWERSHELL_CORE -> List.of("*.ps1", "*.psm1", "*.psd1");
            case BASH -> List.of("*.sh", "*.bash", "*.command");
            case SH -> List.of("*.sh");
            case DIRECT -> directExecutablePatterns();
        };
    }

    public static String filterTranslationKey(RunCommandAction.Shell shell) {
        return switch (shell) {
            case CMD -> "anybind.file_filter.cmd";
            case POWERSHELL, POWERSHELL_CORE -> "anybind.file_filter.powershell";
            case BASH -> "anybind.file_filter.bash";
            case SH -> "anybind.file_filter.sh";
            case DIRECT -> "anybind.file_filter.direct";
        };
    }

    public static String formatCommand(RunCommandAction.Shell shell, Path selectedFile) {
        String path = selectedFile.toAbsolutePath().normalize().toString();
        return switch (shell) {
            case CMD -> isWindowsBatchFile(path) ? "call " + quoteForCmd(path) : quoteForCmd(path);
            case POWERSHELL, POWERSHELL_CORE -> "& " + quoteForPowerShell(path);
            case BASH -> "bash " + quoteForPosixShell(posixPath(path));
            case SH -> "sh " + quoteForPosixShell(posixPath(path));
            case DIRECT -> quoteForDirectExecution(path);
        };
    }

    private static List<String> directExecutablePatterns() {
        String osName = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        if (osName.contains("win")) {
            return List.of("*.exe", "*.com");
        }
        if (osName.contains("mac")) {
            return List.of("*.command");
        }
        // Unix executables commonly have no extension, so filtering would hide valid files.
        return List.of();
    }

    private static boolean isWindowsBatchFile(String path) {
        String lowerPath = path.toLowerCase(Locale.ROOT);
        return lowerPath.endsWith(".bat") || lowerPath.endsWith(".cmd");
    }

    private static String quoteForCmd(String path) {
        return '"' + path + '"';
    }

    private static String quoteForPowerShell(String path) {
        return "'" + path.replace("'", "''") + "'";
    }

    private static String quoteForPosixShell(String path) {
        return "'" + path.replace("'", "'\\''") + "'";
    }

    private static String posixPath(String path) {
        return System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("win")
                ? path.replace('\\', '/')
                : path;
    }

    private static String quoteForDirectExecution(String path) {
        return '"' + path + '"';
    }
}
