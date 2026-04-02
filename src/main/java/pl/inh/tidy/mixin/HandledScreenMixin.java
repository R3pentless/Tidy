package pl.inh.tidy.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pl.inh.tidy.Tidy;
import pl.inh.tidy.compat.TidyCompat;
import pl.inh.tidy.keybind.TidyKeybinds;
import pl.inh.tidy.lock.SlotLockManager;
import pl.inh.tidy.screen.TidyConfigScreen;
import pl.inh.tidy.sort.SortHandler;

@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin {

    private static final Identifier LOCK_OVERLAY_TEXTURE = TidyCompat.id(Tidy.MOD_ID, "lock_overlay");

    //? if <=1.21.4 {
    /*@Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void tidy$handleInventoryKeybinds(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        MinecraftClient client = MinecraftClient.getInstance();
        PlayerEntity player = client.player;
        if (player == null) {
            return;
        }

        HandledScreen<?> screen = (HandledScreen<?>) (Object) this;
        Slot focusedSlot = ((HandledScreenAccessor) screen).tidy$getFocusedSlot();

        if (TidyKeybinds.matchesToggleSlotLockKey(keyCode, scanCode) && focusedSlot != null && SlotLockManager.isPlayerInventorySlot(player, focusedSlot)) {
            boolean wasLocked = SlotLockManager.isLocked(player, focusedSlot);
            SlotLockManager.toggle(player, focusedSlot);
            player.sendMessage(
                    TidyCompat.translatable(wasLocked ? "message.tidy.slot_unlocked" : "message.tidy.slot_locked", SlotLockManager.describeSlot(player.getInventory(), focusedSlot)),
                    true
            );
            client.getSoundManager().play(TidyCompat.buttonClick(wasLocked ? 0.85f : 1.0f));
            cir.setReturnValue(true);
            return;
        }

        if (TidyKeybinds.matchesSortKey(keyCode, scanCode)) {
            SortHandler.sortPlayerInventory(client, player);
            cir.setReturnValue(true);
            return;
        }

        if (TidyKeybinds.matchesOpenConfigKey(keyCode, scanCode)) {
            client.setScreen(TidyConfigScreen.create(screen));
            cir.setReturnValue(true);
        }
    }
    *///?} else {
    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void tidy$handleInventoryKeybinds(net.minecraft.client.input.KeyInput input, CallbackInfoReturnable<Boolean> cir) {
        MinecraftClient client = MinecraftClient.getInstance();
        PlayerEntity player = client.player;
        if (player == null) {
            return;
        }

        HandledScreen<?> screen = (HandledScreen<?>) (Object) this;
        Slot focusedSlot = ((HandledScreenAccessor) screen).tidy$getFocusedSlot();

        if (TidyKeybinds.matchesToggleSlotLockKey(input) && focusedSlot != null && SlotLockManager.isPlayerInventorySlot(player, focusedSlot)) {
            boolean wasLocked = SlotLockManager.isLocked(player, focusedSlot);
            SlotLockManager.toggle(player, focusedSlot);
            player.sendMessage(
                    TidyCompat.translatable(wasLocked ? "message.tidy.slot_unlocked" : "message.tidy.slot_locked", SlotLockManager.describeSlot(player.getInventory(), focusedSlot)),
                    true
            );
            client.getSoundManager().play(TidyCompat.buttonClick(wasLocked ? 0.85f : 1.0f));
            cir.setReturnValue(true);
            return;
        }

        if (TidyKeybinds.matchesSortKey(input)) {
            SortHandler.sortPlayerInventory(client, player);
            cir.setReturnValue(true);
            return;
        }

        if (TidyKeybinds.matchesOpenConfigKey(input)) {
            client.setScreen(TidyConfigScreen.create(screen));
            cir.setReturnValue(true);
        }
    }
    //?}

    //? if <=1.21.4 {
    /*@Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void tidy$handleMiddleClickSort(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (button != 2 || !Tidy.CONFIG.middleClickSort) {
            return;
        }

        HandledScreen<?> screen = (HandledScreen<?>) (Object) this;
        MinecraftClient client = MinecraftClient.getInstance();
        PlayerEntity player = client.player;
        if (player == null || !player.currentScreenHandler.getCursorStack().isEmpty()) {
            return;
        }

        Slot slot = ((HandledScreenAccessor) screen).tidy$getSlotAt(mouseX, mouseY);
        tidy$sortHoveredSlot(screen, client, player, slot, cir);
    }
    *///?} else {
    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void tidy$handleMiddleClickSort(net.minecraft.client.gui.Click click, boolean doubled, CallbackInfoReturnable<Boolean> cir) {
        if (click.button() != 2 || !Tidy.CONFIG.middleClickSort) {
            return;
        }

        HandledScreen<?> screen = (HandledScreen<?>) (Object) this;
        MinecraftClient client = MinecraftClient.getInstance();
        PlayerEntity player = client.player;
        if (player == null || !player.currentScreenHandler.getCursorStack().isEmpty()) {
            return;
        }

        Slot slot = ((HandledScreenAccessor) screen).tidy$getSlotAt(click.x(), click.y());
        tidy$sortHoveredSlot(screen, client, player, slot, cir);
    }
    //?}

    @Inject(method = "onMouseClick(Lnet/minecraft/screen/slot/Slot;IILnet/minecraft/screen/slot/SlotActionType;)V", at = @At("HEAD"), cancellable = true)
    private void tidy$preventLockedSlotActions(Slot slot, int slotId, int button, SlotActionType actionType, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        PlayerEntity player = client.player;
        if (player == null) {
            return;
        }

        if (slot != null && SlotLockManager.isLocked(player, slot)) {
            ci.cancel();
            return;
        }

        if (actionType == SlotActionType.SWAP && (button == 40 || (button >= 0 && button <= 8)) && SlotLockManager.isLocked(button)) {
            ci.cancel();
        }
    }

    //? if >=1.20.1 <1.21.11 {
    /*@Inject(method = "drawSlot", at = @At("TAIL"))
    private void tidy$drawLockedSlotOverlay(net.minecraft.client.gui.DrawContext context, Slot slot, CallbackInfo ci) {
        tidy$drawLockedSlotOverlay(context, slot);
    }
    *///?} elif >=1.21.11 {
    @Inject(method = "drawSlot", at = @At("TAIL"))
    private void tidy$drawLockedSlotOverlay(net.minecraft.client.gui.DrawContext context, Slot slot, int mouseX, int mouseY, CallbackInfo ci) {
        tidy$drawLockedSlotOverlay(context, slot);
    }
    //?}

    //? if >=1.20.1 {
    private static void tidy$drawLockedSlotOverlay(net.minecraft.client.gui.DrawContext context, Slot slot) {
        MinecraftClient client = MinecraftClient.getInstance();
        PlayerEntity player = client.player;
        if (player == null || !SlotLockManager.isLocked(player, slot)) {
            return;
        }

        TidyCompat.drawGuiTexture(context, LOCK_OVERLAY_TEXTURE, slot.x, slot.y, 16, 16);
    }
    //?}

    //? if <=1.19.4 {
    /*@Inject(method = "drawMouseoverTooltip", at = @At("TAIL"))
    private void tidy$drawLockedSlotTooltip(net.minecraft.client.util.math.MatrixStack matrices, int mouseX, int mouseY, CallbackInfo ci) {
        // Legacy versions skip the extra tooltip line to keep the screen mixin simple.
    }
    *///?} else {
    @Inject(method = "drawMouseoverTooltip", at = @At("TAIL"))
    private void tidy$drawLockedSlotTooltip(net.minecraft.client.gui.DrawContext context, int mouseX, int mouseY, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        PlayerEntity player = client.player;
        if (player == null) {
            return;
        }

        HandledScreen<?> screen = (HandledScreen<?>) (Object) this;
        Slot focusedSlot = ((HandledScreenAccessor) screen).tidy$getFocusedSlot();
        if (focusedSlot == null || !SlotLockManager.isLocked(player, focusedSlot)) {
            return;
        }

        context.drawTooltip(
                client.textRenderer,
                java.util.List.of(
                        TidyCompat.translatable("tooltip.tidy.locked_slot"),
                        TidyCompat.literal(
                                TidyCompat.translatable("key.tidy.toggle_slot_lock").getString()
                                        + ": "
                                        + TidyKeybinds.toggleSlotLockKeyText().getString()
                        )
                ),
                mouseX,
                mouseY
        );
    }
    //?}

    private static void tidy$sortHoveredSlot(HandledScreen<?> screen, MinecraftClient client, PlayerEntity player, Slot slot, CallbackInfoReturnable<Boolean> cir) {
        if (slot == null || !slot.hasStack()) {
            return;
        }

        if (slot.inventory == player.getInventory()) {
            SortHandler.sortPlayerInventory(client, player);
            cir.setReturnValue(true);
            return;
        }

        if (screen.getScreenHandler() instanceof GenericContainerScreenHandler) {
            SortHandler.sortOpenContainer(client, player);
            cir.setReturnValue(true);
        }
    }
}
