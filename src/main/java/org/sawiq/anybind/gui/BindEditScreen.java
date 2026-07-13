package org.sawiq.anybind.gui;

import org.sawiq.anybind.bind.Bind;
import org.sawiq.anybind.bind.Modifier;
import org.sawiq.anybind.bind.action.Action;
import org.sawiq.anybind.client.ClientScreens;
import org.sawiq.anybind.client.Rebuildable;
import org.sawiq.anybind.client.ScrollCompat;
import org.sawiq.anybind.client.gui.UiColors;
import org.sawiq.anybind.config.BindConfig;
import org.sawiq.anybind.gui.editor.BindEditorForm;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class BindEditScreen extends Screen {

    private static final int LIST_TOP = 32;
    private static final int FOOTER_HEIGHT = 40;

    private final Screen parent;
    private final Bind bind;
    private final Bind originalBind;
    private final BindConfig config = BindConfig.get();

    private BindEditorForm form;
    private BindEditorList editorList;
    private double pendingScroll;
    private boolean shouldRestoreScroll;

    public BindEditScreen(Screen parent, Bind bind) {
        super(Component.translatable("anybind.edit.title"));
        this.parent = parent;
        this.bind = bind;
        this.originalBind = Bind.fromJson(bind.toJson());
    }

    @Override
    protected void init() {
        int listHeight = Math.max(BindEditorList.ROW_HEIGHT, height - LIST_TOP - FOOTER_HEIGHT);
        editorList = new BindEditorList(minecraft, width, listHeight, LIST_TOP, font, bind);
        int formWidth = Math.min(BindEditorList.MAX_FORM_WIDTH, Math.max(220, width - 52));
        form = new BindEditorForm(
                minecraft,
                bind,
                font,
                editorList,
                formWidth,
                () -> ClientScreens.current(minecraft) == this,
                this::rebuildKeepingScroll
        );
        addRenderableWidget(editorList);
        restoreScroll();
        addFooterButtons();
    }

    private void addFooterButtons() {
        int footerY = height - 32;
        int buttonWidth = Math.min(150, Math.max(90, (width - 18) / 2));
        addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> saveAndClose())
                .bounds(width / 2 - buttonWidth - 4, footerY, buttonWidth, 20)
                .build());
        addRenderableWidget(Button.builder(Component.translatable("gui.cancel"), button -> cancelAndClose())
                .bounds(width / 2 + 4, footerY, buttonWidth, 20)
                .build());
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        String keyTranslation = InputConstants.getKey(keyCode, scanCode).getName();
        return form.handleKeyCapture(keyCode, keyTranslation, modifiers)
                || super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        String keyTranslation = InputConstants.Type.MOUSE.getOrCreate(button).getName();
        return form.handleMouseCapture(keyTranslation, currentModifiers())
                || super.mouseClicked(mouseX, mouseY, button);
    }

    private int currentModifiers() {
        long window = minecraft.getWindow().getWindow();
        int modifiers = 0;
        for (Modifier modifier : Modifier.values()) {
            if (InputConstants.isKeyDown(window, modifier.leftKey())
                    || InputConstants.isKeyDown(window, modifier.rightKey())) {
                modifiers |= modifier.bit();
            }
        }
        return modifiers;
    }

    private void rebuildKeepingScroll() {
        pendingScroll = ScrollCompat.get(editorList);
        shouldRestoreScroll = true;
        rebuildWidgets();
    }

    private void restoreScroll() {
        if (!shouldRestoreScroll) {
            return;
        }
        ScrollCompat.set(editorList, pendingScroll);
        pendingScroll = 0.0;
        shouldRestoreScroll = false;
    }

    private void saveAndClose() {
        String previousConfirmation = confirmationSignature(bind);
        form.flush();
        if (!previousConfirmation.equals(confirmationSignature(bind)) && bind.getActionType().dangerous()) {
            bind.setConfirmed(false);
        }
        config.save();
        returnToParent();
    }

    private void cancelAndClose() {
        restoreBind(originalBind);
        returnToParent();
    }

    private void restoreBind(Bind source) {
        bind.setName(source.getName());
        bind.clearKeyTranslations();
        source.getKeyTranslations().forEach(bind::addKeyTranslation);
        bind.getModifiers().clear();
        bind.getModifiers().addAll(source.getModifiers());
        bind.setAction(Action.fromJson(source.getAction().toJson()));
        bind.setEnabled(source.isEnabled());
        bind.setConfirmed(source.isConfirmed());
    }

    private String confirmationSignature(Bind target) {
        return target.getActionType().dangerous() ? target.getAction().toJson().toString() : "";
    }

    private void returnToParent() {
        if (parent instanceof Rebuildable rebuildable) {
            rebuildable.anybind$rebuild();
        } else {
            ClientScreens.show(minecraft, parent);
        }
    }

    @Override
    public void onClose() {
        cancelAndClose();
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return !form.isCapturingKey();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        super.render(graphics, mouseX, mouseY, delta);
        graphics.drawCenteredString(font, title, width / 2, 12, UiColors.TITLE);
    }
}
