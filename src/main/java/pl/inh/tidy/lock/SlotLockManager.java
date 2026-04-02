package pl.inh.tidy.lock;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.slot.Slot;
import pl.inh.tidy.Tidy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class SlotLockManager {

    private SlotLockManager() {}

    public static boolean isPlayerInventorySlot(PlayerEntity player, Slot slot) {
        return slot != null && slot.inventory == player.getInventory();
    }

    public static boolean isLocked(PlayerEntity player, Slot slot) {
        return isPlayerInventorySlot(player, slot) && isLocked(slot.getIndex());
    }

    public static boolean isLockedPlayerSlot(Slot slot) {
        return slot != null && slot.inventory instanceof PlayerInventory && isLocked(slot.getIndex());
    }

    public static boolean isLocked(int playerInventoryIndex) {
        return Tidy.CONFIG.lockedSlots.contains(playerInventoryIndex);
    }

    public static boolean toggle(PlayerEntity player, Slot slot) {
        if (!isPlayerInventorySlot(player, slot)) {
            return false;
        }

        int slotIndex = slot.getIndex();
        List<Integer> updated = new ArrayList<>(Tidy.CONFIG.lockedSlots);
        if (updated.contains(slotIndex)) {
            updated.remove(Integer.valueOf(slotIndex));
        } else {
            updated.add(slotIndex);
            Collections.sort(updated);
        }

        Tidy.CONFIG.lockedSlots = updated;
        Tidy.CONFIG.sanitize();
        Tidy.CONFIG.save();
        return true;
    }

    public static List<Integer> lockedSlots() {
        return Tidy.CONFIG.lockedSlots;
    }

    public static String describeSlot(PlayerInventory inventory, Slot slot) {
        int index = slot.getIndex();
        if (index >= 0 && index <= 8) {
            return "hotbar " + (index + 1);
        }
        if (index >= 9 && index <= 35) {
            return "inventory " + (index - 8);
        }
        if (index == 40) {
            return "offhand";
        }
        if (index >= 36 && index <= 39) {
            return "armor";
        }
        return "slot " + index;
    }
}
