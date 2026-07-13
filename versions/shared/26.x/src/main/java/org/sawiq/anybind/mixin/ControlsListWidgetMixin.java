package org.sawiq.anybind.mixin;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.screens.options.controls.KeyBindsList;
import net.minecraft.client.gui.screens.options.controls.KeyBindsList.CategoryEntry;
import net.minecraft.client.gui.screens.options.controls.KeyBindsScreen;
import net.minecraft.resources.Identifier;
import org.sawiq.anybind.bind.Bind;
import org.sawiq.anybind.client.gui.AddBindEntry;
import org.sawiq.anybind.client.gui.AnyBindEntry;
import org.sawiq.anybind.config.BindConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyBindsList.class)
public abstract class ControlsListWidgetMixin extends AbstractSelectionList<KeyBindsList.Entry> {

    private static final KeyMapping.Category ANYBIND_CATEGORY =
            KeyMapping.Category.register(Identifier.fromNamespaceAndPath("anybind", "category"));

    private ControlsListWidgetMixin() {
        super(null, 0, 0, 0, 0);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void anybind$appendCategory(KeyBindsScreen parent, Minecraft client, CallbackInfo ci) {
        KeyBindsList self = (KeyBindsList) (Object) this;

        addEntry(self.new CategoryEntry(ANYBIND_CATEGORY));

        for (Bind bind : BindConfig.get().getBinds()) {
            addEntry(new AnyBindEntry(client, bind));
        }
        addEntry(new AddBindEntry(client));
    }
}
