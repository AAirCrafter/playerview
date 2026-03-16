package air.invview.client;

import air.invview.client.gui.PlayerListScreen;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class Keybinds {
    private static KeyBinding openPlayerList;
    private static final KeyBinding.Category category = new KeyBinding.Category(Identifier.of("invview"));

    public static void register() {
        openPlayerList = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                        "key.invview.openui",
                        InputUtil.Type.KEYSYM,
                        GLFW.GLFW_KEY_G,
                        category
                )
        );

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client == null) return;
            while (openPlayerList.wasPressed()) {
                client.setScreen(new PlayerListScreen());
            }
        });
    }
}
