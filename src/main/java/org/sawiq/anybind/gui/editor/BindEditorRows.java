package org.sawiq.anybind.gui.editor;

import org.sawiq.anybind.bind.action.RunCommandAction;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

public interface BindEditorRows {

    void addTextInput(Component label, EditBox field, int preferredWidth, Button... sideButtons);

    void addSingleWidget(Component label, AbstractWidget widget, int preferredWidth);

    void addKeyInput(Button keyButton, Button clearButton, int preferredWidth);

    void addShellSelector(Button button, RunCommandAction action, int preferredWidth);

    void addHelp(Component text, int preferredWidth);
}
