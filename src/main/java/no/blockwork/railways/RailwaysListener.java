package no.blockwork.railways;

import no.blockwork.blockwork.Blockwork;
import no.blockwork.utils.InventoryUtils;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.event.vehicle.VehicleUpdateEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

final class RailwaysListener implements Listener {
    private final Blockwork plugin;
    private final Railways railways;

    public RailwaysListener(final Blockwork pluginInstance, final Railways railwaysInstance) {
        plugin = pluginInstance;
        railways = railwaysInstance;
    }

    @EventHandler
    public void onVehicleUpdate(final VehicleUpdateEvent event) {
        final Vehicle vehicle = event.getVehicle();

        if (!(vehicle instanceof Minecart)) {
            return;
        }

        final Location location = vehicle.getLocation();
        final Chunk chunk = location.getChunk();

        if (!chunk.isLoaded()) {
            chunk.load();
        }

        if (!vehicle.isEmpty()) {
            if (location.getBlock().getType() == Material.RAILS) {
                final Vector velocity = vehicle.getVelocity();
                final Double x = velocity.getX();
                final Double z = velocity.getZ();

                if (x > 0 && x < 1.6) {
                    velocity.setX(x + 0.1);
                }
                if (z > 0 && z < 1.6) {
                    velocity.setZ(z + 0.1);
                }

                if (x < 0 && x > -1.6) {
                    velocity.setX(x - 0.1);
                }
                if (z < 0 && z > -1.6) {
                    velocity.setZ(z - 0.1);
                }

                vehicle.setVelocity(velocity);
            }
        }
    }

    @EventHandler
    public void onVehicleExit(final VehicleExitEvent event) {
        final Vehicle v = event.getVehicle();
        final LivingEntity e = event.getExited();

        if (v instanceof Minecart && e instanceof Player) {
            v.remove();

            InventoryUtils.addOrSpawn((Player) e, new ItemStack(Material.MINECART));
        }
    }

    @EventHandler
    public void onVehicleDamage(final VehicleDamageEvent event) {
        if (event.getVehicle() instanceof Minecart && event.getAttacker() instanceof Player) {
            Entity passenger = event.getVehicle().getPassenger();

            if (passenger instanceof Player) {
                event.setCancelled(true);
            }
        }
    }
}
