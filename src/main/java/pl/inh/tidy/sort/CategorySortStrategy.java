package pl.inh.tidy.sort;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BowItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.HoeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PickaxeItem;
import net.minecraft.item.PotionItem;
import net.minecraft.item.ShovelItem;
import net.minecraft.item.SwordItem;
import net.minecraft.item.TridentItem;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class CategorySortStrategy implements SortStrategy {

    @Override
    public List<ItemStack> sort(List<ItemStack> stacks, SortContext context) {
        return stacks.stream()
                .filter(s -> !s.isEmpty())
                .sorted(Comparator.comparingInt(this::category)
                        .thenComparing(s -> s.getItem().toString()))
                .collect(Collectors.toList());
    }

    private int category(ItemStack stack) {
        Item item = stack.getItem();

        if (item instanceof SwordItem || item instanceof AxeItem)
            return 0; // weapons / combat axes

        if (item instanceof PickaxeItem || item instanceof ShovelItem || item instanceof HoeItem)
            return 1; // mining tools

        if (item instanceof BowItem || item instanceof CrossbowItem || item instanceof TridentItem)
            return 2; // ranged weapons

        if (item instanceof ArmorItem)
            return 3;

        if (item instanceof BlockItem)
            return 4;

        //? 1.18.2 .. 1.20.4
        /*if (item.isFood()) return 5;*/
        //? 1.21 ..
        if (stack.contains(DataComponentTypes.FOOD)) return 5;

        if (item instanceof PotionItem)
            return 6;

        return 7;
    }
}
