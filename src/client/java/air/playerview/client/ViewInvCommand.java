package air.playerview.client;

import air.playerview.client.listener.ChatListener;
import air.playerview.client.listener.EnderChestListener;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class ViewInvCommand {
    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(
                    literal("viewinv")
                            .then(argument("player", StringArgumentType.word())
                                    .executes(ctx -> {
                                        String playerName = StringArgumentType.getString(ctx, "player");
                                        openFor(playerName);
                                        return 1;
                                    })
                            )
            );
        });
    }

    public static void openFor(String playerName) {
        MinecraftClient client = MinecraftClient.getInstance();

        if (client == null || client.player == null || client.getNetworkHandler() == null) return;
        //client.player.sendMessage(Text.literal("[INVVIEW] You don't have permissions to do that!").formatted(Formatting.RED),false);

        try {
            ChatListener.setPendingPlayer(playerName);
            //ChatListener.send("data get entity " + playerName + " Inventory");
            ChatListener.send("data get entity " + playerName + " Inventory");
        } catch (Exception e) {
            client.player.sendMessage(Text.literal("[INVVIEW] Failed to execute command").formatted(Formatting.RED),false);
        }
    }

    public static void openEnderChest(String playerName) {
        MinecraftClient client = MinecraftClient.getInstance();

        if (client == null || client.player == null || client.getNetworkHandler() == null) return;
        //client.player.sendMessage(Text.literal("[INVVIEW] You don't have permissions to do that!").formatted(Formatting.RED),false);

        try {
            EnderChestListener.setPendingPlayer(playerName);
            client.getNetworkHandler().sendChatCommand("data get entity " + playerName + " EnderItems");
        } catch (Exception e) {
            client.player.sendMessage(Text.literal("[INVVIEW] Failed to execute command").formatted(Formatting.RED),false);
        }
    }
}