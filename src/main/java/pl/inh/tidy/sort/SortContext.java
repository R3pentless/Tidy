package pl.inh.tidy.sort;

import net.minecraft.entity.player.PlayerEntity;

public record SortContext(PlayerEntity player, boolean lockHotbar) {}
