package org.sawiq.anybind.client;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public final class ClientMessages {

    private ClientMessages() {
    }

    public static void showOverlay(Minecraft client, String translationKey, Object... args) {
        if (client.player != null) {
            client.player.displayClientMessage(Component.translatable(translationKey, args), true);
        }
    }
}
