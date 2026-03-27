package pl.inh.tidy.keybind;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import pl.inh.tidy.sort.SortHandler;

public final class TidyKeybinds {

    private TidyKeybinds() {}

    private static KeyBinding sortKey;

    public static void register() {
        sortKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.tidy.sort",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_R,
                "category.tidy"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (sortKey.wasPressed()) {
                if (client.player != null) {
                    SortHandler.sortPlayerInventory(client.player);
                }
            }
        });
    }
}
