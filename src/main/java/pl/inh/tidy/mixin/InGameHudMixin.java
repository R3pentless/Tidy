package pl.inh.tidy.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pl.inh.tidy.Tidy;
import pl.inh.tidy.compat.TidyCompat;
import pl.inh.tidy.lock.SlotLockManager;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {

    private static final Identifier LOCK_OVERLAY_TEXTURE = TidyCompat.id(Tidy.MOD_ID, "lock_overlay");

    //? if <=1.19.4 {
    /*@Inject(method = "renderHotbar", at = @At("TAIL"))
    private void tidy$drawLockedHotbarOverlay(float tickDelta, net.minecraft.client.util.math.MatrixStack matrices, CallbackInfo ci) {
        // Legacy HUD rendering keeps slot locking functional but skips the hotbar overlay.
    }
    *///?} elif <=1.20.4 {
    /*@Inject(method = "renderHotbar", at = @At("TAIL"))
    private void tidy$drawLockedHotbarOverlay(float tickDelta, net.minecraft.client.gui.DrawContext context, CallbackInfo ci) {
        tidy$drawLockedHotbarOverlay(context);
    }
    *///?} else {
    @Inject(method = "renderHotbar", at = @At("TAIL"))
    private void tidy$drawLockedHotbarOverlay(net.minecraft.client.gui.DrawContext context, net.minecraft.client.render.RenderTickCounter tickCounter, CallbackInfo ci) {
        tidy$drawLockedHotbarOverlay(context);
    }
    //?}

    //? if >=1.20.1 {
    private static void tidy$drawLockedHotbarOverlay(net.minecraft.client.gui.DrawContext context) {
        PlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null || SlotLockManager.lockedSlots().isEmpty()) {
            return;
        }

        int centerX = context.getScaledWindowWidth() / 2;
        int hotbarItemY = context.getScaledWindowHeight() - 19;

        for (int slot = 0; slot < 9; slot++) {
            if (!SlotLockManager.isLocked(slot)) {
                continue;
            }

            int hotbarItemX = centerX - 88 + slot * 20;
            TidyCompat.drawGuiTexture(context, LOCK_OVERLAY_TEXTURE, hotbarItemX, hotbarItemY, 16, 16);
        }
    }
    //?}
}
