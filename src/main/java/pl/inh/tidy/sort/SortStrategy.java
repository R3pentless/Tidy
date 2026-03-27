package pl.inh.tidy.sort;

import net.minecraft.item.ItemStack;

import java.util.List;

public interface SortStrategy {
    List<ItemStack> sort(List<ItemStack> stacks, SortContext context);
}
