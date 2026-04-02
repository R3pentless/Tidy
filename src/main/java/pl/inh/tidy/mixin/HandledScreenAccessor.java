package pl.inh.tidy.mixin;

import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(HandledScreen.class)
public interface HandledScreenAccessor {

    @Accessor("x")
    int tidy$getX();

    @Accessor("y")
    int tidy$getY();

    @Accessor("backgroundWidth")
    int tidy$getBackgroundWidth();

    @Accessor("backgroundHeight")
    int tidy$getBackgroundHeight();

    @Accessor("focusedSlot")
    Slot tidy$getFocusedSlot();

    @Invoker("getSlotAt")
    Slot tidy$getSlotAt(double mouseX, double mouseY);
}
