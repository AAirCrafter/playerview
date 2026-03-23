package air.playerview.client.gui;

import air.playerview.client.ViewInvCommand;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.RegistryOps;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;

import java.util.Arrays;
import java.util.List;

public class PlayerInvScreen extends Screen {
    private static final Identifier TEXTURE = Identifier.of("playerview", "textures/gui/inv.png");
    private static final Identifier HEART_TEXTURE = Identifier.of("playerview", "textures/heart.png");
    private static final Identifier XP_TEXTURE = Identifier.of("playerview", "textures/xp_bottle.png");
    private static final Identifier ARMOR_TEXTURE = Identifier.of("playerview", "textures/armor.png");
    private static final Identifier FOOD_TEXTURE = Identifier.of("playerview", "textures/food.png");
    private static final Identifier BOOTSPH_TEXTURE = Identifier.of("minecraft", "textures/gui/sprites/container/slot/boots.png");
    private static final Identifier LEGGINGSPH_TEXTURE = Identifier.of("minecraft", "textures/gui/sprites/container/slot/leggings.png");
    private static final Identifier CHESTPH_TEXTURE = Identifier.of("minecraft", "textures/gui/sprites/container/slot/chestplate.png");
    private static final Identifier HELMETPH_TEXTURE = Identifier.of("minecraft", "textures/gui/sprites/container/slot/helmet.png");
    private static final Identifier SHIELDPH_TEXTURE = Identifier.of("minecraft", "textures/gui/sprites/container/slot/shield.png");

    private final String playerName;
    private final List<ItemStack> items;
    private final float health;
    //private final int armor;
    private final int xp;
    private final int hunger;
    private final int[] armorSlots = {39, 38, 37, 36};

    private static final int GUI_WIDTH = 176;
    private static final int GUI_HEIGHT = 166;

    private int x, y;

    public PlayerInvScreen(String playerName, List<ItemStack> items, float health, int armor, int xpLevel, int hunger) {
        super(Text.literal(playerName + "'s Inventory"));
        this.playerName = playerName;
        this.items = items;
        this.health = health;
        //this.armor = armor;
        this.xp = xpLevel;
        this.hunger = hunger;
    }

    @Override
    protected void init() {
        this.x = (this.width - GUI_WIDTH) / 2;
        this.y = (this.height - GUI_HEIGHT) / 2;

        this.addDrawableChild(ButtonWidget.builder(Text.literal("✕"), btn -> this.close())
                .dimensions(x + GUI_WIDTH - 23, y + 7, 16, 16).build());

        this.addDrawableChild(ButtonWidget.builder(Text.literal(""), btn -> openEnderChest(this.playerName))
                .dimensions(x + GUI_WIDTH - 25, y + 61, 18, 18).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, this.width, this.height, 0x20000000);

        context.drawTexture(
                RenderPipelines.GUI_TEXTURED,
                TEXTURE,
                x, y,
                0, 0,
                GUI_WIDTH, GUI_HEIGHT,
                256, 256
        );

        Text name = Text.literal(playerName + "'s ").append(Text.translatable("key.category.minecraft.inventory"));
        int textx = (this.width - textRenderer.getWidth(name)) / 2;

        context.drawText(this.textRenderer,name,textx, y - 10,0xFFFFFFFF,true);
        renderHealth(context);
        //renderArmor(context);
        renderXP(context);
        renderHunger(context);

        renderItems(context, mouseX, mouseY);

        for (var child : this.children()) {
            if (child instanceof Drawable drawable) drawable.render(context, mouseX, mouseY, delta);
        }

        context.drawItem(Items.ENDER_CHEST.getDefaultStack(), x + GUI_WIDTH - 24, y + 62);

        var world = MinecraftClient.getInstance().world;
        if (world != null) {
            for (var p : world.getPlayers()) {
                if (p.getName().getString().equals(playerName)) {
                    InventoryScreen.drawEntity(context,x + 25, y + 10,x + 75, y + 75,30,0.0f,mouseX, mouseY,p);
                    break;
                }
            }
        }
    }

    private void renderItems(DrawContext context, int mouseX, int mouseY) {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                int slotIndex = 9 + row * 9 + col;
                int slotX = x + 8 + col * 18;
                int slotY = y + 84 + row * 18;
                renderSlot(context, slotX, slotY, slotIndex, mouseX, mouseY);
            }
        }

        for (int col = 0; col < 9; col++) {
            int slotX = x + 8 + col * 18;
            int slotY = y + 142;
            renderSlot(context, slotX, slotY, col, mouseX, mouseY);
        }

        for (int i = 0; i < 4; i++) {
            int slotX = x + 8;
            int slotY = y + 8 + i * 18;
            renderSlot(context, slotX, slotY, armorSlots[i], mouseX, mouseY);
        }

        renderSlot(context, x + 77, y + 62, 40, mouseX, mouseY);
    }

    private void renderSlot(DrawContext context, int slotX, int slotY,int index, int mouseX, int mouseY) {
        if (index >= items.size()) return;

        ItemStack stack = items.get(index);
        if (stack == null || stack.isEmpty()) {
            if (Arrays.stream(armorSlots).anyMatch(x -> x == index) || index == 40) {
                switch (index) {
                    case (36):
                        context.drawTexture(RenderPipelines.GUI_TEXTURED,BOOTSPH_TEXTURE,slotX, slotY,0, 0,
                            16, 16,16, 16);
                        break;
                    case (37):
                        context.drawTexture(RenderPipelines.GUI_TEXTURED,LEGGINGSPH_TEXTURE,slotX, slotY,0, 0,
                                16, 16,16, 16);
                        break;
                    case (38):
                        context.drawTexture(RenderPipelines.GUI_TEXTURED,CHESTPH_TEXTURE,slotX, slotY,0, 0,
                                16, 16,16, 16);
                        break;
                    case (39):
                        context.drawTexture(RenderPipelines.GUI_TEXTURED,HELMETPH_TEXTURE,slotX, slotY,0, 0,
                                16, 16,16, 16);
                        break;
                    case (40):
                        context.drawTexture(RenderPipelines.GUI_TEXTURED,SHIELDPH_TEXTURE,slotX, slotY,0, 0,
                                16, 16,16, 16);
                        break;
                }
            }
            return;
        }

        context.drawItem(stack, slotX, slotY);
        context.drawStackOverlay(this.textRenderer, stack, slotX, slotY);

        if (mouseX >= slotX && mouseX < slotX + 16 && mouseY >= slotY && mouseY < slotY + 16) {
            context.fill(slotX, slotY, slotX + 16, slotY + 16, 0x80FFFFFF);
            context.drawItemTooltip(this.textRenderer, stack, mouseX, mouseY);
        }
    }

    private void renderHealth(DrawContext context) {
        context.drawTexture(
                RenderPipelines.GUI_TEXTURED,
                HEART_TEXTURE,
                x + 80, y + 10,
                0, 0,
                8, 8,
                8, 8
        );

        context.drawText(textRenderer,Text.literal(String.valueOf((float) Math.round(health) / 2)),x + 90, y + 10, Colors.WHITE,true);
    }

    private void renderArmor(DrawContext context) {
        context.drawTexture(
                RenderPipelines.GUI_TEXTURED,
                ARMOR_TEXTURE,
                x + 80, y + 20,
                0, 0,
                8, 8,
                8, 8
        );

        //context.drawText(textRenderer,Text.literal(String.valueOf(armor)),x + 90, y + 20, Colors.LIGHT_GRAY,true);
        context.drawText(textRenderer,Text.literal("?"),x + 90, y + 20, Colors.WHITE,true);
    }

    private void renderHunger(DrawContext context) {
        context.drawTexture(
                RenderPipelines.GUI_TEXTURED,
                FOOD_TEXTURE,
                x + 80, y + 20,
                0, 0,
                8, 8,
                8, 8
        );

        context.drawText(textRenderer,Text.literal(String.valueOf(hunger)),x + 90, y + 20, Colors.WHITE,true);
    }

    private void renderXP(DrawContext context) {
        context.drawTexture(
                RenderPipelines.GUI_TEXTURED,
                XP_TEXTURE,
                x + 80, y + 30,
                0, 0,
                8, 8,
                8, 8
        );

        context.drawText(textRenderer,Text.literal(String.valueOf(xp)),x + 90, y + 30, Colors.WHITE,true);
    }

    private void openEnderChest(String playerName) {
        ViewInvCommand.openEnderChest(playerName);
        this.close();
    }

    public static ItemStack readStack(NbtCompound tag) {
        NbtCompound fixed = tag.copy();

        if (fixed.contains("Count")) {
            int count = fixed.getByte("Count").orElse((byte) 1);
            fixed.remove("Count");
            fixed.putInt("count", count);
        }

        fixed.remove("Slot");

        RegistryWrapper.WrapperLookup lookup = MinecraftClient.getInstance().world.getRegistryManager();
        RegistryOps<NbtElement> ops = lookup.getOps(NbtOps.INSTANCE);

        var result = ItemStack.CODEC.parse(ops, fixed);
        result.ifError(e -> System.out.println("[InvView ERROR] " + e.message()));
        return result.result().orElse(ItemStack.EMPTY);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}