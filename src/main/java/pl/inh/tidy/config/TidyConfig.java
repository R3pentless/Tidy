package pl.inh.tidy.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import pl.inh.tidy.Tidy;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

public class TidyConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String FILE_NAME = "tidy.json";

    public String sortMode = "category";
    public boolean lockHotbar = true;
    public boolean middleClickSort = true;
    public boolean showContainerButtons = true;
    public boolean showPlayerSortButton = true;
    public boolean showStorageSortButton = true;
    public boolean showConfigButton = true;
    public boolean buttonsLeftSide = false;
    public int buttonScalePercent = 75;
    public int sortDefaultOffsetX = 0;
    public int sortDefaultOffsetY = 0;
    public int sortColumnsOffsetX = 0;
    public int sortColumnsOffsetY = 0;
    public int sortRowsOffsetX = 0;
    public int sortRowsOffsetY = 0;
    public int configButtonOffsetX = 0;
    public int configButtonOffsetY = 0;
    public int buttonOffsetX = 0;
    public int buttonOffsetY = 0;
    public int buttonSpacing = 2;
    public ButtonLayoutProfile playerButtonLayout = new ButtonLayoutProfile();
    public ButtonLayoutProfile singleStorageButtonLayout = new ButtonLayoutProfile();
    public ButtonLayoutProfile doubleStorageButtonLayout = new ButtonLayoutProfile();

    public boolean autoRefill = true;
    public boolean refillBlocks = true;
    public boolean refillOffhandTotems = true;
    public boolean lowDurabilityRefill = true;
    public int lowDurabilityThreshold = 10;
    public boolean elytraBreakWarning = true;

    public List<Integer> lockedSlots = new ArrayList<>();

    public static TidyConfig load() {
        Path path = FabricLoader.getInstance().getConfigDir().resolve(FILE_NAME);
        if (Files.exists(path)) {
            try (Reader reader = Files.newBufferedReader(path)) {
                TidyConfig cfg = GSON.fromJson(reader, TidyConfig.class);
                if (cfg != null) {
                    cfg.sanitize();
                    return cfg;
                }
            } catch (IOException e) {
                Tidy.LOGGER.error("Failed to load config, using defaults", e);
            }
        }
        TidyConfig config = new TidyConfig();
        config.sanitize();
        config.save();
        return config;
    }

    public void save() {
        Path path = FabricLoader.getInstance().getConfigDir().resolve(FILE_NAME);
        try (Writer writer = Files.newBufferedWriter(path)) {
            GSON.toJson(this, writer);
        } catch (IOException e) {
            Tidy.LOGGER.error("Failed to save config", e);
        }
    }

    public void sanitize() {
        sortMode = SortMode.fromId(sortMode).id();
        lowDurabilityThreshold = Math.max(1, Math.min(64, lowDurabilityThreshold));
        buttonScalePercent = Math.max(50, Math.min(150, buttonScalePercent));
        sortDefaultOffsetX = clampButtonOffsetX(sortDefaultOffsetX);
        sortDefaultOffsetY = clampButtonOffsetY(sortDefaultOffsetY);
        sortColumnsOffsetX = clampButtonOffsetX(sortColumnsOffsetX);
        sortColumnsOffsetY = clampButtonOffsetY(sortColumnsOffsetY);
        sortRowsOffsetX = clampButtonOffsetX(sortRowsOffsetX);
        sortRowsOffsetY = clampButtonOffsetY(sortRowsOffsetY);
        configButtonOffsetX = clampButtonOffsetX(configButtonOffsetX);
        configButtonOffsetY = clampButtonOffsetY(configButtonOffsetY);
        buttonOffsetX = clampButtonOffsetX(buttonOffsetX);
        buttonOffsetY = clampButtonOffsetY(buttonOffsetY);
        buttonSpacing = Math.max(0, Math.min(20, buttonSpacing));
        if (playerButtonLayout == null) {
            playerButtonLayout = new ButtonLayoutProfile();
        }
        if (singleStorageButtonLayout == null) {
            singleStorageButtonLayout = new ButtonLayoutProfile();
        }
        if (doubleStorageButtonLayout == null) {
            doubleStorageButtonLayout = new ButtonLayoutProfile();
        }
        playerButtonLayout.sanitize();
        singleStorageButtonLayout.sanitize();
        doubleStorageButtonLayout.sanitize();

        if (sortDefaultOffsetX == 0 && sortDefaultOffsetY == 0
                && sortColumnsOffsetX == 0 && sortColumnsOffsetY == 0
                && sortRowsOffsetX == 0 && sortRowsOffsetY == 0
                && configButtonOffsetX == 0 && configButtonOffsetY == 0
                && (buttonOffsetX != 0 || buttonOffsetY != 0)) {
            sortDefaultOffsetX = buttonOffsetX;
            sortDefaultOffsetY = buttonOffsetY;
            sortColumnsOffsetX = buttonOffsetX;
            sortColumnsOffsetY = buttonOffsetY;
            sortRowsOffsetX = buttonOffsetX;
            sortRowsOffsetY = buttonOffsetY;
            configButtonOffsetX = buttonOffsetX;
            configButtonOffsetY = buttonOffsetY;
        }

        if (lockedSlots == null) {
            lockedSlots = new ArrayList<>();
        }

        TreeSet<Integer> sanitizedLockedSlots = new TreeSet<>();
        for (Integer slot : lockedSlots) {
            if (slot == null || slot < 0 || slot > 40) {
                continue;
            }
            sanitizedLockedSlots.add(slot);
        }
        lockedSlots = new ArrayList<>(sanitizedLockedSlots);
    }

    private static int clampButtonOffsetX(int value) {
        return Math.max(-600, Math.min(600, value));
    }

    private static int clampButtonOffsetY(int value) {
        return Math.max(-400, Math.min(400, value));
    }

    public static final class ButtonLayoutProfile {
        public Integer sortDefaultX;
        public Integer sortDefaultY;
        public Integer sortColumnsX;
        public Integer sortColumnsY;
        public Integer sortRowsX;
        public Integer sortRowsY;
        public Integer configX;
        public Integer configY;

        void sanitize() {
            sortDefaultX = sanitizeNullableX(sortDefaultX);
            sortDefaultY = sanitizeNullableY(sortDefaultY);
            sortColumnsX = sanitizeNullableX(sortColumnsX);
            sortColumnsY = sanitizeNullableY(sortColumnsY);
            sortRowsX = sanitizeNullableX(sortRowsX);
            sortRowsY = sanitizeNullableY(sortRowsY);
            configX = sanitizeNullableX(configX);
            configY = sanitizeNullableY(configY);
        }

        public void clear() {
            sortDefaultX = null;
            sortDefaultY = null;
            sortColumnsX = null;
            sortColumnsY = null;
            sortRowsX = null;
            sortRowsY = null;
            configX = null;
            configY = null;
        }

        private static Integer sanitizeNullableX(Integer value) {
            return value == null ? null : clampButtonOffsetX(value);
        }

        private static Integer sanitizeNullableY(Integer value) {
            return value == null ? null : clampButtonOffsetY(value);
        }
    }
}
