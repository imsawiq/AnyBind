package org.sawiq.anybind.gui.editor;

import org.lwjgl.glfw.GLFW;
import org.sawiq.anybind.bind.Bind;
import org.sawiq.anybind.bind.Modifier;
import org.sawiq.anybind.bind.action.Action;
import org.sawiq.anybind.bind.action.ActionType;
import org.sawiq.anybind.client.gui.UiColors;

import java.util.EnumSet;
import java.util.function.BooleanSupplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;

public final class BindEditorForm {

    private static final int NAME_MAX_LENGTH = 64;

    private final Bind bind;
    private final Font font;
    private final BindEditorRows rows;
    private final int formWidth;
    private final Runnable rebuildScreen;

    private EditBox nameField;
    private Button keyButton;
    private ActionEditorForm actionEditor;
    private boolean isCapturingKey;

    public BindEditorForm(
            Minecraft client,
            Bind bind,
            Font font,
            BindEditorRows rows,
            int formWidth,
            BooleanSupplier isScreenActive,
            Runnable rebuildScreen
    ) {
        this.bind = bind;
        this.font = font;
        this.rows = rows;
        this.formWidth = formWidth;
        this.rebuildScreen = rebuildScreen;
        buildRows(client, isScreenActive);
    }

    public boolean handleKeyCapture(int keyCode, String keyTranslation, int modifiers) {
        if (!isCapturingKey) {
            return false;
        }
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            finishKeyCapture();
            return true;
        }
        if (!isModifierKey(keyCode)) {
            bind.addKeyTranslation(keyTranslation);
            applyModifiers(modifiers);
            finishKeyCapture();
        }
        return true;
    }

    public boolean handleMouseCapture(String keyTranslation, int modifiers) {
        if (!isCapturingKey) {
            return false;
        }
        bind.addKeyTranslation(keyTranslation);
        applyModifiers(modifiers);
        finishKeyCapture();
        return true;
    }

    public boolean isCapturingKey() {
        return isCapturingKey;
    }

    public void flush() {
        bind.setName(nameField.getValue());
        actionEditor.flush();
    }

    private void buildRows(Minecraft client, BooleanSupplier isScreenActive) {
        nameField = textField(bind.getName(), Component.translatable("anybind.placeholder.name"));
        Button enabledButton = Button.builder(enabledLabel(), button -> {
            bind.setEnabled(!bind.isEnabled());
            button.setMessage(enabledLabel());
            button.setTooltip(enabledTooltip());
        }).bounds(0, 0, 82, 20).tooltip(enabledTooltip()).build();
        rows.addTextInput(Component.translatable("anybind.edit.name"), nameField, formWidth, enabledButton);

        keyButton = Button.builder(keyLabel(), button -> {
            isCapturingKey = true;
            keyButton.setMessage(UiColors.warning(Component.translatable("anybind.edit.press_key")));
        }).bounds(0, 0, 120, 20).build();
        Button clearKeyButton = Button.builder(Component.literal("✖"), button -> {
            bind.clearKeyTranslations();
            finishKeyCapture();
        }).bounds(0, 0, 20, 20).build();
        clearKeyButton.setTooltip(Tooltip.create(Component.translatable("anybind.tooltip.clear_key")));
        rows.addKeyInput(keyButton, clearKeyButton, formWidth);
        rows.addHelp(Component.translatable("anybind.help.keys"), formWidth);

        Button actionTypeButton = Button.builder(actionTypeLabel(), button -> changeActionType())
                .bounds(0, 0, formWidth, 20)
                .build();
        rows.addSingleWidget(Component.translatable("anybind.edit.action"), actionTypeButton, formWidth);

        actionEditor = new ActionEditorForm(client, bind, font, rows, formWidth, isScreenActive);
    }

    private void changeActionType() {
        ActionType nextType = nextValue(ActionType.values(), bind.getActionType());
        flush();
        bind.setAction(Action.createDefault(nextType));
        bind.setConfirmed(false);
        rebuildScreen.run();
    }

    private EditBox textField(String value, Component placeholder) {
        EditBox field = new EditBox(font, 0, 0, 100, 20, placeholder);
        field.setMaxLength(NAME_MAX_LENGTH);
        field.setValue(value);
        field.setHint(placeholder);
        return field;
    }

    private void finishKeyCapture() {
        isCapturingKey = false;
        keyButton.setMessage(keyLabel());
    }

    private void applyModifiers(int glfwModifiers) {
        EnumSet<Modifier> modifiers = bind.getModifiers();
        modifiers.clear();
        for (Modifier modifier : Modifier.values()) {
            if ((glfwModifiers & modifier.bit()) != 0) {
                modifiers.add(modifier);
            }
        }
    }

    private static boolean isModifierKey(int keyCode) {
        return keyCode == GLFW.GLFW_KEY_LEFT_CONTROL || keyCode == GLFW.GLFW_KEY_RIGHT_CONTROL
                || keyCode == GLFW.GLFW_KEY_LEFT_SHIFT || keyCode == GLFW.GLFW_KEY_RIGHT_SHIFT
                || keyCode == GLFW.GLFW_KEY_LEFT_ALT || keyCode == GLFW.GLFW_KEY_RIGHT_ALT;
    }

    private Component actionTypeLabel() {
        return Component.translatable("anybind.edit.action")
                .append(": ")
                .append(Component.translatable(bind.getActionType().translationKey()));
    }

    private Component enabledLabel() {
        return bind.isEnabled()
                ? UiColors.success(Component.translatable("anybind.edit.enabled"))
                : UiColors.faded(Component.translatable("anybind.edit.disabled"));
    }

    private Tooltip enabledTooltip() {
        return Tooltip.create(Component.translatable(bind.isEnabled()
                ? "anybind.tooltip.enabled"
                : "anybind.tooltip.disabled"));
    }

    private Component keyLabel() {
        return bind.isBound()
                ? Component.literal(bind.comboDisplay())
                : UiColors.muted(Component.translatable("anybind.edit.unbound"));
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
