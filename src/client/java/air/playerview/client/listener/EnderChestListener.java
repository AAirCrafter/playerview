package air.playerview.client.listener;

import air.playerview.client.Utils;
import air.playerview.client.gui.PlayerEnderChestScreen;
import air.playerview.client.gui.PlayerInvScreen;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraft.text.Style;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

import static air.playerview.client.listener.ChatListener.LIST_PATTERN;

public class EnderChestListener {
    private static final String NO_DATA = "No element found";

    private static String pendingPlayer = null;

    public static void setPendingPlayer(String name) {
        pendingPlayer = name;
    }

    public static void register() {
        ClientReceiveMessageEvents.ALLOW_GAME.register((message, overlay) -> {
            if (pendingPlayer == null) return true;

            try {
                Style style = message.getStyle();
                if (style.getColor() != null && style.getColor().equals(TextColor.fromFormatting(Formatting.RED))) {
                    Utils.msg("Error executing command.",true);
                    pendingPlayer = null;
                    return false;
                }
            } catch (Exception ignored) {};

            String raw = message.getString();
            Matcher m = LIST_PATTERN.matcher(raw);

            if (m.find()) {
                List<ItemStack> items = parseEnderChest(m.group(1));
                String player = pendingPlayer;
                pendingPlayer = null;

                MinecraftClient.getInstance().execute(() -> MinecraftClient.getInstance().setScreen(new PlayerEnderChestScreen(player, items)));
            } else if (raw.contains(NO_DATA) || raw.contains("no elements")) {
                Utils.msg("empty enderchest", true);
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

            for (NbtElement nbtElement : list) {
                if (!(nbtElement instanceof NbtCompound tag)) continue;

                int slot = tag.getByte("Slot").orElse((byte) 0) & 0xFF;
                ItemStack item = PlayerInvScreen.readStack(tag);
                if (slot < items.size()) items.set(slot, item);
            }
        } catch (CommandSyntaxException e) {
            System.err.println("[InvView] EnderChest parse error: " + e.getMessage());
        }

        return items;
    }
}