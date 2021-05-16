package me.mavis.essentials;

import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

public final class Essentials extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup logic
        //lấy file config mặc định
        getConfig().options().copyDefaults(true);
        saveConfig();

        getCommand("it").setExecutor(new EnchantCommand(this));

        getServer().getConsoleSender().sendMessage(ChatColor.GOLD + "Enchantment Command is ready!");

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getServer().getConsoleSender().sendMessage(ChatColor.GOLD + "Enchantment Command is offline!");
    }
}
