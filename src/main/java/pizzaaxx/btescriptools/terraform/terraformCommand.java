package pizzaaxx.btescriptools.terraform;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import pizzaaxx.btescriptools.main;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.*;

import static pizzaaxx.btescriptools.config.cfNoPermission;
import static pizzaaxx.btescriptools.config.cfNotEnabled;
import static pizzaaxx.btescriptools.terraform.anchorPoints.anchorPoints;
import static pizzaaxx.btescriptools.terraform.borderPoints.terraformPoints;

public class terraformCommand implements CommandExecutor {

    public static boolean tfEnable = true;
    public static int tfMaxArea = 150000;
    public static String tfPrefix = "";
    public static String tfAnchorToolName = "";
    public static String tfBorderToolName = "";
    public static String tfAnchorAddedPoint = "";
    public static String tfBorderAddedPoint = "";
    public static String tfDesel = "";
    public static String tfNoPoints = "";
    public static String tfLessThanThreePoints = "";
    public static String tfConfirmation = "";
    public static String tfCalculating = "";
    public static String tfAreaTooLarge = "";
    public static String tfSuccess = "";

    public static void reloadTerraformConfig(){
        DumperOptions options = new DumperOptions();
        options.setIndent(4);
        options.setPrettyFlow(true);
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);


        // --- Get MAP from YAML ---
        Map<String, Object> terraformData = null;
        File terraformFile = new File(Bukkit.getPluginManager().getPlugin("BTEScripTools").getDataFolder(), "terraform.yml");
        Yaml terraformConfig = new Yaml(options);

        if (!(terraformFile.isFile())) {
            try {
                terraformFile.createNewFile();
                terraformData = new LinkedHashMap<String, Object>();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (terraformFile.length() == 0){
            terraformData = new LinkedHashMap<String, Object>();
        } else {

            InputStream inputStream = null;
            try {
                inputStream = new FileInputStream(terraformFile);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            terraformData = terraformConfig.load(inputStream);

        }

        // --- Write stuff to the MAP ---
        if (!(terraformData.containsKey("enable"))) {
            terraformData.put("enable", true);
        }
        if (!(terraformData.containsKey("max_area"))) {
            terraformData.put("max_area", 150000);
        }
        if (!(terraformData.containsKey("prefix"))) {
            terraformData.put("prefix", "&f[&2TERRAFORM&f] &7>>");
        }



        Map<String, String> terraformMessages = new LinkedHashMap<>();
        if (terraformData.containsKey("messages")) {
            terraformMessages = (Map<String, String>) terraformData.get("messages");
        }


        if (!(terraformMessages.containsKey("anchor_tool_name"))) {
            terraformMessages.put("anchor_tool_name", "&dLeft click to add anchor points");
        }
        if (!(terraformMessages.containsKey("border_tool_name"))) {
            terraformMessages.put("border_tool_name", "&dLeft click to add border points");
        }
        if (!(terraformMessages.containsKey("anchor_added_points"))) {
            terraformMessages.put("anchor_added_points", "Added anchor point at &a$coordinates$&f.");
        }
        if (!(terraformMessages.containsKey("border_added_point"))) {
            terraformMessages.put("border_added_point", "Added border point &a$number$&f at &a$coordinates$&f.");
        }
        if (!(terraformMessages.containsKey("desel"))) {
            terraformMessages.put("desel", "Deselected all points.");
        }
        if (!(terraformMessages.containsKey("no_points"))) {
            terraformMessages.put("no_points", "You have not selected any points.");
        }
        if (!(terraformMessages.containsKey("less_than_three_points"))) {
            terraformMessages.put("less_than_three_points", "You have to select a minimum of 3 border points.");
        }
        if (!(terraformMessages.containsKey("confirmation"))) {
            terraformMessages.put("confirmation", "&cYou can not undo this action. &fUse the command again to confirm.");
        }
        if (!(terraformMessages.containsKey("area_too_large"))) {
            terraformMessages.put("area_too_large", "The area selected is too large. &7Selected: $selected$ / Max: $max$.");
        }
        if (!(terraformMessages.containsKey("calculating"))) {
            terraformMessages.put("calculating", "Calculating...");
        }
        if (!(terraformMessages.containsKey("success"))) {
            terraformMessages.put("success", "Operation completed succesfully.");
        }
        terraformData.put("messages", terraformMessages);

        // Write MAP to YAML
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(terraformFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        terraformConfig.dump(terraformData, writer);

        // Write VARIABLES
        tfEnable = (Boolean) terraformData.get("enable");
        tfMaxArea = (int) terraformData.get("max_area");
        tfPrefix = (String) terraformData.get("prefix");

        tfAnchorToolName = terraformMessages.get("anchor_tool_name");
        tfBorderToolName = terraformMessages.get("border_tool_name");
        tfAnchorAddedPoint = terraformMessages.get("anchor_added_points");
        tfBorderAddedPoint = terraformMessages.get("border_added_point");
        tfDesel = terraformMessages.get("desel");
        tfNoPoints = terraformMessages.get("no_points");
        tfLessThanThreePoints = terraformMessages.get("less_than_three_points");
        tfConfirmation = terraformMessages.get("confirmation");
        tfCalculating = terraformMessages.get("calculating");
        tfAreaTooLarge = terraformMessages.get("area_too_large");
        tfSuccess = terraformMessages.get("success");

    }

    public static Map<Player, Boolean> terraformConfirm = new HashMap<>();

    public static boolean isDoubleParsable(String input) {
        try {
            Double.parseDouble(input);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isBooleanParsable(String input) {
        return input.equals("true") || input.equals("false");
    }

    public static boolean linesIntersect(Location loc1, Location loc2, Location loc3, Location loc4) {
        double x1 = loc1.getX();
        double y1 = loc1.getZ();
        double x2 = loc2.getX();
        double y2 = loc2.getZ();
        double x3 = loc3.getX();
        double y3 = loc3.getZ();
        double x4 = loc4.getX();
        double y4 = loc4.getZ();

        // --- Function of Line 1 ---

        double m1 = (y2 - y1)/(x2 - x1);
        double n1 = y1 - (m1 * x1);

        // --- Check if Line 2 is perpendicular ---

        if (x3 == x4) {
            double y = (m1 * x3) + n1;

            if (Math.min(x1, x2) <= x3 && x3 <= Math.max(x1, x2)) {
                if (Math.min(y1, y2) <= y && y <= Math.max(y1, y2)) {
                    return Math.min(y3, y4) <= y && y <= Math.max(y3, y4);
                }
                return false;
            }
            return false;
        }

        // --- Function of Line 2 ---

        double m2 = (y4 - y3)/(x4 -x3);
        double n2 = y3 - (m2 * x3);

        // --- Check if they're parallel

        if (m1 == m2) {
            return n1 == n2;
        }

        // --- Find intersection point of functions ---

        double x = (n2 - n1)/(m1 - m2);
        double y = (m1 * x) + n1;

        // --- Check whether it's within the lines domain ---

        if (Math.min(x1, x2) <= x && x <= Math.max(x1, x2)) {
            if (Math.min(y1, y2) <= y && y <= Math.max(y1, y2)) {
                if (Math.min(x3, x4) <= x && x <= Math.max(x3, x4)) {
                    return Math.min(y3, y4) <= y && y <= Math.max(y3, y4);
                }
                return false;
            }
            return false;
        }
        return false;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (cmd.getName().equalsIgnoreCase("terraform")) {
            if (tfEnable) {
                if (!(sender instanceof Player)) {
                    return true;
                }
                Player p = (Player) sender;
                if (p.hasPermission("btescriptools.commands.terraform")) {
                    boolean copy = false;
                    double steep = 2;
                    boolean cont = true;
                    if (args.length > 0) {
                        if (args[0].equalsIgnoreCase("desel")) {
                            terraformPoints.remove(p);
                            anchorPoints.remove(p);
                            p.sendMessage(tfPrefix.replace("&", "§") + " §r" + tfDesel.replace("&", "§"));
                            cont = false;
                        } else if (args[0].equalsIgnoreCase("wand")) {
                            PlayerInventory inventory = p.getInventory();
                            ItemStack diamond = new ItemStack(Material.DIAMOND_PICKAXE);
                            ItemMeta dMeta = diamond.getItemMeta();
                            dMeta.setDisplayName(tfBorderToolName.replace("&", "§"));
                            diamond.setItemMeta(dMeta);
                            inventory.addItem(diamond);
                            ItemStack gold = new ItemStack(Material.GOLDEN_PICKAXE);
                            ItemMeta gMeta = gold.getItemMeta();
                            gMeta.setDisplayName(tfAnchorToolName.replace("&", "§"));
                            gold.setItemMeta(gMeta);
                            inventory.addItem(gold);
                            cont = false;
                        } else {
                            if (isDoubleParsable(args[0])) {
                                steep = Double.parseDouble(args[0]);
                                if (args.length > 1) {
                                    if (isBooleanParsable(args[1])) {
                                        copy = Boolean.parseBoolean(args[1]);
                                    }
                                }
                            } else if (isBooleanParsable(args[0])) {
                                copy = Boolean.parseBoolean(args[0]);
                            }
                        }
                    }
                    if (cont) {
                        if (!(terraformPoints.containsKey(p))) {
                            p.sendMessage(tfPrefix.replace("&", "§") + " §r" + tfNoPoints.replace("&", "§"));
                        } else if (terraformPoints.get(p).size() < 3) {
                            p.sendMessage(tfPrefix.replace("&", "§") + " §r" + tfLessThanThreePoints.replace("&", "§"));
                        } else {
                            if (!(terraformConfirm.containsKey(p))) {
                                p.sendMessage(tfPrefix.replace("&", "§") + " §r" + tfConfirmation.replace("&", "§"));
                                terraformConfirm.put(p, true);
                                Bukkit.getScheduler().runTaskLater(main.getPlugin(main.class), () -> terraformConfirm.remove(p), 200);
                            } else {
                                terraformConfirm.remove(p);
                                p.sendMessage(tfPrefix.replace("&", "§") + " §r" + tfCalculating.replace("&", "§"));
                                double xMax = terraformPoints.get(p).get(0).getX();
                                double xMin = terraformPoints.get(p).get(0).getX();
                                double zMax = terraformPoints.get(p).get(0).getZ();
                                double zMin = terraformPoints.get(p).get(0).getZ();
                                for (Location point : terraformPoints.get(p)) {
                                    if (point.getX() > xMax) {
                                        xMax = point.getX();
                                    }
                                    if (point.getX() < xMin) {
                                        xMin = point.getX();
                                    }
                                    if (point.getZ() > zMax) {
                                        zMax = point.getZ();
                                    }
                                    if (point.getZ() < zMin) {
                                        zMin = point.getZ();
                                    }
                                }
                                double totalArea = ((Math.abs(xMax-xMin))*(Math.abs(zMax-zMin)));
                                if (totalArea > 20000) {
                                    p.sendMessage(tfPrefix.replace("&", "§") + " §r" + tfAreaTooLarge.replace("&", "§").replace("$selected$", String.valueOf(totalArea)).replace("$max$", String.valueOf(tfMaxArea)));
                                } else {
                                    List<Location> points = new ArrayList(terraformPoints.get(p));
                                    points.add(terraformPoints.get(p).get(0));

                                    List<Location> pointsPlusAnchors = new ArrayList(terraformPoints.get(p));
                                    if (anchorPoints.containsKey(p)) {
                                        pointsPlusAnchors.addAll(new ArrayList(anchorPoints.get(p)));
                                    }
                                    double numerator;
                                    double denominator;
                                    for (double x = xMin+1; x < xMax; x++) {
                                        for (double z = zMin+1; z < zMax; z++) {
                                            Location loc = new Location(p.getWorld(), x, 100, z);
                                            double counter = 0;
                                            for (int i = 0; i < points.size() - 1; i++) {
                                                Location loc2 = new Location(p.getWorld(), xMax + 5, 100, z);
                                                Location loc3 = points.get(i);
                                                Location loc4 = points.get(i+1);
                                                if (linesIntersect(loc, loc2, loc3, loc4)) {
                                                    counter = counter + 1;
                                                }
                                            }
                                            if (counter % 2 != 0) {
                                                // #######
                                                numerator = 0;
                                                denominator = 0;
                                                for (Location point : pointsPlusAnchors) {
                                                    double w = 1/(Math.pow(loc.distance(new Location(p.getWorld(), point.getX(), 100, point.getZ())), steep));
                                                    numerator = numerator + (w * (point.getY() + 0.5));
                                                    denominator = denominator + w;
                                                }
                                                double v = numerator/denominator;
                                                double minV = numerator/denominator;
                                                // #########
                                                numerator = 0;
                                                denominator = 0;
                                                for (Location point : pointsPlusAnchors) {
                                                    double w = 1/(Math.pow(new Location(p.getWorld(), loc.getX()+1, 100, loc.getZ()).distance(new Location(p.getWorld(), point.getX(), 100, point.getZ())), steep));
                                                    numerator = numerator + (w * (point.getY() + 0.5));
                                                    denominator = denominator + w;
                                                }
                                                if (numerator/denominator < minV) {
                                                    minV = numerator/denominator;
                                                }
                                                // #########
                                                numerator = 0;
                                                denominator = 0;
                                                for (Location point : pointsPlusAnchors) {
                                                    double w = 1/(Math.pow(new Location(p.getWorld(), loc.getX()-1, 100, loc.getZ()).distance(new Location(p.getWorld(), point.getX(), 100, point.getZ())), steep));
                                                    numerator = numerator + (w * (point.getY() + 0.5));
                                                    denominator = denominator + w;
                                                }
                                                if (numerator/denominator < minV) {
                                                    minV = numerator/denominator;
                                                }
                                                // #########
                                                numerator = 0;
                                                denominator = 0;
                                                for (Location point : pointsPlusAnchors) {
                                                    double w = 1/(Math.pow(new Location(p.getWorld(), loc.getX()+1, 100, loc.getZ()+1).distance(new Location(p.getWorld(), point.getX(), 100, point.getZ())), steep));
                                                    numerator = numerator + (w * (point.getY() + 0.5));
                                                    denominator = denominator + w;
                                                }
                                                if (numerator/denominator < minV) {
                                                    minV = numerator/denominator;
                                                }
                                                // #########
                                                numerator = 0;
                                                denominator = 0;
                                                for (Location point : pointsPlusAnchors) {
                                                    double w = 1/(Math.pow(new Location(p.getWorld(), loc.getX(), 100, loc.getZ()-1).distance(new Location(p.getWorld(), point.getX(), 100, point.getZ())), steep));
                                                    numerator = numerator + (w * (point.getY() + 0.5));
                                                    denominator = denominator + w;
                                                }
                                                if (numerator/denominator < minV) {
                                                    minV = numerator/denominator;
                                                }
                                                // #########
                                                Material m = Material.GOLD_BLOCK;
                                                if (copy) {
                                                    m = Material.GRASS_BLOCK;
                                                    if (p.getWorld().getHighestBlockAt(loc).getType() == Material.BRICKS || p.getWorld().getHighestBlockAt(loc).getType() == Material.GRAY_CONCRETE  || p.getWorld().getHighestBlockAt(loc).getType() == Material.GRASS_BLOCK || p.getWorld().getHighestBlockAt(loc).getType() == Material.GRASS_PATH ) {
                                                        m = p.getWorld().getHighestBlockAt(loc).getType();
                                                    }
                                                }
                                                p.getWorld().getBlockAt(new Location(p.getWorld(), x, v, z)).setType(m);
                                                for (double j = minV; j < v; j++) {
                                                    p.getWorld().getBlockAt(new Location(p.getWorld(), x, j, z)).setType(m);
                                                }
                                            }
                                        }
                                    }
                                    p.sendMessage(tfPrefix.replace("&", "§") + " §r" + tfSuccess.replace("&", "§"));
                                    terraformPoints.remove(p);
                                    anchorPoints.remove(p);
                                }
                            }
                        }
                    }
                } else {
                    p.sendMessage(cfNoPermission.replace("&", "§"));
                }
            } else {
                sender.sendMessage(tfPrefix + " §r" + cfNotEnabled);
            }

        }
        return true;
    }
}
