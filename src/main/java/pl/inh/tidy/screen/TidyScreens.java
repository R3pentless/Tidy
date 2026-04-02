package pl.inh.tidy.screen;

import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import pl.inh.tidy.Tidy;
import pl.inh.tidy.compat.TidyCompat;
import pl.inh.tidy.mixin.HandledScreenAccessor;
import pl.inh.tidy.sort.SortHandler;
import pl.inh.tidy.sort.SortLayout;

public final class TidyScreens {

    static final int BASE_BUTTON_SIZE = 20;
    private static final int BUTTON_SCREEN_PADDING = 4;
    private static final int BUTTON_ANCHOR_GAP = 4;
    static final Identifier SORT_DEFAULT_TEXTURE = TidyCompat.id(Tidy.MOD_ID, "button/sort_default");
    static final Identifier SORT_DEFAULT_HOVERED_TEXTURE = TidyCompat.id(Tidy.MOD_ID, "button/sort_default_hovered");
    static final Identifier SORT_COLUMNS_TEXTURE = TidyCompat.id(Tidy.MOD_ID, "button/sort_columns");
    static final Identifier SORT_COLUMNS_HOVERED_TEXTURE = TidyCompat.id(Tidy.MOD_ID, "button/sort_columns_hovered");
    static final Identifier SORT_ROWS_TEXTURE = TidyCompat.id(Tidy.MOD_ID, "button/sort_rows");
    static final Identifier SORT_ROWS_HOVERED_TEXTURE = TidyCompat.id(Tidy.MOD_ID, "button/sort_rows_hovered");
    static final Identifier CONFIG_TEXTURE = TidyCompat.id(Tidy.MOD_ID, "button/config");
    static final Identifier CONFIG_HOVERED_TEXTURE = TidyCompat.id(Tidy.MOD_ID, "button/config_hovered");

    enum ButtonKind {
        SORT_DEFAULT,
        SORT_COLUMNS,
        SORT_ROWS,
        CONFIG
    }

    public enum LayoutContext {
        PLAYER("button.tidy.layout_player", 176, 166),
        SINGLE_STORAGE("button.tidy.layout_single_storage", 176, 166),
        DOUBLE_STORAGE("button.tidy.layout_double_storage", 176, 222),
        UNSUPPORTED("", 0, 0);

        private final String labelKey;
        private final int backgroundWidth;
        private final int backgroundHeight;

        LayoutContext(String labelKey, int backgroundWidth, int backgroundHeight) {
            this.labelKey = labelKey;
            this.backgroundWidth = backgroundWidth;
            this.backgroundHeight = backgroundHeight;
        }

        public Text label() {
            return TidyCompat.translatable(this.labelKey);
        }

        public int backgroundWidth() {
            return this.backgroundWidth;
        }

        public int backgroundHeight() {
            return this.backgroundHeight;
        }

        public boolean supported() {
            return this != UNSUPPORTED;
        }

        public boolean usesPlayerSortButtons() {
            return this == PLAYER;
        }
    }

    private TidyScreens() {}

    public static void register() {
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (!(screen instanceof HandledScreen<?> hs)) return;
            if (!Tidy.CONFIG.showContainerButtons) return;
            LayoutContext layoutContext = detectLayoutContext(hs);
            if (!layoutContext.supported()) return;

            boolean containerScreen = layoutContext != LayoutContext.PLAYER;
            boolean showSortButtons = layoutContext.usesPlayerSortButtons() ? Tidy.CONFIG.showPlayerSortButton : Tidy.CONFIG.showStorageSortButton;
            HandledScreenAccessor acc = (HandledScreenAccessor) hs;
            if (showSortButtons) {
                ButtonPlacement defaultPlacement = resolveButtonPlacement(
                        ButtonKind.SORT_DEFAULT,
                        layoutContext,
                        scaledWidth,
                        scaledHeight,
                        acc.tidy$getX(),
                        acc.tidy$getY(),
                        acc.tidy$getBackgroundWidth()
                );
                ButtonWidget defaultSortButton = createIconButton(
                        defaultPlacement.x(),
                        defaultPlacement.y(),
                        defaultPlacement.size(),
                        SORT_DEFAULT_TEXTURE,
                        SORT_DEFAULT_HOVERED_TEXTURE,
                        "button.tidy.sort_default",
                        "button.tidy.sort_default.tooltip",
                        "",
                        btn -> {
                            if (client.player != null) {
                                if (containerScreen) {
                                    SortHandler.sortOpenContainer(client, client.player, SortLayout.DEFAULT);
                                } else {
                                    SortHandler.sortPlayerInventory(client, client.player, SortLayout.DEFAULT);
                                }
                            }
                        }
                );
                Screens.getButtons(screen).add(defaultSortButton);

                ButtonPlacement columnPlacement = resolveButtonPlacement(
                        ButtonKind.SORT_COLUMNS,
                        layoutContext,
                        scaledWidth,
                        scaledHeight,
                        acc.tidy$getX(),
                        acc.tidy$getY(),
                        acc.tidy$getBackgroundWidth()
                );
                ButtonWidget columnSortButton = createIconButton(
                        columnPlacement.x(),
                        columnPlacement.y(),
                        columnPlacement.size(),
                        SORT_COLUMNS_TEXTURE,
                        SORT_COLUMNS_HOVERED_TEXTURE,
                        "button.tidy.sort_columns",
                        "button.tidy.sort_columns.tooltip",
                        "",
                        btn -> {
                            if (client.player != null) {
                                if (containerScreen) {
                                    SortHandler.sortOpenContainer(client, client.player, SortLayout.COLUMNS);
                                } else {
                                    SortHandler.sortPlayerInventory(client, client.player, SortLayout.COLUMNS);
                                }
                            }
                        }
                );
                Screens.getButtons(screen).add(columnSortButton);

                ButtonPlacement rowPlacement = resolveButtonPlacement(
                        ButtonKind.SORT_ROWS,
                        layoutContext,
                        scaledWidth,
                        scaledHeight,
                        acc.tidy$getX(),
                        acc.tidy$getY(),
                        acc.tidy$getBackgroundWidth()
                );
                ButtonWidget rowSortButton = createIconButton(
                        rowPlacement.x(),
                        rowPlacement.y(),
                        rowPlacement.size(),
                        SORT_ROWS_TEXTURE,
                        SORT_ROWS_HOVERED_TEXTURE,
                        "button.tidy.sort_rows",
                        "button.tidy.sort_rows.tooltip",
                        "",
                        btn -> {
                            if (client.player != null) {
                                if (containerScreen) {
                                    SortHandler.sortOpenContainer(client, client.player, SortLayout.ROWS);
                                } else {
                                    SortHandler.sortPlayerInventory(client, client.player, SortLayout.ROWS);
                                }
                            }
                        }
                );
                Screens.getButtons(screen).add(rowSortButton);
            }

            if (Tidy.CONFIG.showConfigButton) {
                ButtonPlacement configPlacement = resolveButtonPlacement(
                        ButtonKind.CONFIG,
                        layoutContext,
                        scaledWidth,
                        scaledHeight,
                        acc.tidy$getX(),
                        acc.tidy$getY(),
                        acc.tidy$getBackgroundWidth()
                );
                ButtonWidget configButton = createIconButton(
                        configPlacement.x(),
                        configPlacement.y(),
                        configPlacement.size(),
                        CONFIG_TEXTURE,
                        CONFIG_HOVERED_TEXTURE,
                        "button.tidy.config",
                        "button.tidy.config.tooltip",
                        "",
                        btn -> client.setScreen(TidyConfigScreen.create(screen))
                );
                Screens.getButtons(screen).add(configButton);
            }
        });
    }

    private static ButtonWidget createIconButton(
            int x,
            int y,
            int size,
            Identifier texture,
            Identifier hoveredTexture,
            String labelKey,
            String tooltipKey,
            String fallbackLabel,
            ButtonWidget.PressAction onPress
    ) {
        return TidyCompat.createSpriteButton(
                x,
                y,
                size,
                texture,
                hoveredTexture,
                TidyCompat.translatable(labelKey),
                TidyCompat.translatable(tooltipKey),
                TidyCompat.literal(fallbackLabel),
                onPress
        );
    }

    public static LayoutContext detectLayoutContext(HandledScreen<?> screen) {
        if (screen.getClass() == InventoryScreen.class) {
            return LayoutContext.PLAYER;
        }
        if (screen.getScreenHandler() instanceof GenericContainerScreenHandler containerHandler) {
            int rows = containerHandler.getRows();
            if (rows == 3) {
                return LayoutContext.SINGLE_STORAGE;
            }
            if (rows == 6) {
                return LayoutContext.DOUBLE_STORAGE;
            }
        }
        return LayoutContext.UNSUPPORTED;
    }

    static ButtonPlacement resolveButtonPlacement(ButtonKind buttonKind, LayoutContext layoutContext, int screenWidth, int screenHeight, int anchorX, int anchorY, int backgroundWidth) {
        ButtonStackLayout base = resolveBaseLayout(screenWidth, screenHeight, anchorX, anchorY, backgroundWidth);
        Integer relativeX = buttonRelativeX(layoutContext, buttonKind);
        Integer relativeY = buttonRelativeY(layoutContext, buttonKind);
        int x = relativeX != null ? anchorX + relativeX : base.x() + buttonOffsetX(buttonKind);
        int y = relativeY != null
                ? anchorY + relativeY
                : base.y() + buttonBaseIndex(buttonKind) * (base.size() + base.gap()) + buttonOffsetY(buttonKind);
        x = clamp(x, BUTTON_SCREEN_PADDING, Math.max(BUTTON_SCREEN_PADDING, screenWidth - base.size() - BUTTON_SCREEN_PADDING));
        y = clamp(y, BUTTON_SCREEN_PADDING, Math.max(BUTTON_SCREEN_PADDING, screenHeight - base.size() - BUTTON_SCREEN_PADDING));
        return new ButtonPlacement(x, y, base.size());
    }

    private static ButtonStackLayout resolveBaseLayout(int screenWidth, int screenHeight, int anchorX, int anchorY, int backgroundWidth) {
        int size = buttonSize();
        int preferredX = Tidy.CONFIG.buttonsLeftSide
                ? anchorX - size - BUTTON_ANCHOR_GAP
                : anchorX + backgroundWidth + BUTTON_ANCHOR_GAP;
        int buttonX = clamp(preferredX, BUTTON_SCREEN_PADDING, Math.max(BUTTON_SCREEN_PADDING, screenWidth - size - BUTTON_SCREEN_PADDING));
        int buttonY = clamp(anchorY + BUTTON_SCREEN_PADDING, BUTTON_SCREEN_PADDING, Math.max(BUTTON_SCREEN_PADDING, screenHeight - size - BUTTON_SCREEN_PADDING));
        return new ButtonStackLayout(buttonX, buttonY, size, Math.max(0, Tidy.CONFIG.buttonSpacing));
    }

    static void updateDraggedButton(ButtonKind buttonKind, LayoutContext layoutContext, int screenWidth, int screenHeight, int anchorX, int anchorY, int backgroundWidth, int desiredX, int desiredY) {
        ButtonStackLayout base = resolveBaseLayout(screenWidth, screenHeight, anchorX, anchorY, backgroundWidth);
        int clampedX = clamp(desiredX, BUTTON_SCREEN_PADDING, Math.max(BUTTON_SCREEN_PADDING, screenWidth - base.size() - BUTTON_SCREEN_PADDING));
        int clampedY = clamp(desiredY, BUTTON_SCREEN_PADDING, Math.max(BUTTON_SCREEN_PADDING, screenHeight - base.size() - BUTTON_SCREEN_PADDING));
        setButtonRelativeX(layoutContext, buttonKind, clampedX - anchorX);
        setButtonRelativeY(layoutContext, buttonKind, clampedY - anchorY);
        Tidy.CONFIG.sanitize();
    }

    static void resetButtonLayout(LayoutContext layoutContext) {
        switch (layoutContext) {
            case PLAYER -> Tidy.CONFIG.playerButtonLayout.clear();
            case SINGLE_STORAGE -> Tidy.CONFIG.singleStorageButtonLayout.clear();
            case DOUBLE_STORAGE -> Tidy.CONFIG.doubleStorageButtonLayout.clear();
            case UNSUPPORTED -> {
            }
        }
        Tidy.CONFIG.sanitize();
    }

    static int buttonSize() {
        return clamp(Math.round(BASE_BUTTON_SIZE * (Tidy.CONFIG.buttonScalePercent / 100.0f)), 10, 32);
    }

    static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    static Text buttonLabel(ButtonKind buttonKind) {
        return switch (buttonKind) {
            case SORT_DEFAULT -> TidyCompat.translatable("button.tidy.sort_default");
            case SORT_COLUMNS -> TidyCompat.translatable("button.tidy.sort_columns");
            case SORT_ROWS -> TidyCompat.translatable("button.tidy.sort_rows");
            case CONFIG -> TidyCompat.translatable("button.tidy.config");
        };
    }

    static Identifier buttonTexture(ButtonKind buttonKind, boolean hovered) {
        return switch (buttonKind) {
            case SORT_DEFAULT -> hovered ? SORT_DEFAULT_HOVERED_TEXTURE : SORT_DEFAULT_TEXTURE;
            case SORT_COLUMNS -> hovered ? SORT_COLUMNS_HOVERED_TEXTURE : SORT_COLUMNS_TEXTURE;
            case SORT_ROWS -> hovered ? SORT_ROWS_HOVERED_TEXTURE : SORT_ROWS_TEXTURE;
            case CONFIG -> hovered ? CONFIG_HOVERED_TEXTURE : CONFIG_TEXTURE;
        };
    }

    private static int buttonBaseIndex(ButtonKind buttonKind) {
        return switch (buttonKind) {
            case SORT_DEFAULT -> 0;
            case SORT_COLUMNS -> 1;
            case SORT_ROWS -> 2;
            case CONFIG -> 3;
        };
    }

    private static int buttonOffsetX(ButtonKind buttonKind) {
        return switch (buttonKind) {
            case SORT_DEFAULT -> Tidy.CONFIG.sortDefaultOffsetX;
            case SORT_COLUMNS -> Tidy.CONFIG.sortColumnsOffsetX;
            case SORT_ROWS -> Tidy.CONFIG.sortRowsOffsetX;
            case CONFIG -> Tidy.CONFIG.configButtonOffsetX;
        };
    }

    private static int buttonOffsetY(ButtonKind buttonKind) {
        return switch (buttonKind) {
            case SORT_DEFAULT -> Tidy.CONFIG.sortDefaultOffsetY;
            case SORT_COLUMNS -> Tidy.CONFIG.sortColumnsOffsetY;
            case SORT_ROWS -> Tidy.CONFIG.sortRowsOffsetY;
            case CONFIG -> Tidy.CONFIG.configButtonOffsetY;
        };
    }

    private static Integer buttonRelativeX(LayoutContext layoutContext, ButtonKind buttonKind) {
        pl.inh.tidy.config.TidyConfig.ButtonLayoutProfile profile = layoutProfile(layoutContext);
        if (profile == null) {
            return null;
        }
        return switch (buttonKind) {
            case SORT_DEFAULT -> profile.sortDefaultX;
            case SORT_COLUMNS -> profile.sortColumnsX;
            case SORT_ROWS -> profile.sortRowsX;
            case CONFIG -> profile.configX;
        };
    }

    private static Integer buttonRelativeY(LayoutContext layoutContext, ButtonKind buttonKind) {
        pl.inh.tidy.config.TidyConfig.ButtonLayoutProfile profile = layoutProfile(layoutContext);
        if (profile == null) {
            return null;
        }
        return switch (buttonKind) {
            case SORT_DEFAULT -> profile.sortDefaultY;
            case SORT_COLUMNS -> profile.sortColumnsY;
            case SORT_ROWS -> profile.sortRowsY;
            case CONFIG -> profile.configY;
        };
    }

    private static void setButtonRelativeX(LayoutContext layoutContext, ButtonKind buttonKind, int value) {
        pl.inh.tidy.config.TidyConfig.ButtonLayoutProfile profile = layoutProfile(layoutContext);
        if (profile == null) {
            return;
        }
        switch (buttonKind) {
            case SORT_DEFAULT -> profile.sortDefaultX = value;
            case SORT_COLUMNS -> profile.sortColumnsX = value;
            case SORT_ROWS -> profile.sortRowsX = value;
            case CONFIG -> profile.configX = value;
        }
    }

    private static void setButtonRelativeY(LayoutContext layoutContext, ButtonKind buttonKind, int value) {
        pl.inh.tidy.config.TidyConfig.ButtonLayoutProfile profile = layoutProfile(layoutContext);
        if (profile == null) {
            return;
        }
        switch (buttonKind) {
            case SORT_DEFAULT -> profile.sortDefaultY = value;
            case SORT_COLUMNS -> profile.sortColumnsY = value;
            case SORT_ROWS -> profile.sortRowsY = value;
            case CONFIG -> profile.configY = value;
        }
    }

    private static pl.inh.tidy.config.TidyConfig.ButtonLayoutProfile layoutProfile(LayoutContext layoutContext) {
        return switch (layoutContext) {
            case PLAYER -> Tidy.CONFIG.playerButtonLayout;
            case SINGLE_STORAGE -> Tidy.CONFIG.singleStorageButtonLayout;
            case DOUBLE_STORAGE -> Tidy.CONFIG.doubleStorageButtonLayout;
            case UNSUPPORTED -> null;
        };
    }

    private static void setButtonOffsetX(ButtonKind buttonKind, int value) {
        switch (buttonKind) {
            case SORT_DEFAULT -> Tidy.CONFIG.sortDefaultOffsetX = value;
            case SORT_COLUMNS -> Tidy.CONFIG.sortColumnsOffsetX = value;
            case SORT_ROWS -> Tidy.CONFIG.sortRowsOffsetX = value;
            case CONFIG -> Tidy.CONFIG.configButtonOffsetX = value;
        }
    }

    private static void setButtonOffsetY(ButtonKind buttonKind, int value) {
        switch (buttonKind) {
            case SORT_DEFAULT -> Tidy.CONFIG.sortDefaultOffsetY = value;
            case SORT_COLUMNS -> Tidy.CONFIG.sortColumnsOffsetY = value;
            case SORT_ROWS -> Tidy.CONFIG.sortRowsOffsetY = value;
            case CONFIG -> Tidy.CONFIG.configButtonOffsetY = value;
        }
    }

    record ButtonStackLayout(int x, int y, int size, int gap) {}
    record ButtonPlacement(int x, int y, int size) {}
}
