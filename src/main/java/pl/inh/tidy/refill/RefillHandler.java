package pl.inh.tidy.refill;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import pl.inh.tidy.Tidy;

public final class RefillHandler {

    private RefillHandler() {}

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player != null) {
                ToolTracker.tick(client.player);
            }
        });
    }

    public static void onItemBroken(PlayerEntity player, int hotbarSlot) {
        if (!Tidy.CONFIG.autoRefill) return;
        ItemStack broken = ToolTracker.getLastStack();
        if (broken.isEmpty()) return;
        tryRefill(player, broken, hotbarSlot);
    }

    public static void onStackExhausted(PlayerEntity player, int hotbarSlot) {
        if (!Tidy.CONFIG.refillBlocks) return;
        ItemStack last = ToolTracker.getLastStack();
        if (last.isEmpty()) return;
        tryRefill(player, last, hotbarSlot);
    }

    private static void tryRefill(PlayerEntity player, ItemStack target, int hotbarSlot) {
        //? 1.18.2 .. 1.20.4
        /*var inv = player.inventory;*/
        //? 1.21 ..
        var inv = player.getInventory();

        // Search main inventory (skip hotbar slots 0–8)
        for (int i = 9; i <= 35; i++) {
            ItemStack candidate = inv.getStack(i);
            if (!candidate.isEmpty() && candidate.getItem() == target.getItem()) {
                inv.setStack(hotbarSlot, candidate.copy());
                inv.setStack(i, ItemStack.EMPTY);
                return;
            }
        }
    }
}
