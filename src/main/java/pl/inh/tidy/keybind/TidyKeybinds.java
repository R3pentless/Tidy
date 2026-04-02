package pl.inh.tidy.keybind;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import org.lwjgl.glfw.GLFW;
import pl.inh.tidy.compat.TidyCompat;
import pl.inh.tidy.screen.TidyConfigScreen;
import pl.inh.tidy.sort.SortHandler;

public final class TidyKeybinds {

    private TidyKeybinds() {}

    private static KeyBinding sortKey;
    private static KeyBinding openConfigKey;
    private static KeyBinding toggleSlotLockKey;
    private static final String TIDY_CATEGORY_KEY = "category.tidy.main";


    public static void register() {
        sortKey = KeyBindingHelper.registerKeyBinding(createKeyBinding("key.tidy.sort", GLFW.GLFW_KEY_R));
        openConfigKey = KeyBindingHelper.registerKeyBinding(createKeyBinding("key.tidy.open_config", GLFW.GLFW_KEY_O));
        toggleSlotLockKey = KeyBindingHelper.registerKeyBinding(createKeyBinding("key.tidy.toggle_slot_lock", GLFW.GLFW_KEY_V));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (sortKey.wasPressed()) {
                if (client.player != null) {
                    SortHandler.sortPlayerInventory(client, client.player);
                }
            }

            while (openConfigKey.wasPressed()) {
                client.setScreen(TidyConfigScreen.create(client.currentScreen));
            }
        });
    }

    private static KeyBinding createKeyBinding(String translationKey, int defaultKey) {
        return TidyCompat.createKeyBinding(translationKey, defaultKey, TIDY_CATEGORY_KEY, TidyCompat.id("tidy", "main_category"));
    }

    public static boolean matchesSortKey(Object input) {
        return sortKey != null && TidyCompat.matchesKey(sortKey, input);
    }

    public static boolean matchesOpenConfigKey(Object input) {
        return openConfigKey != null && TidyCompat.matchesKey(openConfigKey, input);
    }

    public static boolean matchesSortKey(int keyCode, int scanCode) {
        return sortKey != null && TidyCompat.matchesKey(sortKey, keyCode, scanCode);
    }

    public static boolean matchesOpenConfigKey(int keyCode, int scanCode) {
        return openConfigKey != null && TidyCompat.matchesKey(openConfigKey, keyCode, scanCode);
    }

    public static boolean matchesToggleSlotLockKey(int keyCode, int scanCode) {
        return toggleSlotLockKey != null && TidyCompat.matchesKey(toggleSlotLockKey, keyCode, scanCode);
    }

    public static boolean matchesToggleSlotLockKey(Object input) {
        return toggleSlotLockKey != null && TidyCompat.matchesKey(toggleSlotLockKey, input);
    }

    public static net.minecraft.text.Text toggleSlotLockKeyText() {
        return toggleSlotLockKey != null ? toggleSlotLockKey.getBoundKeyLocalizedText() : TidyCompat.literal("Unbound");
    }
}
