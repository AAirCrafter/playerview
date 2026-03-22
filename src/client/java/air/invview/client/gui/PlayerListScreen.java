package air.invview.client.gui;

import air.invview.client.Utils;
import air.invview.client.ViewInvCommand;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.Objects;

public class PlayerListScreen extends Screen {
    private PlayerListWidget list;
    public static PlayerListEntry hoveredPlayer;

    public PlayerListScreen() {
        super(Text.literal("Players"));
    }

    @Override
    protected void init() {
        list = new PlayerListWidget(client, width, height, 0, height, 0);

        if (client != null && client.world != null) {
            for (PlayerListEntry player : Utils.getOnlinePlayers()) {
                String playername;
                playername = player.getProfile().name();

                list.addPlayerEntry(playername);
            }
        }

        addDrawableChild(list);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        //context.drawText(textRenderer,Text.literal("test"), 0,0,0xFFFFFFFF,true);
    }


    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void close() {
        hoveredPlayer = null;
        super.close();
    }

    private static class PlayerListWidget extends AlwaysSelectedEntryListWidget<PlayerEntry> {
        public PlayerListWidget(MinecraftClient client, int w, int h, int top, int bottom, int itemH) {
            super(client, w, h, top, bottom);
        }

        @Override
        protected void drawMenuListBackground(DrawContext context) {
            context.fill(this.getX(), this.getY(), this.getRight(), this.getBottom(), 0x20000000);
        }

        public void addPlayerEntry(String name) {
            this.addEntry(new PlayerEntry(name));
        }
    }

    private static class PlayerEntry extends AlwaysSelectedEntryListWidget.Entry<PlayerEntry> {
        private final String name;
        private final PlayerListEntry playerEntry;

        public PlayerEntry(String name) {
            this.name = name;
            PlayerListEntry found = null;
            for (PlayerListEntry player : Objects.requireNonNull(MinecraftClient.getInstance().getNetworkHandler()).getPlayerList()) {
                String n = player.getProfile().name();
                if (n.equals(name)) { found = player; break; }
            }
            this.playerEntry = found;
        }

        @Override
        public int getHeight() {
            return 22;
        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, boolean hovered, float deltaTicks) {
            int x = this.getX();
            int y = this.getY();

            if (hovered) {
                context.fill(x, y, x + this.getWidth(), y + this.getHeight(), 0x30FFFFFF);
                hoveredPlayer = playerEntry;
            } else {
                hoveredPlayer = null;
            }

            if (playerEntry != null) {
                Identifier skin = playerEntry.getSkinTextures().body().texturePath();
                context.drawTexture(
                        RenderPipelines.GUI_TEXTURED,
                        skin,
                        x + 5, y + 3,
                        8f, 8f,
                        16, 16,
                        8, 8,
                        64, 64
                );

                context.drawTexture(
                        RenderPipelines.GUI_TEXTURED,
                        skin,
                        x + 5, y + 3,
                        40f, 8f,
                        16, 16,
                        8, 8,
                        64, 64
                );
            }

            Text n = Text.literal(name).formatted();
            assert MinecraftClient.getInstance().player != null;
            if (name.equals(MinecraftClient.getInstance().player.getStringifiedName())) n = Text.literal(name).formatted(Formatting.BOLD);

            context.drawTextWithShadow(
                    MinecraftClient.getInstance().textRenderer,
                    n,
                    x + 24,
                    y + (this.getHeight() - 8) / 2,
                    0xFFFFFFFF
            );

            context.fill(x + 5, y + this.getHeight() - 1, x + this.getWidth() - 5, y + this.getHeight(), 0x30FFFFFF);
        }

        @Override
        public boolean mouseClicked(Click click, boolean doubled) {
            ViewInvCommand.openFor(name);
            return true;
        }

        @Override
        public Text getNarration() {
            return Text.literal(name);
        }
    }
}