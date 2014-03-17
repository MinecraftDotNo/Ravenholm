package no.blockwork.protection;

import com.google.common.collect.Lists;
import no.blockwork.blockwork.Blockwork;
import no.blockwork.groups.Group;
import no.blockwork.mybb.MyBb;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.material.Bed;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.BlockIterator;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

final class ProtectionListener implements Listener {
    private final Blockwork plugin;
    private final Protection protection;

    public ProtectionListener(final Blockwork pluginInstance, final Protection protectionInstance) {
        plugin = pluginInstance;
        protection = protectionInstance;
    }

    @EventHandler
    public void onBlockPlace(final BlockPlaceEvent event) {
        final Block block = event.getBlock();
        final Player player = event.getPlayer();

        final MyBb mybb = plugin.getMyBb();

        if (mybb.isUserGuest(player)) {
            event.setCancelled(true);
            return;
        }

        Block against = event.getBlockAgainst();
        if (block.getLocation().equals(against.getLocation())) {
            BlockIterator bi = new BlockIterator(
                    player.getWorld(),
                    block.getLocation().toVector(),
                    player.getLocation().getDirection().normalize(),
                    0,
                    1
            );
            bi.next();
            against = bi.next();
        }

        if (!protection.canUse(against, player)) {
            event.setCancelled(true);
            return;
        }

        final Material type = event.getBlockPlaced().getType();

        if (type == Material.CHEST) {
            for (final BlockFace face : Arrays.asList(BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST)) {
                final Block relative = block.getRelative(face);
                if (relative.getType() == Material.CHEST && !protection.canUse(relative, player)) {
                    event.setCancelled(true);
                    return;
                }
            }
        }

        // Never protect these blocks.
        if (type != Material.TNT &&
                type != Material.STATIONARY_LAVA &&
                type != Material.LAVA &&
                type != Material.STATIONARY_WATER &&
                type != Material.WATER &&
                type != Material.LONG_GRASS &&
                type != Material.RED_ROSE &&
                type != Material.YELLOW_FLOWER) {
            boolean protect = true;

            int owner = mybb.getUserId(player);
            final Group group = plugin.getGroups().getGroup(player);
            if (group != null) {
                owner = group.getUid();
            }

            // Only protect these blocks if they're placed on the player's own blocks.
            if (type == Material.TORCH ||
                    type == Material.REDSTONE_TORCH_ON ||
                    type == Material.REDSTONE_TORCH_OFF ||
                    type == Material.LEVER ||
                    type == Material.STONE_BUTTON ||
                    type == Material.WOOD_BUTTON) {
                if (protection.getBlockOwner(event.getBlockAgainst()) == 0) {
                    protect = false;
                }
            } else if (type == Material.RAILS ||
                    type == Material.POWERED_RAIL ||
                    type == Material.DETECTOR_RAIL ||
                    type == Material.REDSTONE_WIRE ||
                    type == Material.DIODE_BLOCK_OFF ||
                    type == Material.DIODE_BLOCK_ON) {
                if (protection.getBlockOwner(block.getRelative(BlockFace.DOWN)) == 0) {
                    protect = false;
                }
            }

            if (protect) {
                // Protect both blocks that make up a door.
                if (type == Material.WOODEN_DOOR || type == Material.IRON_DOOR_BLOCK) {
                    protection.setBlockOwner(block.getRelative(BlockFace.UP), owner);
                } else if (type == Material.BED_BLOCK) {
                    protection.setBlockOwner(block.getRelative(new Bed(Material.BED, block.getData()).getFacing()), owner);
                }

                protection.setBlockOwner(block, owner);
            }
        }
    }

    @EventHandler
    public void onBlockBreak(final BlockBreakEvent event) {
        final Block block = event.getBlock();

        final Player player = event.getPlayer();

        final MyBb mybb = plugin.getMyBb();

        if (mybb.isUserGuest(player)) {
            event.setCancelled(true);
            return;
        }

        final Material type = block.getType();

        if (type == Material.DIRT ||
                type == Material.GRASS ||
                type == Material.GRAVEL ||
                type == Material.SAND) {
            protection.freeBlock(block);
            return;
        }

        if ((type == Material.ENDER_PORTAL_FRAME || type == Material.MOB_SPAWNER) && !mybb.isPowerUser(player)) {
            event.setCancelled(true);
            return;
        }

        if (!protection.canUse(block, player)) {
            event.setCancelled(true);
            return;
        }

        protection.freeBlock(block);

        if (block.getType() == Material.WOODEN_DOOR || block.getType() == Material.IRON_DOOR_BLOCK) {
            // Was this the lower block of a door?
            final Block up = block.getRelative(BlockFace.UP);
            if (up.getType() == Material.WOODEN_DOOR || up.getType() == Material.IRON_DOOR_BLOCK) {
                protection.freeBlock(up);
                return;
            }

            // Was this the upper block of a door?
            final Block down = block.getRelative(BlockFace.DOWN);
            if (down.getType() == Material.WOODEN_DOOR || down.getType() == Material.IRON_DOOR_BLOCK) {
                protection.freeBlock(down);
                return;
            }
        }

        // Was this part of a bed?
        if (block.getType() == Material.BED_BLOCK) {
            final Bed bed = new Bed(block.getType(), block.getData());

            if (bed.isHeadOfBed()) {
                protection.freeBlock(block.getRelative(bed.getFacing().getOppositeFace()));
            } else {
                protection.freeBlock(block.getRelative(bed.getFacing()));
            }
        }
    }

    @EventHandler
    public void onBlockSpread(final BlockSpreadEvent event) {
        if (event.getSource().getType() == Material.FIRE) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onLeavesDecay(final LeavesDecayEvent event) {
        final Block block = event.getBlock();

        if (protection.getBlockOwner(block) != 0) {
            protection.freeBlock(block);
        }
    }

    @EventHandler
    public void onBlockFromTo(final BlockFromToEvent event) {
        final Block toBlock = event.getToBlock();

        // Let water break crops and carrots.
        final Material m = toBlock.getType();
        if (m == Material.CROPS || m == Material.CARROT) {
            protection.freeBlock(toBlock);
            return;
        }

        // Don't let water wash away protected blocks, like redstone and torches.
        if (protection.getBlockOwner(toBlock) != 0) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockIgnite(final BlockIgniteEvent event) {
        final Player player = event.getPlayer();
        final Block block = event.getBlock();

        if (player == null) {
            if (protection.getBlockOwner(block) != 0) {
                event.setCancelled(true);
            }
        } else if (!protection.canUse(block, player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockBurn(final BlockBurnEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onBlockPistonExtend(final BlockPistonExtendEvent event) {
        final Block piston = event.getBlock();
        final int pistonOwner = protection.getBlockOwner(piston);
        List<Block> blocks = event.getBlocks();

        // Can we move all of these blocks?
        for (final Block b : blocks) {
            final int blockOwner = protection.getBlockOwner(b);

            if (blockOwner != 0 && blockOwner != pistonOwner) {
                event.setCancelled(true);
                return;
            }
        }

        blocks = Lists.reverse(blocks);
        final BlockFace face = event.getDirection();

        // Move protection.
        for (final Block block : blocks) {
            protection.setBlockOwner(block.getRelative(face), pistonOwner);
            protection.freeBlock(block);
        }

        // Protect the piston head.
        protection.setBlockOwner(piston.getRelative(event.getDirection()), pistonOwner);
    }

    @EventHandler
    public void onBlockPistonRetract(final BlockPistonRetractEvent event) {
        final Block piston = event.getBlock();
        final Block retractFrom = event.getRetractLocation().getBlock();
        final Block retractTo = retractFrom.getRelative(event.getDirection().getOppositeFace());

        final int pistonOwner = protection.getBlockOwner(piston);
        final int retractFromOwner = protection.getBlockOwner(retractFrom);

        protection.freeBlock(retractTo);

        if (event.isSticky()) {
            if (retractFromOwner != 0 && retractFromOwner != pistonOwner) {
                event.setCancelled(true);
                return;
            }

            protection.freeBlock(retractFrom);

            if (retractFrom.getType() != Material.AIR) {
                protection.setBlockOwner(retractTo, pistonOwner);
            }
        }
    }

    @EventHandler
    public void onEntityExplode(final EntityExplodeEvent event) {
        final Location location = event.getLocation();
        if (location.getWorld().getSpawnLocation().distance(location) < plugin.getConfig().getInt("mobs.spawn-safe-radius")) {
            event.blockList().clear();
            return;
        }

        event.setYield(100);

        final Iterator<Block> blocks = event.blockList().iterator();

        while (blocks.hasNext()) {
            Block block = blocks.next();

            if (protection.getBlockOwner(block) != 0) {
                blocks.remove();
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(final PlayerInteractEvent event) {
        if (!event.hasBlock()) {
            return;
        }

        final Player player = event.getPlayer();

        if (plugin.getMyBb().isUserGuest(player)) {
            event.setCancelled(true);
            return;
        }

        final Block block = event.getClickedBlock();
        final Material material = block.getType();

        if (event.getAction() == Action.PHYSICAL && material == Material.SOIL) {
            event.setCancelled(true);
            return;
        }

        if (material == Material.FURNACE ||
                material == Material.BURNING_FURNACE ||
                material == Material.CHEST ||
                material == Material.ENDER_CHEST ||
                material == Material.ANVIL ||
                material == Material.STONE_PLATE ||
                material == Material.STONE_BUTTON ||
                material == Material.DRAGON_EGG ||
                material == Material.DISPENSER) {
            if (!protection.canUse(block, player)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onEntityInteract(final EntityInteractEvent event) {
        if (!(event.getEntity() instanceof Player) && event.getBlock().getType() == Material.STONE_PLATE) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onChunkUnload(final ChunkUnloadEvent event) {
        protection.getCache().remove(event.getChunk());
    }

    @EventHandler
    public void onStructureGrow(final StructureGrowEvent event) {
        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            @Override
            public void run() {
                for (final BlockState state : event.getBlocks()) {
                    protection.freeBlock(state.getBlock());
                }
            }
        });
    }

    @EventHandler
    public void onEntityChangeBlock(final EntityChangeBlockEvent event) {
        if (
                event.getEntity() instanceof Enderman ||
                        event.getEntity() instanceof Wither
                ) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityBreakDoor(final EntityBreakDoorEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onEntityDamageByEntity(final EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();

        if (damager instanceof Projectile) {
            ProjectileSource source = ((Projectile) damager).getShooter();

            if (source instanceof Entity) {
                damager = (Entity) source;
            }
        }

        if (!(damager instanceof Player)) {
            return;
        }

        final Entity victim = event.getEntity();

        switch (victim.getType()) {
            case WOLF:
                final Wolf wolf = (Wolf) victim;

                if (wolf.isTamed()) {
                    event.setCancelled(true);
                    wolf.setTarget((Player) damager);
                }
                break;
            case COW:
            case MUSHROOM_COW:
            case OCELOT:
            case SHEEP:
            case PIG:
                if (!protection.canUse(victim.getLocation().getBlock().getRelative(BlockFace.DOWN), (Player) damager)) {
                    event.setCancelled(true);
                }
                break;
            case PLAYER:
                final Location l = victim.getLocation();
                final World w = l.getWorld();
                final World.Environment env = w.getEnvironment();

                if (
                        env != World.Environment.THE_END &&
                                env != World.Environment.NETHER &&
                                l.distanceSquared(w.getSpawnLocation()) < Math.pow(plugin.getConfig().getInt("mobs.spawn-safe-radius"), 2)
                        ) {
                    event.setCancelled(true);
                }
                break;
        }
    }

    @EventHandler
    public void onEntityTarget(final EntityTargetEvent event) {
        final Entity e = event.getTarget();

        if (e == null) {
            return;
        }

        final Location l = e.getLocation();
        final World w = l.getWorld();
        final World.Environment env = w.getEnvironment();

        if (
                env != World.Environment.THE_END &&
                        env != World.Environment.NETHER &&
                        l.distanceSquared(w.getSpawnLocation()) < Math.pow(plugin.getConfig().getInt("mobs.spawn-safe-radius"), 2)
                ) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBucketEmpty(PlayerBucketEmptyEvent event) {
        Player player = event.getPlayer();
        Block clicked = event.getBlockClicked();
        Block block = clicked.getRelative(event.getBlockFace());

        if (!protection.canUse(clicked, player) || !protection.canUse(block, player)) {
            event.setCancelled(true);
        }
    }
}
