package no.blockwork.tools;

import no.blockwork.blockwork.Blockwork;
import no.blockwork.tools.commands.*;
import no.blockwork.tools.tools.ToolsSelectionTool;
import no.blockwork.tools.tools.ToolsTeleportTool;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

import java.util.HashMap;
import java.util.Map;

public class Tools {
    private final Blockwork plugin;

    private final Map<Material, Tool> tools;

    private final ToolsListener toolsListener;

    private ToolsSelectionTool toolsSelectionTool;
    private ToolsTeleportTool toolsTeleportTool;

    private final ToolsToolsCommand toolsToolsCommand;
    private final ToolsButcherCommand toolsButcherCommand;
    private final ToolsFlyCommand toolsFlyCommand;
    private final ToolsMobCommand toolsMobCommand;
    private final ToolsTpCommand toolsTpCommand;
    private final ToolsTpbCommand toolsTpbCommand;
    private final ToolsTphCommand toolsTphCommand;
    private final ToolsGotoCommand toolsGotoCommand;
    private final ToolsSetSpawnCommand toolsSetSpawnCommand;

    public Tools(Blockwork pluginInstance) {
        plugin = pluginInstance;

        tools = new HashMap<>();

        toolsListener = new ToolsListener(plugin, this);

        toolsToolsCommand = new ToolsToolsCommand(plugin, this);
        toolsButcherCommand = new ToolsButcherCommand(plugin, this);
        toolsFlyCommand = new ToolsFlyCommand(plugin, this);
        toolsMobCommand = new ToolsMobCommand(plugin, this);
        toolsTpCommand = new ToolsTpCommand(plugin, this);
        toolsTpbCommand = new ToolsTpbCommand(plugin, this);
        toolsTphCommand = new ToolsTphCommand(plugin, this);
        toolsGotoCommand = new ToolsGotoCommand(plugin, this);
        toolsSetSpawnCommand = new ToolsSetSpawnCommand(plugin, this);
    }

    public void onEnable() {
        // These two tools have external dependencies that might not be available when the class constructor
        // is invoked. This means they must be in onEnable().
        toolsSelectionTool = new ToolsSelectionTool(plugin, this);
        toolsTeleportTool = new ToolsTeleportTool(plugin, this);

        plugin.getServer().getPluginManager().registerEvents(toolsListener, plugin);

        plugin.getCommand("tools").setExecutor(toolsToolsCommand);
        plugin.getCommand("killall").setExecutor(toolsButcherCommand);
        plugin.getCommand("fly").setExecutor(toolsFlyCommand);
        plugin.getCommand("mob").setExecutor(toolsMobCommand);
        plugin.getCommand("tp").setExecutor(toolsTpCommand);
        plugin.getCommand("tpb").setExecutor(toolsTpbCommand);
        plugin.getCommand("tph").setExecutor(toolsTphCommand);
        plugin.getCommand("goto").setExecutor(toolsGotoCommand);
        plugin.getCommand("setspawn").setExecutor(toolsSetSpawnCommand);

        registerTool(Material.WOOD_AXE, toolsSelectionTool);
        registerTool(Material.COMPASS, toolsTeleportTool);
    }

    public void onDisable() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            toolsToolsCommand.disableTools(player);
        }

        unregisterTool(Material.COMPASS);
        unregisterTool(Material.WOOD_AXE);

        plugin.getCommand("setspawn").setExecutor(null);
        plugin.getCommand("goto").setExecutor(null);
        plugin.getCommand("tph").setExecutor(null);
        plugin.getCommand("tpb").setExecutor(null);
        plugin.getCommand("tp").setExecutor(null);
        plugin.getCommand("mob").setExecutor(null);
        plugin.getCommand("fly").setExecutor(null);
        plugin.getCommand("killall").setExecutor(null);
        plugin.getCommand("tools").setExecutor(null);

        HandlerList.unregisterAll(toolsListener);
    }

    public Map<Material, Tool> getTools() {
        return tools;
    }

    public ToolsSelectionTool getToolsSelectionTool() {
        return toolsSelectionTool;
    }

    public ToolsToolsCommand getToolsToolsCommand() {
        return toolsToolsCommand;
    }

    public ToolsTpCommand getToolsTpCommand() {
        return toolsTpCommand;
    }

    public void registerTool(Material material, Tool tool) {
        tools.put(material, tool);
    }

    public void unregisterTool(Material material) {
        tools.remove(material);
    }
}
