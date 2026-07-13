package org.sawiq.anybind.mixin;

import org.lwjgl.glfw.GLFW;
import org.sawiq.anybind.bind.Bind;
import org.sawiq.anybind.bind.Modifier;
import org.sawiq.anybind.client.ClientScreens;
import org.sawiq.anybind.client.KeyCapture;
import org.sawiq.anybind.client.Rebuildable;
import org.sawiq.anybind.config.BindConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import com.mojang.blaze3d.platform.InputConstants;
import java.util.EnumSet;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.controls.KeyBindsList;
import net.minecraft.client.gui.screens.options.controls.KeyBindsScreen;

@Mixin(KeyBindsScreen.class)
public abstract class KeybindsScreenMixin extends Screen implements Rebuildable {

    private KeybindsScreenMixin() {
        super(null);
    }

    @Override
    public void anybind$rebuild() {
        GameOptionsScreenAccessor self = (GameOptionsScreenAccessor) this;
        KeyBindsList currentList = ((KeybindsScreenAccessor) this).anybind$getControlsList();
        double scroll = currentList == null ? 0.0 : currentList.scrollAmount();

        KeyBindsScreen nextScreen = new KeyBindsScreen(self.anybind$getParent(), self.anybind$getGameOptions());
        ClientScreens.show(minecraft, nextScreen);

        KeyBindsList newList = ((KeybindsScreenAccessor) nextScreen).anybind$getControlsList();
        if (newList != null) {
            newList.setScrollAmount(scroll);
        }
    }

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void anybind$captureKey(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if (!KeyCapture.isCapturing()) {
            return;
        }
        Bind bind = KeyCapture.current();
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            KeyCapture.cancel();
        } else if (!isModifierKey(keyCode)) {
            bind.addKeyTranslation(InputConstants.getKey(keyCode, scanCode).getName());
            applyModifiers(bind, modifiers);
            BindConfig.get().save();
            KeyCapture.cancel();
        }
        refresh();
        cir.setReturnValue(true);
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void anybind$captureMouse(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (!KeyCapture.isCapturing()) {
            return;
        }
        Bind bind = KeyCapture.current();
        bind.addKeyTranslation(InputConstants.Type.MOUSE.getOrCreate(button).getName());
        applyModifiers(bind, currentModifiers());
        BindConfig.get().save();
        KeyCapture.cancel();
        refresh();
        cir.setReturnValue(true);
    }

    private void applyModifiers(Bind bind, int glfwMods) {
        EnumSet<Modifier> mods = bind.getModifiers();
        mods.clear();
        for (Modifier m : Modifier.values()) {
            if ((glfwMods & m.bit()) != 0) {
                mods.add(m);
            }
        }
    }

    private int currentModifiers() {
        long handle = minecraft.getWindow().getWindow();
        int mods = 0;
        for (Modifier m : Modifier.values()) {
            if (InputConstants.isKeyDown(handle, m.leftKey()) || InputConstants.isKeyDown(handle, m.rightKey())) {
                mods |= m.bit();
            }
        }
        return mods;
    }

    private boolean isModifierKey(int keyCode) {
        return keyCode == GLFW.GLFW_KEY_LEFT_CONTROL || keyCode == GLFW.GLFW_KEY_RIGHT_CONTROL
                || keyCode == GLFW.GLFW_KEY_LEFT_SHIFT || keyCode == GLFW.GLFW_KEY_RIGHT_SHIFT
                || keyCode == GLFW.GLFW_KEY_LEFT_ALT || keyCode == GLFW.GLFW_KEY_RIGHT_ALT;
    }

    private void refresh() {
        anybind$rebuild();
    }
}
