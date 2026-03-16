package air.invview.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class Utils {
    public static void msg(String msg, boolean err) {
        assert MinecraftClient.getInstance().player != null;
        MinecraftClient.getInstance().player.sendMessage(Text.literal(msg).formatted(err ? Formatting.RED : Formatting.RESET), false);
    }

    public static Collection<PlayerListEntry> getOnlinePlayers() {
        MinecraftClient client = MinecraftClient.getInstance();
        List<PlayerListEntry> players = new ArrayList<>(Objects.requireNonNull(client.getNetworkHandler()).getPlayerList());

        players.sort((a,b) -> {
            String teamA = a.getScoreboardTeam() != null ? a.getScoreboardTeam().getName() : "";
            String teamB = b.getScoreboardTeam() != null ? b.getScoreboardTeam().getName() : "";

            int teamCompare = teamA.compareToIgnoreCase(teamB);
            if (teamCompare != 0) return teamCompare;

            return a.getProfile().name().compareToIgnoreCase(b.getProfile().name());
        });

        return players;
    }

    public static boolean isErrorMsg(Text msg) {
        TextColor red = TextColor.fromFormatting(Formatting.RED);
        Style style = msg.getStyle();

        if (style.getColor() != null && style.getColor().equals(red)) return true;

        for (Text sibling : msg.getSiblings()) {
            if (sibling.getStyle().getColor() != null && sibling.getStyle().getColor().equals(red)) return true;
        }

        return false;
    }
}
