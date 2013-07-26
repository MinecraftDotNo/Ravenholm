package no.blockwork.tools.commands;

import no.blockwork.blockwork.Blockwork;
import no.blockwork.tools.Tools;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public final class ToolsToolsCommand implements CommandExecutor {
    private final Blockwork plugin;
    private final Tools tools;

    public final Map<String, ItemStack[]> inventories;

    public ToolsToolsCommand(final Blockwork pluginInstance, final Tools toolsInstance) {
        plugin = pluginInstance;
        tools = toolsInstance;

        inventories = new HashMap<>();
    }

    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (!(sender instanceof Player)) {
            return false;
        }

        final Player player = (Player) sender;

        if (!plugin.getMyBb().isPowerUser(player)) {
            return true;
        }

        if (!inventories.containsKey(player.getName())) {
            enableTools(player);
        } else {
            disableTools(player);
        }

        return true;
    }

    public void enableTools(final Player player) {
        final Inventory inventory = player.getInventory();

        inventories.put(player.getName(), inventory.getContents().clone());
        inventory.clear();

        for (final Material material : tools.getTools().keySet()) {
            inventory.addItem(new ItemStack(material, 1));
        }

        player.sendMessage(ChatColor.GRAY + "Du har nå adminverktøy.");
    }

    public void disableTools(final Player player) {
        if (inventories.containsKey(player.getName())) {
            final Inventory inventory = player.getInventory();

            inventory.clear();

            for (final ItemStack stack : inventories.get(player.getName())) {
                if (stack != null) {
                    inventory.addItem(stack);
                }
            }

            inventories.remove(player.getName());

            player.sendMessage(ChatColor.GRAY + "Du har nå dine egne eiendeler.");
        }
    }
}
