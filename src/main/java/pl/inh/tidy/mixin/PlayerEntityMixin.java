package pl.inh.tidy.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pl.inh.tidy.refill.RefillHandler;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin {

    @Inject(method = "tick", at = @At("TAIL"))
    private void tidy$onTick(CallbackInfo ci) {
        PlayerEntity self = (PlayerEntity) (Object) this;
        // Only fire on client; ToolTracker is already updated via ClientTickEvents in RefillHandler
        if (!self.getWorld().isClient()) return;
    }

    // TODO: hook into item-break and item-exhaust events to trigger RefillHandler
}
