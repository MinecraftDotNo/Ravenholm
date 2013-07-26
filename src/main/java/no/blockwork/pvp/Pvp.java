package no.blockwork.pvp;

import no.blockwork.blockwork.Blockwork;
import no.blockwork.utils.MapUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class Pvp implements Listener {
    private final Blockwork plugin;
    private final Map<Player, Player> challenges;
    private final Map<Player, Player> fights;

    public Pvp(final Blockwork pluginInstance) {
        plugin = pluginInstance;

        challenges = new HashMap<>();
        fights = new HashMap<>();
    }

    public void onEnable() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void onDisable() {

    }

    @EventHandler
    public void onDamage(final EntityDamageByEntityEvent event) {
        final Entity rxe = event.getEntity();
        Entity txe = event.getDamager();

        if (txe instanceof Projectile) {
            txe = ((Projectile) txe).getShooter();
        }

        if (!(rxe instanceof Player) || !(txe instanceof Player) || txe == rxe) {
            return;
        }

        event.setCancelled(true);

        final Player rxp = (Player) rxe;
        final Player txp = (Player) txe;

        if (!(fights.containsKey(txp) && fights.get(txp) == rxp) && !(fights.containsKey(rxp) && fights.get(rxp) == txp)) {
            switch (txp.getItemInHand().getType()) {
                case WOOD_SWORD:
                case STONE_SWORD:
                case IRON_SWORD:
                case GOLD_SWORD:
                case DIAMOND_SWORD:
                    break;
                default:
                    return;
            }

            switch (rxp.getItemInHand().getType()) {
                case WOOD_SWORD:
                case STONE_SWORD:
                case IRON_SWORD:
                case GOLD_SWORD:
                case DIAMOND_SWORD:
                    break;
                default:
                    return;
            }
        }

        if (challenges.containsKey(txp)) {
            txp.sendMessage(ChatColor.RED + "Du har alt invitert " + challenges.get(txp).getDisplayName() + ChatColor.RED + " til duell!");
            return;
        }

        if (challenges.containsValue(txp)) {
            txp.sendMessage(ChatColor.RED + "Du er alt invitert til duell av " + ((Player) MapUtils.getInverse(challenges, txp).get(0)).getDisplayName() + ChatColor.RED + "!");
            return;
        }

        if (challenges.containsKey(rxp)) {
            txp.sendMessage(rxp.getDisplayName() + ChatColor.RED + " har alt invitert noen til duell!");
            return;
        }

        if (challenges.containsValue(rxp)) {
            txp.sendMessage(rxp.getDisplayName() + ChatColor.RED + " er alt invitert til duell av noen.");
            return;
        }

        if (fights.containsKey(txp)) {
            if (fights.get(txp).getName().equals(rxp.getName())) {
                event.setCancelled(false);
                return;
            }

            txp.sendMessage(ChatColor.RED + "Du er alt i duell med " + fights.get(txp).getDisplayName() + ChatColor.RED + "!");
            return;
        }

        if (fights.containsValue(txp)) {
            final Player enemy = (Player) MapUtils.getInverse(fights, txp).get(0);

            if (enemy.getName().equals(rxp.getName())) {
                event.setCancelled(false);
                return;
            }

            txp.sendMessage(ChatColor.RED + "Du er alt i duell med " + enemy.getDisplayName() + ChatColor.RED + "!");
            return;
        }

        if (fights.containsKey(rxp)) {
            if (fights.get(rxp).getName().equals(txp.getName())) {
                event.setCancelled(false);
                return;
            }

            txp.sendMessage(rxp.getDisplayName() + ChatColor.RED + " er alt i duell med noen!");
            return;
        }

        if (fights.containsValue(rxp)) {
            final Player enemy = (Player) MapUtils.getInverse(fights, rxp).get(0);

            if (enemy.getName().equals(txp.getName())) {
                event.setCancelled(false);
                return;
            }

            txp.sendMessage(rxp.getDisplayName() + ChatColor.RED + " er alt i duell med noen!");
            return;
        }

        challenges.put(txp, rxp);
        txp.sendMessage(ChatColor.GREEN + "Du har utfordret " + rxp.getDisplayName() + ChatColor.GREEN + " til duell!");
        rxp.sendMessage(txp.getDisplayName() + ChatColor.GREEN + " utfordrer deg til duell!");
    }

    @EventHandler
    public void onPlayerInteractEntity(final PlayerInteractEntityEvent event) {
        final Player player = event.getPlayer();

        switch (player.getItemInHand().getType()) {
            case WOOD_SWORD:
            case STONE_SWORD:
            case IRON_SWORD:
            case GOLD_SWORD:
            case DIAMOND_SWORD:
                break;
            default:
                return;
        }

        final Entity entity = event.getRightClicked();

        if (!(entity instanceof Player)) {
            return;
        }

        final Player otherPlayer = (Player) entity;

        if (challenges.containsValue(player)) {
            final Player challenger = (Player) MapUtils.getInverse(challenges, player).get(0);

            if (challenger != otherPlayer) {
                return;
            }

            challenges.remove(challenger);
            fights.put(challenger, player);

            challenger.sendMessage(player.getDisplayName() + ChatColor.GREEN + " godtok din utfordring!");
            player.sendMessage(ChatColor.GREEN + "Du godtok utfordringen fra " + challenger.getDisplayName() + ChatColor.GREEN + "!");
        }
    }

    @EventHandler
    public void onPlayerInteract(final PlayerInteractEvent event) {
        final Player player = event.getPlayer();

        if (challenges.containsValue(player)) {
            final Player challenger = (Player) MapUtils.getInverse(challenges, player).get(0);
            challenges.remove(challenger);

            challenger.sendMessage(player.getDisplayName() + ChatColor.RED + " avviste din utfordring.");
            player.sendMessage(ChatColor.RED + "Du avviste utfordringen fra " + challenger.getDisplayName() + ChatColor.RED + ".");
        }
    }

    @EventHandler
    public void onQuit(final PlayerQuitEvent event) {
        final Player player = event.getPlayer();

        if (challenges.containsKey(player)) {
            challenges.get(player).sendMessage(player.getDisplayName() + ChatColor.RED + " trakk seg.");
            challenges.remove(player);
        }

        if (challenges.containsValue(player)) {
            final Player challenger = (Player) MapUtils.getInverse(challenges, player).get(0);
            challenger.sendMessage(player.getDisplayName() + ChatColor.RED + " trakk seg.");
            challenges.remove(challenger);
        }

        if (fights.containsKey(player)) {
            fights.get(player).sendMessage(player.getDisplayName() + ChatColor.RED + " trakk seg.");
            fights.remove(player);
        }

        if (fights.containsValue(player)) {
            final Player challenger = (Player) MapUtils.getInverse(fights, player).get(0);
            challenger.sendMessage(player.getDisplayName() + ChatColor.RED + " trakk seg.");
            fights.remove(challenger);
        }
    }

    @EventHandler
    public void onPlayerDeath(final PlayerDeathEvent event) {
        final Player player = event.getEntity();

        boolean dropSkull = false;

        if (fights.containsKey(player)) {
            player.sendMessage(ChatColor.RED + "Du tapte duellen.");
            fights.get(player).sendMessage(ChatColor.GREEN + "Du vant duellen!");

            fights.remove(player);

            dropSkull = true;
        }

        if (fights.containsValue(player)) {
            final Player enemy = (Player) MapUtils.getInverse(fights, player).get(0);

            player.sendMessage(ChatColor.RED + "Du tapte duellen.");
            enemy.sendMessage(ChatColor.GREEN + "Du vant duellen!");

            fights.remove(enemy);

            dropSkull = true;
        }

        if (dropSkull) {
            final ItemStack skullStack = new ItemStack(Material.SKULL_ITEM);
            final SkullMeta skullMeta = (SkullMeta) skullStack.getItemMeta();

            final List<String> lore = new ArrayList<>();
            lore.add("Drept av " + player.getKiller().getName());
            lore.add(new Date(System.currentTimeMillis()).toString());

            skullStack.setDurability((short) SkullType.PLAYER.ordinal());

            skullMeta.setDisplayName(player.getName());
            skullMeta.setOwner(player.getName());
            skullMeta.setLore(lore);

            skullStack.setItemMeta(skullMeta);
            player.getWorld().dropItem(player.getLocation(), skullStack);
        }
    }
}
