package pl.inh.tidy.mixin;

import net.minecraft.screen.ScreenHandler;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ScreenHandler.class)
public abstract class ScreenHandlerMixin {
    // TODO: inject sort button into chest / barrel / shulker GUIs
}
