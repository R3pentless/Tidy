package pl.inh.tidy.sort;

import net.minecraft.item.ItemStack;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class CountSortStrategy implements SortStrategy {

    @Override
    public List<ItemStack> sort(List<ItemStack> stacks, SortContext context) {
        return stacks.stream()
                .filter(s -> !s.isEmpty())
                .sorted(Comparator.comparingInt(ItemStack::getCount).reversed()
                        .thenComparing(s -> s.getItem().toString()))
                .collect(Collectors.toList());
    }
}
