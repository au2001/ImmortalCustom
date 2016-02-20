package me.au2001.ImmortalCustom;

import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

public class Minebow implements Listener {

	public static ItemStack arrow, bow;
	public static String prefix;

	public Minebow () {
		prefix = ImmortalCustom.plugin.getConfig().getString("minebow.prefix");
		prefix = ChatColor.translateAlternateColorCodes('&', prefix);

		bow = new ItemStack(Material.BOW);
		ItemMeta meta = bow.getItemMeta();
		String name = ImmortalCustom.plugin.getConfig().getString("minebow.name");
		meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
		bow.setItemMeta(meta);

		arrow = new ItemStack(Material.ARROW);
		meta = arrow.getItemMeta();
		name = ImmortalCustom.plugin.getConfig().getString("minebow.arrow");
		meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
		arrow.setItemMeta(meta);
	}

	public static void updateBows (Player player, int arrows) {
		for (ItemStack item : player.getInventory().getContents())
			if (item != null && item.isSimilar(arrow)) arrows += item.getAmount();

		for (ItemStack item : player.getInventory().getContents()) {
			if (isBow(item)) {
				ItemMeta meta = item.getItemMeta();
				String name = bow.getItemMeta().getDisplayName();
				meta.setDisplayName(name.replace("{ARROWS}", "" + arrows));
				item.setItemMeta(meta);
			}
		}
	}

	@EventHandler
	public void onEntityShootBow (EntityShootBowEvent event) {
		if (event.getEntity() instanceof Player && event.getProjectile() instanceof Arrow) {
			if (event.getBow() != null && event.getBow().getType().equals(bow.getType())) {
				ItemMeta meta = event.getBow().getItemMeta();
				String name = bow.getItemMeta().getDisplayName();
				if (meta.hasDisplayName() && meta.getDisplayName().startsWith(name.split("\\{ARROWS}")[0])) {
					if (meta.getDisplayName().endsWith(name.split("\\{ARROWS}")[1])) {
						updateBows((Player) event.getEntity(), -1);
						MetadataValue value = new FixedMetadataValue(ImmortalCustom.plugin, true);
						event.getProjectile().setMetadata("MineBow-Arrow", value);
					}
				}
			}
		}
	}

	@EventHandler
	public void onPlayerItemHeld (PlayerItemHeldEvent event) {
		ItemStack item = event.getPlayer().getInventory().getItem(event.getNewSlot());
		if (item != null && item.getType().equals(bow.getType())) {
			ItemMeta meta = item.getItemMeta();
			String name = bow.getItemMeta().getDisplayName();
			if (meta.hasDisplayName() && meta.getDisplayName().startsWith(name.split("\\{ARROWS}")[0])) {
				if (meta.getDisplayName().endsWith(name.split("\\{ARROWS}")[1])) {
					updateBows(event.getPlayer(), 0);
				}
			}
		}
	}

	@EventHandler
	public void onProjectileHit (ProjectileHitEvent event) {
		if (event.getEntity().hasMetadata("MineBow-Arrow") && event.getEntity().getShooter() != null) {
			if (event.getEntity().getShooter() instanceof Player && ((Player) event.getEntity().getShooter()).isOnline()) {
				Player shooter = (Player) event.getEntity().getShooter();
				Location origin = event.getEntity().getLocation();
				if (Utils.isActive(origin)) origin.getWorld().playEffect(origin, Effect.EXPLOSION_HUGE, 1);
				int r = ImmortalCustom.plugin.getConfig().getInt("minebow.radius");
				for (int x = origin.getBlockX()-r; x <= origin.getBlockX()+r; x++) {
					for (int y = origin.getBlockY()-r; y <= origin.getBlockY()+r; y++) {
						for (int z = origin.getBlockZ()-r; z <= origin.getBlockZ()+r; z++) {
							Block block = origin.getWorld().getBlockAt(x, y, z);
							if (origin.distanceSquared(block.getLocation()) <= r*r && Utils.isActive(origin)) {
								origin.getWorld().playEffect(block.getLocation(), Effect.EXPLOSION_LARGE, 1);
								if (!block.getType().equals(Material.AIR))
									ImmortalCustom.breakBlock(new BlockBreakEvent(block, shooter));
							}
						}
					}
				}
			}
			event.getEntity().remove();
		}
	}

	@EventHandler
	public void onPlayerDropItem (PlayerDropItemEvent event) {
		if (event.getItemDrop().getItemStack().isSimilar(arrow))
			updateBows(event.getPlayer(), 0);
	}

	@EventHandler
	public void onPlayerPickupItem (PlayerPickupItemEvent event) {
		if (event.getItem().getItemStack().isSimilar(arrow))
			updateBows(event.getPlayer(), 1);
	}

	public static boolean isBow (ItemStack item) {
		if (item != null && item.getType().equals(bow.getType())) {
			ItemMeta meta = item.getItemMeta();
			String name = bow.getItemMeta().getDisplayName();
			if (meta.getDisplayName().startsWith(name.split("\\{ARROWS}")[0]))
				if (meta.getDisplayName().endsWith(name.split("\\{ARROWS}")[1]))
					return true;
		}
		return false;
	}

}