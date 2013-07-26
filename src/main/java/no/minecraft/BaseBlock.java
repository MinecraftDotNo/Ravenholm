package no.minecraft;

import org.bukkit.block.Block;

public interface BaseBlock {
    public void setBlock(Block block);

    public Block getBlock();

    public void setOwner(BasePlayer player);

    public BasePlayer getOwner();

    public void clearOwner();
}
