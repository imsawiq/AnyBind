package org.sawiq.anybind.client;

import java.lang.reflect.Method;
import net.minecraft.client.gui.components.AbstractSelectionList;

/**
 * Reads and writes the scroll amount of an {@link AbstractSelectionList} across the whole
 * 1.21.1-1.21.8 range from a single artifact.
 *
 * <p>The scroll accessors were renamed by the 1.21.2 smooth-scroll refactor:
 * <ul>
 *   <li>getter: {@code getScrollAmount()} / {@code method_25341} (1.21.1) became
 *       {@code scrollAmount()} / {@code method_44387} (1.21.2+)</li>
 *   <li>setter: {@code setScrollAmount(double)} / {@code method_25307} (1.21.1) became
 *       {@code method_44382} (1.21.2+)</li>
 * </ul>
 * A direct call compiled against 1.21.8 throws {@link NoSuchMethodError} on 1.21.1, so we resolve
 * the method reflectively, trying both the dev (Mojang) names and both production (intermediary)
 * names. The first name that resolves on the actual runtime class is cached.
 */
public final class ScrollCompat {

    // Ordered so the version-appropriate accessor is picked first. Names that do not exist on the
    // running Minecraft version simply fall through to the next candidate.
    private static final String[] GETTER_NAMES = {"scrollAmount", "getScrollAmount", "method_25341", "method_44387"};
    private static final String[] SETTER_NAMES = {"setScrollAmount", "method_44382", "method_25307"};

    private static Method getter;
    private static Method setter;
    private static boolean resolved;

    private ScrollCompat() {
    }

    public static double get(AbstractSelectionList<?> list) {
        if (list == null) {
            return 0.0;
        }
        resolve(list.getClass());
        if (getter == null) {
            return 0.0;
        }
        try {
            return (double) getter.invoke(list);
        } catch (ReflectiveOperationException | ClassCastException e) {
            return 0.0;
        }
    }

    public static void set(AbstractSelectionList<?> list, double value) {
        if (list == null) {
            return;
        }
        resolve(list.getClass());
        if (setter == null) {
            return;
        }
        try {
            setter.invoke(list, value);
        } catch (ReflectiveOperationException ignored) {
            // No compatible setter on this version; leave the scroll position untouched.
        }
    }

    private static void resolve(Class<?> listClass) {
        if (resolved) {
            return;
        }
        getter = findMethod(listClass, GETTER_NAMES, double.class);
        setter = findMethod(listClass, SETTER_NAMES, void.class, double.class);
        resolved = true;
    }

    private static Method findMethod(Class<?> start, String[] names, Class<?> returnType, Class<?>... params) {
        for (String name : names) {
            for (Class<?> c = start; c != null; c = c.getSuperclass()) {
                try {
                    Method m = c.getDeclaredMethod(name, params);
                    if (m.getReturnType() == returnType) {
                        m.setAccessible(true);
                        return m;
                    }
                } catch (NoSuchMethodException ignored) {
                    // Try the next class up the hierarchy, then the next candidate name.
                }
            }
        }
        return null;
    }
}
