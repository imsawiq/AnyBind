package org.sawiq.anybind.gui.editor;

import org.sawiq.anybind.bind.Bind;
import org.sawiq.anybind.bind.action.GameChatAction;
import org.sawiq.anybind.bind.action.OpenPathAction;
import org.sawiq.anybind.bind.action.OpenUrlAction;
import org.sawiq.anybind.bind.action.RunCommandAction;
import org.sawiq.anybind.client.files.CommandFileSelection;
import org.sawiq.anybind.client.files.NativeFileDialogs;
import org.sawiq.anybind.client.gui.UiColors;
import org.sawiq.anybind.config.BindConfig;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;

final class ActionEditorForm {

    private static final int PATH_MAX_LENGTH = 512;
    private static final int MESSAGE_MAX_LENGTH = 256;
    private static final int COMMAND_MAX_LENGTH = 1024;

    private final Minecraft client;
    private final Bind bind;
    private final BindConfig config = BindConfig.get();
    private final Font font;
    private final BindEditorRows rows;
    private final int formWidth;
    private final BooleanSupplier isScreenActive;
    private final List<Runnable> pendingFieldUpdates = new ArrayList<>();

    private EditBox commandField;

    ActionEditorForm(
            Minecraft client,
            Bind bind,
            Font font,
            BindEditorRows rows,
            int formWidth,
            BooleanSupplier isScreenActive
    ) {
        this.client = client;
        this.bind = bind;
        this.font = font;
        this.rows = rows;
        this.formWidth = formWidth;
        this.isScreenActive = isScreenActive;
        buildRows();
    }

    void flush() {
        pendingFieldUpdates.forEach(Runnable::run);
    }

    private void buildRows() {
        switch (bind.getAction().type()) {
            case OPEN_URL -> addUrlRows((OpenUrlAction) bind.getAction());
            case OPEN_PATH -> addPathRows((OpenPathAction) bind.getAction());
            case GAME_CHAT -> addChatRows((GameChatAction) bind.getAction());
            case RUN_COMMAND -> addCommandRows((RunCommandAction) bind.getAction());
        }
    }

    private void addUrlRows(OpenUrlAction action) {
        EditBox urlField = textField(PATH_MAX_LENGTH, action.getUrl(), "anybind.placeholder.url");
        pendingFieldUpdates.add(() -> action.setUrl(urlField.getValue()));
        rows.addTextInput(Component.translatable("anybind.param.url"), urlField, formWidth);
        rows.addHelp(Component.translatable("anybind.help.url"), formWidth);
    }

    private void addPathRows(OpenPathAction action) {
        EditBox pathField = textField(PATH_MAX_LENGTH, action.getPath(), "anybind.placeholder.path");
        pendingFieldUpdates.add(() -> action.setPath(pathField.getValue()));

        Button fileButton = Button.builder(Component.translatable("anybind.button.file"), button ->
                applySelection(button, NativeFileDialogs.chooseFile(
                        Component.translatable("anybind.dialog.open_file").getString(),
                        pathField.getValue(),
                        List.of(),
                        null
                ), selected -> pathField.setValue(selected.toString())))
                .bounds(0, 0, 64, 20).build();
        Button folderButton = Button.builder(Component.translatable("anybind.button.folder"), button ->
                applySelection(button, NativeFileDialogs.chooseFolder(
                        Component.translatable("anybind.dialog.open_folder").getString(),
                        pathField.getValue()
                ), selected -> pathField.setValue(selected.toString())))
                .bounds(0, 0, 64, 20).build();

        rows.addTextInput(
                Component.translatable("anybind.param.path"),
                pathField,
                formWidth,
                fileButton,
                folderButton
        );
        addDangerRows(Component.translatable("anybind.help.path"));
    }

    private void addChatRows(GameChatAction action) {
        EditBox messageField = textField(MESSAGE_MAX_LENGTH, action.getMessage(), "anybind.placeholder.message");
        pendingFieldUpdates.add(() -> action.setMessage(messageField.getValue()));
        rows.addTextInput(Component.translatable("anybind.param.message"), messageField, formWidth);
        rows.addHelp(Component.translatable("anybind.help.message"), formWidth);
    }

    private void addCommandRows(RunCommandAction action) {
        Button shellButton = Button.builder(shellLabel(action), button -> {
            action.setShell(nextValue(RunCommandAction.Shell.values(), action.getShell()));
            button.setMessage(shellLabel(action));
            updateCommandPlaceholder(action);
        }).bounds(0, 0, formWidth, 20).build();
        rows.addShellSelector(shellButton, action, formWidth);

        commandField = textField(COMMAND_MAX_LENGTH, action.getCommand(), null);
        EditBox workingDirectoryField = textField(
                PATH_MAX_LENGTH,
                action.getWorkingDir(),
                "anybind.placeholder.workdir"
        );
        updateCommandPlaceholder(action);
        pendingFieldUpdates.add(() -> action.setCommand(commandField.getValue()));

        Button scriptButton = Button.builder(Component.translatable("anybind.button.script"), button -> {
            RunCommandAction.Shell selectedShell = action.getShell();
            applySelection(button, NativeFileDialogs.chooseFile(
                    Component.translatable("anybind.dialog.select_script").getString(),
                    workingDirectoryField.getValue(),
                    CommandFileSelection.filterPatterns(selectedShell),
                    Component.translatable(CommandFileSelection.filterTranslationKey(selectedShell)).getString()
            ), selected -> {
                commandField.setValue(CommandFileSelection.formatCommand(selectedShell, selected));
                if (workingDirectoryField.getValue().isBlank() && selected.getParent() != null) {
                    workingDirectoryField.setValue(selected.getParent().toString());
                }
            });
        }).bounds(0, 0, 70, 20).build();
        rows.addTextInput(Component.translatable("anybind.param.command"), commandField, formWidth, scriptButton);

        pendingFieldUpdates.add(() -> action.setWorkingDir(workingDirectoryField.getValue()));
        Button workingDirectoryButton = Button.builder(Component.translatable("anybind.button.folder"), button ->
                applySelection(button, NativeFileDialogs.chooseFolder(
                        Component.translatable("anybind.dialog.select_workdir").getString(),
                        workingDirectoryField.getValue()
                ), selected -> workingDirectoryField.setValue(selected.toString())))
                .bounds(0, 0, 70, 20).build();
        rows.addTextInput(
                Component.translatable("anybind.param.workdir"),
                workingDirectoryField,
                formWidth,
                workingDirectoryButton
        );

        addDangerRows(Component.translatable("anybind.help.command"));
    }

    private void addDangerRows(Component helpText) {
        Button executionButton = Button.builder(executionLabel(), button -> {
            boolean isAllowed = config.getSettings().isAllowCommandExecution();
            config.getSettings().setAllowCommandExecution(!isAllowed);
            config.save();
            button.setMessage(executionLabel());
        }).bounds(0, 0, formWidth, 20)
                .tooltip(Tooltip.create(Component.translatable("anybind.tooltip.exec")))
                .build();
        rows.addSingleWidget(Component.translatable("anybind.param.execution"), executionButton, formWidth);
        rows.addHelp(UiColors.warning(helpText.copy()), formWidth);
    }

    private EditBox textField(int maxLength, String value, String placeholderKey) {
        Component placeholder = placeholderKey == null ? Component.empty() : Component.translatable(placeholderKey);
        EditBox field = new EditBox(font, 0, 0, 100, 20, placeholder);
        field.setMaxLength(maxLength);
        field.setValue(value);
        field.setHint(placeholder);
        return field;
    }

    private void applySelection(
            Button button,
            CompletableFuture<Optional<Path>> selection,
            Consumer<Path> applySelectedPath
    ) {
        button.active = false;
        selection.whenComplete((selectedPath, exception) -> client.execute(() -> {
            button.active = true;
            if (exception == null && isScreenActive.getAsBoolean()) {
                selectedPath.ifPresent(applySelectedPath);
            }
        }));
    }

    private void updateCommandPlaceholder(RunCommandAction action) {
        String translationKey = switch (action.getShell()) {
            case CMD -> "anybind.placeholder.command.cmd";
            case POWERSHELL, POWERSHELL_CORE -> "anybind.placeholder.command.powershell";
            case BASH, SH -> "anybind.placeholder.command.shell";
            case DIRECT -> "anybind.placeholder.command.direct";
        };
        commandField.setHint(Component.translatable(translationKey));
    }

    private Component shellLabel(RunCommandAction action) {
        return Component.translatable("anybind.param.shell")
                .append(": ")
                .append(Component.translatable(action.getShell().translationKey()));
    }

    private Component executionLabel() {
        boolean isAllowed = config.getSettings().isAllowCommandExecution();
        return Component.translatable(
                "anybind.button.exec",
                Component.translatable(isAllowed ? "anybind.on" : "anybind.off")
        ).withColor((isAllowed ? UiColors.SUCCESS : UiColors.DANGER) & 0xFFFFFF);
    }

    private static <T> T nextValue(T[] values, T current) {
        for (int index = 0; index < values.length; index++) {
            if (values[index] == current) {
                return values[(index + 1) % values.length];
            }
        }
        return values[0];
    }
}
