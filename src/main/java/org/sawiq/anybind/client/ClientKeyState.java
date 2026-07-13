package org.sawiq.anybind.client;

import org.lwjgl.glfw.GLFW;
import org.sawiq.anybind.bind.Modifier;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;

public final class ClientKeyState {

    private ClientKeyState() {
    }

    public static boolean isKeyDown(Minecraft client, InputConstants.Key key) {
        long window = client.getWindow().getWindow();
        return switch (key.getType()) {
            case MOUSE -> GLFW.glfwGetMouseButton(window, key.getValue()) == GLFW.GLFW_PRESS;
            default -> InputConstants.isKeyDown(window, key.getValue());
        };
    }

    public static boolean isModifierDown(Minecraft client, Modifier modifier) {
        long window = client.getWindow().getWindow();
        return InputConstants.isKeyDown(window, modifier.leftKey())
                || InputConstants.isKeyDown(window, modifier.rightKey());
    }
}
