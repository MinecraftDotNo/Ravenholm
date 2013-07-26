package no.blockwork.lumberjack;

import no.blockwork.blockwork.Blockwork;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.inventory.ItemStack;

public class Lumberjack implements Listener {
    private Blockwork plugin;
    private BlockFace[] checkFaces = {
            BlockFace.NORTH,
            BlockFace.EAST,
            BlockFace.SOUTH,
            BlockFace.WEST,
            BlockFace.NORTH_EAST,
            BlockFace.SOUTH_EAST,
            BlockFace.SOUTH_WEST,
            BlockFace.NORTH_WEST,
            BlockFace.UP
    };

    public Lumberjack(Blockwork pluginInstance) {
        plugin = pluginInstance;
    }

    public void onEnable() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void onDisable() {
        HandlerList.unregisterAll(this);
    }

    private void nest(Block block) {
        if (plugin.getProtection().getBlockOwner(block) != 0) {
            return;
        }

        Material type = block.getType();
        block.breakNaturally();

        for (BlockFace face : checkFaces) {
            Block near = block.getRelative(face);

            switch (near.getType()) {
                case LEAVES:
                    if (type != Material.LEAVES) {
                        nest(near);
                    }
                    break;

                case LOG:
                    nest(near);
                    break;
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        final Block block = event.getBlock();

        if (block.getType() != Material.LOG) {
            return;
        }

        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            @Override
            public void run() {
                nest(block);
            }
        });
    }

    @EventHandler
    public void onItemDespawn(ItemDespawnEvent event) {
        ItemStack item = event.getEntity().getItemStack();

        if (item.getType() == Material.SAPLING) {
            Block block = event.getLocation().getBlock();

            switch (block.getType()) {
                case AIR:
                case SNOW:
                case LONG_GRASS:
                    break;
                default:
                    return;
            }

            switch (block.getRelative(BlockFace.DOWN).getType()) {
                case DIRT:
                case GRASS:
                    block.setType(Material.SAPLING);
                    block.setData(item.getData().getData());
            }
        }
    }
}
