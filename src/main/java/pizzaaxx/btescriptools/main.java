package pizzaaxx.btescriptools;

import pizzaaxx.btescriptools.terraform.borderPoints;
import pizzaaxx.btescriptools.terraform.terraformCommand;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import pizzaaxx.btescriptools.terraform.anchorPoints;

import java.io.*;

import static pizzaaxx.btescriptools.config.reloadMainConfig;
import static pizzaaxx.btescriptools.terraform.terraformCommand.reloadTerraformConfig;

public final class main extends JavaPlugin {

    @Override
    public void onEnable() {
        getLogger().info("Enabling BTEScripTools plugin!");

        org.bukkit.Bukkit.getPluginManager().registerEvents(new borderPoints(), this);
        org.bukkit.Bukkit.getPluginManager().registerEvents(new anchorPoints(), this);

        getCommand("terraform").setExecutor(new terraformCommand());
        getCommand("btescriptools").setExecutor(new config());

        File configFolder =  new File(Bukkit.getPluginManager().getPlugin("BTEScripTools").getDataFolder(), "");
        configFolder.mkdirs();

        reloadTerraformConfig();
        reloadMainConfig();

    }

    @Override
    public void onDisable() {
        getLogger().info("Disabling BTEScripTools plugin!");
    }
}
