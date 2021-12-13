package pizzaaxx.btescriptools;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.LinkedHashMap;
import java.util.Map;

import static pizzaaxx.btescriptools.terraform.terraformCommand.reloadTerraformConfig;

public class config implements CommandExecutor {

    public static String cfReload = "";
    public static String cfNoPermission = "";
    public static String cfNotEnabled = "";

    public static void reloadMainConfig() {
        DumperOptions options = new DumperOptions();
        options.setIndent(4);
        options.setPrettyFlow(true);
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);


        // --- Get MAP from YAML ---
        Map<String, Object> configData = null;
        File configFile = new File(Bukkit.getPluginManager().getPlugin("BTEScripTools").getDataFolder(), "config.yml");
        Yaml config = new Yaml(options);

        if (!(configFile.isFile())) {
            try {
                configFile.createNewFile();
                configData = new LinkedHashMap<>();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (configFile.length() == 0){
            configData = new LinkedHashMap<>();
        } else {

            InputStream inputStream = null;
            try {
                inputStream = new FileInputStream(configFile);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            configData = config.load(inputStream);

        }

        // --- Write stuff to the MAP ---
        Map<String, String> configMessages = new LinkedHashMap<>();
        if (configData.containsKey("messages")) {
            configMessages = (Map<String, String>) configData.get("messages");
        }

        if (!(configMessages.containsKey("reload"))) {
            configMessages.put("reload", "The configuration has been reloaded.");
        }
        if (!(configMessages.containsKey("no_permission"))) {
            configMessages.put("no_permission", "&cYou do not have permission to do this.");
        }
        if (!(configMessages.containsKey("not_enabled"))) {
            configMessages.put("not_enabled", "&cThis command is not enabled.");
        }
        configData.put("messages", configMessages);

        // Write MAP to YAML
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(configFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        config.dump(configData, writer);

        // --- Write VARIABLES ---
        cfReload = configMessages.get("reload");
        cfNoPermission = configMessages.get("no_permission");
        cfNotEnabled = configMessages.get("not_enabled");
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (cmd.getName().equalsIgnoreCase("btescriptools")){
            if (args.length > 0) {
                if (args[0].equalsIgnoreCase("reload")) {
                    if (sender.hasPermission("btescriptools.commands.reload")) {
                        reloadMainConfig();
                        reloadTerraformConfig();
                        sender.sendMessage("[§9BTEScripTools§f] §7>> §r" + cfReload);
                    } else {
                        sender.sendMessage(cfNoPermission.replace("&", "§"));
                    }

                    }
                }
            }

        return true;
    }
}
