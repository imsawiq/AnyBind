package org.sawiq.anybind.gui;

import org.sawiq.anybind.bind.Bind;
import org.sawiq.anybind.client.ClientScreens;
import org.sawiq.anybind.client.gui.UiColors;

import java.util.function.Consumer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ConfirmActionScreen extends Screen {

    private final Screen parent;
    private final Bind bind;
    private final Consumer<Boolean> onConfirm;
    private boolean rememberChoice;
    private Button rememberButton;

    public ConfirmActionScreen(Screen parent, Bind bind, Consumer<Boolean> onConfirm) {
        super(Component.translatable("anybind.confirm.title"));
        this.parent = parent;
        this.bind = bind;
        this.onConfirm = onConfirm;
    }

    @Override
    protected void init() {
        int centerX = width / 2;
        int y = height / 2 + 20;

        rememberButton = Button.builder(rememberLabel(), button -> {
            rememberChoice = !rememberChoice;
            rememberButton.setMessage(rememberLabel());
        }).bounds(centerX - 154, y, 308, 20).build();
        addRenderableWidget(rememberButton);

        addRenderableWidget(Button.builder(
                UiColors.warning(Component.translatable("anybind.confirm.run")), button -> {
                    ClientScreens.show(minecraft, parent);
                    onConfirm.accept(rememberChoice);
                }).bounds(centerX - 154, y + 24, 150, 20).build());

        addRenderableWidget(Button.builder(
                Component.translatable("gui.cancel"), button -> ClientScreens.show(minecraft, parent))
                .bounds(centerX + 4, y + 24, 150, 20).build());
    }

    private Component rememberLabel() {
        return Component.translatable(rememberChoice ? "anybind.checkbox.checked" : "anybind.checkbox.unchecked",
                Component.translatable("anybind.confirm.remember"));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        super.render(graphics, mouseX, mouseY, delta);
        int centerX = width / 2;
        int y = height / 2 - 60;
        graphics.drawCenteredString(font, title, centerX, y, UiColors.WARNING);
        graphics.drawCenteredString(font,
                Component.translatable("anybind.confirm.bind", bind.getName()), centerX, y + 20, UiColors.TEXT);
        graphics.drawCenteredString(font,
                Component.translatable(bind.getActionType().translationKey()), centerX, y + 34, UiColors.INFO);
        graphics.drawCenteredString(font,
                Component.literal(trim(bind.getAction().summary(), 60)), centerX, y + 48, UiColors.MUTED);
        graphics.drawCenteredString(font,
                Component.translatable("anybind.confirm.warning"), centerX, y + 68, UiColors.FADED);
    }

    private String trim(String text, int maxLength) {
        return text.length() <= maxLength ? text : text.substring(0, maxLength - 1) + "…";
    }

    @Override
    public void onClose() {
        ClientScreens.show(minecraft, parent);
    }
}
