package pl.inh.tidy.sort;

import net.minecraft.item.BlockItem;
import net.minecraft.item.BowItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PotionItem;
import net.minecraft.item.TridentItem;
import pl.inh.tidy.compat.TidyCompat;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class CategorySortStrategy implements SortStrategy {

    @Override
    public List<ItemStack> sort(List<ItemStack> stacks, SortContext context) {
        return stacks.stream()
                .filter(s -> !s.isEmpty())
                .sorted(Comparator.comparingInt(this::category)
                        .thenComparing(TidyCompat::itemId))
                .collect(Collectors.toList());
    }

    int category(ItemStack stack) {
        Item item = stack.getItem();
        String itemId = TidyCompat.itemId(item);
        if (itemId.contains("sword") || itemId.contains("axe")) return 0;
        if (itemId.contains("pickaxe") || itemId.contains("shovel") || itemId.contains("hoe")) return 1;
        if (itemId.contains("helmet")
                || itemId.contains("chestplate")
                || itemId.contains("leggings")
                || itemId.contains("boots")) return 3;

        if (item instanceof BowItem || item instanceof CrossbowItem || item instanceof TridentItem)
            return 2;

        if (item instanceof BlockItem)
            return 4;

        if (isLikelyFood(itemId)) return 5;

        if (item instanceof PotionItem)
            return 6;

        return 7;
    }

    private boolean isLikelyFood(String itemId) {
        return itemId.contains("apple")
                || itemId.contains("bread")
                || itemId.contains("beef")
                || itemId.contains("pork")
                || itemId.contains("chicken")
                || itemId.contains("mutton")
                || itemId.contains("rabbit")
                || itemId.contains("cod")
                || itemId.contains("salmon")
                || itemId.contains("potato")
                || itemId.contains("carrot")
                || itemId.contains("berries")
                || itemId.contains("cookie")
                || itemId.contains("melon")
                || itemId.contains("stew")
                || itemId.contains("pie")
                || itemId.contains("bottle")
                || itemId.contains("golden_apple")
                || itemId.contains("rotten_flesh");
    }
}
