package org.sawiq.anybind.mixin;

import net.minecraft.client.Options;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(OptionsSubScreen.class)
public interface GameOptionsScreenAccessor {

    @Accessor("lastScreen")
    Screen anybind$getParent();

    @Accessor("options")
    Options anybind$getGameOptions();
}
