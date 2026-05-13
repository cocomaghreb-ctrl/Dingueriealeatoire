package fr.bilal.dinguerie.managers;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class ConfigManager {

    private final JavaPlugin plugin;
    private File configFile;
    private File exclusionFile;
    private FileConfiguration config;
    private FileConfiguration exclusionConfig;
    private Set<String> excludedItems;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.excludedItems = new HashSet<>();
    }

    public void loadConfig() {
        // Créer le dossier du plugin s'il n'existe pas
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        // Charger le fichier de configuration principal
        configFile = new File(plugin.getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            plugin.saveResource("config.yml", false);
        }
        config = YamlConfiguration.loadConfiguration(configFile);

        // Charger le fichier d'exclusion
        exclusionFile = new File(plugin.getDataFolder(), "exclusions.yml");
        if (!exclusionFile.exists()) {
            try {
                exclusionFile.createNewFile();
                exclusionConfig = new YamlConfiguration();
                exclusionConfig.set("excluded-items", new ArrayList<>());
                exclusionConfig.save(exclusionFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        exclusionConfig = YamlConfiguration.loadConfiguration(exclusionFile);
        
        // Charger les items exclus en mémoire
        List<String> items = exclusionConfig.getStringList("excluded-items");
        excludedItems.addAll(items);

        plugin.getLogger().info("Configuration chargée avec succès!");
    }

    public String getMessage(String path) {
        return config.getString("messages." + path, "§cMessage non trouvé");
    }

    public int getIntervalSeconds() {
        return config.getInt("interval-seconds", 30);
    }

    public void addExcludedItem(String itemName) {
        excludedItems.add(itemName.toUpperCase());
        saveExclusions();
    }

    public void removeExcludedItem(String itemName) {
        excludedItems.remove(itemName.toUpperCase());
        saveExclusions();
    }

    public boolean isItemExcluded(String itemName) {
        return excludedItems.contains(itemName.toUpperCase());
    }

    public Set<String> getExcludedItems() {
        return new HashSet<>(excludedItems);
    }

    private void saveExclusions() {
        try {
            exclusionConfig.set("excluded-items", new ArrayList<>(excludedItems));
            exclusionConfig.save(exclusionFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
