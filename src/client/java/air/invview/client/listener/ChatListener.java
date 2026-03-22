package air.invview.client.listener;

import air.invview.client.Utils;
import air.invview.client.gui.PlayerInvScreen;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static air.invview.client.gui.PlayerInvScreen.readStack;

public class ChatListener {
    private enum Step {
        INVENTORY,
        EQUIPMENT,
        HEALTH,
        ARMOR,
        XP,
        HUNGER
    }

    public static final Pattern INVENTORY_RESULT = Pattern.compile("(\\[.*?]|\\{.*?})",Pattern.DOTALL);

    public static final Pattern LIST_PATTERN = Pattern.compile("(\\[.*\\])", Pattern.DOTALL);
    public static final Pattern COMPOUND_PATTERN = Pattern.compile("(\\{.*\\})", Pattern.DOTALL);
    public static final Pattern FLOAT_PATTERN = Pattern.compile("([\\d.]+)f?\\s*$");
    public static final Pattern INT_PATTERN = Pattern.compile("(\\d+)\\s*$");

    public static final String NO_DATA = "No element found";

    private static String pendingPlayer;
    private static List<ItemStack> pendingItems;

    private static float pendingHealth = 20f;
    private static int pendingArmor = 0;
    private static int pendingXP = 0;
    private static int pendingHunger = 20;
    private static String pendingMessage;
    private static long lastResponse = 0;

    private static Step step = Step.INVENTORY;

    public static void setPendingPlayer(String name) {
        pendingPlayer = name;

        pendingItems = new ArrayList<>();
        for (int i = 0; i < 41; i++) pendingItems.add(ItemStack.EMPTY);

        pendingHealth = 20f;
        pendingArmor = 0;
        pendingXP = 0;
        pendingHunger = 20;

        step = Step.INVENTORY;
        lastResponse = System.currentTimeMillis();
    }

    public static void register() {
        ClientReceiveMessageEvents.ALLOW_GAME.register((message, overlay) -> {
            if (pendingPlayer == null) return true;

            try {
                if (Utils.isErrorMsg(message)) {
                    pendingMessage = "[InvView] Error executing command.";
                    reset();
                    return false;
                }
            } catch (Exception ignored) {};

            lastResponse = System.currentTimeMillis();

            String raw = message.getString();

            switch (step) {
                case INVENTORY -> {
                    if (!handleInventory(raw)) return true;
                    step = Step.EQUIPMENT;
                    next();
                }

                case EQUIPMENT -> {
                    if (!handleEquipment(raw)) return true;
                    step = Step.HEALTH;
                    next();
                }

                case HEALTH -> {
                    if (!handleHealth(raw)) return true;
                    //step = Step.ARMOR;
                    step = Step.XP;
                    next();
                }

                case ARMOR -> {
                    if (!handleArmor(raw)) return true;
                    step = Step.XP;
                    next();
                }

                case XP -> {
                    if (!handleXP(raw)) return true;
                    step = Step.HUNGER;
                    next();
                }

                case HUNGER -> {
                    if (!handleHunger(raw)) return true;
                    finish();
                }
            }
            return false;
        });

        ClientSendMessageEvents.ALLOW_COMMAND.register(cmd -> {
            //if (pendingPlayer != null) return !cmd.contains("data get entity");
            return true;
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (pendingMessage != null) {
                Utils.msg(pendingMessage,true);
                pendingMessage = null;
            }

            if (pendingPlayer == null) return;

            if (System.currentTimeMillis() - lastResponse > 1000) {
                pendingMessage = "[InvView] Server didn't return command data.";
                reset();
            }
        });
    }

    private static void next() {
        switch (step) {
            case EQUIPMENT -> send("data get entity " + pendingPlayer + " equipment");
            case HEALTH -> send("data get entity " + pendingPlayer + " Health");
            case ARMOR -> send("data get entity " + pendingPlayer + " ArmorPoints");
            case XP -> send("data get entity " + pendingPlayer + " XpLevel");
            case HUNGER -> send("data get entity " + pendingPlayer + " foodLevel");
        }
    }

    private static boolean handleInventory(String raw) {
        if (!raw.contains("{") || !raw.contains("}")) return false;

        Matcher m = INVENTORY_RESULT.matcher(raw);

        if (m.find()) {
            parseInventory(m.group(1));
            return true;
        }

        if (raw.contains(NO_DATA)) {
            pendingMessage = "[InvView] empty inventory";
            return true;
        }

        return false;
    }

    private static boolean handleEquipment(String raw) {
        if (!raw.contains("{") || !raw.contains("}")) return false;

        Matcher m = COMPOUND_PATTERN.matcher(raw);

        if (m.find()) {
            parseEquipment(m.group(1));
            return true;
        }

        for (int slot : new int[]{36, 37, 38, 39, 40}) {
            if (slot < pendingItems.size()) pendingItems.set(slot, ItemStack.EMPTY);
        }
        return true;
    }

    private static boolean handleHealth(String raw) {
        Matcher m = FLOAT_PATTERN.matcher(raw);

        if (m.find()) {
            pendingHealth = Float.parseFloat(m.group(1));
            return true;
        }

        return raw.contains(NO_DATA);
    }

    private static boolean handleArmor(String raw) {
        Matcher m = INT_PATTERN.matcher(raw);

        if (m.find()) {
            pendingArmor = Integer.parseInt(m.group(1));
            return true;
        }

        return raw.contains(NO_DATA);
    }

    private static boolean handleXP(String raw) {
        Matcher m = INT_PATTERN.matcher(raw);

        if (m.find()) {
            pendingXP = Integer.parseInt(m.group(1));
            return true;
        }

        return raw.contains(NO_DATA);
    }

    private static boolean handleHunger(String raw) {
        Matcher m = INT_PATTERN.matcher(raw);

        if (m.find()) {
            pendingHunger = Integer.parseInt(m.group(1));
            return true;
        }

        return raw.contains(NO_DATA);
    }

    private static void finish() {
        String player = pendingPlayer;
        List<ItemStack> items = pendingItems;

        float health = pendingHealth;
        int armor = pendingArmor;
        int xp = pendingXP;
        int hunger = pendingHunger;

        reset();

        MinecraftClient.getInstance().execute(() -> MinecraftClient.getInstance().setScreen(new PlayerInvScreen(player, items, health, armor, xp, hunger)));
    }

    private static void reset() {
        pendingPlayer = null;
        pendingItems = null;
        step = Step.INVENTORY;
    }

    public static void send(String cmd) {
        var client = MinecraftClient.getInstance();

        if (client == null || client.getNetworkHandler() == null) return;

        client.execute(() -> client.getNetworkHandler().sendChatCommand(cmd));
    }

    private static void parseInventory(String nbt) {
        try {
            NbtElement parsed = StringNbtReader.fromOps(NbtOps.INSTANCE).read(nbt);

            if (!(parsed instanceof NbtList list)) return;

            for (NbtElement nbtElement : list) {
                if (!(nbtElement instanceof NbtCompound tag)) continue;

                int slot = tag.getByte("Slot").orElse((byte) 0) & 0xFF;

                ItemStack item = readStack(tag);
                if (slot < pendingItems.size()) pendingItems.set(slot,item);
            }
        } catch (CommandSyntaxException ignored) {}
    }

    private static void parseEquipment(String nbtString) {
        try {
            NbtElement parsed = StringNbtReader.fromOps(NbtOps.INSTANCE).read(nbtString);
            if (!(parsed instanceof NbtCompound equipment)) return;

            Map<String, Integer> slotMap = Map.of(
                    "head", 39,
                    "chest", 38,
                    "legs", 37,
                    "feet", 36,
                    "offhand", 40
            );

            for (var entry : slotMap.entrySet()) {
                if (!equipment.contains(entry.getKey())) continue;

                NbtCompound tag = equipment.getCompound(entry.getKey()).orElse(null);
                if (tag == null) continue;

                int slot = entry.getValue();

                ItemStack item = readStack(tag);
                if (slot < pendingItems.size()) pendingItems.set(slot,item);
            }
        } catch (CommandSyntaxException e) {
            System.err.println("[InvView] Equipment parse error: " + e.getMessage());
        }
    }
}