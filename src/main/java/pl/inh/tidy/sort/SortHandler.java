package pl.inh.tidy.sort;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import pl.inh.tidy.Tidy;

import java.util.ArrayList;
import java.util.List;

public final class SortHandler {

    private SortHandler() {}

    public static void sortPlayerInventory(PlayerEntity player) {
        //? 1.18.2 .. 1.20.4
        /*var inv = player.inventory;*/
        //? 1.21 ..
        var inv = player.getInventory();

        boolean lockHotbar = Tidy.CONFIG.lockHotbar;
        // Slots 0–8: hotbar, 9–35: main inventory, 36–39: armor, 40: offhand
        int startSlot = lockHotbar ? 9 : 0;
        int endSlot = 35;

        List<ItemStack> stacks = new ArrayList<>();
        for (int i = startSlot; i <= endSlot; i++) {
            stacks.add(inv.getStack(i).copy());
        }

        stacks = mergeStacks(stacks);

        SortContext ctx = new SortContext(player, lockHotbar);
        stacks = strategy().sort(stacks, ctx);

        for (int i = startSlot; i <= endSlot; i++) {
            int idx = i - startSlot;
            inv.setStack(i, idx < stacks.size() ? stacks.get(idx) : ItemStack.EMPTY);
        }
    }

    private static SortStrategy strategy() {
        return switch (Tidy.CONFIG.sortMode) {
            case "alpha" -> new AlphaSortStrategy();
            case "count" -> new CountSortStrategy();
            default      -> new CategorySortStrategy();
        };
    }

    static List<ItemStack> mergeStacks(List<ItemStack> input) {
        List<ItemStack> merged = new ArrayList<>();
        for (ItemStack stack : input) {
            if (stack.isEmpty()) continue;
            boolean consumed = false;
            for (ItemStack m : merged) {
                if (m.getItem() == stack.getItem() && m.getCount() < m.getMaxCount()) {
                    int transfer = Math.min(stack.getCount(), m.getMaxCount() - m.getCount());
                    m.increment(transfer);
                    stack.decrement(transfer);
                    if (stack.isEmpty()) { consumed = true; break; }
                }
            }
            if (!consumed) merged.add(stack.copy());
        }
        return merged;
    }
}
