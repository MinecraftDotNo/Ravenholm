package no.blockwork.tools;

import org.bukkit.event.player.PlayerInteractEvent;

public abstract class Tool {
    public final void trigger(final PlayerInteractEvent event) {
        switch (event.getAction()) {
            case LEFT_CLICK_AIR:
                leftClickAir(event);
                break;
            case LEFT_CLICK_BLOCK:
                leftClickBlock(event);
                break;
            case RIGHT_CLICK_AIR:
                rightClickAir(event);
                break;
            case RIGHT_CLICK_BLOCK:
                rightClickBlock(event);
                break;
        }
    }

    protected abstract void leftClickAir(final PlayerInteractEvent event);

    protected abstract void leftClickBlock(final PlayerInteractEvent event);

    protected abstract void rightClickAir(final PlayerInteractEvent event);

    protected abstract void rightClickBlock(final PlayerInteractEvent event);
}
