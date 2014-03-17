package no.blockwork.blockwork;

import me.desht.dhutils.ExperienceManager;
import no.blockwork.mybb.MyBb;
import no.blockwork.protection.Protection;
import no.blockwork.utils.InventoryUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.util.List;

public class BlockworkListener implements Listener {
    private final Blockwork plugin;

    public BlockworkListener(Blockwork pluginInstance) {
        plugin = pluginInstance;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        MyBb mybb = plugin.getMyBb();

        if (mybb.isUserGuest(player)) {
            player.sendMessage(ChatColor.GOLD + "Velkommen til Ravenholm, " + player.getName() + ".");
            player.sendMessage(ChatColor.GOLD + "Du er for øyeblikket gjest. Besøk http://wiki.minecraft.no/ for informasjon om registrering.");
        }

        for (PermissionAttachmentInfo info : player.getEffectivePermissions()) {
            PermissionAttachment att = info.getAttachment();

            if (att != null) {
                att.unsetPermission(info.getPermission());
            }
        }

        switch (mybb.getUserGroup(player)) {
            case ADMINISTRATOR:
                player.addAttachment(plugin, "blockwork.admin", true);
                break;

            case SUPER_MODERATOR:
                player.addAttachment(plugin, "blockwork.supermod", true);
                break;

            case MODERATOR:
                player.addAttachment(plugin, "blockwork.mod", true);
                break;

            case REGISTERED:
                player.addAttachment(plugin, "blockwork.user", true);
                break;

            case AWAITING_ACTIVATION:
                player.addAttachment(plugin, "blockwork.guest", true);
                break;
        }

        plugin.getChat().tagPlayer(player);

        if (!player.hasPlayedBefore()) {
            InventoryUtils.addOrSpawn(player, new ItemStack(Material.WOOD_SWORD));
            InventoryUtils.addOrSpawn(player, new ItemStack(Material.WOOD_PICKAXE));
            InventoryUtils.addOrSpawn(player, new ItemStack(Material.BREAD, 3));
        }
    }

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        LivingEntity entity = event.getEntity();

        if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.CUSTOM) {
            return;
        }

        if (entity.getType() == EntityType.SLIME && entity.getLocation().getBlockY() > 60) {
            event.setCancelled(true);
            return;
        }

        FileConfiguration config = plugin.getConfig();

        int r = config.getInt("mobs.check-radius");
        List<Entity> nearby = entity.getNearbyEntities(r / 2, r / 2, r / 2);
        int max = config.getInt("mobs.limit");
        if (nearby.size() >= max) {
            event.setCancelled(true);
        }
    }


    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!event.hasBlock()) {
            return;
        }

        Player p = event.getPlayer();
        Block b = event.getClickedBlock();

        Protection protection = plugin.getProtection();

        // Ender chest inventory sharing.
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && b.getType() == Material.ENDER_CHEST && protection.canUse(b, p)) {
            int uid = protection.getBlockOwner(b);
            if (uid != 0) {
                Player owner = plugin.getServer().getPlayerExact(plugin.getMyBb().getUserName(uid));
                if (owner != null) {
                    event.setCancelled(true);
                    p.openInventory(owner.getEnderChest());
                }
            }
            return;
        }

        Location l = b.getLocation();

        // Ender dragon spawning.
        if (l.getWorld().getEnvironment() == World.Environment.THE_END && b.getType() == Material.DRAGON_EGG) {
            event.setCancelled(true);
            b.setType(Material.AIR);
            l.getWorld().spawnEntity(l, EntityType.ENDER_DRAGON);
            return;
        }

        ItemStack hand = p.getItemInHand();

        // Experience bottle crafting.
        if (
                b.getType() == Material.ENCHANTMENT_TABLE &&
                        hand != null &&
                        hand.getType() == Material.GLASS_BOTTLE
                ) {
            ExperienceManager e = new ExperienceManager(p);
            if (e.hasExp(10)) {
                if (hand.getAmount() > 1) {
                    hand.setAmount(hand.getAmount() - 1);
                } else {
                    p.setItemInHand(null);
                }
                e.changeExp(-10);
                InventoryUtils.addOrSpawn(p, new ItemStack(Material.EXP_BOTTLE));
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        for (HumanEntity e : event.getPlayer().getEnderChest().getViewers()) {
            e.closeInventory();
        }
    }
}
