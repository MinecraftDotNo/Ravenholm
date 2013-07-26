package no.blockwork.tools.tools;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import com.sk89q.worldedit.regions.Region;
import no.blockwork.blockwork.Blockwork;
import no.blockwork.tools.Tool;
import no.blockwork.tools.Tools;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.ArrayList;
import java.util.Collection;

public final class ToolsSelectionTool extends Tool {
    private final Blockwork plugin;
    private final Tools tools;

    public ToolsSelectionTool(final Blockwork pluginInstance, final Tools toolsInstance) {
        plugin = pluginInstance;
        tools = toolsInstance;
    }

    @Override
    public void leftClickAir(final PlayerInteractEvent event) {

    }

    @Override
    public void leftClickBlock(final PlayerInteractEvent event) {

    }

    @Override
    public void rightClickAir(final PlayerInteractEvent event) {

    }

    @Override
    public void rightClickBlock(final PlayerInteractEvent event) {

    }

    public Collection<Block> getBlocks(final Player player) {
        final Collection<Block> blocks = new ArrayList<>();

        try {
            final Region r = plugin.getWorldEdit().getSession(player).getRegionSelector(
                    BukkitUtil.getLocalWorld(player.getWorld())
            ).getRegion();

            for (final BlockVector v : r) {
                blocks.add(player.getWorld().getBlockAt(
                        v.getBlockX(),
                        v.getBlockY(),
                        v.getBlockZ()
                ));
            }
        } catch (IncompleteRegionException exception) {

        } catch (NullPointerException exception) {

        }

        return blocks;
    }
}
