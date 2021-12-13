package pizzaaxx.btescriptools.terraform;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static pizzaaxx.btescriptools.config.cfNotEnabled;
import static pizzaaxx.btescriptools.terraform.terraformCommand.*;

public class anchorPoints implements Listener {

    public static Map<Player, List<Location>> anchorPoints = new HashMap<>();

    @EventHandler
    public void onRightClick(PlayerInteractEvent e) {
        Player p = e.getPlayer();

        if (e.getAction() == Action.LEFT_CLICK_BLOCK) {
            ItemStack tool = p.getInventory().getItemInMainHand();
            if (tool.getType() == Material.GOLDEN_PICKAXE) {
                if (tool.getItemMeta().hasDisplayName()) {
                    if (ChatColor.stripColor(tool.getItemMeta().getDisplayName()).equals(ChatColor.stripColor(tfAnchorToolName.replace("&", "§")))) {
                        if (tfEnable) {
                            Block b = e.getClickedBlock();
                            List<Location> points;
                            if (anchorPoints.containsKey(p)) {
                                points = anchorPoints.get(p);
                            } else {
                                points = new ArrayList<>();
                            }
                            points.add(b.getLocation());
                            anchorPoints.put(p, points);

                            p.sendMessage(tfPrefix.replace("&", "§") + " §r" + tfAnchorAddedPoint.replace("&", "§").replace("$coordinates$", b.getX() + ", " + b.getY() + ", " + b.getZ()));
                            e.setCancelled(true);
                        } else {
                            p.sendMessage(tfPrefix + " §r" + cfNotEnabled);
                        }
                    }
                }
            }
        }
    }

}
