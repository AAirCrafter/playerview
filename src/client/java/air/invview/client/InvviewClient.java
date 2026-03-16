package air.invview.client;

import air.invview.client.listener.ChatListener;
import air.invview.client.listener.EnderChestListener;
import net.fabricmc.api.ClientModInitializer;

public class InvviewClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        Keybinds.register();
        ChatListener.register();
        ViewInvCommand.register();
        EnderChestListener.register();
    }
}
