package org.sawiq.anybind.client;

import org.lwjgl.glfw.GLFW;
import org.sawiq.anybind.bind.Modifier;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;

public final class ClientKeyState {

    private ClientKeyState() {
    }

    public static boolean isKeyDown(Minecraft client, InputConstants.Key key) {
        Window window = client.getWindow();
        return switch (key.getType()) {
            case MOUSE -> GLFW.glfwGetMouseButton(window.handle(), key.getValue()) == GLFW.GLFW_PRESS;
            default -> InputConstants.isKeyDown(window, key.getValue());
        };
    }

    public static boolean isModifierDown(Minecraft client, Modifier modifier) {
        Window window = client.getWindow();
        return InputConstants.isKeyDown(window, modifier.leftKey())
                || InputConstants.isKeyDown(window, modifier.rightKey());
    }
}
