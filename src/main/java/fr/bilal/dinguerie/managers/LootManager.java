package fr.bilal.dinguerie.managers;

import fr.bilal.dinguerie.DinguerieLoot;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.util.*;

public class LootManager {

    private final DinguerieLoot plugin;
    private final ConfigManager configManager;
    private boolean isRunning;
    private Integer taskId;
    private int timeLeft;
    private final Random random;
    private Scoreboard scoreboard;
    private Objective objective;
    private List<Material> availableItems;

    public LootManager(DinguerieLoot plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
        this.isRunning = false;
        this.random = new Random();
        this.availableItems = new ArrayList<>();
        loadAvailableItems();
    }

    private void loadAvailableItems() {
        // Charger tous les items du registre Minecraft
        for (Material material : Material.values()) {
            if (material.isItem() && !material.isAir()) {
                availableItems.add(material);
            }
        }
    }

    public void startLoot() {
        if (isRunning) {
            Bukkit.getOnlinePlayers().forEach(player ->
                player.sendMessage(configManager.getMessage("already-running"))
            );
            return;
        }

        isRunning = true;
        timeLeft = configManager.getIntervalSeconds();
        setupScoreboard();

        // Annoncer le démarrage
        Bukkit.broadcastMessage(configManager.getMessage("start"));

        // Tâche de distribution et décompte
        taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            if (timeLeft <= 0) {
                // Distribuer les items
                distributeItems();
                timeLeft = configManager.getIntervalSeconds();
            } else {
                // Mettre à jour le scoreboard
                updateScoreboard();
                timeLeft--;
            }
        }, 0, 20); // 20 ticks = 1 seconde
    }

    public void stopLoot() {
        if (!isRunning) {
            if (Bukkit.getOnlinePlayers().size() > 0) {
                Bukkit.broadcastMessage(configManager.getMessage("not-running"));
            }
            return;
        }

        isRunning = false;
        if (taskId != null) {
            Bukkit.getScheduler().cancelTask(taskId);
        }

        // Retirer le scoreboard
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
        }

        Bukkit.broadcastMessage(configManager.getMessage("stop"));
    }

    private void setupScoreboard() {
        scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        objective = scoreboard.registerNewObjective("loot_timer", "dummy", configManager.getMessage("scoreboard-title"));
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        // Mettre à jour pour tous les joueurs
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.setScoreboard(scoreboard);
        }

        updateScoreboard();
    }

    private void updateScoreboard() {
        if (objective == null) return;

        objective.getScore(configManager.getMessage("scoreboard-timer")).setScore(timeLeft);
    }

    private void distributeItems() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            Material randomItem = getRandomItem();
            ItemStack item = new ItemStack(randomItem, 1);
            
            if (player.getInventory().firstEmpty() != -1) {
                player.getInventory().addItem(item);
            } else {
                // Si l'inventaire est plein, drop l'item au sol
                player.getWorld().dropItem(player.getLocation(), item);
            }

            String message = configManager.getMessage("item-distributed")
                .replace("%item%", randomItem.toString());
            player.sendMessage(message);
        }
    }

    private Material getRandomItem() {
        Material item;
        do {
            item = availableItems.get(random.nextInt(availableItems.size()));
        } while (configManager.isItemExcluded(item.toString()));

        return item;
    }

    public void excludeItem(Material material) {
        configManager.addExcludedItem(material.toString());
    }

    public void includeItem(Material material) {
        configManager.removeExcludedItem(material.toString());
    }

    public boolean isExcluded(Material material) {
        return configManager.isItemExcluded(material.toString());
    }

    public Set<String> getExcludedItems() {
        return configManager.getExcludedItems();
    }

    public boolean isRunning() {
        return isRunning;
    }
}
