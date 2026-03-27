package pl.inh.tidy.refill;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

public final class ToolTracker {

    private ToolTracker() {}

    private static ItemStack lastStack = ItemStack.EMPTY;
    private static int lastSlot = -1;

    public static void tick(PlayerEntity player) {
        //? 1.18.2 .. 1.20.4
        /*var inv = player.inventory;*/
        //? 1.21 ..
        var inv = player.getInventory();

        int selected = inv.selectedSlot;
        ItemStack current = inv.getStack(selected);

        if (current.getItem() != lastStack.getItem() || selected != lastSlot) {
            lastStack = current.isEmpty() ? ItemStack.EMPTY : current.copy();
            lastSlot = selected;
        }
    }

    public static ItemStack getLastStack() {
        return lastStack;
    }

    public static int getLastSlot() {
        return lastSlot;
    }
}
