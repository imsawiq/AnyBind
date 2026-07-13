package org.sawiq.anybind.client;

import java.lang.reflect.Method;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

/**
 * Draws text across Minecraft 1.21.1-1.21.8 from a single artifact.
 *
 * <p>The return type of the relevant {@link GuiGraphics} methods changed from {@code int} to
 * {@code void}. Because the JVM includes the return type in a method descriptor, code compiled
 * against a newer version cannot directly invoke the 1.21.1 methods. Reflection resolves methods
 * by name and parameter types without requiring a particular return type, so the unused return
 * value can be ignored on every supported version.
 */
public final class DrawCompat {

    private static final String[] COMPONENT_METHOD_NAMES = {"drawString", "method_27535"};
    private static final String[] SEQUENCE_METHOD_NAMES = {"drawString", "method_35720"};

    private static Method componentMethod;
    private static Method sequenceMethod;
    private static boolean resolved;

    private DrawCompat() {
    }

    public static void drawString(GuiGraphics graphics, Font font, Component text, int x, int y, int color) {
        resolve(graphics.getClass());
        invoke(componentMethod, graphics, font, text, x, y, color);
    }

    public static void drawString(
            GuiGraphics graphics, Font font, FormattedCharSequence text, int x, int y, int color) {
        resolve(graphics.getClass());
        invoke(sequenceMethod, graphics, font, text, x, y, color);
    }

    private static void resolve(Class<?> graphicsClass) {
        if (resolved) {
            return;
        }
        componentMethod = findMethod(graphicsClass, COMPONENT_METHOD_NAMES,
                Font.class, Component.class, int.class, int.class, int.class);
        sequenceMethod = findMethod(graphicsClass, SEQUENCE_METHOD_NAMES,
                Font.class, FormattedCharSequence.class, int.class, int.class, int.class);
        resolved = true;
    }

    private static Method findMethod(Class<?> start, String[] names, Class<?>... params) {
        for (String name : names) {
            for (Class<?> type = start; type != null; type = type.getSuperclass()) {
                try {
                    Method method = type.getDeclaredMethod(name, params);
                    method.setAccessible(true);
                    return method;
                } catch (NoSuchMethodException ignored) {
                    // Try the next class in the hierarchy, then the next mapped name.
                }
            }
        }
        return null;
    }

    private static void invoke(Method method, GuiGraphics graphics, Object... args) {
        if (method == null) {
            return;
        }
        try {
            method.invoke(graphics, args);
        } catch (ReflectiveOperationException ignored) {
            // A missing text label is safer than crashing the entire screen on an unknown mapping.
        }
    }
}
