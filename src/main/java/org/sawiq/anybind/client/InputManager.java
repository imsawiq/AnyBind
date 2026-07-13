package org.sawiq.anybind.client;

import org.sawiq.anybind.Anybind;
import org.sawiq.anybind.bind.Bind;
import org.sawiq.anybind.bind.Modifier;
import org.sawiq.anybind.config.BindConfig;
import org.sawiq.anybind.gui.ConfirmActionScreen;

import java.util.HashSet;
import java.util.Set;
import net.minecraft.client.Minecraft;

public final class InputManager {

    private static final InputManager INSTANCE = new InputManager();

    private final Set<String> pressedBindIds = new HashSet<>();

    private InputManager() {
    }

    public static InputManager get() {
        return INSTANCE;
    }

    public void tick(Minecraft client) {
        boolean isScreenOpen = ClientScreens.current(client) != null;
        for (Bind bind : BindConfig.get().getBinds()) {
            updateBind(client, bind, isScreenOpen);
        }
    }

    private void updateBind(Minecraft client, Bind bind, boolean isScreenOpen) {
        String bindId = bind.getId();
        if (!bind.isEnabled() || !bind.isBound()) {
            pressedBindIds.remove(bindId);
            return;
        }

        boolean isPressed = isComboPressed(client, bind);
        if (isPressed && pressedBindIds.add(bindId)) {
            if (!isScreenOpen) {
                fire(client, bind);
            }
        } else if (!isPressed) {
            pressedBindIds.remove(bindId);
        }
    }

    private boolean isComboPressed(Minecraft client, Bind bind) {
        boolean hasPressedTrigger = bind.getKeys().stream()
                .anyMatch(key -> ClientKeyState.isKeyDown(client, key));
        if (!hasPressedTrigger) {
            return false;
        }

        for (Modifier modifier : Modifier.values()) {
            boolean isRequired = bind.getModifiers().contains(modifier);
            boolean isPressed = ClientKeyState.isModifierDown(client, modifier);
            if (isRequired != isPressed) {
                return false;
            }
        }
        return true;
    }

    private void fire(Minecraft client, Bind bind) {
        if (!bind.getActionType().dangerous()) {
            runAction(client, bind);
            return;
        }

        BindConfig config = BindConfig.get();
        if (!config.getSettings().isAllowCommandExecution()) {
            ClientMessages.showOverlay(client, "anybind.msg.execution_disabled");
            return;
        }
        if (bind.isConfirmed()) {
            runAction(client, bind);
            return;
        }

        ConfirmActionScreen confirmation = new ConfirmActionScreen(
                ClientScreens.current(client),
                bind,
                rememberChoice -> {
                    if (rememberChoice) {
                        bind.setConfirmed(true);
                        config.save();
                    }
                    runAction(client, bind);
                }
        );
        ClientScreens.show(client, confirmation);
    }

    private void runAction(Minecraft client, Bind bind) {
        try {
            bind.getAction().execute(client);
        } catch (RuntimeException exception) {
            Anybind.LOGGER.warn("AnyBind: action for bind '{}' failed", bind.getName(), exception);
            ClientMessages.showOverlay(client, "anybind.msg.action_failed");
        }
    }
}
