package org.sawiq.anybind.bind.action;

import com.google.gson.JsonObject;
import org.sawiq.anybind.Anybind;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;

public class RunCommandAction extends Action {

    public enum Shell {
        CMD("cmd", new String[]{"cmd.exe", "/c"}),
        POWERSHELL("powershell", new String[]{"powershell.exe", "-NoLogo", "-NoProfile", "-ExecutionPolicy", "Bypass", "-Command"}),
        POWERSHELL_CORE("pwsh", new String[]{"pwsh", "-NoLogo", "-NoProfile", "-Command"}),
        BASH("bash", new String[]{"bash", "-lc"}),
        SH("sh", new String[]{"/bin/sh", "-c"}),
        DIRECT("direct", null);

        private final String id;
        private final String[] prefix;

        Shell(String id, String[] prefix) {
            this.id = id;
            this.prefix = prefix;
        }

        public String id() {
            return id;
        }

        public String label() {
            return switch (this) {
                case CMD -> "cmd.exe";
                case POWERSHELL -> "Windows PowerShell";
                case POWERSHELL_CORE -> "PowerShell 7";
                case BASH -> "bash";
                case SH -> "sh";
                case DIRECT -> "Direct";
            };
        }

        public String translationKey() {
            return "anybind.shell." + id;
        }

        public String descriptionKey() {
            return "anybind.shell." + id + ".desc";
        }

        static Shell byId(String id) {
            for (Shell s : values()) {
                if (s.id.equalsIgnoreCase(id)) {
                    return s;
                }
            }
            return defaultShell();
        }
    }

    private Shell shell;
    private String command;
    private String workingDir;

    public RunCommandAction(Shell shell, String command, String workingDir) {
        this.shell = shell == null ? defaultShell() : shell;
        this.command = command == null ? "" : command;
        this.workingDir = workingDir == null ? "" : workingDir;
    }

    public static Shell defaultShell() {
        String osName = System.getProperty("os.name", "").toLowerCase(java.util.Locale.ROOT);
        return osName.contains("win") ? Shell.POWERSHELL : Shell.BASH;
    }

    public Shell getShell() {
        return shell;
    }

    public void setShell(Shell shell) {
        this.shell = shell == null ? defaultShell() : shell;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command == null ? "" : command;
    }

    public String getWorkingDir() {
        return workingDir;
    }

    public void setWorkingDir(String workingDir) {
        this.workingDir = workingDir == null ? "" : workingDir;
    }

    @Override
    public ActionType type() {
        return ActionType.RUN_COMMAND;
    }

    @Override
    public String summary() {
        if (command.isBlank()) {
            return "—";
        }
        return "[" + shell.label() + "] " + command;
    }

    @Override
    public void execute(Minecraft client) {
        String cmd = command.trim();
        if (cmd.isEmpty()) {
            return;
        }
        List<String> argv = new ArrayList<>();
        if (shell.prefix != null) {
            argv.addAll(List.of(shell.prefix));
            argv.add(cmd);
        } else {
            argv.addAll(splitArgs(cmd));
            if (argv.isEmpty()) {
                return;
            }
        }

        File cwd = null;
        if (!workingDir.isBlank()) {
            File f = new File(workingDir.trim());
            if (f.isDirectory()) {
                cwd = f;
            } else {
                sendStatus(client, "anybind.msg.invalid_workdir");
                return;
            }
        }

        final File dir = cwd;
        Thread t = new Thread(() -> {
            try {
                ProcessBuilder pb = new ProcessBuilder(argv);
                if (dir != null) {
                    pb.directory(dir);
                }
                pb.redirectOutput(ProcessBuilder.Redirect.DISCARD);
                pb.redirectError(ProcessBuilder.Redirect.DISCARD);
                pb.start();
                sendStatus(client, "anybind.msg.command_started");
            } catch (IOException | SecurityException exception) {
                sendStatus(client, "anybind.msg.command_failed");
                Anybind.LOGGER.warn("AnyBind: failed to run command '{}'", cmd, exception);
            }
        }, "AnyBind-command");
        t.setDaemon(true);
        t.start();
    }

    private static List<String> splitArgs(String input) {
        List<String> out = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (Character.isWhitespace(c) && !inQuotes) {
                if (cur.length() > 0) {
                    out.add(cur.toString());
                    cur.setLength(0);
                }
            } else {
                cur.append(c);
            }
        }
        if (cur.length() > 0) {
            out.add(cur.toString());
        }
        return out;
    }

    @Override
    protected void writeJson(JsonObject json) {
        json.addProperty("shell", shell.id());
        json.addProperty("command", command);
        json.addProperty("workingDir", workingDir);
    }

    static RunCommandAction read(JsonObject json) {
        Shell shell = Shell.byId(json.has("shell") ? json.get("shell").getAsString() : "");
        String command = json.has("command") ? json.get("command").getAsString() : "";
        String workingDir = json.has("workingDir") ? json.get("workingDir").getAsString() : "";
        return new RunCommandAction(shell, command, workingDir);
    }
}
