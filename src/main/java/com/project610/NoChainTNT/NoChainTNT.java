package com.project610.NoChainTNT;

import com.destroystokyo.paper.event.block.TNTPrimeEvent;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;

public class NoChainTNT extends JavaPlugin implements Listener {

    final private Path pluginPath = Paths.get("plugins" + File.separator + "NoChainTNT");
    final private Path configPath = Paths.get(pluginPath + File.separator + "config.txt");
    private HashMap<Entity, Integer> spawnedTnts;
    private HashMap<String, String> config;

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        spawnedTnts = new HashMap<>();
        loadSettings();
    }

    @EventHandler
    private void onTNTPrime(TNTPrimeEvent event) {
        // Only prevent chaining caused by TNT (eg: Creepers setting off TNT don't count)
        if (event.getReason().equals(TNTPrimeEvent.PrimeReason.EXPLOSION)
                & event.getPrimerEntity() instanceof TNTPrimed) {
            int generation = 0;

            if (spawnedTnts.containsKey(event.getPrimerEntity())) {
                generation = spawnedTnts.get(event.getPrimerEntity());
            }

            // If we haven't reached max TNT generation yet, spawn in a primed TNT manually, and increment  generation
            if (generation + 1 <= getMaxTNTGeneration() || getMaxTNTGeneration() == -1) {
                event.getBlock().setType(Material.AIR);
                Entity spawnedTnt = event.getBlock().getWorld().spawnEntity(
                        event.getBlock().getLocation(), EntityType.PRIMED_TNT);
                ((TNTPrimed) spawnedTnt).setFuseTicks(20);
                spawnedTnts.put(spawnedTnt, generation + 1);
            } else {
                event.getBlock().setType(Material.TNT);
            }

            event.setCancelled(true);
        }
    }

    @EventHandler
    private boolean onPlayerCommand(PlayerCommandPreprocessEvent event) {
        String[] split = event.getMessage().split(" ");

        if (split[0].equalsIgnoreCase("/MaxTntChain")) {
            int maxTntGeneration = getMaxTNTGeneration();
            if (split.length == 1) {
                event.getPlayer().sendMessage("Max TNT Chain is " + maxTntGeneration
                        + (maxTntGeneration == -1 ? " (Infinite)" : ""));
                return true;
            } else if (event.getPlayer().hasPermission("nochaintnt.edit")) {
                try {
                    int newGen = Integer.parseInt(split[1]);
                    setMaxTNTGeneration(newGen);
                    event.getPlayer().sendMessage("Max TNT Chain set to " + newGen);
                    return true;
                } catch (NumberFormatException ex) {
                    return false;
                }
            }
        }
        return true;
    }

    private void log(String s) {
        getLogger().info(s);
    }

    private void loadSettings() {
        // Set defaults
        config = new HashMap<>();
        config.put("maxtntgeneration", "0");

        // Try to load saved config
        if (Files.exists(configPath)) {
            try {
                List<String> configLines = Files.readAllLines(configPath);
                for (String line : configLines) {
                    line.trim();
                    String[] split = line.split("=");
                    config.put(split[0], split[1]);
                }
            } catch (IOException ex) {
                log("Failed to read NoChainTNT config, reverting to default settings: " + ex);
            }
        }
    }

    private int getMaxTNTGeneration() {
        try {
            return Integer.parseInt(config.get("maxtntgeneration"));
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    private void setMaxTNTGeneration(int newGen) {
        config.put("maxtntgeneration", "" + newGen);
        saveSettings();
    }

    private void saveSettings() {
        StringBuilder configString = new StringBuilder();
        for (String key : config.keySet()) {
            configString.append(key).append("=").append(config.get(key)).append("\n");
        }
        try {
            Files.createDirectories(pluginPath);
            Files.writeString(configPath, configString.toString());
        } catch (IOException ex) {
            log("Failed to write settings to file: " + ex);
        }
    }
}
