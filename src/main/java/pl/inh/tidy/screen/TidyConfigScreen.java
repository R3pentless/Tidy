package pl.inh.tidy.screen;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import pl.inh.tidy.Tidy;
import pl.inh.tidy.compat.TidyCompat;
import pl.inh.tidy.config.SortMode;

public final class TidyConfigScreen {

    private TidyConfigScreen() {}

    public static Screen create(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(TidyCompat.translatable("screen.tidy.config"))
                .setSavingRunnable(Tidy.CONFIG::save);

        builder.setAfterInitConsumer(screen -> {
            MinecraftClient client = MinecraftClient.getInstance();
            int screenWidth = client.getWindow().getScaledWidth();
            Screens.getButtons(screen).add(TidyCompat.createButton(
                    Math.max(8, screenWidth - 148),
                    8,
                    140,
                    20,
                    TidyCompat.translatable("button.tidy.move_buttons"),
                    button -> client.setScreen(new TidyButtonLayoutScreen(screen))
            ));
        });

        ConfigEntryBuilder entries = builder.entryBuilder();
        ConfigCategory general = builder.getOrCreateCategory(TidyCompat.translatable("category.tidy.general"));
        ConfigCategory refill = builder.getOrCreateCategory(TidyCompat.translatable("category.tidy.refill"));
        ConfigCategory buttons = builder.getOrCreateCategory(TidyCompat.translatable("category.tidy.buttons"));

        general.addEntry(entries.<SortMode>startEnumSelector(
                        TidyCompat.translatable("option.tidy.sort_mode"),
                        SortMode.class,
                        SortMode.fromId(Tidy.CONFIG.sortMode))
                .setEnumNameProvider(mode -> TidyCompat.translatable("sort_mode.tidy." + ((SortMode) mode).id()))
                .setDefaultValue(SortMode.CATEGORY)
                .setTooltip(TidyCompat.translatable("tooltip.tidy.sort_mode"))
                .setSaveConsumer(mode -> Tidy.CONFIG.sortMode = mode.id())
                .build());

        general.addEntry(entries.startBooleanToggle(TidyCompat.translatable("option.tidy.lock_hotbar"), Tidy.CONFIG.lockHotbar)
                .setDefaultValue(true)
                .setTooltip(TidyCompat.translatable("tooltip.tidy.lock_hotbar"))
                .setSaveConsumer(value -> Tidy.CONFIG.lockHotbar = value)
                .build());

        general.addEntry(entries.startBooleanToggle(TidyCompat.translatable("option.tidy.middle_click_sort"), Tidy.CONFIG.middleClickSort)
                .setDefaultValue(true)
                .setTooltip(TidyCompat.translatable("tooltip.tidy.middle_click_sort"))
                .setSaveConsumer(value -> Tidy.CONFIG.middleClickSort = value)
                .build());

        general.addEntry(entries.startBooleanToggle(TidyCompat.translatable("option.tidy.container_buttons"), Tidy.CONFIG.showContainerButtons)
                .setDefaultValue(true)
                .setTooltip(TidyCompat.translatable("tooltip.tidy.container_buttons"))
                .setSaveConsumer(value -> Tidy.CONFIG.showContainerButtons = value)
                .build());

        buttons.addEntry(entries.startBooleanToggle(TidyCompat.translatable("option.tidy.player_sort_button"), Tidy.CONFIG.showPlayerSortButton)
                .setDefaultValue(true)
                .setTooltip(TidyCompat.translatable("tooltip.tidy.player_sort_button"))
                .setSaveConsumer(value -> Tidy.CONFIG.showPlayerSortButton = value)
                .build());

        buttons.addEntry(entries.startBooleanToggle(TidyCompat.translatable("option.tidy.storage_sort_button"), Tidy.CONFIG.showStorageSortButton)
                .setDefaultValue(true)
                .setTooltip(TidyCompat.translatable("tooltip.tidy.storage_sort_button"))
                .setSaveConsumer(value -> Tidy.CONFIG.showStorageSortButton = value)
                .build());

        buttons.addEntry(entries.startBooleanToggle(TidyCompat.translatable("option.tidy.config_button"), Tidy.CONFIG.showConfigButton)
                .setDefaultValue(true)
                .setTooltip(TidyCompat.translatable("tooltip.tidy.config_button"))
                .setSaveConsumer(value -> Tidy.CONFIG.showConfigButton = value)
                .build());

        buttons.addEntry(entries.startIntSlider(TidyCompat.translatable("option.tidy.button_scale"), Tidy.CONFIG.buttonScalePercent, 50, 150)
                .setDefaultValue(75)
                .setTooltip(TidyCompat.translatable("tooltip.tidy.button_scale"))
                .setSaveConsumer(value -> Tidy.CONFIG.buttonScalePercent = value)
                .build());

        buttons.addEntry(entries.startIntSlider(TidyCompat.translatable("option.tidy.button_spacing"), Tidy.CONFIG.buttonSpacing, 0, 20)
                .setDefaultValue(2)
                .setTooltip(TidyCompat.translatable("tooltip.tidy.button_spacing"))
                .setSaveConsumer(value -> Tidy.CONFIG.buttonSpacing = value)
                .build());

        refill.addEntry(entries.startBooleanToggle(TidyCompat.translatable("option.tidy.auto_refill"), Tidy.CONFIG.autoRefill)
                .setDefaultValue(true)
                .setTooltip(TidyCompat.translatable("tooltip.tidy.auto_refill"))
                .setSaveConsumer(value -> Tidy.CONFIG.autoRefill = value)
                .build());

        refill.addEntry(entries.startBooleanToggle(TidyCompat.translatable("option.tidy.refill_blocks"), Tidy.CONFIG.refillBlocks)
                .setDefaultValue(true)
                .setTooltip(TidyCompat.translatable("tooltip.tidy.refill_blocks"))
                .setSaveConsumer(value -> Tidy.CONFIG.refillBlocks = value)
                .build());

        refill.addEntry(entries.startBooleanToggle(TidyCompat.translatable("option.tidy.refill_offhand_totems"), Tidy.CONFIG.refillOffhandTotems)
                .setDefaultValue(true)
                .setTooltip(TidyCompat.translatable("tooltip.tidy.refill_offhand_totems"))
                .setSaveConsumer(value -> Tidy.CONFIG.refillOffhandTotems = value)
                .build());

        refill.addEntry(entries.startBooleanToggle(TidyCompat.translatable("option.tidy.low_durability_refill"), Tidy.CONFIG.lowDurabilityRefill)
                .setDefaultValue(true)
                .setTooltip(TidyCompat.translatable("tooltip.tidy.low_durability_refill"))
                .setSaveConsumer(value -> Tidy.CONFIG.lowDurabilityRefill = value)
                .build());

        refill.addEntry(entries.startIntField(TidyCompat.translatable("option.tidy.low_durability_threshold"), Tidy.CONFIG.lowDurabilityThreshold)
                .setDefaultValue(10)
                .setMin(1)
                .setMax(64)
                .setTooltip(TidyCompat.translatable("tooltip.tidy.low_durability_threshold"))
                .setSaveConsumer(value -> Tidy.CONFIG.lowDurabilityThreshold = value)
                .build());

        refill.addEntry(entries.startBooleanToggle(TidyCompat.translatable("option.tidy.elytra_warning"), Tidy.CONFIG.elytraBreakWarning)
                .setDefaultValue(true)
                .setTooltip(TidyCompat.translatable("tooltip.tidy.elytra_warning"))
                .setSaveConsumer(value -> Tidy.CONFIG.elytraBreakWarning = value)
                .build());

        return builder.build();
    }
}
