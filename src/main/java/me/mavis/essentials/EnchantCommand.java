package me.mavis.essentials;

import org.bukkit.Bukkit;
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

import javax.swing.*;
import java.util.*;

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
            return true;
        }
        Player player = (Player) sender;
        if (args.length <= 1 || args == null) {
            player.sendMessage(ChatColor.GOLD + config.getString(Details.notEnoughAgruments));
            return false;
        }
        ItemStack itemOnHand = player.getInventory().getItemInMainHand();
        if (itemOnHand == null || itemOnHand.getType().equals(Material.AIR)) {
            player.sendMessage(ChatColor.RED + "Bạn phải cầm một món nào đó trên tay!");
            return true;
        }
        ItemMeta meta = itemOnHand.getItemMeta();
        switch (args[0]) {
            //cuonghoa lore add <tên lore>
            //cuonghoa lore remove <index>
            case "lore": {
                String option = args[1].toLowerCase();
                switch (option) {
                    case "add": {
                        addLore(itemOnHand, args);
                        break;
                    }
                    case "remove": {
                        removeLore(player, itemOnHand, args);
                        break;
                    }
                    default:
                        player.sendMessage("Hiện tại chưa hỗ trợ chức năng này");
                        break;
                }

                break;
            }
            //cuonghoa en [add | remove] <tên enchantment> <level>
            //cuonghoa en add unbreakable
            case "en": {
                String option = args[1].toLowerCase();
                switch (option) {
                    case "add": {
//                        Map<Enchantment, Integer> enchantList = itemOnHand.getEnchantments();
                        if (args.length == 4) {
                            //cuonghoa en add <enchantment> <level>
                            Enchantment input = Enchantment.getByKey(NamespacedKey.minecraft(args[2].toLowerCase()));
                            if (input == null) {
                                player.sendMessage(ChatColor.RED + config.getString(Details.notHaveThatEnchantment));
                                return false;
                            }
                            int level;
                            try {
                                level = Integer.parseInt(args[3]);
                                if (level <= 0 || level > 32767) {
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
                        } else if (args.length == 3) {
                            //it en add unbreakable
                            if (args[2].toLowerCase().equals("unbreakable")) {
                                meta.setUnbreakable(true);
                                itemOnHand.setItemMeta(meta);
                                player.sendMessage(ChatColor.AQUA + "Đã thêm thuộc tính không thể vỡ");
                            } else {
                                player.sendMessage(ChatColor.RED + config.getString(Details.notHaveThatEnchantment));
                                return false;
                            }
                        } else {
                            player.sendMessage("Có gì đó sai sai");
                            return false;
                        }
                        break;
                    }
                    //cuonghoa en remove <index>
                    case "remove": {
                        if (args.length == 3) {
                            String lastArg = args[2];
                            if (lastArg.toLowerCase().equals("unbreakable")) {
                                meta.setUnbreakable(false);
                                itemOnHand.setItemMeta(meta);
                                player.sendMessage(ChatColor.AQUA + "Đã xóa thuộc tính không thể phá vỡ");
                            } else {
                                int index = tryParseUInt(lastArg);
                                if (index < 0 || index > meta.getEnchants().size()) {
                                    player.sendMessage(ChatColor.RED + "Vui lòng nhập từ 0 đến " + (meta.getEnchants().size() - 1));
                                    return true;
                                }
                                Map<Enchantment, Integer> enchants = meta.getEnchants();
                                removeEnchantment(player, itemOnHand, index);
                            }
                        } else {
                            player.sendMessage(ChatColor.RED + config.getString(Details.remove));
                        }
                        break;
                    }
                    default:
                        player.sendMessage("Hiện tại chưa hỗ trợ chức năng này");
                        break;
                }
                break;
            }
            //it name <tên>
            case "name": {
                changeName(player, itemOnHand, args);
                break;
            }
            default:
                player.sendMessage(ChatColor.GOLD + config.getString(Details.notSupportYet) + "câu lệnh "
                        + ChatColor.RED + args[0]);
                break;
        }
        return true;
    }

    private int parseIntInRange(int min, int max, String number) {
        int result = -1;
        try {
            result = Integer.parseInt(number);
            if (result < min || result > max)
                throw new Exception("outofrange");
        } catch (Exception e) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Must in " + min + ".." + max);
            result = -1;
        }
        return result;
    }

    private void changeName(Player player, ItemStack item, String[] args) {
        StringBuilder builder = new StringBuilder("");
        //it name abc itse
        //    0    1    2
        for (int i = 1; i < args.length; i++) {
            builder.append(args[i] + " ");
        }
        String name =
                ChatColor.translateAlternateColorCodes('&', builder.toString().trim());
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        item.setItemMeta(meta);
        player.sendMessage("Đã đổi tên thành " + name);
    }

    private void removeLore(Player player, ItemStack item, String[] args) {
        //it lore remove <index>
        //    0     1       2
        ItemMeta meta = item.getItemMeta();
        List<String> lores = meta.getLore();
        if (lores == null) {
            player.sendMessage(ChatColor.RED + "Món đồ hiện tại chưa có dòng lore nào!");
            return;
        }
        if (args.length == 2) { //nếu không có tham số index
            //xóa dòng lore cuối cùng
            int last = lores.size() - 1;
            lores.remove(last);
        } else {
            //xóa dòng lore ở vị trí bất kỳ
            int index = parseIntInRange(0, lores.size() - 1, args[2]);
            if (index == -1) { //thoát hàm nếu có gì đó sai sót
                player.sendMessage(ChatColor.RED + "Phải nằm trong khoảng 0.." + (lores.size() - 1));
                return;
            }
            lores.remove(index);
        }
        meta.setLore(lores);
        item.setItemMeta(meta);
    }

    private void addLore(ItemStack item, String[] args) {
        StringBuilder strBuilder = new StringBuilder("");
        //it lore add grewg wegweeg weg we gw g weg w egw egweg wegweg
        //    0    1    2     3      4   5 6  7  8  9  10  11    12
        for(int i = 2; i < args.length; i++) {
            strBuilder.append(args[i] + " ");
        }
//        String lore = strBuilder.toString().trim(); //cắt dấu cách phía sau
        String lore =
                ChatColor.translateAlternateColorCodes('&', strBuilder.toString().trim());
        ItemMeta meta = item.getItemMeta();
        List<String> lores = meta.getLore();
        if (lores == null) {
            lores = new ArrayList<>();
        }
        lores.add(lore);
        meta.setLore(lores);
        item.setItemMeta(meta); //cập nhật lại
    }

    private void removeEnchantment(Player player, ItemStack item, int index) {
        Enchantment found = getEnchantmentAt(item.getEnchantments(), index);
        if (found != null) {
            //item.getItemMeta().getEnchants().remove(found); //null pointer
            ItemMeta mitem = item.getItemMeta();
            mitem.removeEnchant(found);
            item.setItemMeta(mitem);
            player.sendMessage("Đã xóa " + found.getName() + " ra khỏi món đồ");
        } else {
            player.sendMessage(ChatColor.GOLD + "Nhập 0.." + item.getEnchantments().size() + " để xóa enchant");
        }
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
