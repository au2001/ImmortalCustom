package me.au2001.ImmortalCustom;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.au2001.ImmortalCustom.ICBlockBreakListener.ICBlockBreakEvent;
import me.clip.ezblocks.EZBlocks;

import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;

public class ImmortalCustom extends JavaPlugin implements Listener {
	static HashMap<ItemStack, Material> materials = new HashMap<ItemStack, Material>();
	static ImmortalCustom plugin = null;
	static Plugin WE = null, WG = null, EZB = null;

	public void onEnable () {
		saveDefaultConfig();

		plugin = this;
		try {
			new MetricsLite(plugin).start();
		} catch (IOException e) {
			e.printStackTrace();
		}

		materials.put(new ItemStack(Material.DIAMOND, 9), Material.DIAMOND_BLOCK);
		materials.put(new ItemStack(Material.EMERALD, 9), Material.EMERALD_BLOCK);
		materials.put(new ItemStack(Material.GOLD_INGOT, 9), Material.GOLD_BLOCK);
		materials.put(new ItemStack(Material.IRON_INGOT, 9), Material.IRON_BLOCK);
		materials.put(new ItemStack(Material.QUARTZ, 9), Material.QUARTZ_BLOCK);
		materials.put(new ItemStack(Material.REDSTONE, 9), Material.REDSTONE_BLOCK);
		materials.put(new ItemStack(Material.COAL, 9), Material.COAL_BLOCK);
		materials.put(new ItemStack(Material.INK_SACK, 9, (short) 4), Material.LAPIS_BLOCK);

		Bukkit.getPluginManager().registerEvents(new Minebow(), this);
		Bukkit.getPluginManager().registerEvents(new Backpacks(), this);
		Bukkit.getPluginManager().registerEvents(this, this);
		getCommand("fortune").setExecutor(new Commands());

		WE = Bukkit.getPluginManager().getPlugin("WorldEdit");
		if (WE != null && !WE.isEnabled()) WE = null;
		WG = Bukkit.getPluginManager().getPlugin("WorldGuard");
		if (WG != null && !WG.isEnabled()) WG = null;
		EZB = Bukkit.getPluginManager().getPlugin("EZBlocks");
		if (EZB != null && !EZB.isEnabled()) EZB = null;

		if (WE == null) getLogger().warning("WorldEdit not found, disabling WorldEdit and WorldGuard features.");
		else if (WG == null) getLogger().warning("WorldGuard not found, disabling WorldGuard features.");
		if (EZB == null) getLogger().warning("EZBlocks not found, disabling EZBlocks features.");

		AutoUpdate.start();
	}

	public void onDisable () {
		for (UUID user : Backpacks.opens.keySet()) {
			Player player = Bukkit.getPlayer(user);
			Backpacks.save(user, player.getOpenInventory().getTopInventory(), Backpacks.opens.remove(user));
			player.closeInventory();
			player.sendMessage(ChatColor.RED + "Your backpack was closed because of a reload!");
		}
	}

	@EventHandler(priority=EventPriority.LOWEST)
	public void onBlockBreak (BlockBreakEvent event) {
		if (Utils.isActive(event.getBlock().getLocation())) {
			String nobuild = plugin.getConfig().getString("nobuild");
			if (!breakBlock(event) && nobuild != null && !nobuild.isEmpty())
				event.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', nobuild));
		}
	}

	@EventHandler(priority=EventPriority.HIGHEST)
	public void onPlayerInteract (PlayerInteractEvent event) {
		if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK) || event.getAction().equals(Action.RIGHT_CLICK_AIR)) {
			if ((event.getPlayer().hasPermission("ImmortalCustom.orestoblocks") || event.getPlayer().hasPermission("ic.orestoblocks")) && getConfig().getBoolean("orestoblocks")) {
				if (event.getPlayer().isSneaking() && !Utils.isClickable(event.getClickedBlock()) && Utils.isActive(event.getPlayer().getLocation())) {
					for (ItemStack material : materials.keySet()) {
						int ores = 0;
						for (Integer slot : event.getPlayer().getInventory().all(material.getType()).keySet()) {
							ores += event.getPlayer().getInventory().getItem(slot).getAmount();
						}
						if (ores >= material.getAmount()) {
							int blocks = (ores-ores%material.getAmount())/material.getAmount();
							event.getPlayer().getInventory().remove(material.getType());
							if (ores%material.getAmount() > 0) {
								event.getPlayer().getInventory().addItem(new ItemStack(material.getType(), ores%material.getAmount(), material.getDurability()));
							}
							event.getPlayer().getInventory().addItem(new ItemStack(materials.get(material), blocks));
							String[] conf = getConfig().getString("orestoblocksound").split(" ");
							event.getPlayer().playSound(event.getPlayer().getEyeLocation(), Sound.valueOf(conf[0]), Integer.parseInt(conf[1]), Integer.parseInt(conf[2]));
						}
					}
					event.getPlayer().updateInventory();
					event.setCancelled(true);
				}
			}
		}
	}

	@EventHandler
	public void onPlayerJoin (PlayerJoinEvent event) {
		if (event.getPlayer().hasPermission("ImmortalCustom.autoupdater") || event.getPlayer().hasPermission("ic.autoupdater")) {
			if (getConfig().getBoolean("autoupdater.admins", false) && AutoUpdate.check()) {
				String prefix = ChatColor.DARK_RED + "[" + ChatColor.RED + "ImmortalCustom" + ChatColor.DARK_RED + "]";
				event.getPlayer().sendMessage(prefix + ChatColor.GOLD + " An update is available! Download it with /fortune update.");
			}
		}
	}

	@SuppressWarnings("deprecation")
	public static boolean breakBlock (BlockBreakEvent event) {
		if (event.isCancelled()) return true;

		if (event.getPlayer().getGameMode().equals(GameMode.SURVIVAL)) {
			if (event.getBlock().getWorld().getGameRuleValue("doTileDrops").equals("true")) {
				if (WG != null) {
					WorldGuardPlugin api = (WorldGuardPlugin) WG;
					Location loc = event.getBlock().getLocation();
					ApplicableRegionSet regions = api.getRegionManager(loc.getWorld()).getApplicableRegions(loc);
					if (!regions.canBuild(api.wrapPlayer(event.getPlayer()))) {
						event.setCancelled(true);
						return false;
					}
				}

				// new BukkitRunnable() {
					// public void run() {
						event.getPlayer().sendBlockChange(event.getBlock().getLocation(), Material.AIR, (byte) 0);
						
						FileConfiguration config = plugin.getConfig();
						ICBlockBreakEvent icevent = new ICBlockBreakEvent(event);
						icevent.preBlockBreak();
						if (icevent.isCancelled()) return true;
						ItemStack hand = event.getPlayer().getItemInHand();
						icevent.tool = (hand == null || hand.getType().equals(Material.AIR))? false : Enchantment.DURABILITY.canEnchantItem(hand);
						icevent.tool = icevent.tool && !Enchantment.PROTECTION_ENVIRONMENTAL.canEnchantItem(hand) && !Enchantment.ARROW_INFINITE.canEnchantItem(hand);
						icevent.drops.addAll(event.getBlock().getDrops(event.getPlayer().getItemInHand()));

						if (Minebow.isBow(event.getPlayer().getItemInHand())) {
							icevent.minebow = true;
							icevent.drops.addAll(event.getBlock().getDrops());
						}
						if (event.getPlayer().getItemInHand().containsEnchantment(Enchantment.SILK_TOUCH)) {
							icevent.silktouch = true;
							icevent.drops.clear();
							icevent.drops.add(new ItemStack(event.getBlock().getType(), 1, event.getBlock().getData()));
							icevent.onSilkTouch();
							if (icevent.isCancelled()) return true;
						}
						
						if (icevent.tool) {
							ItemMeta meta = event.getPlayer().getItemInHand().getItemMeta();
							if (config.getBoolean("blocksbrokenlore", false)) {
								icevent.onBlockBrokenUpdate();
								if (icevent.isCancelled()) return true;
								String brokenmsg = ChatColor.translateAlternateColorCodes('&', config.getString("blocksbroken"));
								if (meta.hasLore()) {
									String[] parts = brokenmsg.split(Pattern.quote("{BLOCKS}"));
									Pattern pattern = Pattern.compile(Pattern.quote(parts[0]) + "([0-9]+)" + (parts.length > 1? Pattern.quote(parts[1]) : ""));
									for (int i = meta.getLore().size()-1; i >= 0; i--) {
										Matcher finder = pattern.matcher(meta.getLore().get(i));
										if (finder.find()) {
											List<String> lores = meta.getLore();
											long blocks = Long.parseLong(finder.group(1)) + 1;
											lores.set(i, brokenmsg.replace("{BLOCKS}", "" + blocks));
											meta.setLore(lores);
											if (config.getBoolean("blocksbrokenactionbar", false)) Utils.sendActionBar(event.getPlayer(), lores.get(i));
											if (!config.getString("blocksbrokenitemname", "").isEmpty()) {
												String brokenname = ChatColor.translateAlternateColorCodes('&', config.getString("blocksbrokenitemname"));
												if (meta.hasDisplayName()) {
													String[] nameparts = brokenname.split(Pattern.quote("{BLOCKS}"));
													String namepattern = Pattern.quote(nameparts[0]) + "[0-9]+" + (nameparts.length > 1? Pattern.quote(nameparts[1]) : "");
													Matcher match = Pattern.compile("(.*)" + namepattern).matcher(meta.getDisplayName());
													if (match.find()) meta.setDisplayName(match.group(1) + brokenname.replace("{BLOCKS}", "" + blocks));
													else meta.setDisplayName(meta.getDisplayName() + brokenname.replace("{BLOCKS}", "" + blocks));
												} else {
													String name = event.getPlayer().getItemInHand().getType().name().replace('_', ' ');
													meta.setDisplayName(WordUtils.capitalizeFully(name) + brokenname.replace("{BLOCKS}", "" + blocks));
												}
											}
											break;
										} else if (i == 0) {
											List<String> lores = meta.getLore();
											lores.add(i, brokenmsg.replace("{BLOCKS}", "1"));
											meta.setLore(lores);
											if (config.getBoolean("blocksbrokenactionbar", false)) Utils.sendActionBar(event.getPlayer(), brokenmsg.replace("{BLOCKS}", "1"));
											if (!config.getString("blocksbrokenitemname", "").isEmpty()) {
												String brokenname = ChatColor.translateAlternateColorCodes('&', config.getString("blocksbrokenitemname"));
												if (meta.hasDisplayName()) {
													String namepattern = Pattern.quote(brokenname).replace(Pattern.quote("{BLOCKS}"), "[0-9]+");
													Matcher match = Pattern.compile("(.*)" + namepattern).matcher(meta.getDisplayName());
													if (match.find()) meta.setDisplayName(match.group(1) + brokenname.replace("{BLOCKS}", "1"));
													else meta.setDisplayName(meta.getDisplayName() + brokenname.replace("{BLOCKS}", "1"));
												} else {
													String name = event.getPlayer().getItemInHand().getType().name().replace('_', ' ');
													meta.setDisplayName(WordUtils.capitalizeFully(name) + brokenname.replace("{BLOCKS}", "1"));
												}
											}
										}
									}
								} else {
									meta.setLore(Arrays.asList(brokenmsg.replace("{BLOCKS}", "1")));
									if (config.getBoolean("blocksbrokenactionbar", false)) Utils.sendActionBar(event.getPlayer(), brokenmsg.replace("{BLOCKS}", "1"));
									if (!config.getString("blocksbrokenitemname", "").isEmpty()) {
										String brokenname = ChatColor.translateAlternateColorCodes('&', config.getString("blocksbrokenitemname"));
										if (meta.hasDisplayName()) {
											String namepattern = Pattern.quote(brokenname).replace(Pattern.quote("{BLOCKS}"), "[0-9]+");
											Matcher match = Pattern.compile("(.*)" + namepattern).matcher(meta.getDisplayName());
											if (match.find()) meta.setDisplayName(match.group(1) + brokenname.replace("{BLOCKS}", "1"));
											else meta.setDisplayName(meta.getDisplayName() + brokenname.replace("{BLOCKS}", "1"));
										} else {
											String name = event.getPlayer().getItemInHand().getType().name().replace('_', ' ');
											meta.setDisplayName(WordUtils.capitalizeFully(name) + brokenname.replace("{BLOCKS}", "1"));
										}
									}
								}
							} else if (!config.getString("blocksbrokenitemname", "").isEmpty()) {
								String before = ChatColor.translateAlternateColorCodes('&', config.getString("blocksbrokenitemname"));
								if (meta.hasDisplayName()) {
									if (meta.getDisplayName().matches("(.*)" + Pattern.quote(before) + "[0-9]+")) {
										String name = meta.getDisplayName().substring(0, before.length());
										String number = meta.getDisplayName().substring(before.length());
										meta.setDisplayName(name + before + (Long.parseLong(number)+1));
									} else {
										meta.setDisplayName(meta.getDisplayName() + before + 1);
									}
								} else {
									String name = event.getPlayer().getItemInHand().getType().name().replace('_', ' ');
									meta.setDisplayName(WordUtils.capitalizeFully(name) + before + 1);
								}
							}
							event.getPlayer().getItemInHand().setItemMeta(meta);
						}
						int fortune = 0;
						if (event.getPlayer().getItemInHand().containsEnchantment(Enchantment.LOOT_BONUS_BLOCKS))
							fortune = event.getPlayer().getItemInHand().getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS);
						if (fortune >= config.getInt("smeltFortune") && (event.getPlayer().hasPermission("ImmortalCustom.autosmelt") || event.getPlayer().hasPermission("ic.autosmelt"))) {
							icevent.smelted = true;
							for (ItemStack drop : icevent.drops) {
								if (config.isSet("smelts." + drop.getType().toString())) {
									ItemStack newdrop = new ItemStack(Material.getMaterial(config.getString("smelts." + drop.getType().toString())), drop.getAmount());
									icevent.drops.set(icevent.drops.indexOf(drop), newdrop);
									event.setExpToDrop(event.getExpToDrop() + 10);
									if (config.getBoolean("flames"))
										event.getBlock().getWorld().playEffect(event.getBlock().getLocation().add(0.5, 1, 0.5), Effect.MOBSPAWNER_FLAMES, 0);
								}
							}
							icevent.onAutoSmelt();
							if (icevent.isCancelled()) return true;
						}
						if (fortune > 0 && (event.getPlayer().hasPermission("ImmortalCustom.fortune") || event.getPlayer().hasPermission("ic.fortune"))) {
							icevent.fortuned = true;
							double multiplier = 1;
							for (String rank : config.getConfigurationSection("multipliers").getValues(false).keySet())
								if (config.getDouble("multipliers." + rank) > multiplier)
									if (event.getPlayer().hasPermission("ImmortalCustom.multiplier." + rank) || event.getPlayer().hasPermission("ic.multiplier." + rank))
										multiplier = config.getDouble("multipliers." + rank);
							int loots = (int) (fortune * config.getDouble("multiplier") * multiplier + 1);
							for (ItemStack drop : icevent.drops) {
								if (config.getStringList("fortuneBlocks").contains(drop.getType().toString()) || !config.getBoolean("fortunelist")) {
									int rand = (config.getInt("difference") > 0)? new Random().nextInt(config.getInt("difference")) : 0;
									if (new Random().nextBoolean() || drop.getAmount() <= config.getInt("difference")) {
										ItemStack newdrop = drop;
										drop.setAmount((drop.getAmount() * loots)+rand);
										icevent.drops.set(icevent.drops.indexOf(drop), newdrop);
									} else {
										ItemStack newdrop = drop;
										drop.setAmount((drop.getAmount() * loots)-rand);
										icevent.drops.set(icevent.drops.indexOf(drop), newdrop);
									}
								}
							}
							icevent.onBlockFortune();
							if (icevent.isCancelled()) return true;
						}
						for (ItemStack drop : icevent.drops.toArray(new ItemStack[icevent.drops.size()])) {
							if (config.getStringList("pickupBlocks").contains(drop.getType().toString()) || !config.getBoolean("pickuplist")) {
								if ((event.getPlayer().hasPermission("ImmortalCustom.autopickup") || event.getPlayer().hasPermission("ic.autopickup")) && config.getBoolean("autopickup")) {
									if (icevent.onAutoPickup(drop)) {
										if (icevent.isCancelled()) return true;
										icevent.autopickup = true;
										Collection<ItemStack> dropp = event.getPlayer().getInventory().addItem(drop).values();
										List<ItemStack> rest = new ArrayList<ItemStack>();
										if (event.getPlayer().hasPermission("ImmortalCustom.backpack.autopickup") || event.getPlayer().hasPermission("ic.backpack.autopickup")) {
											for (ItemStack item : dropp) rest.add(Backpacks.giveItem(event.getPlayer().getUniqueId(), item));
											rest.remove(null);
										} else {
											rest.addAll(dropp);
										}
										if (rest != null && !rest.isEmpty() && icevent.onFullInventory(drop)) {
											icevent.full = true;
											if ((event.getPlayer().hasPermission("ImmortalCustom.dropdown") || event.getPlayer().hasPermission("ic.dropdown")) && config.getBoolean("dropdown")) {
												if (icevent.onDropDown(drop)) {
													if (icevent.isCancelled()) return true;
													icevent.dropdown = true;
													for (ItemStack item : rest) event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), item);
												}
											} else {
												String[] conf = config.getString("fullsound").split(" ");
												event.getPlayer().playSound(event.getPlayer().getEyeLocation(), Sound.valueOf(conf[0]), Integer.parseInt(conf[1]), Integer.parseInt(conf[2]));
												if (!config.getString("fullmessage").isEmpty())
													Utils.sendActionBar(event.getPlayer(), ChatColor.translateAlternateColorCodes('&', config.getString("fullmessage")));
											}
										}
									}
								} else if (icevent.onDropDown(drop)) {
									if (icevent.isCancelled()) return true;
									event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), drop);
								}
							} else if (icevent.onDropDown(drop)) {
								if (icevent.isCancelled()) return true;
								event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), drop);
							}
							if (icevent.isCancelled()) return true;
						}
						if (event.getExpToDrop() > 0) {
							icevent.expdrop = true;
							if ((event.getPlayer().hasPermission("ImmortalCustom.autopickup") || event.getPlayer().hasPermission("ic.autopickup")) && config.getBoolean("autopickup"))
								event.getPlayer().giveExp(event.getExpToDrop());
							else event.getBlock().getWorld().spawn(event.getBlock().getLocation(), ExperienceOrb.class).setExperience(event.getExpToDrop());
						}
						new BukkitRunnable() {
							public void run() {
								event.getBlock().setType(Material.AIR);
							}
						}.runTask(plugin);
						event.getBlock().getWorld().playEffect(event.getBlock().getLocation(), Effect.STEP_SOUND, event.getBlock().getType(), 1);
						if (icevent.tool) {
							ItemStack item = event.getPlayer().getItemInHand();
							if (!item.containsEnchantment(Enchantment.DURABILITY))
								item.setDurability((short) (item.getDurability()+1));
							else if (new Random().nextInt(item.getEnchantmentLevel(Enchantment.DURABILITY)) == 0)
								item.setDurability((short) (item.getDurability()+1));
							if (item.getDurability() >= item.getType().getMaxDurability()) {
								icevent.itembreak = true;
								Bukkit.getPluginManager().callEvent(new PlayerItemBreakEvent(event.getPlayer(), item));
								item.setType(Material.AIR);
								icevent.onItemBreak();
								if (icevent.isCancelled()) return true;
							}
							event.getPlayer().setItemInHand(item);
							event.getPlayer().updateInventory();
						}
						if (EZB != null) ((EZBlocks) EZB).getBreakHandler().handleBlockBreakEvent(event.getPlayer(), event.getBlock());
						icevent.onBlockBreak();
					// }
				// }.runTaskAsynchronously(plugin);
				event.setCancelled(true);
			}
		}
		return true;
	}
}