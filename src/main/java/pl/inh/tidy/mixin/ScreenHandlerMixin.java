package pl.inh.tidy.mixin;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.collection.DefaultedList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import pl.inh.tidy.compat.TidyCompat;
import pl.inh.tidy.lock.SlotLockManager;

@Mixin(ScreenHandler.class)
public abstract class ScreenHandlerMixin {

    @Shadow
    public DefaultedList<Slot> slots;

    @Overwrite
    public boolean insertItem(ItemStack stack, int startIndex, int endIndex, boolean fromLast) {
        boolean changed = false;
        int index = fromLast ? endIndex - 1 : startIndex;

        if (stack.isStackable()) {
            while (!stack.isEmpty() && (fromLast ? index >= startIndex : index < endIndex)) {
                Slot slot = this.slots.get(index);
                if (tidy$canQuickMoveMergeInto(slot)) {
                    ItemStack existingStack = slot.getStack();
                    if (!existingStack.isEmpty() && TidyCompat.sameItemAndData(stack, existingStack)) {
                        int combinedCount = existingStack.getCount() + stack.getCount();
                        int maxCount = slot.getMaxItemCount(existingStack);
                        if (combinedCount <= maxCount) {
                            stack.setCount(0);
                            existingStack.setCount(combinedCount);
                            slot.markDirty();
                            changed = true;
                        } else if (existingStack.getCount() < maxCount) {
                            stack.decrement(maxCount - existingStack.getCount());
                            existingStack.setCount(maxCount);
                            slot.markDirty();
                            changed = true;
                        }
                    }
                }

                index += fromLast ? -1 : 1;
            }
        }

        if (!stack.isEmpty()) {
            index = fromLast ? endIndex - 1 : startIndex;

            while (fromLast ? index >= startIndex : index < endIndex) {
                Slot slot = this.slots.get(index);
                if (!SlotLockManager.isLockedPlayerSlot(slot)) {
                    ItemStack existingStack = slot.getStack();
                    if (existingStack.isEmpty() && slot.canInsert(stack)) {
                        int maxCount = slot.getMaxItemCount(stack);
                        slot.setStack(stack.split(Math.min(stack.getCount(), maxCount)));
                        slot.markDirty();
                        changed = true;
                        break;
                    }
                }

                index += fromLast ? -1 : 1;
            }
        }

        return changed;
    }

    private static boolean tidy$canQuickMoveMergeInto(Slot slot) {
        return !SlotLockManager.isLockedPlayerSlot(slot) || tidy$isSelectedLockedHotbarSlot(slot);
    }

    private static boolean tidy$isSelectedLockedHotbarSlot(Slot slot) {
        return slot != null
                && slot.inventory instanceof PlayerInventory inventory
                && SlotLockManager.isLocked(slot.getIndex())
                && slot.getIndex() == TidyCompat.selectedSlot(inventory);
    }
}
