package me.mavis.essentials;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EnchantingInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class EnchantCommand implements CommandExecutor {

    private Essentials plugin;
    private FileConfiguration config;

    public EnchantCommand(Essentials plugin) {
        this.plugin = plugin;
        config = plugin.getConfig();
    }

    private int tryParseUInt(String number) {
        int result = -1;
        try {
            result = Integer.parseInt(number);
            if (result < 0)
                throw new Exception("less than 0");
        } catch (Exception e) {
            return result;
        }
        return result;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            plugin.getServer().getConsoleSender().sendMessage(ChatColor.RED + config.getString(Details.senderError));
            return false;
        }
        Player player = (Player) sender;
        if (args.length < 1 || args == null) {
            player.sendMessage(ChatColor.GOLD + config.getString(Details.notEnoughAgruments));
            return false;
        }
        ItemStack itemOnHand = player.getInventory().getItemInMainHand();
        if (itemOnHand == null || itemOnHand.getType().equals(Material.AIR)) {
            player.sendMessage(ChatColor.RED + "Bạn phải cầm một món nào đó trên tay!");
            return false;
        }
        ItemMeta meta = itemOnHand.getItemMeta();
        switch (args[0]) {
            //cuonghoa lore add <tên lore>
            case "lore":
                player.sendMessage("Hiện tại chưa hỗ trợ chức năng này");
                break;
            //cuonghoa en [add | remove] <tên enchantment> <level>
            //cuonghoa en add unbreakable
            case "en":
                if (args.length != 4) {
                    if (args.length == 3) {
                        if (args[2].toLowerCase().equals("unbreakable")) {
                            meta.setUnbreakable(true);
                        }
                    } else {
                        player.sendMessage(ChatColor.RED + config.getString(Details.notEnoughAgruments));
                    }
                    return false;
                }
                String option = args[1].toLowerCase();
                switch (option) {
                    //cuonghoa en add <enchantment> <level>
                    case "add": {
//                        Map<Enchantment, Integer> enchantList = itemOnHand.getEnchantments();
                        Enchantment input = Enchantment.getByKey(NamespacedKey.minecraft(args[2].toLowerCase()));
                        if (input == null) {
                            player.sendMessage(ChatColor.RED + config.getString(Details.notHaveThatEnchantment));
                            return false;
                        }
                        int level;
                        try {
                            level = Integer.parseInt(args[3]);
                            if (level < 0) {
                                throw new Exception("less than 0");
                            }
                        } catch (Exception e) {
                            if (e.getMessage().equals("less than 0"))
                                player.sendMessage(ChatColor.RED + config.getString(Details.outOfRange));
                            else
                                player.sendMessage(ChatColor.RED + config.getString(Details.numberArgument));
                            return false;
                        }

                        meta.addEnchant(input, level, true);
                        itemOnHand.setItemMeta(meta);
                        player.sendMessage(ChatColor.AQUA + "Đã thêm " + ChatColor.GOLD + input.getKey() + " cấp " + level);
                        break;
                    }
                    //cuonghoa en remove <index>
                    case "remove": {
                        String lastArg = args[2];
                        int index = tryParseUInt(lastArg);
                        if (index < 0 || index > meta.getEnchants().size()) {
                            player.sendMessage(ChatColor.RED + "Vui lòng nhập từ 0 đến " + (meta.getEnchants().size() - 1));
                            return false;
                        }
                        Map<Enchantment, Integer> enchants = meta.getEnchants();
                        Enchantment found = getEnchantmentAt(enchants, index);
                        meta.removeEnchant(found);
                        break;
                    }
                    default:
                        player.sendMessage("Hiện tại chưa hỗ trợ chức năng này");
                        break;
                }
                break;
            default:
                player.sendMessage(ChatColor.GOLD + config.getString(Details.notSupportYet) + "câu lệnh "
                        + ChatColor.RED + args[0]);
                break;
        }
        return true;
    }

    private Enchantment getEnchantmentAt(Map<Enchantment, Integer> enchants, int index) {
        Iterator<Enchantment> it = enchants.keySet().iterator();
        int i = 0;
        while (it.hasNext()) {
            Enchantment found = it.next();
            if (i == index) {
                return found;
            }
            i++;
        }
        return null;
    }
}
