package org.sawiq.anybind.client.gui;

import org.sawiq.anybind.bind.Bind;
import org.sawiq.anybind.client.ClientScreens;
import org.sawiq.anybind.client.KeyCapture;
import org.sawiq.anybind.client.Rebuildable;
import org.sawiq.anybind.config.BindConfig;
import org.sawiq.anybind.gui.BindEditScreen;

import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.options.controls.KeyBindsList;
import net.minecraft.network.chat.Component;

public class AnyBindEntry extends KeyBindsList.Entry {

    private final Minecraft client;
    private final Bind bind;
    private final Button keyButton;
    private final Button actionButton;
    private final Button deleteButton;

    public AnyBindEntry(Minecraft client, Bind bind) {
        this.client = client;
        this.bind = bind;

        this.keyButton = Button.builder(Component.empty(), b -> KeyCapture.begin(bind))
                .bounds(0, 0, 95, 20).build();

        this.actionButton = Button.builder(Component.literal("⚙"), b ->
                        ClientScreens.show(client, new BindEditScreen(ClientScreens.current(client), bind)))
                .bounds(0, 0, 20, 20).build();
        this.actionButton.setTooltip(Tooltip.create(Component.translatable("anybind.tooltip.edit")));

        this.deleteButton = Button.builder(UiColors.danger(Component.literal("✖")), b -> {
                    KeyCapture.cancel(bind);
                    BindConfig.get().removeBind(bind);
                    BindConfig.get().save();
                    if (ClientScreens.current(client) instanceof Rebuildable r) {
                        r.anybind$rebuild();
                    }
                })
                .bounds(0, 0, 20, 20).build();
        this.deleteButton.setTooltip(Tooltip.create(Component.translatable("anybind.tooltip.delete")));

        refreshEntry();
    }

    @Override
    public void refreshEntry() {
        if (KeyCapture.isCapturing(bind)) {
            keyButton.setMessage(UiColors.warning(Component.literal("> ")
                    .append(Component.translatable("anybind.edit.add_key.short"))
                    .append(" <")));
        } else if (!bind.isBound()) {
            keyButton.setMessage(UiColors.muted(Component.translatable("anybind.edit.unbound")));
        } else {
            keyButton.setMessage(Component.literal(bind.comboDisplay()));
        }
    }

    @Override
    public void render(GuiGraphics context, int index, int y, int x, int entryWidth, int entryHeight,
                       int mouseX, int mouseY, boolean hovered, float tickDelta) {
        int right = x + entryWidth;
        int btnY = y + (entryHeight - 20) / 2;

        deleteButton.setPosition(right - 20, btnY);
        actionButton.setPosition(right - 44, btnY);
        keyButton.setPosition(right - 44 - 4 - keyButton.getWidth(), btnY);

        String name = bind.getName().isBlank() ? "(unnamed)" : bind.getName();
        int color = bind.isEnabled() ? UiColors.TEXT : UiColors.FADED;
        String prefix = bind.isEnabled() ? "" : "§o";
        int nameWidth = Math.max(20, keyButton.getX() - x - 8);
        context.drawString(client.font,
                Component.literal(prefix + client.font.plainSubstrByWidth(name, nameWidth)),
                x, y + (entryHeight - 9) / 2, color);

        keyButton.render(context, mouseX, mouseY, tickDelta);
        actionButton.render(context, mouseX, mouseY, tickDelta);
        deleteButton.render(context, mouseX, mouseY, tickDelta);
    }

    public Bind getBind() {
        return bind;
    }

    @Override
    public List<? extends GuiEventListener> children() {
        return List.of(keyButton, actionButton, deleteButton);
    }

    @Override
    public List<? extends NarratableEntry> narratables() {
        return List.of(keyButton, actionButton, deleteButton);
    }
}
