package air.playerview.client.gui;

import air.playerview.client.ViewInvCommand;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.List;

public class PlayerEnderChestScreen extends Screen {
    private static final Identifier TEXTURE = Identifier.of("playerview", "textures/gui/enderchest.png");

    private final String playerName;
    private final List<ItemStack> items;

    private static final int GUI_WIDTH = 176;
    private static final int GUI_HEIGHT = 88;

    private int x, y;

    public PlayerEnderChestScreen(String playerName, List<ItemStack> items) {
        super(Text.literal(playerName + "'s ").append(Text.translatable("container.enderchest")));
        this.playerName = playerName;
        this.items = items;
    }

    @Override
    protected void init() {
        this.x = (this.width - GUI_WIDTH) / 2;
        this.y = (this.height - GUI_HEIGHT) / 2;

        this.addDrawableChild(ButtonWidget.builder(Text.literal("✕"), btn -> this.close())
                .dimensions(x + GUI_WIDTH - 23, y + 7, 16, 16).build());

        this.addDrawableChild(ButtonWidget.builder(Text.literal("←"), btn -> goBack())
                .dimensions(x + 7, y + 7, 16, 16).build());
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

        Text name = Text.literal(playerName + "'s Ender Chest");
        int textx = (this.width - textRenderer.getWidth(name)) / 2;
        context.drawText(this.textRenderer, name, textx, y - 10, 0xFFFFFFFF, true);

        renderItems(context, mouseX, mouseY);

        for (var child : this.children()) {
            if (child instanceof Drawable drawable) drawable.render(context, mouseX, mouseY, delta);
        }
    }

    private void renderItems(DrawContext context, int mouseX, int mouseY) {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                int slotIndex = row * 9 + col;
                int slotX = x + 8 + col * 18;
                int slotY = y + 18 + row * 18 + 10;
                renderSlot(context, slotX, slotY, slotIndex, mouseX, mouseY);
            }
        }
    }

    private void renderSlot(DrawContext context, int slotX, int slotY, int index, int mouseX, int mouseY) {
        if (index >= items.size()) return;

        ItemStack stack = items.get(index);
        if (stack == null || stack.isEmpty()) return;

        context.drawItem(stack, slotX, slotY);
        context.drawStackOverlay(this.textRenderer, stack, slotX, slotY);

        if (mouseX >= slotX && mouseX < slotX + 16 && mouseY >= slotY && mouseY < slotY + 16) {
            context.fill(slotX, slotY, slotX + 16, slotY + 16, 0x80FFFFFF);
            context.drawItemTooltip(this.textRenderer, stack, mouseX, mouseY);
        }
    }

    private void goBack() {
        ViewInvCommand.openFor(playerName);
        this.close();
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}