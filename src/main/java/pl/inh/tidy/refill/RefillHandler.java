package pl.inh.tidy.refill;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import pl.inh.tidy.Tidy;
import pl.inh.tidy.compat.TidyCompat;
import pl.inh.tidy.lock.SlotLockManager;

public final class RefillHandler {

    private static final int ARMOR_INVENTORY_OFFSET = 36;
    private static long lastElytraWarningAt;

    private RefillHandler() {}

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player != null) {
                ToolTracker.tick(client, client.player);
                tick(client, client.player);
            }
        });
    }

    public static void tick(MinecraftClient client, PlayerEntity player) {
        if (client.interactionManager == null) {
            return;
        }

        checkHeldItemLowDurability(client, player);
        checkChestEquipmentLowDurability(client, player);
    }

    static void onItemGone(MinecraftClient client, PlayerEntity player, int slot, ItemStack gone) {
        boolean isTool = gone.isDamageable();
        if (isTool && !Tidy.CONFIG.autoRefill) return;
        if (!isTool && !Tidy.CONFIG.refillBlocks) return;
        tryHotbarRefill(client, player, gone, slot);
    }

    static void onOffhandItemGone(MinecraftClient client, PlayerEntity player, ItemStack gone) {
        if (!Tidy.CONFIG.autoRefill || !Tidy.CONFIG.refillOffhandTotems || !isTotem(gone)) {
            return;
        }

        tryOffhandTotemRefill(client, player);
    }

    private static void checkHeldItemLowDurability(MinecraftClient client, PlayerEntity player) {
        if (!Tidy.CONFIG.autoRefill || !Tidy.CONFIG.lowDurabilityRefill) {
            return;
        }

        var inv = player.getInventory();
        int selected = TidyCompat.selectedSlot(inv);
        ItemStack current = inv.getStack(selected);
        if (!shouldPreemptivelyRefill(current)) {
            return;
        }

        int replacementSlot = findBestInventoryReplacement(player, current, -1);
        if (replacementSlot == -1) {
            return;
        }

        client.interactionManager.clickSlot(
                player.playerScreenHandler.syncId,
                replacementSlot,
                selected,
                SlotActionType.SWAP,
                player
        );
    }

    private static void checkChestEquipmentLowDurability(MinecraftClient client, PlayerEntity player) {
        if (!Tidy.CONFIG.autoRefill || !Tidy.CONFIG.lowDurabilityRefill) {
            return;
        }

        ItemStack chestStack = player.getEquippedStack(EquipmentSlot.CHEST);
        if (!shouldPreemptivelyRefill(chestStack)) {
            return;
        }

        int replacementSlot = findBestInventoryReplacement(player, chestStack, -1);
        if (replacementSlot == -1) {
            if (isElytra(chestStack) && Tidy.CONFIG.elytraBreakWarning) {
                warnElytraNoReplacement(player);
            }
            return;
        }

        int chestSlotId = findEquipmentSlotId(player, EquipmentSlot.CHEST);
        if (chestSlotId == -1) {
            return;
        }

        pickup(client, player, replacementSlot);
        pickup(client, player, chestSlotId);
        pickup(client, player, replacementSlot);
    }

    private static void tryHotbarRefill(MinecraftClient client, PlayerEntity player, ItemStack target, int hotbarSlot) {
        int replacementSlot = findBestInventoryReplacement(player, target, hotbarSlot);
        if (replacementSlot == -1) {
            return;
        }

        client.interactionManager.clickSlot(
                player.playerScreenHandler.syncId,
                replacementSlot,
                hotbarSlot,
                SlotActionType.SWAP,
                player
        );
    }

    private static void tryOffhandTotemRefill(MinecraftClient client, PlayerEntity player) {
        int replacementSlot = findBestTotemReplacement(player);
        if (replacementSlot == -1) {
            return;
        }

        int replacementSlotId = findInventorySlotId(player, replacementSlot);
        int offhandSlotId = findInventorySlotId(player, 40);
        if (replacementSlotId == -1 || offhandSlotId == -1) {
            return;
        }

        pickup(client, player, replacementSlotId);
        pickup(client, player, offhandSlotId);
        if (!player.playerScreenHandler.getCursorStack().isEmpty()) {
            pickup(client, player, replacementSlotId);
        }
    }

    private static void pickup(MinecraftClient client, PlayerEntity player, int slotId) {
        client.interactionManager.clickSlot(player.playerScreenHandler.syncId, slotId, 0, SlotActionType.PICKUP, player);
    }

    private static int findBestInventoryReplacement(PlayerEntity player, ItemStack target, int protectedHotbarSlot) {
        var inv = player.getInventory();
        int bestSlot = -1;
        int bestScore = Integer.MIN_VALUE;

        for (int invSlot = 9; invSlot <= 35; invSlot++) {
            if (invSlot == protectedHotbarSlot) {
                continue;
            }
            if (SlotLockManager.isLocked(invSlot)) {
                continue;
            }

            ItemStack candidate = inv.getStack(invSlot);
            if (!isReplacementCandidate(target, candidate)) {
                continue;
            }

            int score = scoreReplacement(target, candidate);
            if (score > bestScore) {
                bestScore = score;
                bestSlot = invSlot;
            }
        }

        return bestSlot;
    }

    private static int findBestTotemReplacement(PlayerEntity player) {
        var inv = player.getInventory();
        int selectedSlot = TidyCompat.selectedSlot(inv);
        int bestSlot = -1;
        int bestScore = Integer.MIN_VALUE;

        for (int invSlot = 9; invSlot <= 35; invSlot++) {
            int candidateScore = evaluateTotemCandidate(inv, invSlot, selectedSlot);
            if (candidateScore > bestScore) {
                bestScore = candidateScore;
                bestSlot = invSlot;
            }
        }

        for (int invSlot = 0; invSlot <= 8; invSlot++) {
            int candidateScore = evaluateTotemCandidate(inv, invSlot, selectedSlot);
            if (candidateScore > bestScore) {
                bestScore = candidateScore;
                bestSlot = invSlot;
            }
        }

        return bestSlot;
    }

    private static int evaluateTotemCandidate(net.minecraft.entity.player.PlayerInventory inv, int invSlot, int selectedSlot) {
        if (SlotLockManager.isLocked(invSlot)) {
            return Integer.MIN_VALUE;
        }

        ItemStack candidate = inv.getStack(invSlot);
        if (!candidate.isOf(Items.TOTEM_OF_UNDYING)) {
            return Integer.MIN_VALUE;
        }

        int score = invSlot >= 9 ? 100 : 10;
        if (invSlot == selectedSlot) {
            score -= 5;
        }

        return score;
    }

    private static int findEquipmentSlotId(PlayerEntity player, EquipmentSlot slot) {
        int inventoryIndex = slot.getOffsetEntitySlotId(ARMOR_INVENTORY_OFFSET);
        return findInventorySlotId(player, inventoryIndex);
    }

    private static int findInventorySlotId(PlayerEntity player, int inventoryIndex) {
        for (Slot handlerSlot : player.playerScreenHandler.slots) {
            if (handlerSlot.inventory == player.getInventory() && handlerSlot.getIndex() == inventoryIndex) {
                return handlerSlot.id;
            }
        }
        return -1;
    }

    private static boolean shouldPreemptivelyRefill(ItemStack stack) {
        return stack.isDamageable() && remainingDurability(stack) <= Tidy.CONFIG.lowDurabilityThreshold;
    }

    private static boolean isReplacementCandidate(ItemStack target, ItemStack candidate) {
        if (candidate.isEmpty() || candidate == target) {
            return false;
        }

        if (!target.isDamageable()) {
            return ItemStack.areItemsEqual(target, candidate);
        }

        if (!candidate.isDamageable()) {
            return false;
        }

        if (remainingDurability(candidate) <= remainingDurability(target)) {
            return false;
        }

        return replacementGroup(target).equals(replacementGroup(candidate));
    }

    private static int scoreReplacement(ItemStack target, ItemStack candidate) {
        int score = remainingDurability(candidate);
        if (ItemStack.areItemsEqual(target, candidate)) {
            score += 10_000;
        }
        return score;
    }

    private static int remainingDurability(ItemStack stack) {
        return stack.getMaxDamage() - stack.getDamage();
    }

    private static String replacementGroup(ItemStack stack) {
        String id = TidyCompat.itemId(stack);

        if (id.contains("pickaxe")) return "pickaxe";
        if (id.contains("shovel")) return "shovel";
        if (id.contains("axe")) return "axe";
        if (id.contains("hoe")) return "hoe";
        if (id.contains("sword")) return "sword";
        if (id.contains("elytra")) return "elytra";
        if (id.contains("bow")) return "bow";
        if (id.contains("crossbow")) return "crossbow";
        if (id.contains("trident")) return "trident";
        if (id.contains("shears")) return "shears";
        if (id.contains("fishing_rod")) return "fishing_rod";
        if (id.contains("flint_and_steel")) return "flint_and_steel";
        if (id.contains("shield")) return "shield";
        if (id.contains("helmet")) return "helmet";
        if (id.contains("chestplate")) return "chestplate";
        if (id.contains("leggings")) return "leggings";
        if (id.contains("boots")) return "boots";

        return id;
    }

    private static boolean isElytra(ItemStack stack) {
        return replacementGroup(stack).equals("elytra");
    }

    private static boolean isTotem(ItemStack stack) {
        return stack.isOf(Items.TOTEM_OF_UNDYING);
    }

    private static void warnElytraNoReplacement(PlayerEntity player) {
        long now = System.currentTimeMillis();
        if (now - lastElytraWarningAt < 5000L) {
            return;
        }

        lastElytraWarningAt = now;
        TidyCompat.playPling(player, 1.0f, 0.75f);
        player.sendMessage(TidyCompat.translatable("message.tidy.elytra_no_replacement"), true);
    }
}
