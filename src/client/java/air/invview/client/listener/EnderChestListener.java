package air.invview.client.listener;

import air.invview.client.Utils;
import air.invview.client.gui.PlayerEnderChestScreen;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

import static air.invview.client.listener.ChatListener.LIST_PATTERN;

public class EnderChestListener {
    private static final String NO_DATA = "No element found";

    private static String pendingPlayer = null;

    public static void setPendingPlayer(String name) {
        pendingPlayer = name;
    }

    public static void register() {
        ClientReceiveMessageEvents.ALLOW_GAME.register((message, overlay) -> {
            if (pendingPlayer == null) return true;

            String raw = message.getString();
            Matcher m = LIST_PATTERN.matcher(raw);

            if (m.find()) {
                List<ItemStack> items = parseEnderChest(m.group(1));
                String player = pendingPlayer;
                pendingPlayer = null;

                MinecraftClient.getInstance().execute(() -> MinecraftClient.getInstance().setScreen(new PlayerEnderChestScreen(player, items)));
            } else if (raw.contains(NO_DATA) || raw.contains("no elements")) {
                Utils.msg("[InvView] empty enderchest", true);
                String player = pendingPlayer;
                pendingPlayer = null;

                MinecraftClient.getInstance().execute(() -> MinecraftClient.getInstance().setScreen(new PlayerEnderChestScreen(player, new ArrayList<>())));
            }

            return false;
        });
    }

    private static List<ItemStack> parseEnderChest(String nbtString) {
        List<ItemStack> items = new ArrayList<>();
        for (int i = 0; i < 27; i++) items.add(ItemStack.EMPTY);

        try {
            NbtElement parsed = StringNbtReader.fromOps(NbtOps.INSTANCE).read(nbtString);
            if (!(parsed instanceof NbtList list)) return items;

            for (int i = 0; i < list.size(); i++) {
                if (!(list.get(i) instanceof NbtCompound tag)) continue;

                int slot = tag.getByte("Slot").orElse((byte) 0) & 0xFF;
                String id = tag.getString("id").orElse("");
                int count = tag.getInt("count").orElse(1);

                if (id.isEmpty()) continue;

                var item = Registries.ITEM.get(Identifier.of(id));
                if (slot < items.size()) items.set(slot, new ItemStack(item, count));
            }
        } catch (CommandSyntaxException e) {
            System.err.println("[InvView] EnderChest parse error: " + e.getMessage());
        }

        return items;
    }
}