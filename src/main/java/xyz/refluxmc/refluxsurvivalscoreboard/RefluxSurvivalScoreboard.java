package xyz.refluxmc.refluxsurvivalscoreboard;

import me.clip.placeholderapi.PlaceholderAPI;
import fr.mrmicky.fastboard.FastBoard;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import java.lang.Math;
import java.text.DecimalFormat;

public final class RefluxSurvivalScoreboard extends JavaPlugin implements Listener {

    private final Map<UUID, FastBoard> boards = new HashMap<>();
    private final DecimalFormat tpsFormat = new DecimalFormat("##.00");

    @Override
    public void onEnable() {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI")) {
            Bukkit.getPluginManager().registerEvents(this, this);
        } else {
            getLogger().warning("Could not find PlaceholderAPI! Disabling...");
            Bukkit.getPluginManager().disablePlugin(this);
        }

        getServer().getPluginManager().registerEvents(this, this);

        getServer().getScheduler().runTaskTimer(this, () -> {
            for (FastBoard board : this.boards.values()) {
                updateBoard(board);
            }
        }, 0, 100);

        getLogger().info("Starting RefluxSurvivalScoreboard.");
    }

    public String getCurrentTPS() {
        try {
            Object server = Bukkit.getServer().getClass().getMethod("getServer").invoke(Bukkit.getServer());
            double[] recentTps = (double[]) server.getClass().getField("recentTps").get(server);
            recentTps[0] = Math.min(20.00, recentTps[0]);
            return tpsFormat.format(recentTps[0]);
        } catch (Exception e) {
            e.printStackTrace();
            return "Unable to get recent tps!";
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
        String rankPlaceholder = PlaceholderAPI.setPlaceholders(player, "%phoenix_player_rank%");
        String playerCount = PlaceholderAPI.setPlaceholders(player, "%server_online%");

        board.updateLines(
                ChatColor.GRAY + ChatColor.STRIKETHROUGH.toString() + "--*--------------*--",
                ChatColor.AQUA + "Online Players: ",
                ChatColor.WHITE + playerCount,
                "",
                ChatColor.AQUA + "Current TPS: ",
                ChatColor.WHITE + getCurrentTPS(),
                "",
                ChatColor.AQUA + "Your Rank: ",
                ChatColor.WHITE + rankPlaceholder,
                "",
                ChatColor.GRAY + "" + ChatColor.ITALIC + "refluxmc.xyz",
                ChatColor.GRAY + ChatColor.STRIKETHROUGH.toString() + "--*--------------*--"
        );
    }

    @Override
    public void onDisable() {
        getLogger().info("Disabled RefluxSurvivalScoreboard.");
    }
}

