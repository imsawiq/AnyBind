package org.sawiq.anybind.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import org.sawiq.anybind.config.BindConfig;

public class AnybindClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        BindConfig.get();

        ClientTickEvents.END_CLIENT_TICK.register(client -> InputManager.get().tick(client));
    }
}
