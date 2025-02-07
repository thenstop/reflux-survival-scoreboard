package xyz.refluxmc.refluxsurvivalscoreboard;

import me.clip.placeholderapi.PlaceholderAPI;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import fr.mrmicky.fastboard.FastBoard;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class RefluxSurvivalScoreboard extends JavaPlugin implements Listener {

    private final Map<UUID, FastBoard> boards = new HashMap<>();
    private final DecimalFormat tpsFormat = new DecimalFormat("##.00");

    @Override
    public void onEnable() {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            Bukkit.getPluginManager().registerEvents(this, this);
        } else {
            getLogger().warning("Could not find PlaceholderAPI! This plugin is required.");
            Bukkit.getPluginManager().disablePlugin(this);
        }

        getServer().getPluginManager().registerEvents(this, this);

        getServer().getScheduler().runTaskTimer(this, () -> {
            for (FastBoard board : this.boards.values()) {
                updateBoard(board);
            }
        }, 0, 20);
        getLogger().info("Enabled RefluxSurvivalScoreboard.");
    }

    public String getCurrentTPS() {
        try {
            Object server = Bukkit.getServer().getClass().getMethod("getServer").invoke(Bukkit.getServer());
            double[] recentTps = (double[]) server.getClass().getField("recentTps").get(server);
            if (recentTps[0] > 20.00) {
                recentTps[0] = 20.00;
            }
            return tpsFormat.format(recentTps[0]);
        } catch (Exception e) {
            e.printStackTrace();
            return "N/A";
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();

        FastBoard board = new FastBoard(player);
        board.updateTitle(ChatColor.AQUA + "" + ChatColor.BOLD + "Survival");
        this.boards.put(player.getUniqueId(), board);

        updateBoard(board);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Player player = e.getPlayer();

        FastBoard board = this.boards.remove(player.getUniqueId());

        if (board != null) {
            board.delete();
        }
    }

    private void updateBoard(FastBoard board) {
        Player player = board.getPlayer();
        String rankPlaceholder = PlaceholderAPI.setPlaceholders(player, "%luckperms_primary_group_name%");
        String playerCount = PlaceholderAPI.setPlaceholders(player, "%server_online%");

        board.updateLines(
                ChatColor.GRAY + ChatColor.STRIKETHROUGH.toString() + "--------------------",
                ChatColor.AQUA + "Online Players: ",
                ChatColor.WHITE + playerCount,
                "",
                ChatColor.AQUA + "Current TPS: ",
                ChatColor.WHITE + getCurrentTPS(),
                "",
                ChatColor.AQUA + "Your Rank: ",
                ChatColor.WHITE + rankPlaceholder,
                "",
                ChatColor.AQUA + "refluxmc.xyz",
                ChatColor.GRAY + ChatColor.STRIKETHROUGH.toString() + "--------------------"
        );
}

    @Override
    public void onDisable() {
        getLogger().info("Disabled RefluxSurvivalScoreboard.");
    }
}

