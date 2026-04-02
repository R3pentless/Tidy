package pl.inh.tidy.refill;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import pl.inh.tidy.compat.TidyCompat;

public final class ToolTracker {

    private ToolTracker() {}

    private static ItemStack lastStack = ItemStack.EMPTY;
    private static int lastSlot = -1;
    private static ItemStack lastOffhandStack = ItemStack.EMPTY;

    public static void tick(MinecraftClient client, PlayerEntity player) {
        var inv = player.getInventory();

        int selected = TidyCompat.selectedSlot(inv);
        ItemStack current = inv.getStack(selected);

        if (selected == lastSlot && !lastStack.isEmpty() && current.isEmpty()) {
            RefillHandler.onItemGone(client, player, selected, lastStack);
        }

        ItemStack currentOffhand = player.getOffHandStack();
        if (client.currentScreen == null && !lastOffhandStack.isEmpty() && currentOffhand.isEmpty()) {
            RefillHandler.onOffhandItemGone(client, player, lastOffhandStack);
        }

        lastSlot = selected;
        lastStack = current.isEmpty() ? ItemStack.EMPTY : current.copy();
        lastOffhandStack = currentOffhand.isEmpty() ? ItemStack.EMPTY : currentOffhand.copy();
    }

    public static ItemStack getLastStack() { return lastStack; }
    public static int getLastSlot()       { return lastSlot; }
}
