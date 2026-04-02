package pl.inh.tidy.sort;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import pl.inh.tidy.Tidy;
import pl.inh.tidy.compat.TidyCompat;
import pl.inh.tidy.lock.SlotLockManager;

import java.util.ArrayList;
import java.util.List;

public final class SortHandler {

    private SortHandler() {}

    public static void sortPlayerInventory(MinecraftClient client, PlayerEntity player) {
        sortPlayerInventory(client, player, SortLayout.DEFAULT);
    }

    public static void sortPlayerInventory(MinecraftClient client, PlayerEntity player, SortLayout layout) {
        ScreenHandler handler = resolvePlayerSortHandler(player);
        List<Integer> slotIds = arrangeSlots(collectPlayerSlotIds(handler, Tidy.CONFIG.lockHotbar), layout, 9);
        sortSlots(client, player, handler, slotIds, Tidy.CONFIG.lockHotbar);
    }

    public static void sortOpenContainer(MinecraftClient client, PlayerEntity player) {
        sortOpenContainer(client, player, SortLayout.DEFAULT);
    }

    public static void sortOpenContainer(MinecraftClient client, PlayerEntity player, SortLayout layout) {
        ScreenHandler handler = player.currentScreenHandler;
        if (!(handler instanceof GenericContainerScreenHandler containerHandler)) {
            return;
        }

        List<Integer> slotIds = new ArrayList<>();
        int containerSlots = containerHandler.getRows() * 9;
        for (int i = 0; i < containerSlots; i++) {
            slotIds.add(i);
        }

        slotIds = arrangeSlots(slotIds, layout, 9);
        sortSlots(client, player, handler, slotIds, false);
    }

    private static void sortSlots(MinecraftClient client, PlayerEntity player, ScreenHandler handler, List<Integer> slotIds, boolean lockHotbar) {
        if (client.interactionManager == null || slotIds.isEmpty()) {
            return;
        }

        if (!handler.getCursorStack().isEmpty()) {
            Tidy.LOGGER.debug("Skipping sort because the cursor stack is not empty");
            return;
        }

        List<ItemStack> state = snapshot(handler, slotIds);
        if (state.stream().filter(stack -> !stack.isEmpty()).count() < 2) {
            return;
        }

        mergeIdenticalStacks(client, player, handler, slotIds, state);

        List<ItemStack> merged = compact(state);
        List<ItemStack> target = strategy().sort(merged, new SortContext(player, lockHotbar));
        while (target.size() < slotIds.size()) {
            target.add(ItemStack.EMPTY);
        }

        reorderStacks(client, player, handler, slotIds, state, target);
    }

    private static ScreenHandler resolvePlayerSortHandler(PlayerEntity player) {
        if (player.currentScreenHandler instanceof GenericContainerScreenHandler) {
            return player.currentScreenHandler;
        }
        return player.playerScreenHandler;
    }

    private static List<Integer> collectPlayerSlotIds(ScreenHandler handler, boolean lockHotbar) {
        List<Integer> main = new ArrayList<>();
        List<Integer> hotbar = new ArrayList<>();

        if (handler instanceof GenericContainerScreenHandler containerHandler) {
            int playerSlotsStart = containerHandler.getRows() * 9;
            addUnlockedSlots(handler, main, playerSlotsStart, playerSlotsStart + 27);
            if (!lockHotbar) {
                addUnlockedSlots(handler, hotbar, playerSlotsStart + 27, playerSlotsStart + 36);
            }
        } else {
            addUnlockedSlots(handler, main, 9, 36);
            if (!lockHotbar) {
                addUnlockedSlots(handler, hotbar, 36, 45);
            }
        }

        List<Integer> slotIds = new ArrayList<>(main.size() + hotbar.size());
        slotIds.addAll(main);
        slotIds.addAll(hotbar);
        return slotIds;
    }

    private static void addUnlockedSlots(ScreenHandler handler, List<Integer> target, int startInclusive, int endExclusive) {
        for (int slotId = startInclusive; slotId < endExclusive; slotId++) {
            Slot slot = handler.getSlot(slotId);
            if (SlotLockManager.isLocked(slot.getIndex())) {
                continue;
            }
            target.add(slotId);
        }
    }

    private static List<ItemStack> snapshot(ScreenHandler handler, List<Integer> slotIds) {
        List<ItemStack> state = new ArrayList<>(slotIds.size());
        for (int slotId : slotIds) {
            state.add(handler.getSlot(slotId).getStack().copy());
        }
        return state;
    }

    private static List<ItemStack> compact(List<ItemStack> stacks) {
        List<ItemStack> compact = new ArrayList<>();
        for (ItemStack stack : stacks) {
            if (!stack.isEmpty()) {
                compact.add(stack.copy());
            }
        }
        return compact;
    }

    private static void mergeIdenticalStacks(MinecraftClient client, PlayerEntity player, ScreenHandler handler, List<Integer> slotIds, List<ItemStack> state) {
        for (int targetIndex = 0; targetIndex < state.size(); targetIndex++) {
            ItemStack target = state.get(targetIndex);
            if (target.isEmpty() || target.getCount() >= target.getMaxCount()) {
                continue;
            }

            for (int sourceIndex = targetIndex + 1; sourceIndex < state.size(); sourceIndex++) {
                ItemStack source = state.get(sourceIndex);
                if (!canMerge(target, source)) {
                    continue;
                }

                pickup(client, player, handler, slotIds.get(sourceIndex));
                pickup(client, player, handler, slotIds.get(targetIndex));

                int room = target.getMaxCount() - target.getCount();
                int moved = Math.min(room, source.getCount());
                target.increment(moved);
                source.decrement(moved);
                if (source.isEmpty()) {
                    state.set(sourceIndex, ItemStack.EMPTY);
                }

                if (!handler.getCursorStack().isEmpty()) {
                    pickup(client, player, handler, slotIds.get(sourceIndex));
                }

                if (target.getCount() >= target.getMaxCount()) {
                    break;
                }
            }
        }
    }

    private static void reorderStacks(MinecraftClient client, PlayerEntity player, ScreenHandler handler, List<Integer> slotIds, List<ItemStack> state, List<ItemStack> target) {
        for (int index = 0; index < slotIds.size(); index++) {
            ItemStack want = target.get(index);
            ItemStack have = state.get(index);

            if (ItemStack.areEqual(have, want)) {
                continue;
            }

            int sourceIndex = -1;
            for (int candidate = index + 1; candidate < slotIds.size(); candidate++) {
                if (ItemStack.areEqual(state.get(candidate), want)) {
                    sourceIndex = candidate;
                    break;
                }
            }

            if (sourceIndex == -1) {
                continue;
            }

            int targetSlotId = slotIds.get(index);
            int sourceSlotId = slotIds.get(sourceIndex);

            if (have.isEmpty()) {
                pickup(client, player, handler, sourceSlotId);
                pickup(client, player, handler, targetSlotId);
            } else if (want.isEmpty()) {
                pickup(client, player, handler, targetSlotId);
                pickup(client, player, handler, sourceSlotId);
            } else {
                pickup(client, player, handler, targetSlotId);
                pickup(client, player, handler, sourceSlotId);
                pickup(client, player, handler, targetSlotId);
            }

            state.set(index, want.copy());
            state.set(sourceIndex, have.copy());
        }
    }

    private static void pickup(MinecraftClient client, PlayerEntity player, ScreenHandler handler, int slotId) {
        client.interactionManager.clickSlot(handler.syncId, slotId, 0, SlotActionType.PICKUP, player);
    }

    private static boolean canMerge(ItemStack target, ItemStack source) {
        return !target.isEmpty()
                && !source.isEmpty()
                && target.getCount() < target.getMaxCount()
                && TidyCompat.sameItemAndData(target, source);
    }

    private static SortStrategy strategy() {
        return switch (Tidy.CONFIG.sortMode) {
            case "alpha" -> new AlphaSortStrategy();
            case "count" -> new CountSortStrategy();
            default -> new CategorySortStrategy();
        };
    }

    private static List<Integer> arrangeSlots(List<Integer> slotIds, SortLayout layout, int columns) {
        if (layout != SortLayout.COLUMNS || slotIds.isEmpty() || columns <= 0 || slotIds.size() % columns != 0) {
            return slotIds;
        }

        int rows = slotIds.size() / columns;
        List<Integer> arranged = new ArrayList<>(slotIds.size());
        for (int column = 0; column < columns; column++) {
            for (int row = 0; row < rows; row++) {
                arranged.add(slotIds.get(row * columns + column));
            }
        }
        return arranged;
    }
}
