package pl.inh.tidy;

import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.inh.tidy.config.TidyConfig;
import pl.inh.tidy.keybind.TidyKeybinds;
import pl.inh.tidy.refill.RefillHandler;
import pl.inh.tidy.screen.TidyScreens;

public class Tidy implements ClientModInitializer {

    public static final String MOD_ID = "tidy";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static TidyConfig CONFIG;

    @Override
    public void onInitializeClient() {
        CONFIG = TidyConfig.load();
        TidyKeybinds.register();
        RefillHandler.register();
        TidyScreens.register();
        LOGGER.info("Tidy initialized");
    }
}
