package no.blockwork.portals;

import no.blockwork.blockwork.Blockwork;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPortalEvent;

import java.util.Arrays;

public final class Portals implements Listener {
    private final Blockwork plugin;

    public Portals(final Blockwork pluginInstance) {
        plugin = pluginInstance;
    }

    public void onEnable() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void onDisable() {
        HandlerList.unregisterAll(this);
    }

    @EventHandler
    public void onPortal(final PlayerPortalEvent event) {
        switch (event.getCause()) {
            case NETHER_PORTAL:
                onPortalNether(event);
                break;

            case END_PORTAL:
                if (event.getPlayer().getWorld().getEnvironment() != World.Environment.THE_END) {
                    onPortalEnd(event);
                } else {
                    onPortalEndReturn(event);
                }
                break;
        }
    }

    public void onPortalNether(final PlayerPortalEvent event) {
        event.getPortalTravelAgent().setCreationRadius(0);

        final Location to = event.getTo();
        final Location from = event.getFrom();

        to.setX(from.getX());
        to.setZ(from.getZ());

        event.setTo(to);
    }

    public void onPortalEnd(final PlayerPortalEvent event) {
        Location from = event.getFrom();

        if (from.getBlock().getType() != Material.ENDER_PORTAL) {
            event.setCancelled(true);
            return;
        }

        for (final BlockFace face : Arrays.asList(BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST)) {
            final Block relative = from.getBlock().getRelative(face);

            if (relative.getType() == Material.ENDER_PORTAL_FRAME) {
                from = from.getBlock().getRelative(face.getOppositeFace()).getLocation();
            }
        }

        final String name = "world_the_end_" + from.getBlockX() + "_" + from.getBlockZ();
        World end = plugin.getServer().getWorld(name);
        if (end == null) {
            final WorldCreator creator = new WorldCreator(name);
            creator.copy(plugin.getServer().getWorld("world_the_end"));
            end = creator.createWorld();
        }

        final Location to = event.getTo();
        to.setWorld(end);
        event.setTo(to);
    }

    public void onPortalEndReturn(final PlayerPortalEvent event) {
        event.setCancelled(true);
        event.getPlayer().teleport(plugin.getServer().getWorld("world").getSpawnLocation());
    }
}
