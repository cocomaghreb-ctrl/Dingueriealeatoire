package fr.bilal.dinguerie.commands;

import fr.bilal.dinguerie.DinguerieLoot;
import fr.bilal.dinguerie.managers.ConfigManager;
import fr.bilal.dinguerie.managers.LootManager;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.stream.Collectors;

public class BilalCommand implements CommandExecutor {

    private final DinguerieLoot plugin;
    private final LootManager lootManager;
    private final ConfigManager configManager;

    public BilalCommand(DinguerieLoot plugin) {
        this.plugin = plugin;
        this.lootManager = plugin.getLootManager();
        this.configManager = plugin.getConfigManager();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cCette commande ne peut être exécutée que par un joueur!");
            return true;
        }

        Player player = (Player) sender;

        // Vérifier la permission
        if (!player.hasPermission("bilal.admin")) {
            player.sendMessage("§cVous n'avez pas la permission d'utiliser cette commande!");
            return true;
        }

        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        String subcommand = args[0].toLowerCase();

        switch (subcommand) {
            case "start":
                lootManager.startLoot();
                break;
            case "stop":
                lootManager.stopLoot();
                break;
            case "evite":
                handleEvite(player);
                break;
            case "inclut":
                handleInclut(player);
                break;
            case "list":
                handleList(player);
                break;
            default:
                sendHelp(player);
        }

        return true;
    }

    private void handleEvite(Player player) {
        ItemStack itemInHand = player.getInventory().getItemInMainHand();

        if (itemInHand.getType() == Material.AIR) {
            player.sendMessage(configManager.getMessage("no-item-hand"));
            return;
        }

        Material material = itemInHand.getType();

        if (lootManager.isExcluded(material)) {
            player.sendMessage(configManager.getMessage("already-excluded"));
            return;
        }

        lootManager.excludeItem(material);
        String message = configManager.getMessage("excluded")
            .replace("%item%", material.toString());
        player.sendMessage(message);
    }

    private void handleInclut(Player player) {
        ItemStack itemInHand = player.getInventory().getItemInMainHand();

        if (itemInHand.getType() == Material.AIR) {
            player.sendMessage(configManager.getMessage("no-item-hand"));
            return;
        }

        Material material = itemInHand.getType();

        if (!lootManager.isExcluded(material)) {
            player.sendMessage(configManager.getMessage("not-excluded"));
            return;
        }

        lootManager.includeItem(material);
        String message = configManager.getMessage("included")
            .replace("%item%", material.toString());
        player.sendMessage(message);
    }

    private void handleList(Player player) {
        Set<String> excluded = lootManager.getExcludedItems();

        if (excluded.isEmpty()) {
            player.sendMessage("§6[BILAL] §aAucun item exclu!");
            return;
        }

        String items = excluded.stream()
            .collect(Collectors.joining("§f, §e"));

        String message = configManager.getMessage("list-excluded")
            .replace("%items%", items);
        player.sendMessage(message);
    }

    private void sendHelp(Player player) {
        player.sendMessage("§6§m" + "════════════════════════════════════════".repeat(0));
        player.sendMessage("§6[BILAL] Commandes disponibles:");
        player.sendMessage("§f/bilal start §7- Commencer le loot aléatoire");
        player.sendMessage("§f/bilal stop §7- Arrêter le loot aléatoire");
        player.sendMessage("§f/bilal evite §7- Exclure l'item en main");
        player.sendMessage("§f/bilal inclut §7- Réintégrer l'item en main");
        player.sendMessage("§f/bilal list §7- Voir les items exclus");
        player.sendMessage("§6§m" + "════════════════════════════════════════".repeat(0));
    }
}
