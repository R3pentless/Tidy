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

public class TidyConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String FILE_NAME = "tidy.json";

    // Sort settings
    public String sortMode = "category"; // category | alpha | count | custom
    public boolean lockHotbar = true;

    // Refill settings
    public boolean autoRefill = true;
    public boolean refillBlocks = true;

    // Custom sort rules
    public List<SortRule> customRules = new ArrayList<>();

    public static TidyConfig load() {
        Path path = FabricLoader.getInstance().getConfigDir().resolve(FILE_NAME);
        if (Files.exists(path)) {
            try (Reader reader = Files.newBufferedReader(path)) {
                TidyConfig cfg = GSON.fromJson(reader, TidyConfig.class);
                if (cfg != null) return cfg;
            } catch (IOException e) {
                Tidy.LOGGER.error("Failed to load config, using defaults", e);
            }
        }
        TidyConfig config = new TidyConfig();
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
}
