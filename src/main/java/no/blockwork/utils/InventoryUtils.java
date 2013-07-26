package no.blockwork.utils;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public final class InventoryUtils {
    public static void addOrSpawn(final Player player, final ItemStack itemStack) {
        final Map<Integer, ItemStack> left = player.getInventory().addItem(itemStack);
        if (!left.isEmpty()) {
            for (final ItemStack stack : left.values()) {
                player.getWorld().dropItem(player.getLocation(), stack);
            }
        }
    }
}
