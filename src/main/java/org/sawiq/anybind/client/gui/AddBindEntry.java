package org.sawiq.anybind.client.gui;

import org.sawiq.anybind.bind.Bind;
import org.sawiq.anybind.client.ClientScreens;
import org.sawiq.anybind.client.Rebuildable;
import org.sawiq.anybind.config.BindConfig;

import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.options.controls.KeyBindsList;
import net.minecraft.network.chat.Component;

public class AddBindEntry extends KeyBindsList.Entry {

    private final Minecraft client;
    private final Button addButton;

    public AddBindEntry(Minecraft client) {
        this.client = client;
        this.addButton = Button.builder(
                UiColors.accent(Component.translatable("anybind.button.add")), b -> {
                    Bind bind = Bind.create();
                    BindConfig.get().addBind(bind);
                    BindConfig.get().save();
                    if (ClientScreens.current(client) instanceof Rebuildable r) {
                        r.anybind$rebuild();
                    }
                }).bounds(0, 0, 150, 20).build();
    }

    @Override
    public void refreshEntry() {
    }

    @Override
    public void render(GuiGraphics context, int index, int y, int x, int entryWidth, int entryHeight,
                       int mouseX, int mouseY, boolean hovered, float tickDelta) {
        int buttonWidth = Math.min(150, Math.max(96, entryWidth - 20));
        addButton.setWidth(buttonWidth);
        addButton.setPosition(x + entryWidth / 2 - buttonWidth / 2, y + (entryHeight - 20) / 2);
        addButton.render(context, mouseX, mouseY, tickDelta);
    }

    @Override
    public List<? extends GuiEventListener> children() {
        return List.of(addButton);
    }

    @Override
    public List<? extends NarratableEntry> narratables() {
        return List.of(addButton);
    }
}
