package de.rexlmanu.elevator.plugin;

import de.rexlmanu.elevator.plugin.listener.ElevatorListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class ElevatorPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        Bukkit.getPluginManager().registerEvents(new ElevatorListener(this), this);
    }
}
