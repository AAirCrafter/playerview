package air.playerview.client.gui;

import air.playerview.client.Utils;
import air.playerview.client.ViewInvCommand;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.Objects;

public class PlayerListScreen extends Screen {
    private PlayerListWidget list;
    public static PlayerListEntry hoveredPlayer;
    private TextFieldWidget searchField;

    public PlayerListScreen() {
        super(Text.literal("Players"));
    }

    @Override
    protected void init() {
        searchField = new TextFieldWidget(textRenderer, width / 2 - 100, 5, 200, 15, Text.literal("Search"));
        searchField.setChangedListener(this::updateList);
        searchField.setDrawsBackground(false);
        searchField.setEditableColor(0xFFFFFFFF);
        addDrawableChild(searchField);
        setInitialFocus(searchField);

        list = new PlayerListWidget(client, width, height, 20, height, 0);

        if (client != null && client.world != null) {
            for (PlayerListEntry player : Utils.getOnlinePlayers()) {
                String playername;
                playername = player.getProfile().name();

                list.addPlayerEntry(playername,player.getGameMode().toString() != null ? player.getGameMode().toString() : "unknown");
            }
        }

        addDrawableChild(list);
    }

    private void updateList(String query) {
        list.clearEntries();
        for (PlayerListEntry player : Utils.getOnlinePlayers()) {
            String name = player.getProfile().name();
            String gamemode = player.getGameMode().asString();
            if (name.toLowerCase().contains(query.toLowerCase())) list.addPlayerEntry(name, gamemode != null ? gamemode : "unknown");
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(
                searchField.getX() - 2,searchField.getY() - 3,
                searchField.getX() + searchField.getWidth(), searchField.getY() + searchField.getHeight() - 3,
                0x30FFFFFF);

        super.render(context, mouseX, mouseY, delta);
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
            context.fill(this.getX(), this.getY(), this.getRight(), this.getBottom(), 0x10000000);
        }

        public void addPlayerEntry(String name, String gamemode) {
            this.addEntry(new PlayerEntry(name, gamemode));
        }

        public void clearEntries() {
            super.clearEntries();
        }
    }

    private static class PlayerEntry extends AlwaysSelectedEntryListWidget.Entry<PlayerEntry> {
        private final String name;
        private final String gamemode;
        private final PlayerListEntry playerEntry;

        public PlayerEntry(String name, String gamemode) {
            this.name = name;
            this.gamemode = gamemode;
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
            hoveredPlayer = null;
            int x = this.getX();
            int y = this.getY();

            if (hovered) {
                context.fill(x, y, x + this.getWidth(), y + this.getHeight(), 0x30FFFFFF);
                hoveredPlayer = playerEntry;
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

            int iconX = x + this.getWidth() - 20;
            int iconY = y + (this.getHeight() - 16) / 2;

            context.drawItem(getGamemodeIcon(gamemode), x + this.getWidth() - 20, y + (this.getHeight() - 16) / 2);

            if (mouseX >= iconX && mouseX < iconX + 12 && mouseY >= iconY && mouseY < iconY + 12) {
                context.drawTooltip(MinecraftClient.getInstance().textRenderer, Text.literal(gamemode), mouseX, mouseY);
            }

            context.fill(x + 5, y + this.getHeight() - 1, x + this.getWidth() - 5, y + this.getHeight(), 0x30FFFFFF);
        }

        @Override
        public boolean mouseClicked(Click click, boolean doubled) {
            hoveredPlayer = null;
            ViewInvCommand.openFor(name);
            return true;
        }

        @Override
        public Text getNarration() {
            return Text.literal(name);
        }
    }

    private static ItemStack getGamemodeIcon(String gamemode) {
        return switch (gamemode.toLowerCase()) {
            case "survival" -> new ItemStack(Items.IRON_SWORD);
            case "creative" -> new ItemStack(Items.GRASS_BLOCK);
            case "adventure" -> new ItemStack(Items.MAP);
            case "spectator" -> new ItemStack(Items.ENDER_EYE);
            default -> new ItemStack(Items.BARRIER);
        };
    }
}