package org.sawiq.anybind.gui;

import org.sawiq.anybind.bind.Bind;
import org.sawiq.anybind.bind.action.Action;
import org.sawiq.anybind.client.ClientScreens;
import org.sawiq.anybind.client.Rebuildable;
import org.sawiq.anybind.client.gui.UiColors;
import org.sawiq.anybind.config.BindConfig;
import org.sawiq.anybind.gui.editor.BindEditorForm;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
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
    public boolean keyPressed(KeyEvent keyEvent) {
        String keyTranslation = InputConstants.getKey(keyEvent).getName();
        return form.handleKeyCapture(keyEvent.key(), keyTranslation, keyEvent.modifiers())
                || super.keyPressed(keyEvent);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        String keyTranslation = InputConstants.Type.MOUSE.getOrCreate(click.button()).getName();
        return form.handleMouseCapture(keyTranslation, click.modifiers())
                || super.mouseClicked(click, doubled);
    }

    private void rebuildKeepingScroll() {
        pendingScroll = editorList.scrollAmount();
        shouldRestoreScroll = true;
        rebuildWidgets();
    }

    private void restoreScroll() {
        if (!shouldRestoreScroll) {
            return;
        }
        editorList.setScrollAmount(pendingScroll);
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
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);
        graphics.centeredText(font, title, width / 2, 12, UiColors.TITLE);
    }
}
