package de.rexlmanu.elevator.plugin.listener;

import de.rexlmanu.elevator.plugin.ElevatorPlugin;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerStatisticIncrementEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

public class ElevatorListener implements Listener {

    private static final String METADATA_KEY = "elevator";

    private ElevatorPlugin plugin;

    public ElevatorListener(ElevatorPlugin plugin) {
        this.plugin = plugin;
    }

    private void searchForElevatorAndTeleport(Player player, boolean reverse) {
        Block block = player.getLocation().clone().subtract(0, 1, 0).getBlock();
        // Check if block beneath player is a elevator block
        if (!block.hasMetadata(METADATA_KEY)) return;

        Location playerLocation = player.getLocation();
        // Search for blocks in the y axis
        for (int i = 1; i < plugin.getConfig().getInt("range"); i++) {
            Location blockLocation = playerLocation.clone().subtract(0, 1, 0);
            if (reverse) blockLocation.subtract(0, i, 0);
            else blockLocation.add(0, i, 0);

            if (!blockLocation.getBlock().hasMetadata(METADATA_KEY)) {
                continue;
            }
            // Found a elevator block
            Location teleportLocation = blockLocation.add(0, 1, 0);
            teleportLocation.setZ(teleportLocation.getBlockZ());
            teleportLocation.setX(teleportLocation.getBlockX());
            teleportLocation.add(teleportLocation.getBlockX() > 1 ? 0.5 : -0.5, 0, teleportLocation.getBlockZ() > 1 ? 0.5 : -0.5);
            player.teleport(teleportLocation);
            blockLocation.getWorld().playSound(blockLocation, Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1.1f);
            blockLocation.getWorld().spawnParticle(Particle.REVERSE_PORTAL, teleportLocation, 1, 0.25, 0, 0.25, 0.02);
            break;
        }
    }

    @EventHandler
    public void handleDropItem(PlayerDropItemEvent event) {
        if (!event.getItemDrop().getItemStack().getType().equals(Material.ENDER_PEARL)) return;
        Block block = event.getItemDrop().getLocation().clone().subtract(0, 2, 0).getBlock();
        if (!block.getType().name().endsWith("_WOOL") || block.hasMetadata(METADATA_KEY)) return;
        event.getItemDrop().remove();

        block.setMetadata(METADATA_KEY, new FixedMetadataValue(this.plugin, true));
        block.getLocation().getWorld().playSound(block.getLocation(), Sound.ENTITY_ENDER_EYE_LAUNCH, 1f, 1.4f);
    }

    @EventHandler
    public void handleJump(PlayerStatisticIncrementEvent event) {
        // Check if the player jumps
        if (!event.getStatistic().equals(Statistic.JUMP) || event.getPlayer().isFlying()) {
            return;
        }
        this.searchForElevatorAndTeleport(event.getPlayer(), false);
    }

    @EventHandler
    public void handleSneak(PlayerToggleSneakEvent event) {
        if (event.isCancelled() || !event.isSneaking() || event.getPlayer().isFlying()) return;

        this.searchForElevatorAndTeleport(event.getPlayer(), true);
    }

    @EventHandler
    public void handleBlockBreak(BlockBreakEvent event) {
        if (!event.getBlock().hasMetadata(METADATA_KEY) || event.isCancelled()) return;
        event.getBlock().removeMetadata(METADATA_KEY, this.plugin);
        Location location = event.getBlock().getLocation();
        location.getWorld().dropItem(location, new ItemStack(Material.ENDER_PEARL));
        location.getWorld().playSound(location, Sound.ENTITY_ENDER_EYE_DEATH, 1f, 1.4f);
    }
}
