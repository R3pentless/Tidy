package pl.inh.tidy.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pl.inh.tidy.lock.SlotLockManager;

@Mixin(Slot.class)
public abstract class SlotMixin {

    @Inject(method = "canTakeItems", at = @At("HEAD"), cancellable = true)
    private void tidy$preventTakingFromLockedSlot(PlayerEntity player, CallbackInfoReturnable<Boolean> cir) {
        if (SlotLockManager.isLocked(player, (Slot) (Object) this)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "canInsert", at = @At("HEAD"), cancellable = true)
    private void tidy$preventInsertingIntoLockedSlot(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null && SlotLockManager.isLocked(client.player, (Slot) (Object) this)) {
            cir.setReturnValue(false);
        }
    }
}
