package dev.dsmp.economy;

import dev.dsmp.economy.core.BalanceReader;
import dev.dsmp.economy.core.BalanceReading;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.numbers.NumberFormat;
import net.minecraft.network.chat.numbers.StyledFormat;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerScoreEntry;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;

public final class SidebarBalance {
    private SidebarBalance() {
    }

    public static OptionalLong read(Minecraft client) {
        Optional<BalanceReading> reading = SidebarBalance.reading(client);
        return reading.isPresent() ? OptionalLong.of(reading.get().cents()) : OptionalLong.empty();
    }

    public static Optional<BalanceReading> reading(Minecraft client) {
        return BalanceReader.sidebarReading(SidebarBalance.rows(client));
    }

    public static List<BalanceReader.Row> rows(Minecraft client) {
        DisplaySlot slot;
        PlayerTeam playerTeam;
        if (client.level == null) {
            return List.of();
        }
        Scoreboard scoreboard = client.level.getScoreboard();
        Objective objective = scoreboard.getDisplayObjective(DisplaySlot.SIDEBAR);
        if (client.player != null && (playerTeam = scoreboard.getPlayersTeam(client.player.getScoreboardName())) != null && (slot = playerTeam.getColor().map(net.minecraft.world.scores.TeamColor::displaySlot).orElse(null)) != null && scoreboard.getDisplayObjective(slot) != null) {
            objective = scoreboard.getDisplayObjective(slot);
        }
        if (objective == null) {
            return List.of();
        }
        ArrayList<BalanceReader.Row> rows = new ArrayList<BalanceReader.Row>();
        List<PlayerScoreEntry> entries = scoreboard.listPlayerScores(objective).stream().filter(entry -> !entry.isHidden()).sorted(Comparator.comparingInt(PlayerScoreEntry::value).reversed().thenComparing(PlayerScoreEntry::owner, String.CASE_INSENSITIVE_ORDER)).limit(15L).toList();
        for (PlayerScoreEntry entry2 : entries) {
            PlayerTeam team = scoreboard.getPlayersTeam(entry2.owner());
            String name = entry2.ownerName().getString();
            String prefix = team == null ? "" : team.getPlayerPrefix().getString();
            String suffix = team == null ? "" : team.getPlayerSuffix().getString();
            Optional<BalanceReading> components = BalanceReader.sidebarReading(List.of(new BalanceReader.Row(prefix + suffix, "")));
            String right = entry2.formatValue(objective.numberFormatOrDefault((NumberFormat)StyledFormat.SIDEBAR_DEFAULT)).getString();
            rows.add(new BalanceReader.Row(components.isPresent() ? prefix + suffix : prefix + name + suffix, components.isPresent() ? "" : right));
        }
        return List.copyOf(rows);
    }
}
