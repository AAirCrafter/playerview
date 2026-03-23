package air.playerview.client;

import air.playerview.client.listener.ChatListener;
import air.playerview.client.listener.EnderChestListener;
import net.fabricmc.api.ClientModInitializer;

public class PlayerViewClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        Keybinds.register();
        ChatListener.register();
        ViewInvCommand.register();
        EnderChestListener.register();
    }
}
