package fr.bilal.dinguerie;

import fr.bilal.dinguerie.commands.BilalCommand;
import fr.bilal.dinguerie.managers.ConfigManager;
import fr.bilal.dinguerie.managers.LootManager;
import org.bukkit.plugin.java.JavaPlugin;

public class DinguerieLoot extends JavaPlugin {

    private static DinguerieLoot instance;
    private ConfigManager configManager;
    private LootManager lootManager;

    @Override
    public void onEnable() {
        instance = this;
        
        // Créer et charger la configuration
        configManager = new ConfigManager(this);
        configManager.loadConfig();
        
        // Initialiser le gestionnaire de loot
        lootManager = new LootManager(this);
        
        // Enregistrer les commandes
        getCommand("bilal").setExecutor(new BilalCommand(this));
        
        getLogger().info("§6[DinguerieLoot] Plugin activé!");
    }

    @Override
    public void onDisable() {
        if (lootManager != null) {
            lootManager.stopLoot();
        }
        getLogger().info("§c[DinguerieLoot] Plugin désactivé!");
    }

    public static DinguerieLoot getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public LootManager getLootManager() {
        return lootManager;
    }
}
