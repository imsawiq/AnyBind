package org.sawiq.anybind.gui;

import org.sawiq.anybind.bind.Bind;
import org.sawiq.anybind.bind.Modifier;
import org.sawiq.anybind.bind.action.RunCommandAction;
import org.sawiq.anybind.client.gui.UiColors;
import org.sawiq.anybind.gui.editor.BindEditorRows;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

final class BindEditorList extends ContainerObjectSelectionList<BindEditorList.FormEntry>
        implements BindEditorRows {

    static final int ROW_HEIGHT = 50;
    static final int MAX_FORM_WIDTH = 360;

    private final Font font;
    private final Bind bind;

    BindEditorList(Minecraft client, int width, int height, int top, Font font, Bind bind) {
        super(client, width, height, top, ROW_HEIGHT);
        this.font = font;
        this.bind = bind;
        centerListVertically = false;
    }

    @Override
    public void addTextInput(Component label, EditBox field, int preferredWidth, Button... sideButtons) {
        addEntry(new TextInputRow(label, field, List.copyOf(Arrays.asList(sideButtons)), preferredWidth));
    }

    @Override
    public void addSingleWidget(Component label, AbstractWidget widget, int preferredWidth) {
        addEntry(new SingleWidgetRow(label, widget, preferredWidth));
    }

    @Override
    public void addKeyInput(Button keyButton, Button clearButton, int preferredWidth) {
        addEntry(new KeyRow(keyButton, clearButton, preferredWidth));
    }

    @Override
    public void addShellSelector(Button button, RunCommandAction action, int preferredWidth) {
        addEntry(new ShellRow(button, action, preferredWidth));
    }

    @Override
    public void addHelp(Component text, int preferredWidth) {
        addEntry(new HelpRow(text, preferredWidth));
    }

    @Override
    public int getRowWidth() {
        return Math.min(MAX_FORM_WIDTH, Math.max(220, width - 52));
    }

    abstract class FormEntry extends ContainerObjectSelectionList.Entry<FormEntry> {

        private final List<GuiEventListener> children = new ArrayList<>();
        private final List<NarratableEntry> narratables = new ArrayList<>();
        private final int preferredWidth;

        FormEntry(int preferredWidth) {
            this.preferredWidth = preferredWidth;
        }

        final void addChild(GuiEventListener child) {
            children.add(child);
            if (child instanceof NarratableEntry narratable) {
                narratables.add(narratable);
            }
        }

        final int formX(int entryX, int entryWidth) {
            return entryX + Math.max(0, (entryWidth - preferredWidth) / 2);
        }

        final int formWidth(int entryWidth) {
            return Math.min(preferredWidth, entryWidth);
        }

        final void drawLabel(GuiGraphics graphics, Component label, int x, int y) {
            graphics.drawString(font, label, x, y, UiColors.TEXT);
        }

        @Override
        public final List<? extends GuiEventListener> children() {
            return children;
        }

        @Override
        public final List<? extends NarratableEntry> narratables() {
            return narratables;
        }
    }

    private final class TextInputRow extends FormEntry {

        private final Component label;
        private final EditBox field;
        private final List<Button> sideButtons;

        private TextInputRow(Component label, EditBox field, List<Button> sideButtons, int preferredWidth) {
            super(preferredWidth);
            this.label = label;
            this.field = field;
            this.sideButtons = sideButtons;
            addChild(field);
            sideButtons.forEach(this::addChild);
        }

        @Override
        public void render(GuiGraphics graphics, int index, int y, int x, int entryWidth, int entryHeight,
                           int mouseX, int mouseY, boolean hovered, float tickDelta) {
            int formX = formX(x, entryWidth);
            int formWidth = formWidth(entryWidth);
            drawLabel(graphics, label, formX, y + 2);

            int fieldWidth = layoutSideButtons(formX, formWidth, y);
            field.setRectangle(fieldWidth, 20, formX, y + 17);
            field.render(graphics, mouseX, mouseY, tickDelta);
            sideButtons.forEach(button -> button.render(graphics, mouseX, mouseY, tickDelta));
        }

        private int layoutSideButtons(int formX, int formWidth, int y) {
            if (sideButtons.isEmpty()) {
                return formWidth;
            }
            int sideWidth = sideButtons.size() == 1
                    ? Math.min(86, Math.max(70, formWidth / 4))
                    : Math.min(72, Math.max(58, formWidth / 5));
            int buttonsWidth = sideButtons.size() * sideWidth + (sideButtons.size() - 1) * 4;
            int fieldWidth = Math.max(60, formWidth - buttonsWidth - 6);
            int buttonX = formX + fieldWidth + 6;
            for (Button button : sideButtons) {
                button.setWidth(sideWidth);
                button.setPosition(buttonX, y + 17);
                buttonX += sideWidth + 4;
            }
            return fieldWidth;
        }
    }

    private final class SingleWidgetRow extends FormEntry {

        private final Component label;
        private final AbstractWidget widget;

        private SingleWidgetRow(Component label, AbstractWidget widget, int preferredWidth) {
            super(preferredWidth);
            this.label = label;
            this.widget = widget;
            addChild(widget);
        }

        @Override
        public void render(GuiGraphics graphics, int index, int y, int x, int entryWidth, int entryHeight,
                           int mouseX, int mouseY, boolean hovered, float tickDelta) {
            int formX = formX(x, entryWidth);
            int formWidth = formWidth(entryWidth);
            drawLabel(graphics, label, formX, y + 2);
            widget.setWidth(formWidth);
            widget.setPosition(formX, y + 17);
            widget.render(graphics, mouseX, mouseY, tickDelta);
        }
    }

    private final class KeyRow extends FormEntry {

        private final Button keyButton;
        private final Button clearButton;
        private final List<Button> modifierButtons = new ArrayList<>();

        private KeyRow(Button keyButton, Button clearButton, int preferredWidth) {
            super(preferredWidth);
            this.keyButton = keyButton;
            this.clearButton = clearButton;
            addChild(keyButton);
            addChild(clearButton);
            for (Modifier modifier : Modifier.values()) {
                Button button = Button.builder(modifierLabel(modifier), pressed -> {
                    bind.toggleModifier(modifier);
                    pressed.setMessage(modifierLabel(modifier));
                }).bounds(0, 0, 30, 20)
                        .tooltip(Tooltip.create(Component.translatable(modifier.translationKey())))
                        .build();
                modifierButtons.add(button);
                addChild(button);
            }
        }

        @Override
        public void render(GuiGraphics graphics, int index, int y, int x, int entryWidth, int entryHeight,
                           int mouseX, int mouseY, boolean hovered, float tickDelta) {
            int formX = formX(x, entryWidth);
            int formWidth = formWidth(entryWidth);
            drawLabel(graphics, Component.translatable("anybind.edit.key"), formX, y + 2);
            layoutButtons(formX, formWidth, y + 17);

            modifierButtons.forEach(button -> button.render(graphics, mouseX, mouseY, tickDelta));
            keyButton.render(graphics, mouseX, mouseY, tickDelta);
            clearButton.render(graphics, mouseX, mouseY, tickDelta);
        }

        private void layoutButtons(int formX, int formWidth, int buttonY) {
            int modifierWidth = modifierButtons.size() * 30 + Math.max(0, modifierButtons.size() - 1) * 4;
            int keyWidth = Math.max(76, formWidth - modifierWidth - 36);
            keyButton.setWidth(Math.min(150, keyWidth));
            keyButton.setPosition(formX, buttonY);
            clearButton.setPosition(keyButton.getX() + keyButton.getWidth() + 4, buttonY);

            int modifierX = clearButton.getX() + clearButton.getWidth() + 8;
            Modifier[] modifiers = Modifier.values();
            for (int index = 0; index < modifierButtons.size(); index++) {
                Button button = modifierButtons.get(index);
                button.setMessage(modifierLabel(modifiers[index]));
                button.setPosition(modifierX, buttonY);
                modifierX += button.getWidth() + 4;
            }
        }

        private Component modifierLabel(Modifier modifier) {
            boolean isSelected = bind.getModifiers().contains(modifier);
            return Component.literal(modifier.label())
                    .withStyle(isSelected ? ChatFormatting.BOLD : ChatFormatting.ITALIC)
                    .withColor((isSelected ? UiColors.ACCENT : UiColors.FADED) & 0xFFFFFF);
        }
    }

    private final class ShellRow extends FormEntry {

        private final Button button;
        private final RunCommandAction action;

        private ShellRow(Button button, RunCommandAction action, int preferredWidth) {
            super(preferredWidth);
            this.button = button;
            this.action = action;
            addChild(button);
        }

        @Override
        public void render(GuiGraphics graphics, int index, int y, int x, int entryWidth, int entryHeight,
                           int mouseX, int mouseY, boolean hovered, float tickDelta) {
            int formX = formX(x, entryWidth);
            int formWidth = formWidth(entryWidth);
            drawLabel(graphics, Component.translatable("anybind.param.shell"), formX, y + 2);
            button.setWidth(formWidth);
            button.setPosition(formX, y + 17);
            button.render(graphics, mouseX, mouseY, tickDelta);
            graphics.drawString(font, trimmed(Component.translatable(action.getShell().descriptionKey()), formWidth),
                    formX, y + 40, UiColors.MUTED);
        }
    }

    private final class HelpRow extends FormEntry {

        private final Component text;

        private HelpRow(Component text, int preferredWidth) {
            super(preferredWidth);
            this.text = text;
        }

        @Override
        public void render(GuiGraphics graphics, int index, int y, int x, int entryWidth, int entryHeight,
                           int mouseX, int mouseY, boolean hovered, float tickDelta) {
            int formX = formX(x, entryWidth);
            int formWidth = formWidth(entryWidth);
            List<FormattedCharSequence> lines = font.split(text, formWidth);
            for (int lineIndex = 0; lineIndex < Math.min(3, lines.size()); lineIndex++) {
                graphics.drawString(font, lines.get(lineIndex), formX, y + 4 + lineIndex * 10, UiColors.MUTED);
            }
        }
    }

    private Component trimmed(Component text, int width) {
        return Component.literal(font.plainSubstrByWidth(text.getString(), width));
    }
}
